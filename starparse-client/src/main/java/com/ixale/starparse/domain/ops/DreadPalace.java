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
import com.ixale.starparse.parser.Helpers;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;

public class DreadPalace extends Raid {

	private static final long RAPTUS_BOSS_SM_8M = 3273950490525696L,
		RAPTUS_BOSS_SM_16M = 3303555700097024L,
		RAPTUS_BOSS_HM_8M = 3302902865068032L,
		RAPTUS_BOSS_HM_16M = 3303559995064320L,
		RAPTUS_CURSED_CAPTIVE_NiM = 3355490444640256L,
		RAPTUS_CURSED_CAPTIVE_HM = 3289511157039104L,
		RAPTUS_DYING_CAPTIVE = 3289335063379968L,
		RAPTUS_DOOMED_CAPTIVE = 3289498272137216L,
		// Council
		COUNCIL_BESTIA = 3273984850264064L,
		COUNCIL_TYRANS = 3273997735165952L,
		COUNCIL_CALPHAYUS = 3273989145231360L,
		COUNCIL_RAPTUS = 3273993440198656L,
		COUNCIL_BRONTES = 3274019210002432L,
		COUNCIL_STYRAK_DRAGON = 3303830578003968L,
		COUNCIL_STYRAK_MANIFESTATION = 3355541984247808L;

	private static final String BESTIA_PHASE_BEFORE = "Before",
		BESTIA_PHASE_BESTIA = "Bestia",
		BESTIA_PHASE_BURN = "Burn",

	RAPTUS_PHASE_NORMAL = "Normal",
		RAPTUS_PHASE_DPS = "DPS",
		RAPTUS_PHASE_HEAL = "Heal",
		RAPTUS_PHASE_TANK = "Tank",

	COUNCIL_PHASE_FIRST = "First",
		COUNCIL_PHASE_SECOND = "Second",
		COUNCIL_PHASE_THIRD = "Third",
		COUNCIL_PHASE_FOURTH = "Burn";

	private static final int RAPTUS_PHASE_CHALLENGE_LENGTH = 21 * 1000,
		COUNCIL_PHASE_SECOND_LENGTH = (140 + 10) * 1000;

	private static final int BESTIA_TIMER_BOSS_ACTIVATES = 122 * 1000, // 2:01
		BESTIA_TIMER_LAST_MONSTER = 241 * 1000, // 6:02
		BESTIA_TIMER_SOFT_ENRAGE = 150 * 1000; // 8:33

	public DreadPalace() {
		super("Dread Palace");

//		RaidBoss.add(this, RaidBossName.Bestia,
//			new long[]{3273941900591104L}, // SM 8m
//			new long[]{3303585764868096L}, // SM 16m
//			new long[]{3303581469900800L}, // HM 8m
//			new long[]{3303590059835392L}); // HM 16m

		final RaidBoss bestia16n = new RaidBoss(this, RaidBossName.Bestia, Mode.NiM, Size.Sixteen, null);
		final RaidBoss bestia8n = new RaidBoss(this, RaidBossName.Bestia, Mode.NiM, Size.Eight, null);

		final RaidBoss bestia16h = new RaidBoss(this, RaidBossName.Bestia, Mode.HM, Size.Sixteen,
			new long[]{3313343930564608L}); // Loot

		final RaidBoss bestia8h = new RaidBoss(this, RaidBossName.Bestia, Mode.HM, Size.Eight,
			new long[]{3313339635597312L}); // Loot

		final RaidBoss bestia16s = new RaidBoss(this, RaidBossName.Bestia, Mode.SM, Size.Sixteen,
			new long[]{3313335340630016L}); // Loot

		final RaidBoss bestia8s = new RaidBoss(this, RaidBossName.Bestia, Mode.SM, Size.Eight,
			new long[]{3313331045662720L}, // Loot
			new long[]{3273941900591104L, 3292083842449408L, 3291675820556288L, 3292079547482112L}, // Bestia, Tentacle, Monster, Larva
			new RaidBoss.BossUpgrade(new RaidBoss[]{bestia16s, bestia8h, bestia16h, bestia8n, bestia16n}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					if (guid == 3294098182111232L) { // Swelling Despair
						return bestia8h;
					}
					return super.upgradeByAbility(guid, effectGuid, value);
				}
			});

