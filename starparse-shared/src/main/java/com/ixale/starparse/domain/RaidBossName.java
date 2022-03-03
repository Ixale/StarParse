package com.ixale.starparse.domain;

public enum RaidBossName {

	OperationsTrainingDummy("Operations Training Dummy"),

	// TfB
	TheWrithingHorror("The Writhing Horror", "Writhing Horror"),
	DreadGuards("Dread Guards"),
	OperatorIx("Operator IX"),
	KephessTheUndying("Kephess The Undying", "Kephess"),
	TerrorFromBeyond("The Terror From Beyond", "TFB"),

	// SaV
	DashRoode("Dash'Roode"),
	Titan6("Titan 6"),
	Thrasher("Thrasher"),
	OperationsChief("Operations Chief"),
	OlokTheShadow("Olok The Shadow"),
	CartelWarlords("Cartel Warlords"),
	Styrak("Dread Master Styrak", "Styrak"),

	HatefulEntity("Hateful Entity"),

	// DF
	Nefra("Nefra, Who Bars the Way", "Nefra"),
	Draxus("Gate Commander Draxus", "Draxus"),
	Grobthok("Grob’Thok, Who Feeds The Forge", "Grob’Thok"),
	CorruptorZero("Corruptor Zero"),
	Brontes("Dread Master Brontes", "Brontes"),

	// DP
	Bestia("Dread Master Bestia", "Bestia"),
	Tyrans("Dread Master Tyrans", "Tyrans"),
	Calphayus("Dread Master Calphayus", "Calphayus"),
	Raptus("Dread Master Raptus", "Raptus"),
	Council("Dread Council"),

	// Ravagers
	Sparky("Sparky"),
	QuartermasteBulo("Quartermaster Bulo", "Bulo"),
	Torque("Torque"),
	Blaster("Blaster"),
	Coratanni("Coratanni"),

	MalapharTheSavage("Malaphar The Savage", "Malaphar"),
	SwordSquadron("Sword Squadron"),
	Underlurker("The Underlurker"),
	RevaniteCommanders("Revanite Commanders", "Commanders"),
	Revan("Revan"),

	// WB & single instanced
	AncientThreat("Ancient Threat"),
	ColossalMonolith("Colossal Monolith", "Colossal"),
	WorldbreakerMonolith("Worldbreaker Monolith", "Worldbreaker"),
	GoldenFury("Golden Fury"),
	DreadfulEntity("Dreadful Entity"),
	XenoanalystII("Xenoanalyst II"),
	Eyeless("The Eyeless"),

	// Eternity Vault
	AnnihilationDroidXRR3("Annihilation Droid XRR-3", "XRR-3"),
	Gharj("Gharj"),
	AncientPylons("Ancient Pylons"),
	InfernalCouncil("Infernal Council"),
	Soa("Soa"),

	// Karagga's Palace 
	Bonethrasher("Bonethrasher"),
	JargAndSorno("Jarg & Sorno"),
	ForemanCrusher("Foreman Crusher"),
	G4B3HeavyFabricator("G4-B3 Heavy Fabricator", "G4-B3"),
	KaraggaTheUnyielding("Karagga The Unyielding", "Karagga"),

	// Explosive Conflict
	ZornAndToth("Zorn & Toth"),
	FirebrandAndStormcaller("Firebrand & Stormcaller", "Tanks"),
	ColonelVorgath("Colonel Vorgath"),
	WarlordKephess("Warlord Kephess"),

	// Eternal Championship
	ArlaiaZayzen("Arlaia Zayzen"),
	DaruulaGrah("Daruula Grah"),
	GungusBoga("Gungus Boga"),
	ConraadAndChompers("Conraad & Chompers"),
	Lanos("Lanos"),
	BreaktownBrawler("Breaktown Brawler"),
	NocturnoAndDrakeRaven("Nocturno & Drake Raven", "Nocturno"),
	LittleGut("Little Gut"),
	DoomDroid("Doom Droid"),
	EternalChampionZotar("Eternal Champion Zotar", "Zotar"),

	// Iokath
	Tyth("Tyth"),
	AivelaAndEsne("AIVELA & ESNE"),
	Nahut("Nahut"),
	Izax("IZAX"),
	Scyva("SCYVA"),

	// WB 5.10
	MutatedGeonosianQueen("Geonosian Queen"),

	// Dxun 6.0
	Red("The Pack Leader", "Red"),
	BreachCI004("Breach CI-004"),
	TrandoshanSquad("Trandoshan Squad"),
	TheHuntmaster("The Huntmaster"),
	ApexVanguard("Apex Vanguard"),

	// R-4 Anomaly 7.0
	IPCPT("IP-CPT"),
	Watchdog("Watchdog"),
	LordKanoth("Lord Kanoth"),
	LordValeo("Lord Valeo"),
	LadyDominique("Lady Dominique")
	;

	private final String fullName;
	private final String shortName;

	RaidBossName(String fullName) {
		this(fullName, fullName);
	}

	RaidBossName(final String fullName, final String shortName) {
		this.fullName = fullName;
		this.shortName = shortName;
	}

	public String getFullName() {
		return fullName;
	}

	public String getShortName() {
		return shortName;
	}

	@Override
	public String toString() {
		return fullName;
	}
}
