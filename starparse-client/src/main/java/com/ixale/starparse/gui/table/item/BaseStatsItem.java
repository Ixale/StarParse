package com.ixale.starparse.gui.table.item;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public abstract class BaseStatsItem extends BaseItem {

	public final SimpleStringProperty
		name = new SimpleStringProperty(),
		since = new SimpleStringProperty();

	public final SimpleIntegerProperty
		ticks = new SimpleIntegerProperty(),
		total = new SimpleIntegerProperty(),
		max = new SimpleIntegerProperty();

	public final SimpleDoubleProperty
		pctTotal = new SimpleDoubleProperty();

	public Long guid;

	public String getName() {
		return name.get();
	}

	public Integer getTicks() {
		return ticks.get();
	}

	public Integer getTotal() {
		return total.get();
	}

	public Integer getMax() {
		return max.get();
	}

	public Double getPctTotal() {
		return pctTotal.get();
	}

	public String getSince() {
		return since.get();
	}
}