		bosses.add(bestia8s);
		bosses.add(bestia16s);
		bosses.add(bestia8h);
		bosses.add(bestia16h);
		bosses.add(bestia8n);
		bosses.add(bestia16n);

//		RaidBoss.add(this, RaidBossName.Tyrans,
//			new long[]{3273954785492992L},
//			new long[]{3303409671208960L},
//			new long[]{3303413966176256L},
//			new long[]{3303418261143552L});

		final RaidBoss tyrans16n = new RaidBoss(this, RaidBossName.Tyrans, Mode.NiM, Size.Sixteen, null);
		final RaidBoss tyrans8n = new RaidBoss(this, RaidBossName.Tyrans, Mode.NiM, Size.Eight, null);

		final RaidBoss tyrans16h = new RaidBoss(this, RaidBossName.Tyrans, Mode.HM, Size.Sixteen,
			new long[]{3312854304292864L}); // Loot

		final RaidBoss tyrans8h = new RaidBoss(this, RaidBossName.Tyrans, Mode.HM, Size.Eight,
			new long[]{3312845714358272L}); // Loot

		final RaidBoss tyrans16s = new RaidBoss(this, RaidBossName.Tyrans, Mode.SM, Size.Sixteen,
			new long[]{3312850009325568L}); // Loot

