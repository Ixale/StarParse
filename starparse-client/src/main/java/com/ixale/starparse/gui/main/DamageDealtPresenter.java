package com.ixale.starparse.gui.main;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.domain.stats.DamageDealtStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.table.DamageAbilityNameCellFactory;
import com.ixale.starparse.gui.table.FloatCellFactory;
import com.ixale.starparse.gui.table.NumberCellFactory;
import com.ixale.starparse.gui.table.ProgressBarCellFactory;
import com.ixale.starparse.gui.table.TableResizer;
import com.ixale.starparse.gui.table.item.BaseItem;
import com.ixale.starparse.gui.table.item.DamageAbilityItem;

public class DamageDealtPresenter extends BaseStatsPresenter
{
	@FXML
	private CheckBox targetTypeButton, targetInstanceButton, abilityButton;

	@FXML
	private TableView<DamageAbilityItem> damageDealtTable;
	@FXML
	private TableColumn<DamageAbilityItem, String> targetNameCol, targetTimeFromCol, nameCol;
	@FXML
	private TableColumn<DamageAbilityItem, Integer> actionsCol, ticksCol, totalCol, avgNormalCol, avgCritCol,
		avgTotalCol, dpsCol;
	@FXML
	private TableColumn<DamageAbilityItem, Double> pctCritCol, pctMissCol, pctTotalCol;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		targetNameCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, String>("target"));
		targetTimeFromCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, String>("since"));

		nameCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, String>("name"));

		actionsCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Integer>("actions"));
		ticksCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Integer>("ticks"));
		totalCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Integer>("total"));

		avgNormalCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Integer>("avgNormal"));
		avgCritCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Integer>("avgCrit"));
		avgTotalCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Integer>("avgTotal"));
		pctCritCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Double>("pctCrit"));
		pctMissCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Double>("pctMiss"));
		dpsCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Integer>("dps"));

		nameCol.setCellFactory(new DamageAbilityNameCellFactory<DamageAbilityItem>());

		actionsCol.setCellFactory(new NumberCellFactory<DamageAbilityItem>());
		ticksCol.setCellFactory(new NumberCellFactory<DamageAbilityItem>());
		totalCol.setCellFactory(new NumberCellFactory<DamageAbilityItem>(false, "maroon"));

		avgNormalCol.setCellFactory(new NumberCellFactory<DamageAbilityItem>());
		avgCritCol.setCellFactory(new NumberCellFactory<DamageAbilityItem>());
		avgTotalCol.setCellFactory(new NumberCellFactory<DamageAbilityItem>());
		dpsCol.setCellFactory(new NumberCellFactory<DamageAbilityItem>(false, "maroon"));

		pctCritCol.setCellFactory(new FloatCellFactory<DamageAbilityItem>());
		pctMissCol.setCellFactory(new FloatCellFactory<DamageAbilityItem>());

		pctTotalCol.setCellValueFactory(new PropertyValueFactory<DamageAbilityItem, Double>("pctTotal"));
		pctTotalCol.setCellFactory(new ProgressBarCellFactory<DamageAbilityItem>());

		damageDealtTable.setRowFactory(new Callback<TableView<DamageAbilityItem>, TableRow<DamageAbilityItem>>() {
			@Override
			public TableRow<DamageAbilityItem> call(final TableView<DamageAbilityItem> p) {

				final Tooltip t = new Tooltip("text");

				final TableRow<DamageAbilityItem> row = new TableRow<DamageAbilityItem>() {
					@Override
					public void updateItem(DamageAbilityItem item, boolean empty) {
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
						sb.append(row.getItem().getName());
						if (row.getItem().getDamageType() != null) {
							sb.append("\nDamage type: ").append(row.getItem().getDamageType()).append("\n");
						}
						sb.append("\nLargest hit:\t\t").append(Format.formatNumber(row.getItem().getMax()));
						if (row.getItem().getActions() > 0) {
							sb.append("\nPer activation:\t\t").append(Format.formatNumber(
								Math.round(row.getItem().getTotal() * 1.0 / row.getItem().getActions())
								));
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

		damageDealtTable.setColumnResizePolicy(new TableResizer(new TableColumn[] { targetNameCol, nameCol }, new double[] { .5, .5 }));

		pctTotalCol.setSortType(SortType.DESCENDING);

		damageDealtTable.getSortOrder().add(pctTotalCol);

		createContextMenu(damageDealtTable);
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		if (targetTypeButton.isSelected()) {
			targetInstanceButton.setDisable(false);
		} else {
			targetInstanceButton.setSelected(false);
			targetInstanceButton.setDisable(true);
		}

		targetNameCol.setVisible((targetTypeButton.isSelected() || targetInstanceButton.isSelected()) || !abilityButton.isSelected());
		targetTimeFromCol.setVisible(targetInstanceButton.isSelected());

		nameCol.setVisible(abilityButton.isSelected());
		actionsCol.setVisible(abilityButton.isSelected() && !targetTypeButton.isSelected() && !targetInstanceButton.isSelected());

		if (combat == null) {
			clearTable(damageDealtTable);
			return;
		}

		final List<DamageAbilityItem> items = new ArrayList<>();
		for (final DamageDealtStats dds: eventService.getDamageDealtStats(combat,
			targetTypeButton.isSelected(), targetInstanceButton.isSelected(), abilityButton.isSelected(),
			context.getCombatSelection())) {
			final DamageAbilityItem a = new DamageAbilityItem();

			a.guid = dds.getGuid();
			a.target.set(dds.getTarget());
			a.name.set(dds.getName());
			a.actions.set(dds.getActions());
			a.ticks.set(dds.getTicks());
			a.total.set(dds.getTotal());
			a.pctTotal.set(dds.getPercentTotal());
			a.avgNormal.set((int) dds.getAverageNormal());
			a.avgCrit.set((int) dds.getAverageCrit());
			a.avgTotal.set((int) dds.getAverageHit());
			a.pctCrit.set(dds.getPercentCrit());
			a.pctMiss.set(dds.getPercentMiss());
			a.max.set(dds.getMax());
			a.dps.set(dds.getDps());
			a.damageType.set(dds.getDamageType());
			if (dds.getTimeFrom() != null) {
				a.since.set(Format.formatTime(dds.getTimeFrom() - combat.getTimeFrom()));
			}
			a.tickFrom.set(dds.getTimeFrom() - combat.getTimeFrom());
			a.tickTo.set(dds.getTimeTo() - combat.getTimeFrom());

			items.add(a);
		}
		damageDealtTable.getItems().setAll(items);

		resortTable(damageDealtTable);
	}

	@Override
	public void resetCombatStats() {
		clearTable(damageDealtTable);
	}
}
