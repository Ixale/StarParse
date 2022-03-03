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

public class TerrorFromBeyond extends Raid {
	private static final String TFB_PHASE_TENTACLES = "Tentacles",
			TFB_PHASE_BEYOND = "Beyond";

	public TerrorFromBeyond() {
		super("Terror From Beyond");

		RaidBoss.add(this, RaidBossName.TheWrithingHorror,
				new long[]{2962174519541760L}, // SM 8m
				new long[]{3010428477112320L}, // SM 16m
				new long[]{3010424182145024L}, // HM 8m
				new long[]{3010432772079616L}, // HM 16m
				null // not used right now as there is no "backward mode settings"
		);

		RaidBoss.add(this, RaidBossName.DreadGuards,
				new long[]{2938011033534464L, 2938006738567168L, 2938002443599872L}, // Ciphas, Kelsara, Heirad
				new long[]{3013387709579264L, 3013379119644672L, 3013374824677376L},
				new long[]{3013336169971712L, 3013331875004416L, 3013327580037120L},
				new long[]{3013413479383040L, 3013409184415744L, 3013417774350336L},
				new BossUpgradeCallback() {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
						if (guid == 3245096900231168L || guid == 3245736850358272L || guid == 3245131259969536L || guid == 3245732555390976L) {
							// Vitality Extraction, Expiatory Mote 8m / 16m - size already set anyway
							return nimBoss;
						}
						return null;
					}

					@Override
					public RaidBoss upgradeByNpc(long guid, RaidBoss boss) {
						return null;
					}
				});

		RaidBoss.add(this, RaidBossName.OperatorIx,
				new long[]{2939690365747200L, 2942606648541184L}, // Master Control, Operator IX
				new long[]{2994919350206464L, 2994850630729728L},
				new long[]{2994915055239168L, 2994837745827840L},
				new long[]{2994923645173760L, 2994859220664320L},
				null);

		RaidBoss.add(this, RaidBossName.KephessTheUndying,
				new long[]{2937620191510528L},
				new long[]{3013134306508800L},
				new long[]{3013121421606912L},
				new long[]{3013138601476096L},
				null);

		RaidBoss.add(this, RaidBossName.TerrorFromBeyond,
				new long[]{2938891501830144L, 2938895796797440L, 2938887206862848L, 2978340776443904L}, // Tentacles A+B, TFB A+B
				new long[]{3025263294152704L, 3025276179054592L, 3025224639447040L, 3025237524348928L},
				new long[]{3025271884087296L, 3025258999185408L, 3025220344479744L, 3025233229381632L},
				new long[]{3025280474021888L, 3025267589120000L, 3025228934414336L, 3025241819316224L},
				new BossUpgradeCallback() {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
						if (guid == 3246243656499200L || guid == 3250010342817792L) { // Void Disturbance (NiM version) 8m / 16m - size already set anyway
							return nimBoss;
						}
						return null;
					}

					@Override
					public RaidBoss upgradeByNpc(long guid, RaidBoss nimBoss) {
						if (guid == 3245079720361984L) { // Inconsistency 8m (not on 16m?)
							return nimBoss;
						}
						return null;
					}
				});

		bosses.add(new RaidBoss(this, RaidBossName.DreadfulEntity, Mode.HM, Size.Sixteen,
				new long[]{3057806261354496L}));

		npcs.put(2938874321960960L, new Npc(NpcType.boss_raid, "Horror")); // The Writhing Horror, boss_larva, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.larva.boss_larva
		npcs.put(3010424182145024L, new Npc(NpcType.boss_raid, "Horror")); // The Writhing Horror, boss_larva, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.larva.boss_larva
		npcs.put(3010428477112320L, new Npc(NpcType.boss_raid, "Horror")); // The Writhing Horror, boss_larva, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.larva.boss_larva
		npcs.put(3010432772079616L, new Npc(NpcType.boss_raid, "Horror")); // The Writhing Horror, boss_larva, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.larva.boss_larva
		npcs.put(2940287366201344L, new Npc(NpcType.boss_3)); // Jealous Male, boss.larva.tank_baby, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.larva.tank_baby
		npcs.put(3010454246916096L, new Npc(NpcType.boss_3)); // Jealous Male, boss.larva.tank_baby, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.larva.tank_baby
		npcs.put(3010458541883392L, new Npc(NpcType.boss_3)); // Jealous Male, boss.larva.tank_baby, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.larva.tank_baby
		npcs.put(3010467131817984L, new Npc(NpcType.boss_3)); // Jealous Male, boss.larva.tank_baby, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.larva.tank_baby
