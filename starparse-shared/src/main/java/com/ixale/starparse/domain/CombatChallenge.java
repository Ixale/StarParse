package com.ixale.starparse.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CombatChallenge {

	public enum Type {
		DAMAGE, HEALING, FRIENDLY
	}

	final private RaidChallengeName challengeName;
	final private String phaseName;
	final private Type type;

	final private Map<String, ?> args;
	final private String sql;

	public CombatChallenge(RaidChallengeName challengeName, String phaseName, Type type, final List<Long> targetIds) {
		this.challengeName = challengeName;
		this.phaseName = phaseName;
		this.args = Collections.singletonMap(challengeName.name() + "TargetGuids", targetIds);
		this.sql = "target_guid IN (:" + challengeName.name() + "TargetGuids" + ")";
		this.type = type;
	}

	public CombatChallenge(RaidChallengeName challengeName, String phaseName, Type type, final Map<String, Object> args, final String sql) {
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

	public Map<String, ?> getArgs() {
		return args;
	}

	public String getSql() {
		return sql;
	}

	public String toString() {
		return "Challenge ["+phaseName+"]";
	}
}
