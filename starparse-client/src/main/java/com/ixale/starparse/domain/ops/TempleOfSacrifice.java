package com.ixale.starparse.domain.ops;

import static com.ixale.starparse.parser.Helpers.isAbilityEqual;
import static com.ixale.starparse.parser.Helpers.isSourceEqual;
import static com.ixale.starparse.parser.Helpers.isTargetEqual;

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

public class TempleOfSacrifice extends Raid {

	private static final long REVAN_BOSS = 3431605855059968L,
		REVAN_HK = 3444310368321536L,
		REVAN_CORE = 3447583133401088L,
		REVAN_FOCUS = 3440805675008000L,
		REVAN_ABBERATION = 3449584588161024L,
		REVAN_HEAVE = 3462632698806272L,
		REVAN_HK_GRENADES = 3447669032747008L,
		REVAN_HK_PSEUDO = 3447651852877824L,
		REVAN_RESONANCE = 3447578838433792L;

	private static final String SQUADRON_PHASE_OPENING = "Opening",
		SQUADRON_PHASE_SHIELD = "Shield",
		UNDERLURKER_PHASE_OPENING = "Opening",
		UNDERLURKER_PHASE_WAVE = "Wave",
		REVAN_PHASE_FIRST = "First",
		REVAN_PHASE_HK = "HK",
		REVAN_PHASE_FOCUS = "Focus",
		REVAN_PHASE_SECOND = "Second",
		REVAN_PHASE_SHIELD = "Shield",
		REVAN_PHASE_THIRD = "Third",
		REVAN_PHASE_CORE = "Core",
		REVAN_PULL_TIMER = "pull",
		REVAN_PUSH_TIMER = "push";

	private static final int SQUADRON_TIMER_SHIELD_DELAY = 30 * 1000,
		SQUADRON_TIMER_SHIELD_INTERVAL = 60 * 1000 + 700,
		SQUADRON_TIMER_PULL_DELAY = 45 * 1000 - 600,
		SQUADRON_TIMER_PULL_INTERVAL = 40 * 1000 + 600,
		SQUADRON_TIMER_ENRAGE = 6 * 60 * 1000,
		UNDERLURKER_TIMER_ADDS_DELAY = 15 * 1000 + 200,
		UNDERLURKER_TIMER_ADDS_INTERVAL = 50 * 1000 - 800,
//		UNDERLURKER_TIMER_STORM_DELAY = 50 * 1000,
//		UNDERLURKER_TIMER_STORM_INTERVAL = 50 * 1000 + 500,
		UNDERLURKER_TIMER_ENRAGE = 5 * 60 * 1000,
		REVAN_TIMER_HEAVE_INTERVAL = 30 * 1000 + 500,
		REVAN_TIMER_HEARTBEAT_INTERVAL = 15 * 1000,
		REVAN_PHASE_SHIELD_TIMEOUT = 8 * 1000; // 6s apart + 2s "BW buffer"

