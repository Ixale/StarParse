package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatChallenge;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.NpcType;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBoss.BossUpgradeCallback;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.RaidChallengeName;
import com.ixale.starparse.parser.Helpers;

import java.util.Arrays;

public class DreadFortress extends Raid {

	private static final String PHASE_BRONTES_HANDS = "Hands",
			PHASE_BRONTES_KEPHESS = "Kephess",
			PHASE_BRONTES_DROIDS = "Droids",
			PHASE_BRONTES_FINGERS = "Fingers",
			PHASE_BRONTES_BURN = "Brontes Burn";

	private static final long BRONTES_BOSS_SM_8M = 3273937605623808L,
			BRONTES_BOSS_SM_16M = 3303538520227840L,
			BRONTES_BOSS_HM_8M = 3303529930293248L,
			BRONTES_BOSS_HM_16M = 3303547110162432L;

	public DreadFortress() {
		super("Dread Fortress");

		RaidBoss.add(this, RaidBossName.Nefra,
				new long[]{3266533082005504L}, // SM 8m
				new long[]{3303036009054208L}, // SM 16m
				new long[]{3303031714086912L}, // HM 8m
				new long[]{3303040304021504L}, // HM 16m
				new BossUpgradeCallback() {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
						if (guid == 3342167456088064L || guid == 3342034312101888L) { // (Nightmare) Twin Attack
							return nimBoss;
						}
						return null;
					}

					@Override
					public RaidBoss upgradeByNpc(long guid, RaidBoss boss) {
						return null;
					}
				});

