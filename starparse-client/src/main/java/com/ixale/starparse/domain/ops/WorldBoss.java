package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.parser.Helpers;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;

public class WorldBoss extends Raid {

	private static final long ANCIENT_THREAT_BOSS = 3511870203887616L,
			COLOSSAL_MONOLITH_SM_8M = 3541140406009856L,
			COLOSSAL_MONOLITH_HM_8M = 3570947479044096L,
			COLOSSAL_MONOLITH_SM_16M = 3570951774011392L,
			COLOSSAL_MONOLITH_HM_16M = 3570956068978688L,
			WORLDBREAKER_MONOLITH_BOSS = 3547338043817984L;

	private static final String QUEEN_PHASE_OPENING = "Opening",
			QUEEN_PHASE_GUARDS_1 = "Guards 1",
			QUEEN_PHASE_CAUSTIC_2 = "Caustic 2",
			QUEEN_PHASE_GUARDS_3 = "Guards 3",
			QUEEN_PHASE_CAUSTIC_4 = "Caustic 4",
			QUEEN_PHASE_GUARDS_5 = "Guards 5";

	public WorldBoss() {
		super("World Boss");

		bosses.add(new RaidBoss(this, RaidBossName.AncientThreat, Mode.HM, Size.Sixteen,
				new long[]{ANCIENT_THREAT_BOSS}));

		RaidBoss.add(this, RaidBossName.ColossalMonolith,
				new long[]{COLOSSAL_MONOLITH_SM_8M}, // SM 8m
				new long[]{COLOSSAL_MONOLITH_SM_16M}, // SM 16m
				new long[]{COLOSSAL_MONOLITH_HM_8M}, // HM 8m
				new long[]{COLOSSAL_MONOLITH_HM_16M}); // HM 16m

		bosses.add(new RaidBoss(this, RaidBossName.WorldbreakerMonolith, Mode.SM, Size.Sixteen,
				new long[]{WORLDBREAKER_MONOLITH_BOSS}));

		RaidBoss.add(this, RaidBossName.GoldenFury,
				new long[]{3210174521147392L}, // SM 8m
				new long[]{3232800408862720L}, // SM 16m
				new long[]{3232735984353280L}, // HM 8m
				new long[]{3232817588731904L}); // HM 16m

		RaidBoss.add(this, RaidBossName.Eyeless,
				new long[]{3319090596806656L}, // SM 8m
				new long[]{3328376316100608L}, // SM 16m
				new long[]{3328372021133312L}, // HM 8m
				new long[]{3328380611067904L}); // HM 16m

		RaidBoss.add(this, RaidBossName.XenoanalystII,
				new long[]{3153545377349632L}, // SM 8m
				new long[]{3213924027596800L}, // SM 16m
				new long[]{3213919732629504L}, // HM 8m
				new long[]{3213928322564096L}); // HM 16m

		RaidBoss.add(this, RaidBossName.MutatedGeonosianQueen,
				new long[]{4197299739688960L}, // SM 8m
				new long[]{4204768687816704L}, // SM 16m
				new long[]{4204764392849408L}, // HM 8m
				new long[]{4204772982784000L}); // HM 16m
	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {

		if (c.getBoss() == null) {
			return null;
		}

		switch (c.getBoss().getRaidBossName()) {
			case ColossalMonolith:
				return getNewPhaseNameForMonolith(e, c, currentPhaseName);
			case MutatedGeonosianQueen:
				return getNewPhaseNameForQueen(e, c, currentPhaseName);
			default:
				return null;
		}
	}

	private String getNewPhaseNameForMonolith(final Event e, final Combat c, final String currentPhaseName) {

		// ------------------ Timers ------------------

		if (Helpers.isAbilityEqual(e, 3546045258661888L)) {        // Bite Wounds
			TimerManager.stopTimer(MonolithBiteWoundsTimer.class);
			TimerManager.startTimer(MonolithBiteWoundsTimer.class, e.getTimestamp());
		}

		return null;
	}

	private String getNewPhaseNameForQueen(final Event e, final Combat c, final String currentPhaseName) {
		if (Helpers.isTargetOtherPlayer(e)) return null;    // returns if target is other player

		if (currentPhaseName == null) {
			phaseTimers.clear();
			phaseTimers.put(QUEEN_PHASE_GUARDS_1, c.getTimeFrom() + 2500 + 48000 + 3000);

			// setup timers
			TimerManager.startTimer(QueenRoyalSummonsGuardsTimer.class, c.getTimeFrom() + 2500 + 3000);

			return QUEEN_PHASE_OPENING;
		}

		if (QUEEN_PHASE_OPENING.equals(currentPhaseName)
				&& phaseTimers.containsKey(QUEEN_PHASE_GUARDS_1)
				&& phaseTimers.get(QUEEN_PHASE_GUARDS_1) <= e.getTimestamp()) {
			TimerManager.stopTimer(QueenRoyalSummonsGuardsTimer.class);
			TimerManager.startTimer(QueenRoyalSummonsCausticTimer.class, phaseTimers.get(QUEEN_PHASE_GUARDS_1));

			phaseTimers.put(QUEEN_PHASE_CAUSTIC_2, phaseTimers.get(QUEEN_PHASE_GUARDS_1) + 74000);
			phaseTimers.remove(QUEEN_PHASE_GUARDS_1);

			return QUEEN_PHASE_GUARDS_1;
		}

		if (QUEEN_PHASE_GUARDS_1.equals(currentPhaseName)
				&& phaseTimers.containsKey(QUEEN_PHASE_CAUSTIC_2)
				&& phaseTimers.get(QUEEN_PHASE_CAUSTIC_2) <= e.getTimestamp()) {
			TimerManager.stopTimer(QueenRoyalSummonsCausticTimer.class);
			TimerManager.startTimer(QueenRoyalSummonsGuardsTimer.class, phaseTimers.get(QUEEN_PHASE_CAUSTIC_2));

			phaseTimers.put(QUEEN_PHASE_GUARDS_3, phaseTimers.get(QUEEN_PHASE_CAUSTIC_2) + 48000);
			phaseTimers.remove(QUEEN_PHASE_CAUSTIC_2);

			return QUEEN_PHASE_CAUSTIC_2;
		}

		if (QUEEN_PHASE_CAUSTIC_2.equals(currentPhaseName)
				&& phaseTimers.containsKey(QUEEN_PHASE_GUARDS_3)
				&& phaseTimers.get(QUEEN_PHASE_GUARDS_3) <= e.getTimestamp()) {
			TimerManager.stopTimer(QueenRoyalSummonsGuardsTimer.class);
			TimerManager.startTimer(QueenRoyalSummonsCausticTimer.class, phaseTimers.get(QUEEN_PHASE_GUARDS_3));

			phaseTimers.put(QUEEN_PHASE_CAUSTIC_4, phaseTimers.get(QUEEN_PHASE_GUARDS_3) + 74000);
			phaseTimers.remove(QUEEN_PHASE_GUARDS_3);

			return QUEEN_PHASE_GUARDS_3;
		}

		if (QUEEN_PHASE_GUARDS_3.equals(currentPhaseName)
				&& phaseTimers.containsKey(QUEEN_PHASE_CAUSTIC_4)
				&& phaseTimers.get(QUEEN_PHASE_CAUSTIC_4) <= e.getTimestamp()) {
			TimerManager.stopTimer(QueenRoyalSummonsCausticTimer.class);
			TimerManager.startTimer(QueenRoyalSummonsGuardsTimer.class, phaseTimers.get(QUEEN_PHASE_CAUSTIC_4));

			phaseTimers.put(QUEEN_PHASE_GUARDS_5, phaseTimers.get(QUEEN_PHASE_CAUSTIC_4) + 48000);
			phaseTimers.remove(QUEEN_PHASE_CAUSTIC_4);

			return QUEEN_PHASE_CAUSTIC_4;
		}

		if (QUEEN_PHASE_CAUSTIC_4.equals(currentPhaseName)
				&& phaseTimers.containsKey(QUEEN_PHASE_GUARDS_5)
				&& phaseTimers.get(QUEEN_PHASE_GUARDS_5) <= e.getTimestamp()) {
			TimerManager.stopTimer(QueenRoyalSummonsGuardsTimer.class);

			phaseTimers.remove(QUEEN_PHASE_GUARDS_5);

			return QUEEN_PHASE_GUARDS_5;
		}

		return null;
	}

	public static class MonolithBiteWoundsTimer extends BaseTimer {
		public MonolithBiteWoundsTimer() {
			super("Bite Wounds", "Monolith Bite Wounds", 20000);
			setColor(0);
		}
	}

	public static class QueenRoyalSummonsGuardsTimer extends BaseTimer {
		public QueenRoyalSummonsGuardsTimer() {
			super("Royal Guards", "Queen Royal Summons - Guards", 48000);
			setColor(0);
		}
	}

	public static class QueenRoyalSummonsCausticTimer extends BaseTimer {
		public QueenRoyalSummonsCausticTimer() {
			super("Caustic Drones", "Queen Royal Summons - Caustic", 74000);
			setColor(1);
		}
	}
}