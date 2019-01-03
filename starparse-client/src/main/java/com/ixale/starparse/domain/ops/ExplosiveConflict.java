package com.ixale.starparse.domain.ops;

import java.util.Arrays;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatChallenge;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBoss.BossUpgradeCallback;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.RaidChallengeName;

public class ExplosiveConflict extends Raid {

	public static final long KEPHESS_WALKER_SM_8M = 2794185463693312L,
		KEPHESS_WALKER_SM_16M = 2876562936430592L,
		KEPHESS_WALKER_HM_8M = 2876532871659520L,
		KEPHESS_WALKER_HM_16M = 2876593001201664L;

//	public static final String KEPHESS_PHASE_DROIDS = "Siege Droids",
//		KEPHESS_PHASE_WALKER = "Walker",
//		KEPHESS_PHASE_TRANDOSHANS = "Trandoshans",
//		KEPHESS_PHASE_PULSAR = "Pulsar Droids",
//		KEPHESS_PHASE_KEPHESS = "Kephess",
//		KEPHESS_PHASE_BURN = "Burn";

	public ExplosiveConflict() {
		super("Explosive Conflict");

		RaidBoss.add(this, RaidBossName.ZornAndToth,
			new long[]{2788331423268864L, 2788335718236160L}, // SM 8m
			new long[]{2860770341683200L, 2860766046715904L}, // SM 16m
			new long[]{2857544821243904L, 2857549116211200L}, // HM 8m
			new long[]{2861388816973824L, 2861384522006528L}, // HM 16m
			new BossUpgradeCallback() {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
					if (guid == 3004561551785984L || guid == 3004587321589760L) { // Baradium Poisoning 8m / 16m - size already set anyway
						return nimBoss;
					}
					return null;
				}
				@Override
				public RaidBoss upgradeByNpc(long guid, RaidBoss boss) {
					return null;
				}
			});

		RaidBoss.add(this, RaidBossName.FirebrandAndStormcaller,
			new long[]{2808827007205376L, 2808831302172672L},
			new long[]{2876459857215488L, 2876464152182784L},
			new long[]{2876434087411712L, 2876438382379008L},
			new long[]{2876481332051968L, 2876485627019264L},
			null);

		RaidBoss.add(this, RaidBossName.ColonelVorgath,
			new long[]{2783478110224384L, 2813182104043520L}, // Demolitions Probe, Vorgath
			new long[]{2854156092047360L, 2854224811524096L},
			new long[]{2854151797080064L, 2854117437341696L},
			new long[]{2854160387014656L, 2854229106491392L},
			null);

		RaidBoss.add(this, RaidBossName.WarlordKephess,
			new long[]{2802491930443776L, KEPHESS_WALKER_SM_8M, 2800357331697664L}, // Siege Droid, Battlewalker, Kephess
			new long[]{2876545756561408L, KEPHESS_WALKER_SM_16M, 2876550051528704L},
			new long[]{2876515691790336L, KEPHESS_WALKER_HM_8M, 2876528576692224L},
			new long[]{2876575821332480L, KEPHESS_WALKER_HM_16M, 2876588706234368L},
			new BossUpgradeCallback() {

				@Override
				public RaidBoss upgradeByNpc(long guid, RaidBoss boss) {
					return null;
				}

				@Override
				public RaidBoss upgradeByAbility(long guid, long effectGuid, Integer value, RaidBoss nimBoss) {
					if (guid == 3006764870008832L) { // Nightmare of the Masters wipe
						return nimBoss;
					}
					return null;
				}
			});

		addChallenge(RaidBossName.WarlordKephess, new KephessWalkerChallenge());
		addChallenge(RaidBossName.FirebrandAndStormcaller, new TanksShieldChallenge());
	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {

		if (c.getBoss() == null) {
			return null;
		}

		switch (c.getBoss().getRaidBossName()) {
			case WarlordKephess:
				return getNewPhaseNameForKephess(e, c, currentPhaseName);
			case FirebrandAndStormcaller:
				if (!Raid.Mode.SM.equals(c.getBoss().getMode())) {
					return getNewPhaseNameForTanks(e, c, currentPhaseName);
				}
			default:
				return null;
		}
	}

	private String getNewPhaseNameForKephess(final Event e, final Combat c, final String currentPhaseName) {

//		if (KEPHESS_PHASE_BURN.equals(currentPhaseName)) {
//			// nothing else
//			return null;
//		}
//
//		if (!KEPHESS_PHASE_PULSAR.equals(currentPhaseName)
//			&& (Helpers.isTargetWithin(e, 2810295886020608L, 2876554346496000L, 2876519986757632L, 2876580116299776L))) { // Pulsar Droids
//
//			return KEPHESS_PHASE_PULSAR;
//		}
//
//		if (!KEPHESS_PHASE_TRANDOSHANS.equals(currentPhaseName)
//			&& (Helpers.isTargetWithin(e, 2809247914000384L, 2876537166626816L, 2876507101855744L, 2876567231397888L))) { // Trenchcutters
//
//			return KEPHESS_PHASE_TRANDOSHANS;
//		}
//
//		if (!KEPHESS_PHASE_WALKER.equals(currentPhaseName)
//			&& (Helpers.isTargetWithin(e, KEPHESS_WALKER_SM_8M, KEPHESS_WALKER_SM_16M, KEPHESS_WALKER_HM_8M, KEPHESS_WALKER_HM_16M))
//			&& e.getValue() != null && e.getValue() > 0) { 
//			// Walker burn
//			System.out.println("Walker: " + e);
//
//			return KEPHESS_PHASE_WALKER;
//		}
//
		if (currentPhaseName == null) {
			phaseTimers.clear();

			return RaidBossName.WarlordKephess.getFullName(); //KEPHESS_PHASE_DROIDS;
		}

		return null;
	}

	private String getNewPhaseNameForTanks(final Event e, final Combat c, final String currentPhaseName) {

		if (currentPhaseName == null) {
			phaseTimers.clear();

			return RaidBossName.FirebrandAndStormcaller.getFullName(); //KEPHESS_PHASE_DROIDS;
		}

		return null;
	}

	public static class KephessWalkerChallenge extends CombatChallenge {
		public KephessWalkerChallenge() {
			super(RaidChallengeName.KephessWalker,
				RaidBossName.WarlordKephess.getFullName()/* KEPHESS_PHASE_WALKER */, CombatChallenge.Type.DAMAGE,
				Arrays.asList(new Object[]{KEPHESS_WALKER_SM_8M, KEPHESS_WALKER_SM_16M, KEPHESS_WALKER_HM_8M, KEPHESS_WALKER_HM_16M}),
				"target_guid IN (?, ?, ?, ?)");
		}
	}

	public static class TanksShieldChallenge extends CombatChallenge {
		public TanksShieldChallenge() {
			super(RaidChallengeName.TanksShield,
				RaidBossName.FirebrandAndStormcaller.getFullName(), CombatChallenge.Type.FRIENDLY,
				Arrays.asList(new Object[]{
						2876425497477120L, 2876429792444416L, 2876477037084672L, 2876489921986560L, // 8/16 shields
						2876678900547584L, 2923889181065216L, 2923897770999808L, 2876885058977792L}), // 8/16 volley (+ ultimate)
				"target_guid IN (?, ?, ?, ?) AND ability_guid NOT IN (?, ?, ?, ?)");
		}
	}
}
