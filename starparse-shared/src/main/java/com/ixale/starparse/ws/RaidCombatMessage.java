package com.ixale.starparse.ws;

import java.util.List;

import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.stats.AbsorptionStats;
import com.ixale.starparse.domain.stats.ChallengeStats;
import com.ixale.starparse.domain.stats.CombatEventStats;
import com.ixale.starparse.domain.stats.CombatStats;

public class RaidCombatMessage extends BaseMessage {

	private static final long serialVersionUID = 1L;

	String characterName;

	// combat meta (simplified)
	long combatTimeFrom;
	Long combatTimeTo;
	RaidBossName bossName;
	Raid.Size bossSize;
	Raid.Mode bossMode;

	CombatStats combatStats;
	List<AbsorptionStats> absorptionStats;
	List<ChallengeStats> challengeStats;
	List<CombatEventStats> combatEventStats;

	long timestamp;

	CharacterDiscipline discipline;

	Event.Type exitEvent;

	RaidCombatMessage() {
	}

	public RaidCombatMessage(final String characterName,
		final long combatTimeFrom, final Long combatTimeTo,
		final RaidBossName bossName, final Raid.Size bossSize, final Raid.Mode bossMode,
		final CombatStats combatStats,
		final List<AbsorptionStats> absorptionStats,
		final List<ChallengeStats> challengeStats,
		final List<CombatEventStats> combatEventStats,
		final long timestamp,
		final CharacterDiscipline discipline, 
		final Event.Type exitEvent) {

		this.characterName = characterName;
		this.combatTimeFrom = combatTimeFrom;
		this.combatTimeTo = combatTimeTo;
		this.bossName = bossName;
		this.bossSize = bossSize;
		this.bossMode = bossMode;
		this.combatStats = combatStats;
		this.absorptionStats = absorptionStats;
		this.challengeStats = challengeStats;
		this.combatEventStats = combatEventStats;
		this.timestamp = timestamp;
		this.discipline = discipline;
		this.exitEvent = exitEvent;
	}

	public String getCharacterName() {
		return characterName;
	}

	public long getCombatTimeFrom() {
		return combatTimeFrom;
	}

	public Long getCombatTimeTo() {
		return combatTimeTo;
	}

	public RaidBossName getBossName() {
		return bossName;
	}

	public Raid.Size getBossSize() {
		return bossSize;
	}

	public Raid.Mode getBossMode() {
		return bossMode;
	}

	public CombatStats getCombatStats() {
		return combatStats;
	}

	public List<AbsorptionStats> getAbsorptionStats() {
		return absorptionStats;
	}

	public List<ChallengeStats> getChallengeStats() {
		return challengeStats;
	}

	public List<CombatEventStats> getCombatEventStats() {
		return combatEventStats;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public CharacterDiscipline getDiscipline() {
		return discipline;
	}

	public Event.Type getExitEvent() {
		return exitEvent;
	}

	public String toString() {
		return characterName + " (" + (discipline == null ? "?" : discipline) + ") @ " + timestamp + " :"
			+ " combat[" + combatTimeFrom + "-" + combatTimeTo + "]"
			+ " boss[" + (bossName != null ? (bossName + " " + bossSize + " " + bossMode) : "") + " ]"
			+ " stats[" + combatStats + "]"
			+ " absorption[" + absorptionStats + "]"
			+ " challenges[" + challengeStats + "]"
			+ " combatEvents[" + combatEventStats + "]"
			+ " exitEvent[" + exitEvent + "]";
	}

}
