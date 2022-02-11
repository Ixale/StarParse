package com.ixale.starparse.gui.main;

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
import com.ixale.starparse.gui.table.item.HealingTakenItem;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HealingDonePresenter extends BaseStatsPresenter {
	@FXML
	private CheckBox targetButton, abilityButton;

	@FXML
	private TableView<HealingAbilityItem> healingDoneTable;
	@FXML
	private TableColumn<HealingAbilityItem, String> targetNameCol, nameCol;
	@FXML
	private TableColumn<HealingAbilityItem, Integer> actionsCol, ticksCol, totalCol, avgNormalCol, avgCritCol, hpsCol,
			ehpsCol, absorbedCol, apsCol;
	@FXML
	private TableColumn<HealingAbilityItem, Double> pctCritCol, pctEffectiveCol, pctTotalCol;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		targetNameCol.setCellValueFactory(new PropertyValueFactory<>("target"));

		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

		actionsCol.setCellValueFactory(new PropertyValueFactory<>("actions"));
		ticksCol.setCellValueFactory(new PropertyValueFactory<>("ticks"));
		totalCol.setCellValueFactory(new PropertyValueFactory<>("totalEffective"));

		apsCol.setCellValueFactory(new PropertyValueFactory<>("aps"));
		absorbedCol.setCellValueFactory(new PropertyValueFactory<>("absorbed"));

		avgNormalCol.setCellValueFactory(new PropertyValueFactory<>("avgNormal"));
		avgCritCol.setCellValueFactory(new PropertyValueFactory<>("avgCrit"));
		pctCritCol.setCellValueFactory(new PropertyValueFactory<>("pctCrit"));
		pctEffectiveCol.setCellValueFactory(new PropertyValueFactory<>("pctEffective"));
		hpsCol.setCellValueFactory(new PropertyValueFactory<>("hps"));
		ehpsCol.setCellValueFactory(new PropertyValueFactory<>("ehps"));

		nameCol.setCellFactory(new DamageAbilityNameCellFactory<>());

		actionsCol.setCellFactory(new NumberCellFactory<>());
		ticksCol.setCellFactory(new NumberCellFactory<>());
		totalCol.setCellFactory(new NumberCellFactory<>(false, "healing-eff-done"));

		apsCol.setCellFactory(new NumberCellFactory<>(true, "absorbed"));
		absorbedCol.setCellFactory(new NumberCellFactory<>(true, "absorbed"));

		avgNormalCol.setCellFactory(new NumberCellFactory<>());
		avgCritCol.setCellFactory(new NumberCellFactory<>());
		hpsCol.setCellFactory(new NumberCellFactory<>(false, "healing-done"));
		ehpsCol.setCellFactory(new NumberCellFactory<>(false, "healing-eff-done"));

		pctCritCol.setCellFactory(new FloatCellFactory<>());
		pctEffectiveCol.setCellFactory(new FloatCellFactory<>());

		pctTotalCol.setCellValueFactory(new PropertyValueFactory<>("pctTotal"));
		pctTotalCol.setCellFactory(new ProgressBarCellFactory<>());

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

				row.setOnMouseEntered(event -> {
					if (row.getItem() == null) {
						return;
					}

					final StringBuilder sb = new StringBuilder();
					sb.append(row.getItem().getName());
					sb.append("\nLargest heal:\t\t").append(Format.formatNumber(row.getItem().getMax()));
					if (row.getItem().getActions() > 0) {
						sb.append("\nPer activation:\t\t").append(Format.formatNumber(
								Math.round((row.getItem().getTotal() + (row.getItem().getAbsorbed() == null ? 0 : row.getItem().getAbsorbed() * 1.0))
										/ row.getItem().getActions())
						));
					}
					t.setText(sb.append("\n").toString());
					BaseItem.showTooltip(row, t, event);
				});
				row.setOnMouseExited(event -> t.hide());

				return row;
			}
		});

		healingDoneTable.setColumnResizePolicy(new TableResizer(new TableColumn[]{targetNameCol, nameCol}, new double[]{.5, .5}));

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
		for (final HealingDoneStats hds : eventService.getHealingDoneStats(combat, targetButton.isSelected(), abilityButton.isSelected(),
				context.getCombatSelection(), context.getSelectedPlayer())) {
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

			if (hds.getAbsorbed() > 0) {
				a.aps.set(hds.getAps());
				a.absorbed.set(hds.getAbsorbed());
			}

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
