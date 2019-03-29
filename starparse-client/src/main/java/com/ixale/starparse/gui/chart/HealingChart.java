package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

public class HealingChart extends BaseLineChart {

	public HealingChart(Context context) {
		super(context);

		setBoundaries(20000, 10000, null);

		setAsTransparent("chart-healing");

		addSerie("Effective Healing");
	}

	@Override
	public void resetData() {
		super.resetData();

		yAxis.setUpperBound(2000);
	}
}