	public TempleOfSacrifice() {
		super("Temple of Sacrifice");

		RaidBoss.add(this, RaidBossName.MalapharTheSavage,
			new long[]{3431245077807104L}, // SM 8m
			new long[]{3469281308180480L}, // SM 16m
			new long[]{3469277013213184L}, // HM 8m
			new long[]{3469285603147776L}); // HM 16m

		// Sword Squadron
		final RaidBoss sq16h = new RaidBoss(this, RaidBossName.SwordSquadron, Mode.HM, Size.Sixteen,
			new long[]{3468774502039552L}); // Unit 1

		final RaidBoss sq8h = new RaidBoss(this, RaidBossName.SwordSquadron, Mode.HM, Size.Eight,
			new long[]{3468765912104960L}); // Unit 1

		final RaidBoss sq16s = new RaidBoss(this, RaidBossName.SwordSquadron, Mode.SM, Size.Sixteen,
			new long[]{3468770207072256L}); // Unit 1

		final RaidBoss sq8s = new RaidBoss(this, RaidBossName.SwordSquadron, Mode.SM, Size.Eight,
			new long[]{3447784996864000L}, // Unit 1
			new long[]{3447789291831296L}, // Unit 2
			new RaidBoss.BossUpgrade(new RaidBoss[]{sq16s, sq8h, sq16h}));

		bosses.add(sq8s);
		bosses.add(sq16s);
		bosses.add(sq8h);
		bosses.add(sq16h);

		RaidBoss.add(this, RaidBossName.Underlurker,
			new long[]{3411402328899584L}, // SM 8m
			new long[]{3462267626586112L}, // SM 16m
			new long[]{3462263331618816L}, // HM 8m
			new long[]{3462271921553408L}); // HM 16m

		// Revanite Commanders
		final RaidBoss rc16h = new RaidBoss(this, RaidBossName.RevaniteCommanders, Mode.HM, Size.Sixteen,
			new long[]{3483033793462272L}); // Cache

		final RaidBoss rc8h = new RaidBoss(this, RaidBossName.RevaniteCommanders, Mode.HM, Size.Eight,
			new long[]{3483025203527680L}, // Cache
			null,
			new RaidBoss.BossUpgrade(new RaidBoss[]{rc16h})); // Cryo -> Cache

		final RaidBoss rc16s = new RaidBoss(this, RaidBossName.RevaniteCommanders, Mode.SM, Size.Sixteen,
			new long[]{3483029498494976L}, // Cache
			null,
			new RaidBoss.BossUpgrade(new RaidBoss[]{rc16h}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					return upgradeCommandersHM(guid, value, rc16h);
				}
			});

