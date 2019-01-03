package com.ixale.starparse.domain;

import java.util.List;

public class CombatChallenge {

	public enum Type {
		DAMAGE, HEALING, FRIENDLY
	}

	final private RaidChallengeName challengeName;
	final private String phaseName;
	final private Type type;

	final private List<Object> args;
	final private String sql;

	public CombatChallenge(RaidChallengeName challengeName, String phaseName, Type type, final List<Object> args, final String sql) {
		this.challengeName = challengeName;
		this.phaseName = phaseName;
		this.args = args;
		this.sql = sql;
		this.type = type;
	}

	public RaidChallengeName getChallengeName() {
		return challengeName;
	}

	public String getPhaseName() {
		return phaseName;
	}

	public Type getType() {
		return type;
	}

	public List<Object> getArgs() {
		return args;
	}

	public String getSql() {
		return sql;
	}

	public String toString() {
		return "Challenge ["+phaseName+"]";
	}
}
