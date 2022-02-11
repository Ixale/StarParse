package com.ixale.starparse.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CombatInfo {

	private Raid.Mode instanceMode;
	private Raid.Size instanceSize;
	private String instanceName;
	private Long instanceGuid;

	// transient
	private final LinkedHashMap<Actor, CharacterDiscipline> combatPlayers = new LinkedHashMap<>();

	public CombatInfo() {
	}

	public CombatInfo(final Raid.Mode instanceMode, final Raid.Size instanceSize, final String instanceName, final Long instanceGuid) {
		this.instanceMode = instanceMode;
		this.instanceSize = instanceSize;
		this.instanceName = instanceName;
		this.instanceGuid = instanceGuid;
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

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(final String instanceName) {
		this.instanceName = instanceName;
	}

	public Long getInstanceGuid() {
		return instanceGuid;
	}

	public void setInstanceGuid(final Long instanceGuid) {
		this.instanceGuid = instanceGuid;
	}

	public Raid.Mode getInstanceMode() {
		return instanceMode;
	}

	public void setInstanceMode(final Raid.Mode instanceMode) {
		this.instanceMode = instanceMode;
	}

	public Raid.Size getInstanceSize() {
		return instanceSize;
	}

	public void setInstanceSize(final Raid.Size instanceSize) {
		this.instanceSize = instanceSize;
	}

	public String getInstanceDifficulty() {
		return (instanceMode == null ? null : instanceMode + " " + instanceSize);
	}
}
