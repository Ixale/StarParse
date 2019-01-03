package com.ixale.starparse.domain;

public enum ServerName {
	TheHarbinger("US", "The Harbinger", "the-harbinger"),
	TheBastion("US", "The Bastion", "the-bastion"),
	BegerenColony("US", "Begeren Colony", "begeren-colony"),
	TheShadowlands("US", "The Shadowlands", "the-shadowlands"),
	JungMa("US", "Jung Ma", "jung-ma"),
	TheEbonHawk("US", "The Ebon Hawk", "the-ebon-hawk"),
	ProphecyOfTheFive("US", "Prophecy Of The Five", "prophecy-of-the-five"),
	JediCovenant("US", "Jedi Covenant", "jedi-covenant"),
	T3M4("EU", "T3 M4", "t3-m4"),
	DarthNihilus("EU", "Darth Nihilus", "darth-nihilus"),
	TombOfFreedonNadd("EU", "Tomb Of Freedon Nadd", "tomb-of-freedon-nadd"),
	JarkaiSword("EU", "Jar'kai Sword", "jar'kai-sword"),
	TheProgenitor("EU", "The Progenitor", "the-progenitor"),
	VanjervalisChain("EU", "Vanjervalis Chain", "vanjervalis-chain"),
	BattleMeditation("US", "Battle Meditation", "battle-meditation"),
	MantleOfTheForce("EU", "Mantle Of The Force", "mantle-of-the-force"),
	TheRedEclipse("EU", "The Red Eclipse", "the-red-eclipse"),
	//
	StarForge("US", "Star Forge", "star-forge"),
	SateleShan("US", "Satele Shan", "satele-shan"),
	TulakHord("EU", "Tulak Hord", "tulak-hord"),
	DarthMalgus("EU", "Darth Malgus", "darth-malgus"),
	TheLeviathan("EU", "The Leviathan", "the-leviathan");

	final String region, name, webalized;

	private ServerName(String region, String name, String webalized) {
		this.region = region;
		this.name = name;
		this.webalized = webalized;
	}

	public String getName() {
		return name;
	}

	public ServerName getActive() {
		switch (this) {
			case TheHarbinger:
			case TheBastion:
			case BegerenColony:
				return SateleShan;
			case TheShadowlands:
			case JungMa:
			case TheEbonHawk:
			case ProphecyOfTheFive:
			case JediCovenant:
				return StarForge;
			case TombOfFreedonNadd:
			case TheProgenitor:
			case TheRedEclipse:
				return DarthMalgus;
			case T3M4:
			case JarkaiSword:
			case VanjervalisChain:
				return TulakHord;
			case DarthNihilus:
			case BattleMeditation:
			case MantleOfTheForce:
				return TheLeviathan;
			case SateleShan:
			case StarForge:
			case DarthMalgus:
			case TulakHord:
			case TheLeviathan:
			default:
				return this;
		}
	}

	public static String getWebalized(final String serverName) {
		for (ServerName sn : values()) {
			if (sn.name.equals(serverName)) {
				return sn.webalized;
			}
		}
		return null;
	}

	public static ServerName getFromName(final String serverName) {
		for (ServerName sn : values()) {
			if (sn.name.equals(serverName)) {
				return sn;
			}
		}
		return null;
	}
}
