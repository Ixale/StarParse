package com.ixale.starparse.domain;

public enum RaidChallengeName {

	BrontesBurn("Burn"),

	RaptusHealing("Captive HPS"),
	RaptusDamage("Captive DPS"),

	CouncilP2("B & S"),

	TorqueDroid("Poor Droids"),

	RevanBurn("Core"),
	CoratanniRuugar("Ruugar"),

	TanksShield("Shield"),
	KephessWalker("Walker"),

	StyrakManifestation("Manifestation");

	private final String fullName;

	RaidChallengeName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public String toString() {
		return getFullName();
	}
}