		RaidBoss.add(this, RaidBossName.Draxus,
				new long[]{3273924720721920L},
				new long[]{3303401081274368L,},
				new long[]{3303392491339776L},
				new long[]{3303405376241664L},
				new BossUpgradeCallback() {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
						if (guid == 3381651090440192L) { // Slam
							return nimBoss;
						}
						return null;
					}

					@Override
					public RaidBoss upgradeByNpc(long guid, RaidBoss boss) {
						return null;
					}
				});

		RaidBoss.add(this, RaidBossName.Grobthok,
				new long[]{3273929015689216L},
				new long[]{3302563562651648L},
				new long[]{3302559267684352L},
				new long[]{3302567857618944L},
				null); // Dreadful Ugnaught {3307678868701184}:3251006092376] (1741* internal {836045448940876} (1741 absorbed {836045448945511})) <1741>

		RaidBoss.add(this, RaidBossName.CorruptorZero,
				new long[]{3273933310656512L},
				new long[]{3303542815195136L},
				new long[]{3303534225260544L},
				new long[]{3303551405129728L},
				new BossUpgradeCallback() {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
						if (guid == 3344271990063104L || guid == 3367035316731904L) { // Concussion Mine
							return nimBoss;
						}
						return null;
					}

					@Override
					public RaidBoss upgradeByNpc(long guid, RaidBoss boss) {
						return null;
					}
				});

		final RaidBoss br16n = new RaidBoss(this, RaidBossName.Brontes, Mode.NiM, Size.Sixteen, null);
		final RaidBoss br8n = new RaidBoss(this, RaidBossName.Brontes, Mode.NiM, Size.Eight, null);

		final RaidBoss br16h = new RaidBoss(this, RaidBossName.Brontes, Mode.HM, Size.Sixteen,
				null,
				new long[]{BRONTES_BOSS_HM_16M}, // Brontes 16HM = tentative
				new RaidBoss.BossUpgrade(new RaidBoss[]{br16n}) {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
						if ((guid == 3347252697366528L && effectGuid == 3347252697366794L) // Bad Touch
								|| guid == 3346827495604224L) { // Static Field
							return br16n;
						}
						return super.upgradeByAbility(guid, effectGuid, value);
					}
				});

		final RaidBoss br8h = new RaidBoss(this, RaidBossName.Brontes, Mode.HM, Size.Eight,
				null,
				new long[]{BRONTES_BOSS_HM_8M}, // Brontes 8HM = tentative
				new RaidBoss.BossUpgrade(new RaidBoss[]{br8n}) {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
						if ((guid == 3347252697366528L && effectGuid == 3347252697366794L) // Bad Touch
								|| guid == 3346827495604224L) { // Static Field
							return br8n;
						}
						return super.upgradeByAbility(guid, effectGuid, value);
					}
				});

		final RaidBoss br16s = new RaidBoss(this, RaidBossName.Brontes, Mode.SM, Size.Sixteen,
				new long[]{BRONTES_BOSS_SM_16M}); // Brontes 16SM = final

		final RaidBoss br8s = new RaidBoss(this, RaidBossName.Brontes, Mode.SM, Size.Eight,
				new long[]{BRONTES_BOSS_SM_8M}, // Brontes 8SM = final
				new long[]{3275625527771136L, 3277721471811584L}, // Hands
				new RaidBoss.BossUpgrade(new RaidBoss[]{br16s, br8h, br16h, br8n, br16n}));

		bosses.add(br8s);
		bosses.add(br16s);
		bosses.add(br8h);
		bosses.add(br16h);
		bosses.add(br8n);
		bosses.add(br16n);

		addChallenge(RaidBossName.Brontes, new BrontesBurnChallenge());

		npcs.put(3266533082005504L, new Npc(NpcType.boss_raid, "Nefra")); // boss_gatekeeper	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gatekeeper.boss_gatekeeper
		npcs.put(3303031714086912L, new Npc(NpcType.boss_raid, "Nefra")); // boss_gatekeeper	npc.operation.oricon.fortress.enemy.difficulty_2.boss.gatekeeper.boss_gatekeeper
		npcs.put(3303036009054208L, new Npc(NpcType.boss_raid, "Nefra")); // boss_gatekeeper	npc.operation.oricon.fortress.enemy.difficulty_3.boss.gatekeeper.boss_gatekeeper
		npcs.put(3303040304021504L, new Npc(NpcType.boss_raid, "Nefra")); // boss_gatekeeper	npc.operation.oricon.fortress.enemy.difficulty_4.boss.gatekeeper.boss_gatekeeper

		npcs.put(3273924720721920L, new Npc(NpcType.boss_raid, "Draxus")); // boss_gatemaster	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.boss_gatemaster
		npcs.put(3303392491339776L, new Npc(NpcType.boss_raid, "Draxus")); // boss_gatemaster	npc.operation.oricon.fortress.enemy.difficulty_2.boss.gate_puzzle.boss_gatemaster
		npcs.put(3303401081274368L, new Npc(NpcType.boss_raid, "Draxus")); // boss_gatemaster	npc.operation.oricon.fortress.enemy.difficulty_3.boss.gate_puzzle.boss_gatemaster
		npcs.put(3303405376241664L, new Npc(NpcType.boss_raid, "Draxus")); // boss_gatemaster	npc.operation.oricon.fortress.enemy.difficulty_4.boss.gate_puzzle.boss_gatemaster
		npcs.put(3317793516683264L, new Npc(NpcType.boss_1)); // boss	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.wave_mini_boss
		npcs.put(3272597575827456L, new Npc(NpcType.boss_1)); // boss	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.dreadguard_mini_boss
		npcs.put(3281333539307520L, new Npc(NpcType.boss_2)); // boss	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.wave_boss

