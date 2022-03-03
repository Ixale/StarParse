package com.ixale.starparse.domain.ops;

import java.util.Arrays;

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
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;

public class ScumAndVillainy extends Raid {

	private static final long STYRAK_KELL_DRAGON = 3067057620910080L,
			STYRAK_CLONE_KNOCKBACK = 3294742427205632L,
			STYRAK_MANIFESTATION = 3147154466013184L;

	public ScumAndVillainy() {
		super("Scum & Villainy");

		RaidBoss.add(this, RaidBossName.DashRoode,
				new long[]{3058837053505536L}, // SM 8m
				new long[]{3153571147153408L}, // SM 16m
				new long[]{3153558262251520L}, // HM 8m
				new long[]{3153575442120704L}, // HM 16m
				null); // not used right now

		RaidBoss.add(this, RaidBossName.Titan6,
				new long[]{3016450021261312L},
				new long[]{3152463045591040L},
				new long[]{3152458750623744L},
				new long[]{3152467340558336L},
				new BossUpgradeCallback() {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
						if (guid == 3261684063928320L) { // Missile Bursts (NiM only) - shared
							return nimBoss;
						}
						return null;
					}

					@Override
					public RaidBoss upgradeByNpc(long guid, RaidBoss boss) {
						return null;
					}
				});

