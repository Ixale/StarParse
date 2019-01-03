package com.ixale.starparse.domain.stats;

import java.io.Serializable;
import java.util.ArrayList;

public class CombatLogStats implements Serializable {

	private static final long serialVersionUID = 1L;

	private String combatLogName;

	private ArrayList<RaidCombatStats> raids;

	public CombatLogStats() {
		//
	}

	public String getCombatLogName() {
		return combatLogName;
	}

	public void setCombatLogName(String combatLogName) {
		this.combatLogName = combatLogName;
	}

	public ArrayList<RaidCombatStats> getRaids() {
		if (raids == null) {
			raids = new ArrayList<RaidCombatStats>();
		}
		return raids;
	}

	public String toString() {
		return "Stats [" + combatLogName + "] (" + (raids != null ? raids.size() : "?") + ")";
	}
}
