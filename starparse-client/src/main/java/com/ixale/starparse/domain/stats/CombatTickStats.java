package com.ixale.starparse.domain.stats;

public class CombatTickStats {

	private final int tick;

	private final int damage;
	private final int dps;

	private final int heal;
	private final int hps;
	private final int effectiveHeal;
	private final int ehps;

	private final int damageTaken;
	private final int dtps;
	
	private final int effectiveHealTaken;

	private final int healthBalance;

	public CombatTickStats(int tick, int damage, int dps,
			int heal, int hps, int effectiveHeal, int ehps,
			int damageTaken, int dtps,
			int effectiveHealTaken,
			int healthBalance) {
		this.tick = tick;
		this.damage = damage;
		this.dps = dps;
		this.heal = heal;
		this.hps = hps;
		this.effectiveHeal = effectiveHeal;
		this.ehps = ehps;

		this.damageTaken = damageTaken;
		this.dtps = dtps;

		this.effectiveHealTaken = effectiveHealTaken;

		this.healthBalance = healthBalance;
	}

	public int getTick() {
		return tick;
	}

	public int getDamage() {
		return damage;
	}

	public int getDps() {
		return dps;
	}

	public int getHeal() {
		return heal;
	}

	public int getHps() {
		return hps;
	}

	public int getEffectiveHeal() {
		return effectiveHeal;
	}

	public int getEhps() {
		return ehps;
	}

	public int getDamageTaken() {
		return damageTaken;
	}

	public int getDtps() {
		return dtps;
	}

	public int getEffectiveHealTaken() {
		return effectiveHealTaken;
	}

	public int getHealthBalance() {
		return healthBalance;
	}

	public String toString() {
		return "Tick: "+tick
			+ ", dmg: "+damage+" ("+dps+")"
			+ ", heal: "+heal+" ("+hps+"), effective "+effectiveHeal+" ("+ehps+")"
			+ ", dmg taken: "+damageTaken+" ("+dtps+")"
			+ ", heal taken: "+effectiveHealTaken
			+ ", balance: "+healthBalance;
	}
}
