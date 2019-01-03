package com.ixale.starparse.domain;

public enum RaidBossName {

	OperationsTrainingDummy("Operations Training Dummy"),

	// TfB
	TheWrithingHorror("The Writhing Horror"),
	DreadGuards("Dread Guards"),
	OperatorIx("Operator IX"),
	KephessTheUndying("Kephess The Undying"),
	TerrorFromBeyond("The Terror From Beyond"),

	// SaV
	DashRoode("Dash'Roode"),
	Titan6("Titan 6"),
	Thrasher("Thrasher"),
	OperationsChief("Operations Chief"),
	OlokTheShadow("Olok The Shadow"),
	CartelWarlords("Cartel Warlords"),
	Styrak("Dread Master Styrak"),

	HatefulEntity("Hateful Entity"),

	// DF
	Nefra("Nefra, Who Bars the Way"),
	Draxus("Gate Commander Draxus"),
	Grobthok("Grob’Thok, Who Feeds The Forge"),
	CorruptorZero("Corruptor Zero"),
	Brontes("Dread Master Brontes"),

	// DP
	Bestia("Dread Master Bestia"),
	Tyrans("Dread Master Tyrans"),
	Calphayus("Dread Master Calphayus"),
	Raptus("Dread Master Raptus"),
	Council("Dread Council"),

	// Ravagers
	Sparky("Sparky"),
	QuartermasteBulo("Quartermaster Bulo"),
	Torque("Torque"),
	Blaster("Blaster"),
	Coratanni("Coratanni"),

	MalapharTheSavage("Malaphar The Savage"),
	SwordSquadron("Sword Squadron"),
	Underlurker("The Underlurker"),
	RevaniteCommanders("Revanite Commanders"),
	Revan("Revan"),

	// WB & single instanced
	AncientThreat("Ancient Threat"),
	ColossalMonolith("Colossal Monolith"),
	WorldbreakerMonolith("Worldbreaker Monolith"),
	GoldenFury("Golden Fury"),
	DreadfulEntity("Dreadful Entity"),
	XenoanalystII("Xenoanalyst II"),
	Eyeless("The Eyeless"),

	// Eternity Vault
	AnnihilationDroidXRR3("Annihilation Droid XRR-3"),
	Gharj("Gharj"),
	AncientPylons("Ancient Pylons"),
	InfernalCouncil("Infernal Council"),
	Soa("Soa"),

	// Karagga's Palace 
	Bonethrasher("Bonethrasher"),
	JargAndSorno("Jarg & Sorno"),
	ForemanCrusher("Foreman Crusher"),
	G4B3HeavyFabricator("G4-B3 Heavy Fabricator"),
	KaraggaTheUnyielding("Karagga The Unyielding"),

	// Explosive Conflict
	ZornAndToth("Zorn & Toth"),
	FirebrandAndStormcaller("Firebrand & Stormcaller"),
	ColonelVorgath("Colonel Vorgath"),
	WarlordKephess("Warlord Kephess"),

	// Eternal Championship
	ArlaiaZayzen("Arlaia Zayzen"),
	DaruulaGrah("Daruula Grah"),
	GungusBoga("Gungus Boga"),
	ConraadAndChompers("Conraad & Chompers"),
	Lanos("Lanos"),
	BreaktownBrawler("Breaktown Brawler"),
	NocturnoAndDrakeRaven("Nocturno & Drake Raven"),
	LittleGut("Little Gut"),
	DoomDroid("Doom Droid"),
	EternalChampionZotar("Eternal Champion Zotar"),

	// Iokath
	Tyth("Tyth"),
	AivelaAndEsne("AIVELA & ESNE"),
	Nahut("Nahut"),
	Izax("IZAX"),
	Scyva("SCYVA")
	;

	private String fullName;

	RaidBossName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public String toString() {
		return fullName;
	}
}
