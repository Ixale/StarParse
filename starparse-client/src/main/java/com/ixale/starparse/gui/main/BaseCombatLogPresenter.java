package com.ixale.starparse.gui.main;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.Entity;
import com.ixale.starparse.domain.EntityGuid;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.table.DamageAbilityNameCellFactory;
import com.ixale.starparse.gui.table.EventAbsorptionCellFactory;
import com.ixale.starparse.gui.table.EventActionCellFactory;
import com.ixale.starparse.gui.table.EventActorCellFactory;
import com.ixale.starparse.gui.table.EventEffectsCellFactory;
import com.ixale.starparse.gui.table.EventTimeCellFactory;
import com.ixale.starparse.gui.table.EventValueCellFactory;
import com.ixale.starparse.gui.table.NumberCellFactory;
import com.ixale.starparse.gui.table.TableResizer;
import com.ixale.starparse.gui.table.item.BaseItem;
import com.ixale.starparse.gui.table.item.EventItem;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

abstract public class BaseCombatLogPresenter extends BaseStatsPresenter {

	@FXML
	protected CheckBox damageDealtButton, damageTakenButton, healingDoneButton, healingTakenButton, actionsButton;
	@FXML
	protected TableView<EventItem> combatLogTable;
	@FXML
	protected TableColumn<EventItem, String> sourceNameCol, targetNameCol, abilityCol, effectsCol;
	@FXML
	protected TableColumn<EventItem, Event> actionCol;
	@FXML
	protected TableColumn<EventItem, Integer> timeCol, valueCol, absorbedCol, overhealCol, threatCol;

	protected final HashSet<Event.Type> filterFlags = new HashSet<>();
	protected final HashMap<CheckBox, Event.Type> toggles = new HashMap<>();

