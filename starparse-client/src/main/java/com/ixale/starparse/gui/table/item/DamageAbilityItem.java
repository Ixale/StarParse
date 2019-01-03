package com.ixale.starparse.gui.table.item;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class DamageAbilityItem extends BaseStatsItem {

	public final SimpleStringProperty
		target = new SimpleStringProperty(),
		damageType = new SimpleStringProperty();
	
	public final SimpleIntegerProperty 
		actions = new SimpleIntegerProperty(),
		avgNormal = new SimpleIntegerProperty(),
		avgCrit = new SimpleIntegerProperty(),
		avgTotal = new SimpleIntegerProperty(),
		dps = new SimpleIntegerProperty(),
		max = new SimpleIntegerProperty();
	
	public final SimpleDoubleProperty 
		pctCrit = new SimpleDoubleProperty(),
		pctMiss = new SimpleDoubleProperty();

	public String getTarget() {
		return target.get();
	}

	public Integer getActions() {
		return actions.get();
	}

	public Integer getAvgNormal() {
		return avgNormal.get();
	}

	public Integer getAvgCrit() {
		return avgCrit.get();
	}

	public Integer getAvgTotal() {
		return avgTotal.get();
	}

	public Double getPctCrit() {
		return pctCrit.get();
	}

	public Double getPctMiss() {
		return pctMiss.get();
	}

	public Integer getMax() {
		return max.get();
	}

	public Integer getDps() {
		return dps.get();
	}

	public String getDamageType() {
		return damageType.get();
	}

	@Override
	public String getFullName() {
		return name.get()+" @ "+target.get();
	}
}