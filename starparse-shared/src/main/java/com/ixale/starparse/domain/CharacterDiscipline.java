package com.ixale.starparse.domain;

import static com.ixale.starparse.domain.CharacterClass.Assassin;
import static com.ixale.starparse.domain.CharacterClass.Commando;
import static com.ixale.starparse.domain.CharacterClass.Guardian;
import static com.ixale.starparse.domain.CharacterClass.Gunslinger;
import static com.ixale.starparse.domain.CharacterClass.Juggernaut;
import static com.ixale.starparse.domain.CharacterClass.Marauder;
import static com.ixale.starparse.domain.CharacterClass.Mercenary;
import static com.ixale.starparse.domain.CharacterClass.Operative;
import static com.ixale.starparse.domain.CharacterClass.Powertech;
import static com.ixale.starparse.domain.CharacterClass.Sage;
import static com.ixale.starparse.domain.CharacterClass.Scoundrel;
import static com.ixale.starparse.domain.CharacterClass.Sentinel;
import static com.ixale.starparse.domain.CharacterClass.Shadow;
import static com.ixale.starparse.domain.CharacterClass.Sniper;
import static com.ixale.starparse.domain.CharacterClass.Sorcerer;
import static com.ixale.starparse.domain.CharacterClass.Vanguard;
import static com.ixale.starparse.domain.CharacterRole.DPS;
import static com.ixale.starparse.domain.CharacterRole.HEALER;
import static com.ixale.starparse.domain.CharacterRole.TANK;

public enum CharacterDiscipline {

