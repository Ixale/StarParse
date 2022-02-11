package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

public class DtpsChart extends BaseLineChart {

	public DtpsChart(Context context) {
		super(context);

		setBoundaries(5000, null);

		setAsOpaque("chart-dtps");

		addSerie("DTPS");
	}
}
