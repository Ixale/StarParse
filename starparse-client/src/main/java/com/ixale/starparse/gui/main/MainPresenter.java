package com.ixale.starparse.gui.main;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatInfo;
import com.ixale.starparse.domain.CombatLog;
import com.ixale.starparse.domain.ConfigTimer;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.RaidGroup;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.gui.FlashMessage;
import com.ixale.starparse.gui.FlashMessage.Type;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.FullscreenLoader;
import com.ixale.starparse.gui.StarparseApp;
import com.ixale.starparse.gui.Win32Utils;
import com.ixale.starparse.gui.dialog.RaidNotesDialogPresenter;
import com.ixale.starparse.gui.dialog.SettingsDialogPresenter;
import com.ixale.starparse.gui.dialog.UploadParselyDialogPresenter;
import com.ixale.starparse.gui.dialog.UploadParselyDialogPresenter.UploadParselyListener;
import com.ixale.starparse.gui.popout.BasePopoutPresenter;
import com.ixale.starparse.gui.popout.BaseRaidPopoutPresenter;
import com.ixale.starparse.gui.popout.BaseTimersPopoutPresenter;
import com.ixale.starparse.gui.popout.ChallengesPopoutPresenter;
import com.ixale.starparse.gui.popout.HotsPopoutPresenter;
import com.ixale.starparse.gui.popout.PersonalStatsPopoutPresenter;
import com.ixale.starparse.gui.popout.RaidBossPopoutPresenter;
import com.ixale.starparse.gui.popout.RaidDpsPopoutPresenter;
import com.ixale.starparse.gui.popout.RaidHpsPopoutPresenter;
import com.ixale.starparse.gui.popout.RaidNotesPopoutPresenter;
import com.ixale.starparse.gui.popout.RaidTpsPopoutPresenter;
import com.ixale.starparse.gui.popout.TimersBPopoutPresenter;
import com.ixale.starparse.gui.popout.TimersCPopoutPresenter;
import com.ixale.starparse.gui.popout.TimersCenterPopoutPresenter;
import com.ixale.starparse.gui.popout.TimersPopoutPresenter;
import com.ixale.starparse.gui.timeline.Timeline;
import com.ixale.starparse.log.LogWatcher;
import com.ixale.starparse.log.LogWatcherListener;
import com.ixale.starparse.parser.Parser;
import com.ixale.starparse.raid.RaidListener;
import com.ixale.starparse.raid.RaidManager;
import com.ixale.starparse.service.EventService;
import com.ixale.starparse.service.EventServiceListener;
import com.ixale.starparse.service.TimerService;
import com.ixale.starparse.service.impl.Context;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerListener;
import com.ixale.starparse.timer.TimerManager;
import com.ixale.starparse.ws.RaidClient.RequestIncomingCallback;
import com.ixale.starparse.ws.RaidCombatMessage;
import com.ixale.starparse.ws.RaidRequestMessage;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MainPresenter implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(MainPresenter.class);

	@FXML
	private Parent root;

	@FXML
	private ToggleButton parseButton, raidButton;

	@FXML
	private AnchorPane headCombat;

	@FXML
	private ListView<Combat> combatList;
	@FXML
	private Label combatName, combatTime;

	@FXML
	private HBox characterName;
	@FXML
	private MenuButton characterNameMenu;
	@FXML
	private Button characterNameReset;

	@FXML
	private CheckBox trashButton;

	@FXML
	private TabPane contentTabs;
	@FXML
	private Tab contentOverview, contentDamageDealt, contentHealingDone,
			contentDamageTaken, contentHealingTaken, contentCombatLog, contentRaid;

	@FXML
	private Node header, resizer, status;
	@FXML
	private Label statusMessage;
	@FXML
	private Hyperlink linkTwitter, linkDonate;

	@FXML
	private Menu recentMenu, raidGroupsMenu, timersMenu;
	@FXML
	private MenuItem raidGroupsSettingsMenu, timersSettingsMenu;
	@FXML
	private CheckMenuItem timersAPopoutMenu, timersBPopoutMenu, timersCPopoutMenu,
			timersCenterPopoutMenu, personalStatsPopoutMenu, challengesPopoutMenu,
			raidDpsPopoutMenu, raidHpsPopoutMenu, raidTpsPopoutMenu, raidBossPopoutMenu,
			hotsPopoutMenu, raidNotesPopoutMenu, lockOverlaysMenu;
	@FXML
	private Button darkModeButton;

	@Inject
	private OverviewPresenter overviewPresenter;
	@Inject
	private DamageDealtPresenter damageDealtPresenter;
	@Inject
	private HealingDonePresenter healingDonePresenter;
	@Inject
	private DamageTakenPresenter damageTakenPresenter;
	@Inject
	private HealingTakenPresenter healingTakenPresenter;
	@Inject
	private CombatLogPresenter combatLogPresenter;
	@Inject
	private RaidPresenter raidPresenter;

	@Inject
	private SettingsDialogPresenter settingsDialogPresenter;
	@Inject
	private UploadParselyDialogPresenter uploadParselyDialogPresenter;
	@Inject
	private RaidNotesDialogPresenter raidNotesDialogPresenter;

	@Inject
	private TimersPopoutPresenter timersPopoutPresenter;
	@Inject
	private TimersBPopoutPresenter timersBPopoutPresenter;
	@Inject
	private TimersCPopoutPresenter timersCPopoutPresenter;
	@Inject
	private TimersCenterPopoutPresenter timersCenterPopoutPresenter;
	@Inject
	private PersonalStatsPopoutPresenter personalStatsPopoutPresenter;
	@Inject
	private ChallengesPopoutPresenter challengesPopoutPresenter;
	@Inject
	private RaidDpsPopoutPresenter raidDpsPopoutPresenter;
	@Inject
	private RaidHpsPopoutPresenter raidHpsPopoutPresenter;
	@Inject
	private RaidTpsPopoutPresenter raidTpsPopoutPresenter;
	@Inject
	private RaidBossPopoutPresenter raidBossPopoutPresenter;
	@Inject
	private HotsPopoutPresenter hotsPopoutPresenter;
	@Inject
	private RaidNotesPopoutPresenter raidNotesPopoutPresenter;

	@FXML
	private Label apm, time,
			dps, damage, hps, heal, ehps, ehpsPercent,
			tps, threat, aps, absorbed,
			dtps, damageTaken, hpsTaken, healTaken, ehpsTaken, ehpsTakenPercent,
			logTime;

