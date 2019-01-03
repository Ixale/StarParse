package com.ixale.starparse.gui.main;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.EffectKey;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.domain.stats.CombatTickStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.chart.BaseLineChart;
import com.ixale.starparse.gui.chart.DamageChart;
import com.ixale.starparse.gui.chart.DamageTakenChart;
import com.ixale.starparse.gui.chart.DpsChart;
import com.ixale.starparse.gui.chart.DtpsChart;
import com.ixale.starparse.gui.chart.HealingChart;
import com.ixale.starparse.gui.chart.HpsChart;
import com.ixale.starparse.gui.table.EffectActorCellFactory;
import com.ixale.starparse.gui.table.EffectActorCellFactory.Type;
import com.ixale.starparse.gui.table.FloatCellFactory;
import com.ixale.starparse.gui.table.NumberCellFactory;
import com.ixale.starparse.gui.table.TableResizer;
import com.ixale.starparse.gui.table.TimeCellFactory;
import com.ixale.starparse.gui.table.item.EffectItem;

public class OverviewPresenter extends BaseStatsPresenter
{
	@FXML
	private StackPane dpsChartWrapper, hpsChartWrapper, dtpsChartWrapper;
	@FXML
	private GridPane mineEffectsWrapper, groupEffectsWrapper;
	@FXML
	private ScrollPane containerWrapper;

	@FXML
	private TableView<EffectItem> procsTable, actTable, recTable, givTable;
	@FXML
	private TableColumn<EffectItem, String> procNameCol, actNameCol, recSourceCol, recNameCol, givTargetCol,
		givNameCol;
	@FXML
	private TableColumn<EffectItem, Integer> procCntCol, procDurCol, actCntCol, actDurCol, recCntCol, recDurCol,
		givCntCol, givDurCol;
	@FXML
	private TableColumn<EffectItem, Double> procPctCol, actPctCol, recPctCol, givPctCol;

	private DamageChart damageChart = null;
	private DpsChart dpsChart = null;
	private HealingChart healingChart = null;
	private HpsChart hpsChart = null;
	private DamageTakenChart damageTakenChart = null;
	private DtpsChart dtpsChart = null;

	final private LinkedHashMap<EffectKey, EffectItem>
		procEffects = new LinkedHashMap<EffectKey, EffectItem>(),
		actEffects = new LinkedHashMap<EffectKey, EffectItem>(),
		recEffects = new LinkedHashMap<EffectKey, EffectItem>(),
		givEffects = new LinkedHashMap<EffectKey, EffectItem>();

	class EffectCell extends TableCell<EffectItem, String> {
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				getTableRow().getStyleClass().clear();
				setText(null);
				setCursor(Cursor.DEFAULT);
				return;
			}
			setText(item);
			setCursor(Cursor.HAND);

