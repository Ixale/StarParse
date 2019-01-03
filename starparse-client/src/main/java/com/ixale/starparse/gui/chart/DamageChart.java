package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

public class DamageChart extends BaseLineChart {

	public DamageChart(Context context) {
		super(context);

		setBoundaries(6000, 3000, null);

		setAsTransparent("chart-damage");

		addSerie("Damage Dealt");
	}
}
