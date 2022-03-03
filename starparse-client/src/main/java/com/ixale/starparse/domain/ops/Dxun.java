package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.NpcType;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBoss.BossUpgradeCallback;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.parser.Helpers;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;

import java.util.ArrayList;
import java.util.HashMap;

public class Dxun extends Raid {

	private static final long RED_SM_8M = 4246176467517440L,
			RED_SM_16M = -100L,
			RED_HM_8M = 4330233272467456L,
			RED_HM_16M = -200L;

	private static final long BREACH_SM_8M = 4333218274738176L,
			BREACH_SM_16M = -1123L,
			BREACH_HM_8M = -1223L,
			BREACH_HM_16M = -1323L;

	private static final long TRANDOSHAN_SQUAD_SM_8M = 4245970309087232L,
			TRANDOSHAN_SQUAD_SM_16M = -1423L,
			TRANDOSHAN_SQUAD_HM_8M = -123,
			TRANDOSHAN_SQUAD_HM_16M = -1623L;

	private static final long HUNTMASTER_SM_8M = 4265104388390912L,
			HUNTMASTER_SM_16M = -1523L,
			HUNTMASTER_HM_8M = 4330237567434752L, // Shelleigh
			HUNTMASTER_HM_16M = -1723L;

	private static final long APEX_VG_SM_8M = 4282872668094464L, // 4285612857229312
			APEX_VG_SM_16M = -1233L,
			APEX_VG_HM_8M = 4350020186800128L,
			APEX_VG_HM_16M = -1263L;

	private static final String RED_PHASE_RED = "Red",
			RED_PHASE_BULL = "Bull";

	private static final String BREACH_PHASE_RUN = "Run",
			BREACH_PHASE_WARDEN = "Warden",
			BREACH_PHASE_REAPER = "Reaper",
			BREACH_PHASE_SANITIZER = "Sanitizer";

	private static final String TRANDOSHAN_SQUAD_PHASE_ONE = "Damage",
			TRANDOSHAN_SQUAD_PHASE_ULTIMATE_HUNTER = "Ultimate Hunter";

	private static final String HUNTMASTER_PHASE_OPENING = "Opening",
			HUNTMASTER_PHASE_CHARGER = "Charger",
			HUNTMASTER_PHASE_FORTRESS = "Fortress",
			HUNTMASTER_PHASE_LAKE = "Lake",
			HUNTMASTER_PHASE_DAMAGE = "Damage",
			HUNTMASTER_PHASE_SHELLEIGH = "Shelleigh";

	private static final String APEX_VG_PHASE_OPENING = "Opening",
			APEX_VG_PHASE_BATTERY = "Battery",
			APEX_VG_PHASE_DAMAGE = "Damage",
			APEX_VG_PHASE_VOLTINATOR = "Voltinator";

	private final HashMap<String, ArrayList<Long>> flareBuilds = new HashMap<>();