			updateEffectCellStyles(this, false);
		}
	}

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		recSourceCol.setCellValueFactory(new PropertyValueFactory<EffectItem, String>("source"));
		givTargetCol.setCellValueFactory(new PropertyValueFactory<EffectItem, String>("target"));

		procNameCol.setCellValueFactory(new PropertyValueFactory<EffectItem, String>("name"));
		actNameCol.setCellValueFactory(new PropertyValueFactory<EffectItem, String>("name"));
		recNameCol.setCellValueFactory(new PropertyValueFactory<EffectItem, String>("name"));
		givNameCol.setCellValueFactory(new PropertyValueFactory<EffectItem, String>("name"));

		procCntCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Integer>("count"));
		actCntCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Integer>("count"));
		recCntCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Integer>("count"));
		givCntCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Integer>("count"));

		procDurCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Integer>("duration"));
		actDurCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Integer>("duration"));
		recDurCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Integer>("duration"));
		givDurCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Integer>("duration"));

		procDurCol.setCellFactory(new TimeCellFactory<EffectItem>());
		actDurCol.setCellFactory(new TimeCellFactory<EffectItem>());
		recDurCol.setCellFactory(new TimeCellFactory<EffectItem>());
		givDurCol.setCellFactory(new TimeCellFactory<EffectItem>());

		procPctCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Double>("pct"));
		actPctCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Double>("pct"));
		recPctCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Double>("pct"));
		givPctCol.setCellValueFactory(new PropertyValueFactory<EffectItem, Double>("pct"));

		procCntCol.setCellFactory(new NumberCellFactory<EffectItem>());
		actCntCol.setCellFactory(new NumberCellFactory<EffectItem>());
		recCntCol.setCellFactory(new NumberCellFactory<EffectItem>());
		givCntCol.setCellFactory(new NumberCellFactory<EffectItem>());

		procPctCol.setCellFactory(new FloatCellFactory<EffectItem>());
		actPctCol.setCellFactory(new FloatCellFactory<EffectItem>());
		recPctCol.setCellFactory(new FloatCellFactory<EffectItem>());
		givPctCol.setCellFactory(new FloatCellFactory<EffectItem>());

		procsTable.setColumnResizePolicy(new TableResizer(new TableColumn[] { procNameCol }, new double[] { 1 }));
		actTable.setColumnResizePolicy(new TableResizer(new TableColumn[] { actNameCol }, new double[] { 1 }));
		recTable.setColumnResizePolicy(new TableResizer(new TableColumn[] { recSourceCol, recNameCol }, new double[] { .4, .6 }));
		givTable.setColumnResizePolicy(new TableResizer(new TableColumn[] { givTargetCol, givNameCol }, new double[] { .4, .6 }));

		procPctCol.setSortType(SortType.DESCENDING);
		actPctCol.setSortType(SortType.DESCENDING);
		recPctCol.setSortType(SortType.DESCENDING);
		givPctCol.setSortType(SortType.DESCENDING);

		procsTable.getSortOrder().add(procPctCol);
		actTable.getSortOrder().add(actPctCol);
		recTable.getSortOrder().add(procPctCol);
		givTable.getSortOrder().add(actPctCol);

		final Callback<TableColumn<EffectItem, String>, TableCell<EffectItem, String>> effectNameFactory =
			new Callback<TableColumn<EffectItem, String>, TableCell<EffectItem, String>>() {

				@Override
				public TableCell<EffectItem, String> call(TableColumn<EffectItem, String> p) {
					final EffectCell c = new EffectCell();

					c.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent me) {
							updateEffectCellStyles(c, true);
							me.consume();
						}
					});

					return c;
				}
			};

		procNameCol.setCellFactory(effectNameFactory);
		actNameCol.setCellFactory(effectNameFactory);
		recNameCol.setCellFactory(effectNameFactory);
		givNameCol.setCellFactory(effectNameFactory);

		recSourceCol.setCellFactory(new EffectActorCellFactory<EffectItem>(Type.SOURCE));
		givTargetCol.setCellFactory(new EffectActorCellFactory<EffectItem>(Type.TARGET));

		hpsChartWrapper.setVisible(false);
		dtpsChartWrapper.setVisible(false);
		groupEffectsWrapper.setVisible(false);
	}

	public void toggleDpsChart(ActionEvent event) throws Exception {
		toggleWrapper(dpsChartWrapper, ((CheckBox) event.getSource()).isSelected(), 165);
	}

	public void toggleHpsChart(ActionEvent event) throws Exception {
		toggleWrapper(hpsChartWrapper, ((CheckBox) event.getSource()).isSelected(), 165);
	}

	public void toggleDtpsChart(ActionEvent event) throws Exception {
		toggleWrapper(dtpsChartWrapper, ((CheckBox) event.getSource()).isSelected(), 165);
	}

	public void toggleMineEffects(ActionEvent event) throws Exception {
		toggleWrapper(mineEffectsWrapper, ((CheckBox) event.getSource()).isSelected(), 165);
	}

	public void toggleGroupEffects(ActionEvent event) throws Exception {
		toggleWrapper(groupEffectsWrapper, ((CheckBox) event.getSource()).isSelected(), 165);
	}

	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		boolean ticksNeeded = dpsChartWrapper.isVisible() || hpsChartWrapper.isVisible() || dtpsChartWrapper.isVisible()
			|| mineEffectsWrapper.isVisible() || groupEffectsWrapper.isVisible();
		final List<CombatTickStats> ticks;

		if (ticksNeeded) {
			ticks = eventService.getCombatTicks(combat, context.getCombatSelection());
		} else {
			ticks = null;
		}

		if (ticks == null || (ticksNeeded && stats == null)) {
			// combat gone away
			return;
		}

		if (dpsChartWrapper.isVisible()) {
			updateDamageChart(ticks);
		}
		if (hpsChartWrapper.isVisible()) {
			updateHealingChart(ticks);
		}
		if (dtpsChartWrapper.isVisible()) {
			updateDamageTakenChart(ticks);
		}

		if (mineEffectsWrapper.isVisible() || groupEffectsWrapper.isVisible()) {
			updateEffectsTables(combat, stats);
		}

		refreshChartOverlay();
	}

	protected void refreshChartOverlay() {

		if (dpsChartWrapper.isVisible()) {
			damageChart.refreshOverlay();
		}
		if (hpsChartWrapper.isVisible()) {
			healingChart.refreshOverlay();
		}
		if (dtpsChartWrapper.isVisible()) {
			damageTakenChart.refreshOverlay();
		}
	}

	public void resetCombatStats() {

		resetCharts(damageChart, dpsChart);
		resetCharts(healingChart, hpsChart);
		resetCharts(damageTakenChart, dtpsChart);

		if (procsTable.getItems() != null) {
			procsTable.getItems().clear();
			actTable.getItems().clear();
			recTable.getItems().clear();
			givTable.getItems().clear();
		}
	}

	private void createCharts(final StackPane wrapper, final BaseLineChart... charts) {

		for (int i = 0; i < charts.length; i++) {
			wrapper.getChildren().add(charts[i].getChartNode());
		}

		wrapper.getChildren().add(BaseLineChart.getLegendNode(charts));
		wrapper.getChildren().add(charts[charts.length - 1].getOverlayNode());
		wrapper.getChildren().add(charts[charts.length - 1].getOverlayButtonNode());

		charts[charts.length - 1].getOverlayButtonNode().pressedProperty().addListener(new ChangeListener<Boolean>() {
			@SuppressWarnings("unchecked")
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				updateEffectsInOverlays(true, procsTable, actTable, recTable, givTable);
				refreshChartOverlay();
			}
		});
	}

	private void resetCharts(final BaseLineChart... charts) {

		for (int i = 0; i < charts.length; i++) {
			if (charts[i] != null) {
				charts[i].resetData();
			}
		}
	}

	private void updateDamageChart(final List<CombatTickStats> ticks) {

		if (ticks.size() == 0) {
			return;
		}
		if (damageChart == null) {
			damageChart = new DamageChart(context);
			dpsChart = new DpsChart(context);

			createCharts(dpsChartWrapper, dpsChart, damageChart);
		}

		for (final CombatTickStats tick: ticks) {
			damageChart.addData(tick.getTick(), tick.getDamage());
			dpsChart.addData(tick.getTick(), tick.getDps());
		}
	}

	private void updateHealingChart(final List<CombatTickStats> ticks) {

		if (ticks.size() == 0) {
			return;
		}
		if (healingChart == null) {
			healingChart = new HealingChart(context);
			hpsChart = new HpsChart(context);

			createCharts(hpsChartWrapper, hpsChart, healingChart);
		}

		for (final CombatTickStats tick: ticks) {
			healingChart.addData(tick.getTick(), tick.getEffectiveHeal());
			hpsChart.addData(tick.getTick(), tick.getHps(), tick.getEhps());
		}
	}

	private void updateDamageTakenChart(final List<CombatTickStats> ticks) {

		if (ticks.size() == 0) {
			return;
		}
		if (damageTakenChart == null) {
			damageTakenChart = new DamageTakenChart(context);
			dtpsChart = new DtpsChart(context);

			createCharts(dtpsChartWrapper, dtpsChart, damageTakenChart);
		}

		for (final CombatTickStats tick: ticks) {
			damageTakenChart.addData(tick.getTick(), tick.getHealthBalance(), tick.getEffectiveHealTaken(), tick.getDamageTaken());
			dtpsChart.addData(tick.getTick(), tick.getDtps());
		}
	}

	@SuppressWarnings("unchecked")
	private void updateEffectsTables(final Combat combat, final CombatStats stats) throws Exception {

		// assemble different tables and summaries
		procEffects.clear();
		actEffects.clear();
		recEffects.clear();
		givEffects.clear();

		for (Effect effect: eventService.getCombatEffects(combat, context.getCombatSelection())) {
			switch (effect.getType()) {
			case ACT:
				addToEffectTableData(combat, stats, actEffects, effect);
				break;
			case PROC:
				addToEffectTableData(combat, stats, procEffects, effect);
				break;
			case REC:
				addToEffectTableData(combat, stats, recEffects, effect);
				break;
			case GIV:
				addToEffectTableData(combat, stats, givEffects, effect);
				break;
			}
		}

		procsTable.getItems().setAll(procEffects.values());
		actTable.getItems().setAll(actEffects.values());
		recTable.getItems().setAll(recEffects.values());
		givTable.getItems().setAll(givEffects.values());

		updateEffectsInOverlays(false, procsTable, actTable, recTable, givTable);

		// resort
		resortTable(procsTable);
		resortTable(actTable);
		resortTable(recTable);
		resortTable(givTable);
	}

	private void addToEffectTableData(final Combat combat, final CombatStats stats,
		final LinkedHashMap<EffectKey, EffectItem> effects, final Effect effect) {

		Long timeFrom = combat.getTimeFrom();
		if (context.getTickFrom() != null) {
			timeFrom = combat.getTimeFrom() + context.getTickFrom();
		}

		int duration = effect.getTimeTo() == null ? 0
			: (int) (Math.min(timeFrom + stats.getTick(), effect.getTimeTo())
			- Math.max(timeFrom, effect.getTimeFrom()));

		final EffectKey effectKey = new EffectKey(effect.getSource(), effect.getTarget(), effect.getEffect(), effect.getAbility());
		final EffectItem e;

		if (!effects.containsKey(effectKey)) {
			e = new EffectItem(effectKey);
			e.name.set(Format.formatEffectName(effect.getEffect(), effect.getAbility()));
			e.source.set(effect.getSource().getName());
			e.target.set(effect.getTarget().getName());
			e.isBuff.set(effect.getTimeTo() == null);

			e.count.set(1);
			e.duration.set(duration);

			effects.put(effectKey, e);

		} else {
			e = effects.get(effectKey);
			e.count.set(e.count.get() + 1);
			if (duration > 0) {
				e.duration.set(e.duration.get() + duration);
			}
		}
		e.addWindow(
			effect.getTimeFrom() - combat.getTimeFrom(),
			effect.getTimeTo() == null ? null : effect.getTimeTo() - combat.getTimeFrom());

		if (duration > 0) {
			e.pct.set(e.duration.get() * 100.0 / stats.getTick());
		}
	}

	private void updateEffectsInOverlays(boolean doClear,
		@SuppressWarnings("unchecked") final TableView<EffectItem>... effectTables) {

		windows: for (EffectKey effectKey: context.getSelectedEffects().keySet()) {
			for (TableView<EffectItem> effectTable: effectTables) {
				for (EffectItem effectItem: effectTable.getItems()) {
					if (effectKey.equals(effectItem.getEffectKey())) {
						if (doClear) {
							// found, remove
							for (final Node n: effectTable.lookupAll("." + BaseLineChart.getOverlayClass(effectKey))) {
								n.getStyleClass().clear();
								n.getStyleClass().add("selected-overlay-none");
							}
						} else {
							// found, update
							context.getSelectedEffects().put(effectKey, effectItem.getWindows());
							continue windows;
						}
					}
				}
			}
			if (doClear) {
				BaseLineChart.removeOverlayClass(effectKey);
			} else {
				context.getSelectedEffects().put(effectKey, null);
			}
		}
		if (doClear) {
			context.getSelectedEffects().clear();
		}
	}

	private void updateEffectCellStyles(final EffectCell c, boolean doToggle) {

		final EffectItem e = (EffectItem) c.getTableRow().getItem();
		if (e == null) {
			return;
		}

		c.getTableRow().getStyleClass().clear();
		if (context.getSelectedEffects().containsKey(e.getEffectKey())) {
			if (doToggle) {
				context.getSelectedEffects().remove(e.getEffectKey());
				c.getTableRow().getStyleClass().add("selected-overlay-none");
				BaseLineChart.removeOverlayClass(e.getEffectKey());
			} else {
				c.getTableRow().getStyleClass().add(BaseLineChart.getOverlayClass(e.getEffectKey()));
			}

		} else {
			if (doToggle) {
				context.getSelectedEffects().put(e.getEffectKey(), e.getWindows());
				c.getTableRow().getStyleClass().add(BaseLineChart.getOverlayClass(e.getEffectKey()));
			} else {
				c.getTableRow().getStyleClass().add("selected-overlay-none");
			}
		}
		if (doToggle) {
			refreshChartOverlay();
		}
	}
}
