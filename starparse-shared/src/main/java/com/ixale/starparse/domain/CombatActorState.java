package com.ixale.starparse.domain;

public class CombatActorState {

	private final Actor actor;
	private final Raid.Npc npc;

	private Long maxHp;
	private Long currentHp;
	private long tick;

	public CombatActorState(final Actor actor, final Raid.Npc npc, final long tick) {
		this.actor = actor;
		this.npc = npc;
		this.tick = tick;
	}

	public Actor getActor() {
		return actor;
	}

	public Raid.Npc getNpc() {
		return npc;
	}

	public Long getMaxHp() {
		return maxHp;
	}

	public void setMaxHp(final Long maxHp) {
		this.maxHp = maxHp;
	}

	public Long getCurrentHp() {
		return currentHp;
	}

	public void setCurrentHp(final Long currentHp) {
		this.currentHp = currentHp;
	}

	public long getTick() {
		return tick;
	}

	public void setTick(final long tick) {
		this.tick = tick;
	}

}