//		npcs.put(3020465815683072L, new Npc(NpcType.boss_2)); // Twisted Spawn, boss.larva.mutant_baby, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.larva.mutant_baby
//		npcs.put(3020487290519552L, new Npc(NpcType.boss_2)); // Twisted Spawn, boss.larva.mutant_baby, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.larva.mutant_baby

		npcs.put(2938002443599872L, new Npc(NpcType.boss_raid)); // Heirad, boss.dread_guard.dread_guard_01, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.dread_guard.dread_guard_01
		npcs.put(3013327580037120L, new Npc(NpcType.boss_raid)); // Heirad, boss.dread_guard.dread_guard_01, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.dread_guard.dread_guard_01
		npcs.put(3013374824677376L, new Npc(NpcType.boss_raid)); // Heirad, boss.dread_guard.dread_guard_01, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.dread_guard.dread_guard_01
		npcs.put(3013409184415744L, new Npc(NpcType.boss_raid)); // Heirad, boss.dread_guard.dread_guard_01, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.dread_guard.dread_guard_01
		npcs.put(2938006738567168L, new Npc(NpcType.boss_raid)); // Kel'sara, boss.dread_guard.dread_guard_02, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.dread_guard.dread_guard_02
		npcs.put(3013331875004416L, new Npc(NpcType.boss_raid)); // Kel'sara, boss.dread_guard.dread_guard_02, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.dread_guard.dread_guard_02
		npcs.put(3013379119644672L, new Npc(NpcType.boss_raid)); // Kel'sara, boss.dread_guard.dread_guard_02, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.dread_guard.dread_guard_02
		npcs.put(3013413479383040L, new Npc(NpcType.boss_raid)); // Kel'sara, boss.dread_guard.dread_guard_02, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.dread_guard.dread_guard_02
		npcs.put(2938011033534464L, new Npc(NpcType.boss_raid)); // Ciphas, boss.dread_guard.dread_guard_03, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.dread_guard.dread_guard_03
		npcs.put(3013336169971712L, new Npc(NpcType.boss_raid)); // Ciphas, boss.dread_guard.dread_guard_03, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.dread_guard.dread_guard_03
		npcs.put(3013387709579264L, new Npc(NpcType.boss_raid)); // Ciphas, boss.dread_guard.dread_guard_03, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.dread_guard.dread_guard_03
		npcs.put(3013417774350336L, new Npc(NpcType.boss_raid)); // Ciphas, boss.dread_guard.dread_guard_03, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.dread_guard.dread_guard_03