		RaidBoss.add(this, RaidBossName.Thrasher,
				new long[]{3045819007631360L},
				new long[]{3154567579566080L},
				new long[]{3154563284598784L},
				new long[]{3154571874533376L},
				new BossUpgradeCallback() {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
						if (guid == 3257350441926656L) { // Insane Roar (kb 50%) - shared
							return nimBoss;
						}
						return null;
					}

					@Override
					public RaidBoss upgradeByNpc(long guid, RaidBoss boss) {
						return null;
					}
				});

		RaidBoss.add(this, RaidBossName.OperationsChief,
				new long[]{3141940375715840L},
				new long[]{3157552581836800L},
				new long[]{3157548286869504L},
				new long[]{3157556876804096L},
				null);
		/*
		 * 3131391936036864L, 3131387641069568L, // red
		 * 3131396231004160L, 3131400525971456L, // green
		 * 3131404820938752L, 3131409115906048L, // gold
		 * 3131211547410432L, 3131383346102272L, // blue
		 */

		RaidBoss.add(this, RaidBossName.OlokTheShadow,
				new long[]{3016445726294016L},
				new long[]{3154674953748480L},
				new long[]{3154662068846592L},
				new long[]{3154679248715776L},
				new BossUpgradeCallback() {
					@Override
					public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
						return null;
					}

					@Override
					public RaidBoss upgradeByNpc(long guid, RaidBoss nimBoss) {
						if (guid == 3256920945197056L || guid == 3256933830098944L || guid == 3256946715000832L || guid == 3256951009968128L) { // droids (hostile)
							return nimBoss;
						}
						return null;
					}
				});

		final RaidBoss cw16n = new RaidBoss(this, RaidBossName.CartelWarlords, Mode.NiM, Size.Sixteen, null);
		final RaidBoss cw8n = new RaidBoss(this, RaidBossName.CartelWarlords, Mode.NiM, Size.Eight, null);

		final RaidBoss cw16h = new RaidBoss(this, RaidBossName.CartelWarlords, Mode.HM, Size.Sixteen,
				null,
				new long[]{3156904041775104L}, // Tuchuk 16HM = tentative
				new RaidBoss.BossUpgrade(new RaidBoss[]{cw16n}));

		final RaidBoss cw8h = new RaidBoss(this, RaidBossName.CartelWarlords, Mode.HM, Size.Eight,
				null,
				new long[]{3156895451840512L}, // Tuchuk 8HM = tentative
				new RaidBoss.BossUpgrade(new RaidBoss[]{cw8n}));

		final RaidBoss cw16s = new RaidBoss(this, RaidBossName.CartelWarlords, Mode.SM, Size.Sixteen,
				new long[]{3156899746807808L}); // Tuchuk 16SM = final

		final RaidBoss cw8s = new RaidBoss(this, RaidBossName.CartelWarlords, Mode.SM, Size.Eight,
				new long[]{3054413237190656L}, // Tuchuk 8SM = final
				new long[]{3054400352288768L, 3054404647256064L, 3054408942223360L}, // Horric, Sunder, Vilus
				new RaidBoss.BossUpgrade(new RaidBoss[]{cw16s, cw8h, cw16h, cw8n, cw16n}));

		bosses.add(cw8s);
		bosses.add(cw16s);
		bosses.add(cw8h);
		bosses.add(cw16h);
		bosses.add(cw8n);
		bosses.add(cw16n);

		// 3283107360800768 Destroy Armor

		final RaidBoss ds16n = new RaidBoss(this, RaidBossName.Styrak, Mode.NiM, Size.Sixteen, null);
		final RaidBoss ds8n = new RaidBoss(this, RaidBossName.Styrak, Mode.NiM, Size.Eight, null);

		final RaidBoss ds16h = new RaidBoss(this, RaidBossName.Styrak, Mode.HM, Size.Sixteen,
				null,
				new long[]{3152445865721856L}, // Styrak 16HM = tentative
				new RaidBoss.BossUpgrade(new RaidBoss[]{ds16n}) {
					@Override
					public RaidBoss upgradeByNpc(long npcGuid) {
						if (npcGuid == STYRAK_CLONE_KNOCKBACK) { // Knockback
							return ds16n;
						}
						return super.upgradeByNpc(npcGuid);
					}
				});

		final RaidBoss ds8h = new RaidBoss(this, RaidBossName.Styrak, Mode.HM, Size.Eight,
				null,
				new long[]{3152407211016192L}, // Styrak 8HM = tentative
				new RaidBoss.BossUpgrade(new RaidBoss[]{ds8n}) {
					@Override
					public RaidBoss upgradeByNpc(long npcGuid) {
						if (npcGuid == STYRAK_CLONE_KNOCKBACK) { // Knockback
							return ds8n;
						}
						return super.upgradeByNpc(npcGuid);
					}
				});

		final RaidBoss ds16s = new RaidBoss(this, RaidBossName.Styrak, Mode.SM, Size.Sixteen,
				new long[]{3152441570754560L}); // Styrak 16SM = final

		final RaidBoss ds8s = new RaidBoss(this, RaidBossName.Styrak, Mode.SM, Size.Eight,
				new long[]{3066945951760384L}, // Styrak 8SM = final
				new long[]{STYRAK_KELL_DRAGON, 3225679353085952L}, // Kell Dragon = tentative
				new RaidBoss.BossUpgrade(new RaidBoss[]{ds16s, ds8h, ds16h, ds8n, ds16n}) {
					@Override
					public RaidBoss upgradeByNpc(long npcGuid) {
						if (npcGuid == STYRAK_CLONE_KNOCKBACK) { // Knockback
							return ds8n; // ... or maybe ds16n, who knows :'(
						}
						return super.upgradeByNpc(npcGuid);
					}
				});

		bosses.add(ds8s);
		bosses.add(ds16s);
		bosses.add(ds8h);
		bosses.add(ds16h);
		bosses.add(ds8n);
		bosses.add(ds16n);

		bosses.add(new RaidBoss(this, RaidBossName.HatefulEntity, Mode.NiM, Size.Sixteen,
				new long[]{3264737785675776L}));

		addChallenge(RaidBossName.Styrak, new StyrakManifestationChallenge());

		// NPCs
		npcs.put(3058837053505536L, new Npc(NpcType.boss_raid)); // Dash'Roode, darvannis, raidEncounter
		npcs.put(3153575442120704L, new Npc(NpcType.boss_raid)); // Dash'Roode, darvannis, raidEncounter
		npcs.put(3153571147153408L, new Npc(NpcType.boss_raid)); // Dash'Roode, darvannis, raidEncounter
		npcs.put(3153558262251520L, new Npc(NpcType.boss_raid)); // Dash'Roode, darvannis, raidEncounter

		npcs.put(3016450021261312L, new Npc(NpcType.boss_raid)); // Titan 6, darvannis, raidEncounter
		npcs.put(3152458750623744L, new Npc(NpcType.boss_raid)); // Titan 6, darvannis, raidEncounter
		npcs.put(3152463045591040L, new Npc(NpcType.boss_raid)); // Titan 6, darvannis, raidEncounter
		npcs.put(3152467340558336L, new Npc(NpcType.boss_raid)); // Titan 6, darvannis, raidEncounter

		npcs.put(3045819007631360L, new Npc(NpcType.boss_raid)); // Thrasher, darvannis, raidEncounter
		npcs.put(3154567579566080L, new Npc(NpcType.boss_raid)); // Thrasher, darvannis, raidEncounter
		npcs.put(3154563284598784L, new Npc(NpcType.boss_raid)); // Thrasher, darvannis, raidEncounter
		npcs.put(3154571874533376L, new Npc(NpcType.boss_raid)); // Thrasher, darvannis, raidEncounter
		npcs.put(3062092638715904L, new Npc(NpcType.boss_1)); // Mercenary Demolitionist, darvannis, raidEncounter
