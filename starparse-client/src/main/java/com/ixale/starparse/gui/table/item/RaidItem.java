package com.ixale.starparse.gui.table.item;

import com.ixale.starparse.ws.RaidCombatMessage;

import javafx.beans.property.SimpleIntegerProperty;

public class RaidItem extends BaseStatsItem {

	public final SimpleIntegerProperty 
		time = new SimpleIntegerProperty(),
		damage = new SimpleIntegerProperty(),
		dps = new SimpleIntegerProperty(),
		threat = new SimpleIntegerProperty(),
		tps = new SimpleIntegerProperty(),
		damageTaken = new SimpleIntegerProperty(),
		dtps = new SimpleIntegerProperty(),
		aps = new SimpleIntegerProperty(),
		healing = new SimpleIntegerProperty(),
		hps = new SimpleIntegerProperty(),
		ehps = new SimpleIntegerProperty(),
		pctEffective = new SimpleIntegerProperty(),
		shielding = new SimpleIntegerProperty(),
		sps = new SimpleIntegerProperty(),
		rank = new SimpleIntegerProperty();

	private RaidCombatMessage message;

	public RaidItem() {
	}

	public void setMessage(final RaidCombatMessage message) {
		this.message = message;
	}

	public RaidCombatMessage getMessage() {
		return message;
	}

	public Integer getDamage() {
		return damage.get();
	}

	public Integer getThreat() {
		return threat.get();
	}

	public Integer getTps() {
		return tps.get();
	}

	public Integer getDamageTaken() {
		return damageTaken.get();
	}

	public Integer getDtps() {
		return dtps.get();
	}

	public Integer getAps() {
		return aps.get();
	}

	public Integer getHealing() {
		return healing.get();
	}

	public Integer getHps() {
		return hps.get();
	}

	public Integer getEhps() {
		return ehps.get();
	}

	public Integer getShielding() {
		return shielding.get();
	}

	public Integer getSps() {
		return sps.get();
	}

	public Integer getPctEffective() {
		return pctEffective.get();
	}

	public Integer getDps() {
		return dps.get();
	}

	public Integer getTime() {
		return time.get();
	}

	public Integer getRank() {
		return rank.get();
	}

	@Override
	public String getFullName() {
		return name.get();
	}

	public SimpleIntegerProperty timeProperty() {
		return time;
	}

	public SimpleIntegerProperty damageProperty() {
		return damage;
	}

	public SimpleIntegerProperty dpsProperty() {
		return dps;
	}

	public SimpleIntegerProperty threatProperty() {
		return threat;
	}

	public SimpleIntegerProperty tpsProperty() {
		return tps;
	}

	public SimpleIntegerProperty damageTakenProperty() {
		return damageTaken;
	}

	public SimpleIntegerProperty dtpsProperty() {
		return dtps;
	}

	public SimpleIntegerProperty apsProperty() {
		return aps;
	}

	public SimpleIntegerProperty healingProperty() {
		return healing;
	}

	public SimpleIntegerProperty hpsProperty() {
		return hps;
	}

	public SimpleIntegerProperty ehpsProperty() {
		return ehps;
	}

	public SimpleIntegerProperty shieldingProperty() {
		return shielding;
	}

	public SimpleIntegerProperty spsProperty() {
		return sps;
	}

	public SimpleIntegerProperty pctEffectiveProperty() {
		return pctEffective;
	}

}