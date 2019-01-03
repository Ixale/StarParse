package com.ixale.starparse.service;

import java.util.List;
import java.util.Set;

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

public interface EventService {

	void addListener(EventServiceListener l);

	void storeCombatLog(CombatLog combatLog) throws Exception;

	void flushEvents(List<Event> events, 
			List<Combat> combats, Combat currentCombat,
			List<Effect> effects, List<Effect> currentEffects,
			List<Phase> phases, Phase currentPhase,
			List<Absorption> absorptions) throws Exception;

	CombatLog getCurrentCombatLog() throws Exception;

	Combat getLastCombat() throws Exception;

	Combat findCombat(int combatId) throws Exception;

	List<Combat> getCombats() throws Exception;

	CombatStats getCombatStats(Combat combat, CombatSelection combatSel) throws Exception;

	CombatStats getCombatStats(List<Combat> combat, CombatSelection combatSel) throws Exception;

	List<CombatTickStats> getCombatTicks(Combat combat, CombatSelection combatSel) throws Exception;

	List<Actor> getCombatActors(Combat combat, Actor.Role role, CombatSelection combatSel) throws Exception;

	List<Event> getCombatEvents(Combat combat, Set<Event.Type> filterFlags,
			Actor filterSource, Actor filterTarget, String filterSearch,
			CombatSelection combatSel) throws Exception;

	List<Effect> getCombatEffects(Combat combat, CombatSelection combatSel) throws Exception;

	List<Phase> getCombatPhases(Combat combat) throws Exception;

	List<DamageDealtStats> getDamageDealtStatsSimple(Combat combat, CombatSelection combatSel) throws Exception;

	List<DamageDealtStats> getDamageDealtStats(Combat combat, boolean byTargetType, boolean byTargetInstance, boolean byAbility, CombatSelection combatSel) throws Exception;

	List<HealingDoneStats> getHealingDoneStats(Combat combat, boolean byTarget, boolean byAbility, CombatSelection combatSel) throws Exception;

	CombatMitigationStats getCombatMitigationStats(Combat combat, CombatSelection combatSel) throws Exception;
	
	List<DamageTakenStats> getDamageTakenStats(Combat combat, boolean bySourceType, boolean bySourceInstance, boolean byAbility, CombatSelection combatSel) throws Exception;

	List<HealingTakenStats> getHealingTakenStats(Combat combat, boolean bySource, boolean byAbility, CombatSelection combatSel) throws Exception;

	List<AbsorptionStats> getAbsorptionStats(Combat combat, CombatSelection combatSel) throws Exception;

	List<ChallengeStats> getCombatChallengeStats(Combat combat, CombatSelection combatSel) throws Exception;

	void resetAll() throws Exception;
}