//		npcs.put(3048576376635392L, new Npc(NpcType.boss_1)); // Mercenary Sniper, darvannis, raidEncounter

		npcs.put(3016445726294016L, new Npc(NpcType.boss_raid)); // Olok the Shadow, darvannis, raidEncounter
		npcs.put(3050663730741248L, new Npc(NpcType.boss_raid)); // Olok the Shadow, darvannis, raidEncounter
		npcs.put(3071365473107968L, new Npc(NpcType.boss_raid)); // Olok the Shadow, darvannis, raidEncounter
		npcs.put(3154662068846592L, new Npc(NpcType.boss_raid)); // Olok the Shadow, darvannis, raidEncounter
		npcs.put(3154674953748480L, new Npc(NpcType.boss_raid)); // Olok the Shadow, darvannis, raidEncounter
		npcs.put(3154679248715776L, new Npc(NpcType.boss_raid)); // Olok the Shadow, darvannis, raidEncounter
		npcs.put(3235506238259200L, new Npc(NpcType.boss_1)); // Shady Customer, darvannis, raidEncounter
		npcs.put(3067130635354112L, new Npc(NpcType.boss_2)); // Wealthy Buyer, darvannis, raidEncounter
		npcs.put(3235501943291904L, new Npc(NpcType.boss_1)); // Underworld Arms Trader, darvannis, raidEncounter
		npcs.put(3252041862348800L, new Npc(NpcType.boss_2)); // Underworld Arms Trader, darvannis, raidEncounter
		npcs.put(3243219999522816L, new Npc(NpcType.boss_4)); // Accomplished Arms Trader, darvannis, raidEncounter
		npcs.put(3247089765056512L, new Npc(NpcType.boss_4)); // Accomplished Arms Trader, darvannis, raidEncounter
		npcs.put(3247094060023808L, new Npc(NpcType.boss_4)); // Accomplished Arms Trader, darvannis, raidEncounter
		npcs.put(3247098354991104L, new Npc(NpcType.boss_4)); // Accomplished Arms Trader, darvannis, raidEncounter


