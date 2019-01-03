package com.ixale.starparse.domain;

public class Actor extends Entity implements Comparable<Actor> {

	private static final long serialVersionUID = 1L;

	public enum Type {
		SELF(1), PLAYER(2), COMPANION(3), NPC(4);

		int id;

		private Type(int i) {
			id = i;
		}

		public int getId() {
			return id;
		}

		public static Type valueOf(int id) {
			for (Type tt: values()) {
				if (tt.id == id) {
					return tt;
				}
			}
			return null;
		}
	}

	Type type;
	Long instanceId;

	// contextual
	Long timeFrom, timeTo;
	Boolean isHostile;

	public enum Role {
		SOURCE, TARGET
	}

	Actor() {
	}

	// companion
	public Actor(String name, Type type, Long guid) {
		super(name, guid);
		this.type = type;
		this.instanceId = null;
	}

	// NPC
	public Actor(String name, Type type, Long guid, Long instanceId) {
		super(name, guid);
		this.type = type;
		this.instanceId = instanceId;
	}

	// player
	public Actor(String name, Type type) {
		super(name, null);
		this.type = type;
		this.instanceId = null;
	}

	public Type getType() {
		return type;
	}

	public Long getInstanceId() {
		return instanceId;
	}

	public Long getTimeFrom() {
		return timeFrom;
	}

	public void setTimeFrom(Long timeFrom) {
		this.timeFrom = timeFrom;
	}

	public Long getTimeTo() {
		return timeTo;
	}

	public void setTimeTo(Long timeTo) {
		this.timeTo = timeTo;
	}

	public Boolean isHostile() {
		return isHostile;
	}

	public void setIsHostile(Boolean isHostile) {
		this.isHostile = isHostile;
	}

	public String toString() {
		switch (type) {
			case SELF:
				return "Self";
			case PLAYER:
				return name + (isHostile != null && isHostile ? " (hostile)" : "");
			case COMPANION:
				return name + " [" + guid + "]";
			default:
				return name + " [" + guid + (instanceId != null ? ":" + instanceId : "") + "]";
		}
	}

	@Override
	public int compareTo(Actor o) {
		// players first
		if (this.type != Type.NPC && o.type == Type.NPC) {
			return -1;
		} else if (this.type == Type.NPC && o.type != Type.NPC) {
			return 1;
			// other's last
		} else if (this.getInstanceId() == null && o.getInstanceId() != null) {
			return 1;
		} else if (this.getInstanceId() != null && o.getInstanceId() == null) {
			return -1;
			// same GUIDs by time
		} else if (this.getGuid() != null && o.getGuid() != null && this.getGuid().equals(o.getGuid())) {
			if (this.getTimeFrom() != null && o.getTimeFrom() != null) {
				return this.getTimeFrom().compareTo(o.getTimeFrom());
			}
		}
		// by name
		return this.getName().compareTo(o.getName());
	}
}