	public Dxun() {
		super("The Nature of Progress");

		RaidBoss.add(this, RaidBossName.Red,
				new long[]{RED_SM_8M}, // SM 8m
				new long[]{RED_SM_16M}, // SM 16m
				new long[]{RED_HM_8M}, // HM 8m
				new long[]{RED_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.BreachCI004,
				new long[]{BREACH_SM_8M}, // SM 8m
				new long[]{BREACH_SM_16M}, // SM 16m
				new long[]{BREACH_HM_8M}, // HM 8m
				new long[]{BREACH_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.TrandoshanSquad,
				new long[]{TRANDOSHAN_SQUAD_SM_8M, 4245978899021824L, 4245983193989120L, 4245987488956416L}, // SM 8m
				new long[]{TRANDOSHAN_SQUAD_SM_16M}, // SM 16m
				new long[]{TRANDOSHAN_SQUAD_HM_8M}, // HM 8m
				new long[]{TRANDOSHAN_SQUAD_HM_16M}, // HM 16m
				null);

//		addChallenge(RaidBossName.TrandoshanSquad, new TrandoshanSquadUltimateHunterChallenge());
//
		RaidBoss.add(this, RaidBossName.TheHuntmaster,
				new long[]{HUNTMASTER_SM_8M, 4281661487316992L}, // SM 8m
				new long[]{HUNTMASTER_SM_16M}, // SM 16m
				new long[]{HUNTMASTER_HM_8M}, // HM 8m
				new long[]{HUNTMASTER_HM_16M}, // HM 16m
				null);

//		addChallenge(RaidBossName.TheHuntmaster, new HuntmasterShelleighChallenge());
//
		RaidBoss.add(this, RaidBossName.ApexVanguard,
				new long[]{APEX_VG_SM_8M}, // SM 8m
				new long[]{APEX_VG_SM_16M}, // SM 16m
				new long[]{APEX_VG_HM_8M}, // HM 8m
				new long[]{APEX_VG_HM_16M}, // HM 16m
				new BossUpgradeCallback() {

					@Override
					public RaidBoss upgradeByAbility(long guid, long effectGuid, Integer value, RaidBoss nimBoss) {
						if (effectGuid == 4305240857772294L) {
							// Acid Blast Effect (only nim)
							return nimBoss;
						}
						return null;
					}

					@Override
					public RaidBoss upgradeByNpc(long guid, RaidBoss boss) {
						return null;
					}
				});
//
//		addChallenge(RaidBossName.ApexVanguard, new ApexVoltinatorChallenge());

		npcs.put(4246356856143872L, new Npc(NpcType.boss_1 /*boss_raid*/)); // Stampeding Bull, boss.the_pack_leader.charging_bull, npc.operation.dxun.difficulty_1.boss.the_pack_leader.charging_bull
		npcs.put(4246176467517440L, new Npc(NpcType.boss_raid)); // Red, boss.the_pack_leader.red, npc.operation.dxun.difficulty_1.boss.the_pack_leader.red
		npcs.put(4330233272467456L, new Npc(NpcType.boss_raid)); // Red, boss.the_pack_leader.red, npc.operation.dxun.difficulty_2.boss.the_pack_leader.red
		npcs.put(4382185196879872L, new Npc(NpcType.boss_raid)); // Red, boss.the_pack_leader.red, npc.operation.dxun.difficulty_3.boss.the_pack_leader.red
//		npcs.put(4247202964701184L, new Npc(NpcToughness.boss_raid)); // Venomous Harvorisk, boss.the_pack_leader.venomous_shriek, npc.operation.dxun.difficulty_1.boss.the_pack_leader.venomous_shriek
//		npcs.put(4246253776928768L, new Npc(NpcToughness.boss_2)); // Venomous Stalker, boss.the_pack_leader.venomous_stalker, npc.operation.dxun.difficulty_1.boss.the_pack_leader.venomous_stalker
//		npcs.put(4247112770387968L, new Npc(NpcToughness.boss_2)); // Venomous Stalker, boss.the_pack_leader.venomous_stalker_nightmare, npc.operation.dxun.difficulty_1.boss.the_pack_leader.venomous_stalker_nightmare
//		npcs.put(4247108475420672L, new Npc(NpcToughness.boss_2)); // Venomous Stalker, boss.the_pack_leader.venomous_stalker_veteran, npc.operation.dxun.difficulty_1.boss.the_pack_leader.venomous_stalker_veteran

//		npcs.put(4250475729780736L, new Npc(NpcToughness.boss_2)); // Felshade Reaper, boss.holding_pens.cz_felshade, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_felshade
//		npcs.put(4256042007396352L, new Npc(NpcToughness.boss_1)); // Felshade Hunter, boss.holding_pens.cz_felshade_lesser, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_felshade_lesser
//		npcs.put(4333506037547008L, new Npc(NpcToughness.boss_1)); // Felshade Hunter, boss.holding_pens.cz_felshade_lesser_veteran, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_felshade_lesser_veteran
//		npcs.put(4408835468951552L, new Npc(NpcToughness.boss_2)); // Cz Felshade Reaper Mount, boss.holding_pens.cz_felshade_reaper_mount, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_felshade_reaper_mount
//		npcs.put(4333471677808640L, new Npc(NpcToughness.boss_2)); // Felshade Reaper, boss.holding_pens.cz_felshade_veteran, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_felshade_veteran
//		npcs.put(4250514384486400L, new Npc(NpcToughness.boss_raid)); // Rampaging Charger, boss.holding_pens.cz_fiesty_feast, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_fiesty_feast
//		npcs.put(4397956316790784L, new Npc(NpcToughness.boss_raid)); // Delicious Rampaging Charger, boss.holding_pens.cz_fiesty_feast_reaper_bait, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_fiesty_feast_reaper_bait
//		npcs.put(4334515354861568L, new Npc(NpcToughness.boss_raid)); // Rampaging Charger, boss.holding_pens.cz_fiesty_feast_veteran, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_fiesty_feast_veteran
//		npcs.put(4334133102772224L, new Npc(NpcToughness.boss_1)); // Shadow Reaper, boss.holding_pens.cz_shadow_reaper_veteran, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_shadow_reaper_veteran
//		npcs.put(4251424917553152L, new Npc(NpcToughness.boss_1)); // Living Smoke, boss.holding_pens.cz_smoke, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_smoke
//		npcs.put(4333463087874048L, new Npc(NpcToughness.boss_1)); // Living Smoke, boss.holding_pens.cz_smoke_veteran, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_smoke_veteran
//		npcs.put(4333218274738176L, new Npc(NpcToughness.boss_1)); // Crimson Stalker, boss.holding_pens.cz_stalker, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_stalker
//		npcs.put(4333458792906752L, new Npc(NpcToughness.boss_1)); // Crimson Stalker, boss.holding_pens.cz_stalker_veteran, npc.operation.dxun.difficulty_1.boss.holding_pens.cz_stalker_veteran
//		npcs.put(4389417921806336L, new Npc(NpcToughness.boss_raid)); // Czerka Pursuit Droid, boss.holding_pens.czerka_predator_droid, npc.operation.dxun.difficulty_1.boss.holding_pens.czerka_predator_droid
//		npcs.put(4377769970499584L, new Npc(NpcToughness.boss_raid)); // Czerka Warden Predator Droid, boss.holding_pens.czerka_predator_droid, npc.operation.dxun.difficulty_3.boss.holding_pens.czerka_predator_droid
//		npcs.put(4390229670625280L, new Npc(NpcToughness.boss_raid)); // Czerka Pursuit Droid, boss.holding_pens.czerka_predator_droid_checkpoint, npc.operation.dxun.difficulty_1.boss.holding_pens.czerka_predator_droid_checkpoint
//		npcs.put(4390328454873088L, new Npc(NpcToughness.boss_raid)); // Czerka Pursuit Droid, boss.holding_pens.czerka_predator_droid_control_room, npc.operation.dxun.difficulty_1.boss.holding_pens.czerka_predator_droid_control_room
//		npcs.put(4383168744390656L, new Npc(NpcToughness.boss_raid)); // Czerka Predator Droid Control Room, boss.holding_pens.czerka_predator_droid_control_room, npc.operation.dxun.difficulty_3.boss.holding_pens.czerka_predator_droid_control_room
//		npcs.put(4390556088139776L, new Npc(NpcToughness.boss_raid)); // Czerka Pursuit Droid, boss.holding_pens.czerka_predator_droid_control_room_idle, npc.operation.dxun.difficulty_1.boss.holding_pens.czerka_predator_droid_control_room_idle
//		npcs.put(4390577562976256L, new Npc(NpcToughness.boss_raid)); // Czerka Sanitizer Droid, boss.holding_pens.czerka_pyro_droid, npc.operation.dxun.difficulty_1.boss.holding_pens.czerka_pyro_droid
		npcs.put(4377860164812800L, new Npc(NpcType.boss_raid, "Incineration Droid")); // Czerka Warden Incineration Droid, boss.holding_pens.czerka_pyro_droid, npc.operation.dxun.difficulty_3.boss.holding_pens.czerka_pyro_droid
//		npcs.put(4245686841245696L, new Npc(NpcToughness.boss_raid)); // Czerka Warden Droid, boss.holding_pens.czerka_warden_droid, npc.operation.dxun.difficulty_1.boss.holding_pens.czerka_warden_droid
//		npcs.put(4338552624119808L, new Npc(NpcToughness.boss_raid)); // Czerka Warden Enforcer Droid, boss.holding_pens.czerka_warden_droid_friendly, npc.operation.dxun.difficulty_1.boss.holding_pens.czerka_warden_droid_friendly
//		npcs.put(4257339087519744L, new Npc(NpcToughness.boss_raid)); // Lake Crab, boss.holding_pens.lake_crab, npc.operation.dxun.difficulty_1.boss.holding_pens.lake_crab
//		npcs.put(4250819327164416L, new Npc(NpcToughness.boss_raid)); // Security Coil, boss.holding_pens.pens_turret, npc.operation.dxun.difficulty_1.boss.holding_pens.pens_turret
//		npcs.put(4258451484049408L, new Npc(NpcToughness.boss_raid)); // Secondary Generator, boss.holding_pens.secondary_generator, npc.operation.dxun.difficulty_1.boss.holding_pens.secondary_generator

		npcs.put(4245970309087232L, new Npc(NpcType.boss_raid)); // Hissyphus, boss.morphing_trandoshans.acid_trandoshan, npc.operation.dxun.difficulty_1.boss.morphing_trandoshans.acid_trandoshan
		npcs.put(4245978899021824L, new Npc(NpcType.boss_raid)); // Greus, boss.morphing_trandoshans.fire_trandoshan, npc.operation.dxun.difficulty_1.boss.morphing_trandoshans.fire_trandoshan
		npcs.put(4245983193989120L, new Npc(NpcType.boss_raid)); // Kronissus, boss.morphing_trandoshans.ice_trandoshan, npc.operation.dxun.difficulty_1.boss.morphing_trandoshans.ice_trandoshan
		npcs.put(4245987488956416L, new Npc(NpcType.boss_raid)); // Titax, boss.morphing_trandoshans.strength_trandoshan, npc.operation.dxun.difficulty_1.boss.morphing_trandoshans.strength_trandoshan
		npcs.put(4381150109761536L, new Npc(NpcType.boss_raid)); // The Ultimate Hunter, boss.morphing_trandoshans.the_ultimate_hunter, npc.operation.dxun.difficulty_1.boss.morphing_trandoshans.the_ultimate_hunter

		npcs.put(4265104388390912L, new Npc(NpcType.boss_raid)); // Huntmaster, boss.huntmaster.huntmaster, npc.operation.dxun.difficulty_1.boss.huntmaster.huntmaster
		npcs.put(4281661487316992L, new Npc(NpcType.boss_raid)); // Shelleigh, boss.huntmaster.beast_crab, npc.operation.dxun.difficulty_1.boss.huntmaster.beast_crab
		npcs.put(4330237567434752L, new Npc(NpcType.boss_raid)); // Shelleigh, boss.huntmaster.beast_crab, npc.operation.dxun.difficulty_2.boss.huntmaster.beast_crab
//		npcs.put(4277723002306560L, new Npc(NpcToughness.boss_1)); // Lurking Beast, boss.huntmaster.beast_holdout, npc.operation.dxun.difficulty_1.boss.huntmaster.beast_holdout
//		npcs.put(4270898299273216L, new Npc(NpcToughness.boss_1)); // Lurking Beast, boss.huntmaster.beast_standard, npc.operation.dxun.difficulty_1.boss.huntmaster.beast_standard
//		npcs.put(4271624148746240L, new Npc(NpcToughness.boss_raid)); // Deliberate Charger, boss.huntmaster.beast_charger, npc.operation.dxun.difficulty_1.boss.huntmaster.beast_charger

		npcs.put(4282872668094464L, new Npc(NpcType.boss_raid)); // Apex Vanguard, boss.hybrid_horror.hybrid_horror, npc.operation.dxun.difficulty_1.boss.hybrid_horror.hybrid_horror
		npcs.put(4350020186800128L, new Npc(NpcType.boss_raid)); // Apex Vanguard, boss.hybrid_horror.hybrid_horror, npc.operation.dxun.difficulty_2.boss.hybrid_horror.hybrid_horror
		npcs.put(4297557161279488L, new Npc(NpcType.boss_1 /*boss_raid*/)); // Power Transformer, boss.hybrid_horror.power_transformer, npc.operation.dxun.difficulty_1.boss.hybrid_horror.power_transformer
//		npcs.put(4383022715502592L, new Npc(NpcToughness.boss_1)); // CI Anti-Theft Droid, boss.hybrid_horror.tether_droid, npc.operation.dxun.difficulty_1.boss.hybrid_horror.tether_droid

	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {
		if (c.getBoss() == null) return null;

		if (Helpers.isTargetThisPlayer(e) && Helpers.isAbilityEqual(e, 4250840802000896L)) { // Breach Flare (self)
			if (!(phaseTimers.containsKey("Flare")) || (e.getTimestamp() - phaseTimers.get("Flare") > 4000)) { // no flare yet or longer than 4 seconds ago
				phaseTimers.put("Flare", e.getTimestamp());

				TimerManager.stopTimer(BreachFlareTimer.class);
				TimerManager.startTimer(BreachFlareTimer.class, e.getTimestamp());
			}
		}

		switch (c.getBoss().getRaidBossName()) {
			case Red:
				return getNewPhaseNameForRed(e, currentPhaseName);
			case BreachCI004:
				return getNewPhaseNameForBreach(e, currentPhaseName);
			case TrandoshanSquad:
				return getNewPhaseNameForTrandoshanSquad(e, c, currentPhaseName);
			case TheHuntmaster:
				return getNewPhaseNameForHuntmaster(e, currentPhaseName);
			case ApexVanguard:
				return getNewPhaseNameForApex(e, c, currentPhaseName);
			default:
				return null;
		}
	}

	private String getNewPhaseNameForRed(final Event e, final String currentPhaseName) {
		if (Helpers.isTargetOtherPlayer(e)) return null; // returns if target is other player

		// ------------------ Red ------------------

		if (currentPhaseName == null) {
			phaseTimers.clear();
			return RED_PHASE_RED;
		}

		// ------------------ Bull ------------------

		if (RED_PHASE_RED.equals(currentPhaseName) && Helpers.isTargetOrSourceWithin(e, 4246356856143872L)) { // Bull
			phaseTimers.put(RED_PHASE_BULL, e.getTimestamp() + 5000); // add 5 sec bull action
			return RED_PHASE_BULL;
		}

		if (RED_PHASE_BULL.equals(currentPhaseName)) {
			if (Helpers.isTargetOrSourceWithin(e, 4246356856143872L)) { // Bull
				phaseTimers.put(RED_PHASE_BULL, e.getTimestamp() + 5000); // add 5 sec bull action
				return null;
			}

			if (phaseTimers.get(RED_PHASE_BULL) <= e.getTimestamp()) { // no warden action 
				phaseTimers.remove(RED_PHASE_BULL);
				return RED_PHASE_RED;
			}
		}

		return null;
	}

	private String getNewPhaseNameForBreach(final Event e, final String currentPhaseName) {
		if (Helpers.isTargetOtherPlayer(e)) return null; // returns if target is other player

		// ------------------ Run ------------------

		if (currentPhaseName == null) {
			phaseTimers.clear();
			return BREACH_PHASE_RUN;
		}

		// ------------------ Warden ------------------

		if (BREACH_PHASE_RUN.equals(currentPhaseName) && Helpers.isTargetOrSourceWithin(e, 4245686841245696L)) { // Warden
			phaseTimers.put(BREACH_PHASE_WARDEN, e.getTimestamp() + 15000); // add 15 sec warden action
			return BREACH_PHASE_WARDEN;
		}

		if (BREACH_PHASE_WARDEN.equals(currentPhaseName)) {
			if (Helpers.isTargetOrSourceWithin(e, 4245686841245696L)) { // Warden
				phaseTimers.put(BREACH_PHASE_WARDEN, e.getTimestamp() + 15000); // add 15 sec warden action
				return null;
			}

			if (phaseTimers.get(BREACH_PHASE_WARDEN) <= e.getTimestamp()) { // no warden action 
				phaseTimers.remove(BREACH_PHASE_WARDEN);
				return BREACH_PHASE_RUN;
			}
		}

		// ------------------ Reaper ------------------

		if (BREACH_PHASE_RUN.equals(currentPhaseName) && Helpers.isTargetOrSourceWithin(e, 4333471677808640L)) { // Reaper
			phaseTimers.put(BREACH_PHASE_REAPER, e.getTimestamp() + 15000); // add 15 sec warden action
			return BREACH_PHASE_REAPER;
		}

		if (BREACH_PHASE_REAPER.equals(currentPhaseName)) {
			if (Helpers.isTargetOrSourceWithin(e, 4333471677808640L)) { // Reaper
				phaseTimers.put(BREACH_PHASE_REAPER, e.getTimestamp() + 15000); // add 15 sec reaper action
				return null;
			}

			if (phaseTimers.get(BREACH_PHASE_REAPER) <= e.getTimestamp()) { // no reaper action 
				phaseTimers.remove(BREACH_PHASE_REAPER);
				return BREACH_PHASE_RUN;
			}
		}

		if (BREACH_PHASE_RUN.equals(currentPhaseName) && Helpers.isTargetOrSourceWithin(e, 4390577562976256L)) { // Sanitizer
			return BREACH_PHASE_SANITIZER;
		}

		return null;
	}

	private String getNewPhaseNameForTrandoshanSquad(final Event e, final Combat c, final String currentPhaseName) {
		if (Helpers.isTargetOtherPlayer(e)) return null; // returns if target is other player

		// ------------------ Phase One ------------------

		if (currentPhaseName == null) {
			phaseTimers.clear();
			return TRANDOSHAN_SQUAD_PHASE_ONE;
		}

		// ------------------ Ultimate Hunter ------------------

		if (!TRANDOSHAN_SQUAD_PHASE_ULTIMATE_HUNTER.equals(currentPhaseName) && Helpers.isTargetOrSourceWithin(e, 4381150109761536L)) {
			return TRANDOSHAN_SQUAD_PHASE_ULTIMATE_HUNTER;
		}

		return null;
	}

	private String getNewPhaseNameForHuntmaster(final Event e, final String currentPhaseName) {
		if (Helpers.isTargetOtherPlayer(e)) return null; // returns if target is other player

		// ------------------ Opening ------------------

		if (currentPhaseName == null) {
			phaseTimers.clear();
			return HUNTMASTER_PHASE_OPENING;
		}

		// ------------------ Fortress ------------------

		if (!HUNTMASTER_PHASE_FORTRESS.equals(currentPhaseName) && (Helpers.isAbilityEqual(e, 4271405105414144L) || Helpers.isAbilityEqual(e, 4387871733579776L))) { // Fortress, Flying Fortress
			return HUNTMASTER_PHASE_FORTRESS;
		}

		// ------------------ Charger ------------------

		if (!HUNTMASTER_PHASE_CHARGER.equals(currentPhaseName) && Helpers.isTargetOrSourceWithin(e, 4271624148746240L)) { // Charger
			return HUNTMASTER_PHASE_CHARGER;
		}

		// ------------------ Lake ------------------

		if (Helpers.isSourceWithin(e, HUNTMASTER_HM_8M, HUNTMASTER_HM_16M)) { // Huntmaster
			if (!HUNTMASTER_PHASE_LAKE.equals(currentPhaseName) && Helpers.isAbilityEqual(e, 4277787426816333L)) { // Holdout Cover
				return HUNTMASTER_PHASE_LAKE;
			}

			if (!HUNTMASTER_PHASE_DAMAGE.equals(currentPhaseName)) {
				if (HUNTMASTER_PHASE_LAKE.equals(currentPhaseName) && !Helpers.isAbilityEqual(e, 4277787426816333L)) { // no more Holdout Cover (dds)
					return HUNTMASTER_PHASE_DAMAGE;
				}

				if (HUNTMASTER_PHASE_CHARGER.equals(currentPhaseName) && !Helpers.isAbilityEqual(e, 4269910456795136L)) { // no more Range Shot (tanks)
					return HUNTMASTER_PHASE_DAMAGE;
				}
			}
		}

		// ------------------ Shelleigh ------------------

		if (!HUNTMASTER_PHASE_SHELLEIGH.equals(currentPhaseName) && Helpers.isTargetOrSourceWithin(e, HUNTMASTER_HM_8M)) { // Shelleigh
			return HUNTMASTER_PHASE_SHELLEIGH;
		}

		return null;
	}

	private String getNewPhaseNameForApex(final Event e, final Combat c, final String currentPhaseName) {
		String sourceName = e.getSource().getName();
		boolean isApply = Helpers.isActionApply(e);

		// ------------------ Timers ------------------

		if (Helpers.isAbilityEqual(e, 4296410405011456L)) { // Apex Flare
			TimerManager.stopTimer(ApexFlareTimer.class);
			TimerManager.startTimer(ApexFlareTimer.class, e.getTimestamp());
		}

		if (Helpers.isAbilityEqual(e, 4382760722497536L)) { // Insert/Extract Battery
			if (!(flareBuilds.containsKey(sourceName))) flareBuilds.put(sourceName, new ArrayList<Long>());

			ArrayList<Long> flares = flareBuilds.get(sourceName);

			if (flares.size() == 3 && flares.get(0) - flares.get(1) <= 2000 && flares.get(1) - flares.get(2) <= 2000) { // three uses from same person, uses with max 2 sec delay
				TimerManager.startTimer(ApexFlareBuildTimer.class, flares.get(0));
			}

			flares.clear();
		}

		if (Helpers.isAbilityEqual(e, 4326784413728768L)) { // Use Battery
			if (!(flareBuilds.containsKey(sourceName))) flareBuilds.put(sourceName, new ArrayList<Long>());

			flareBuilds.get(sourceName).add(e.getTimestamp());
		}

		if (!phaseTimers.containsKey("Acid") || e.getTimestamp() - phaseTimers.get("Acid") > 26000) { // no acid blast yet or longer than 26 seconds ago
			if (isApply && Helpers.isEffectEqual(e, 4305240857772294L)) { // Acid Blast Effect (MM)
				phaseTimers.put("Acid", e.getTimestamp());

				TimerManager.stopTimer(ApexAcidBlastTimer.class);
				TimerManager.startTimer(ApexAcidBlastTimer.class, e.getTimestamp());
			} else if (c.getBoss().getMode() != Mode.NiM && Helpers.isAbilityEqual(e, 4305240857772032L)) { // Acid Blast Damage (SM/VM)
				phaseTimers.put("Acid", e.getTimestamp());

				TimerManager.stopTimer(ApexAcidBlastTimer.class);
				TimerManager.startTimer(ApexAcidBlastTimer.class, e.getTimestamp());
			}
		}

		if (Helpers.isEffectEqual(e, 4308741256118272L)) { // Rocket
			if (!phaseTimers.containsKey("Rockets") || e.getTimestamp() - phaseTimers.get("Rockets") > 4000) { // no rocket yet or longer than 4 seconds ago
				phaseTimers.put("Rockets", e.getTimestamp());

				TimerManager.stopTimer(ApexRocketsTimer.class);
				TimerManager.startTimer(ApexRocketsTimer.class, e.getTimestamp());
			}
		}

		if (isApply && Helpers.isEffectEqual(e, 4308741256118272L)) { // Rocket Effect
			TimerManager.stopTimer(ApexRocketsTimer.class);
			TimerManager.startTimer(ApexRocketsTimer.class, e.getTimestamp());
		}

		if (Helpers.isTargetOtherPlayer(e)) return null; // returns if target is other player

		if (Helpers.isEffectEqual(e, 4352726016196922L)) { // Contagion
			if (TimerManager.getTimer(ApexContagionTimer.class) == null)
				TimerManager.startTimer(ApexContagionTimer.class, e.getTimestamp());
		}

		if (Helpers.isEffectEqual(e, 4308938824613888L)) { // Mass Target Lock
			if (isApply)
				TimerManager.startTimer(ApexMassTargetLockTimer.class, e.getTimestamp()); // start on effect gain
			else TimerManager.stopTimer(ApexMassTargetLockTimer.class); // stop on effect lost
		}

		// ------------------ Opening ------------------

		if (currentPhaseName == null) {
			phaseTimers.clear();
			return APEX_VG_PHASE_OPENING;
		}

		// ------------------ Transformator ------------------

		if (APEX_VG_PHASE_OPENING.equals(currentPhaseName) && Helpers.isTargetEqual(e, 4297557161279488L)) { // Transformator
			phaseTimers.put(APEX_VG_PHASE_BATTERY, e.getTimestamp() + 4000); // add 4 sec transformator action

			TimerManager.stopTimer(ApexRocketsTimer.class);
			TimerManager.stopTimer(ApexAcidBlastTimer.class);
			return APEX_VG_PHASE_BATTERY;
		}

		if (APEX_VG_PHASE_BATTERY.equals(currentPhaseName)) {
			if (Helpers.isTargetEqual(e, 4297557161279488L)) { // Transformator
				phaseTimers.put(APEX_VG_PHASE_BATTERY, e.getTimestamp() + 4000); // add 4 sec transformator action
				return null;
			}

			if (phaseTimers.get(APEX_VG_PHASE_BATTERY) <= e.getTimestamp()) { // no transformator action 
				phaseTimers.remove(APEX_VG_PHASE_BATTERY);
				return APEX_VG_PHASE_DAMAGE;
			}
		}

		// ------------------ Voltinator ------------------

		if (APEX_VG_PHASE_DAMAGE.equals(currentPhaseName) && Helpers.isEffectEqual(e, 4310493602775302L)) { // Blinding Spray
			phaseTimers.put(APEX_VG_PHASE_VOLTINATOR, e.getTimestamp() + 20000); // 20 sec blinding
			return APEX_VG_PHASE_VOLTINATOR;
		}

		if (APEX_VG_PHASE_VOLTINATOR.equals(currentPhaseName) && phaseTimers.get(APEX_VG_PHASE_VOLTINATOR) <= e.getTimestamp()) { // no blinding effect
			phaseTimers.remove(APEX_VG_PHASE_VOLTINATOR);
			return APEX_VG_PHASE_DAMAGE;
		}

		return null;
	}

	public static class BreachFlareTimer extends BaseTimer {
		public BreachFlareTimer() {
			super("Flare", "Breach Flare", 20000);
			setColor(1);
		}
	}
//
//	public static class TrandoshanSquadUltimateHunterChallenge extends CombatChallenge {
//		public TrandoshanSquadUltimateHunterChallenge() {
//			super(RaidChallengeName.UltimateHunter, TRANDOSHAN_SQUAD_PHASE_ULTIMATE_HUNTER, CombatChallenge.Type.DAMAGE,
//					Arrays.asList(new Object[]{4381150109761536L}), "target_guid IN (?)");
//		}
//	}
//
//	public static class HuntmasterShelleighChallenge extends CombatChallenge {
//		public HuntmasterShelleighChallenge() {
//			super(RaidChallengeName.Shelleigh, HUNTMASTER_PHASE_SHELLEIGH, CombatChallenge.Type.DAMAGE,
//					Arrays.asList(new Object[]{4330237567434752L}), "target_guid IN (?)");
//		}
//	}

	public static class ApexFlareTimer extends BaseTimer {
		public ApexFlareTimer() {
			super("Flare", "Apex Flare", 32000);
			setColor(1);
		}
	}

	public static class ApexFlareBuildTimer extends BaseTimer {
		public ApexFlareBuildTimer() {
			super("Flare Build", "Apex Flare Build (on 3 flares)", 45000);
			setColor(1);
		}
	}

	public static class ApexContagionTimer extends BaseTimer {
		public ApexContagionTimer() {
			super("Contagion", "Apex Contagion", 60000);
			setColor(3);
		}
	}

	public static class ApexAcidBlastTimer extends BaseTimer {
		public ApexAcidBlastTimer() {
			super("Acid", "Apex Acid Blast", 30000);
			setColor(2);
		}
	}

	public static class ApexRocketsTimer extends BaseTimer {
		public ApexRocketsTimer() {
			super("Rockets", "Apex Rockets", 24000);
			setColor(0);
		}
	}

	public static class ApexMassTargetLockTimer extends BaseTimer {
		public ApexMassTargetLockTimer() {
			super("Mass Target", "Apex Mass Target", 30000);
			setColor(0);
		}
	}

//	public static class ApexVoltinatorChallenge extends CombatChallenge {
//		public ApexVoltinatorChallenge() {
//			super(RaidChallengeName.Voltinator, APEX_VG_PHASE_VOLTINATOR, CombatChallenge.Type.DAMAGE,
//					Arrays.asList(new Object[]{APEX_VG_SM_8M_16M, APEX_VG_HM_8M_16M}), "target_guid IN (?, ?)");
//		}
//	}
}