//		npcs.put(3272601870794752L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.dreadguard_add	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.dreadguard_add
//		npcs.put(3270364192833536L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.dreadguard_healer	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.dreadguard_healer
//		npcs.put(3272541741252608L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.dreadguard_tank	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.dreadguard_tank
//		npcs.put(3288136767504384L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.wave_aoe	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.wave_aoe
//		npcs.put(3281329244340224L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.wave_elite	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.wave_elite
//		npcs.put(3288029393321984L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.wave_elite_2	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.wave_elite_2
//		npcs.put(3288141062471680L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.wave_nuke	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.wave_nuke
//		npcs.put(3288145357438976L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.wave_shield	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.wave_shield
//		npcs.put(3281320654405632L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.wave_standard	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.wave_standard
//		npcs.put(3281324949372928L, new Npc(NpcToughness.boss_1)); // boss.gate_puzzle.wave_strong	npc.operation.oricon.fortress.enemy.difficulty_1.boss.gate_puzzle.wave_strong


		npcs.put(3302567857618944L, new Npc(NpcType.boss_raid)); // boss_mecha_rancor	npc.operation.oricon.fortress.enemy.difficulty_4.boss.mecha_rancor.boss_mecha_rancor
		npcs.put(3273929015689216L, new Npc(NpcType.boss_raid)); // boss_mecha_rancor	npc.operation.oricon.fortress.enemy.difficulty_1.boss.mecha_rancor.boss_mecha_rancor
		npcs.put(3302563562651648L, new Npc(NpcType.boss_raid)); // boss_mecha_rancor	npc.operation.oricon.fortress.enemy.difficulty_3.boss.mecha_rancor.boss_mecha_rancor
		npcs.put(3302559267684352L, new Npc(NpcType.boss_raid)); // boss_mecha_rancor	npc.operation.oricon.fortress.enemy.difficulty_2.boss.mecha_rancor.boss_mecha_rancor

		npcs.put(3273933310656512L, new Npc(NpcType.boss_raid)); // boss_droid_general	npc.operation.oricon.fortress.enemy.difficulty_1.boss.droid_general.boss_droid_general
		npcs.put(3303551405129728L, new Npc(NpcType.boss_raid)); // boss_droid_general	npc.operation.oricon.fortress.enemy.difficulty_4.boss.droid_general.boss_droid_general
		npcs.put(3303534225260544L, new Npc(NpcType.boss_raid)); // boss_droid_general	npc.operation.oricon.fortress.enemy.difficulty_2.boss.droid_general.boss_droid_general
		npcs.put(3303542815195136L, new Npc(NpcType.boss_raid)); // boss_droid_general	npc.operation.oricon.fortress.enemy.difficulty_3.boss.droid_general.boss_droid_general

		npcs.put(3309040373334016L, new Npc(NpcType.boss_4)); // boss.dread_master_brontes.corrupted_clone	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.corrupted_clone
		npcs.put(3275625527771136L, new Npc(NpcType.boss_raid)); // boss.dread_master_brontes.tentacles.large_tentacle	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.large_tentacle
		npcs.put(3277721471811584L, new Npc(NpcType.boss_raid)); // boss.dread_master_brontes.tentacles.large_tentacle_b	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.large_tentacle_b
		npcs.put(3303276527222784L, new Npc(NpcType.boss_raid)); // boss.dread_master_brontes.tentacles.phase5_large_tentacle	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.phase5_large_tentacle
//		npcs.put(3303289412124672L, new Npc(NpcType.boss_1)); // boss.dread_master_brontes.tentacles.phase5_small_tentacle	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.phase5_small_tentacle
		npcs.put(3314013945462784L, new Npc(NpcType.boss_raid)); // boss.dread_master_brontes.tentacles.phase6_large_tentacle	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.phase6_large_tentacle
		npcs.put(3314958838267904L, new Npc(NpcType.boss_raid)); // boss.dread_master_brontes.tentacles.phase6_large_tentacle_b	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.phase6_large_tentacle_b
		npcs.put(3295399557201920L, new Npc(NpcType.boss_2)); // boss.dread_master_brontes.tentacles.shield_tentacle	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.shield_tentacle
