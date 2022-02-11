package com.ixale.starparse.domain.stats;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.ixale.starparse.domain.CharacterDiscipline;

import java.io.Serializable;

public class CombatStats implements Serializable {

	private static final long serialVersionUID = 1L;

	int tick;

	int actions;

	int damage;

	int heal;
	int effectiveHeal;

	int damageTaken;
	int damageTakenTotal;

	int absorbed;
	int absorbedTotal;

	int healTaken;
	int effectiveHealTaken;
	int effectiveHealTakenTotal;

	int threat;
	int threatPositive;

	@FieldSerializer.Optional("ignored")
	CharacterDiscipline discipline;

	CombatStats() {

	}

	public CombatStats(int tick, int actions, int damage,
			int heal, int effectiveHeal,
			int damageTaken, int damageTakenTotal,
			int absorbed, int absorbedTotal,
			int healTaken, int effectiveHealTaken, int effectiveHealTakenTotal,
			int threat, int threatPositive, CharacterDiscipline discipline) {
		this.tick = tick;
		this.actions = actions;
		this.damage = damage;
		this.heal = heal;
		this.effectiveHeal = effectiveHeal;

		this.damageTaken = damageTaken;
		this.damageTakenTotal = damageTakenTotal;

		this.absorbed = absorbed;
		this.absorbedTotal = absorbedTotal;

		this.healTaken = healTaken;
		this.effectiveHealTaken = effectiveHealTaken;
		this.effectiveHealTakenTotal = effectiveHealTakenTotal;
		this.threat = threat;
		this.threatPositive = threatPositive;

		this.discipline = discipline;
	}

	public int getTick() {
		return tick;
	}

	public int getActions() {
		return actions;
	}

	public double getApm() {
		return Math.round(actions * 60000.0 * 100 / tick) / 100.0;
	}

	public int getDamage() {
		return damage;
	}

	public int getDps() {
		return (int) Math.round(damage * 1000.0 / tick);
	}

	public int getHeal() {
		return heal;
	}

	public int getHps() {
		return (int) Math.round(heal * 1000.0 / tick);
	}

	public int getEffectiveHeal() {
		return effectiveHeal;
	}

	public int getEhps() {
		return (int) Math.round(effectiveHeal * 1000.0 / tick);
	}

	public double getEhpsPercent() {
		return Math.round(effectiveHeal * 100.0 * 100 / heal) / 100.0;
	}

	public int getDamageTaken() {
		return damageTaken;
	}

	public int getDtps() {
		return (int) Math.round(damageTaken * 1000.0 / tick);
	}

	public int getDamageTakenTotal() {
		return damageTakenTotal;
	}

	public int getAbsorbed() {
		return absorbed;
	}

	public int getAps() {
		return (int) Math.round(absorbed * 1000.0 / tick);
	}

	public int getAbsorbedTotal() {
		return absorbedTotal;
	}

	public int getHealTaken() {
		return healTaken;
	}

	public int getHpsTaken() {
		return (int) Math.round(healTaken * 1000.0 / tick);
	}

	public int getEffectiveHealTaken() {
		return effectiveHealTaken;
	}

	public int getEhpsTaken() {
		return (int) Math.round(effectiveHealTaken * 1000.0 / tick);
	}

	public double getEhpsTakenPercent() {
		return Math.round(effectiveHealTaken * 100.0 * 100 / healTaken) / 100.0;
	}

	public int getEffectiveHealTakenTotal() {
		return effectiveHealTakenTotal;
	}

	public int getThreat() {
		return threat;
	}

	public int getThreatPositive() {
		return threatPositive;
	}

	public int getTps() {
		return (int) Math.round(threat * 1000.0 / tick);
	}

	public CharacterDiscipline getDiscipline() {
		return discipline;
	}

	public String toString() {
		return "APM: " + getApm()
				+ ", dmg: " + damage + " (" + getDps() + ")"
				+ ", heal: " + heal + " (" + getHps() + "), effective " + effectiveHeal + " (" + getEhps() + ", " + getEhpsPercent() + "%)"
				+ ", dmg taken: " + damageTaken + " (" + getDtps() + ", " + damageTakenTotal + ")"
				+ ", absorbed: " + absorbed + " (" + getAps() + ", " + absorbedTotal + ")"
				+ ", heal taken: " + healTaken + " (" + getHpsTaken() + ", " + effectiveHealTaken + ", " + getEhpsTaken() + ", " + effectiveHealTakenTotal
				+ ")"
				+ ", threat: " + threat + " (" + getTps() + ", " + threatPositive + ")";
	}
}
