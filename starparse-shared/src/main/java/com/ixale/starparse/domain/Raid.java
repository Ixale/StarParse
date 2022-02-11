package com.ixale.starparse.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

abstract public class Raid {

	public enum Mode {
		SM, HM, NiM
	}

	public enum Size {
		Eight("8m"),
		Sixteen("16m");

		final String name;

		Size(String n) {
			name = n;
		}

		public String toString() {
			return name;
		}
	}

	protected final String name;
	protected final ArrayList<RaidBoss> bosses = new ArrayList<RaidBoss>();
	protected final HashMap<RaidBossName, ArrayList<CombatChallenge>> challenges = new HashMap<RaidBossName, ArrayList<CombatChallenge>>();

	protected final HashMap<String, Long> phaseTimers = new HashMap<String, Long>();

	public Raid(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public ArrayList<RaidBoss> getBosses() {
		return bosses;
	}

	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {
		return null;
	}

	protected void addChallenge(final RaidBossName bossName, final CombatChallenge challenge) {
		if (!challenges.containsKey(bossName)) {
			challenges.put(bossName, new ArrayList<>());
		}
		challenges.get(bossName).add(challenge);
	}

	public List<CombatChallenge> getChallenges(final RaidBoss boss) {
		return challenges.getOrDefault(boss.getRaidBossName(), null);
	}
}