//		npcs.put(3275642707640320L, new Npc(NpcType.boss_1)); // boss.dread_master_brontes.tentacles.small_tentacle	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.small_tentacle
//		npcs.put(3315362565193728L, new Npc(NpcType.boss_1)); // boss.dread_master_brontes.tentacles.small_tentacle_16m	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.small_tentacle_16m
		npcs.put(3297946472808448L, new Npc(NpcType.boss_raid)); // boss.dread_master_brontes.tentacles.targeted_cone_tentacle	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.tentacles.targeted_cone_tentacle

		npcs.put(3273937605623808L, new Npc(NpcType.boss_raid, "Brontes")); // boss.dread_master_brontes.dread_master_brontes	npc.operation.oricon.fortress.enemy.difficulty_1.boss.dread_master_brontes.dread_master_brontes
		npcs.put(3303529930293248L, new Npc(NpcType.boss_raid, "Brontes")); // boss.dread_master_brontes.dread_master_brontes	npc.operation.oricon.fortress.enemy.difficulty_2.boss.dread_master_brontes.dread_master_brontes
		npcs.put(3303538520227840L, new Npc(NpcType.boss_raid, "Brontes")); // boss.dread_master_brontes.dread_master_brontes	npc.operation.oricon.fortress.enemy.difficulty_3.boss.dread_master_brontes.dread_master_brontes
		npcs.put(3303547110162432L, new Npc(NpcType.boss_raid, "Brontes")); // boss.dread_master_brontes.dread_master_brontes	npc.operation.oricon.fortress.enemy.difficulty_4.boss.dread_master_brontes.dread_master_brontes

	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {
		switch (c.getBoss().getRaidBossName()) {
			case Brontes:
				return getNewPhaseNameForBrontes(e, currentPhaseName);
			default:
				return null;
		}
	}

	private String getNewPhaseNameForBrontes(final Event e, final String currentPhaseName) {

		if (PHASE_BRONTES_BURN.equals(currentPhaseName)) {
			return null; // last phase
		}

		if (PHASE_BRONTES_FINGERS.equals(currentPhaseName)
				&& Helpers.isEffectDamage(e)
				&& (Helpers.isTargetWithin(e, BRONTES_BOSS_SM_8M, BRONTES_BOSS_SM_16M, BRONTES_BOSS_HM_8M, BRONTES_BOSS_HM_16M)
				|| ((Helpers.isSourceWithin(e, BRONTES_BOSS_SM_8M, BRONTES_BOSS_SM_16M, BRONTES_BOSS_HM_8M, BRONTES_BOSS_HM_16M))
				&& !Helpers.isAbilityEqual(e, 3303641599442944L))) // Spike of Pain
				&& (e.getValue() != null && e.getValue() > 0)) {
			// first hit from/to Brontes in the last phase (and not Spike of Pain circle in Fingers phase)
			return PHASE_BRONTES_BURN;
		}

		if (Helpers.isAbilityEqual(e, 3309164927385870L)) {
			// Manifestation stack (actually -5 second)
			return PHASE_BRONTES_BURN;
		}

		if (!PHASE_BRONTES_FINGERS.equals(currentPhaseName)
				&& (Helpers.isTargetWithin(e, 3303289412124672L, 3303276527222784L)
				|| Helpers.isSourceWithin(e, 3303289412124672L, 3303276527222784L)) // Finder, Hand
				&& Helpers.isSourceOrTargetAnyPlayer(e)) {
			// Finger or Hand shooting or taking damage
			return PHASE_BRONTES_FINGERS;
		}

		if (!PHASE_BRONTES_DROIDS.equals(currentPhaseName)
				&& (Helpers.isTargetEqual(e, 3300910000242688L) || Helpers.isSourceEqual(e, 3300910000242688L)) // Unshielded D-09 Droid
				&& Helpers.isSourceOrTargetAnyPlayer(e)) {
			// Droids shooting or taking damage
			return PHASE_BRONTES_DROIDS;
		}

		if (PHASE_BRONTES_HANDS.equals(currentPhaseName)
				&& (Helpers.isTargetOrSourceWithin(e, 3309040373334016L)) // Kephess
				&& Helpers.isSourceOrTargetAnyPlayer(e)
				&& e.getValue() != null) {
			// Kephess appears
			return PHASE_BRONTES_KEPHESS;
		}

		if (currentPhaseName == null) {
			return PHASE_BRONTES_HANDS;
		}

		return null;
	}

	public static class BrontesBurnChallenge extends CombatChallenge {
		public BrontesBurnChallenge() {
			super(RaidChallengeName.BrontesBurn,
					PHASE_BRONTES_BURN, CombatChallenge.Type.DAMAGE,
					Arrays.asList(
							BRONTES_BOSS_SM_8M, BRONTES_BOSS_SM_16M, BRONTES_BOSS_HM_8M, BRONTES_BOSS_HM_16M,
							3314958838267904L, 3314013945462784L)); // Hands BR+TL
		}
	}
}
