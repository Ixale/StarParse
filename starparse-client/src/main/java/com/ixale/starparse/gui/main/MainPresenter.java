package com.ixale.starparse.gui.main;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.ixale.starparse.gui.popout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatLog;
import com.ixale.starparse.domain.ConfigTimer;
import com.ixale.starparse.domain.RaidGroup;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.gui.FlashMessage;
import com.ixale.starparse.gui.FlashMessage.Type;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.StarparseApp;
import com.ixale.starparse.gui.Win32Utils;
import com.ixale.starparse.gui.dialog.RaidNotesDialogPresenter;
import com.ixale.starparse.gui.dialog.SettingsDialogPresenter;
import com.ixale.starparse.gui.dialog.UploadParselyDialogPresenter;
import com.ixale.starparse.gui.dialog.UploadParselyDialogPresenter.UploadParselyListener;
import com.ixale.starparse.gui.timeline.Timeline;
import com.ixale.starparse.gui.timeline.TimelineListener;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

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
	private Label characterName, combatName, combatTime;
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
	private ImageView logoImage;
	@FXML
	private MenuItem raidGroupsSettingsMenu, timersSettingsMenu;
	@FXML
	private CheckMenuItem timersPopoutMenu, timersCenterPopoutMenu, personalStatsPopoutMenu, damageTakenPopoutMenu, challengesPopoutMenu,
		raidDpsPopoutMenu, raidHpsPopoutMenu, raidTpsPopoutMenu, hotsPopoutMenu, raidNotesPopoutMenu, lockOverlaysMenu;

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
	private TimersCenterPopoutPresenter timersCenterPopoutPresenter;
	@Inject
	private PersonalStatsPopoutPresenter personalStatsPopoutPresenter;
	@Inject
	private DamageTakenPopoutPresenter damageTakenPopoutPresenter;
	@Inject
	private ChallengesPopoutPresenter challengesPopoutPresenter;
	@Inject
	private RaidDpsPopoutPresenter raidDpsPopoutPresenter;
	@Inject
	private RaidHpsPopoutPresenter raidHpsPopoutPresenter;
	@Inject
	private RaidTpsPopoutPresenter raidTpsPopoutPresenter;
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

	// currently viewing combat
	private Combat currentCombat = null;
	// last combat available (may or may not be running and/or equal to currentCombat)
	private Combat lastCombat = null;
	// helper to store "last current combat"
	private Combat selectedCombat = null;
	private CombatLog currentCombatLog = null, lastCombatLog = null;
	private List<Combat> combats;
	private String currentCharacterName = null;

	private CombatStats stats = null;
	private Timeline timeline = null;

	private RaidManager raidManager;

	private FlashMessage flash;

	static class StatsTab {
		final BaseStatsPresenter presenter;
		final Tab tab;

		final static ArrayList<StatsTab> tabs = new ArrayList<StatsTab>();

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
					Platform.runLater(new Runnable() {
						public void run() {
							mainPresenter.setFlash(message, type);
						}
					});
				}
			});
		}

		public static boolean update(final Tab t, final Combat combat, final CombatStats stats) throws Exception {
			for (StatsTab st: tabs) {
				if (st.tab == t) {
					st.presenter.updateCombatStats(combat, stats);
					return true;
				}
			}
			return false;
		}

		public static void resetAll() {
			for (StatsTab st: tabs) {
				st.presenter.resetCombatStats();
			}
		}
	}

	static class StatsPopout {

		final BasePopoutPresenter presenter;

		final static ArrayList<StatsPopout> popouts = new ArrayList<StatsPopout>();

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
			for (final StatsPopout sp: popouts) {
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
			for (final StatsPopout sp: popouts) {
				if (sp.presenter instanceof BaseRaidPopoutPresenter && sp.presenter.getParentMenuItem().isSelected()) {
					((BaseRaidPopoutPresenter) sp.presenter).onRaidDataUpdate(combat, message);
				}
			}
		}

		public static void sendRaidDataFinalize() {
			for (final StatsPopout sp: popouts) {
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
			final double raidChallengesOpacity, final boolean raidChallengesBars,
			final double timersOpacity, final boolean timersBars,
			final double personalOpacity, final boolean personalBars, final String personalMode,
			final double damageTakenOpacity, final boolean damageTakenBars, final String damageTakenMode,
			final boolean timersCenter, final Double timersCenterX, final Double timersCenterY,
			final Integer fractions,  final Integer dtDelay1, final Integer dtDelay2,
			final boolean popoutSolid) {
			// popout settings
			for (final StatsPopout sp: popouts) {
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

					} else if (sp.presenter instanceof TimersPopoutPresenter) {
						sp.presenter.setOpacity(timersOpacity);
						sp.presenter.setBars(timersBars);
						((TimersPopoutPresenter) sp.presenter).setFractions(fractions);

					} else if (sp.presenter instanceof PersonalStatsPopoutPresenter) {
						sp.presenter.setOpacity(personalOpacity);
						sp.presenter.setBars(personalBars);
						sp.presenter.setMode(personalMode);

					}else if (sp.presenter instanceof DamageTakenPopoutPresenter) {
						sp.presenter.setOpacity(damageTakenOpacity);
						sp.presenter.setBars(damageTakenBars);
						sp.presenter.setMode(damageTakenMode);
						((DamageTakenPopoutPresenter) sp.presenter).setDtDelay1(dtDelay1);
						((DamageTakenPopoutPresenter) sp.presenter).setDtDelay2(dtDelay2);

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
			for (StatsPopout sp: popouts) {
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
			for (StatsPopout sp: popouts) {
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
			for (StatsPopout sp: popouts) {
				sp.presenter.hidePopout();
				sp.presenter.getParentMenuItem().setSelected(false);
			}
		}

		public static void bringPopoutsToFront() {
			for (StatsPopout sp: popouts) {
				sp.presenter.bringPopoutToFront();
			}
		}

		public static void lock(boolean isLocked) {
			for (StatsPopout sp: popouts) {
				sp.presenter.setMouseTransparent(isLocked);
			}
		}

		public static void resetAll() {
			for (StatsPopout sp: popouts) {
				sp.presenter.resetCombatStats();
			}
		}

		public static void destroyAll() {
			for (StatsPopout sp: popouts) {
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

			this.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent arg0) {
					handleFileOpen(new File(combatLog.getFileName()));
				}
			});
		}
	}

	public class LabelSeparatorMenuItem extends SeparatorMenuItem {
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
					characterName.setText(currentCombatLog.getCharacterName());
					logTime.setText(Format.formatCombatLogTime(currentCombatLog, null));

					// set the current combat log (will trigger full reset)
					raidPresenter.setCombatLogName(currentCombatLog.getFileName());

					hotsPopoutPresenter.resetPlayers();

				} else {
					raidPresenter.setCombatLogName(null);
				}

				combatList.getItems().clear();

				resetCombatStats();

				currentCombat = lastCombat = selectedCombat = null;

			} catch (Exception e) {
				logger.error("General error", e);
				e.printStackTrace();
			}
		}
	};

	final Runnable onNewCombatAction = new Runnable() {
		@Override
		public void run() {
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
				raidPresenter.setLastCombat(eventService.getLastCombat());

			} catch (Exception e) {
				logger.error("General error", e);
				e.printStackTrace();
			}
		}
	};

	final Runnable onNewEventsAction = new Runnable() {
		@Override
		public void run() {
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

				if ((currentCharacterName == null || !currentCharacterName.equals(currentCombatLog.getCharacterName()))
					&& currentCombatLog.getCharacterName() != null) {

					currentCharacterName = currentCombatLog.getCharacterName();
					characterName.setText(currentCharacterName);

					// will force restart if running
					raidManager.setCharacterName(currentCharacterName);
					raidPresenter.setCharacterName(currentCharacterName);

					if (parseButton.isSelected() || config.isKnownCharacter(currentCharacterName)) {
						// store as known character only if actually parsing (and not browsing others' logs)
						config.setLastCharacterName(currentCharacterName);
						reloadPopouts();

					} else {
						config.setLastCharacterName(Config.DEFAULT_CHARACTER);
					}
				}

				if ((lastCombat = eventService.getLastCombat()) != null) {
					// extend combat log time
					logTime.setText(Format.formatCombatLogTime(currentCombatLog,
						lastCombat.getTimeTo() != null ? lastCombat.getTimeTo() : lastCombat.getTimeFrom()));

					// resolve raiding BEFORE firing wide combat update
					if (raidManager.isUpdateNeeded(lastCombat)) {
						// newer data, send
						raidPresenter.updateRaidCombatStats(raidManager.sendCombatUpdate(lastCombat, eventService.getCombatStats(lastCombat, null),
							eventService.getAbsorptionStats(lastCombat, null), eventService.getCombatChallengeStats(lastCombat, null),
							context.getCombatEvents(lastCombat.getCombatId())));
					}

					// ensure the list contains updated summary
					for (int i = combatList.getItems().size() - 1; i >= 0; i--) {
						if (combatList.getItems().get(i).getCombatId() == lastCombat.getCombatId() && combatList.getItems().get(i).isRunning()) {
							// combat updated, refresh the list entry (this will fire the selection even, setting currentCombat)
							combatList.getItems().set(i, lastCombat);
							if (combatList.getItems().size() == 1) {
								// make sure the only fight is selected
								combatList.getSelectionModel().select(lastCombat);
							}
							break;
						}
					}

					// ensure raid consistency
					raidPresenter.setLastCombat(lastCombat);

					// mute timers if needed
					TimerManager.setMuted(!lastCombat.isRunning() || lastCombat.getTimeTo() != null);

				} else {
					combatName.setText("Log does not contain any combats yet ...");
				}

				if (currentCharacterName != null) {
					// TODO: leaking parser?
					hotsPopoutPresenter.setActorStates(parser.getActorStates(), currentCharacterName);
				}

			} catch (Exception e) {
				logger.error("General error", e);
				e.printStackTrace();
			}
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
				if (currentCombat == null) {
					selectedCombat = null;
					return;
				}
				if (currentCombat.getTimeTo() == null || (selectedCombat != null && currentCombat.getCombatId() != selectedCombat.getCombatId())) {
					// ensure the timeline is reset for or newly selected running combat
					context.setTickFrom(null);
					context.setTickTo(null);
					timeline.resetSelection();
				}
				// update overview, tab & timeline
				updateCombatStats(currentCombat, true, true, true, null);

				selectedCombat = currentCombat;

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
		public void onNewLine(final String line) throws Exception {
			parser.parseLogLine(line);
		}

		@Override
		public void onReadComplete() throws Exception {
			// evaluate timers if running combat
			final Combat currentCombat = parser.getCurrentCombat();

			timerService.triggerTimers(currentCombat, parser.getEvents(), config.getConfigTimers().getTimers());

			eventService.storeCombatLog(parser.getCombatLog());
			eventService.flushEvents(parser.getEvents(), parser.getCombats(), currentCombat, parser.getEffects(), parser.getCurrentEffects(),
				parser.getPhases(), parser.getCurrentPhase(), parser.getAbsorptions());
		}

		@Override
		public void onFileComplete() throws Exception {
			parser.closeCombatLogFile();
			onReadComplete();
		}

		@Override
		public void onFlashMessage(final String message, final Type type) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					setFlash((Type.ERROR.equals(type) ? "Error: " : "") + message, type);
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

			// send first update if raiding
			if (lastCombat != null) {
				try {
					raidPresenter.updateRaidCombatStats(raidManager.sendCombatUpdate(lastCombat, eventService.getCombatStats(lastCombat, null),
						eventService.getAbsorptionStats(lastCombat, null), eventService.getCombatChallengeStats(lastCombat, null),
						context.getCombatEvents(lastCombat.getCombatId())));

				} catch (Exception e) {
					logger.error("Unable to send first raid update: " + e.getMessage(), e);
				}
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
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					raidPresenter.addPlayer(characterNames);
				}
			});
		}

		@Override
		public void onPlayerQuit(final String[] characterNames) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					raidPresenter.removePlayer(characterNames);
				}
			});
		}

		@Override
		public void onError(final String message, final boolean reconnecting) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (reconnecting) {
						raidButton.setText("Waiting");
					}
					setFlash("Raiding error: " + message, Type.ERROR);
				}
			});
		}

		@Override
		public void onCombatUpdated(final RaidCombatMessage[] messages) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					raidPresenter.updateRaidCombatStats(messages);
				}
			});
		}

		@Override
		public void onRequestIncoming(final RaidRequestMessage message, final RequestIncomingCallback callback) {
			raidPresenter.onRequestIncoming(message, callback);
		};
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
			public void onNewFile() throws Exception {
				Platform.runLater(onNewFileAction);
			}

			@Override
			public void onNewCombat() throws Exception {
				Platform.runLater(onNewCombatAction);
			}

			@Override
			public void onNewEvents() throws Exception {
				Platform.runLater(onNewEventsAction);
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
		combatList.setCellFactory(new Callback<ListView<Combat>, ListCell<Combat>>() {

			public ListCell<Combat> call(ListView<Combat> combatListView) {
				return new CombatCell();
			}
		});
		// FIXME: workaround for Java FX 2.2 BSS URL bug
		combatList.setStyle("-fx-background-image: url('img/list-back.gif'); -fx-background-position: 0px -1px");

		// upload menu
		final MenuItem miCombats = new MenuItem("Upload selected combats");
		miCombats.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					handleUpload(currentCombatLog, eventService.getCombats(), combatList.getSelectionModel().getSelectedItems());
				} catch (Exception e) {
					logger.error("Unable to load combats: " + e.getMessage(), e);
				}
			}
		});
		final MenuItem miLog = new MenuItem("Upload whole combat log");
		miLog.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					handleUpload(currentCombatLog, eventService.getCombats(), null);
				} catch (Exception e) {
					logger.error("Unable to load combats: " + e.getMessage(), e);
				}
			}
		});

		final ContextMenu combatMenu = new ContextMenu(miCombats, miLog);
		combatMenu.setOnShowing(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent e) {
				if (combatList.getItems().size() > 0) {
					miLog.setDisable(false);
					int count = 0;
					if (combatList.getSelectionModel().getSelectedItems().size() > 0) {
						for (final Combat c: combatList.getSelectionModel().getSelectedItems()) {
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
			};
		});
		combatList.setContextMenu(combatMenu);

		final int[] combatSelCount = new int[]{0};
		combatList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		combatList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Combat>() {
			@Override
			public void changed(ObservableValue<? extends Combat> arg0, Combat oldValue, Combat newValue) {
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
			}
		});
		// FIXME: ugly hack to detect CTRL+click item removal (the event above is NOT being fired for that)
		combatList.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (combatSelCount[0] > 1 && combatList.getSelectionModel().getSelectedItems().size() < combatSelCount[0]) {
					combatSelCount[0] = combatList.getSelectionModel().getSelectedItems().size();
					Platform.runLater(onCombatSelectAction);
				}
			};
		});

		contentTabs.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> values, Number oldValue, Number newValue) {
				// update tab only
				updateCombatStats(currentCombat, false, false, false, null);
			}
		});

		logTime.setText("");

		TimerManager.addListener(new TimerListener() {
			@Override
			public void onTimersTick() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						timersPopoutPresenter.tickTimers();
						if (hotsPopoutPresenter.isEnabled()) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									hotsPopoutPresenter.tickHots();
								}
							});
						}
					}
				});
			}

			@Override
			public void onTimerUpdated(final BaseTimer timer) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						timersPopoutPresenter.updateTimer(timer);
					}
				});
			}

			@Override
			public void onTimerFinished(final BaseTimer timer) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						timersPopoutPresenter.removeTimer(timer);
					}
				});

			}

			@Override
			public void onTimersReset() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						timersPopoutPresenter.resetTimers();
					}
				});
			}
		});

		final ImageView twitter = new ImageView("img/icon/Twitter_logo_blue.png");
		linkTwitter.setGraphic(twitter);
		linkTwitter.setFocusTraversable(false);
		linkTwitter.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					Desktop.getDesktop().browse(new URI("https://twitter.com/starparse"));
				} catch (Exception e) {
					logger.warn("Unable to open web client: " + e.getMessage(), e);
				}
			}
		});
		linkTwitter.setTextFill(Paint.valueOf("#333"));

		final ImageView donate = new ImageView("img/icon/heart.png");
		linkDonate.setGraphic(donate);
		linkDonate.setFocusTraversable(false);
		linkDonate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					Desktop.getDesktop().browse(new URI("https://www.paypal.com/cgi-bin/webscr?cmd=_donations"
						+ "&business=marek%2edusek%40gmail%2ecom&lc=CZ&item_name=Ixale%20StarParse"
						+ "&item_number=075&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"));
				} catch (Exception e) {
					logger.warn("Unable to open web client: " + e.getMessage(), e);
				}
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
		StatsPopout.setListener(new BasePopoutPresenter.ShowingListener() {
			@Override
			public void onPopoutShowing(final BasePopoutPresenter popoutPres) {
				// ensure the content gets updated upon showing
				updateCombatStats(currentCombat, false, false, true, popoutPres);
			}
		});
		StatsPopout.add(timersPopoutPresenter, timersPopoutMenu, config);
		StatsPopout.add(timersCenterPopoutPresenter, timersCenterPopoutMenu, config);
		StatsPopout.add(personalStatsPopoutPresenter, personalStatsPopoutMenu, config);
		StatsPopout.add(damageTakenPopoutPresenter, damageTakenPopoutMenu, config);
		StatsPopout.add(challengesPopoutPresenter, challengesPopoutMenu, config);
		StatsPopout.add(raidDpsPopoutPresenter, raidDpsPopoutMenu, config);
		StatsPopout.add(raidHpsPopoutPresenter, raidHpsPopoutMenu, config);
		StatsPopout.add(raidTpsPopoutPresenter, raidTpsPopoutMenu, config);
		StatsPopout.add(hotsPopoutPresenter, hotsPopoutMenu, config);
		StatsPopout.add(raidNotesPopoutPresenter, raidNotesPopoutMenu, config);

		timersPopoutPresenter.setTimersCenterControl(timersCenterPopoutPresenter);
		timersPopoutPresenter.setFractions(config.getPopoutDefault().getTimersFractions());
		damageTakenPopoutPresenter.setDtDelay1(config.getPopoutDefault().getDtDelay1());
		damageTakenPopoutPresenter.setDtDelay2(config.getPopoutDefault().getDtDelay2());

		raidPresenter.addRaidUpdateListener(raidDataListener);
		raidPresenter.setRaidManager(raidManager);
		raidPresenter.setConfig(config);

		// wire settings
		settingsDialogPresenter.setConfig(config);
		settingsDialogPresenter.setStage((Stage) (root.getScene().getWindow()));
		final SettingsDialogPresenter.SettingsUpdatedListener settings = new SettingsDialogPresenter.SettingsUpdatedListener() {
			@Override
			public void onRaidGroupsUpdated(final RaidGroup newGroup) {
				// currently raiding & relevant group affected?
				boolean found = false;
				if (raidManager.getRaidGroupName() != null) {
					for (RaidGroup rg: config.getRaidGroups()) {
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
				final double raidChallengesOpacity, final boolean raidChallengesBars,
				final double timersOpacity, final boolean timersBars,
				final double personalOpacity, final boolean personalBars, final String personalMode,
				final double damageTakenOpacity, final boolean damageTakenBars, final String damageTakenMode,
				final boolean timersCenter, final Double timersCenterX, final Double timersCenterY,
				final Integer fractions, final Integer dtDelay1, final Integer dtDelay2,
				final boolean popoutSolid) {
				StatsPopout.setSettings(backgroundColor, textColor, damageColor, healingColor, threatColor, friendlyColor,
					raidDamageOpacity, raidDamageBars,
					raidHealingOpacity, raidHealingBars, raidHealingMode,
					raidThreatOpacity, raidThreatBars,
					raidChallengesOpacity, raidChallengesBars,
					timersOpacity, timersBars,
					personalOpacity, personalBars, personalMode,
					damageTakenOpacity, damageTakenBars, damageTakenMode,
					timersCenter, timersCenterX, timersCenterY,
					fractions, dtDelay1, dtDelay2,
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
						callback = new Runnable() {
							public void run() {
								raidPresenter.handlePullCountdown(null);
							}
						};
						break;
					case LOCK_OVERLAYS:
						callback = new Runnable() {
							public void run() {
								lockOverlaysMenu.setSelected(!lockOverlaysMenu.isSelected());
								handleOverlaysLock(null);
							}
						};
						break;
					default:
						callback = null;
				}
				if (callback == null) {
					logger.error("Invalid hotkey: " + hotkey);
					return;
				}

				Win32Utils.registerHotkey(newHotkey, callback, new Runnable() {
					@Override
					public void run() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								setFlash("Unable to register hotkey " + hotkey + " [" + newHotkey + "],"
									+ " please check it's not already in use (or StarParse is not running twice)", Type.ERROR);
							}
						});
					}
				});
			}
		};
		settingsDialogPresenter.setListener(settings);

		// wire upload
		uploadParselyDialogPresenter.setConfig(config);
		uploadParselyDialogPresenter.setStage((Stage) (root.getScene().getWindow()));
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
		raidNotesDialogPresenter.setStage((Stage) (root.getScene().getWindow()));
		// listener set in RaidPresenter

		// wire time-line
		timeline = new Timeline();
		timeline.addListener(new TimelineListener() {
			@Override
			public void onSelectedInterval(Long tickFrom, Long tickTo) {
				try {
					context.setTickFrom(tickFrom);
					context.setTickTo(tickTo);
					// update overview & tab, not timeline
					updateCombatStats(currentCombat, true, false, true, null);

				} catch (Exception e) {
					logger.error("Unable to select interval", e);
					e.printStackTrace();
				}
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
					stats = eventService.getCombatStats(combatList.getSelectionModel().getSelectedItems(), context.getCombatSelection());
					combatCount = combatList.getSelectionModel().getSelectedItems().size();
				} else {
					stats = eventService.getCombatStats(combat, context.getCombatSelection());
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
		}

		updateActiveTab(combat, stats);

		if (doUpdatePopouts) {
			StatsPopout.updateVisible(combat, stats, popoutPres);
		}
	}

	private void updateCombatOverview(final Combat combat, final CombatStats stats, final int combatCount) {
		combatName.setText(Format.formatCombatName(combat));

		// discipline
		combatTime.setText((combat != null && combat.getDiscipline() != null ? combat.getDiscipline() + " " : "")
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

	public void handleOpenLog(ActionEvent event) {
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

	public void handleParse(ActionEvent event) {
		try {
			if (parseButton.isSelected()) {
				setParsing(true);
			} else {
				setRaiding(false);
				setParsing(false);

				currentCharacterName = null;
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

		logger.debug("Parsing stopping");

		logWatcher.interrupt();
		try {
			logWatcher.join();
		} catch (InterruptedException e) {
		}

		logWatcher = null;

		logger.info("Parsing stopped");
	}

	private void refreshCombatList(boolean doSelectLastCombat) throws Exception {

		combats = eventService.getCombats();
		if (!(combats.size() > 0)) {
			return;
		}
		if (trashButton.isSelected()) {
			combatList.getItems().setAll(combats);

		} else {
			combatList.getItems().clear();
			for (Combat c: combats) {
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

	public void handleToggleThrash(ActionEvent event) {
		Platform.runLater(onThrashToggleAction);
	}

	public void handleRaid(ActionEvent event) {
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

	public void handleSettings(ActionEvent event) {
		settingsDialogPresenter.show();
	}

	public void handleRaidGroupsSettings(ActionEvent event) {
		settingsDialogPresenter.show();
		settingsDialogPresenter.selectRaidGroups();
	}

	public void handleOverlaysSettings(ActionEvent event) {
		settingsDialogPresenter.show();
		settingsDialogPresenter.selectOverlays();
	}

	public void handleTimersSettings(ActionEvent event) {
		settingsDialogPresenter.show();
		settingsDialogPresenter.selectTimers();
	}

	public void handleOverlaysLock(ActionEvent event) {
		config.getPopoutDefault().setMouseTransparent(lockOverlaysMenu.isSelected());
		StatsPopout.lock(lockOverlaysMenu.isSelected());
	}

	public void handleAbout(ActionEvent event) {
		StarparseApp.showChangelog((Stage) (root.getScene().getWindow()));
	}

	public void handleMinimize(ActionEvent event) {
		((Stage) (root.getScene().getWindow())).setIconified(true);
	}

	public void handleClose(ActionEvent event) {
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
			item.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent arg0) {
					config.getRecentOpenedLogs().clear();
					config.getRecentParsedLogs().clear();
					rebuildRecentMenu();
				}
			});
			recentMenu.getItems().add(item);
		}
	}

	private void rebuildRaidGroupsMenu(final RaidGroup newGroup) {
		raidGroupsMenu.getItems().clear();

		for (final RaidGroup raidGroup: config.getRaidGroups()) {
			final CheckMenuItem item = new CheckMenuItem(raidGroup.getName());

			item.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent arg0) {
					if (item.isSelected()) {
						for (final MenuItem mi: raidGroupsMenu.getItems()) {
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
		folders.put("_root", new ArrayList<CustomMenuItem>());

		// build folder tree
		for (final ConfigTimer timer: config.getConfigTimers().getTimers()) {

			final CheckBox cb = new CheckBox(timer.getName());
			cb.setSelected(timer.isEnabled());
			cb.getStyleClass().add("check-item");
			cb.setCursor(Cursor.HAND);
			cb.setPrefWidth(150);
			final CustomMenuItem item = new CustomMenuItem(cb, false);
			item.getStyleClass().add("check-item-menu");
			item.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					timer.setEnabled(cb.isSelected());
					if (!cb.isSelected()) {
						TimerManager.stopTimer(timer.getName());
					}
					// FIXME: workaround for java8 bug
					if (timersMenu != item.getParentMenu() && !item.getParentMenu().isShowing()) {
						timersMenu.show();
						item.getParentMenu().show();
					}
				}
			});

			if (timer.getFolder() != null && !timer.getFolder().isEmpty()) {
				if (!folders.containsKey(timer.getFolder())) {
					folders.put(timer.getFolder(), new ArrayList<CustomMenuItem>());

				}
				folders.get(timer.getFolder()).add(item);
			} else {
				folders.get("_root").add(item);
			}
		}
		// sort all
		for (final String folder: folders.keySet()) {
			Collections.sort(folders.get(folder), new Comparator<CustomMenuItem>() {
				@Override
				public int compare(CustomMenuItem o1, CustomMenuItem o2) {
					return ((CheckBox) o1.getContent()).getText().compareTo(((CheckBox) o2.getContent()).getText());
				}
			});
		}
		final List<String> folderNames = new ArrayList<>(folders.keySet());
		Collections.sort(folderNames, new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				if (a.startsWith(ConfigTimer.SYSTEM_FOLDER) && (!b.startsWith(ConfigTimer.SYSTEM_FOLDER) || a.contains("Raiding"))) {
					return 1;
				}
				if (b.startsWith(ConfigTimer.SYSTEM_FOLDER) && (!a.startsWith(ConfigTimer.SYSTEM_FOLDER) || b.contains("Raiding"))) {
					return -1;
				}
				return a.compareTo(b);
			}
		});

		// create menu
		timersMenu.getItems().addAll(folders.get("_root"));
		boolean hasDivider = false;
		for (final String folderName: folderNames) {
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
		getView().getScene().setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().hasFiles()) {
					event.acceptTransferModes(TransferMode.LINK);
				} else {
					event.consume();
				}
			}
		});

		// Dropping over surface
		getView().getScene().setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				boolean success = false;
				if (event.getDragboard().hasFiles()) {
					success = true;
					for (File file: event.getDragboard().getFiles()) {
						handleFileOpen(file);
						break;
					}
				}
				event.setDropCompleted(success);
				event.consume();
			}
		});
	}
}
