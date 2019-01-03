package com.ixale.starparse.gui.main;

import java.io.File;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatSelection;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.RaidRequest;
import com.ixale.starparse.domain.RankClass;
import com.ixale.starparse.domain.stats.AbsorptionStats;
import com.ixale.starparse.domain.stats.ChallengeStats;
import com.ixale.starparse.domain.stats.CombatEventStats;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.gui.FlashMessage;
import com.ixale.starparse.gui.FlashMessage.Type;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.dialog.RaidNotesDialogPresenter;
import com.ixale.starparse.gui.dialog.RaidNotesDialogPresenter.RaidNotesListener;
import com.ixale.starparse.gui.popout.RaidNotesPopoutPresenter;
import com.ixale.starparse.gui.table.CharacterCellFactory;
import com.ixale.starparse.gui.table.NumberCellFactory;
import com.ixale.starparse.gui.table.RaidTimeCellFactory;
import com.ixale.starparse.gui.table.RankCellFactory;
import com.ixale.starparse.gui.table.TableResizer;
import com.ixale.starparse.gui.table.item.BaseItem;
import com.ixale.starparse.gui.table.item.RaidItem;
import com.ixale.starparse.parser.Helpers;
import com.ixale.starparse.raid.RaidManager;
import com.ixale.starparse.service.RaidService;
import com.ixale.starparse.service.RankService;
import com.ixale.starparse.service.RankService.RankType;
import com.ixale.starparse.time.TimeUtils;
import com.ixale.starparse.timer.TimerManager;
import com.ixale.starparse.timer.TimerManager.RaidBreakTimer;
import com.ixale.starparse.timer.TimerManager.RaidPullTimer;
import com.ixale.starparse.ws.RaidClient.RequestIncomingCallback;
import com.ixale.starparse.ws.RaidClient.RequestOutgoingCallback;
import com.ixale.starparse.ws.RaidCombatMessage;
import com.ixale.starparse.ws.RaidRequestMessage;
import com.ixale.starparse.ws.RaidResponseMessage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class RaidPresenter extends BaseCombatLogPresenter {

	private static final Logger logger = LoggerFactory.getLogger(RaidPresenter.class);

	private static final int MAX_UPDATE_AGE = 15 * 60 * 1000;

	@FXML
	private Label raidTitle, raidPlayers, raidDeathsTitle, damageTaken, healingTaken;

	@FXML
	private TableView<RaidItem> raidTable;
	@FXML
	private TableColumn<RaidItem, String> nameCol;
	@FXML
	private TableColumn<RaidItem, Integer> damageCol, dpsCol, raidThreatCol, tpsCol, damageTakenCol, dtpsCol, apsCol,
		healingCol, hpsCol, ehpsCol, pctEffCol, shieldingCol, spsCol, raidTimeCol, rankCol;

	@FXML
	private HBox raidBar, raidDeaths, combatLogFilter;
	@FXML
	private Button pullButton, breakButton, raidNotesButton;

	private final List<String> players = new ArrayList<String>();

	private String raidGroupName, characterName, combatLogName;
	private boolean isAdmin = false;

	// context
	private final Map<Long, Integer> combatBeginnings = new LinkedHashMap<Long, Integer>();
	private Combat currentCombat = null;
	private Combat lastCombat = null;
	private final Map<RaidRequest, Label> raidDeathLabels = new HashMap<>();
	private List<Event> currentEvents;

	private final HashMap<String, List<AbsorptionStats>> absorptions = new HashMap<String, List<AbsorptionStats>>();

	@Inject
	private RaidNotesDialogPresenter raidNotesDialogPresenter;
	@Inject
	private RaidNotesPopoutPresenter raidNotesPopoutPresenter;

	@Autowired
	private RaidService raidService;
	@Autowired
	private RankService rankService;
	// manually injected
	private RaidManager raidManager;
	// manually injected
	private Config config;

	public interface RaidDataListener {

		void onRaidDataUpdate(Combat combat, RaidCombatMessage message);

		void onRaidDataFinalize();
	}

	private final HashSet<RaidDataListener> listeners = new HashSet<RaidDataListener>();

	public void addRaidUpdateListener(final RaidDataListener listener) {
		listeners.add(listener);
	}

	public void setConfig(final Config config) {
		this.config = config;
		rankService.initialize(config.getServerHost());

		raidNotesDialogPresenter.setListener(new RaidNotesListener() {
			@Override
			public void onPreview(String note) {
				raidNotesPopoutPresenter.updateNoteIfNeeded(note, true);
			}

			@Override
			public void onSave(String note) {
				if (raidNotesPopoutPresenter.updateNoteIfNeeded(note, false)) {
					// broadcast
					final RaidRequest request = new RaidRequest(RaidRequest.Type.RAID_NOTES, null,
						new RaidRequest.Params(TimeUtils.getCurrentTime(), note));

					handleAnnouncement(raidNotesButton, request);
				}
			}
		});
	}

	public void setRaidManager(final RaidManager rm) {
		this.raidManager = rm;
	}

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		initializeRaidTable();
		initializeCombatLogTable();
		hideDeathRecap();

		reloadTitle();

		pullButton.setTooltip(new Tooltip("Broadcast a raid-wide pull (see 'Settings -> Raiding')"));
		breakButton.setTooltip(new Tooltip("Broadcast a raid-wide break (see 'Settings -> Raiding')"));
		raidNotesButton.setTooltip(new Tooltip("Broadcast a raid-wide notes (displayed on the Raid Notes overlay)"));
	}

	private void initializeRaidTable() {
		nameCol.setCellValueFactory(new PropertyValueFactory<RaidItem, String>("name"));
		raidTimeCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("time"));

		damageCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("damage"));
		dpsCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("dps"));
		raidThreatCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("threat"));
		tpsCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("tps"));
		damageTakenCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("damageTaken"));
		dtpsCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("dtps"));
		apsCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("aps"));
		healingCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("healing"));
		hpsCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("hps"));
		ehpsCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("ehps"));
		pctEffCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("pctEffective"));
		shieldingCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("shielding"));
		spsCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("sps"));
		rankCol.setCellValueFactory(new PropertyValueFactory<RaidItem, Integer>("rank"));

		nameCol.setCellFactory(new CharacterCellFactory<RaidItem>());
		raidTimeCol.setCellFactory(new RaidTimeCellFactory());

		damageCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "maroon", true));
		dpsCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "maroon"));

		raidThreatCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "goldenrod", true));
		tpsCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "goldenrod"));

		damageTakenCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "maroon", true));
		dtpsCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "maroon"));
		apsCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "0x30cccd"));

		healingCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "DarkSeaGreen", true));
		hpsCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "DarkSeaGreen"));
		ehpsCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "limegreen"));
		pctEffCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "limegreen"));

		shieldingCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "0x30cccd", true));
		spsCol.setCellFactory(new NumberCellFactory<RaidItem>(false, "0x30cccd"));
		rankCol.setCellFactory(new RankCellFactory<RaidItem>());

		raidTable.setRowFactory(new Callback<TableView<RaidItem>, TableRow<RaidItem>>() {
			@Override
			public TableRow<RaidItem> call(final TableView<RaidItem> p) {

				final Tooltip t = new Tooltip("text");

				final TableRow<RaidItem> row = new TableRow<RaidItem>() {
					@Override
					public void updateItem(RaidItem item, boolean empty) {
						super.updateItem(item, empty);
					}
				};

				row.setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (row.getItem() == null) {
							return;
						}
						t.setText(getTooltipText(row.getItem()));
						if (t.getText() != null) {
							BaseItem.showTooltip(row, t, event);
						}
					}
				});
				row.setOnMouseExited(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						t.hide();
					}
				});

				return row;
			}
		});

		raidTable.setColumnResizePolicy(new TableResizer(new TableColumn[]{nameCol}, new double[]{1}));

		damageCol.setSortType(SortType.DESCENDING);

		raidTable.getSortOrder().add(damageCol);
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		if (combatLogName == null) {
			// nothing started yet, nothing to display
			return;
		}

		if (currentCombat != null && combat.getCombatId() == currentCombat.getCombatId()) {
			// already displaying this combat with the latest data available
			// @see updateRaidCombatStats()
			// bump to ensure consistent data
			currentCombat = combat;
			return;
		}

		// other combat selected, reset the display
		currentCombat = combat;

		resetTable();

		// try to fetch latest data
		final Collection<RaidCombatMessage> messages = getCombatUpdates(combatLogName, currentCombat);
		if (messages != null) {
			for (final RaidCombatMessage message: messages) {
				createOrReplaceTableRow(message);
			}
			finalizeTable();
		}
	}

	public void resetCombatStats() {
		currentCombat = null;

		resetTable();
	}

	public Collection<RaidCombatMessage> getCombatUpdates(final String combatLogName, final Combat combat) {
		return raidService.getCombatUpdates(combatLogName, combat.getCombatId());
	}

	public void setCombatLogName(final String fullCombatLogName) {
		// normalize
		final String combatLogName = fullCombatLogName == null ? null : new File(fullCombatLogName).getName();

		if (this.combatLogName != null && (combatLogName == null || !this.combatLogName.equals(combatLogName))) {
			if (this.combatLogName != null) {
				// ensure the old data are flushed
				raidService.storeCombatStats(this.combatLogName);
			}
			// reset all
			lastCombat = null;
			// characterName = null; // keep as it may not change
			combatBeginnings.clear();
			raidDeathLabels.clear();
		}

		this.combatLogName = combatLogName;

		if (combatLogName != null) {
			// try to load any previously stored data
			raidService.loadCombatStats(combatLogName);
		}
	}

	public String getCombatLogName() {
		return combatLogName;
	}

	public void setCharacterName(final String characterName) {
		assert combatLogName != null;

		this.characterName = characterName;
	}

	public void setLastCombat(final Combat lastCombat) {
		assert combatLogName != null;
		assert characterName != null;

		if (this.lastCombat != null
			&& lastCombat != null
			&& this.lastCombat.getCombatId() == lastCombat.getCombatId()) {
			// just updating, bump
			this.lastCombat = lastCombat;
			return;
		}

		if (this.lastCombat != null) {
			// ensure the old data are flushed
			raidService.storeCombatStats(this.combatLogName);
		}

		this.lastCombat = lastCombat;

		if (lastCombat == null) {
			// combat gone away
			return;
		}

		combatBeginnings.put(lastCombat.getTimeFrom(), lastCombat.getCombatId());
	}

	public void setRaidGroup(String raidGroupName, boolean isAdmin) {
		if (this.raidGroupName != null) {
			// cleanup
			TimerManager.stopTimer(RaidPullTimer.class);
			TimerManager.stopTimer(RaidBreakTimer.class);
		}

		if (raidGroupName != null) {
			this.raidGroupName = raidGroupName;
			this.isAdmin = isAdmin;
			pullButton.setDisable(!isAdmin);
			breakButton.setDisable(!isAdmin);
			raidNotesButton.setDisable(!isAdmin);
		} else {
			this.raidGroupName = null;
			this.isAdmin = false;
			players.clear();
		}
		reloadTitle();
	}

	// FIXME
	private String possibleDoubleJoin = null;

	public void addPlayer(final String... characterNames) {
		possibleDoubleJoin = null;
		for (String n: characterNames) {
			if (players.contains(n)) {
				// should not happen, but well
				possibleDoubleJoin = n;
				continue;
			}
			players.add(n);
		}
		reloadTitle();
	}

	public void removePlayer(final String... characterNames) {
		for (String n: characterNames) {
			if (possibleDoubleJoin != null && n.equals(possibleDoubleJoin)) {
				// ignore remove
				possibleDoubleJoin = null;
				continue;
			}
			players.remove(n);
		}
		reloadTitle();
	}

	private void reloadTitle() {
		if (raidGroupName == null) {
			raidTitle.setText("");
			raidPlayers.setText("");
			raidBar.setVisible(false);
			raidBar.setPrefHeight(0);
		} else {
			raidTitle.setText(raidGroupName);
			final StringBuilder sb = new StringBuilder();

			sb.append("(");
			sb.append(players.size());
			sb.append(") ");
			Collections.sort(players);
			for (int i = 0; i < players.size(); i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(players.get(i));
			}
			raidPlayers.setText(sb.toString());
			if (!raidBar.isVisible()) {
				raidBar.setPrefHeight(26);
				raidBar.setVisible(true);
			}
		}
	}

	public void storeLastCombatLogStats() {
		if (this.combatLogName == null) {
			// nothing to do
			return;
		}
		raidService.storeCombatStats(combatLogName);
	}

	public void updateRaidCombatStats(final RaidCombatMessage message) {
		if (message == null) {
			// can happen if sendCombatUpdate() returns null
			return;
		}
		updateRaidCombatStats(new RaidCombatMessage[]{message});
	}

	public void updateRaidCombatStats(final RaidCombatMessage[] messages) {

		if (messages == null || messages.length == 0) {
			return;
		}

		if (lastCombat == null || characterName == null) {
			logger.debug("Ignoring raid updates before first combat");
			return;
		}

		boolean isRefreshNeeded = false;
		Integer combatId = null;
		loop: for (final RaidCombatMessage message: messages) {
			if (message.getCombatTimeTo() == null) {
				// running combat ...
				if (message.getTimestamp() + MAX_UPDATE_AGE < lastCombat.getTimeFrom()) {
					// ... somewhere in a distant past
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring too old combat update [" + lastCombat + "]: " + message);
					}
					continue loop;
					// NOTREACHED
				}
				if (lastCombat.getTimeTo() != null && (message.getCombatTimeFrom() > lastCombat.getTimeTo())) {
					// ... after our last one has ended - ignore (will be fetched once we start as well)
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring too new combat update [" + lastCombat + "]: " + message);
					}
					continue loop;
					// NOTREACHED
				}
				// ... after the last one started
				combatId = lastCombat.getCombatId();

			} else if (lastCombat.getTimeTo() != null && (message.getCombatTimeFrom() > lastCombat.getTimeTo())) {
				// closed combat after our last one finished
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring too new combat closure [" + lastCombat + "]: " + message);
				}
				continue loop;
				// NOTREACHED

			} else if (message.getCombatTimeTo() > lastCombat.getTimeFrom()) {
				// closed combat after the last one started
				combatId = lastCombat.getCombatId();

			} else {
				// try to find other combat which started before the message's combat beginning
				for (Long from: combatBeginnings.keySet()) {
					if (message.getCombatTimeTo() < from) {
						break;
					}
					// keep going as long as possible // FIXME: reverse?
					combatId = combatBeginnings.get(from);
				}
				if (combatId == null) {
					// no suitable combat found (possible in previous log after DC?)
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring too old combat update (not after "
							+ combatBeginnings.keySet().toArray()[0] + "): " + message);
					}
					continue loop;
					// NOTREACHED
				}
			}
			assert combatId != null;
			raidService.storeCombatUpdate(combatLogName, combatId, message);

			if (currentCombat != null && combatId.equals(currentCombat.getCombatId())) {
				// displaying this combat, update table
				isRefreshNeeded = true;
				createOrReplaceTableRow(message);
				for (final RaidDataListener listener: listeners) {
					listener.onRaidDataUpdate(currentCombat, message);
				}
			}
		}

		if (isRefreshNeeded) {
			finalizeTable();
			for (final RaidDataListener listener: listeners) {
				listener.onRaidDataFinalize();
			}
		}
	}

	private void resetTable() {
		clearTable(raidTable);
		clearTable(combatLogTable);
		absorptions.clear();
		rankCol.setVisible(false);

		raidDeaths.getChildren().clear();
		raidDeaths.setVisible(false);
		raidDeaths.setPrefHeight(0);

		hideDeathRecap();
	}

	private void createOrReplaceTableRow(final RaidCombatMessage message) {

		absorptions.put(message.getCharacterName(), message.getAbsorptionStats());

		for (final RaidItem item: raidTable.getItems()) {
			if (item.getName().equals(message.getCharacterName())) {
				// update item (table is bound and will update itself)
				if ((item.getMessage() == null || item.getMessage().getDiscipline() == null) && message.getDiscipline() != null) {
					// http://stackoverflow.com/questions/12707558/javafx-2-refresh-table-cell-to-change-style
					// https://javafx-jira.kenai.com/browse/RT-22599
					raidTable.getColumns().get(0).setVisible(false);
					raidTable.getColumns().get(0).setVisible(true);
				}
				fillItem(item, message);
				return;
				// NOTREACHED
			}
		}

		// create new
		raidTable.getItems().add(fillItem(new RaidItem(), message));
	}

	private final static Comparator<Node> deathComparator = new Comparator<Node>() {
		@Override
		public int compare(Node o1, Node o2) {
			if (o1.getUserData() == null || o2.getUserData() == null) {
				return 0;
			}
			if ((long) o2.getUserData() > ((long) o1.getUserData())) {
				return -1;
			}
			return 1;
		}
	};

	private void finalizeTable() {
		// cross-fill absorptions
		for (final RaidItem item: raidTable.getItems()) {
			fillAbsorption(item);
			fillRank(item);
		}

		this.<RaidItem> resortTable(raidTable);

		if (!rankCol.isVisible() && canRank(currentCombat)) {
			rankCol.setVisible(true);
		}

		if (currentCombat.isRunning()) {
			int exits = 0;
			for (final RaidItem item: raidTable.getItems()) {
				if (item.getMessage().getExitEvent() != null) {
					exits++;
				}
			}
			if (exits < raidTable.getItems().size() - 1) {
				// wait until the combat is finished
				return;
			}
		}

		// combat deaths
		final List<Node> items = new ArrayList<>();
		items.add(raidDeathsTitle);

		for (final RaidItem item: raidTable.getItems()) {
			if (item.getMessage() == null) {
				continue;
			}

			if (item.getMessage().getCombatEventStats() != null && !item.getMessage().getCombatEventStats().isEmpty()) {
				for (final CombatEventStats ce: item.getMessage().getCombatEventStats()) {
					fillCombatEvents(item, ce.getType(), ce.getTimestamp(), items);
				}
			}

			// expand last event
			if (item.getMessage().getExitEvent() != null && Event.Type.DEATH.equals(item.getMessage().getExitEvent())) {
				fillCombatEvents(item, item.getMessage().getExitEvent(), item.getMessage().getTimestamp(), items);
			}
		}
		int j = items.size();
		if (j > 1) {
			Collections.sort(items, deathComparator);
			// ignore "wipe" deaths (if almost everyone dies, cut in half and include only if the gap is above X seconds)
			if (j > raidTable.getItems().size()) { // i.e. wipe: j >= 2
				j = items.size() - 1;
				long last = ((long) items.get(j).getUserData());
				for (int i = j - 1; i >= 1; i--) { // 1 = label
					if (((long) items.get(i).getUserData()) > (last - 5000)) {
						last = ((long) items.get(i).getUserData());
						j = i;
					}
				}
			}
			if (j > 1) {
				raidDeaths.getChildren().setAll(items.subList(0, j));
				if (!raidDeaths.isVisible()) {
					raidDeaths.setVisible(true);
					raidDeaths.setPrefHeight(25);
				}
			}
		}
	}

	private void fillCombatEvents(final RaidItem item, final Event.Type type, final long timestamp, final List<Node> items) {
		switch (type) {
			case DEATH:
				final RaidRequest request = new RaidRequest(RaidRequest.Type.DEATH_RECAP, item.getName(),
					new RaidRequest.Params(timestamp));

				// already cached from earlier?
				final Label death;
				if (raidDeathLabels.containsKey(request)) {
					death = raidDeathLabels.get(request);

					// already displaying?
					for (final Node n: raidDeaths.getChildren()) {
						if (n == death) {
							items.add(n);
							return;
						}
					}
					// FALLTHROUGH

				} else {
					death = new Label(Format.formatTime(timestamp - currentCombat.getTimeFrom()) + " " + item.getFullName());
					death.getStyleClass().add("raid-death");
					death.setUserData(timestamp);
					death.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							handleDeathRecap(death, request);
						}
					});
					raidDeathLabels.put(request, death);
				}

				items.add(death);
				break;
			default:
		}
	}

	private RaidItem fillItem(final RaidItem item, final RaidCombatMessage message) {

		item.setMessage(message);
		item.name.set(message.getCharacterName());
		item.damage.set(message.getCombatStats().getDamage());
		item.dps.set(message.getCombatStats().getDps());
		item.threat.set(message.getCombatStats().getThreat());
		item.tps.set(message.getCombatStats().getTps());
		item.damageTaken.set(message.getCombatStats().getDamageTaken());
		item.dtps.set(message.getCombatStats().getDtps());
		item.aps.set(message.getCombatStats().getAps());
		item.healing.set(message.getCombatStats().getHeal());
		item.hps.set(message.getCombatStats().getHps());
		item.ehps.set(message.getCombatStats().getEhps());
		item.pctEffective.set((int) Math.round(message.getCombatStats().getEhpsPercent()));

		// item.shielding.set(message.getCombatStats().
		// item.sps.set(message.getCombatStats().

		item.time.set((int) ((message.getCombatTimeTo() != null
			? message.getCombatTimeTo()
			: message.getTimestamp()) - message.getCombatTimeFrom()));

		return item;
	}

	private void fillAbsorption(final RaidItem item) {
		Integer total = 0;
		for (String target: absorptions.keySet()) {
			for (AbsorptionStats as: absorptions.get(target)) {
				if (item.getName().equals(as.getSource())) {
					total += as.getTotal();
				}
			}
		}
		item.shielding.set(total);
		item.sps.set((int) Math.round(total * 1000.0 / item.time.get()));
	}

	public int getShieldingTotal(String name) {
		for (final RaidItem item: raidTable.getItems()) {
			if (item.getName().equals(name)) {
				return item.shielding.get();
			}
		}
		return 0;
	}

	public int getShieldingPerSecond(String name) {
		for (final RaidItem item: raidTable.getItems()) {
			if (item.getName().equals(name)) {
				return item.sps.get();
			}
		}
		return 0;
	}

	// TODO: move this somewhere more proper
	public int[] getShieldingSelf(int tick) throws Exception {
		if (lastCombat == null || combatLogName == null || characterName == null) {
			return null;
		}
		if (characterName != null && !raidTable.getItems().isEmpty() && context.getCombatSelection() == null) {
			// load from normally from the table
			return new int[]{getShieldingTotal(characterName), getShieldingPerSecond(characterName)};
		}
		// try to load from the cache (if raiding was active)
		final Collection<RaidCombatMessage> messages = getCombatUpdates(combatLogName, lastCombat);
		Integer total = null;
		if (messages != null && context.getCombatSelection() == null) {
			total = 0;
			for (final RaidCombatMessage message: messages) {
				if (message.getAbsorptionStats() != null) {
					for (final AbsorptionStats s: message.getAbsorptionStats()) {
						if (characterName.equals(s.getSource())) {
							total += s.getTotal();
						}
					}
				}
			}
		} else {
			// last resort - load parsed data (self-only)
			final List<AbsorptionStats> as = eventService.getAbsorptionStats(lastCombat, context.getCombatSelection());
			total = 0;
			for (final AbsorptionStats abs: as) {
				if (characterName.equals(abs.getSource())) {
					total += abs.getTotal();
				}
			}
		}

		return new int[]{total, tick <= 0 ? 0 : (int) Math.round(total * 1000.0 / tick)};
	}

	private boolean canRank(final Combat combat) {
		if (combat == null
			|| combat.isRunning()
			|| combat.getBoss() == null
			|| combat.getDiscipline() == null) {
			return false;
		}
		return !RaidBossName.OperationsTrainingDummy.equals(currentCombat.getBoss().getRaidBossName());
	}

	private void fillRank(final RaidItem item) {
		if (!canRank(currentCombat) || item.getMessage().getDiscipline() == null) {
			item.rank.set(-1);
			return;
		}

		final RankType type;
		final int value;
		switch (item.getMessage().getDiscipline().getRole()) {
			case TANK:
				type = RankType.DTPS;
				value = item.dtps.get();
				break;
			case HEALER:
				type = RankType.EHPS;
				value = item.ehps.get() + item.sps.get();
				break;
			default:
			case DPS:
				type = RankType.DPS;
				value = item.dps.get();
				break;
		}

		try {
			RankClass rc = null;
			try {
				rc = getRank(item, type, currentCombat, value);
			} catch (SocketTimeoutException | SocketException e) {
				// try again
				Thread.sleep(15000);
				rc = getRank(item, type, currentCombat, value);
			}

			if (rc.getReason() != null) {
				// unable to rank
				if (logger.isDebugEnabled()) {
					logger.debug("Unable to rank player " + item.name.get() + ": " + rc.getReason());
				}
				item.rank.set(-2);
			} else {
				item.rank.set(rc.getPercent());
			}

		} catch (Exception e) {
			if (e.getMessage().equals("Read timed out")) {
				// local issue, silently ignore
				return;
			}
			logger.error("Failed to rank player " + item.name.get() + ": " + e.getMessage(), e);
		}
	}

	private RankClass getRank(final RaidItem item, final RankType type, final Combat currentCombat, final int value) throws Exception {
		return rankService.getRank(currentCombat.getBoss(),
			type,
			item.getMessage().getDiscipline(),
			item.time.get(),
			value);
	}

	private String getTooltipText(final RaidItem item) {
		final StringBuilder sb = new StringBuilder();
		if (item.getMessage().getDiscipline() != null) {
			sb.append("\n" + item.getMessage().getDiscipline() + "\n");

			if (item.rank.getValue() != null) {
				if (item.rank.get() >= 0) {
					switch (item.getMessage().getDiscipline().getRole()) {
						case TANK:
							sb.append("Damage taken rating: ");
							break;
						case HEALER:
							sb.append("Effective healing rating: ");
							break;
						default:
							sb.append("Damage dealt rating: ");
							break;
					}
					sb.append(item.rank.get()).append("%\n");

				} else if (item.rank.get() == -2) {
					sb.append("(not ranked - combat too short)\n");
				}
			}
		}
		if (item.getMessage().getAbsorptionStats() != null && !item.getMessage().getAbsorptionStats().isEmpty()) {
			sb.append("\nShielding received:\n");
			for (AbsorptionStats as: item.getMessage().getAbsorptionStats()) {
				sb.append(as.getSource() + " [" + Format.formatAbility(as.getAbility()) + "]: " + Format.formatThousands(as.getTotal()) + "\n");
			}
		}
		if (item.shielding.get() > 0) {
			sb.append("\nShielding done:\n");
			for (String target: absorptions.keySet()) {
				for (AbsorptionStats as: absorptions.get(target)) {
					if (!item.getName().equals(as.getSource())) {
						continue;
					}
					sb.append(target + " [" + Format.formatAbility(as.getAbility()) + "]: " + Format.formatThousands(as.getTotal()) + "\n");
				}
			}
		}
		if (item.getMessage().getChallengeStats() != null && !item.getMessage().getChallengeStats().isEmpty()) {
			sb.append("\nChallenges:\n");
			for (ChallengeStats cs: item.getMessage().getChallengeStats()) {
				sb.append(cs.getChallengeName().getFullName() + " @ " + Format.formatTime(cs.getTickFrom()) + ": "
					+ Format.formatThousands(cs.getHeal() > 0 ? cs.getHeal() : cs.getDamage()) + "\n");
			}
		}
		if (item.getMessage().getCombatStats().getApm() > 0) {
			sb.append("\nAPM: " + item.getMessage().getCombatStats().getApm() + "\n");
		}

		if (Event.Type.DEATH.equals(item.getMessage().getExitEvent())) {
			sb.append("\n(died)");
		}
		return sb.length() > 0 ? item.getFullName() + sb.toString() : null;
	}

	private void handleDeathRecap(final Label label, final RaidRequest request) {
		// toggle
		if (label.getStyleClass().contains("raid-death-active")) {
			hideDeathRecap();
			return;
		}

		// already available?
		List<Event> events = raidService.getStoredResponses(combatLogName, request);

		// is it me?
		if (events == null && request.getTargetName().equals(characterName)) {
			try {
				events = getDeathRecap(request);
				if (events == null) {
					listener.setFlash("Unable to obtain death recap, combat is no longer available", Type.ERROR);
					return;
				}

			} catch (Exception e) {
				listener.setFlash("Unable to obtain death recap: " + e.getMessage(), Type.ERROR);
				return;
			}
		}

		if (events != null) {
			// great, display
			showDeathRecap(label, events);
			return;
		}

		// raiding available?
		if (!raidManager.isRunning()) {
			listener.setFlash("Unable to obtain death recap for " + request.getTargetName() + ", no longer raiding", Type.ERROR);
			return;
		}

		// player present?
		if (!players.contains(request.getTargetName())) {
			listener.setFlash("Unable to obtain death recap for " + request.getTargetName() + ", not connected to the group", Type.ERROR);
			return;
		}

		// already fired?
		final String style = "raid-death-inter";
		if (label.getStyleClass().contains(style)) {
			// already pending
			return;
		}

		// fire and wait
		label.getStyleClass().add(style);
		raidManager.sendRequest(request, new RequestOutgoingCallback() {
			@Override
			public void onResponseIncoming(final RaidResponseMessage message) {
				label.getStyleClass().remove(style);

				try {
					// is error?
					if (RaidRequest.Type.SYSTEM_ERROR.equals(message.getRequestType())) {
						listener.setFlash("Error while fetching death recap for " + request.getTargetName() + ": " + new String(message.getPayload()),
							FlashMessage.Type.ERROR);
						return;
					}

					final List<Event> events = raidService.decodeAndStoreResponse(combatLogName, request, message.getPayload());
					Platform.runLater(new Runnable() {
						public void run() {
							showDeathRecap(label, events);
						};
					});

				} catch (Exception e) {
					logger.error("Unable to decode " + message + " as " + request + ": " + e.getMessage(), e);
					listener.setFlash("Error while decoding death recap for " + request.getTargetName() + ": " + e.getMessage(),
						FlashMessage.Type.ERROR);
				}
			}
		});
	}

	private void showDeathRecap(final Label label, final List<Event> events) {
		raidTable.setVisible(false);
		raidTable.setPrefHeight(0);

		for (final Label l: raidDeathLabels.values()) {
			l.getStyleClass().remove("raid-death-active");
		}
		label.getStyleClass().add("raid-death-active");

		// summary
		int dt = 0, ht = 0;
		for (final Event e: events) {
			if (Helpers.isTargetThisPlayer(e)) {
				if (e.getEffectiveHeal() != null) {
					ht += e.getEffectiveHeal();
				} else if (e.getValue() != null && Helpers.isEffectDamage(e)) {
					dt += e.getValue();
				}
			}
		}
		damageTaken.setAlignment(Pos.CENTER_RIGHT);
		damageTaken.setText(Format.formatAdaptive(dt));
		healingTaken.setAlignment(Pos.CENTER_RIGHT);
		healingTaken.setText(Format.formatAdaptive(ht));

		combatLogTable.setPrefHeight(-1);
		combatLogTable.setVisible(true);
		combatLogFilter.setPrefHeight(25);
		combatLogFilter.setVisible(true);

		currentEvents = events;
		try {
			toggleBreakdown(null);
		} catch (Exception e) {
			logger.error("Unable to display death recap breakdown: " + e.getMessage(), e);
		}
		if (combatLogTable.getItems().size() > 7) {
			combatLogTable.scrollTo(combatLogTable.getItems().size() - 1);
		}
	}

	private void hideDeathRecap() {
		combatLogTable.setVisible(false);
		combatLogTable.setPrefHeight(0);
		combatLogFilter.setVisible(false);
		combatLogFilter.setPrefHeight(0);
		raidTable.setPrefHeight(-1);
		raidTable.setVisible(true);

		for (final Label l: raidDeathLabels.values()) {
			l.getStyleClass().remove("raid-death-active");
		}

		currentEvents = null;
	}

	private List<Event> getDeathRecap(final RaidRequest request) throws Exception {
		final long timestamp = request.getParams().getTimestamp();
		final Integer combatId = context.findCombatIdByCombatEvent(Event.Type.DEATH, timestamp);
		Combat combat = null;
		if (combatId != null) {
			combat = eventService.findCombat(combatId);
		}
		if (combat == null) {
			// no longer valid (another log maybe?)
			return null;
		}
		final long deathTick = timestamp - combat.getTimeFrom();
		final CombatSelection combatSel = new CombatSelection(
			combat.getEventIdFrom(),
			combat.getEventIdTo(),
			deathTick > 10000 ? deathTick - 10000 : 0, // last 10 seconds
			deathTick);

		return eventService.getCombatEvents(combat,
			Collections.singleton(Event.Type.SIMPLIFIED), null, null, null,
			combatSel);
	}

	@Override
	public void toggleBreakdown(final ActionEvent event) throws Exception {
		if (toggles.isEmpty()) {
			toggles.put(damageDealtButton, Event.Type.DAMAGE_DEALT);
			toggles.put(damageTakenButton, Event.Type.DAMAGE_TAKEN);
			toggles.put(healingDoneButton, Event.Type.HEALING_DONE);
			toggles.put(healingTakenButton, Event.Type.HEALING_TAKEN);
			toggles.put(actionsButton, Event.Type.ACTIONS);
		}
		for (final CheckBox b: toggles.keySet()) {
			if (b.isSelected()) {
				filterFlags.add(toggles.get(b));
			} else {
				filterFlags.remove(toggles.get(b));
			}
		}

		if (currentEvents == null || currentEvents.isEmpty() || currentCombat == null) {
			return;
		}

		if (filterFlags.isEmpty() || filterFlags.size() == toggles.size()) {
			fillCombatLogTable(currentCombat, currentEvents, null);
			return;
		}

		final List<Event> filteredEvents = new ArrayList<>();

		for (final Event e: currentEvents) {
			boolean keep = false;
			for (Event.Type t: filterFlags) {
				switch (t) {
					case DAMAGE_DEALT:
						keep |= Helpers.isEffectDamage(e) && Helpers.isSourceThisPlayer(e);
						break;
					case DAMAGE_TAKEN:
						keep |= Helpers.isEffectDamage(e) && Helpers.isTargetThisPlayer(e);
						break;
					case HEALING_DONE:
						keep |= Helpers.isEffectHeal(e) && Helpers.isSourceThisPlayer(e);
						break;
					case HEALING_TAKEN:
						keep |= Helpers.isEffectHeal(e) && Helpers.isTargetThisPlayer(e);
						break;
					case ACTIONS:
						keep |= Helpers.isEffectAbilityActivate(e) && Helpers.isSourceThisPlayer(e);
						break;
					default:
				}
			}
			if (keep) {
				filteredEvents.add(e);
			}
		}

		fillCombatLogTable(currentCombat, filteredEvents, null);
	}

	public void resetBreakdown(final ActionEvent event) throws Exception {

		for (final CheckBox b: toggles.keySet()) {
			b.setSelected(false);
		}

		toggleBreakdown(null);
	}

	public void onRequestIncoming(final RaidRequestMessage message, final RequestIncomingCallback callback) {
		try {
			final RaidRequest request = message.getRequest();
			switch (request.getType()) {
				case DEATH_RECAP:
					List<Event> events = getDeathRecap(request);
					if (events == null) {
						respondError("Combat is no longer available", callback);
						return;
					}
					if (!ArrayList.class.equals(events.getClass())) { // TODO: investigate
						logger.error("Fixed events: " + events.getClass() + " (" + events.size() + ")");
						events = new ArrayList<>(events);
					}
					callback.onResponseOutgoing(request.getType(), raidService.encodeResponse(events));
					break;
				case RAID_PULL:
					if (request.getParams().getTimestamp() > 0) {
						TimerManager.startTimer(RaidPullTimer.class, TimeUtils.getCurrentTime(),
							(int) (request.getParams().getTimestamp() - TimeUtils.getCurrentTime()));
					} else {
						TimerManager.stopTimer(RaidPullTimer.class);
					}
					break;
				case RAID_BREAK:
					if (request.getParams().getTimestamp() > 0) {
						TimerManager.startTimer(RaidBreakTimer.class, TimeUtils.getCurrentTime(),
							(int) (request.getParams().getTimestamp() - TimeUtils.getCurrentTime()));
					} else {
						TimerManager.stopTimer(RaidBreakTimer.class);
					}
					break;
				case RAID_NOTES:
					raidNotesPopoutPresenter.updateNoteIfNeeded(request.getParams().getNote(), false);
					break;
				default:
					logger.error("Unable to process request " + message + ": Unknown request");
					respondError("Unknown request: " + request, callback);
					return;
			}

		} catch (Exception e) {
			logger.error("Unable to process request " + message + ": " + e.getMessage(), e);
			respondError("General error: " + e.getMessage(), callback);
		}
	};

	private void respondError(String error, final RequestIncomingCallback callback) {
		callback.onResponseOutgoing(RaidRequest.Type.SYSTEM_ERROR, error.getBytes()); // TODO
	}

	public void handlePullCountdown(final ActionEvent e) {
		if (raidGroupName == null || !isAdmin) {
			return;
		}

		final RaidRequest request = new RaidRequest(RaidRequest.Type.RAID_PULL, null,
			new RaidRequest.Params(TimerManager.getTimer(RaidPullTimer.class) == null
				? (TimeUtils.getCurrentTime() + config.getRaidPullSeconds() * 1000)
				: 0));

		handleAnnouncement(pullButton, request);
	}

	public void handleBreakCountdown(final ActionEvent e) {
		if (raidGroupName == null || !isAdmin) {
			return;
		}

		final RaidRequest request = new RaidRequest(RaidRequest.Type.RAID_BREAK, null,
			new RaidRequest.Params(TimerManager.getTimer(RaidBreakTimer.class) == null
				? (TimeUtils.getCurrentTime() + config.getRaidBreakMinutes() * 60 * 1000)
				: 0));

		handleAnnouncement(breakButton, request);
	}

	public void handleRaidNotes(final ActionEvent e) {
		if (raidGroupName == null || !isAdmin) {
			return;
		}

		if (raidNotesPopoutPresenter.getNote() != null) {
			raidNotesDialogPresenter.setNote(raidNotesPopoutPresenter.getNote());
		}
		raidNotesDialogPresenter.show();
	}

	private void handleAnnouncement(final Button button, final RaidRequest request) {
		if (button.getStyleClass().contains("toggle-button-inter")) {
			// already running
			return;
		}

		// raiding available?
		if (!raidManager.isRunning()) {
			listener.setFlash("Unable to broadcast, no longer raiding", Type.ERROR);
			return;
		}

		// fire and wait
		button.getStyleClass().add("toggle-button-inter");
		raidManager.sendRequest(request, new RequestOutgoingCallback() {
			@Override
			public void onResponseIncoming(final RaidResponseMessage message) {
				button.getStyleClass().remove("toggle-button-inter");

				try {
					if (RaidRequest.Type.SYSTEM_OK.equals(message.getRequestType())) {
						// okay
						return;
					}

					// is error?
					if (RaidRequest.Type.SYSTEM_ERROR.equals(message.getRequestType())) {
						listener.setFlash("Error while broadcasting: " + new String(message.getPayload()), FlashMessage.Type.ERROR);
						return;
					}

				} catch (Exception e) {
					logger.error("Unable to decode " + message + " as " + request + ": " + e.getMessage(), e);
					listener.setFlash("Error while broadcasting: " + e.getMessage(), FlashMessage.Type.ERROR);
				}
			}
		});
	}

}
