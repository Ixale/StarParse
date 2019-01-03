package com.ixale.starparse.gui.table.item;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class HealingTakenItem extends BaseStatsItem {

	public final SimpleStringProperty
		source = new SimpleStringProperty();

	public final SimpleIntegerProperty 
		actions = new SimpleIntegerProperty(),
		totalEffective = new SimpleIntegerProperty(),
		avgNormal = new SimpleIntegerProperty(),
		avgCrit = new SimpleIntegerProperty(),
		htps = new SimpleIntegerProperty(),
		ehtps = new SimpleIntegerProperty(),
		aps = new SimpleIntegerProperty(),
		absorbed = new SimpleIntegerProperty();
	
	public final SimpleDoubleProperty 
		pctCrit = new SimpleDoubleProperty(),
		pctEffective = new SimpleDoubleProperty();

	public String getSource() {
		return source.get();
	}

	public Integer getActions() {
		return actions.get();
	}

	public Integer getTotalEffective() {
		return totalEffective.get();
	}

	public Integer getAvgNormal() {
		return avgNormal.get();
	}

	public Integer getAvgCrit() {
		return avgCrit.get();
	}

	public Double getPctCrit() {
		return pctCrit.get();
	}

	public Double getPctEffective() {
		return pctEffective.get();
	}

	public Integer getHtps() {
		return htps.get();
	}

	public Integer getEhtps() {
		return ehtps.get();
	}

	public Integer getAps() {
		return aps.getValue();
	}

	public Integer getAbsorbed() {
		return absorbed.getValue();
	}

	@Override
	public String getFullName() {
		return name.get()+" @ "+source.get();
	}
}