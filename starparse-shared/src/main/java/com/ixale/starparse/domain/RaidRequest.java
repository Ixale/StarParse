package com.ixale.starparse.domain;

import java.io.Serializable;

public class RaidRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		DEATH_RECAP,
		RAID_PULL,
		RAID_BREAK,
		// feedback messages
		SYSTEM_ERROR,
		SYSTEM_OK,
		// raid notes
		RAID_NOTES,
	}

	public static class Params {
		long timestamp;
		String note;

		Params() {

		}

		public Params(long timestamp) {
			this.timestamp = timestamp;
		}

		public Params(long timestamp, String note) {
			this.timestamp = timestamp;
			this.note = note;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public String getNote() {
			return note;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((note == null) ? 0 : note.hashCode());
			result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
			Params other = (Params) obj;
			if (note == null) {
				if (other.note != null)
					return false;
			} else if (!note.equals(other.note))
				return false;
			if (timestamp != other.timestamp)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.valueOf(timestamp) + (note != null ? ", " + note : "");
		}

		public String getCacheKey() {
			return toString();
		}
	}

	Type type;
	String targetName;
	Params params;

	RaidRequest() {

	}

	public RaidRequest(Type type, String targetName, Params params) {
		super();
		this.type = type;
		this.targetName = targetName;
		this.params = params;
	}

	public Type getType() {
		return type;
	}

	public String getTargetName() {
		return targetName;
	}

	public Params getParams() {
		return params;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((targetName == null) ? 0 : targetName.hashCode());
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
		RaidRequest other = (RaidRequest) obj;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (targetName == null) {
			if (other.targetName != null)
				return false;
		} else if (!targetName.equals(other.targetName))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public String toString() {
		return type + " @ " + (targetName == null ? "all" : targetName) + " (" + params + ")";
	}

	public String getCacheKey() {
		return type.ordinal()
			+ "-" + (targetName == null ? "all" : targetName.replaceAll("[^a-zA-Z0-9.-]", "_"))
			+ "-" + (params == null ? "all" : params.getCacheKey());
	}
}
