package com.ixale.starparse.gui.main;

import static com.ixale.starparse.parser.Helpers.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

abstract public class BaseCombatLogPresenter extends BaseStatsPresenter {

	@FXML
	protected CheckBox damageDealtButton, damageTakenButton, healingDoneButton, healingTakenButton, actionsButton;
	@FXML
	protected TableView<EventItem> combatLogTable;
	@FXML
	protected TableColumn<EventItem, String> sourceNameCol, targetNameCol, actionCol, abilityCol, effectsCol;
	@FXML
	protected TableColumn<EventItem, Integer> timeCol, valueCol, absorbedCol, overhealCol, threatCol;

	protected final EnumSet<Event.Type> filterFlags = EnumSet.allOf(Event.Type.class);
	protected final HashMap<CheckBox, Event.Type> toggles = new HashMap<CheckBox, Event.Type>();

	protected void initializeCombatLogTable() {
		timeCol.setCellValueFactory(new PropertyValueFactory<EventItem, Integer>("eventId"));

		sourceNameCol.setCellValueFactory(new PropertyValueFactory<EventItem, String>("sourceName"));
		targetNameCol.setCellValueFactory(new PropertyValueFactory<EventItem, String>("targetName"));

		actionCol.setCellValueFactory(new PropertyValueFactory<EventItem, String>("action"));
		abilityCol.setCellValueFactory(new PropertyValueFactory<EventItem, String>("ability"));

		valueCol.setCellValueFactory(new PropertyValueFactory<EventItem, Integer>("value"));
		absorbedCol.setCellValueFactory(new PropertyValueFactory<EventItem, Integer>("absorbed"));
		overhealCol.setCellValueFactory(new PropertyValueFactory<EventItem, Integer>("overheal"));

		threatCol.setCellValueFactory(new PropertyValueFactory<EventItem, Integer>("threat"));

		effectsCol.setCellValueFactory(new PropertyValueFactory<EventItem, String>("sourceName")); // XXX

		actionCol.setCellFactory(new EventActionCellFactory<EventItem>());
		abilityCol.setCellFactory(new DamageAbilityNameCellFactory<EventItem>());

		sourceNameCol.setCellFactory(new EventActorCellFactory<EventItem>(EventActorCellFactory.Type.SOURCE));
		targetNameCol.setCellFactory(new EventActorCellFactory<EventItem>(EventActorCellFactory.Type.TARGET));

		timeCol.setCellFactory(new EventTimeCellFactory<EventItem>());
		valueCol.setCellFactory(new EventValueCellFactory<EventItem>(true));
		absorbedCol.setCellFactory(new EventAbsorptionCellFactory<EventItem>(true));
		overhealCol.setCellFactory(new NumberCellFactory<EventItem>(true, "DarkSeaGreen"));
		threatCol.setCellFactory(new NumberCellFactory<EventItem>(true, "Goldenrod"));

		effectsCol.setCellFactory(new EventEffectsCellFactory<EventItem>());

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

				row.setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
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

						} else
							if (e.getAbility() != null && e.getAbility().getGuid() != null
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

		combatLogTable.setColumnResizePolicy(new TableResizer(
			new TableColumn[]{sourceNameCol, targetNameCol, abilityCol, effectsCol},
			new double[]{.2, .2, .5, .1}));

		timeCol.setSortType(SortType.ASCENDING);

		combatLogTable.getSortOrder().add(timeCol);
		combatLogTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		createContextMenu(combatLogTable);

		combatLogTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (!(ke.isControlDown() && ke.getText().equals("c"))) {
					return;
				}
				if (combatLogTable.getSelectionModel().getSelectedItems().isEmpty()) {
					return;
				}
				StringBuilder sb = new StringBuilder();
				for (EventItem e: combatLogTable.getSelectionModel().getSelectedItems()) {
					if (sb.length() > 0) {
						sb.append("\n ");
					}
					sb.append(e.toString());
				}
				copyToClipboard(sb.toString());
			}
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
		miDetails.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (table.getSelectionModel().getSelectedItems().isEmpty()) {
					return;
				}

