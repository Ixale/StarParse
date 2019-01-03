package com.ixale.starparse.domain;

public enum CharacterClass {

	Marauder("Marauder"),
	Juggernaut("Juggernaut"),
	Sorcerer("Sorcerer"),
	Assassin("Assassin"),
	Mercenary("Mercenary"),
	Powertech("Powertech"),
	Operative("Operative"),
	Sniper("Sniper"),
	Sentinel("Sentinel"),
	Guardian("Guardian"),
	Sage("Sage"),
	Shadow("Shadow"),
	Commando("Commando"),
	Vanguard("Vanguard"),
	Scoundrel("Scoundrel"),
	Gunslinger("Gunslinger")
	;

	private String fullName;

	CharacterClass(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public String toString() {
		return fullName;
	}
}
