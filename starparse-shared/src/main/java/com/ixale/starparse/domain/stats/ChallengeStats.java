package com.ixale.starparse.domain.stats;

import java.io.Serializable;

import com.ixale.starparse.domain.RaidChallengeName;

public class ChallengeStats implements Serializable {

	private static final long serialVersionUID = 1L;

	RaidChallengeName challengeName;
	long tickFrom;
	long tickTo;

	Integer damage;
	Integer heal;
	Integer effectiveHeal;

	ChallengeStats() {

	}

	public ChallengeStats(RaidChallengeName challengeName, long tickFrom, long tickTo, Integer damage, Integer heal,
		Integer effectiveHeal) {
		this.challengeName = challengeName;
		this.tickFrom = tickFrom;
		this.tickTo = tickTo;
		this.damage = damage;

		this.heal = heal;
		this.effectiveHeal = effectiveHeal;
	}

	public RaidChallengeName getChallengeName() {
		return challengeName;
	}

	public long getTickFrom() {
		return tickFrom;
	}

	public Long getTickTo() {
		return tickTo;
	}

	public Integer getDamage() {
		return damage;
	}

	public Integer getDps() {
		return (int) Math.round(damage * 1000.0 / (tickTo - tickFrom));
	}

	public Integer getHeal() {
		return heal;
	}

	public Integer getHps() {
		return (int) Math.round(heal * 1000.0 / (tickTo - tickFrom));
	}

	public Integer getEffectiveHeal() {
		return effectiveHeal;
	}

	public Integer getEhps() {
		return (int) Math.round(effectiveHeal * 1000.0 / (tickTo - tickFrom));
	}

	public String toString() {
		return challengeName + " [" + tickFrom + "-" + tickTo + "]: " + damage + ", " + effectiveHeal + " (" + heal + ")";
	}
}