	Lightning(Sorcerer, DPS, 2031339142381586L, "Lightning", 808484643799040L, 3411385149030400L, 3401218961440768L, 961338234896384L),
	Madness(Sorcerer, DPS, 2031339142381584L, "Madness", /*808424514256896L, 808433104191488L, 3400617666019328L,*/ 3400587601248256L), // shared with Hatred
	Corruption(Sorcerer, HEALER, 2031339142381587L, "Corruption", 3411032961712128L, 1104395005591552L, 808703687131136L, 3401283385950208L),
	Vengeance(Juggernaut, DPS, 2031339142381576L, "Vengeance", 2210507998101504L, 1169953386397696L, 807891938312192L, 3503361873674240L),
	Immortal(Juggernaut, TANK, 2031339142381577L, "Immortal", 3438288824172544L, 1582326081388544L, 2211062048882688L, 1000001530494976L),
	Rage(Juggernaut, DPS, 2031339142381578L, "Rage", 1148263801552896L, 3451633287561216L, 1009763991158784L, 3434638101970944L),
	Arsenal(Mercenary, DPS, 2031339142381601L, "Arsenal", 3394145150304256L, 814592087293952L, 3411372264128512L, 985140943650816L),
	InnovativeOrdnance(Mercenary, DPS, 2031339142381598L, "Innovative Ordnance", 3394733560823808L, 3394707791020032L),
	Bodyguard(Mercenary, HEALER, 2031339142381600L, "Bodyguard", 985514605805568L, 985226842996736L, 3394132265402368L, 3401390760132608L),
	Concealment(Operative, DPS, 2031339142381595L, "Concealment", 3403443754500096L, 2176972893454336L, 3403456639401984L, 3403448049467392L),
	Lethality(Operative, DPS, 2031339142381593L, "Lethality", 3440822854877184L, /*1703989619982336L,*/ 3403731517308928L, 3440831444811776L),
	Medicine(Operative, HEALER, 2031339142381596L, "Medicine", 3401961990782976L, 3402082249867264L, 815232037421056L, 815240627355648L),
	ShieldTech(Powertech, TANK, 2031339142381604L, "Shield Tech", 3178507727273984L, 2264070535249920L, 2028135096778752L, 2264298168516608L),
	Pyrotech(Powertech, DPS, 2031339142381602L, "Pyrotech", 3384704812187648L, 814519072849920L, /*2027022700249088L, */3384812186370048L), // shared with InnovativeOrdnance
	AdvancedPrototype(Powertech, DPS, 2031339142381605L, "Advanced Prototype", 3388896700268544L, 3387479361060864L, 2029831608860672L, 3387565260406784L),
	Hatred(Assassin, DPS, 2031339142381580L, "Hatred", 3410053709168640L),
	Darkness(Assassin, TANK, 2031339142381582L, "Darkness", 975760735076352L, 3408949902573568L, 2453744880975872L, 975687720632320L),
	Deception(Assassin, DPS, 2031339142381583L, "Deception", 3408941312638976L, 1962284658196480L, 948659491438592L, 975584641417216L),
	Marksmanship(Sniper, DPS, 2031339142381591L, "Marksmanship", 3394042071089152L, 2300994369093632L, 3394699201085440L, 2301299311771648L),
	Engineering(Sniper, DPS, 2031339142381592L, "Engineering", 3394870999777280L, 3394789395398656L, 879630777057280L, 993073748246528L),
	Virulence(Sniper, DPS, 2031339142381589L, "Virulence", 3394939719254016L, 3395639798923264L, 3440827149844480L),
	Annihilation(Marauder, DPS, 2031339142381572L, "Annihilation", true, 808123866546176L, 1259666663276544L, 3361615068004352L, 3455541707800576L),
	Carnage(Marauder, DPS, 2031339142381573L, "Carnage", true, 1259692433080320L, 3361602183102464L, 1261122657189888L, 1259670958243840L),
	Fury(Marauder, DPS, 2031339142381574L, "Fury", true, 3434638101970944L), // shared with Rage
	Scrapper(Scoundrel, DPS, 2031339142381632L, "Scrapper", 3406463116509184L, 3406458821541888L, 3406471706443776L, 985076519141376L),
	Ruffian(Scoundrel, DPS, 2031339142381629L, "Ruffian", 3406729404481536L, 3406802418925568L, 3459828085161984L),
	Sawbones(Scoundrel, HEALER, 2031339142381631L, "Sawbones", 807518276157440L, 1147159994957824L, 3406411576901632L, 3406424461803520L),
	Telekinetics(Sage, DPS, 2031339142381618L, "Telekinetics", 812904165146624L, 3347325711810560L, 3347321416843264L, 947130483081216L, 885781170225152L),
	Seer(Sage, HEALER, 2031339142381619L, "Seer", 812951409786880L, 812964294688768L, 812990064492544L, 3347287057104896L),
	Balance(Sage, DPS, 2031339142381616L, "Balance", /*2157001295527936L, */3347338596712448L/*, 813226287693824L, 3347334301745152L*/), // shared with Serenity
	Combat(Sentinel, DPS, 2031339142381613L, "Combat", true, 1261706772742144L, 1261711067709440L, 3409001442181120L, 2209167968305152L),
	Watchman(Sentinel, DPS, 2031339142381614L, "Watchman", true, 3460841697443840L, 3409216190545920L, 1261715362676736L, 1261719657644032L),
	Concentration(Sentinel, DPS, 2031339142381611L, "Concentration", true, 3460562524569600L), // shared with Focus
	Focus(Guardian, DPS, 2031339142381607L, "Focus", 3462722893119488L, 3460562524569600L, 1263338860314624L, 1263330270380032L),
	Vigilance(Guardian, DPS, 2031339142381610L, "Vigilance", 2210512293068800L, 3429368177098752L, 1280806492307456L, 3503417708249088L),
	Defense(Guardian, TANK, 2031339142381609L, "Defense", 2211203782803456L, 3453231015395328L, 812483258351616L, 3409336449630208L),
	Plasmatech(Vanguard, DPS, 2031339142381638L, "Plasmatech", 3391499450449920L/*, 3391477975613440L*/, 3391666954174464L, 3391447910842368L), // shared with AssaultSpecialist
	ShieldSpecialist(Vanguard, TANK, 2031339142381641L, "Shield Specialist", 2264568751456256L, 3389987621961728L, 3396893929373696L, 999889861345280L),
	Tactics(Vanguard, DPS, 2031339142381640L, "Tactics", 3393277566910464L, 3393354876321792L, 2029878853500928L, 3393260387041280L),
	Infiltration(Shadow, DPS, 2031339142381620L, "Infiltration", 980042817470464L, 1962349082705920L, 3401425119870976L, 979819479171072L),
	KineticCombat(Shadow, TANK, 2031339142381622L, "Kinetic Combat", 3414228417380352L, 2453757765877760L, 980828796485632L, 981503106351104L, 980485199101952L),
	Serenity(Shadow, DPS, 2031339142381623L, "Serenity", 3414395921104896L),
	Gunnery(Commando, DPS, 2031339142381636L, "Gunnery", 3393522380046336L, 2025880238948352L, 1202535008305152L, 3393513790111744L),
	AssaultSpecialist(Commando, DPS, 2031339142381634L, "Assault Specialist", 3393762898214912L, 3418278571540480L),
	CombatMedic(Commando, HEALER, 2031339142381637L, "Combat Medic", 999090997428224L, 3393462250504192L, 3393470840438784L, 2496909302300672L, 999516199190528L),
	Sharpshooter(Gunslinger, DPS, 2031339142381627L, "Sharpshooter", 1112293450448896L, 2302072405884928L, 1107762259951616L, 3404435891945472L, 2301861952487424L),
	Saboteur(Gunslinger, DPS, 2031339142381628L, "Saboteur", 1159319047372800L, 1957027618226176L, 3404564740964352L, 1957100632670208L),
	DirtyFighting(Gunslinger, DPS, 2031339142381625L, "Dirty Fighting", 3404611985604608L, 3404620575539200L, /*807698664783872L, */807711549685760L);

	private final String fullName;
	private final CharacterRole role;
	private final long[] abilities;
	private final boolean isDualWield;
	private final CharacterClass className;
	private final long disciplineGuid;

	CharacterDiscipline(CharacterClass className, CharacterRole role, long disciplineGuid, String fullName, long... abilities) {
		this(className, role, disciplineGuid, fullName, false, abilities);
	}

	CharacterDiscipline(CharacterClass className, CharacterRole role, long disciplineGuid, String fullName, Boolean isDualWield, long... abilities) {
		this.fullName = fullName;
		this.role = role;
		this.abilities = abilities;
		this.isDualWield = isDualWield;
		this.className = className;
		this.disciplineGuid = disciplineGuid;
	}

	public String getFullName() {
		return fullName;
	}

	public long[] getAbilities() {
		return abilities;
	}

	public boolean isDualWield() {
		return isDualWield;
	}

	public CharacterRole getRole() {
		return role;
	}

	public CharacterClass getCharacterClass() {
		return className;
	}

	public String toString() {
		return fullName;
	}

	public static CharacterDiscipline fromGuid(final String disciplineGuid) {
		try {
			final long guid = Long.parseLong(disciplineGuid);
			for (final CharacterDiscipline d : values()) {
				if (guid == d.disciplineGuid) {
					return d;
				}
			}
		} catch (Exception ignored) {

		}
		return null;
	}
}
