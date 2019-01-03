package com.ixale.starparse.gui.table.item;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.gui.Format;

public class EventItem extends BaseItem {

	public final SimpleStringProperty sourceName = new SimpleStringProperty(),
		targetName = new SimpleStringProperty(),
		action = new SimpleStringProperty(),
		ability = new SimpleStringProperty(),
		damageType = new SimpleStringProperty(),
		mitigationType = new SimpleStringProperty(),
		absorptionType = new SimpleStringProperty(),
		actionIcon = new SimpleStringProperty();

	public final SimpleIntegerProperty eventId = new SimpleIntegerProperty();

	public final SimpleObjectProperty<Integer> value = new SimpleObjectProperty<Integer>(),
		absorbed = new SimpleObjectProperty<Integer>(),
		overheal = new SimpleObjectProperty<Integer>(),
		threat = new SimpleObjectProperty<Integer>();

	public final SimpleLongProperty tick = new SimpleLongProperty();

	private final Event event;
	private final List<Effect> effectsAll;
	private List<Effect> effects;

	public EventItem(final Event event, final List<Effect> effectsAll) {
		this.event = event;
		this.effectsAll = effectsAll;
	}

	public Integer getEventId() {
		return eventId.get();
	}

	public Long getTick() {
		return tick.get();
	}

	public String getSourceName() {
		return sourceName.get();
	}

	public String getTargetName() {
		return targetName.get();
	}

	public String getAction() {
		return action.get();
	}

	public String getAbility() {
		return ability.get();
	}

	public Integer getValue() {
		return value.get();
	}

	public String getMitigation() {
		return mitigationType.get();
	}

	public Integer getAbsorbed() {
		return absorbed.get();
	}

	public Integer getOverheal() {
		return overheal.get();
	}

	public Integer getThreat() {
		return threat.get();
	}

	public String getActionIcon() {
		return actionIcon.get();
	}

	public Event getEvent() {
		return event;
	}

	public List<Effect> getEffects() {
		if (effects == null && effectsAll != null && !effectsAll.isEmpty()) {
			effects = new ArrayList<>();
			for (final Effect effect: effectsAll) {
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
		return tick.getValue();
	}

	public Long getTickTo() {
		return tick.getValue();
	}

	public String toString() {
		String v = Format.formatTime(event.getTimestamp(), true) + " (" + Format.formatTime(tick.get()) + ") "
			+ sourceName.get() + (targetName.get().equals(sourceName.get()) ? "" : " @ " + targetName.get()) + ": "
			+ action.get() + " [" + ability.get() + "]";

		if (value.get() != null) {
			v += " (" + value.get()
				+ (event.isCrit() ? "*" : "")
				+ (overheal.get() != null && overheal.get() > 0 ? " [" + overheal.get() + "]" : "")
				+ (event.getDamage() != null ? " " + event.getDamage().getName() : "")
				+ (event.getMitigation() != null ? " " + event.getMitigation().getName() : "")
				+ (event.getReflect() != null ? " " + event.getReflect().getName() : "")
				+ (absorbed.get() != null ? " [" + absorbed.get() + " " + Format.formatAbsorptionName(event) + "]" : "")
				+ ")";
		}
		if (threat.get() != null) {
			v += " <" + threat.get() + ">";
		}

		return v;
	}

	@Override
	public String getFullName() {
		return sourceName.get() + " @ " + targetName.get() + ": " + action.get() + " [" + ability.get() + "]";
	}
}