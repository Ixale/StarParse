package com.ixale.starparse.domain;

public class EffectKey {
	private final Actor source, target;
	private final Entity effect, ability;

	public EffectKey(final Actor s, final Actor t, final Entity e, final Entity a) {
		source = s;
		target = t;
		effect = e;
		ability = a;
	}

	public Actor getSource() {
		return source;
	}

	public Actor getTarget() {
		return target;
	}

	public Entity getEffect() {
		return effect;
	}

	public Entity getAbility() {
		return ability;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
        if (!(o instanceof EffectKey)) return false;
        EffectKey key = (EffectKey) o;
        return effect.equals(key.effect) && source.equals(key.source) && target.equals(key.target); 
	}

	@Override
    public int hashCode() {
        return effect.hashCode() 
        		+ (source != null ? 11 * source.hashCode() : 0)
        		+ (target != null ? 31 * target.hashCode() : 0);
    }

	public String toString() {
		return effect+" ("+source+"@"+target+")";
	}
}