//		npcs.put(3009560893718528L, new Npc(NpcType.boss_2)); // Dread Guard Legionnaire, boss.dread_guard.add_enforcer, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.dread_guard.add_enforcer
//		npcs.put(3013344759906304L, new Npc(NpcType.boss_3)); // Dread Guard Legionnaire, boss.dread_guard.add_enforcer, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.dread_guard.add_enforcer
//		npcs.put(3013357644808192L, new Npc(NpcType.boss_2)); // Dread Guard Legionnaire, boss.dread_guard.add_enforcer, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.dread_guard.add_enforcer
//		npcs.put(3013392004546560L, new Npc(NpcType.boss_3)); // Dread Guard Legionnaire, boss.dread_guard.add_enforcer, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.dread_guard.add_enforcer
//		npcs.put(3009556598751232L, new Npc(NpcType.boss_1)); // Dread Guard Legionnaire, boss.dread_guard.add_executioner, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.dread_guard.add_executioner
//		npcs.put(3013349054873600L, new Npc(NpcType.boss_3)); // Dread Guard Legionnaire, boss.dread_guard.add_executioner, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.dread_guard.add_executioner
//		npcs.put(3013361939775488L, new Npc(NpcType.boss_1)); // Dread Guard Legionnaire, boss.dread_guard.add_executioner, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.dread_guard.add_executioner
//		npcs.put(3013400594481152L, new Npc(NpcType.boss_3)); // Dread Guard Legionnaire, boss.dread_guard.add_executioner, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.dread_guard.add_executioner
//		npcs.put(3009569483653120L, new Npc(NpcType.boss_2)); // Dread Guard Legionnaire, boss.dread_guard.add_reaver, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.dread_guard.add_reaver
//		npcs.put(3013353349840896L, new Npc(NpcType.boss_3)); // Dread Guard Legionnaire, boss.dread_guard.add_reaver, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.dread_guard.add_reaver
//		npcs.put(3013370529710080L, new Npc(NpcType.boss_2)); // Dread Guard Legionnaire, boss.dread_guard.add_reaver, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.dread_guard.add_reaver
//		npcs.put(3013404889448448L, new Npc(NpcType.boss_3)); // Dread Guard Legionnaire, boss.dread_guard.add_reaver, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.dread_guard.add_reaver

		npcs.put(2942606648541184L, new Npc(NpcType.boss_raid)); // Operator IX, boss.core.core_guardian, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.core.core_guardian
		npcs.put(2994837745827840L, new Npc(NpcType.boss_raid)); // Operator IX, boss.core.core_guardian, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.core.core_guardian
		npcs.put(2994850630729728L, new Npc(NpcType.boss_raid)); // Operator IX, boss.core.core_guardian, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.core.core_guardian
		npcs.put(2994859220664320L, new Npc(NpcType.boss_raid)); // Operator IX, boss.core.core_guardian, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.core.core_guardian
//		npcs.put(3014487221207040L, new Npc(NpcType.boss_1)); // Defragmenter, boss.core.battledroid_add, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.core.battledroid_add
		npcs.put(2940596603846656L, new Npc(NpcType.boss_2)); // Data Core, boss.core.data_core, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.core.data_core
		npcs.put(2994867810598912L, new Npc(NpcType.boss_2)); // Data Core, boss.core.data_core, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.core.data_core
		npcs.put(2994872105566208L, new Npc(NpcType.boss_2)); // Data Core, boss.core.data_core, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.core.data_core
		npcs.put(2994876400533504L, new Npc(NpcType.boss_2)); // Data Core, boss.core.data_core, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.core.data_core
