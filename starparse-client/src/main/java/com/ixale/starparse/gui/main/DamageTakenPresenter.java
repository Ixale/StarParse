package com.ixale.starparse.gui.main;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import com.ixale.starparse.domain.AttackType;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.stats.CombatMitigationStats;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.domain.stats.DamageTakenStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.table.DamageAbilityNameCellFactory;
import com.ixale.starparse.gui.table.FloatCellFactory;
import com.ixale.starparse.gui.table.NumberCellFactory;
import com.ixale.starparse.gui.table.ProgressBarCellFactory;
import com.ixale.starparse.gui.table.TableResizer;
import com.ixale.starparse.gui.table.item.BaseItem;
import com.ixale.starparse.gui.table.item.DamageTakenItem;

public class DamageTakenPresenter extends BaseStatsPresenter
{
	@FXML
	private CheckBox sourceTypeButton, sourceInstanceButton, abilityButton;

	@FXML
	private Label
		ie, iePercent, ke, kePercent, ft, ftPercent, mr, mrPercent,
		missPercent, shieldPercent,
		absorbedSelf, absorbedSelfPercent, absorbedOthers, absorbedOthersPercent;

	@FXML
	private TableView<DamageTakenItem> damageTakenTable;
	@FXML
	private TableColumn<DamageTakenItem, String> sourceNameCol, sourceTimeFromCol, nameCol, damageTypeCol, attackTypeCol;
	@FXML
	private TableColumn<DamageTakenItem, Integer> ticksCol, totalCol, absorbedCol, avgNormalCol, dtpsCol;
	@FXML
	private TableColumn<DamageTakenItem, Double> pctShieldCol, pctMissCol, pctTotalCol;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		sourceNameCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, String>("source"));
		sourceTimeFromCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, String>("since"));

		nameCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, String>("name"));
		damageTypeCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, String>("damageType"));
		attackTypeCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, String>("attackType"));

		ticksCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, Integer>("ticks"));
		totalCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, Integer>("total"));
		absorbedCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, Integer>("absorbed"));

		avgNormalCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, Integer>("avgNormal"));
		pctShieldCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, Double>("pctShield"));
		pctMissCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, Double>("pctMiss"));
		dtpsCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, Integer>("dtps"));

		nameCol.setCellFactory(new DamageAbilityNameCellFactory<DamageTakenItem>());

		absorbedCol.setCellFactory(new NumberCellFactory<DamageTakenItem>(false, "absorbed"));
		ticksCol.setCellFactory(new NumberCellFactory<DamageTakenItem>());
		totalCol.setCellFactory(new NumberCellFactory<DamageTakenItem>(false, "damage-dealt"));

		avgNormalCol.setCellFactory(new NumberCellFactory<DamageTakenItem>());
		dtpsCol.setCellFactory(new NumberCellFactory<DamageTakenItem>(false, "damage-dealt"));

		pctShieldCol.setCellFactory(new FloatCellFactory<DamageTakenItem>());
		pctMissCol.setCellFactory(new FloatCellFactory<DamageTakenItem>());

		pctTotalCol.setCellValueFactory(new PropertyValueFactory<DamageTakenItem, Double>("pctTotal"));
		pctTotalCol.setCellFactory(new ProgressBarCellFactory<DamageTakenItem>());

		damageTakenTable.setRowFactory(new Callback<TableView<DamageTakenItem>, TableRow<DamageTakenItem>>() {
			@Override
			public TableRow<DamageTakenItem> call(final TableView<DamageTakenItem> p) {

				final Tooltip t = new Tooltip("text");

				final TableRow<DamageTakenItem> row = new TableRow<DamageTakenItem>() {
					@Override
					public void updateItem(DamageTakenItem item, boolean empty) {
						super.updateItem(item, empty);
					}
				};

				row.setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (row.getItem() == null) {
							return;
						}

						final StringBuilder sb = new StringBuilder();
						sb.append(row.getItem().getName()).append("\n");
						if (row.getItem().getDamageType() != null) {
							sb.append("Damage type: ").append(row.getItem().getDamageType()).append("\n");
						}
						if (row.getItem().getAttackType() != null) {
							final String text;
							if ("FT".equals(row.getItem().getAttackType())) {
								text = "Force / Tech";
							} else if ("MR".equals(row.getItem().getAttackType())) {
								text = "Melee / Ranged";
							} else if ("++".equals(row.getItem().getAttackType())) {
								text = "Multiple (M/R + F/T)";
							} else {
								text = row.getItem().getAttackType();
							}
							sb.append("\nAttack type: ").append(text).append("\n");

							if ("++".equals(row.getItem().getAttackType())) {
								sb.append("Melee / Ranged:\t").append(Format.formatNumber(
									row.getItem().total.get() - row.getItem().totalIe.get())).append("\n");
								sb.append("Force / Tech:\t\t").append(Format.formatNumber(
									row.getItem().totalIe.get())).append("\n");
							}
						}
						sb.append("\nLargest hit:\t\t").append(Format.formatNumber(row.getItem().getMax()));
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

		damageTakenTable.setColumnResizePolicy(new TableResizer(new TableColumn[] { sourceNameCol, nameCol }, new double[] { .5, .5 }));

		pctTotalCol.setSortType(SortType.DESCENDING);

		damageTakenTable.getSortOrder().add(pctTotalCol);

		createContextMenu(damageTakenTable);
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		if (sourceTypeButton.isSelected()) {
			sourceInstanceButton.setDisable(false);
		} else {
			sourceInstanceButton.setSelected(false);
			sourceInstanceButton.setDisable(true);
		}

		sourceNameCol.setVisible((sourceTypeButton.isSelected() || sourceInstanceButton.isSelected()) || !abilityButton.isSelected());
		sourceTimeFromCol.setVisible(sourceInstanceButton.isSelected());

		nameCol.setVisible(abilityButton.isSelected());
		attackTypeCol.setVisible(abilityButton.isSelected() && combat != null && (combat.getBoss() != null || Boolean.TRUE.equals(combat.isPvp())));
		damageTypeCol.setVisible(abilityButton.isSelected());

		if (combat == null) {
			clearTable(damageTakenTable);
			return;
		}

		// mitigation overview
		final CombatMitigationStats mitiStats = eventService.getCombatMitigationStats(combat, context.getCombatSelection(), context.getSelectedPlayer());

		if (mitiStats == null) {
			// combat gone away
			resetCombatStats();
			return;
		}

		ie.setText(Format.formatMillions(mitiStats.getInternal() + mitiStats.getElemental()));
		iePercent.setText(Format.formatFloat(mitiStats.getInternalPercent() + mitiStats.getElementalPercent()) + " %");
		ke.setText(Format.formatMillions(mitiStats.getEnergy() + mitiStats.getKinetic()));
		kePercent.setText(Format.formatFloat(mitiStats.getEnergyPercent() + mitiStats.getKineticPercent()) + " %");

		// FIXME: database
		int ftTotal = 0, mrTotal = 0, total = 0;
		List<DamageTakenStats> dtStats = eventService.getDamageTakenStats(combat,
			sourceTypeButton.isSelected(), sourceInstanceButton.isSelected(), true,
			context.getCombatSelection(), context.getSelectedPlayer());
		for (final DamageTakenStats dts: dtStats) {
			total += dts.getTotal();
			if (dts.getGuid() > 0 && context.getAttacks().containsKey(dts.getGuid())) {
				switch (context.getAttacks().get(dts.getGuid())) {
				case FT:
					ftTotal += dts.getTotal();
					break;
				case MR:
					ftTotal += dts.getTotalIe();
					mrTotal += dts.getTotal() - dts.getTotalIe();
					break;
				}
			} else {
				ftTotal += dts.getTotalIe();
			}
		}
		ft.setText(Format.formatMillions(ftTotal));
		ftPercent.setText(Format.formatFloat(total > 0 ? (ftTotal * 100.0 / total) : 0) + " %");
		mr.setText(Format.formatMillions(mrTotal));
		mrPercent.setText(Format.formatFloat(total > 0 ? (mrTotal * 100.0 / total) : 0) + " %");

		missPercent.setText(Format.formatFloat(mitiStats.getMissPercent()) + " %");
		shieldPercent.setText(Format.formatFloat(mitiStats.getShieldPercent()) + " %");

		absorbedSelfPercent.setText(Format.formatFloat(mitiStats.getAbsorbedSelfPercent()) + " %");
		absorbedSelf.setText(Format.formatMillions(mitiStats.getAbsorbedSelf()));
		absorbedOthersPercent.setText(Format.formatFloat(mitiStats.getAbsorbedOthersPercent()) + " %");
		absorbedOthers.setText(Format.formatMillions(mitiStats.getAbsorbedOthers()));

		// damage taken table
		final List<DamageTakenItem> items = new ArrayList<>();
		if (!abilityButton.isSelected()) {
			dtStats = eventService.getDamageTakenStats(combat,
				sourceTypeButton.isSelected(), sourceInstanceButton.isSelected(), abilityButton.isSelected(),
				context.getCombatSelection(), context.getSelectedPlayer());
		}
		for (final DamageTakenStats dts: dtStats) {
			final DamageTakenItem a = new DamageTakenItem();

			a.guid = dts.getGuid();
			a.source.set(dts.getSource());
			a.name.set(dts.getName() == null || dts.getName().isEmpty() ? "(" + dts.getGuid() + ")" : dts.getName());
			a.absorbed.set(dts.getTotalAbsorbed());
			a.ticks.set(dts.getTicks());
			a.total.set(dts.getTotal());
			a.max.set(dts.getMax());
			a.pctTotal.set(dts.getPercentTotal());
			a.avgNormal.set((int) dts.getAverageNormal());
			a.pctShield.set(dts.getPercentShield());
			a.pctMiss.set(dts.getPercentMiss());
			a.dtps.set(dts.getDtps());
			if (dts.getDamageType() != null && !dts.getDamageType().isEmpty()) {
				a.damageType.set(dts.getDamageType());
			}
			if (dts.getGuid() > 0 && context.getAttacks().containsKey(dts.getGuid())) {
				final AttackType at = context.getAttacks().get(dts.getGuid());
				if (AttackType.MR.equals(at) && dts.getTotalIe() > 0) {
					a.attackType.set("++");
				} else {
					a.attackType.set(at.toString());
				}
			} else if (dts.getTotalIe() > 0) {
				a.attackType.set(AttackType.FT.toString());
			}
			a.totalIe.set(dts.getTotalIe());

			if (dts.getTimeFrom() != null) {
				a.since.set(Format.formatTime(dts.getTimeFrom() - combat.getTimeFrom()));
			}
			a.tickFrom.set(dts.getTimeFrom() - combat.getTimeFrom());
			a.tickTo.set(dts.getTimeTo() - combat.getTimeFrom());

			items.add(a);
		}
		damageTakenTable.getItems().setAll(items);

		resortTable(damageTakenTable);
	}

	@Override
	public void resetCombatStats() {
		ie.setText("");
		iePercent.setText("");
		ke.setText("");
		kePercent.setText("");
		ft.setText("");
		ftPercent.setText("");
		mr.setText("");
		mrPercent.setText("");

		missPercent.setText("");
		shieldPercent.setText("");

		absorbedSelfPercent.setText("");
		absorbedSelf.setText("");
		absorbedOthersPercent.setText("");
		absorbedOthers.setText("");

		clearTable(damageTakenTable);
	}
}
