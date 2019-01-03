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
import com.ixale.starparse.domain.stats.HealingDoneStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.table.DamageAbilityNameCellFactory;
import com.ixale.starparse.gui.table.FloatCellFactory;
import com.ixale.starparse.gui.table.NumberCellFactory;
import com.ixale.starparse.gui.table.ProgressBarCellFactory;
import com.ixale.starparse.gui.table.TableResizer;
import com.ixale.starparse.gui.table.item.BaseItem;
import com.ixale.starparse.gui.table.item.HealingAbilityItem;

public class HealingDonePresenter extends BaseStatsPresenter
{
	@FXML
	private CheckBox targetButton, abilityButton;

	@FXML
	private TableView<HealingAbilityItem> healingDoneTable;
	@FXML
	private TableColumn<HealingAbilityItem, String> targetNameCol, nameCol;
	@FXML
	private TableColumn<HealingAbilityItem, Integer> actionsCol, ticksCol, totalCol, avgNormalCol, avgCritCol, hpsCol,
		ehpsCol;
	@FXML
	private TableColumn<HealingAbilityItem, Double> pctCritCol, pctEffectiveCol, pctTotalCol;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		targetNameCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, String>("target"));

		nameCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, String>("name"));

		actionsCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Integer>("actions"));
		ticksCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Integer>("ticks"));
		totalCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Integer>("totalEffective"));

		avgNormalCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Integer>("avgNormal"));
		avgCritCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Integer>("avgCrit"));
		pctCritCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Double>("pctCrit"));
		pctEffectiveCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Double>("pctEffective"));
		hpsCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Integer>("hps"));
		ehpsCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Integer>("ehps"));

		nameCol.setCellFactory(new DamageAbilityNameCellFactory<HealingAbilityItem>());

		actionsCol.setCellFactory(new NumberCellFactory<HealingAbilityItem>());
		ticksCol.setCellFactory(new NumberCellFactory<HealingAbilityItem>());
		totalCol.setCellFactory(new NumberCellFactory<HealingAbilityItem>(false, "Limegreen"));

		avgNormalCol.setCellFactory(new NumberCellFactory<HealingAbilityItem>());
		avgCritCol.setCellFactory(new NumberCellFactory<HealingAbilityItem>());
		hpsCol.setCellFactory(new NumberCellFactory<HealingAbilityItem>(false, "DarkSeaGreen"));
		ehpsCol.setCellFactory(new NumberCellFactory<HealingAbilityItem>(false, "Limegreen"));

		pctCritCol.setCellFactory(new FloatCellFactory<HealingAbilityItem>());
		pctEffectiveCol.setCellFactory(new FloatCellFactory<HealingAbilityItem>());

		pctTotalCol.setCellValueFactory(new PropertyValueFactory<HealingAbilityItem, Double>("pctTotal"));
		pctTotalCol.setCellFactory(new ProgressBarCellFactory<HealingAbilityItem>());

		healingDoneTable.setRowFactory(new Callback<TableView<HealingAbilityItem>, TableRow<HealingAbilityItem>>() {
			@Override
			public TableRow<HealingAbilityItem> call(final TableView<HealingAbilityItem> p) {

				final Tooltip t = new Tooltip("text");

				final TableRow<HealingAbilityItem> row = new TableRow<HealingAbilityItem>() {
					@Override
					public void updateItem(HealingAbilityItem item, boolean empty) {
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
						sb.append("\nLargest heal:\t\t").append(Format.formatNumber(row.getItem().getMax()));
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

		healingDoneTable.setColumnResizePolicy(new TableResizer(new TableColumn[] { targetNameCol, nameCol }, new double[] { .5, .5 }));

		pctTotalCol.setSortType(SortType.DESCENDING);

		healingDoneTable.getSortOrder().add(pctTotalCol);

		createContextMenu(healingDoneTable);
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		targetNameCol.setVisible(targetButton.isSelected() || !abilityButton.isSelected());

		nameCol.setVisible(abilityButton.isSelected());
		actionsCol.setVisible(abilityButton.isSelected() && !targetButton.isSelected());

		if (combat == null) {
			clearTable(healingDoneTable);
			return;
		}

		final List<HealingAbilityItem> items = new ArrayList<>();
		for (final HealingDoneStats hds: eventService.getHealingDoneStats(combat, targetButton.isSelected(), abilityButton.isSelected(),
			context.getCombatSelection())) {
			final HealingAbilityItem a = new HealingAbilityItem();

			a.guid = hds.getGuid();
			a.target.set(hds.getTarget());
			a.name.set(hds.getName());
			a.actions.set(hds.getActions());
			a.ticks.set(hds.getTicks());
			a.total.set(hds.getTotal());
			a.max.set(hds.getMax());
			a.totalEffective.set(hds.getTotalEffective());
			a.pctTotal.set(hds.getPercentTotal());
			a.avgNormal.set((int) hds.getAverageNormal());
			a.avgCrit.set((int) hds.getAverageCrit());
			a.pctCrit.set(hds.getPercentCrit());
			a.pctEffective.set(hds.getPercentEffective());
			a.hps.set(hds.getHps());
			a.ehps.set(hds.getEhps());

			a.tickFrom.set(hds.getTimeFrom() - combat.getTimeFrom());
			a.tickTo.set(hds.getTimeTo() - combat.getTimeFrom());

			items.add(a);
		}
		healingDoneTable.getItems().setAll(items);

		resortTable(healingDoneTable);
	}

	@Override
	public void resetCombatStats() {
		clearTable(healingDoneTable);
	}
}
