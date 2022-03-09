package com.ixale.starparse.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public interface NpcNameResolver {

		String getNpcName(String name, Float x, Float y, Float angle);
	}

	public static class Npc {
		final NpcType type;
		final String name;
		final Double hidePct;
		final NpcNameResolver nameResolver;

		public Npc(final NpcType type) {
			this(type, null, null, null);
		}

		public Npc(final NpcType type, final String name) {
			this(type, name, null, null);
		}

		public Npc(final NpcType type, final String name, final Double hidePct) {
			this(type, name, hidePct, null);
		}

		public Npc(final NpcType type, final String name, final Double hidePct, final NpcNameResolver nameResolver) {
			this.type = type;
			this.name = name;
			this.hidePct = hidePct;
			this.nameResolver = nameResolver;
		}

		public NpcType getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public Double getHidePct() {
			return hidePct;
		}

		public NpcNameResolver getNameResolver() {
			return nameResolver;
		}

	}

	protected final String name;
	protected final ArrayList<RaidBoss> bosses = new ArrayList<>();
	protected final HashMap<RaidBossName, ArrayList<CombatChallenge>> challenges = new HashMap<>();
	protected final Map<Long, Npc> npcs = new HashMap<>();

	protected final HashMap<String, Long> phaseTimers = new HashMap<>();

	public Raid(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public Long getInstanceGuid() {
		return null;
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

	public Map<Long, Npc> getNpcs() {
		return npcs;
	}

}
