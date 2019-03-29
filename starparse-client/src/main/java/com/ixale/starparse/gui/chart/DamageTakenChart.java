package com.ixale.starparse.gui.chart;

import com.ixale.starparse.service.impl.Context;

public class DamageTakenChart extends BaseLineChart {

	public DamageTakenChart(Context context) {
		super(context);

		setBoundaries(20000, 10000, 200000);

		setAsTransparent("chart-damage-taken");

		addSerie("Health Balance");
		addSerie("Effective Heal Taken");
		addSerie("Damage Taken");
	}

	@Override
	public void resetData() {
		super.resetData();

		yAxis.setUpperBound(10000);
	}
}
