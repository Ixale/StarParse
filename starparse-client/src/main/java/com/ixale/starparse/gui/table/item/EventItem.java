package com.ixale.starparse.gui.table.item;

import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.EntityGuid;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.gui.Format;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

import static com.ixale.starparse.parser.Helpers.isEffectDamage;
import static com.ixale.starparse.parser.Helpers.isEffectHeal;

public class EventItem extends BaseItem {

	private final Event event;
	private final long tick;
	private final List<Effect> effectsAll;
	private List<Effect> effects;

	public final SimpleObjectProperty<Integer> eventId;

	private final Integer value, overheal, threat;
	private String action, ability;

	public EventItem(final Event e, final long eventTick, final List<Effect> effectsAll) {
		this.event = e;
		this.tick = eventTick;
		this.effectsAll = effectsAll;

		eventId = new SimpleObjectProperty<>(e.getEventId()); // one is required for the table to populate

		if (isEffectDamage(e) && e.getAbsorbed() != null) {
			value = e.getValue() - e.getAbsorbed();
			overheal = null;
		} else if (isEffectHeal(e)) {
			value = e.getEffectiveHeal() != null ? e.getEffectiveHeal() : 0;
			overheal = e.getValue() - (e.getEffectiveHeal() != null ? e.getEffectiveHeal() : 0);
		} else {
			value = e.getValue();
			overheal = null;
		}

		threat = e.getThreat() != null
				? (int) Math.min(Math.max(e.getThreat(), Integer.MIN_VALUE), Integer.MAX_VALUE)
				: null;
	}

	public Event getEvent() {
		return event;
	}

	public List<Effect> getEffects() {
		if (effects == null && effectsAll != null && !effectsAll.isEmpty()) {
			effects = new ArrayList<>();
			for (final Effect effect : effectsAll) {
				if (effect.getEventIdTo() == null) {
					continue;
				}
				if (event.getAbsorptionEventId() != null
						&& effect.isAbsorption()
						&& effect.getEventIdFrom() == event.getAbsorptionEventId()) {
					// keep
				} else {
					if (effect.getEventIdFrom() > event.getEventId()) {
						continue;
					}
					if (effect.getEventIdTo() < event.getEventId()) {
						continue;
					}
				}
				effects.add(effect);
			}
		}
		return effects;
	}

	public Long getTickFrom() {
		return tick;
	}

	public Long getTickTo() {
		return tick;
	}

	public String getAbility() {
		if (ability == null) {
			ability = getSimplifiedAbility(event, EntityGuid.fromGuid(event.getEffect().getGuid()));
		}
		return ability;
	}

	public String getAction() {
		if (action == null) {
			action = getSimplifiedAction(event,
					EntityGuid.fromGuid(event.getEffect().getGuid()),
					EntityGuid.fromGuid(event.getAction().getGuid()));
		}
		return action;
	}

	public Integer getValue() {
		return value;
	}

	public Integer getOverheal() {
		return overheal;
	}

	public Integer getThreat() {
		return threat;
	}

	public String toString() {
		String v = Format.formatTime(event.getTimestamp(), true) + " (" + Format.formatTime(tick) + ") "
				+ event.getSource().getName() + (event.getSource() == event.getTarget() ? "" : " @ " + event.getTarget().getName()) + ": "
				+ getAction() + " [" + getAbility() + "]";

		if (value != null) {
			v += " (" + value
					+ (event.isCrit() ? "*" : "")
					+ (overheal != null && overheal > 0 ? " [" + overheal + "]" : "")
					+ (event.getDamage() != null ? " " + event.getDamage().getName() : "")
					+ (event.getMitigation() != null ? " " + event.getMitigation().getName() : "")
					+ (event.getReflect() != null ? " " + event.getReflect().getName() : "")
					+ (event.getAbsorbed() != null ? " [" + event.getAbsorbed() + " " + Format.formatAbsorptionName(event) + "]" : "")
					+ ")";
		}
		if (threat != null) {
			v += " <" + threat + ">";
		}

		return v;
	}

	@Override
	public String getFullName() {
		return event.getSource().getName() + " @ " + event.getTarget().getName() + ": " + getAction() + " [" + getAbility() + "]";
	}

	private String getSimplifiedAction(final Event e, final EntityGuid effect, final EntityGuid action) {
		if (effect != null) {
			switch (effect) {
				case AbilityActivate:
					return "Activated";
				case AbilityDeactivate:
					return "Deactivated";
				case AbilityCancel:
					return "Cancelled";
				case AbilityInterrupt:
					return "Interrupted";
				case FailedEffect:
					return "Failed Effect";

				case FallingDamage:
					return "Fell Down";
				case Damage:
					return "Hit";
				case Heal:
					return "Healed";

				case Taunt:
					return "Taunted";
				case ModifyThreat:
					return "Modified Threat";
				case NoLongerSuspicious:
					return "No Longer Suspicious";
				case EnterCombat:
					return "Entered Combat";
				case ExitCombat:
					return "Exited Combat";

				case Death:
					return "Killed";
				case Revived:
					return "Revived";

				case Crouch:
					return "Entered Cover";
				case LeaveCover:
					return "Exited Cover";
				default:
			}
		}

		if (action != null) {
			switch (action) {
				case ApplyEffect:
					return "Applied Effect";
				case RemoveEffect:
					return "Removed Effect";
				case Spend:
					return "Spent";
				case Restore:
					return "Restored";
				default:
			}
		}

		if (e.getAction() != null) {
			return e.getAction().getName();
		}

		return null;
	}

	private String getSimplifiedAbility(final Event e, final EntityGuid effect) {
		if (effect != null) {
			switch (effect) {
				case Death:
				case EnterCombat:
				case ExitCombat:
				case Revived:
				case Crouch:
				case LeaveCover:
				case FallingDamage:
				case ModifyThreat:
					return e.getEffect().getName();

				case AbilityActivate:
				case AbilityDeactivate:
				case AbilityInterrupt:
				case AbilityCancel:
				case Damage:
				case Heal:
				case Taunt:
				case FailedEffect:
					// ability name is enough
					if (e.getAbility() != null) {
						if (e.getAbility().getName().isEmpty()) {
							return "(" + e.getAbility().getGuid() + ")"; // e.g. Shadow's ID 3298019487252480
						}
						return e.getAbility().getName();
					}
				default:
					// FALLTHROUGH
			}
		}

		if (e.getAbility() == null
				|| e.getAbility().getName() == null
				|| (e.getAbility() != null && e.getAbility().getGuid() != null
				&& e.getEffect() != null && e.getEffect().getGuid() != null
				&& e.getAbility().getGuid().equals(e.getEffect().getGuid()))
				|| (e.getAbility() != null && e.getAbility().getName() != null
				&& e.getEffect() != null && e.getEffect().getName() != null
				&& e.getEffect().getName().contains(e.getAbility().getName()))) {
			// effect name is enough
			return e.getEffect() == null ? "" : (e.getEffect().getName() == null || e.getEffect().getName().isEmpty()
					? "(" + e.getEffect().getGuid() + ")"
					: e.getEffect().getName());
		}

		// both ability and effect name
		return e.getAbility().getName() + ": " + e.getEffect().getName();
	}

}