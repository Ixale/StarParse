package com.ixale.starparse.domain;

import java.util.Objects;

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
	StarForge("US", "Star Forge", "star-forge", "he3000"),
	SateleShan("US", "Satele Shan", "satele-shan", "he3001"),
	TulakHord("EU", "Tulak Hord", "tulak-hord", "he4001"),
	DarthMalgus("EU", "Darth Malgus", "darth-malgus", "he4000"),
	TheLeviathan("EU", "The Leviathan", "the-leviathan", "he4002");

	final String region, name, webalized, code;

	ServerName(String region, String name, String webalized) {
		this(region, name, webalized, null);
	}

	ServerName(String region, String name, String webalized, String code) {
		this.region = region;
		this.name = name;
		this.webalized = webalized;
		this.code = code;
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
				return sn.code == null ? sn.webalized : sn.code;
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

	public static String getTitleFromCode(final String code) {
		if ("HE600".equalsIgnoreCase(code)) {
			return "PTS";
		}
		for (ServerName sn : values()) {
			if (code.equalsIgnoreCase(sn.code)) {
				return sn.name;
			}
		}
		return null;
	}

	public String getCode() {
		return code;
	}

}