				ta.setText(getEventDetail(((EventItem) table.getSelectionModel().getSelectedItems().get(0)).getEvent()));
				t.show(table, table.getContextMenu().getX(), table.getContextMenu().getY());
			}
		});

		table.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if (t.isShowing()) {
					t.hide();
				}
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
		for (final Event e: events) {
			final EventItem a = new EventItem(e, effects);

			a.eventId.set(e.getEventId());
			a.tick.set(e.getTimestamp() - combat.getTimeFrom());

			if (e.getSource() != null) {
				a.sourceName.set(e.getSource().getName());
			}
			if (e.getTarget() != null) {
				a.targetName.set(e.getTarget().getName());
			}

			final EntityGuid effect = getEntityGuid(e.getEffect());
			final EntityGuid action = getEntityGuid(e.getAction());

			a.action.set(getSimplifiedAction(e, effect, action));
			a.ability.set(getSimplifiedAbility(e, effect));

			a.actionIcon.set(getActionIcon(e, effect, action));

			if (isEffectDamage(e) && e.getAbsorbed() != null && isTargetThisPlayer(e)) {
				a.value.set(e.getValue() - e.getAbsorbed());
			} else if (isEffectHeal(e)) {
				a.value.set(e.getEffectiveHeal() != null ? e.getEffectiveHeal() : 0);
				a.overheal.set(e.getValue() - (e.getEffectiveHeal() != null ? e.getEffectiveHeal() : 0));
			} else {
				a.value.set(e.getValue());
			}

			if (e.getAbsorbed() != null) {
				a.absorbed.set(e.getAbsorbed());
			}
			if (e.getThreat() != null) {
				a.threat.set((int) Math.min(Math.max(e.getThreat(), Integer.MIN_VALUE), Integer.MAX_VALUE));
			}

			items.add(a);
		}
		combatLogTable.getItems().setAll(items);

		resortTable(combatLogTable);
	}

	private String getSimplifiedAction(final Event e, final EntityGuid effect, final EntityGuid action) {
		if (effect != null) {
			switch (effect) {
				case AbilityActivate:
					return "Activated";
				case AbilityDeactivate:
					return "Deactivated";
				case AbilityCancel:
					return "Cancelled";
				case AbilityInterrupt:
					return "Interrupted";
				case FailedEffect:
					return "Failed Effect";

				case FallingDamage:
					return "Fell Down";
				case Damage:
					return "Hit";
				case Heal:
					return "Healed";

				case Taunt:
					return "Taunted";
				case ModifyThreat:
					return "Modified Threat";
				case NoLongerSuspicious:
					return "No Longer Suspicious";
				case EnterCombat:
					return "Entered Combat";
				case ExitCombat:
					return "Exited Combat";

				case Death:
					return "Killed";
				case Revived:
					return "Revived";

				case Crouch:
					return "Entered Cover";
				case LeaveCover:
					return "Exited Cover";
				default:
			}
		}

		if (action != null) {
			switch (action) {
				case ApplyEffect:
					return "Applied Effect";
				case RemoveEffect:
					return "Removed Effect";
				case Spend:
					return "Spent";
				case Restore:
					return "Restored";
				default:
			}
		}

		if (e.getAction() != null) {
			return e.getAction().getName();
		}

		return null;
	}

	private String getSimplifiedAbility(final Event e, final EntityGuid effect) {
		if (effect != null) {
			switch (effect) {
				case Death:
				case EnterCombat:
				case ExitCombat:
				case Revived:
				case Crouch:
				case LeaveCover:
				case FallingDamage:
				case ModifyThreat:
					return e.getEffect().getName();

				case AbilityActivate:
				case AbilityDeactivate:
				case AbilityInterrupt:
				case AbilityCancel:
				case Damage:
				case Heal:
				case Taunt:
				case FailedEffect:
					// ability name is enough
					if (e.getAbility() != null) {
						if (e.getAbility().getName().isEmpty()) {
							return "(" + e.getAbility().getGuid() + ")"; // e.g. Shadow's ID 3298019487252480
						}
						return e.getAbility().getName();
					}
				default:
					// FALLTHROUGH
			}
		}

		if (e.getAbility() == null
			|| e.getAbility().getName() == null
			|| (e.getAbility() != null && e.getAbility().getGuid() != null
				&& e.getEffect() != null && e.getEffect().getGuid() != null
				&& e.getAbility().getGuid().equals(e.getEffect().getGuid()))
			|| (e.getAbility() != null && e.getAbility().getName() != null
				&& e.getEffect() != null && e.getEffect().getName() != null
				&& e.getEffect().getName().contains(e.getAbility().getName()))) {
			// effect name is enough
			return e.getEffect().getName();
		}

		// both ability and effect name
		return e.getAbility().getName() + ": " + e.getEffect().getName();
	}

	private String getActionIcon(final Event e, final EntityGuid effect, final EntityGuid action) {
		if (effect != null) {
			switch (effect) {
				case AbilityActivate:
					return "up.png";
				case AbilityDeactivate:
					return "down.png";
				case AbilityCancel:
					return "cancel.png";
				case AbilityInterrupt:
					return "delete.png";
				case FailedEffect:
					return "cancel.png";

				case FallingDamage:
					return "fire.png";
				case Damage:
					return "target2.png";
				case Heal:
					return "heal2.png";

				case Taunt:
					return "shield.png";
				case ModifyThreat:
					return "shield-strike.png";
				case NoLongerSuspicious:
					return "shield-strike.png";
				case EnterCombat:
					return "lighting.png";
				case ExitCombat:
					return "lighting.png";

				case Death:
					return "fire.png";
				case Revived:
					return "heart-stroke.png";

				case Crouch:
					return "lighting.png";
				case LeaveCover:
					return "lighting.png";
				default:
			}
		}

		if (action != null) {
			switch (action) {
				case ApplyEffect:
					return "up2.png";
				case RemoveEffect:
					return "down2.png";
				case Spend:
					return "inject.png";
				case Restore:
					return "eject.png";
				default:
			}
		}

		return "lighting.png";
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
			sb.append("Absorption: ").append(getEntityDetail(e.getAbsorbtion())).append(" (" + e.getAbsorbed() + ")").append("\n");
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