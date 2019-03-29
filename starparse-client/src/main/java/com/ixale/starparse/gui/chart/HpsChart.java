package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

public class HpsChart extends BaseLineChart {

	public HpsChart(Context context) {
		super(context);

		setBoundaries(2000, 1000, null);

		setAsOpaque("chart-hps");

		addSerie("HPS");
		addSerie("EHPS");
	}
}
