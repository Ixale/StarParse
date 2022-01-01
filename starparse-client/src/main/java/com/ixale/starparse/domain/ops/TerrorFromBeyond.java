package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Event;
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
		
		// ------------------ Timers ------------------
		
		if (Helpers.isEffectEqual(e, 3025864589574402L) || Helpers.isEffectEqual(e, 3025873179508994L)) {		// Near Tentacle (8m / 16m)
			TimerManager.startTimer(TFBFirstSlamTimer.class, e.getTimestamp());
		}
		
		if (Helpers.isAbilityEqual(e, 3025877474476032L) || Helpers.isAbilityEqual(e, 3025886064410624L)) {		// Tentacle Slam (8m / 16m)
			TimerManager.stopTimer(TFBSlamTimer.class);
			TimerManager.startTimer(TFBSlamTimer.class, e.getTimestamp());
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
