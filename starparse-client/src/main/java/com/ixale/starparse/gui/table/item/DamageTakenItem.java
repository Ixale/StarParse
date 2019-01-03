package com.ixale.starparse.gui.table.item;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class DamageTakenItem extends BaseStatsItem {

	public final SimpleStringProperty
		source = new SimpleStringProperty(),
		damageType = new SimpleStringProperty(),
		attackType = new SimpleStringProperty();

	public final SimpleIntegerProperty
		avgNormal = new SimpleIntegerProperty(),
		absorbed = new SimpleIntegerProperty(),
		dtps = new SimpleIntegerProperty(),
		totalIe = new SimpleIntegerProperty();

	public final SimpleDoubleProperty
		pctShield = new SimpleDoubleProperty(),
		pctMiss = new SimpleDoubleProperty();

	public String getSource() {
		return source.get();
	}

	public Integer getAvgNormal() {
		return avgNormal.get();
	}

	public Double getPctShield() {
		return pctShield.get();
	}

	public Double getPctMiss() {
		return pctMiss.get();
	}

	public Integer getDtps() {
		return dtps.get();
	}

	public Integer getAbsorbed() {
		return absorbed.get();
	}

	public String getDamageType() {
		return damageType.get();
	}

	public String getAttackType() {
		return attackType.get();
	}

	public String getSince() {
		return since.get();
	}

	@Override
	public String getFullName() {
		return name.get() + " @ " + source.get();
	}
}