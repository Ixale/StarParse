package com.ixale.starparse.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.Actor.Type;
import com.ixale.starparse.domain.AttackType;
import com.ixale.starparse.domain.CombatSelection;
import com.ixale.starparse.domain.EffectKey;
import com.ixale.starparse.domain.Entity;
import com.ixale.starparse.domain.EntityGuid;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.stats.CombatEventStats;

@Service("context")
public class Context {

	private final Map<Object, Actor> actors = new HashMap<>();
	private final Map<Long, Entity> entities = new HashMap<>();
	private final Map<Long, AttackType> attacks = new HashMap<>();
	private final Map<Integer, List<CombatEventStats>> combatEvents = new HashMap<>();

	private Long tickFrom, tickTo;

	final private LinkedHashMap<EffectKey, ArrayList<Long[]>> effects = new LinkedHashMap<>();

	public Actor getActor(final String name, final Type type, final Long guid, final Long instanceId) {
		if (instanceId == null) {
			return getActor(name, type, guid);
		}
		if (guid == null) {
			return getActor(name, type);
		}
		if (!actors.containsKey(instanceId)) {
			actors.put(instanceId, new Actor(name, type, guid, instanceId));
		}
		return actors.get(instanceId);
	}

	public Actor getActor(final String name, final Type type, final Long guid) {
		if (guid == null) {
			return getActor(name, type);
		}
		if (!actors.containsKey(guid)) {
			actors.put(guid, new Actor(name, type, guid));
		}
		return actors.get(guid);
	}

	public Actor getActor(final String name, final Type type) {
		if (!actors.containsKey(name)) {
			actors.put(name, new Actor(name, type));
		}
		return actors.get(name);
	}

	public Entity getEntity(final String name, final Long guid) {
		if (!entities.containsKey(guid)) {
			entities.put(guid, new Entity(name, guid));
		}
		return entities.get(guid);
	}

	public Map<Object, Actor> getActors() {
		return actors;
	}

	public CombatSelection getCombatSelection() {
		if (tickFrom == null && tickTo == null) {
			return null;
		}
		return new CombatSelection(null, null, tickFrom, tickTo);
	}

	public void setTickFrom(final Long tickFrom) {
		this.tickFrom = tickFrom;
	}

	public void setTickTo(final Long tickTo) {
		this.tickTo = tickTo;
	}

	public Long getTickFrom() {
		return tickFrom;
	}

	public Long getTickTo() {
		return tickTo;
	}

	public void addSelectedEffect(EffectKey effectKey, final ArrayList<Long[]> windows) {
		effects.put(effectKey, windows);
	}

	public LinkedHashMap<EffectKey, ArrayList<Long[]>> getSelectedEffects() {
		return effects;
	}

	public void addAttack(final AttackType type, long guid) {
		attacks.put(guid, type);
	}

	public void addAttacks(final Map<Long, AttackType> attacks) {
		for (final long guid: EntityGuid.MR) {
			attacks.put(guid, AttackType.MR);
		}
		for (final long guid: EntityGuid.FT) {
			attacks.put(guid, AttackType.FT);
		}
		this.attacks.putAll(attacks);
	}

	public Map<Long, AttackType> getAttacks() {
		return attacks;
	}

	public void addCombatEvent(final int combatId, final Event.Type type, final long timestamp) {
		if (!combatEvents.containsKey(combatId)) {
			combatEvents.put(combatId, new ArrayList<CombatEventStats>());
		}
		combatEvents.get(combatId).add(new CombatEventStats(type, timestamp));
	}

	public List<CombatEventStats> getCombatEvents(int combatId) {
		return combatEvents.get(combatId);
	}

	public Integer findCombatIdByCombatEvent(final Event.Type type, final long timestamp) {
		for (final Integer combatId: combatEvents.keySet()) {
			for (final CombatEventStats ce: combatEvents.get(combatId)) {
				if (ce.getTimestamp() == timestamp && ce.getType().equals(type)) {
					return combatId;
				}
			}
		}
		return null;
	}

	public void reset() {
		actors.clear();
		entities.clear();
		tickFrom = tickTo = null;
		effects.clear();
		combatEvents.clear();
	}
}