//		npcs.put(2954606787166208L, new Npc(NpcType.boss_2)); // Regulator, boss.core.mortal_add, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.core.mortal_add
//		npcs.put(2994880695500800L, new Npc(NpcType.boss_2)); // Regulator, boss.core.mortal_add, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.core.mortal_add
//		npcs.put(2994889285435392L, new Npc(NpcType.boss_2)); // Regulator, boss.core.mortal_add, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.core.mortal_add
//		npcs.put(2994897875369984L, new Npc(NpcType.boss_2)); // Regulator, boss.core.mortal_add, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.core.mortal_add
//		npcs.put(2939690365747200L, new Npc(NpcType.boss_raid)); // Master Control, boss.core.operator, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.core.operator
//		npcs.put(2994915055239168L, new Npc(NpcType.boss_raid)); // Master Control, boss.core.operator, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.core.operator
//		npcs.put(2994919350206464L, new Npc(NpcType.boss_raid)); // Master Control, boss.core.operator, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.core.operator
//		npcs.put(2994923645173760L, new Npc(NpcType.boss_raid)); // Master Control, boss.core.operator, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.core.operator
//		npcs.put(2949766359023616L, new Npc(NpcType.boss_2)); // Rectifier, boss.core.prime_add, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.core.prime_add
//		npcs.put(2994932235108352L, new Npc(NpcType.boss_2)); // Rectifier, boss.core.prime_add, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.core.prime_add
//		npcs.put(2994940825042944L, new Npc(NpcType.boss_2)); // Rectifier, boss.core.prime_add, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.core.prime_add
//		npcs.put(2994949414977536L, new Npc(NpcType.boss_2)); // Rectifier, boss.core.prime_add, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.core.prime_add

		npcs.put(2937620191510528L, new Npc(NpcType.boss_raid, "Kephess")); // Kephess the Undying, boss_kephess, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.kephess.boss_kephess
		npcs.put(3013121421606912L, new Npc(NpcType.boss_raid, "Kephess")); // Kephess the Undying, boss_kephess, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.kephess.boss_kephess
		npcs.put(3013134306508800L, new Npc(NpcType.boss_raid, "Kephess")); // Kephess the Undying, boss_kephess, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.kephess.boss_kephess
		npcs.put(3013138601476096L, new Npc(NpcType.boss_raid, "Kephess")); // Kephess the Undying, boss_kephess, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.kephess.boss_kephess

		npcs.put(2938891501830144L, new Npc(NpcType.boss_4, "Tentacle L")); // Tunneling Tentacle, boss.hypergate_terror.tentacle_1, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.hypergate_terror.tentacle_1
		npcs.put(3025258999185408L, new Npc(NpcType.boss_4, "Tentacle L")); // Tunneling Tentacle, boss.hypergate_terror.tentacle_1, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.hypergate_terror.tentacle_1
		npcs.put(3025263294152704L, new Npc(NpcType.boss_4, "Tentacle L")); // Tunneling Tentacle, boss.hypergate_terror.tentacle_1, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.hypergate_terror.tentacle_1
		npcs.put(3025267589120000L, new Npc(NpcType.boss_4, "Tentacle L")); // Tunneling Tentacle, boss.hypergate_terror.tentacle_1, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.hypergate_terror.tentacle_1
		npcs.put(2938895796797440L, new Npc(NpcType.boss_4, "Tentacle R")); // Tunneling Tentacle, boss.hypergate_terror.tentacle_2, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.hypergate_terror.tentacle_2
		npcs.put(3025271884087296L, new Npc(NpcType.boss_4, "Tentacle R")); // Tunneling Tentacle, boss.hypergate_terror.tentacle_2, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.hypergate_terror.tentacle_2
		npcs.put(3025276179054592L, new Npc(NpcType.boss_4, "Tentacle R")); // Tunneling Tentacle, boss.hypergate_terror.tentacle_2, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.hypergate_terror.tentacle_2
		npcs.put(3025280474021888L, new Npc(NpcType.boss_4, "Tentacle R")); // Tunneling Tentacle, boss.hypergate_terror.tentacle_2, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.hypergate_terror.tentacle_2
		npcs.put(2938887206862848L, new Npc(NpcType.boss_raid, "TFB", 0.5)); // The Terror From Beyond, boss.hypergate_terror.hypergate_terror, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.hypergate_terror.hypergate_terror
		npcs.put(3025220344479744L, new Npc(NpcType.boss_raid, "TFB", 0.5)); // The Terror From Beyond, boss.hypergate_terror.hypergate_terror, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.hypergate_terror.hypergate_terror
		npcs.put(3025224639447040L, new Npc(NpcType.boss_raid, "TFB", 0.5)); // The Terror From Beyond, boss.hypergate_terror.hypergate_terror, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.hypergate_terror.hypergate_terror
		npcs.put(3025228934414336L, new Npc(NpcType.boss_raid, "TFB", 0.5)); // The Terror From Beyond, boss.hypergate_terror.hypergate_terror, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.hypergate_terror.hypergate_terror
		npcs.put(2978340776443904L, new Npc(NpcType.boss_raid, "TFB")); // The Terror From Beyond, boss.hypergate_terror.hypergate_terror_inside, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.hypergate_terror.hypergate_terror_inside
		npcs.put(3025233229381632L, new Npc(NpcType.boss_raid, "TFB")); // The Terror From Beyond, boss.hypergate_terror.hypergate_terror_inside, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.hypergate_terror.hypergate_terror_inside
		npcs.put(3025237524348928L, new Npc(NpcType.boss_raid, "TFB")); // The Terror From Beyond, boss.hypergate_terror.hypergate_terror_inside, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.hypergate_terror.hypergate_terror_inside
		npcs.put(3025241819316224L, new Npc(NpcType.boss_raid, "TFB")); // The Terror From Beyond, boss.hypergate_terror.hypergate_terror_inside, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.hypergate_terror.hypergate_terror_inside
		npcs.put(2988545618739200L, new Npc(NpcType.boss_4, "Tentacle")); // Grasping Tentacle, boss.hypergate_terror.tentacle_inside, npc.qtr.1x4.raid.asation.enemy.difficulty_1.boss.hypergate_terror.tentacle_inside
		npcs.put(3025284768989184L, new Npc(NpcType.boss_4, "Tentacle")); // Grasping Tentacle, boss.hypergate_terror.tentacle_inside, npc.qtr.1x4.raid.asation.enemy.difficulty_2.boss.hypergate_terror.tentacle_inside
		npcs.put(3025289063956480L, new Npc(NpcType.boss_4, "Tentacle")); // Grasping Tentacle, boss.hypergate_terror.tentacle_inside, npc.qtr.1x4.raid.asation.enemy.difficulty_3.boss.hypergate_terror.tentacle_inside
		npcs.put(3025293358923776L, new Npc(NpcType.boss_4, "Tentacle")); // Grasping Tentacle, boss.hypergate_terror.tentacle_inside, npc.qtr.1x4.raid.asation.enemy.difficulty_4.boss.hypergate_terror.tentacle_inside

		npcs.put(3057806261354496L, new Npc(NpcType.boss_raid)); // Dreadful Entity, secret_cave, npc.qtr.1x4.raid.asation.enemy.difficulty_1.trash.secret_cave
	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {
		if (c.getBoss() == null) {
			return null;
		}

		switch (c.getBoss().getRaidBossName()) {
			case TerrorFromBeyond:
				return getNewPhaseNameForTFB(e, currentPhaseName);
			default:
				return null;
		}
	}

	private String getNewPhaseNameForTFB(final Event e, final String currentPhaseName) {

		if (!Helpers.isTargetThisPlayer(e)) {
			return null;
		}

		// ------------------ Timers ------------------

		if (Helpers.isEffectEqual(e, 3025864589574402L) || Helpers.isEffectEqual(e, 3025873179508994L)) { // Near Tentacle (8m / 16m)
			if (!phaseTimers.containsKey("FirstSlam") || e.getTimestamp() - phaseTimers.get("FirstSlam") > 12000) { // no nearby yet or longer than 12 seconds ago
				phaseTimers.put("FirstSlam", e.getTimestamp());

				TimerManager.stopTimer(TFBFirstSlamTimer.class);
				TimerManager.stopTimer(TFBSlamTimer.class);
				TimerManager.startTimer(TFBFirstSlamTimer.class, e.getTimestamp());
			}
		}

		if (Helpers.isAbilityEqual(e, 3025877474476032L) || Helpers.isAbilityEqual(e, 3025886064410624L)) { // Tentacle Slam (8m / 16m)
			if (!phaseTimers.containsKey("Slam") || e.getTimestamp() - phaseTimers.get("Slam") > 8000) { // no slam yet or longer than 8 seconds ago
				phaseTimers.put("Slam", e.getTimestamp());

				TimerManager.stopTimer(TFBFirstSlamTimer.class);
				TimerManager.stopTimer(TFBSlamTimer.class);
				TimerManager.startTimer(TFBSlamTimer.class, e.getTimestamp());
			}
		}
		// ------------------ Tentacles ------------------

		if (currentPhaseName == null) {
			return TFB_PHASE_TENTACLES;
		}

		// ------------------ Beyond ------------------

		if (!TFB_PHASE_BEYOND.equals(currentPhaseName) && (Helpers.isAbilityEqual(e, 3009973210578944L))) {
			// Pulled into the Beyond
			TimerManager.stopTimer(TFBSlamTimer.class);
			return TFB_PHASE_BEYOND;
		}

		return null;
	}

	public static class TFBSlamTimer extends BaseTimer {
		public TFBSlamTimer() {
			super("Slam", "Terror Slam", 10000);
			setColor(0);
		}
	}

	public static class TFBFirstSlamTimer extends BaseTimer {
		public TFBFirstSlamTimer() {
			super("Slam", "Terror First Slam (trigger on nearby)", 14000);
			setColor(0);
		}
	}
}