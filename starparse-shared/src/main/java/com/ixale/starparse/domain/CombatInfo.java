package com.ixale.starparse.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CombatInfo {

	private LocationInfo locationInfo;

	// transient
	private final LinkedHashMap<Actor, CharacterDiscipline> combatPlayers = new LinkedHashMap<>();

	// transient
	private final LinkedHashMap<Actor, CombatActorState> combatActorStates = new LinkedHashMap<>();

	public CombatInfo() {
	}

	public CombatInfo(final LocationInfo locationInfo) {
		this.locationInfo = locationInfo;
	}

	public void addCombatPlayer(Actor player, CharacterDiscipline discipline) {
		if (combatPlayers.get(player) != null && Objects.equals(combatPlayers.get(player), discipline)) {
			return;
		}
		combatPlayers.put(player, discipline);
	}

	public LinkedHashMap<Actor, CharacterDiscipline> getCombatPlayers() {
		return combatPlayers;
	}

	public Map.Entry<Actor, CharacterDiscipline> getSelf() {
		for (final Map.Entry<Actor, CharacterDiscipline> e : combatPlayers.entrySet()) {
			if (Actor.Type.SELF.equals(e.getKey().getType())) {
				return e;
			}
		}
		throw new IllegalStateException("Missing SELF, got " + combatPlayers);
	}

	public LocationInfo getLocationInfo() {
		return locationInfo;
	}

	public void setLocationInfo(final LocationInfo locationInfo) {
		this.locationInfo = locationInfo;
	}

	public LinkedHashMap<Actor, CombatActorState> getCombatActorStates() {
		return combatActorStates;
	}

}
