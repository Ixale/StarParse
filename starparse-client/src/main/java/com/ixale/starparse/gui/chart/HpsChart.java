package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

public class HpsChart extends BaseLineChart {

	public HpsChart(Context context) {
		super(context);

		setBoundaries(500, 500, null);

		setAsOpaque("chart-hps");

		addSerie("HPS");
		addSerie("EHPS");
	}
}
