package com.ixale.starparse.domain.stats;

import java.io.Serializable;

import com.ixale.starparse.domain.Event;

public class CombatEventStats implements Serializable {

	private static final long serialVersionUID = 1L;

	Event.Type type;
	long timestamp;

	CombatEventStats() {
	}

	public CombatEventStats(final Event.Type type, final long timestamp) {
		this.type = type;
		this.timestamp = timestamp;
	}

	public Event.Type getType() {
		return type;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String toString() {
		return type + " @ " + timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CombatEventStats other = (CombatEventStats) obj;
		if (timestamp != other.timestamp)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
