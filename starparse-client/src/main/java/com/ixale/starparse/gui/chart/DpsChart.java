package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

public class DpsChart extends BaseLineChart {

	public DpsChart(Context context) {
		super(context);

		setBoundaries(1000, 500, null);
		
		setAsOpaque("chart-dps");

		addSerie("DPS");
	}
}
