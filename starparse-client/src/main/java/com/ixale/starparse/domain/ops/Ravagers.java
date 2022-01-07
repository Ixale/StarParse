package com.ixale.starparse.domain.ops;

import static com.ixale.starparse.parser.Helpers.*;

import java.util.Arrays;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatChallenge;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.RaidChallengeName;
import com.ixale.starparse.parser.Helpers;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;

public class Ravagers extends Raid {

	private static final long CORATANNI_RUUGAR = 3371441953177600L;

	private static final String TORQUE_PHASE_OPENING = "Opening",
		TORQUE_PHASE_CONSOLE = "Consoles",
		CORATANNI_PHASE_CORATANNI = "Coratanni",
		CORATANNI_PHASE_RUUGAR = "Ruugar";

	private static final int TORQUE_TIMER_RAGE_INTERVAL = 45 * 1000 + 500,
		TORQUE_TIMER_ENRAGE = (5 * 60 + 40) * 1000,
		RUUGAR_TIMER_ENRAGE = (4 * 60 + 20) * 1000;

	public Ravagers() {
		super("Ravagers");

		RaidBoss.add(this, RaidBossName.Sparky,
			new long[]{3367555007774720L}, // SM 8m
			new long[]{3458114393210880L}, // SM 16m
			new long[]{3458110098243584L}, // HM 8m
			new long[]{3458122983145472L}); // HM 16m

		RaidBoss.add(this, RaidBossName.QuartermasteBulo,
			new long[]{3371446248144896L},
			new long[]{3468701487595520L},
			new long[]{3468705782562816L},
			new long[]{3468697192628224L});

		RaidBoss.add(this, RaidBossName.Torque,
			new long[]{3397005598523392L},
			new long[]{3468714372497408L},
			new long[]{3468710077530112L},
			new long[]{3468718667464704L});

		RaidBoss.add(this, RaidBossName.Blaster,
			new long[]{3391095723524096L},
			new long[]{3462181727240192L},
			new long[]{3458148752949248L},
			new long[]{3462186022207488L});

		final RaidBoss cora16h = new RaidBoss(this, RaidBossName.Coratanni, Mode.HM, Size.Sixteen,
			new long[]{3468740142301184L}); // Chest

		final RaidBoss cora8h = new RaidBoss(this, RaidBossName.Coratanni, Mode.HM, Size.Eight,
			new long[]{3468731552366592L}, // Chest
			null,
			new RaidBoss.BossUpgrade(new RaidBoss[]{cora16h})); // Burning -> Chest

		final RaidBoss cora16s = new RaidBoss(this, RaidBossName.Coratanni, Mode.SM, Size.Sixteen,
			new long[]{3468735847333888L}, // Chest
			null,
			new RaidBoss.BossUpgrade(new RaidBoss[]{cora16h}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					return upgradeCoratanniHM(guid, value, cora16h);
				}
			});

