package com.ixale.starparse.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ixale.starparse.domain.Absorption;
import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatLog;
import com.ixale.starparse.domain.CombatSelection;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Phase;
import com.ixale.starparse.domain.stats.AbsorptionStats;
import com.ixale.starparse.domain.stats.ChallengeStats;
import com.ixale.starparse.domain.stats.CombatMitigationStats;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.domain.stats.CombatTickStats;
import com.ixale.starparse.domain.stats.DamageDealtStats;
import com.ixale.starparse.domain.stats.DamageTakenStats;
import com.ixale.starparse.domain.stats.HealingDoneStats;
import com.ixale.starparse.domain.stats.HealingTakenStats;
import com.ixale.starparse.service.EventService;
import com.ixale.starparse.service.EventServiceListener;
import com.ixale.starparse.service.dao.AbsorptionDao;
import com.ixale.starparse.service.dao.CombatDao;
import com.ixale.starparse.service.dao.CombatLogDao;
import com.ixale.starparse.service.dao.EffectDao;
import com.ixale.starparse.service.dao.EventDao;
import com.ixale.starparse.service.dao.PhaseDao;

@Service("eventService")
public class EventServiceImpl implements EventService {

	private CombatLogDao combatLogDao;
	private EventDao eventDao;
	private CombatDao combatDao;
	private EffectDao effectDao;
	private AbsorptionDao absorptionDao;
	private PhaseDao phaseDao;

	private Context context;

	final private ArrayList<EventServiceListener> listeners = new ArrayList<EventServiceListener>();

	private Integer lastCombatId = null;
	private Integer lastLogId = null;

	@Autowired
	public void setCombatLogDao(CombatLogDao combatLogDao) {
		this.combatLogDao = combatLogDao;
	}

	@Autowired
	public void setEventDao(EventDao eventDao) {
		this.eventDao = eventDao;
	}

	@Autowired
	public void setCombatDao(CombatDao combatDao) {
		this.combatDao = combatDao;
	}

	@Autowired
	public void setEffectDao(EffectDao effectDao) {
		this.effectDao = effectDao;
	}

	@Autowired
	public void setAbsorptionDao(AbsorptionDao absorptionDao) {
		this.absorptionDao = absorptionDao;
	}

	@Autowired
	public void setPhaseDao(PhaseDao phaseDao) {
		this.phaseDao = phaseDao;
	}

	@Autowired
	public void setContext(Context context) {
		this.context = context;
	}

	public void addListener(EventServiceListener l) {
		listeners.add(l);
	}

	@Override
	public void storeCombatLog(CombatLog combatLog) throws Exception {
		// store
		combatLogDao.storeCombatLog(combatLog);

		if (lastLogId == null || !lastLogId.equals(combatLog.getLogId())) {
			for (EventServiceListener l: listeners) {
				l.onNewFile();
			}
			lastLogId = combatLog.getLogId();
		}
	}

	@Override
	public void flushEvents(final List<Event> events,
		final List<Combat> combats, final Combat currentCombat,
		final List<Effect> effects, final List<Effect> currentEffects,
		final List<Phase> phases, final Phase currentPhase,
		final List<Absorption> absorptions) throws Exception {

		// store everything
		eventDao.storeEvents(events);
		effectDao.storeEffects(effects, currentEffects);
		combatDao.storeCombats(combats, currentCombat);
		absorptionDao.storeAbsorptions(absorptions);
		phaseDao.storePhases(phases, currentPhase);

		// fire events
		if (currentCombat != null) {
			if (lastCombatId == null || !lastCombatId.equals(currentCombat.getCombatId())) {
				for (EventServiceListener l: listeners) {
					l.onNewCombat();
				}
				lastCombatId = currentCombat.getCombatId();
			}

		} else if (combats.size() > 0) {
			for (EventServiceListener l: listeners) {
				if (lastCombatId == null || !lastCombatId.equals(combats.get(combats.size() - 1).getCombatId())) {
					l.onNewCombat();
				}
			}
			lastCombatId = combats.get(combats.size() - 1).getCombatId();
		}

		if (events != null && !events.isEmpty()) {
			for (EventServiceListener l: listeners) {
				l.onNewEvents();
			}
		}

		// stored, reset buffers
		if (events != null) {
			events.clear();
		}
		combats.clear();
		effects.clear();
		absorptions.clear();
		phases.clear();
	}

	@Override
	public CombatLog getCurrentCombatLog() throws Exception {
		return combatLogDao.getCurrentCombatLog();
	}

	@Override
	public List<Combat> getCombats() throws Exception {
		return combatDao.getCombats();
	}

	@Override
	public Combat getLastCombat() throws Exception {
		return combatDao.getLastCombat();
	}

	@Override
	public Combat findCombat(int combatId) throws Exception {
		return combatDao.findCombat(combatId);
	}

	@Override
	public CombatStats getCombatStats(final Combat combat, final CombatSelection combatSel) throws Exception {
		return combatDao.getCombatStats(combat, combatSel);
	}

