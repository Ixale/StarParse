package com.ixale.starparse.domain;

import java.io.Serializable;

public class Phase implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		BOSS, DAMAGE
	}

	private final int phaseId;
	private final String name;
	private final Type type;

	private final int combatId;

	private final int eventIdFrom;
	private Integer eventIdTo;

	private final long tickFrom;
	private Long tickTo;

	public Phase(int phaseId, String name, Type type, int combatId, int eventIdFrom, long tickFrom) {
		super();
		this.phaseId = phaseId;
		this.name = name;
		this.type = type;

		this.combatId = combatId;

		this.eventIdFrom = eventIdFrom;
		this.tickFrom = tickFrom;
	}

	public void setEventIdTo(Integer eventIdTo) {
		this.eventIdTo = eventIdTo;
	}

	public void setTickTo(Long tickTo) {
		this.tickTo = tickTo;
	}

	public int getPhaseId() {
		return phaseId;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public int getCombatId() {
		return combatId;
	}

	public int getEventIdFrom() {
		return eventIdFrom;
	}

	public long getTickFrom() {
		return tickFrom;
	}

	public Integer getEventIdTo() {
		return eventIdTo;
	}

	public Long getTickTo() {
		return tickTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Phase)) return false;
		Phase other = (Phase) o;
		return other.combatId == combatId
				&& other.phaseId == phaseId
				&& other.eventIdFrom == eventIdFrom
				&& ((other.eventIdTo == null && eventIdTo == null) 
						|| (eventIdTo != null && other.eventIdTo != null && other.eventIdTo.equals(eventIdTo)));
	}

	@Override
	public int hashCode() {
		return combatId + 11 * phaseId + 31 * eventIdFrom + 51 * eventIdTo;
	}

	public String toString() {
		return phaseId+"@"+combatId+": "+name+" ("+type+")"
				+ " ["+tickFrom+"-"+tickTo+"]"
				+ " ["+eventIdFrom+"-"+eventIdTo+"]";
	}
}
