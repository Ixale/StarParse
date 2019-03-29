package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

public class DamageChart extends BaseLineChart {

	public DamageChart(Context context) {
		super(context);

		setBoundaries(20000, 10000, null);

		setAsTransparent("chart-damage");

		addSerie("Damage Dealt");
	}
}
