package com.ixale.starparse.gui.table.item;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class HealingAbilityItem extends BaseStatsItem {

	public final SimpleStringProperty
		target = new SimpleStringProperty();

	public final SimpleIntegerProperty 
		actions = new SimpleIntegerProperty(),
		totalEffective = new SimpleIntegerProperty(),
		avgNormal = new SimpleIntegerProperty(),
		avgCrit = new SimpleIntegerProperty(),
		hps = new SimpleIntegerProperty(),
		ehps = new SimpleIntegerProperty();
	
	public final SimpleDoubleProperty 
		pctCrit = new SimpleDoubleProperty(),
		pctEffective = new SimpleDoubleProperty();

	public String getTarget() {
		return target.get();
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

	public Integer getHps() {
		return hps.get();
	}

	public Integer getEhps() {
		return ehps.get();
	}

	public Double getPctTotal() {
		return pctTotal.get();
	}

	@Override
	public String getFullName() {
		return name.get()+" @ "+target.get();
	}
}