package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

public class DpsChart extends BaseLineChart {

	public DpsChart(Context context) {
		super(context);

		setBoundaries(2000, 1000, null);
		
		setAsOpaque("chart-dps");

		addSerie("DPS");
	}
}
