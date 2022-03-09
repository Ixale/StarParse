package com.ixale.starparse.gui.main;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.table.EventEffectsCellFactory;
import com.ixale.starparse.gui.table.item.EventItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class CombatLogPresenter extends BaseCombatLogPresenter {
	@FXML
	private CheckBox eventsOnSelfButton, eventsOnOthersButton, simplifiedButton;

	@FXML
	private MenuButton sourceMenu, targetMenu;
	@FXML
	private TextField searchText;
	@FXML
	private Button resetButton, findButton;

	private ActorMenuLoader sourceLoader, targetLoader;

	private Actor filterSource, filterTarget = null;
	private String filterSearch = null;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {

		initializeCombatLogTable();

		sourceLoader = new ActorMenuLoader(sourceMenu, Actor.Role.SOURCE);
		sourceMenu.showingProperty().addListener(sourceLoader);

		targetLoader = new ActorMenuLoader(targetMenu, Actor.Role.TARGET);
		targetMenu.showingProperty().addListener(targetLoader);

		filterFlags.clear();
		filterFlags.add(Event.Type.SIMPLIFIED);
	}

	@Override
	public void toggleBreakdown(final ActionEvent event) throws Exception {
		if (toggles.isEmpty()) {
			toggles.put(damageDealtButton, Event.Type.DAMAGE_DEALT);
			toggles.put(damageTakenButton, Event.Type.DAMAGE_TAKEN);
			toggles.put(healingDoneButton, Event.Type.HEALING_DONE);
			toggles.put(healingTakenButton, Event.Type.HEALING_TAKEN);
			toggles.put(actionsButton, Event.Type.ACTIONS);
			toggles.put(eventsOnSelfButton, Event.Type.EVENT_SELF);
			toggles.put(eventsOnOthersButton, Event.Type.EVENT_OTHERS);
			toggles.put(simplifiedButton, Event.Type.SIMPLIFIED);
		}
		for (CheckBox b : toggles.keySet()) {
			if (b.isSelected()) {
				filterFlags.add(toggles.get(b));
			} else {
				filterFlags.remove(toggles.get(b));
			}
		}
		if (searchText.getText() != null && !searchText.getText().isEmpty()) {
			filterSearch = searchText.getText();
		} else {
			filterSearch = null;
		}

		resetButton.setDisable(filterSearch == null
				&& (filterFlags.contains(Event.Type.SIMPLIFIED) && filterFlags.size() == 1)
				&& filterSource == null && filterTarget == null);

		super.toggleBreakdown(event);
	}

	public void resetBreakdown(@SuppressWarnings("unused") final ActionEvent event) throws Exception {
		searchText.setText(null);
		sourceLoader.reset();
		targetLoader.reset();

		for (final CheckBox b : toggles.keySet()) {
			b.setSelected(b == simplifiedButton);
		}

		toggleBreakdown(null);
	}

	public void searchTable(@SuppressWarnings("unused") final ActionEvent event) {
		if (searchText.getText() == null || searchText.getText().isEmpty()) {
			return;
		}
		findButton.setDisable(true);

		Platform.runLater(() -> {
			final Pattern needle = Pattern.compile(".*" + searchText.getText() + ".*", Pattern.CASE_INSENSITIVE);

			boolean wrapped = false;
			int j = combatLogTable.getSelectionModel().getSelectedIndex() + 1;
			for (int i = j; i < combatLogTable.getItems().size() && (!wrapped || i < j); i++) {
				final EventItem it = combatLogTable.getItems().get(i);
				if (needle.matcher(it.getAction()).find()
						|| needle.matcher(it.getAbility()).find()
						|| needle.matcher(it.getEvent().getSource().getName()).find()
						|| needle.matcher(it.getEvent().getTarget().getName()).find()) {
					combatLogTable.getSelectionModel().clearAndSelect(i);
					combatLogTable.scrollTo(i);
					break;
				}
				if (i + 1 == combatLogTable.getItems().size() && !wrapped) {
					i = 0;
					wrapped = true;
				}
			}
			findButton.setDisable(false);
		});
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		if (combat == null) {
			clearTable(combatLogTable);
			return;
		}

		final List<Effect> effects = eventService.getCombatEffects(combat, context.getCombatSelection());

		final List<Event> events = eventService.getCombatEvents(combat, filterFlags, filterSource, filterTarget, filterSearch,
				context.getCombatSelection(), context.getSelectedPlayer());

		fillCombatLogTable(combat, events, effects);

		sourceLoader.setCombat(combat, context.getTickFrom(), context.getTickTo());
		targetLoader.setCombat(combat, context.getTickFrom(), context.getTickTo());
	}

	@Override
	public void resetCombatStats() {
		sourceLoader.setCombat(null, null, null);
		targetLoader.setCombat(null, null, null);

		EventEffectsCellFactory.reset();

		clearTable(combatLogTable);
	}

	class ActorMenuLoader implements ChangeListener<Boolean> {

		MenuButton mb;
		Combat combat;
		Long tickFrom, tickTo;

		private final Long GUID_PLAYER = -1L, GUID_OTHER = -2L;

		final Actor.Role role;
		final TreeMap<Actor, TreeSet<Actor>> menuTree = new TreeMap<>(Comparator.naturalOrder());

		public ActorMenuLoader(final MenuButton mb, final Actor.Role role) {
			this.mb = mb;
			this.role = role;
		}

		public void setCombat(final Combat combat, final Long tickFrom, final Long tickTo) {
			if (this.combat == null || combat == null
					|| this.combat.getCombatId() != combat.getCombatId()
					|| this.combat.getEventIdTo() == null
					|| ((tickFrom == null && this.tickFrom != null) || (tickFrom != null && (!tickFrom.equals(this.tickFrom))))
					|| ((tickTo == null && this.tickTo != null) || (tickTo != null && (!tickTo.equals(this.tickTo))))) {
				mb.getItems().clear();
			}
			this.combat = combat;
			this.tickFrom = tickFrom;
			this.tickTo = tickTo;
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!newValue || !mb.getItems().isEmpty() || combat == null) {
				return;
			}

			// rebuild menu tree
			menuTree.clear();

			try {
				final HashMap<Long, TreeSet<Actor>> items = new HashMap<>();
				for (final Actor a : eventService.getCombatActors(combat, role, context.getCombatSelection())) {
					final Long guid;
					if (a.getType() != Actor.Type.NPC) {
						guid = GUID_PLAYER;
					} else if (a.getInstanceId() == null) {
						guid = GUID_OTHER;
					} else {
						guid = a.getGuid();
					}
					if (!items.containsKey(guid)) {
						items.put(guid, new TreeSet<>());
					}
					items.get(guid).add(a);
				}
				for (final Long k : items.keySet()) {
					// ensure proper sorting
					menuTree.put(items.get(k).first(), items.get(k).size() > 1 ? items.get(k) : null);
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}

			for (final Actor a : menuTree.keySet()) {
				if (a.getType() != Actor.Type.NPC || a.getInstanceId() == null || menuTree.get(a) != null) {
					final String label;
					if (a.getType() != Actor.Type.NPC) {
						label = "Players";
					} else if (a.getInstanceId() == null) {
						label = "Others";
					} else {
						if (a.getName().matches(".* [NS][0-9]$")) {
							label = a.getName().replaceFirst(" [NS][0-9]$", "");
						} else {
							label = a.getName();
						}
					}

					final Menu m = new Menu(label + " (" + (menuTree.get(a) == null ? 1 : menuTree.get(a).size()) + ")");

					if (menuTree.get(a) != null) {
						if (a.getType() == Actor.Type.NPC) {
							m.getItems().add(createMenuItem(new Actor(label + " (all)", a.getType(), a.getGuid())));
						}
						for (Actor sa : menuTree.get(a)) {
							m.getItems().add(createMenuItem(sa));
						}
					} else {
						m.getItems().add(createMenuItem(a));
					}
					mb.getItems().add(m);

				} else {
					mb.getItems().add(createMenuItem(a));
				}
			}

			mb.getItems().add(createMenuItem(null));
		}

		private MenuItem createMenuItem(final Actor a) {
			final MenuItem m;
			final String label;
			if (a == null) {
				label = getDefaultLabel();
			} else if (a.getType().equals(Actor.Type.NPC) && a.getTimeFrom() != null) {
				label = a.getName() + " @ " + Format.formatTime(a.getTimeFrom() - combat.getTimeFrom());
			} else {
				label = a.getName();
			}
			m = new MenuItem(label);
			m.setOnAction(e -> handleClick(m, a));
			return m;
		}

		private <T extends MenuItem> void handleClick(final T m, final Actor a) {
			try {
				if (role == Actor.Role.SOURCE) {
					filterSource = a;
				} else {
					filterTarget = a;
				}
				mb.setText(m.getGraphic() != null && m.getGraphic() instanceof Label ? ((Label) m.getGraphic()).getText() : m.getText());
				toggleBreakdown(null);

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		public void reset() {
			mb.setText(getDefaultLabel());
			if (role == Actor.Role.SOURCE) {
				filterSource = null;
			} else {
				filterTarget = null;
			}
		}

		private String getDefaultLabel() {
			return (role == Actor.Role.SOURCE ? "All Sources" : "All Targets");
		}
	}

}