		final RaidBoss rc8s = new RaidBoss(this, RaidBossName.RevaniteCommanders, Mode.SM, Size.Eight,
			new long[]{3482806160195584L}, // Cache
			new long[]{3456890327531520L, 3456894622498816L, 3456898917466112L}, // Deron, Sano, Kurse
			new RaidBoss.BossUpgrade(new RaidBoss[]{rc16s, rc8h, rc16h}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					return upgradeCommandersHM(guid, value, rc8h);
				}
			});

		bosses.add(rc8s);
		bosses.add(rc16s);
		bosses.add(rc8h);
		bosses.add(rc16h);

		// Revan
		final RaidBoss revan16h = new RaidBoss(this, RaidBossName.Revan, Mode.HM, Size.Sixteen,
			new long[]{3518883885481984L, 3484412477964288L}); // Probe, Cache

		final RaidBoss revan8h = new RaidBoss(this, RaidBossName.Revan, Mode.HM, Size.Eight,
			new long[]{3518888180449280L, 3484403888029696L}, // Probe, Cache
			null,
			new RaidBoss.BossUpgrade(new RaidBoss[]{revan16h})); // Probe -> Cache

		final RaidBoss revan16s = new RaidBoss(this, RaidBossName.Revan, Mode.SM, Size.Sixteen,
			new long[]{3484408182996992L}, // Cache
			null,
			new RaidBoss.BossUpgrade(new RaidBoss[]{revan16h}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					return upgradeRevanHM(guid, value, revan16h);
				}
			});

		final RaidBoss revan8s = new RaidBoss(this, RaidBossName.Revan, Mode.SM, Size.Eight,
			new long[]{3484395298095104L}, // Cache
			new long[]{REVAN_BOSS, REVAN_HK, REVAN_FOCUS, REVAN_ABBERATION, REVAN_CORE}, // tentative
			new RaidBoss.BossUpgrade(new RaidBoss[]{revan16s, revan8h, revan16h}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					return upgradeRevanHM(guid, value, revan8h);
				}
			});
		bosses.add(revan8s);
		bosses.add(revan16s);
		bosses.add(revan8h);
		bosses.add(revan16h);

		addChallenge(RaidBossName.Revan, new RevanBurnChallenge());
	}

	private RaidBoss upgradeCommandersHM(long guid, final Integer value, final RaidBoss hmBoss) {
		if (guid == 3470617043009536L) { // Cryo Grenade
			return hmBoss;
		}
		if (guid == 3470050107326464L && value != null && value > 5000) { // Death From Above
			return hmBoss;
		}
		return null;
	}

	private RaidBoss upgradeRevanHM(long guid, final Integer value, final RaidBoss hmBoss) {
		if (guid == 3447359795101696L // Essence Corruption
			|| guid == 3443275281203200L // Trial of Agony 
			|| guid == 3454111483691008L) { // Malevolent Force Bond
			return hmBoss;
		}
		if (guid == 3456112938451302L && value != null && value > 900) { // Consume Essence
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
			case SwordSquadron:
				return getNewPhaseNameForSwordSquadron(e, c, currentPhaseName);
			case Underlurker:
				return getNewPhaseNameForUnderlurker(e, c, currentPhaseName);
			case Revan:
				return getNewPhaseNameForRevan(e, c, currentPhaseName);
			default:
				return null;
		}
	}

	private String getNewPhaseNameForSwordSquadron(final Event e, final Combat c, final String currentPhaseName) {
		
		for (int i = 1; i <= 10; i++) {
			if (((i == 1 && SQUADRON_PHASE_OPENING.equals(currentPhaseName))
				|| (SQUADRON_PHASE_SHIELD + " " + (i - 1)).equals(currentPhaseName)) && phaseTimers.get(SQUADRON_PHASE_SHIELD + i) <= e.getTimestamp()) {

				phaseTimers.put(SQUADRON_PHASE_SHIELD + (i + 1), phaseTimers.get((SQUADRON_PHASE_SHIELD + i)) + SQUADRON_TIMER_SHIELD_INTERVAL);
				phaseTimers.remove(SQUADRON_PHASE_SHIELD + i);

				if (i == 6) {
					TimerManager.startTimer(SwordSquadronEnrageTimer.class, c.getTimeFrom());
				}

				return (SQUADRON_PHASE_SHIELD + " " + i);
			}
		}

		if (currentPhaseName == null) {
			phaseTimers.clear();
			phaseTimers.put(SQUADRON_PHASE_SHIELD + "1", c.getTimeFrom() + SQUADRON_TIMER_SHIELD_DELAY);

			// setup timers
			TimerManager.startTimer(SwordSquadronShieldTimer.class, c.getTimeFrom());
			if (c.getBoss().getMode().equals(Mode.HM)) {
				TimerManager.startTimer(SwordSquadronPullTimer.class, c.getTimeFrom());
			}

			return SQUADRON_PHASE_OPENING;
		}

		return null;
	}

	private String getNewPhaseNameForUnderlurker(final Event e, final Combat c, final String currentPhaseName) {

		for (int i = 1; i <= 10; i++) {
			if (((i == 1 && UNDERLURKER_PHASE_OPENING.equals(currentPhaseName))
				|| (UNDERLURKER_PHASE_WAVE + " " + (i - 1)).equals(currentPhaseName)) && phaseTimers.get(UNDERLURKER_PHASE_WAVE + i) <= e.getTimestamp()) {

				phaseTimers.put(UNDERLURKER_PHASE_WAVE + (i + 1), phaseTimers.get((UNDERLURKER_PHASE_WAVE + i)) + UNDERLURKER_TIMER_ADDS_INTERVAL);
				phaseTimers.remove(UNDERLURKER_PHASE_WAVE + i);

				if (i == 5) {
					TimerManager.startTimer(UnderlurkerEnrageTimer.class, c.getTimeFrom());
				}

				return (UNDERLURKER_PHASE_WAVE + " " + i);
			}
		}

		if (currentPhaseName == null) {
			phaseTimers.clear();
			phaseTimers.put(UNDERLURKER_PHASE_WAVE + "1", c.getTimeFrom() + UNDERLURKER_TIMER_ADDS_DELAY);

			// setup timers
			TimerManager.startTimer(UnderlurkerAddsTimer.class, c.getTimeFrom());

			return SQUADRON_PHASE_OPENING;
		}

		return null;
	}

	private String getNewPhaseNameForRevan(final Event e, final Combat c, final String currentPhaseName) {

		// timers
		if (isAbilityEqual(e, REVAN_HEAVE)) {
			TimerManager.stopTimer(RevanHeaveTimer.class);
			TimerManager.startTimer(RevanHeaveTimer.class, e.getTimestamp());
		}

		if (REVAN_PHASE_THIRD.equals(currentPhaseName) || REVAN_PHASE_CORE.equals(currentPhaseName)) {
			if (phaseTimers.get(REVAN_PULL_TIMER) != null && phaseTimers.get(REVAN_PULL_TIMER) <= e.getTimestamp()) {
				// show pull timer, prepare push
				TimerManager.stopTimer(RevanPushTimer.class);
				TimerManager.startTimer(RevanPullTimer.class, phaseTimers.get(REVAN_PULL_TIMER));
				phaseTimers.put(REVAN_PUSH_TIMER, phaseTimers.get(REVAN_PULL_TIMER) + REVAN_TIMER_HEARTBEAT_INTERVAL);
				phaseTimers.remove(REVAN_PULL_TIMER);

			} else if (phaseTimers.get(REVAN_PUSH_TIMER) != null && phaseTimers.get(REVAN_PUSH_TIMER) <= e.getTimestamp()) {
				// show push timer, prepare pull
				TimerManager.stopTimer(RevanPullTimer.class);
				TimerManager.startTimer(RevanPushTimer.class, phaseTimers.get(REVAN_PUSH_TIMER));
				phaseTimers.put(REVAN_PULL_TIMER, phaseTimers.get(REVAN_PUSH_TIMER) + REVAN_TIMER_HEARTBEAT_INTERVAL);
				phaseTimers.remove(REVAN_PUSH_TIMER);
			}
		}

		if (!REVAN_PHASE_CORE.equals(currentPhaseName)
			&& (isSourceEqual(e, REVAN_CORE) || isTargetEqual(e, REVAN_CORE))) {
			return REVAN_PHASE_CORE;
		}

		if (REVAN_PHASE_SHIELD.equals(currentPhaseName)) {
			if (isAbilityEqual(e, REVAN_RESONANCE)) {
				// Resonance tick, prolong
				phaseTimers.put(REVAN_PHASE_THIRD, e.getTimestamp() + REVAN_PHASE_SHIELD_TIMEOUT);
				return null;
			}

			final boolean hit = (isTargetEqual(e, REVAN_BOSS)
				&& e.getValue() != null && e.getValue() > 0
				&& (e.getAbsorbed() == null || (e.getAbsorbed() + 2 /* 5.x fix */) < e.getValue()));
			if (
			// no Resonance within timeout (6s + buffer) => proceed to the next phase
			(phaseTimers.get(REVAN_PHASE_THIRD) < e.getTimestamp())
				// non-absorbed hit on Revan => shield is down
				|| hit) {
				// start last phase (will trigger challenge and push/pull timers)
				phaseTimers.put(REVAN_PULL_TIMER, e.getTimestamp() + REVAN_TIMER_HEARTBEAT_INTERVAL + (hit ? 0 : -4000)); // compensate timeout
				TimerManager.startTimer(RevanPushTimer.class, e.getTimestamp());

				return REVAN_PHASE_THIRD;
			}
		}

		if (REVAN_PHASE_SECOND.equals(currentPhaseName)
			&& (isAbilityEqual(e, REVAN_RESONANCE))) {
			phaseTimers.put(REVAN_PHASE_THIRD, e.getTimestamp() + REVAN_PHASE_SHIELD_TIMEOUT);

			TimerManager.stopTimer(RevanHeaveTimer.class);

			return REVAN_PHASE_SHIELD;
		}

		if (REVAN_PHASE_FOCUS.equals(currentPhaseName)
			&& (isTargetEqual(e, REVAN_BOSS) || isSourceEqual(e, REVAN_BOSS)
				&& (e.getThreat() != null && e.getThreat() > 0))) {

			return REVAN_PHASE_SECOND;
		}

		if (REVAN_PHASE_HK.equals(currentPhaseName)
			&& (isTargetEqual(e, REVAN_FOCUS) || isSourceEqual(e, REVAN_FOCUS))) {

			return REVAN_PHASE_FOCUS;
		}

		if (REVAN_PHASE_FIRST.equals(currentPhaseName)
			&& ((isTargetEqual(e, REVAN_HK) && e.getValue() != null && e.getValue() > 0) // non-zero hit on HK
				|| isAbilityEqual(e, REVAN_HK_PSEUDO)
				|| isAbilityEqual(e, REVAN_HK_GRENADES))) {

			TimerManager.stopTimer(RevanHeaveTimer.class);

			return REVAN_PHASE_HK;
		}

		if (currentPhaseName == null) {
			phaseTimers.clear();

			return REVAN_PHASE_FIRST;
		}

		return null;
	}

	public static class SwordSquadronEnrageTimer extends BaseTimer {
		public SwordSquadronEnrageTimer() {
			super("Enrage", "Walkers Enrage", SQUADRON_TIMER_ENRAGE);
			setColor(2);
		}
	}

	public static class SwordSquadronPullTimer extends BaseTimer {
		public SwordSquadronPullTimer() {
			this(SQUADRON_TIMER_PULL_DELAY);
		}

		public SwordSquadronPullTimer(Integer delay) {
			super("Gravity", "Walkers Gravity", delay, SQUADRON_TIMER_PULL_INTERVAL);
			setColor(1);
		}
	}

	public static class SwordSquadronShieldTimer extends BaseTimer {
		public SwordSquadronShieldTimer() {
			this(SQUADRON_TIMER_SHIELD_DELAY);
		}

		public SwordSquadronShieldTimer(Integer delay) {
			super("Shield", "Walkers Shield", delay, SQUADRON_TIMER_SHIELD_INTERVAL);
			setColor(0);
		}
	}

	public static class UnderlurkerEnrageTimer extends BaseTimer {
		public UnderlurkerEnrageTimer() {
			super("Enrage", "Underluker Enrage", UNDERLURKER_TIMER_ENRAGE);
			setColor(2);
		}
	}

	public static class UnderlurkerAddsTimer extends BaseTimer {
		public UnderlurkerAddsTimer() {
			this(UNDERLURKER_TIMER_ADDS_DELAY);
		}

		public UnderlurkerAddsTimer(Integer delay) {
			super("Adds", "Underluker Adds", delay, UNDERLURKER_TIMER_ADDS_INTERVAL);
			setColor(0);
		}
	}

	public static class RevanHeaveTimer extends BaseTimer {
		public RevanHeaveTimer() {
			super("Heave", "Revan Heave", REVAN_TIMER_HEAVE_INTERVAL);
			setColor(0);
		}
	}

	public static class RevanPullTimer extends BaseTimer {
		public RevanPullTimer() {
			super("Pull", "Revan Pull", REVAN_TIMER_HEARTBEAT_INTERVAL);
			setColor(1);
		}
	}

	public static class RevanPushTimer extends BaseTimer {
		public RevanPushTimer() {
			super("Push", "Revan Push", REVAN_TIMER_HEARTBEAT_INTERVAL);
			setColor(3);
		}
	}

	public static class RevanBurnChallenge extends CombatChallenge {
		public RevanBurnChallenge() {
			super(RaidChallengeName.RevanBurn,
				REVAN_PHASE_CORE, CombatChallenge.Type.DAMAGE,
				Arrays.asList(new Object[]{REVAN_CORE}),
				"target_guid IN (?)");
		}
	}
}
