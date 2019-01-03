package com.ixale.starparse.domain.stats;

public class CombatMitigationStats {

	private final int
		duration, ticks, damage,
		internal, elemental, energy, kinetic, 
		shieldTicks, missTicks,
		absorbedSelf, absorbedOthers, aps;

	private final double
		internalPercent, elementalPercent, energyPercent, kineticPercent,
		shieldPercent, missPercent,
		absorbedSelfPercent, absorbedOthersPercent;

	public CombatMitigationStats(int duration, int ticks, int damage,
			int internal, double internalPercent,
			int elemental, double elementalPercent, 
			int energy, double energyPercent,
			int kinetic, double kineticPercent,
			int shieldTicks, double shieldPercent,
			int missTicks, double missPercent,
			int absorbedSelf, double absorbedSelfPercent, 
			int absorbedOthers, double absorbedOthersPercent,
			int aps) {
		super();
		this.duration = duration;
		this.ticks = ticks;
		this.damage = damage;

		this.internal = internal;
		this.internalPercent = internalPercent;
		this.elemental = elemental;
		this.elementalPercent = elementalPercent;
		this.energy = energy;
		this.energyPercent = energyPercent;
		this.kinetic = kinetic;
		this.kineticPercent = kineticPercent;

		this.shieldTicks = shieldTicks;
		this.shieldPercent = shieldPercent;
		this.missTicks = missTicks;
		this.missPercent = missPercent;

		this.absorbedSelf = absorbedSelf;
		this.absorbedSelfPercent = absorbedSelfPercent;
		this.absorbedOthers = absorbedOthers;
		this.absorbedOthersPercent = absorbedOthersPercent;
		this.aps = aps;
	}

	public int getDuration() {
		return duration;
	}

	public int getTicks() {
		return ticks;
	}

	public int getDamage() {
		return damage;
	}

	public int getInternal() {
		return internal;
	}

	public int getElemental() {
		return elemental;
	}

	public int getEnergy() {
		return energy;
	}

	public int getKinetic() {
		return kinetic;
	}

	public int getShieldTicks() {
		return shieldTicks;
	}

	public int getMissTicks() {
		return missTicks;
	}

	public int getAbsorbedSelf() {
		return absorbedSelf;
	}

	public int getAbsorbedOthers() {
		return absorbedOthers;
	}

	public int getAps() {
		return aps;
	}

	public double getInternalPercent() {
		return internalPercent;
	}

	public double getElementalPercent() {
		return elementalPercent;
	}

	public double getEnergyPercent() {
		return energyPercent;
	}

	public double getKineticPercent() {
		return kineticPercent;
	}

	public double getShieldPercent() {
		return shieldPercent;
	}

	public double getMissPercent() {
		return missPercent;
	}

	public double getAbsorbedSelfPercent() {
		return absorbedSelfPercent;
	}

	public double getAbsorbedOthersPercent() {
		return absorbedOthersPercent;
	}
	
}
