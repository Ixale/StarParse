package com.ixale.starparse.domain.stats;

import java.io.Serializable;
import java.util.ArrayList;

import com.ixale.starparse.ws.RaidCombatMessage;

public class RaidCombatStats implements Serializable {

	private static final long serialVersionUID = 1L;

	private int combatId;

	private ArrayList<RaidCombatMessage> combatStats;

	public RaidCombatStats() {
		//
	}

	public int getCombatId() {
		return combatId;
	}

	public void setCombatId(int combatId) {
		this.combatId = combatId;
	}

	public ArrayList<RaidCombatMessage> getCombatStats() {
		if (combatStats == null) {
			combatStats = new ArrayList<RaidCombatMessage>();
		}
		return combatStats;
	}
}
