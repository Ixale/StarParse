package com.ixale.starparse.gui.main;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.domain.stats.HealingTakenStats;
import com.ixale.starparse.gui.table.DamageAbilityNameCellFactory;
import com.ixale.starparse.gui.table.FloatCellFactory;
import com.ixale.starparse.gui.table.NumberCellFactory;
import com.ixale.starparse.gui.table.ProgressBarCellFactory;
import com.ixale.starparse.gui.table.TableResizer;
import com.ixale.starparse.gui.table.item.HealingTakenItem;

public class HealingTakenPresenter extends BaseStatsPresenter
{
	@FXML
	private CheckBox sourceButton, abilityButton;

	@FXML
	private TableView<HealingTakenItem> healingTakenTable;
	@FXML
	private TableColumn<HealingTakenItem, String> sourceNameCol, nameCol;
	@FXML
	private TableColumn<HealingTakenItem, Integer> ticksCol, totalCol, avgNormalCol, avgCritCol, htpsCol, ehtpsCol,
		absorbedCol, apsCol;
	@FXML
	private TableColumn<HealingTakenItem, Double> pctCritCol, pctEffectiveCol, pctTotalCol;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		sourceNameCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, String>("source"));

		nameCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, String>("name"));

		ticksCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Integer>("ticks"));

		avgNormalCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Integer>("avgNormal"));
		avgCritCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Integer>("avgCrit"));
		pctCritCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Double>("pctCrit"));

		htpsCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Integer>("htps"));
		ehtpsCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Integer>("ehtps"));
		totalCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Integer>("totalEffective"));

		apsCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Integer>("aps"));
		absorbedCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Integer>("absorbed"));

		pctEffectiveCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Double>("pctEffective"));

		nameCol.setCellFactory(new DamageAbilityNameCellFactory<HealingTakenItem>());

		ticksCol.setCellFactory(new NumberCellFactory<HealingTakenItem>());

		avgNormalCol.setCellFactory(new NumberCellFactory<HealingTakenItem>());
		avgCritCol.setCellFactory(new NumberCellFactory<HealingTakenItem>());
		htpsCol.setCellFactory(new NumberCellFactory<HealingTakenItem>(false, "healing-done"));
		ehtpsCol.setCellFactory(new NumberCellFactory<HealingTakenItem>(false, "healing-eff-done"));
		totalCol.setCellFactory(new NumberCellFactory<HealingTakenItem>(false, "healing-eff-done"));

		apsCol.setCellFactory(new NumberCellFactory<HealingTakenItem>(true, "absorbed"));
		absorbedCol.setCellFactory(new NumberCellFactory<HealingTakenItem>(true, "absorbed"));

		pctCritCol.setCellFactory(new FloatCellFactory<HealingTakenItem>());
		pctEffectiveCol.setCellFactory(new FloatCellFactory<HealingTakenItem>());

		pctTotalCol.setCellValueFactory(new PropertyValueFactory<HealingTakenItem, Double>("pctTotal"));
		pctTotalCol.setCellFactory(new ProgressBarCellFactory<HealingTakenItem>());

		healingTakenTable.setColumnResizePolicy(new TableResizer(new TableColumn[] { sourceNameCol, nameCol }, new double[] { .5, .5 }));

		pctTotalCol.setSortType(SortType.DESCENDING);

		healingTakenTable.getSortOrder().add(pctTotalCol);

		createContextMenu(healingTakenTable);
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		sourceNameCol.setVisible(sourceButton.isSelected() || !abilityButton.isSelected());

		nameCol.setVisible(abilityButton.isSelected());

		if (combat == null) {
			clearTable(healingTakenTable);
			return;
		}

		final List<HealingTakenItem> items = new ArrayList<>();
		for (HealingTakenStats hts: eventService.getHealingTakenStats(combat, sourceButton.isSelected(), abilityButton.isSelected(),
			context.getCombatSelection(), context.getSelectedPlayer())) {
			final HealingTakenItem a = new HealingTakenItem();

			a.guid = hts.getGuid();
			a.source.set(hts.getSource());
			a.name.set(hts.getName());
			a.ticks.set(hts.getTicks());
			a.total.set(hts.getTotal());
			a.totalEffective.set(hts.getTotalEffective());
			a.pctTotal.set(hts.getPercentTotal());
			a.avgNormal.set((int) hts.getAverageNormal());
			a.avgCrit.set((int) hts.getAverageCrit());
			a.pctCrit.set(hts.getPercentCrit());
			a.pctEffective.set(hts.getPercentEffective());
			a.htps.set(hts.getHtps());
			a.ehtps.set(hts.getEhtps());

			if (hts.getAbsorbed() > 0) {
				a.aps.set(hts.getAps());
				a.absorbed.set(hts.getAbsorbed());
			}
			a.tickFrom.set(hts.getTimeFrom() - combat.getTimeFrom());
			a.tickTo.set(hts.getTimeTo() - combat.getTimeFrom());

			items.add(a);
		}
		healingTakenTable.getItems().setAll(items);

		resortTable(healingTakenTable);
	}

	@Override
	public void resetCombatStats() {
		clearTable(healingTakenTable);
	}
}