//		npcs.put(3131211547410432L, new Npc(NpcToughness.boss_1)); // Blue Team Pack Hunter, darvannis, raidTrash
//		npcs.put(3131383346102272L, new Npc(NpcToughness.boss_1)); // Blue Team Pack Hunter, darvannis, raidTrash
//		npcs.put(3131404820938752L, new Npc(NpcToughness.boss_1)); // Gold Team Medtech, darvannis, raidTrash
//		npcs.put(3131409115906048L, new Npc(NpcToughness.boss_1)); // Gold Team Pyroguard, darvannis, raidTrash
//		npcs.put(3131396231004160L, new Npc(NpcToughness.boss_1)); // Green Team Duelist, darvannis, raidTrash
//		npcs.put(3131400525971456L, new Npc(NpcToughness.boss_1)); // Green Team Pyroguard, darvannis, raidTrash
//		npcs.put(3141399209836544L, new Npc(NpcToughness.boss_2)); // Oasis Enforcer Battledroid, darvannis, raidTrash
//		npcs.put(3210586838007808L, new Npc(NpcToughness.boss_4)); // Oasis Reinforcer Battledroid, darvannis, raidTrash
//		npcs.put(3131387641069568L, new Npc(NpcToughness.boss_1)); // Red Team Assault Gunner, darvannis, raidTrash
//		npcs.put(3131391936036864L, new Npc(NpcToughness.boss_1)); // Red Team Flametech, darvannis, raidTrash
		npcs.put(3157556876804096L, new Npc(NpcType.boss_raid)); // Operations Chief, id, raidTrash
		npcs.put(3141940375715840L, new Npc(NpcType.boss_raid)); // Operations Chief, id, raidTrash
		npcs.put(3157548286869504L, new Npc(NpcType.boss_raid)); // Operations Chief, id, raidTrash
		npcs.put(3157552581836800L, new Npc(NpcType.boss_raid)); // Operations Chief, id, raidTrash

		npcs.put(3054408942223360L, new Npc(NpcType.boss_raid)); // Vilus Garr, darvannis, raidEncounter
		npcs.put(3054404647256064L, new Npc(NpcType.boss_raid)); // Sunder, darvannis, raidEncounter
		npcs.put(3054400352288768L, new Npc(NpcType.boss_raid)); // Captain Horic, darvannis, raidEncounter
		npcs.put(3054413237190656L, new Npc(NpcType.boss_raid)); // Tu'chuk, darvannis, raidEncounter
		npcs.put(3156904041775104L, new Npc(NpcType.boss_raid)); // Tu'chuk, darvannis, raidEncounter
		npcs.put(3156899746807808L, new Npc(NpcType.boss_raid)); // Tu'chuk, darvannis, raidEncounter
		npcs.put(3156895451840512L, new Npc(NpcType.boss_raid)); // Tu'chuk, darvannis, raidEncounter

		npcs.put(3147154466013184L, new Npc(NpcType.boss_4, "Manifestation")); // Dread Master Styrak, darvannis, raidEncounter
		npcs.put(3152445865721856L, new Npc(NpcType.boss_raid, "Styrak")); // Dread Master Styrak, darvannis, raidEncounter
		npcs.put(3152441570754560L, new Npc(NpcType.boss_raid, "Styrak")); // Dread Master Styrak, darvannis, raidEncounter
		npcs.put(3152407211016192L, new Npc(NpcType.boss_raid, "Styrak")); // Dread Master Styrak, darvannis, raidEncounter
		npcs.put(3066945951760384L, new Npc(NpcType.boss_raid, "Styrak")); // Dread Master Styrak, darvannis, raidEncounter
		npcs.put(3067057620910080L, new Npc(NpcType.boss_raid)); // Kell Dragon, darvannis, raidEncounter
		npcs.put(3225679353085952L, new Npc(NpcType.boss_raid)); // Kell Dragon, darvannis, raidEncounter

		npcs.put(3241669516328960L, new Npc(NpcType.boss_2)); // Hateful Presence, darvannis, raidTrash
		npcs.put(3264737785675776L, new Npc(NpcType.boss_raid)); // Hateful Entity, darvannis, raidTrash
	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {
		switch (c.getBoss().getRaidBossName()) {
			case Styrak:
				return getNewPhaseNameForStyrak(e, currentPhaseName, c.getBoss());
			default:
				return null;
		}
	}

	private String getNewPhaseNameForStyrak(final Event e, final String currentPhaseName, final RaidBoss boss) {

		if (!"Boss".equals(currentPhaseName) && !"Burn".equals(currentPhaseName) && (
				(
						(Helpers.isAbilityEqual(e, 3218004246528000L) // mass storm
								|| Helpers.isSourceEqual(e, 3142326922772480L) // adds
								|| Helpers.isTargetEqual(e, 3142326922772480L) // adds
						) && Helpers.isSourceOrTargetAnyPlayer(e)
				) || (phaseTimers.containsKey("CM") && phaseTimers.get("CM") < e.getTimestamp() - 35 * 1000)
		)) { // should be dead in 35s anyway (prevents infinite phase for healers)
			phaseTimers.remove("CM");
			return "Boss";
		}

		if (!"CM".equals(currentPhaseName)
				&& (Helpers.isSourceEqual(e, STYRAK_MANIFESTATION) || Helpers.isTargetEqual(e, STYRAK_MANIFESTATION))
				&& Helpers.isSourceOrTargetAnyPlayer(e)) {
			phaseTimers.put("CM", e.getTimestamp());
			return "CM";
		}

		/*
		 * if (!"Adds".equals(currentPhaseName) && (
		 * Helpers.isSourceEqual(e, 3142326922772480L) || Helpers.isTargetEqual(e, 3142326922772480L)
		 * )) {
		 * return "Adds";
		 * }
		 */

		if (!"Burn".equals(currentPhaseName)
				&& (Helpers.isSourceEqual(e, 3225679353085952L) || Helpers.isTargetEqual(e, 3225679353085952L))
				&& Helpers.isSourceOrTargetAnyPlayer(e)) {
			return "Burn";
		}

		final String phaseName;
		if (currentPhaseName == null) {
			phaseTimers.clear();
			phaseName = "Dragon";
		} else {
			phaseName = null;
		}

		if (Mode.NiM.equals(boss.getMode())) {
			if (!phaseTimers.containsKey("kb") && ("Dragon".equals(currentPhaseName) || "Dragon".equals(phaseName))) {

				if (Helpers.isSourceEqual(e, STYRAK_KELL_DRAGON) && Helpers.isAbilityEqual(e, 2995606544973824L)) { // ModifyThreat
					// setup Knockback timer - from the pull
					TimerManager.startTimer(StyrakKnockbackTimer.class, e.getTimestamp());
					phaseTimers.put("kb", 0L);

				} else if (Helpers.isSourceEqual(e, STYRAK_CLONE_KNOCKBACK) && Helpers.isAbilityEqual(e, 3294845506420736L)) { // Overload
					// setup Knockback timer - from the first (?) cast
					TimerManager.startTimer(StyrakKnockbackTimer.class, e.getTimestamp() + 36000 /* bypass first offset */);
					phaseTimers.put("kb", 0L);
				}

			} else if (phaseTimers.containsKey("kb") && !"Dragon".equals(currentPhaseName)) {
				// clear Knockback timer
				TimerManager.stopTimer(StyrakKnockbackTimer.class);
				phaseTimers.remove("kb");
			}
		}

		return phaseName;
	}

	public static class StyrakKnockbackTimer extends BaseTimer {
		public StyrakKnockbackTimer() {
			super("Knockback", "Styrak Knockback", 24 * 1000, 60 * 1000);
			setColor(1);
		}
	}

	public static class StyrakManifestationChallenge extends CombatChallenge {
		public StyrakManifestationChallenge() {
			super(RaidChallengeName.StyrakManifestation,
					"CM", CombatChallenge.Type.DAMAGE,
					Arrays.asList(STYRAK_MANIFESTATION));
		}
	}
}
