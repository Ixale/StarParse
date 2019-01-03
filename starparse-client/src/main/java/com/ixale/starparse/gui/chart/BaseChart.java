package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

import javafx.scene.chart.Chart;

public abstract class BaseChart<T extends Chart>  {

	protected Context context;

	public BaseChart(Context context) {
		this.context = context;
	}

	abstract public T getChartNode();

	abstract public void resetData();
}
