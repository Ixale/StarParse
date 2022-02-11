package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.parser.Helpers;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;

public class Iokath extends Raid {

	private static final long TYTH_SM_8M = 4078427929837568L,
			TYTH_SM_16M = 4078423634870272L,
			TYTH_HM_8M = 4078419339902976L,
			TYTH_HM_16M = 4078415044935680L;

	private static final String TYTH_PHASE_OPENING = "Opening",
			TYTH_PHASE_INVERSION = "Inversion";

	private static final long ESNE_SM_8M = 4088538282852352L,
			AIVELA_SM_8M = 4088525397950464L,
			AIVELA_SM_16M = 4114282316824576L,
			AIVELA_HM_8M = 4114286611791872L,
			AIVELA_HM_16M = 4114290906759168L;

	private static final long NAHUT_SM_8M = 4108011664572416L,
			NAHUT_SM_16M = 4122949560827904L,
			NAHUT_HM_8M = 4122953855795200L,
			NAHUT_HM_16M = 4122958150762496L;

	private static final long SCYVA_SM_8M = 4108140513591296L,
			SCYVA_SM_16M = 4158786767945728L, // Iokath Control Center
			SCYVA_HM_8M = 4158791062913024L, // Iokath Control Center
			SCYVA_HM_16M = 4158859782389760L; // Iokath Control Center

	private static final long IZAX_SM_8M = 4108097563918336L,
			IZAX_SM_16M = 4163128979881984L, // Relay tower
			IZAX_HM_8M = 4163133274849280L, // Relay tower
			IZAX_HM_16M = 4163137569816576L; // Relay tower

	public Iokath() {
		super("Gods of the Machine");

		RaidBoss.add(this, RaidBossName.Tyth,
				new long[]{TYTH_SM_8M}, // SM 8m
				new long[]{TYTH_SM_16M}, // SM 16m
				new long[]{TYTH_HM_8M}, // HM 8m
				new long[]{TYTH_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.AivelaAndEsne,
				new long[]{AIVELA_SM_8M, ESNE_SM_8M, 4109905745149952L}, // SM 8m
				new long[]{AIVELA_SM_16M}, // SM 16m
				new long[]{AIVELA_HM_8M}, // HM 8m
				new long[]{AIVELA_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.Nahut,
				new long[]{NAHUT_SM_8M}, // SM 8m
				new long[]{NAHUT_SM_16M}, // SM 16m
				new long[]{NAHUT_HM_8M}, // HM 8m
				new long[]{NAHUT_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.Scyva,
				new long[]{SCYVA_SM_8M}, // SM 8m
				new long[]{SCYVA_SM_16M}, // SM 16m
				new long[]{SCYVA_HM_8M}, // HM 8m
				new long[]{SCYVA_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.Izax,
				new long[]{IZAX_SM_8M}, // SM 8m
				new long[]{IZAX_SM_16M}, // SM 16m
				new long[]{IZAX_HM_8M}, // HM 8m
				new long[]{IZAX_HM_16M}, // HM 16m
				null);
	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {
		switch (c.getBoss().getRaidBossName()) {
			case Tyth:
				return getNewPhaseNameForTyth(e, c, currentPhaseName);
			case Nahut:
				return getNewPhaseNameForNahut(e, c, currentPhaseName);
			default:
				return null;
		}
	}

	private String getNewPhaseNameForTyth(final Event e, final Combat c, final String currentPhaseName) {
		if (c.getBoss().getMode() == Mode.SM) return null; // only HM/NiM

		// dummy phases
		if (currentPhaseName == null) {
			phaseTimers.clear();
			phaseTimers.put(TYTH_PHASE_INVERSION + "1", c.getTimeFrom());

			// setup timers
			TimerManager.startTimer(TythInversionTimer.class, c.getTimeFrom() - 11000); // first @ 26.5s

			return TYTH_PHASE_OPENING;
		}

		if (Helpers.isAbilityEqual(e, 4071117895499776L) && Helpers.isActionApply(e) && Helpers.isTargetThisPlayer(e)) { // Inversion
			if (Helpers.isEffectEqual(e, 4071117895500098L) || Helpers.isEffectEqual(e, 4071117895500102L)) { // Short/Long Wave
				TimerManager.stopTimer(TythInversionTimer.class);
				TimerManager.startTimer(TythInversionTimer.class, e.getTimestamp());

				for (int i = 1; i <= 10; i++) {
					if (((i == 1 && TYTH_PHASE_OPENING.equals(currentPhaseName))
							|| (TYTH_PHASE_INVERSION + " " + (i - 1)).equals(currentPhaseName))) {

						phaseTimers.put(TYTH_PHASE_INVERSION + (i + 1), -1L); // dummy
						phaseTimers.remove(TYTH_PHASE_INVERSION + i);

						return (TYTH_PHASE_INVERSION + " " + i);
					}
				}
			}
		}

		return null;
	}

	private String getNewPhaseNameForNahut(final Event e, final Combat c, final String currentPhaseName) {

		// ------------------ Timers ------------------

		if (Helpers.isAbilityEqual(e, 4137075708264448L) && Helpers.isEffectAbilityActivate(e)) { // Energized Slice
			TimerManager.stopTimer(NahutSliceTimer.class);
			TimerManager.startTimer(NahutSliceTimer.class, e.getTimestamp());
		}

		return null;
	}

	public static class TythInversionTimer extends BaseTimer {
		public TythInversionTimer() {
			super("Inversion", "Tyth Inversion", 37500);
			setColor(0);
		}
	}

	public static class NahutSliceTimer extends BaseTimer {
		public NahutSliceTimer() {
			super("Slice", "Nahut Slice", 12000);
			setColor(0);
		}
	}
}
