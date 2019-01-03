package com.ixale.starparse.gui.chart;

import java.util.LinkedHashMap;

import com.ixale.starparse.service.impl.Context;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;

public abstract class BaseStackedChart extends BaseChart<StackedAreaChart<Number, Number>> {

	protected StackedAreaChart<Number, Number> chart;

	protected final NumberAxis xAxis = new NumberAxis();
	protected final NumberAxis yAxis = new NumberAxis();

	private LinkedHashMap<String, XYChart.Series<Number, Number>> series = new LinkedHashMap<String, XYChart.Series<Number, Number>>();

	private Integer lastTick = null;

	public BaseStackedChart(Context context) {
		super(context);

		chart = new StackedAreaChart<Number,Number>(xAxis, yAxis);
		chart.setAnimated(false);
		chart.setMaxHeight(100);

		xAxis.setAutoRanging(false);
		xAxis.setTickUnit(10);
		xAxis.setMinorTickCount(2);
		
		yAxis.setAutoRanging(false);
		yAxis.setMinorTickCount(0);
		yAxis.setPrefWidth(40);
	}

	protected void addSerie(String title) {
		series.put(title, null);
	}

	@Override
	public StackedAreaChart<Number, Number> getChartNode() {
		return chart;
	}

	public void addData(int x, double... y) {

		if (lastTick == null && !series.isEmpty()) {
			for (String serieTitle: series.keySet()) {
				if (series.get(serieTitle) != null) {
					throw new RuntimeException("Serie "+serieTitle+" was not reset");
				}
				series.put(serieTitle, new XYChart.Series<Number, Number>());
				series.get(serieTitle).setName(serieTitle);
			}
		}

		if (lastTick != null && lastTick.compareTo(x) > 0) {
			// already there
			return;
			
		} 
		
		int s = 0;
		for (String serieTitle: series.keySet()) {
			if (lastTick != null && lastTick.equals(x)) {
				// update very last one
				series.get(serieTitle).getData().set(series.get(serieTitle).getData().size() - 1, new XYChart.Data<Number, Number>(x, y[s]));
			} else {
				// append new one
				series.get(serieTitle).getData().add(new XYChart.Data<Number, Number>(x, y[s]));
			}

			if (yAxis.getUpperBound() < (y[s] * 1.1)) {
				yAxis.setUpperBound(y[s] * 1.1);
			}
			s++;
		}

		xAxis.setUpperBound(x);

		if (lastTick == null) {
			for (String serieTitle: series.keySet()) {
				chart.getData().add(series.get(serieTitle));
			}
		}
		lastTick = x;
	}

	@Override
	public void resetData() {

		chart.getData().clear();

		if (!series.isEmpty()) {
			for (String serieTitle: series.keySet()) {
				series.put(serieTitle, null);
			}
		}
		lastTick = null;

		yAxis.setUpperBound(1);
	}
}