	protected void initializeCombatLogTable() {
		timeCol.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getEvent().getEventId()));

		sourceNameCol.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getEvent().getSource().getName()));
		targetNameCol.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getEvent().getTarget().getName()));

		actionCol.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getEvent()));
		abilityCol.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getAbility()));

		valueCol.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getValue()));
		absorbedCol.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getEvent().getAbsorbed()));
		overhealCol.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getOverheal()));

		threatCol.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getThreat()));

		effectsCol.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getEvent().getSource().getName()));

		actionCol.setCellFactory(new EventActionCellFactory<>());
		abilityCol.setCellFactory(new DamageAbilityNameCellFactory<>());

		sourceNameCol.setCellFactory(new EventActorCellFactory<>(EventActorCellFactory.Type.SOURCE));
		targetNameCol.setCellFactory(new EventActorCellFactory<>(EventActorCellFactory.Type.TARGET));

		timeCol.setCellFactory(new EventTimeCellFactory<>());
		valueCol.setCellFactory(new EventValueCellFactory<>(true));
		absorbedCol.setCellFactory(new EventAbsorptionCellFactory<>(true));
		overhealCol.setCellFactory(new NumberCellFactory<>(true, "healing-done"));
		threatCol.setCellFactory(new NumberCellFactory<>(true, "threat"));

		effectsCol.setCellFactory(new EventEffectsCellFactory<>());

		combatLogTable.setRowFactory(new Callback<TableView<EventItem>, TableRow<EventItem>>() {
			@Override
			public TableRow<EventItem> call(final TableView<EventItem> p) {

				final Tooltip t = new Tooltip("text");

				final TableRow<EventItem> row = new TableRow<EventItem>() {
					@Override
					public void updateItem(EventItem item, boolean empty) {
						super.updateItem(item, empty);
					}
				};

				row.setOnMouseEntered(event -> {
					if (row.getItem() == null) {
						return;
					}
					final Event e = row.getItem().getEvent();
					if (e.getDamage() == null && e.getMitigation() == null) {
						return;
					}

					final StringBuilder sb = new StringBuilder();
					sb.append(Format.formatTime(e.getTimestamp(), true, true)).append("\n");
					sb.append((e.getSource() == null ? "?" : e.getSource().getName()))
							.append(": ")
							.append(e.getAbility() == null ? "?" : e.getAbility().getName()).append("\n");
					if (e.getDamage() != null) {
						sb.append("\nDamage type: ").append(e.getDamage().getName()).append("\n");
					}

					final String text;
					if (e.getDamage() != null && e.getDamage().getGuid() != null &&
							(EntityGuid.Internal.getGuid() == e.getDamage().getGuid()
									|| EntityGuid.Elemental.getGuid() == e.getDamage().getGuid())) {
						text = "Force / Tech";

					} else if (e.getAbility() != null && e.getAbility().getGuid() != null
							&& context.getAttacks().containsKey(e.getAbility().getGuid())) {
						switch (context.getAttacks().get(e.getAbility().getGuid())) {
							case FT:
								text = "Force / Tech";
								break;
							default:
								text = "Melee / Ranged";
						}

					} else {
						text = null;
					}
					if (text != null) {
						sb.append("Attack type: ").append(text).append("\n");
					}
					if (e.getMitigation() != null) {
						sb.append("Mitigation: ").append(e.getMitigation().getName()).append("\n");
					}
					if (e.getReflect() != null) {
						sb.append("Reflect: ").append(e.getReflect().getName()).append("\n");
					}
					final String absText = Format.formatAbsorptionName(e);
					if (absText != null) {
						sb.append("\nAbsorbed: ").append(absText).append("\n");
					}
					t.setText(sb.append("\n").toString());
					BaseItem.showTooltip(row, t, event);
				});
				row.setOnMouseExited(event -> t.hide());

				return row;
			}
		});

		combatLogTable.setColumnResizePolicy(new TableResizer(
				new TableColumn[]{sourceNameCol, targetNameCol, abilityCol, effectsCol},
				new double[]{.2, .2, .5, .1}));

		timeCol.setSortType(SortType.ASCENDING);

		combatLogTable.getSortOrder().add(timeCol);
		combatLogTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		createContextMenu(combatLogTable);

		combatLogTable.setOnKeyPressed(ke -> {
			if (!(ke.isControlDown() && ke.getText().equals("c"))) {
				return;
			}
			if (combatLogTable.getSelectionModel().getSelectedItems().isEmpty()) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			for (EventItem e : combatLogTable.getSelectionModel().getSelectedItems()) {
				if (sb.length() > 0) {
					sb.append("\n ");
				}
				sb.append(e.toString());
			}
			copyToClipboard(sb.toString());
		});
		combatLogTable.setFocusTraversable(true);
	}

	@Override
	protected void createContextMenu(final TableView<? extends BaseItem> table) {
		super.createContextMenu(table);

		final Tooltip t = new Tooltip();
		final TextArea ta = new TextArea();

		ta.setEditable(false);
		ta.setFocusTraversable(false);
		ta.setStyle("-fx-background-color: silver;"
				+ " -fx-padding: 0;");

		t.setGraphic(ta);
		t.setAutoHide(true);
		t.setAutoFix(true);
		t.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		t.setGraphicTextGap(0);
		t.setHideOnEscape(true);
		t.setStyle("-fx-padding: 1;");

		ta.setPrefColumnCount(50);
		ta.setPrefRowCount(10);
		ta.setPrefHeight(150);
		ta.setPrefWidth(320);

		final MenuItem miDetails = new MenuItem("Show details");
		miDetails.setOnAction(e -> {
			if (table.getSelectionModel().getSelectedItems().isEmpty()) {
				return;
			}

			ta.setText(getEventDetail(((EventItem) table.getSelectionModel().getSelectedItems().get(0)).getEvent()));
			t.show(table, table.getContextMenu().getX(), table.getContextMenu().getY());
		});

		table.setOnMouseClicked(arg0 -> {
			if (t.isShowing()) {
				t.hide();
			}
		});

		table.getContextMenu().getItems().add(miDetails);
		table.getContextMenu().setOnShowing(new MenuShowingHandler(table) {
			@Override
			public void handle(WindowEvent arg0) {
				super.handle(arg0);

				if (table.getSelectionModel().getSelectedItems().isEmpty()) {
					miDetails.setDisable(true);
					return;
				}
				miDetails.setDisable(false);
			}
		});
	}

	protected void fillCombatLogTable(final Combat combat, final List<Event> events, final List<Effect> effects) {

		final List<EventItem> items = new ArrayList<>();
		for (final Event e : events) {
			items.add(new EventItem(e, e.getTimestamp() - combat.getTimeFrom(), effects));
		}
		combatLogTable.getItems().setAll(items);

		resortTable(combatLogTable);
	}

	private void copyToClipboard(Object text) {
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();

		content.put(DataFormat.PLAIN_TEXT, text.toString());
		clipboard.setContent(content);
	}

	private String getEventDetail(final Event e) {
		final StringBuilder sb = new StringBuilder();

		sb.append(Format.formatTime(e.getTimestamp(), true, true)).append("\n");
		sb.append("Source: ").append(getEntityDetail(e.getSource())).append("\n");
		sb.append("Target: ").append(getEntityDetail(e.getTarget())).append("\n");
		if (e.getAbility() != null) {
			sb.append("Ability: ").append(getEntityDetail(e.getAbility())).append("\n");
		}
		// if (e.getAction() != null) {
		// sb.append("Action: ").append(getEntityDetail(e.getAction())).append("\n");
		// }
		if (e.getEffect() != null) {
			sb.append("Effect: ").append(getEntityDetail(e.getEffect())).append("\n");
		}
		if (e.getDamage() != null) {
			sb.append("Type: ").append(getEntityDetail(e.getDamage())).append("\n");
		}
		if (e.getMitigation() != null) {
			sb.append("Mitigation: ").append(getEntityDetail(e.getMitigation())).append("\n");
		}
		if (e.getAbsorbtion() != null) {
			sb.append("Absorption: ").append(getEntityDetail(e.getAbsorbtion())).append(" (").append(e.getAbsorbed()).append(")").append("\n");
		}
		if (e.getReflect() != null) {
			sb.append("Reflect: ").append(getEntityDetail(e.getReflect())).append("\n");
		}
		if (e.getValue() != null) {
			sb.append("Value: ").append(e.getValue()).append(e.isCrit() ? "*" : "").append((e.getThreat() != null ? " <" + e.getThreat() + ">" : ""));
		} else if (e.getThreat() != null) {
			sb.append("Threat: <").append(e.getThreat()).append(">");
		}

		return sb.toString();
	}

	private String getEntityDetail(Entity entity) {
		if (entity == null) {
			return "Unknown";
		}
		if (entity.getName() == null || entity.getName().isEmpty()) {
			return String.valueOf(entity.getGuid());
		}
		return entity.getName() + (entity.getGuid() != null ? " (" + entity.getGuid() + ")" : "");
	}
}