		final RaidBoss cora8s = new RaidBoss(this, RaidBossName.Coratanni, Mode.SM, Size.Eight,
			new long[]{3443983950807040L}, // Chest
			new long[]{3371437658210304L, CORATANNI_RUUGAR}, // Coratanni, Ruugar
			new RaidBoss.BossUpgrade(new RaidBoss[]{cora16s, cora8h, cora16h}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					return upgradeCoratanniHM(guid, value, cora8h);
				}
			});

		bosses.add(cora8s);
		bosses.add(cora16s);
		bosses.add(cora8h);
		bosses.add(cora16h);

		addChallenge(RaidBossName.Torque, new TorqueDroidChallenge());
		addChallenge(RaidBossName.Coratanni, new CoratanniRuugarChallenge());
	}

	private RaidBoss upgradeCoratanniHM(long guid, final Integer value, final RaidBoss hmBoss) {
		if ((guid == 3441368315724050L || guid == 3438915889398034L) && value != null && value > 6000) { // Coratanni Burning, Ruugar Burned
			return hmBoss;
		}
		return null;
	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {

		if (c.getBoss() == null) {
			return null;
		}
		
		if (Helpers.isTargetOtherPlayer(e)) return null;	// returns if target is other player

		switch (c.getBoss().getRaidBossName()) {
			case Torque:
				return getNewPhaseNameForTorque(e, c, currentPhaseName);
			case Coratanni:
				return getNewPhaseNameForCoratanni(e, c, currentPhaseName);
			default:
				return null;
		}
	}

	private String getNewPhaseNameForTorque(final Event e, final Combat c, final String currentPhaseName) {

		for (int i = 1; i <= 10; i++) {
			if (currentPhaseName != null && phaseTimers.get(TORQUE_PHASE_CONSOLE + i) != null && (phaseTimers.get(TORQUE_PHASE_CONSOLE + i) <= e.getTimestamp())) {

				phaseTimers.put(TORQUE_PHASE_CONSOLE + (i + 1), phaseTimers.get((TORQUE_PHASE_CONSOLE + i)) + TORQUE_TIMER_RAGE_INTERVAL);
				phaseTimers.remove(TORQUE_PHASE_CONSOLE + i);

				if (i == 6) {
					TimerManager.startTimer(TorqueEnrageTimer.class, c.getTimeFrom());
				}

				return TORQUE_PHASE_CONSOLE.equals(currentPhaseName) ? null : TORQUE_PHASE_CONSOLE;
			}
		}

		if (currentPhaseName == null) {
			phaseTimers.clear();
			phaseTimers.put(TORQUE_PHASE_CONSOLE + "1", c.getTimeFrom() + TORQUE_TIMER_RAGE_INTERVAL);

			// setup timers
			TimerManager.startTimer(TorqueRageTimer.class, c.getTimeFrom());

			return TORQUE_PHASE_OPENING;
		}

		return null;
	}

	private String getNewPhaseNameForCoratanni(final Event e, final Combat c, final String currentPhaseName) {

		if (CORATANNI_PHASE_RUUGAR.equals(currentPhaseName)) {
			if (phaseTimers.containsKey(CORATANNI_PHASE_RUUGAR)
				&& (phaseTimers.get(CORATANNI_PHASE_RUUGAR) + (RUUGAR_TIMER_ENRAGE - 60000)) < e.getTimestamp()) {
				// show when 60s to enrage
				TimerManager.startTimer(RuugarEnrageTimer.class, phaseTimers.get(CORATANNI_PHASE_RUUGAR));
				phaseTimers.clear();
			}
		}

		if (CORATANNI_PHASE_CORATANNI.equals(currentPhaseName)) {
			if (isTargetEqual(e, CORATANNI_RUUGAR) || isSourceEqual(e, CORATANNI_RUUGAR)) {

				phaseTimers.put(CORATANNI_PHASE_RUUGAR, e.getTimestamp());

				return CORATANNI_PHASE_RUUGAR;
			}
		}

		if (currentPhaseName == null) {
			phaseTimers.clear();

			return CORATANNI_PHASE_CORATANNI;
		}

		return null;
	}

	public static class TorqueEnrageTimer extends BaseTimer {
		public TorqueEnrageTimer() {
			super("Enrage", "Torque Enrage", TORQUE_TIMER_ENRAGE);
			setColor(2);
		}
	}

	public static class TorqueRageTimer extends BaseTimer {
		public TorqueRageTimer() {
			super("Rage", "Torque Wookierage", TORQUE_TIMER_RAGE_INTERVAL, TORQUE_TIMER_RAGE_INTERVAL);
			setColor(0);
		}
	}

	public static class RuugarEnrageTimer extends BaseTimer {
		public RuugarEnrageTimer() {
			super("Enrage", "Ruugar Enrage", RUUGAR_TIMER_ENRAGE);
			setColor(2);
		}
	}

	/**
	 * Challenges
	 * ------------------------------------------------------------------------
	 */

	public static class TorqueDroidChallenge extends CombatChallenge {
		public TorqueDroidChallenge() {
			super(RaidChallengeName.TorqueDroid,
				TORQUE_PHASE_CONSOLE, CombatChallenge.Type.FRIENDLY,
				Arrays.asList(new Object[]{/*3513403507212288L,*/ 3397735742963712L}), // FIX-4U, Astromech Repair Unit
				"target_guid IN (?)");
		}
	}

	public static class CoratanniRuugarChallenge extends CombatChallenge {
		public CoratanniRuugarChallenge() {
			super(RaidChallengeName.CoratanniRuugar,
				CORATANNI_PHASE_RUUGAR, CombatChallenge.Type.DAMAGE,
				Arrays.asList(new Object[]{CORATANNI_RUUGAR}),
				"target_guid IN (?)");
		}
	}
}