	@Override
	public CombatStats getCombatStats(final List<Combat> combats, final CombatSelection combatSel) throws Exception {
		int tick = 0;
		int actions = 0;
		int damage = 0;
		int heal = 0;
		int effectiveHeal = 0;
		int damageTaken = 0;
		int damageTakenTotal = 0;
		int absorbed = 0;
		int absorbedTotal = 0;
		int healTaken = 0;
		int effectiveHealTaken = 0;
		int effectiveHealTakenTotal = 0;
		int threat = 0;
		int threatPositive = 0;

		for (final Combat combat: combats) {
			if (combat == null) {
				continue;
			}
			final CombatStats cs = combatDao.getCombatStats(combat, combatSel);
			tick += cs.getTick();
			actions += cs.getActions();
			damage += cs.getDamage();
			heal += cs.getHeal();
			effectiveHeal += cs.getEffectiveHeal();

			damageTaken += cs.getDamageTaken();
			damageTakenTotal += cs.getDamageTakenTotal();

			absorbed += cs.getAbsorbed();
			absorbedTotal += cs.getAbsorbedTotal();

			healTaken += cs.getHealTaken();
			effectiveHealTaken += cs.getEffectiveHealTaken();
			effectiveHealTakenTotal += cs.getEffectiveHealTakenTotal();
			threat += cs.getThreat();
			threatPositive += cs.getThreatPositive();
		}

		return new CombatStats(tick, actions, damage, heal, effectiveHeal,
			damageTaken, damageTakenTotal, absorbed, absorbedTotal,
			healTaken, effectiveHealTaken, effectiveHealTakenTotal,
			threat, threatPositive);
	}

	@Override
	public List<Actor> getCombatActors(final Combat combat, final Actor.Role role, final CombatSelection combatSel) throws Exception {
		return combatDao.getCombatActors(combat, role, combatSel);
	}

	@Override
	public List<Event> getCombatEvents(final Combat combat, final Set<Event.Type> filterFlags,
		final Actor filterSource, final Actor filterTarget, final String filterSearch,
		final CombatSelection combatSel) throws Exception {
		return combatDao.getCombatEvents(combat, filterFlags, filterSource, filterTarget, filterSearch, combatSel);
	}

	@Override
	public List<CombatTickStats> getCombatTicks(final Combat combat, final CombatSelection combatSel) throws Exception {
		return combatDao.getCombatTicks(combat, combatSel);
	}

	@Override
	public List<Effect> getCombatEffects(final Combat combat, final CombatSelection combatSel) throws Exception {
		return combatDao.getCombatEffects(combat, combatSel);
	}

	@Override
	public List<Phase> getCombatPhases(final Combat combat) throws Exception {
		return phaseDao.getCombatPhases(combat);
	}

	@Override
	public List<DamageDealtStats> getDamageDealtStatsSimple(final Combat combat, final CombatSelection combatSel) throws Exception {
		return combatDao.getDamageDealtStatsSimple(combat, combatSel);
	}

	@Override
	public List<DamageDealtStats> getDamageDealtStats(final Combat combat, boolean byTargetType, boolean byTargetInstance, boolean byAbility,
		final CombatSelection combatSel) throws Exception {
		return combatDao.getDamageDealtStats(combat, byTargetType, byTargetInstance, byAbility, combatSel);
	}

	@Override
	public List<HealingDoneStats> getHealingDoneStats(Combat combat, boolean byTarget, boolean byAbility, final CombatSelection combatSel)
		throws Exception {
		return combatDao.getHealingDoneStats(combat, byTarget, byAbility, combatSel);
	}

	@Override
	public CombatMitigationStats getCombatMitigationStats(Combat combat, final CombatSelection combatSel) throws Exception {
		return combatDao.getCombatMitigationStats(combat, combatSel);
	}

	@Override
	public List<DamageTakenStats> getDamageTakenStats(Combat combat, boolean bySourceType, boolean bySourceInstance, boolean byAbility,
		final CombatSelection combatSel) throws Exception {
		return combatDao.getDamageTakenStats(combat, bySourceType, bySourceInstance, byAbility, combatSel);
	}

	@Override
	public List<HealingTakenStats> getHealingTakenStats(final Combat combat, boolean bySource, boolean byAbility, final CombatSelection combatSel)
		throws Exception {
		return combatDao.getHealingTakenStats(combat, bySource, byAbility, combatSel);
	}

	@Override
	public List<AbsorptionStats> getAbsorptionStats(Combat combat, CombatSelection combatSel) throws Exception {
		return combatDao.getAbsorptionStats(combat, combatSel);
	}

	@Override
	public List<ChallengeStats> getCombatChallengeStats(final Combat combat, final CombatSelection combatSel) throws Exception {
		return combatDao.getCombatChallengeStats(combat, combatSel);
	}

	@Override
	public void resetAll() throws Exception {
		phaseDao.reset();
		absorptionDao.reset();
		effectDao.reset();
		eventDao.reset();
		combatDao.reset();
		combatLogDao.reset();
		lastCombatId = lastLogId = null;
		context.reset();
	}
}