//	private ImageView disciplineIcon;

	private FileChooser fileChooser;
	private Config config;

	private EventService eventService;
	private TimerService timerService;
	private Context context;
	private Parser parser;
	private LogWatcher logWatcher;
	private FullscreenLoader fullscreenLoader;
	private boolean isLoadingFullFile = false;

	// currently viewing combat
	private Combat currentCombat = null;
	// helper to store "last current combat"
	private Integer selectedCombatId = null;
	private CombatLog currentCombatLog = null, lastCombatLog = null;

	private CombatStats stats = null;
	private Timeline timeline = null;

	private RaidManager raidManager;

	private FlashMessage flash;

	static class StatsTab {
		final BaseStatsPresenter presenter;
		final Tab tab;

		final static ArrayList<StatsTab> tabs = new ArrayList<>();

		static MainPresenter mainPresenter;
		static Runnable onUpdateRequestedAction;

		private StatsTab(final Tab t, final BaseStatsPresenter p) {
			presenter = p;
			tab = t;

			tab.setContent(presenter.getView());
		}

		public static void setOnUpdateRequestedAction(final MainPresenter mp, final Runnable r) {
			mainPresenter = mp;
			onUpdateRequestedAction = r;
		}

		public static void add(final Tab t, final BaseStatsPresenter p) {
			tabs.add(new StatsTab(t, p));

			p.setRequestUpdateListener(new BaseStatsPresenter.RequestUpdateListener() {
				@Override
				public void onUpdateRequested() {
					Platform.runLater(onUpdateRequestedAction);
				}

				@Override
				public void setFlash(final String message, final FlashMessage.Type type) {
					Platform.runLater(() -> mainPresenter.setFlash(message, type));
				}
			});
		}

		public static boolean update(final Tab t, final Combat combat, final CombatStats stats) throws Exception {
			for (StatsTab st : tabs) {
				if (st.tab == t) {
					st.presenter.updateCombatStats(combat, stats);
					return true;
				}
			}
			return false;
		}

		public static void resetAll() {
			for (StatsTab st : tabs) {
				st.presenter.resetCombatStats();
			}
		}
	}

	static class StatsPopout {

		final BasePopoutPresenter presenter;

		final static ArrayList<StatsPopout> popouts = new ArrayList<>();

		private static BasePopoutPresenter.ShowingListener listener;
		private static RaidPresenter raidPresenter;

		private StatsPopout(final BasePopoutPresenter p, final CheckMenuItem m, final Config config) {
			presenter = p;

			presenter.setParentMenuItem(m);
			presenter.setConfig(config);
			presenter.setListener(listener);
			if (presenter instanceof BaseRaidPopoutPresenter) {
				((BaseRaidPopoutPresenter) presenter).setRaidPresenter(raidPresenter);
			}
			if (presenter instanceof PersonalStatsPopoutPresenter) {
				((PersonalStatsPopoutPresenter) presenter).setRaidPresenter(raidPresenter);
			}
		}

		public static void setListener(final BasePopoutPresenter.ShowingListener l) {
			listener = l;
		}

		public static void setRaidPresenter(final RaidPresenter p) {
			raidPresenter = p;
		}

		public static void add(final BasePopoutPresenter p, final CheckMenuItem m, final Config config) {
			popouts.add(new StatsPopout(p, m, config));
		}

		public static void updateVisible(final Combat combat, final CombatStats stats,
				final BasePopoutPresenter popoutPres) {
			for (final StatsPopout sp : popouts) {
				if ((popoutPres == null && sp.presenter.getParentMenuItem().isSelected()) || (popoutPres != null && sp.presenter == popoutPres)) {
					try {
						sp.presenter.updateCombatStats(combat, stats);

					} catch (Exception e) {
						logger.error("Unable to update " + sp.presenter, e);
					}
				}
			}
		}

		public static void sendRaidDataUpdate(final Combat combat, final RaidCombatMessage message) {
			for (final StatsPopout sp : popouts) {
				if (sp.presenter instanceof BaseRaidPopoutPresenter && sp.presenter.getParentMenuItem().isSelected()) {
					((BaseRaidPopoutPresenter) sp.presenter).onRaidDataUpdate(combat, message);
				}
			}
		}

		public static void sendRaidDataFinalize() {
			for (final StatsPopout sp : popouts) {
				if (sp.presenter instanceof BaseRaidPopoutPresenter && sp.presenter.getParentMenuItem().isSelected()) {
					((BaseRaidPopoutPresenter) sp.presenter).onRaidDataFinalize();
				}
			}
		}

		public static void setSettings(final Color backgroundColor, final Color textColor,
				final Color damageColor, final Color healingColor, final Color threatColor, final Color friendlyColor,
				final double raidDamageOpacity, final boolean raidDamageBars,
				final double raidHealingOpacity, final boolean raidHealingBars, final String raidHealingMode,
				final double raidThreatOpacity, final boolean raidThreatBars,
				final double raidBossOpacity, final boolean raidBossBars,
				final double raidChallengesOpacity, final boolean raidChallengesBars,
				final double timersOpacity, final boolean timersBars,
				final double personalOpacity, final boolean personalBars, final String personalMode,
				final boolean timersCenter, final Double timersCenterX, final Double timersCenterY,
				final Integer fractions,
				final boolean popoutSolid) {
			// popout settings
			for (final StatsPopout sp : popouts) {
				// extra handling FIXME
				if (sp.presenter instanceof TimersCenterPopoutPresenter) {
					sp.presenter.setEnabled(timersCenter);
					if (timersCenter) {
						sp.presenter.showPopout();
					} else {
						sp.presenter.hidePopout();
					}
					sp.presenter.setLocation(timersCenterX, timersCenterY);
					sp.presenter.getParentMenuItem().setSelected(timersCenter);
					sp.presenter.repaint(null);
					continue;
				}
				if (sp.presenter.getParentMenuItem().isSelected()) {

					if (sp.presenter instanceof RaidDpsPopoutPresenter) {
						sp.presenter.setOpacity(raidDamageOpacity);
						sp.presenter.setBars(raidDamageBars);

					} else if (sp.presenter instanceof RaidHpsPopoutPresenter) {
						sp.presenter.setOpacity(raidHealingOpacity);
						sp.presenter.setBars(raidHealingBars);
						sp.presenter.setMode(raidHealingMode);

					} else if (sp.presenter instanceof RaidTpsPopoutPresenter) {
						sp.presenter.setOpacity(raidThreatOpacity);
						sp.presenter.setBars(raidThreatBars);

					} else if (sp.presenter instanceof ChallengesPopoutPresenter) {
						sp.presenter.setOpacity(raidChallengesOpacity);
						sp.presenter.setBars(raidChallengesBars);

					} else if (sp.presenter instanceof BaseTimersPopoutPresenter) {
						sp.presenter.setOpacity(timersOpacity);
						sp.presenter.setBars(timersBars);
						((BaseTimersPopoutPresenter) sp.presenter).setFractions(fractions);

					} else if (sp.presenter instanceof PersonalStatsPopoutPresenter) {
						sp.presenter.setOpacity(personalOpacity);
						sp.presenter.setBars(personalBars);
						sp.presenter.setMode(personalMode);

					} else if (sp.presenter instanceof RaidBossPopoutPresenter) {
						sp.presenter.setOpacity(raidBossOpacity);
						sp.presenter.setBars(raidBossBars);

					} else if (sp.presenter instanceof RaidNotesPopoutPresenter) {
						sp.presenter.setOpacity(personalOpacity); // TODO
					}

					sp.presenter.setTextColor(textColor);
					sp.presenter.setBackgroundColor(backgroundColor);
					sp.presenter.setBarColors(damageColor, healingColor, threatColor, friendlyColor);
					sp.presenter.setSolid(popoutSolid);
					sp.presenter.repaint(null);
				}
			}
		}

		public static void reloadEnabled() {
			for (StatsPopout sp : popouts) {
				if (sp.presenter.isEnabled()) {
					sp.presenter.showPopout();
					sp.presenter.getParentMenuItem().setSelected(true);
				} else {
					sp.presenter.hidePopout();
					sp.presenter.getParentMenuItem().setSelected(false);
				}
			}
		}

		public static void toggle(final CheckMenuItem menu) {
			for (StatsPopout sp : popouts) {
				if (sp.presenter.getParentMenuItem() == menu) {
					if (menu.isSelected()) {
						sp.presenter.setEnabled(true);
						sp.presenter.showPopout();
					} else {
						sp.presenter.setEnabled(false);
						sp.presenter.hidePopout();
					}
				}
			}
		}

		public static void hideAll() {
			for (StatsPopout sp : popouts) {
				sp.presenter.hidePopout();
				sp.presenter.getParentMenuItem().setSelected(false);
			}
		}

		public static void bringPopoutsToFront() {
			for (StatsPopout sp : popouts) {
				sp.presenter.bringPopoutToFront();
			}
		}

		public static void lock(boolean isLocked) {
			for (StatsPopout sp : popouts) {
				sp.presenter.setMouseTransparent(isLocked);
			}
		}

		public static void resetAll() {
			for (StatsPopout sp : popouts) {
				sp.presenter.resetCombatStats();
			}
		}

		public static void destroyAll() {
			for (StatsPopout sp : popouts) {
				sp.presenter.destroyPopout();
			}
		}
	}

	static class CombatCell extends ListCell<Combat> {

		final VBox container = new VBox();
		final Label label = new Label("...");
		final Label time = new Label();

		public CombatCell() {
			super();

			container.getChildren().addAll(label, time);

			label.getStyleClass().add("combat-title");
			label.setMaxWidth(134);
			time.getStyleClass().add("combat-time");
			time.setMaxWidth(134);
		}

		protected void updateItem(final Combat combat, boolean empty) {

			super.updateItem(combat, empty);
			setText(null);
			this.getStyleClass().remove("combat-boss");
			this.getStyleClass().remove("combat-pvp");

			if (empty || combat == null) {
				setGraphic(null);
				return;
			}

			if (combat.getBoss() != null) {
				this.getStyleClass().add("combat-boss");
			} else if (Boolean.TRUE.equals(combat.isPvp())) {
				this.getStyleClass().add("combat-pvp");
			}

			label.setText(Format.formatCombatName(combat));
			time.setText(Format.formatCombatTime(combat));

			setGraphic(container);
		}
	}

	class LogMenuItem extends MenuItem {

		public LogMenuItem(final CombatLog combatLog) {
			super(Format.formatCombatLogTitle(combatLog));

			this.setOnAction(arg0 -> handleFileOpen(new File(combatLog.getFileName())));
		}
	}

	public static class LabelSeparatorMenuItem extends SeparatorMenuItem {
		public LabelSeparatorMenuItem(final String title) {
			super();
			final VBox content = new VBox();
			final HBox line = new HBox();
			line.getStyleClass().add("line");
			line.setMinHeight(2);
			line.setPrefHeight(2);
			line.setPrefWidth(2);
			line.setMaxHeight(2);

			final Label label = new Label(title);
			label.setPrefWidth(-1);
			label.setMaxWidth(Double.MAX_VALUE);
			label.setAlignment(Pos.CENTER);

			content.getChildren().addAll(label, line);
			content.getStyleClass().add("label-separator");
			setContent(content);
		}
	}

	final Runnable onNewFileAction = new Runnable() {

		@Override
		public void run() {
			try {
				if ((currentCombatLog = eventService.getCurrentCombatLog()) != null) {
					resetSelectedPlayer();
					logTime.setText(Format.formatCombatLogTime(currentCombatLog, null));

					// set the current combat log (will trigger full reset)
					raidPresenter.setCombatLogName(currentCombatLog.getFileName());

					hotsPopoutPresenter.resetPlayers();

				} else {
					raidPresenter.setCombatLogName(null);
				}

				combatList.getItems().clear();

				resetCombatStats();

				currentCombat = null;
				selectedCombatId = null;

			} catch (Exception e) {
				logger.error("General error", e);
				e.printStackTrace();
			}
		}
	};

	final Consumer<Combat> onNewCombatAction = (newCombat) -> {
		try {
			final CombatLog currentCombatLog = MainPresenter.this.currentCombatLog;
			if (currentCombatLog == null) {
				// parsing got cancelled in the meantime, discard this update
				logger.debug("Parsing cancelled, ignoring new combat");
				return;
			}

			boolean doSelectLastCombat = false;

			if (currentCombat == null || combatList.getItems().isEmpty()
					|| currentCombat.getCombatId() == combatList.getItems().get(combatList.getItems().size() - 1).getCombatId()) {
				// first combat ever or last combat currently selected => update selection to the new one
				doSelectLastCombat = true;
			}

			// rebuild the menu
			refreshCombatList(doSelectLastCombat);

			// append to the "last parsed list" (not before at least one combat anyway
			if (lastCombatLog == null || lastCombatLog.getLogId() != currentCombatLog.getLogId()) {
				// TODO
				if (parseButton.isSelected()) {
					config.addRecentParsedLog(currentCombatLog);
				} else {
					config.addRecentOpenedLog(currentCombatLog);
				}
				rebuildRecentMenu();
			}
			lastCombatLog = currentCombatLog;

			// bump the last available combat
			raidPresenter.setLastCombat(newCombat);

		} catch (Exception e) {
			logger.error("General error", e);
			e.printStackTrace();
		}
	};

	final BiConsumer<Combat, Map<Actor, Parser.ActorState>> onNewEventsAction = (final Combat lastCombat, final Map<Actor, Parser.ActorState> actorStates) -> {
		try {
			if (currentCombatLog == null) {
				// parsing got cancelled in the meantime, discard this update
				logger.debug("Parsing cancelled, ignoring new events");
				return;
			}
			if ((currentCombatLog = eventService.getCurrentCombatLog()) == null) {
				logger.error("Log file no longer available (on 'new events')");
				return;
			}

			if (context.getCharacterName() == null || !context.getCharacterName().equals(currentCombatLog.getCharacterName())) {
				// will force restart if running
				context.setCharacterName(currentCombatLog.getCharacterName());
				raidManager.setCharacterName(context.getCharacterName());
				raidPresenter.setCharacterName(context.getCharacterName());

				if (parseButton.isSelected() || config.isKnownCharacter(context.getCharacterName())) {
					// store as known character only if actually parsing (and not browsing others' logs)
					config.setLastCharacterName(context.getCharacterName());
					reloadPopouts();

				} else {
					config.setLastCharacterName(Config.DEFAULT_CHARACTER);
				}
			}

			if (lastCombat != null) {
				// extend combat log time
				logTime.setText(Format.formatCombatLogTime(currentCombatLog,
						lastCombat.getTimeTo() != null ? lastCombat.getTimeTo() : lastCombat.getTimeFrom()));

				// resolve raiding BEFORE firing wide combat update
				if (raidManager.isUpdateNeeded(lastCombat, currentCombatLog.getFileName())) {
					// newer data, send
					context.getCombatInfo().get(lastCombat.getCombatId()).getCombatPlayers().forEach((player, discipline) -> {
						final String playerName = player.getName();
						try {
							if (isLoadingFullFile) {
								raidPresenter.setLastCombat(lastCombat);
							}
							raidPresenter.updateRaidCombatStats(raidManager.sendCombatUpdate(
									lastCombat,
									eventService.getCombatStats(lastCombat, null, playerName),
									eventService.getAbsorptionStats(lastCombat, null, playerName),
									eventService.getCombatChallengeStats(lastCombat, null, playerName),
									context.getCombatEvents(lastCombat.getCombatId(), playerName),
									currentCombatLog.getCharacterName().equals(playerName)
											? playerName
											: Format.formatFakePlayerName(playerName)
							));

						} catch (Exception e) {
							logger.error("Update Raid Combat Stats failed for [" + playerName + "]", e);
						}
					});
				}
				if (isLoadingFullFile) {
					return;
				}

				// ensure the list contains updated summary
				for (int i = combatList.getItems().size() - 1; i >= 0; i--) {
					if (combatList.getItems().get(i).getCombatId() == lastCombat.getCombatId() && combatList.getItems().get(i).isRunning()) {
						// combat updated, refresh the list entry (this will fire the selection even, setting currentCombat)
						final Combat refreshedCombat = eventService.findCombat(lastCombat.getCombatId());
						combatList.getItems().set(i, refreshedCombat);
						if (combatList.getItems().size() == 1) {
							// make sure the only fight is selected
							combatList.getSelectionModel().select(refreshedCombat);
						}
						break;
					}
				}

				// ensure raid consistency
				raidPresenter.setLastCombat(lastCombat);

				// mute timers if needed
				TimerManager.setMuted(!lastCombat.isRunning() || lastCombat.getTimeTo() != null);

			} else if (combatList.getItems().isEmpty()) {
				combatName.setText("Log does not contain any combats yet ...");
			}

			if (currentCombatLog.getCharacterName() != null) {
				hotsPopoutPresenter.setActorStates(actorStates, currentCombatLog.getCharacterName());
			}

		} catch (Exception e) {
			logger.error("General error", e);
			e.printStackTrace();
		}
	};

	final Runnable onThrashToggleAction = new Runnable() {
		@Override
		public void run() {
			try {
				combatList.scrollTo(0); // needed to avoid "scrolling overflow"
				refreshCombatList(false);

			} catch (Exception e) {
				logger.error("General error", e);
				e.printStackTrace();
			}
		}
	};

	final Runnable onCombatSelectAction = new Runnable() {
		@Override
		public void run() {
			try {
				characterName.getStyleClass().remove("disabled");
				if (currentCombat == null) {
					selectedCombatId = null;
					characterName.getStyleClass().add("disabled");
					return;
				}
				if (context.getVersion() == null || !(context.getVersion().compareTo("7.0.0") >= 0)) {
					characterName.getStyleClass().add("disabled");
				}
				if (currentCombat.getTimeTo() == null || (selectedCombatId != null && currentCombat.getCombatId() != selectedCombatId)) {
					// ensure the timeline is reset for or newly selected running combat
					context.setTickFrom(null);
					context.setTickTo(null);
					timeline.resetSelection();
				}
				// update overview, tab & timeline
				updateCombatStats(currentCombat, true, true, true, null);

				selectedCombatId = currentCombat.getCombatId();

			} catch (Exception e) {
				logger.error("General error", e);
				e.printStackTrace();
			}
		}
	};

	// requested update by children presenters
	final Runnable onUpdateRequestedAction = new Runnable() {
		@Override
		public void run() {
			try {
				if (currentCombat == null) {
					return;
				}
				// update overview, tab & timeline
				timeline.resetSelection();
				if (context.getTickFrom() != null || context.getTickTo() != null) {
					timeline.getLeftHandle().moveToTick(context.getTickFrom());
					timeline.getRightHandle().moveToTick(context.getTickTo());
				}
				updateCombatStats(currentCombat, true, false, true, null);

			} catch (Exception e) {
				logger.error("General error", e);
				e.printStackTrace();
			}
		}
	};

	final LogWatcherListener logWatcherListener = new LogWatcherListener() {

		@Override
		public void onNewFile(final File logFile) throws Exception {
			eventService.resetAll();
			parser.setCombatLogFile(logFile);
		}

		@Override
		public boolean onNewLine(final String line) throws Exception {
			return parser.parseLogLine(line);
		}

		@Override
		public void onReadComplete(final Integer percent) throws Exception {
			// evaluate timers if running combat
			final Combat currentCombat = parser.getCurrentCombat();

			timerService.triggerTimers(currentCombat, parser.getEvents(), config.getConfigTimers().getTimers());

			eventService.storeCombatLog(parser.getCombatLog());
			eventService.flushEvents(
					parser.getEvents(),
					parser.getCombats(),
					currentCombat,
					parser.getEffects(),
					parser.getCurrentEffects(),
					parser.getPhases(),
					parser.getCurrentPhase(),
					parser.getAbsorptions(),
					parser.getActorStates());

			if (percent != null && fullscreenLoader != null) {
				Platform.runLater(() -> {
					if (fullscreenLoader != null) {
						if (percent >= 100) {
							isLoadingFullFile = false;
							onNewCombatAction.accept(currentCombat);
							System.gc();
							hideFullscreenLoader();
							logger.info("Parsing complete");
						} else {
							fullscreenLoader.setPercent(percent);
						}
					}
				});
			}
		}

		@Override
		public void onFileComplete() throws Exception {
			parser.closeCombatLogFile();
			onReadComplete(null);
			final boolean wasLoadingFullFile = isLoadingFullFile; // neeed for "parse -> file" switch
			Platform.runLater(() -> {
				if (wasLoadingFullFile) {
					isLoadingFullFile = false;
				}
				onNewCombatAction.accept(currentCombat);
				System.gc();
				if (wasLoadingFullFile) {
					hideFullscreenLoader();
				}
			});
		}

		@Override
		public void onFlashMessage(final String message, final Type type) {
			Platform.runLater(() -> {
				setFlash((Type.ERROR.equals(type) ? "Error: " : "") + message, type);
				if (isLoadingFullFile) {
					isLoadingFullFile = false;
					combatName.setText("Parsing failed");
					Platform.runLater(() -> hideFullscreenLoader());
				}
			});
		}
	};

	final Runnable onRaidStartedAction = new Runnable() {
		@Override
		public void run() {
			raidButton.setText("Raiding");
			raidButton.setSelected(true);
			raidButton.getStyleClass().remove("toggle-button-inter");

			// will force repaint
			raidPresenter.setRaidGroup(raidManager.getRaidGroupName(), raidManager.isGroupAdmin());

			// send first update if raiding (only for SELF)
			try {
				final Combat lastCombat = combatList.getItems().isEmpty() ? null : combatList.getItems().get(combatList.getItems().size() - 1);
				if (lastCombat != null) {
					raidPresenter.updateRaidCombatStats(raidManager.sendCombatUpdate(
							lastCombat,
							eventService.getCombatStats(lastCombat, null, currentCombatLog.getCharacterName()),
							eventService.getAbsorptionStats(lastCombat, null, currentCombatLog.getCharacterName()),
							eventService.getCombatChallengeStats(lastCombat, null, currentCombatLog.getCharacterName()),
							context.getCombatEvents(lastCombat.getCombatId(), currentCombatLog.getCharacterName()),
							currentCombatLog.getCharacterName()));

				}
			} catch (Exception e) {
				logger.error("Unable to send first raid update: " + e.getMessage(), e);
			}
			clearFlash();

			contentTabs.getSelectionModel().select(contentRaid);
		}
	};

	final Runnable onRaidStoppedAction = new Runnable() {
		@Override
		public void run() {
			if (raidManager.isEnabled()) {
				raidButton.setText("Waiting");
				raidButton.setSelected(true);
				raidButton.getStyleClass().add("toggle-button-inter");
			} else {
				raidButton.setText("Raid");
				raidButton.setSelected(false);
				raidButton.getStyleClass().remove("toggle-button-inter");
			}
			raidPresenter.setRaidGroup(null, false);
			raidNotesPopoutPresenter.updateNoteIfNeeded(null, false);
		}
	};

	final RaidListener raidListener = new RaidListener() {
		@Override
		public void onRaidStarted() {
			Platform.runLater(onRaidStartedAction);
		}

		@Override
		public void onRaidStopped() {
			Platform.runLater(onRaidStoppedAction);
		}

		@Override
		public void onPlayerJoin(final String[] characterNames) {
			Platform.runLater(() -> raidPresenter.addPlayer(characterNames));
		}

		@Override
		public void onPlayerQuit(final String[] characterNames) {
			Platform.runLater(() -> raidPresenter.removePlayer(characterNames));
		}

		@Override
		public void onError(final String message, final boolean reconnecting) {
			Platform.runLater(() -> {
				if (reconnecting) {
					raidButton.setText("Waiting");
				}
				setFlash("Raiding error: " + message, Type.ERROR);
			});
		}

		@Override
		public void onCombatUpdated(final RaidCombatMessage[] messages) {
			Platform.runLater(() -> raidPresenter.updateRaidCombatStats(messages));
		}

		@Override
		public void onRequestIncoming(final RaidRequestMessage message, final RequestIncomingCallback callback) {
			raidPresenter.onRequestIncoming(message, callback);
		}
	};

	final RaidPresenter.RaidDataListener raidDataListener = new RaidPresenter.RaidDataListener() {

		@Override
		public void onRaidDataUpdate(final Combat combat, final RaidCombatMessage message) {
			StatsPopout.sendRaidDataUpdate(combat, message);
		}

		@Override
		public void onRaidDataFinalize() {
			StatsPopout.sendRaidDataFinalize();
		}
	};

	@Autowired
	public void setEventService(EventService eventService) {
		this.eventService = eventService;

		this.eventService.addListener(new EventServiceListener() {
			@Override
			public void onNewFile() {
				Platform.runLater(onNewFileAction);
			}

			@Override
			public void onNewCombat(final Combat newCombat) {
				if (isLoadingFullFile) {
					return;
				}
				Platform.runLater(() -> onNewCombatAction.accept(newCombat));
			}

			@Override
			public void onNewEvents(final Combat lastCombat, final Map<Actor, Parser.ActorState> actorStates) {
				Platform.runLater(() -> onNewEventsAction.accept(lastCombat, actorStates));
			}
		});
	}

	@Autowired
	public void setTimerService(final TimerService timerService) {
		this.timerService = timerService;
	}

	@Autowired
	public void setContext(Context context) {
		this.context = context;
	}

	public Parent getView() {
		return root;
	}

	public Node getHeader() {
		return header;
	}

	public Node getResizer() {
		return resizer;
	}

	public Node getFooter() {
		return status;
	}

	public Label getStatusMessageLabel() {
		return statusMessage;
	}

	public void initialize(URL url, ResourceBundle resourceBundle) {
		// player selection (7.0+)
		characterName.getStyleClass().add("disabled");
		characterNameMenu.showingProperty().addListener(new SelectedPlayerMenuLoader());
		characterNameReset.setOnAction((e) -> {
			resetSelectedPlayer();
			updateCombatStats(currentCombat, true, false, true, null);
		});
		combatList.setCellFactory(combatListView -> new CombatCell());

		// upload menu
		final MenuItem miCombats = new MenuItem("Upload selected combats");
		miCombats.setOnAction(arg0 -> {
			try {
				handleUpload(currentCombatLog, eventService.getCombats(), combatList.getSelectionModel().getSelectedItems());
			} catch (Exception e) {
				logger.error("Unable to load combats: " + e.getMessage(), e);
			}
		});
		final MenuItem miLog = new MenuItem("Upload whole combat log");
		miLog.setOnAction(arg0 -> {
			try {
				handleUpload(currentCombatLog, eventService.getCombats(), null);
			} catch (Exception e) {
				logger.error("Unable to load combats: " + e.getMessage(), e);
			}
		});

		final ContextMenu combatMenu = new ContextMenu(miCombats, miLog);
		combatMenu.setOnShowing(e -> {
			if (combatList.getItems().size() > 0) {
				miLog.setDisable(false);
				int count = 0;
				if (combatList.getSelectionModel().getSelectedItems().size() > 0) {
					for (final Combat c : combatList.getSelectionModel().getSelectedItems()) {
						if (c != null) {
							count++;
						}
					}
				}
				if (count > 0) {
					miCombats.setDisable(false);
					if (count > 1) {
						miCombats.setText("Upload " + count + " selected combats");
					} else {
						miCombats.setText("Upload selected combat");
					}
				} else {
					miCombats.setDisable(true);
				}
			} else {
				miLog.setDisable(true);
				miCombats.setDisable(true);
				miCombats.setText("Upload selected combats");
			}
		});
		combatList.setContextMenu(combatMenu);

		final int[] combatSelCount = new int[]{0};
		combatList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		combatList.getSelectionModel().selectedItemProperty().addListener((arg0, oldValue, newValue) -> {
			combatSelCount[0] = combatList.getSelectionModel().getSelectedItems().size();
			if (newValue == null) {
				// clearing, ignore
				return;
			}
			// Fired from either:
			// - manual click
			// - auto-last selection during new combat
			// - update of the list entry during new events
			currentCombat = newValue;

			Platform.runLater(onCombatSelectAction);
		});
		// FIXME: ugly hack to detect CTRL+click item removal (the event above is NOT being fired for that)
		combatList.setOnMouseClicked(event -> {
			if (combatSelCount[0] > 1 && combatList.getSelectionModel().getSelectedItems().size() < combatSelCount[0]) {
				combatSelCount[0] = combatList.getSelectionModel().getSelectedItems().size();
				Platform.runLater(onCombatSelectAction);
			}
		});

		contentTabs.getSelectionModel().selectedIndexProperty().addListener((values, oldValue, newValue) -> {
			// update tab only
			updateCombatStats(currentCombat, false, false, false, null);
		});

		logTime.setText("");

		TimerManager.addListener(new TimerListener() {
			@Override
			public void onTimersTick() {
				Platform.runLater(() -> {
					timersPopoutPresenter.tickTimers();
//					timersBPopoutPresenter.tickTimers();
//					timersCPopoutPresenter.tickTimers();
					if (hotsPopoutPresenter.isEnabled()) {
						Platform.runLater(() -> hotsPopoutPresenter.tickHots());
					}
				});
			}

			@Override
			public void onTimerUpdated(final BaseTimer timer) {
				Platform.runLater(() -> {
					switch (timer.getSlot()) {
						case A:
						default:
							timersPopoutPresenter.updateTimer(timer);
							break;
						case B:
							timersBPopoutPresenter.updateTimer(timer);
							break;
						case C:
							timersCPopoutPresenter.updateTimer(timer);
							break;
					}
				});
			}

			@Override
			public void onTimerFinished(final BaseTimer timer) {
				Platform.runLater(() -> {
					switch (timer.getSlot()) {
						case A:
						default:
							timersPopoutPresenter.removeTimer(timer);
							break;
						case B:
							timersBPopoutPresenter.removeTimer(timer);
							break;
						case C:
							timersCPopoutPresenter.removeTimer(timer);
							break;
					}
				});
			}

			@Override
			public void onTimersReset() {
				Platform.runLater(() -> {
					timersPopoutPresenter.resetTimers();
					timersBPopoutPresenter.resetTimers();
					timersCPopoutPresenter.resetTimers();
				});
			}
		});

		final ImageView twitter = new ImageView("img/icon/Twitter_logo_blue.png");
		linkTwitter.setGraphic(twitter);
		linkTwitter.setFocusTraversable(false);
		linkTwitter.setOnAction(arg0 -> {
			try {
				Desktop.getDesktop().browse(new URI("https://twitter.com/starparse"));
			} catch (Exception e) {
				logger.warn("Unable to open web client: " + e.getMessage(), e);
			}
		});
		linkTwitter.setTextFill(Paint.valueOf("#333"));

		final ImageView donate = new ImageView("img/icon/heart.png");
		linkDonate.setGraphic(donate);
		linkDonate.setFocusTraversable(false);
		linkDonate.setOnAction(arg0 -> {
			try {
				Desktop.getDesktop().browse(new URI("https://www.paypal.com/cgi-bin/webscr?cmd=_donations"
						+ "&business=marek%2edusek%40gmail%2ecom&lc=CZ&item_name=Ixale%20StarParse"
						+ "&item_number=075&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"));
			} catch (Exception e) {
				logger.warn("Unable to open web client: " + e.getMessage(), e);
			}
		});
		linkDonate.setTextFill(Paint.valueOf("#333"));
	}

	public void run(final Config config) {

		this.config = config;

		TimerManager.setConfig(config);

		// raid manager
		raidManager = new RaidManager(config, raidListener);
		if (config.getLastRaidGroupName() != null) {
			raidManager.setRaidGroup(config.getLastRaidGroupName(), config.isRaidGroupAdmin(config.getLastRaidGroupName()));
		}

		// wire tabs to controllers
		StatsTab.setOnUpdateRequestedAction(this, onUpdateRequestedAction);
		StatsTab.add(contentOverview, overviewPresenter);
		StatsTab.add(contentDamageDealt, damageDealtPresenter);
		StatsTab.add(contentHealingDone, healingDonePresenter);
		StatsTab.add(contentDamageTaken, damageTakenPresenter);
		StatsTab.add(contentHealingTaken, healingTakenPresenter);
		StatsTab.add(contentCombatLog, combatLogPresenter);
		StatsTab.add(contentRaid, raidPresenter);

		// wire popouts
		StatsPopout.setRaidPresenter(raidPresenter);
		StatsPopout.setListener(popoutPres -> {
			// ensure the content gets updated upon showing
			updateCombatStats(currentCombat, false, false, true, popoutPres);
		});
		StatsPopout.add(timersPopoutPresenter, timersAPopoutMenu, config);
		StatsPopout.add(timersBPopoutPresenter, timersBPopoutMenu, config);
		StatsPopout.add(timersCPopoutPresenter, timersCPopoutMenu, config);
		StatsPopout.add(timersCenterPopoutPresenter, timersCenterPopoutMenu, config);
		StatsPopout.add(personalStatsPopoutPresenter, personalStatsPopoutMenu, config);
		StatsPopout.add(challengesPopoutPresenter, challengesPopoutMenu, config);
		StatsPopout.add(raidDpsPopoutPresenter, raidDpsPopoutMenu, config);
		StatsPopout.add(raidHpsPopoutPresenter, raidHpsPopoutMenu, config);
		StatsPopout.add(raidTpsPopoutPresenter, raidTpsPopoutMenu, config);
		StatsPopout.add(raidBossPopoutPresenter, raidBossPopoutMenu, config);
		StatsPopout.add(hotsPopoutPresenter, hotsPopoutMenu, config);
		StatsPopout.add(raidNotesPopoutPresenter, raidNotesPopoutMenu, config);

		timersPopoutPresenter.setTimersCenterControl(timersCenterPopoutPresenter);
		timersPopoutPresenter.setFractions(config.getPopoutDefault().getTimersFractions());
		timersBPopoutPresenter.setFractions(config.getPopoutDefault().getTimersFractions());
		timersCPopoutPresenter.setFractions(config.getPopoutDefault().getTimersFractions());

		raidPresenter.addRaidUpdateListener(raidDataListener);
		raidPresenter.setRaidManager(raidManager);
		raidPresenter.setConfig(config);

		// wire settings
		settingsDialogPresenter.setConfig(config);
		settingsDialogPresenter.setStage(getRootStage());
		final SettingsDialogPresenter.SettingsUpdatedListener settings = new SettingsDialogPresenter.SettingsUpdatedListener() {
			@Override
			public void onRaidGroupsUpdated(final RaidGroup newGroup) {
				// currently raiding & relevant group affected?
				boolean found = false;
				if (raidManager.getRaidGroupName() != null) {
					for (RaidGroup rg : config.getRaidGroups()) {
						if (rg.getName().equals(raidManager.getRaidGroupName())) {
							// found
							found = true;
							break;
						}
					}
					if (!found) {
						// group is no longer valid, will force stop if running
						raidManager.setRaidGroup(null, false);
					}
				}
				rebuildRaidGroupsMenu(newGroup);
			}

			@Override
			public void onOverlaysSettings(final Color backgroundColor, final Color textColor,
					final Color damageColor, final Color healingColor, final Color threatColor, final Color friendlyColor,
					final double raidDamageOpacity, final boolean raidDamageBars,
					final double raidHealingOpacity, final boolean raidHealingBars, final String raidHealingMode,
					final double raidThreatOpacity, final boolean raidThreatBars,
					final double raidBossOpacity, final boolean raidBossBars,
					final double raidChallengesOpacity, final boolean raidChallengesBars,
					final double timersOpacity, final boolean timersBars,
					final double personalOpacity, final boolean personalBars, final String personalMode,
					final boolean timersCenter, final Double timersCenterX, final Double timersCenterY,
					final Integer fractions,
					final boolean popoutSolid) {
				StatsPopout.setSettings(backgroundColor, textColor, damageColor, healingColor, threatColor, friendlyColor,
						raidDamageOpacity, raidDamageBars,
						raidHealingOpacity, raidHealingBars, raidHealingMode,
						raidThreatOpacity, raidThreatBars,
						raidBossOpacity, raidBossBars,
						raidChallengesOpacity, raidChallengesBars,
						timersOpacity, timersBars,
						personalOpacity, personalBars, personalMode,
						timersCenter, timersCenterX, timersCenterY,
						fractions,
						popoutSolid);
			}

			@Override
			public void onOverlaysReset(String characterName) {
				reloadPopouts();
			}

			@Override
			public void onTimersUpdated() {
				rebuildTimersMenu();
			}

			@Override
			public void onUploadUpdated() {
				uploadParselyDialogPresenter.refresh();
			}

			@Override
			public void onHotkeyUpdated(Config.Hotkey hotkey, String oldHotkey, String newHotkey) {
				if (oldHotkey != null) {
					Win32Utils.unregisterHotkey(oldHotkey);
				}
				if (newHotkey == null) {
					return;
				}

				final Runnable callback;
				switch (hotkey) {
					case RAID_PULL:
						callback = () -> raidPresenter.handlePullCountdown(null);
						break;
					case LOCK_OVERLAYS:
						callback = () -> {
							lockOverlaysMenu.setSelected(!lockOverlaysMenu.isSelected());
							handleOverlaysLock(null);
						};
						break;
					default:
						callback = null;
				}
				if (callback == null) {
					logger.error("Invalid hotkey: " + hotkey);
					return;
				}

				Win32Utils.registerHotkey(newHotkey, callback, ()
						-> Platform.runLater(() -> setFlash("Unable to register hotkey " + hotkey + " [" + newHotkey + "],"
						+ " please check it's not already in use (or StarParse is not running twice)", Type.ERROR)));
			}
		};
		settingsDialogPresenter.setListener(settings);

		// wire upload
		uploadParselyDialogPresenter.setConfig(config);
		uploadParselyDialogPresenter.setStage(getRootStage());
		uploadParselyDialogPresenter.setListener(new UploadParselyListener() {
			@Override
			public void onUploadSettings() {
				settingsDialogPresenter.show();
				settingsDialogPresenter.selectUpload();
			}

			@Override
			public void onUploadSaved(String link) {
				setFlash("Uploaded succesfully: ", Type.SUCCESS, link);
			}
		});

		// wire raid notes
		raidNotesDialogPresenter.setConfig(config);
		raidNotesDialogPresenter.setStage(getRootStage());
		// listener set in RaidPresenter

		// wire time-line
		timeline = new Timeline();
		timeline.addListener((tickFrom, tickTo) -> {
			try {
				context.setTickFrom(tickFrom);
				context.setTickTo(tickTo);
				// update overview & tab, not timeline
				updateCombatStats(currentCombat, true, false, true, null);

			} catch (Exception e) {
				logger.error("Unable to select interval", e);
				e.printStackTrace();
			}
		});

		AnchorPane.setTopAnchor(timeline, 30D);
		AnchorPane.setLeftAnchor(timeline, 10D);
		AnchorPane.setRightAnchor(timeline, 10D);

		headCombat.getChildren().add(timeline);

		// load & link timers
		TimerManager.linkSystemTimers(config.getConfigTimers().getTimers());

		// setup initial menus and content
		rebuildRecentMenu();
		rebuildRaidGroupsMenu(null);
		rebuildTimersMenu();
		resetCombatStats();

		if (config.getPopoutDefault().getMouseTransparent() != null && config.getPopoutDefault().getMouseTransparent()) {
			lockOverlaysMenu.setSelected(true);
			StatsPopout.lock(true);
		}

		combatName.setText("Click 'Parse' or load an existing log via the 'File' menu");

		// load attack types
		context.addAttacks(config.getConfigAttacks().getAttacks());

		// drag & drop file support
		activateLogFileDropSupport();

		// load hotkeys
		settings.onHotkeyUpdated(Config.Hotkey.RAID_PULL, null, config.getRaidPullHotkey());
		settings.onHotkeyUpdated(Config.Hotkey.LOCK_OVERLAYS, null, config.getlockOverlaysHotkey());

		darkModeButton.setText(Boolean.TRUE.equals(config.getDarkMode()) ? "L" : "D");
	}

	public void stop() {
		StatsPopout.destroyAll();
		raidPresenter.storeLastCombatLogStats();
	}

	private void updateCombatStats(final Combat combat,
			boolean doUpdateOverview, boolean doUpdateTimeline,
			boolean doUpdatePopouts, final BasePopoutPresenter popoutPres) {

		final int combatCount;
		if (combat != null) {
			try {
				if (combatList.getSelectionModel().getSelectedItems().size() > 1) {
					stats = eventService.getCombatStats(combatList.getSelectionModel().getSelectedItems(), context.getCombatSelection(), context.getSelectedPlayer());
					combatCount = combatList.getSelectionModel().getSelectedItems().size();
				} else {
					stats = eventService.getCombatStats(combat, context.getCombatSelection(), context.getSelectedPlayer());
					combatCount = 1;
				}
			} catch (Exception e) {
				logger.error("Unable to get stats", e);
				return;
			}
		} else {
			return;
		}

		if (stats == null) {
			// combat gone away
			return;
		}

		if (doUpdateOverview) {
			updateCombatOverview(combat, stats, combatCount);
		}

		if (doUpdateTimeline) {
			updateTimeline(combat, stats);

			try {
				if (contentTabs.getTabs().get(contentTabs.getSelectionModel().getSelectedIndex()) != contentRaid) {
					// make sure raid is updated when solo parsing as well (for shielding cross-fill) // TODO: rework
					raidPresenter.updateCombatStats(combat, stats);
				}
			} catch (Exception ignored) {

			}
		}

		updateActiveTab(combat, stats);

		if (doUpdatePopouts) {
			StatsPopout.updateVisible(combat, stats, popoutPres);
		}
	}

	private void updateCombatOverview(final Combat combat, final CombatStats stats, final int combatCount) {
		final CombatInfo combatInfo = context.getCombatInfo().get(combat.getCombatId());
		combatName.setText((combatInfo != null && combatInfo.getLocationInfo() != null && combatInfo.getLocationInfo().getInstanceName() != null
				? combatInfo.getLocationInfo().getInstanceName() + ": "
				: "")
				+ Format.formatCombatName(combat));

		// discipline
		combatTime.setText((stats.getDiscipline() != null ? stats.getDiscipline() + " " : "")
				+ Format.formatCombatTime(combat));
//		Image icon = null;
//		if (combat != null && combat.getDiscipline() != null) {
//			icon = BaseStatsPresenter.getDisciplineIcon(combat.getDiscipline());
//		}
//		if (icon != null) {
//			if (disciplineIcon == null) {
//				disciplineIcon = new ImageView();
//				disciplineIcon.setFitHeight(20);
//				disciplineIcon.setFitWidth(20);
//			}
//			disciplineIcon.setImage(icon);
//			combatTime.setGraphic(disciplineIcon);
//		} else {
//			combatTime.setGraphic(null);
//		}

		apm.setText(Format.formatFloat(stats.getApm()));
		time.setText((combatCount > 1 ? "(" + combatCount + ") " : "") + Format.formatTime(stats.getTick()));

		dps.setText(Format.formatAdaptive(stats.getDps()));
		damage.setText(Format.formatMillions(stats.getDamage()));

		hps.setText(Format.formatAdaptive(stats.getHps()));
		heal.setText(Format.formatMillions(stats.getHeal()));
		ehps.setText(Format.formatAdaptive(stats.getEhps()));
		ehpsPercent.setText(Format.formatNumber(stats.getEhpsPercent()) + " %");

		tps.setText(Format.formatAdaptive(stats.getTps()));
		threat.setText(Format.formatMillions(stats.getThreat()));

		aps.setText(Format.formatAdaptive(stats.getAps()));
		absorbed.setText(Format.formatMillions(stats.getAbsorbed()));

		dtps.setText(Format.formatAdaptive(stats.getDtps()));
		damageTaken.setText(Format.formatMillions(stats.getDamageTaken()));

		healTaken.setText(Format.formatMillions(stats.getHealTaken()));
		hpsTaken.setText(Format.formatAdaptive(stats.getHpsTaken()));
		ehpsTaken.setText(Format.formatAdaptive(stats.getEhpsTaken()));
		ehpsTakenPercent.setText(Format.formatNumber(stats.getEhpsTakenPercent()) + " %");
	}

	private void updateTimeline(final Combat combat, final CombatStats stats) {
		try {
			if (combat.getTimeTo() != null) {
				// finished (or about to be), let timeline decide what to do
				timeline.update(combat, eventService.getCombatPhases(combat), stats);
			} else {
				// underway
				timeline.reset();
			}

		} catch (Exception e) {
			logger.error("Unable to update timeline", e);
			e.printStackTrace();
		}
	}

	private void updateActiveTab(final Combat combat, final CombatStats stats) {

		try {
			if (!StatsTab.update(contentTabs.getTabs().get(contentTabs.getSelectionModel().getSelectedIndex()), combat, stats)) {
				// default
				overviewPresenter.updateCombatStats(combat, stats);
			}

		} catch (Exception e) {
			logger.error("Unable to update tab", e);
			e.printStackTrace();
		}
	}

	public void resetCombatStats() {

		combatName.setText("");
		combatTime.setGraphic(null);
		combatTime.setText("");

		apm.setText("");
		time.setText("");

		dps.setText("");
		damage.setText("");

		hps.setText("");
		heal.setText("");
		ehps.setText("");
		ehpsPercent.setText("");

		tps.setText("");
		threat.setText("");

		aps.setText("");
		absorbed.setText("");

		dtps.setText("");
		damageTaken.setText("");
		healTaken.setText("");
		hpsTaken.setText("");
		ehpsTaken.setText("");
		ehpsTakenPercent.setText("");

		StatsTab.resetAll();
		StatsPopout.resetAll();

		timeline.reset();
	}

	/**
	 * Handlers
	 * ------------------------------------------------------------------------
	 */

	public void handleOpenLog(@SuppressWarnings("unused") ActionEvent event) {
		if (fileChooser == null) {
			fileChooser = new FileChooser();
			fileChooser.setTitle("Open Combat Log");
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("Combat Logs", "combat*.txt"),
					new FileChooser.ExtensionFilter("All Files", "*.*"));
		}
		final File f = new File(config.getLogDirectory());
		if (f.isDirectory() && f.exists()) {
			fileChooser.setInitialDirectory(f);
		} else {
			fileChooser.setInitialDirectory(null);
		}
		final File logFile = fileChooser.showOpenDialog(getView().getScene().getWindow());
		if (logFile == null) {
			return;
		}

		handleFileOpen(logFile);
	}

	public void handleParse(@SuppressWarnings("unused") ActionEvent event) {
		try {
			if (parseButton.isSelected()) {
				setParsing(true);
			} else {
				setRaiding(false);
				setParsing(false);

				context.setCharacterName(null);
				hidePopouts();
			}
		} catch (Exception e) {
			logger.error("Unable to parse: " + e.getMessage(), e);
			e.printStackTrace();
		}
	}

	private void setParsing(boolean doProcessDir) throws Exception {
		setParsing(doProcessDir, null);
	}

	private void setParsing(final File combatLog) throws Exception {
		setParsing(false, combatLog);
	}

	private synchronized void setParsing(boolean doProcessDir, final File combatLog) throws Exception {

		if (!doProcessDir || combatLog != null) {
			// deactivate directory watching
			parseButton.setText("Parse");
			parseButton.setSelected(false);
			stopLogWatching();
		}

		if (combatLog != null) {
			// explicit combat log parse
			startLogWatching(combatLog, null, true);

		} else if (doProcessDir) {
			// activate directory watching
			parseButton.setText("Parsing");
			parseButton.setSelected(true);

			startLogWatching(new File(config.getLogDirectory()), config.getLogPolling(), false);
		}
	}

	private void startLogWatching(final File parent, final Integer polling, boolean isFile) throws Exception {

		if (logWatcher != null && logWatcher.isAlive()) {
			return;
		}

		boolean doFileCheck = false;
		if (parser == null) {
			parser = new Parser(context);
			doFileCheck = true;
		}

		eventService.resetAll();
		// ensure its reset
		onNewFileAction.run();
		System.gc();

		logWatcher = new LogWatcher(parent, polling, isFile);
		logWatcher.addLogWatcherListener(logWatcherListener);
		logWatcher.setDaemon(true);
		if (doFileCheck) {
			logWatcher.doFileCheck();
		}
		logWatcher.start();

		if (isFile) {
			isLoadingFullFile = true;
			showFullscreenLoader("Parsing combat log ...");
			logger.info("Parsing started for combat log " + parent.getAbsolutePath());
			combatName.setText("Parsing started, please wait ...");

		} else {
			logger.info("Parsing started, waiting for a combat log at " + parent.getAbsolutePath());
			combatName.setText("Parsing started, waiting for combat log ...");

			TimerManager.start();
		}

	}

	private void stopLogWatching() {

		if (logWatcher == null) {
			return;
		}

		TimerManager.stop();

		if (logger.isDebugEnabled()) {
			logger.debug("Parsing stopping");
		}

		logWatcher.interrupt();
		try {
			logWatcher.join();
		} catch (InterruptedException e) {
			// ignore
		}

		logWatcher = null;

		if (logger.isDebugEnabled()) {
			logger.info("Parsing stopped");
		}
	}

	private void refreshCombatList(boolean doSelectLastCombat) throws Exception {

		final List<Combat> combats = eventService.getCombats();
		if (!(combats.size() > 0)) {
			return;
		}
		if (trashButton.isSelected()) {
			combatList.getItems().setAll(combats);

		} else {
			combatList.getItems().clear();
			for (Combat c : combats) {
				if (c.getBoss() != null
						|| Boolean.TRUE.equals(c.isPvp())
						|| c.isRunning()) {
					combatList.getItems().add(c);
				}
			}
			if (combatList.getItems().size() == 0) {
				// ensure the last combat is there if nothing else
				combatList.getItems().add(combats.get(combats.size() - 1));
			}
		}

		if (doSelectLastCombat) {
			// update current combat to the last one
			currentCombat = combats.get(combats.size() - 1);
		}

		if (currentCombat != null) {
			boolean found = false;
			for (int i = 0; i < combatList.getItems().size(); i++) {
				if (combatList.getItems().get(i).getCombatId() == currentCombat.getCombatId()) {
					// this will fire the selection even, setting currentCombat
					combatList.getSelectionModel().select(combatList.getItems().get(i));
					combatList.scrollTo(i);
					found = true;
					break;
				}
			}
			if (!found) {
				// trying to auto-select a combat which is hidden by trash filter
				// just select the last one available
				combatList.getSelectionModel().select(combatList.getItems().get(combatList.getItems().size() - 1));
				combatList.scrollTo(combatList.getItems().size() - 1);
			}
		}
	}

	public void handleToggleThrash(@SuppressWarnings("unused") ActionEvent event) {
		Platform.runLater(onThrashToggleAction);
	}

	public void handleRaid(@SuppressWarnings("unused") ActionEvent event) {
		try {
			if (raidButton.isSelected()) {
				setParsing(true);
				setRaiding(true);
			} else {
				setRaiding(false);
			}
		} catch (Exception e) {
			logger.error("Raiding failed: " + e.getMessage(), e);
		}
	}

	private synchronized void setRaiding(boolean isEnabled) {

		if (isEnabled) {
			raidManager.enable();
		} else {
			raidManager.disable();
		}
		onRaidStoppedAction.run();
	}

	private void handleUpload(final CombatLog combatLog, final List<Combat> allCombats, final List<Combat> selectedCombats) {

		if (combatLog == null || allCombats == null || allCombats.isEmpty()) {
			return;
		}
		uploadParselyDialogPresenter.setUpload(combatLog, allCombats, selectedCombats);
		uploadParselyDialogPresenter.show();
	}

	public void handlePopoutToggle(ActionEvent event) {
		StatsPopout.toggle((CheckMenuItem) event.getSource());
	}

	public void handleSettings(@SuppressWarnings("unused") ActionEvent event) {
		settingsDialogPresenter.show();
	}

	public void handleRaidGroupsSettings(@SuppressWarnings("unused") ActionEvent event) {
		settingsDialogPresenter.show();
		settingsDialogPresenter.selectRaidGroups();
	}

	public void handleOverlaysSettings(@SuppressWarnings("unused") ActionEvent event) {
		settingsDialogPresenter.show();
		settingsDialogPresenter.selectOverlays();
	}

	public void handleTimersSettings(@SuppressWarnings("unused") ActionEvent event) {
		settingsDialogPresenter.show();
		settingsDialogPresenter.selectTimers();
	}

	public void handleOverlaysLock(@SuppressWarnings("unused") ActionEvent event) {
		config.getPopoutDefault().setMouseTransparent(lockOverlaysMenu.isSelected());
		StatsPopout.lock(lockOverlaysMenu.isSelected());
	}

	public void handleDarkMode(@SuppressWarnings("unused") ActionEvent event) {
		config.setDarkMode("D".equals(darkModeButton.getText()));
		StarparseApp.setStylesheets(getRootStage(), this, Boolean.TRUE.equals(config.getDarkMode()));
		darkModeButton.setText(Boolean.TRUE.equals(config.getDarkMode()) ? "L" : "D");
	}

	public void handleAbout(@SuppressWarnings("unused") ActionEvent event) {
		StarparseApp.showChangelog(getRootStage());
	}

	public void handleMinimize(@SuppressWarnings("unused") ActionEvent event) {
		(getRootStage()).setIconified(true);
	}

	public void handleClose(@SuppressWarnings("unused") ActionEvent event) {
		// FIXME: save attack types
		if (config != null && context != null) {
			config.getConfigAttacks().setAttacks(context.getAttacks());
		}

		Platform.exit();
	}

	/**
	 * Menu
	 * ------------------------------------------------------------------------
	 */

	private void rebuildRecentMenu() {

		recentMenu.getItems().clear();

		if (config.getRecentOpenedLogs().isEmpty() && config.getRecentParsedLogs().isEmpty()) {
			recentMenu.setDisable(true);
			return;
		}
		recentMenu.setDisable(false);

		for (int i = config.getRecentOpenedLogs().size() - 1; i >= 0; i--) {
			recentMenu.getItems().add(new LogMenuItem(config.getRecentOpenedLogs().get(i)));
		}

		if (!config.getRecentOpenedLogs().isEmpty() && !config.getRecentParsedLogs().isEmpty()) {
			recentMenu.getItems().add(new LabelSeparatorMenuItem("Parsed"));
		}

		for (int i = config.getRecentParsedLogs().size() - 1; i >= 0; i--) {
			recentMenu.getItems().add(new LogMenuItem(config.getRecentParsedLogs().get(i)));
		}

		recentMenu.getItems().add(new SeparatorMenuItem());

		if (!config.getRecentOpenedLogs().isEmpty() || !config.getRecentParsedLogs().isEmpty()) {
			MenuItem item = new MenuItem("Clear all");
			item.setOnAction(arg0 -> {
				config.getRecentOpenedLogs().clear();
				config.getRecentParsedLogs().clear();
				rebuildRecentMenu();
			});
			recentMenu.getItems().add(item);
		}
	}

	private void rebuildRaidGroupsMenu(final RaidGroup newGroup) {
		raidGroupsMenu.getItems().clear();

		for (final RaidGroup raidGroup : config.getRaidGroups()) {
			final CheckMenuItem item = new CheckMenuItem(raidGroup.getName());

			item.setOnAction(arg0 -> {
				if (item.isSelected()) {
					for (final MenuItem mi : raidGroupsMenu.getItems()) {
						if (mi instanceof CheckMenuItem && mi != item) {
							((CheckMenuItem) mi).setSelected(false);
						}
					}
					// will force restart if running
					raidManager.setRaidGroup(raidGroup.getName(), config.isRaidGroupAdmin(raidGroup.getName()));
					config.setLastRaidGroupName(raidGroup.getName());

				} else {
					// will force stop if running
					raidManager.setRaidGroup(null, false);
					config.setLastRaidGroupName(null);
				}
			});

			if (raidManager.getRaidGroupName() != null && raidManager.getRaidGroupName().equals(raidGroup.getName())) {
				// currently raiding
				item.setSelected(true);

			} else if (config.getLastRaidGroupName() != null && raidGroup.getName().equals(config.getLastRaidGroupName())) {
				// last raid
				item.setSelected(true);

			} else if (config.getLastRaidGroupName() == null && raidManager.getRaidGroupName() == null
					&& newGroup != null && newGroup == raidGroup) {
				// just added
				item.setSelected(true);
				item.fire();

			}

			raidGroupsMenu.getItems().add(item);
		}
		if (!config.getRaidGroups().isEmpty()) {
			raidGroupsMenu.getItems().add(new SeparatorMenuItem());

		}

		raidGroupsMenu.getItems().add(raidGroupsSettingsMenu);
	}

	private void rebuildTimersMenu() {
		timersMenu.getItems().clear();
		final Map<String, List<CustomMenuItem>> folders = new HashMap<>();
		folders.put("_root", new ArrayList<>());

		// build folder tree
		for (final ConfigTimer timer : config.getConfigTimers().getTimers()) {

			final CheckBox cb = new CheckBox(timer.getName());
			cb.setSelected(timer.isEnabled());
			cb.getStyleClass().add("check-item");
			cb.setCursor(Cursor.HAND);
			cb.setPrefWidth(150);
			final CustomMenuItem item = new CustomMenuItem(cb, false);
			item.getStyleClass().add("check-item-menu");
			item.setOnAction(e -> {
				timer.setEnabled(cb.isSelected());
				if (!cb.isSelected()) {
					TimerManager.stopTimer(timer);
				}
				// FIXME: workaround for java8 bug
				if (timersMenu != item.getParentMenu() && !item.getParentMenu().isShowing()) {
					timersMenu.show();
					item.getParentMenu().show();
				}
			});

			if (timer.getFolder() != null && !timer.getFolder().isEmpty()) {
				if (!folders.containsKey(timer.getFolder())) {
					folders.put(timer.getFolder(), new ArrayList<>());

				}
				folders.get(timer.getFolder()).add(item);
			} else {
				folders.get("_root").add(item);
			}
		}
		// sort all
		for (final String folder : folders.keySet()) {
			folders.get(folder).sort(Comparator.comparing(o -> ((CheckBox) o.getContent()).getText()));
		}
		final List<String> folderNames = new ArrayList<>(folders.keySet());
		folderNames.sort((a, b) -> {
			if (a.startsWith(ConfigTimer.SYSTEM_FOLDER) && (!b.startsWith(ConfigTimer.SYSTEM_FOLDER) || a.contains("Raiding"))) {
				return 1;
			}
			if (b.startsWith(ConfigTimer.SYSTEM_FOLDER) && (!a.startsWith(ConfigTimer.SYSTEM_FOLDER) || b.contains("Raiding"))) {
				return -1;
			}
			return a.compareTo(b);
		});

		// create menu
		timersMenu.getItems().addAll(folders.get("_root"));
		boolean hasDivider = false;
		for (final String folderName : folderNames) {
			if ("_root".equals(folderName)) {
				continue;
			}
			if (!hasDivider && folderName.startsWith(ConfigTimer.SYSTEM_FOLDER)) {
				timersMenu.getItems().add(new SeparatorMenuItem());
				hasDivider = true;
			}
			final Menu folder = new Menu(folderName);
			folder.getItems().setAll(folders.get(folderName));
			timersMenu.getItems().add(folder);
		}

		if (!timersMenu.getItems().isEmpty()) {
			timersMenu.getItems().add(new SeparatorMenuItem());

		}
		timersMenu.getItems().add(timersSettingsMenu);
	}

	private void hidePopouts() {
		StatsPopout.hideAll();
	}

	private void reloadPopouts() {
		// this will show previously enabled (stored) popouts
		// if this was triggered by "new events" upon parsing, then the update (refresh) handler will be triggered twice
		// (once by the "on-showing" event and another by the generic "update stats" event)
		StatsPopout.reloadEnabled();
	}

	public void bringPopoutsToFront() {
		StatsPopout.bringPopoutsToFront();
	}

	public void setFlash(String message, FlashMessage.Type type) {
		setFlash(message, type, null);
	}

	public void setFlash(String message, FlashMessage.Type type, String link) {
		clearFlash();
		if (message == null) {
			return;
		}
		flash = new FlashMessage(headCombat, message, type, link);
	}

	protected void clearFlash() {
		if (flash != null) {
			flash.close();
			flash = null;
		}
	}

	private void handleFileOpen(final File logFile) {
		try {
			// reset
			setRaiding(false);
			setParsing(logFile);

		} catch (Exception e) {
			logger.error("Unable to open file " + logFile + ": " + e.getMessage(), e);
		}
	}

	private void activateLogFileDropSupport() {
		getView().getScene().setOnDragOver(event -> {
			if (event.getDragboard().hasFiles()) {
				event.acceptTransferModes(TransferMode.LINK);
			} else {
				event.consume();
			}
		});

		// Dropping over surface
		getView().getScene().setOnDragDropped(event -> {
			boolean success = false;
			if (event.getDragboard().hasFiles()) {
				success = true;
				for (File file : event.getDragboard().getFiles()) {
					handleFileOpen(file);
					break;
				}
			}
			event.setDropCompleted(success);
			event.consume();
		});
	}

	private Stage getRootStage() {
		return (Stage) (root.getScene().getWindow());
	}

	protected void showFullscreenLoader(@SuppressWarnings("SameParameterValue") final String text) {
		fullscreenLoader = new FullscreenLoader(getRootStage(), text);
		fullscreenLoader.show();
	}

	public void hideFullscreenLoader() {
		if (fullscreenLoader == null) {
			return;
		}
		fullscreenLoader.close();
		fullscreenLoader = null;
	}

	private void resetSelectedPlayer() {
		context.setSelectedPlayer(currentCombatLog.getCharacterName());
		characterNameMenu.setText(context.getSelectedPlayer());
		characterName.getStyleClass().remove("other-actor");
		characterNameReset.setVisible(false);
		characterNameReset.setPrefWidth(0);
		characterNameMenu.setPrefWidth(155);
	}

	class SelectedPlayerMenuLoader implements ChangeListener<Boolean> {

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			final boolean wasEmpty = characterNameMenu.getItems().isEmpty();
			characterNameMenu.getItems().clear();
			if (!newValue || currentCombat == null || characterName.getStyleClass().contains("disabled")) {
				return;
			}

			try {
				final Map<String, Integer> nameCounts = new HashMap<>();
				final List<Actor> actors = new ArrayList<>();
				for (final Actor a : eventService.getCombatActors(currentCombat, Actor.Role.SOURCE, context.getCombatSelection())) {
					if (Actor.Type.NPC.equals(a.getType()) && a.getInstanceId() == null) {
						continue; // unknown
					}
					if (!nameCounts.containsKey(a.getName())) {
						actors.add(a);
					}
					nameCounts.put(a.getName(), nameCounts.getOrDefault(a.getName(), 0) + 1);
				}

				actors.sort((a, b) -> {
					if (Actor.Type.SELF.equals(a.getType())) {
						return -1;
					}
					if (Actor.Type.SELF.equals(b.getType())) {
						return 1;
					}
					if (Actor.Type.PLAYER.equals(a.getType())) {
						if (!Actor.Type.PLAYER.equals(b.getType())) {
							return -1;
						}
					} else {
						if (Actor.Type.PLAYER.equals(b.getType())) {
							return 1;
						}
					}
					return a.compareTo(b);
				});

				boolean players = false, npcs = false;
				for (final Actor a : actors) {
					if (Actor.Type.PLAYER.equals(a.getType())) {
						if (!players) {
							characterNameMenu.getItems().add(new SeparatorMenuItem());
							players = true;
						}
					}
					if (Actor.Type.NPC.equals(a.getType())) {
						if (!npcs) {
							characterNameMenu.getItems().add(new SeparatorMenuItem());
							npcs = true;
						}
					}
					characterNameMenu.getItems().add(createMenuItem(a, nameCounts));
				}
				if (wasEmpty) {
					Platform.runLater(() -> {
						characterNameMenu.show();
					});
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}
		}

		private MenuItem createMenuItem(final Actor a, final Map<String, Integer> nameCounts) {
			final String label;
			if (nameCounts.getOrDefault(a.getName(), 0) > 1) {
				label = a.getName() + " (" + nameCounts.get(a.getName()) + ")";
			} else {
				label = a.getName();
			}
			final MenuItem m = new MenuItem(label);
			m.setOnAction(e -> handleClick(m, a));
			return m;
		}

		private <T extends MenuItem> void handleClick(final T m, final Actor a) {
			try {
				characterNameMenu.setText(m.getText());
				characterName.getStyleClass().remove("other-actor");
				if (!characterNameMenu.getText().equals(currentCombatLog.getCharacterName())) {
					characterName.getStyleClass().add("other-actor");
					characterNameReset.setVisible(true);
					characterNameReset.setPrefWidth(20);
					characterNameMenu.setPrefWidth(135);
				} else {
					characterNameReset.setVisible(false);
					characterNameReset.setPrefWidth(0);
					characterNameMenu.setPrefWidth(155);
				}
				context.setSelectedPlayer(characterNameMenu.getText().matches("^.* \\([0-9]+\\)$")
						? characterNameMenu.getText().substring(0, characterNameMenu.getText().lastIndexOf("(") - 1)
						: characterNameMenu.getText());
				updateCombatStats(currentCombat, true, false, true, null);

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

}