		final RaidBoss tyrans8s = new RaidBoss(this, RaidBossName.Tyrans, Mode.SM, Size.Eight,
			new long[]{3312841419390976L}, // Loot
			new long[]{3273954785492992L}, // Tyrans
			new RaidBoss.BossUpgrade(new RaidBoss[]{tyrans16s, tyrans8h, tyrans16h, tyrans8n, tyrans16n}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					if (guid == 3349606339444736L) { // Simplification
						return tyrans8n;
					}
					return super.upgradeByAbility(guid, effectGuid, value);
				}
			});

		bosses.add(tyrans8s);
		bosses.add(tyrans16s);
		bosses.add(tyrans8h);
		bosses.add(tyrans16h);
		bosses.add(tyrans8n);
		bosses.add(tyrans16n);

		final RaidBoss calph16n = new RaidBoss(this, RaidBossName.Calphayus, Mode.NiM, Size.Sixteen, null);
		final RaidBoss calph8n = new RaidBoss(this, RaidBossName.Calphayus, Mode.NiM, Size.Eight, null);

		final RaidBoss calph16h = new RaidBoss(this, RaidBossName.Calphayus, Mode.HM, Size.Sixteen,
			new long[]{3312996038213632L}); // Loot

		final RaidBoss calph8h = new RaidBoss(this, RaidBossName.Calphayus, Mode.HM, Size.Eight,
			new long[]{3312987448279040L}); // Loot

		final RaidBoss calph16s = new RaidBoss(this, RaidBossName.Calphayus, Mode.SM, Size.Sixteen,
			new long[]{3312991743246336L}); // Loot

		final RaidBoss calph8s = new RaidBoss(this, RaidBossName.Calphayus, Mode.SM, Size.Eight,
			new long[]{3312983153311744L}, // Loot
			new long[]{3284949901770752L, 3284954196738048L, 3273946195558400L, 3349812497874944L}, // Calphayus (various versions)
			new RaidBoss.BossUpgrade(new RaidBoss[]{calph16s, calph8h, calph16h, calph8n, calph16n}) {
				@Override
				public RaidBoss upgradeByAbility(long guid, long effectGuid, Integer value) {
					if (guid == 3303504160489472L && effectGuid == 3303504160489753L) { // Lasting Distortions ?
						return calph8n;
					}
					return super.upgradeByAbility(guid, effectGuid, value);
				}
				@Override
				public RaidBoss upgradeByNpc(long npcGuid) {
					if (npcGuid == 3349915577090048L || npcGuid == 3350598476890112L) { // Energy Sphere, Guardian of Knowledge
						return calph8n;
					}
					return super.upgradeByNpc(npcGuid);
				}
			});

		bosses.add(calph8s);
		bosses.add(calph16s);
		bosses.add(calph8h);
		bosses.add(calph16h);
		bosses.add(calph8n);
		bosses.add(calph16n);

		RaidBoss.add(this, RaidBossName.Raptus,
			new long[]{RAPTUS_BOSS_SM_8M},
			new long[]{RAPTUS_BOSS_SM_16M},
			new long[]{RAPTUS_BOSS_HM_8M},
			new long[]{RAPTUS_BOSS_HM_16M},
			new BossUpgradeCallback() {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value, final RaidBoss nimBoss) {
					if ((guid == 3289309293576192L && effectGuid == 3289309293576476L) // Curse of Enfeeblement
						|| (guid == 3289524041940992L && effectGuid == 3289524041941334L) // Curse of Proximity
						|| guid == 3356173344440320L) { // Spinning Attack
						return nimBoss;
					}
					return null;
				}
				@Override
				public RaidBoss upgradeByNpc(long guid, RaidBoss nimBoss) {
					if (guid == RAPTUS_CURSED_CAPTIVE_NiM) {
						return nimBoss;
					}
					return null;
				}
			});

		final RaidBoss council16n = new RaidBoss(this, RaidBossName.Council, Mode.NiM, Size.Sixteen, null);
		final RaidBoss council8n = new RaidBoss(this, RaidBossName.Council, Mode.NiM, Size.Eight, null);

		final RaidBoss council16h = new RaidBoss(this, RaidBossName.Council, Mode.HM, Size.Sixteen,
			new long[]{3303431146045440L}); // Loot

		final RaidBoss council8h = new RaidBoss(this, RaidBossName.Council, Mode.HM, Size.Eight,
			new long[]{3303422556110848L}, // Loot
			null,
			new RaidBoss.BossUpgrade(new RaidBoss[]{council16h, council8n, council16n}) {
				@Override
				public RaidBoss upgradeByAbility(long guid, long effectGuid, Integer value) {
					if (guid == 3301257892593664L && effectGuid == 3301257892593933L) { // Bestia: Deadly Weakness
						return council8n;
					}
					return super.upgradeByAbility(guid, effectGuid, value);
				}
				@Override
				public RaidBoss upgradeByNpc(long npcGuid) {
					if (npcGuid == COUNCIL_STYRAK_MANIFESTATION) {
						return council8n;
					}
					return super.upgradeByNpc(npcGuid);
				}
			});

		final RaidBoss council16s = new RaidBoss(this, RaidBossName.Council, Mode.SM, Size.Sixteen,
			new long[]{3303426851078144L}); // Loot

		final RaidBoss council8s = new RaidBoss(this, RaidBossName.Council, Mode.SM, Size.Eight,
			new long[]{3302645167030272L}, // Loot
			new long[]{COUNCIL_BESTIA, COUNCIL_TYRANS, COUNCIL_CALPHAYUS, COUNCIL_RAPTUS, COUNCIL_BRONTES, COUNCIL_STYRAK_DRAGON},
			new RaidBoss.BossUpgrade(new RaidBoss[]{council16s, council8h, council16h, council8n, council16n}) {
				@Override
				public RaidBoss upgradeByAbility(long guid, long effectGuid, Integer value) {
					if (guid == 3301257892593664L && effectGuid == 3301257892593933L) { // Bestia: Deadly Weakness
						return council8n;
					}
					if (guid == 3302258619973632L || guid == 3322475031035904L) { // Force Carry, Power of the Masters
						return council8h;
					}
					return super.upgradeByAbility(guid, effectGuid, value);
				}
				@Override
				public RaidBoss upgradeByNpc(long npcGuid) {
					if (npcGuid == COUNCIL_STYRAK_MANIFESTATION) {
						return council8n;
					}
					return super.upgradeByNpc(npcGuid);
				}
			});

		bosses.add(council8s);
		bosses.add(council16s);
		bosses.add(council8h);
		bosses.add(council16h);
		bosses.add(council8n);
		bosses.add(council16n);

		addChallenge(RaidBossName.Raptus, new RaptusDpsChallenge());
		addChallenge(RaidBossName.Raptus, new RaptusHealingChallenge());
		addChallenge(RaidBossName.Council, new CouncilPhase2Challenge());
	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {

		if (c.getBoss() == null) {
			return null;
		}

		switch (c.getBoss().getRaidBossName()) {
			case Bestia:
				return getNewPhaseNameForBestia(e, c, currentPhaseName);
			case Tyrans:
				return getNewPhaseNameForTyrans(e, c, currentPhaseName);
			case Raptus:
				return getNewPhaseNameForRaptus(e, currentPhaseName);
			case Council:
				return getNewPhaseNameForCouncil(e, c, currentPhaseName);
			default:
				return null;
		}
	}

	private String getNewPhaseNameForBestia(final Event e, final Combat c, final String currentPhaseName) {

		// ------------------ Timers ------------------
		
		if (Helpers.isAbilityEqual(e, 3294098182111232L)) {		// Swelling Despair
			TimerManager.stopTimer(BestiaDespairTimer.class);
			TimerManager.startTimer(BestiaDespairTimer.class, e.getTimestamp());
		}
		
		if (Helpers.isTargetOtherPlayer(e)) return null;	// returns if target is other player
		
		if (Helpers.isAbilityEqual(e, 3302993059381248L)) {		// Pulverize
			TimerManager.stopTimer(BestiaPulverizeTimer.class);
			TimerManager.startTimer(BestiaPulverizeTimer.class, e.getTimestamp());
		}
		
		// ------------------ Phases ------------------
				
		if (BESTIA_PHASE_BESTIA.equals(currentPhaseName) && phaseTimers.get(BESTIA_PHASE_BURN) <= e.getTimestamp()) {

			TimerManager.stopTimer(BestiaLastMonsterTimer.class); // should be done anyway
			TimerManager.startTimer(BestiaSoftEnrageTimer.class, phaseTimers.get(BESTIA_PHASE_BURN));

			phaseTimers.remove(BESTIA_PHASE_BURN);

			return BESTIA_PHASE_BURN;
		}

		if (BESTIA_PHASE_BEFORE.equals(currentPhaseName) && phaseTimers.get(BESTIA_PHASE_BESTIA) <= e.getTimestamp()) {

			TimerManager.stopTimer(BestiaBossActivatesTimer.class); // should be done anyway
			TimerManager.startTimer(BestiaLastMonsterTimer.class, phaseTimers.get(BESTIA_PHASE_BESTIA));

			phaseTimers.put(BESTIA_PHASE_BURN, phaseTimers.get(BESTIA_PHASE_BESTIA) + BESTIA_TIMER_LAST_MONSTER);
			phaseTimers.remove(BESTIA_PHASE_BESTIA);

			return BESTIA_PHASE_BESTIA;
		}

		if (currentPhaseName == null) {

			TimerManager.startTimer(BestiaBossActivatesTimer.class, c.getTimeFrom());

			phaseTimers.clear();
			phaseTimers.put(BESTIA_PHASE_BESTIA, c.getTimeFrom() + BESTIA_TIMER_BOSS_ACTIVATES);

			return BESTIA_PHASE_BEFORE;
		}

		return null;
	}
	
	private String getNewPhaseNameForTyrans(final Event e, final Combat c, final String currentPhaseName) {
		
		// ------------------ Timers ------------------
		
		if (Helpers.isAbilityEqual(e, 3279619847356416L)) {		// Thundering Blast
			TimerManager.stopTimer(TyransThunderingBlastTimer.class);
			TimerManager.startTimer(TyransThunderingBlastTimer.class, e.getTimestamp());
		}
		
		if (Helpers.isTargetOtherPlayer(e)) return null;	// returns if target is other player
		
		if (TimerManager.getTimer(TyransThunderingBlastTimer.class) != null && Helpers.isActionApply(e) && Helpers.isEffectEqual(e, 3318510776221696L)) {		// Inferno
			TimerManager.stopTimer(TyransThunderingBlastTimer.class);
			TimerManager.startTimer(TyransThunderingBlastTimer.class, e.getTimestamp() - 6000);	// start new timer after inferno effect
		}
		
		return null;
	}

	private String getNewPhaseNameForRaptus(final Event e, final String currentPhaseName) {
		if (Helpers.isTargetOtherPlayer(e)) return null;	// returns if target is other player
		
		if (!RAPTUS_PHASE_DPS.equals(currentPhaseName)
			&& (Helpers.isTargetEqual(e, RAPTUS_CURSED_CAPTIVE_HM) || Helpers.isSourceEqual(e, RAPTUS_CURSED_CAPTIVE_HM)
				|| Helpers.isTargetEqual(e, RAPTUS_CURSED_CAPTIVE_NiM) || Helpers.isSourceEqual(e, RAPTUS_CURSED_CAPTIVE_NiM))
			&& e.getValue() != null) {
			phaseTimers.put(RAPTUS_PHASE_NORMAL, e.getTimestamp() + RAPTUS_PHASE_CHALLENGE_LENGTH);
			return RAPTUS_PHASE_DPS;
		}

		if (!RAPTUS_PHASE_HEAL.equals(currentPhaseName)
			&& ((Helpers.isTargetEqual(e, RAPTUS_DYING_CAPTIVE) && e.getValue() != null)
				|| (Helpers.isSourceEqual(e, RAPTUS_DYING_CAPTIVE) && Helpers.isEffectEqual(e, 3289309293576476L)))) { // curse
			phaseTimers.put(RAPTUS_PHASE_NORMAL, e.getTimestamp() + RAPTUS_PHASE_CHALLENGE_LENGTH);
			return RAPTUS_PHASE_HEAL;
		}

		if (!RAPTUS_PHASE_TANK.equals(currentPhaseName)
			&& (Helpers.isTargetEqual(e, RAPTUS_DOOMED_CAPTIVE) || Helpers.isSourceEqual(e, RAPTUS_DOOMED_CAPTIVE))
			&& e.getValue() != null) {
			phaseTimers.put(RAPTUS_PHASE_NORMAL, e.getTimestamp() + RAPTUS_PHASE_CHALLENGE_LENGTH);
			return RAPTUS_PHASE_TANK;
		}

		if ((RAPTUS_PHASE_HEAL.equals(currentPhaseName)
			|| RAPTUS_PHASE_DPS.equals(currentPhaseName)
			|| RAPTUS_PHASE_TANK.equals(currentPhaseName))
			&& (Helpers.isSourceWithin(e, RAPTUS_BOSS_SM_8M, RAPTUS_BOSS_SM_16M, RAPTUS_BOSS_HM_8M, RAPTUS_BOSS_HM_16M)
				|| phaseTimers.get(RAPTUS_PHASE_NORMAL) < e.getTimestamp())) {
			phaseTimers.remove(RAPTUS_PHASE_NORMAL);
			return RAPTUS_PHASE_NORMAL;
		}

		if (currentPhaseName == null) {
			phaseTimers.clear();
			return RAPTUS_PHASE_NORMAL;
		}

		return null;
	}

	private String getNewPhaseNameForCouncil(final Event e, final Combat c, final String currentPhaseName) {

		// ------------------ Timers ------------------
		
		if (Helpers.isAbilityEqual(e, 3301257892593664L)) {		// Bestia Kick
			TimerManager.stopTimer(CouncilBestiaKickTimer.class);
			TimerManager.startTimer(CouncilBestiaKickTimer.class, e.getTimestamp());
		}
		
		if (Helpers.isAbilityEqual(e, 3317217991065600L)) {		// Calphayus Crystal Projection
			TimerManager.stopTimer(CouncilCalphayusCrystalsTimer.class);
			TimerManager.startTimer(CouncilCalphayusCrystalsTimer.class, e.getTimestamp());
		}
		
		if (Helpers.isTargetOtherPlayer(e)) return null;	// returns if target is other player
		
		// ------------------ Phases ------------------
		
		if (COUNCIL_PHASE_FOURTH.equals(currentPhaseName)) {
			// nothing else
			return null;
		}

		if (COUNCIL_PHASE_THIRD.equals(currentPhaseName)
			&& Helpers.isTargetThisPlayer(e)
			&& (Helpers.isAbilityEqual(e, 3322475031035904L) || Helpers.isAbilityEqual(e, 3317359724986368L))) { // Power of the Masters (HM, SM)

			// setup timers
			TimerManager.stopTimer(CouncilTyransDmP3Timer.class);
			TimerManager.stopTimer(CouncilTyransTpP3Timer.class);

			return COUNCIL_PHASE_FOURTH;
		}

		if (COUNCIL_PHASE_SECOND.equals(currentPhaseName)
			&& phaseTimers.get(COUNCIL_PHASE_THIRD) < e.getTimestamp()) {

			// setup timers
			TimerManager.stopTimer(CouncilBrontesTpTimer.class);

			TimerManager.startTimer(CouncilTyransDmP3Timer.class, phaseTimers.get(COUNCIL_PHASE_THIRD));
			TimerManager.startTimer(CouncilTyransTpP3Timer.class, phaseTimers.get(COUNCIL_PHASE_THIRD));

			phaseTimers.remove(COUNCIL_PHASE_THIRD);

			return COUNCIL_PHASE_THIRD;
		}

		if (!COUNCIL_PHASE_SECOND.equals(currentPhaseName)
			&& !COUNCIL_PHASE_THIRD.equals(currentPhaseName)
			&& (Helpers.isTargetEqual(e, COUNCIL_BRONTES) || Helpers.isSourceEqual(e, COUNCIL_BRONTES)
				|| Helpers.isTargetEqual(e, COUNCIL_STYRAK_DRAGON) || Helpers.isSourceEqual(e, COUNCIL_STYRAK_DRAGON))
			&& (e.getValue() != null || Helpers.isAbilityEqual(e, 2995606544973824L))) { // threat modifier
			// Brontes & Styrak appears
			phaseTimers.put(COUNCIL_PHASE_THIRD, e.getTimestamp() + COUNCIL_PHASE_SECOND_LENGTH);

			// setup timers
			TimerManager.stopTimer(CouncilTyransDmP1Timer.class);
			TimerManager.stopTimer(CouncilTyransTpP1Timer.class);

			TimerManager.startTimer(CouncilBrontesTpTimer.class, e.getTimestamp());

			return COUNCIL_PHASE_SECOND;
		}

		if (currentPhaseName == null) {
			phaseTimers.clear();

			// setup timers
			TimerManager.startTimer(CouncilTyransDmP1Timer.class, c.getTimeFrom());
			TimerManager.startTimer(CouncilTyransTpP1Timer.class, c.getTimeFrom());

			return COUNCIL_PHASE_FIRST;
		}

		return null;
	}

	/**
	 * Timers
	 * ------------------------------------------------------------------------
	 */

	public static class BestiaBossActivatesTimer extends BaseTimer {
		public BestiaBossActivatesTimer() {
			super("Bestia", "Bestia Arrives", BESTIA_TIMER_BOSS_ACTIVATES);
		}
	}

	public static class BestiaLastMonsterTimer extends BaseTimer {
		public BestiaLastMonsterTimer() {
			super("Last Monster", "Bestia Last Monster", BESTIA_TIMER_LAST_MONSTER);
		}
	}

	public static class BestiaSoftEnrageTimer extends BaseTimer {
		public BestiaSoftEnrageTimer() {
			super("Soft Enrage", "Bestia Soft Enrage", BESTIA_TIMER_SOFT_ENRAGE);
		}
	}
	
	public static class BestiaPulverizeTimer extends BaseTimer {
		public BestiaPulverizeTimer() {
			super("Pulverize", "Bestia Pulverize", 12000);
			setColor(0);
		}
	}
	
	public static class BestiaDespairTimer extends BaseTimer {
		public BestiaDespairTimer() {
			super("Despair", "Bestia Despair", 25000);
			setColor(0);
		}
	}

	public static class TyransThunderingBlastTimer extends BaseTimer {
		public TyransThunderingBlastTimer() {
			super("Thundering Blast", "Tyrans Thundering Blast", 8000);
			setColor(0);
		}
	}
	
	public static class CouncilTyransDmP1Timer extends BaseTimer {
		public CouncilTyransDmP1Timer() {
			this(29 * 1000);
		}

		public CouncilTyransDmP1Timer(Integer delay) {
			super("Tyrans DM", "Council Deathmark", delay, 63 * 1000);
			setColor(0);
		}
	}

	public static class CouncilTyransDmP3Timer extends CouncilTyransDmP1Timer {
		public CouncilTyransDmP3Timer() {
			super(24 * 1000 + 500);
		}
	}

	public static class CouncilTyransTpP1Timer extends BaseTimer {
		public CouncilTyransTpP1Timer() {
			this(12 * 1000);
		}

		public CouncilTyransTpP1Timer(Integer delay) {
			super("Tyrans TP", "Council Tyrans TP", delay, 31 * 1000 + 250);
			setColor(1);
		}
	}

	public static class CouncilTyransTpP3Timer extends CouncilTyransTpP1Timer {
		public CouncilTyransTpP3Timer() {
			super(13 * 1000);
		}
	}

	public static class CouncilBrontesTpTimer extends BaseTimer {
		public CouncilBrontesTpTimer() {
			super("Brontes TP", "Council Brontes TP", 12 * 1000 + 250, 20 * 1000 + 500);
			setColor(1);
		}
	}
	
	public static class CouncilBestiaKickTimer extends BaseTimer {
		public CouncilBestiaKickTimer() {
			super("Kick", "Council Bestia Kick", 20000);
			setColor(0);
		}
	}
	
	public static class CouncilCalphayusCrystalsTimer extends BaseTimer {
		public CouncilCalphayusCrystalsTimer() {
			super("Crystals", "Council Calphayus Crystals", 10000);
			setColor(0);
		}
	}

	/**
	 * Challenges
	 * ------------------------------------------------------------------------
	 */

	public static class RaptusHealingChallenge extends CombatChallenge {
		public RaptusHealingChallenge() {
			super(RaidChallengeName.RaptusHealing,
				RAPTUS_PHASE_HEAL, CombatChallenge.Type.HEALING,
				Arrays.asList(new Object[]{RAPTUS_DYING_CAPTIVE}),
				"target_guid IN (?)");
		}
	}

	public static class RaptusDpsChallenge extends CombatChallenge {
		public RaptusDpsChallenge() {
			super(RaidChallengeName.RaptusDamage,
				RAPTUS_PHASE_DPS, CombatChallenge.Type.DAMAGE,
				Arrays.asList(new Object[]{RAPTUS_CURSED_CAPTIVE_HM, RAPTUS_CURSED_CAPTIVE_NiM}),
				"target_guid IN (?, ?)");
		}
	}

	public static class CouncilPhase2Challenge extends CombatChallenge {
		public CouncilPhase2Challenge() {
			super(RaidChallengeName.CouncilP2,
				COUNCIL_PHASE_SECOND, CombatChallenge.Type.DAMAGE,
				Arrays.asList(new Object[]{COUNCIL_BRONTES, COUNCIL_STYRAK_DRAGON, COUNCIL_STYRAK_MANIFESTATION}),
				"target_guid IN (?, ?, ?)");
		}
	}
}
