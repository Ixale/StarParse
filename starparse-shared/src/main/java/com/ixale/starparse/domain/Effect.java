package com.ixale.starparse.domain;

import java.io.Serializable;

public class Effect implements Serializable {

	public enum Type {
		PROC, ACT, REC, GIV
	}

	private static final long serialVersionUID = 1L;

	private final int effectId;

	private final long timeFrom;
	private Long timeTo;

	private final int eventIdFrom;
	private Integer eventIdTo;

	private final Actor source, target;
	private final Entity ability, effect;

	private boolean isActivated;
	private final boolean isAbsorption;

	private transient Type type;

	public Effect(int effectId, int eventIdFrom, long timeFrom,
		Actor source, Actor target, Entity ability, Entity effect,
		boolean isActivated, boolean isAbsorption) {
		super();
		this.effectId = effectId;
		this.timeFrom = timeFrom;
		this.eventIdFrom = eventIdFrom;
		this.source = source;
		this.target = target;
		this.ability = ability;
		this.effect = effect;
		this.isActivated = isActivated;
		this.isAbsorption = isAbsorption;
	}

	public void setTimeTo(Long timeTo) {
		this.timeTo = timeTo;
	}

	public void setEventIdTo(Integer eventIdTo) {
		this.eventIdTo = eventIdTo;
	}

	public void setIsActivated(Boolean isActivated) {
		this.isActivated = isActivated;
	}

	public int getEffectId() {
		return effectId;
	}

	public long getTimeFrom() {
		return timeFrom;
	}

	public Long getTimeTo() {
		return timeTo;
	}

	public int getEventIdFrom() {
		return eventIdFrom;
	}

	public Integer getEventIdTo() {
		return eventIdTo;
	}

	public Actor getSource() {
		return source;
	}

	public Actor getTarget() {
		return target;
	}

	public Entity getAbility() {
		return ability;
	}

	public Entity getEffect() {
		return effect;
	}

	public Boolean isActivated() {
		return isActivated;
	}

	public Boolean isAbsorption() {
		return isAbsorption;
	}

	public Type getType() {
		if (type == null) {
			if (source.getType().equals(Actor.Type.SELF) && target.getType().equals(Actor.Type.SELF)) {
				if (Boolean.TRUE.equals(isActivated)) {
					// player on himself, activated effects (e.g. Rebuke)
					type = Type.ACT;
				} else {
					// player on himself, gained effects (e.g. Power surge)
					type = Type.PROC;
				}

			} else if (target.getType().equals(Actor.Type.SELF)) {
				// others on player (e.g. Force Armor)
				type = Type.REC;
			} else {
				// player on others (e.g. Inspiration)
				type = Type.GIV;
			}
		}
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Effect))
			return false;
		Effect other = (Effect) o;
		if (other.effectId == this.effectId) {
			return true;
		}
		return effect == other.effect && source == other.source && target == other.target;
	}

	@Override
	public int hashCode() {
		return this.effectId;
	}

	public String toString() {
		return "[" + effectId + "] " + source + " @ " + target
			+ " [" + timeFrom + "-" + timeTo + "][" + eventIdFrom + "-" + eventIdTo + "]"
			+ "[" + isActivated + " " + isAbsorption + "]: " + effect;
	}
}
