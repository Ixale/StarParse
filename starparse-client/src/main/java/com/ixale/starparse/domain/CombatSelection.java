package com.ixale.starparse.domain;

import java.util.List;

public class CombatSelection {

	private final Integer eventIdFrom, eventIdTo;
	private final Long tickFrom, tickTo;

	final private List<Object> args;
	final private String sql;

	public CombatSelection(final Integer eventIdFrom, final Integer eventIdTo, final Long tickFrom, final Long tickTo) {
		this(eventIdFrom, eventIdTo, tickFrom, tickTo, null, null);
	}

	public CombatSelection(final Integer eventIdFrom, final Integer eventIdTo, final Long tickFrom, final Long tickTo, final List<Object> args, final String sql) {
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

	public List<Object> getArgs() {
		return args;
	}

	public String getSql() {
		return sql;
	}

	public String toString() {
		return "ID ["+eventIdFrom+"-"+eventIdTo+"]"
			+ ", time ["+tickFrom+"-"+tickTo+"]";
	}
}
