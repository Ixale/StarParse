package com.ixale.starparse.domain;

import java.util.Map;

public class CombatSelection {

	private final Integer eventIdFrom, eventIdTo;
	private final Long tickFrom, tickTo;

	final private Map<String, ?> args;
	final private String sql;

	public CombatSelection(final Integer eventIdFrom, final Integer eventIdTo, final Long tickFrom, final Long tickTo) {
		this(eventIdFrom, eventIdTo, tickFrom, tickTo, null, null);
	}

	public CombatSelection(final Integer eventIdFrom, final Integer eventIdTo, final Long tickFrom, final Long tickTo, final Map<String, ?> args, final String sql) {
		this.eventIdFrom = eventIdFrom;
		this.eventIdTo = eventIdTo;
		this.tickFrom = tickFrom;
		this.tickTo = tickTo;

		this.args = args;
		this.sql = sql;
	}

	public Long getTickFrom() {
		return tickFrom;
	}

	public Long getTickTo() {
		return tickTo;
	}

	public Integer getEventIdFrom() {
		return eventIdFrom;
	}

	public Integer getEventIdTo() {
		return eventIdTo;
	}

	public Map<String, ?> getArgs() {
		return args;
	}

	public String getSql() {
		return sql;
	}

	public String toString() {
		return "ID [" + eventIdFrom + "-" + eventIdTo + "]"
				+ ", time [" + tickFrom + "-" + tickTo + "]";
	}
}
