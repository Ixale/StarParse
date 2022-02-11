package com.ixale.starparse.service.impl;

import static com.ixale.starparse.parser.Helpers.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.ConfigTimer;
import com.ixale.starparse.domain.ConfigTimer.Condition;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.service.TimerService;
import com.ixale.starparse.timer.TimerManager;

@Service("timerService")
public class TimerServiceImpl implements TimerService {

	private static final Logger logger = LoggerFactory.getLogger(TimerServiceImpl.class);

	@Override
	public void triggerTimers(final Combat combat, final List<Event> events, final List<ConfigTimer> configTimers) {

		if (events == null || events.isEmpty()) {
			// no events, no timers
			return;
		}

		if (TimerManager.getStartTime() == null) {
			// not enabled
			return;
		}

		if (configTimers == null || configTimers.isEmpty()) {
			// no timers, ... no timers ;)
			return;
		}

		final long minTime = TimerManager.getStartTime();
		// peek
		if (events.get(events.size() - 1).getTimestamp() < minTime) {
			// whole batch is obsolete
			return;
		}

		for (final Event e: events) {
			if (e.getTimestamp() < minTime) {
				continue;
			}
			for (final ConfigTimer timer: configTimers) {
				if (!timer.isEnabled() || timer.isSystem()) {
					continue;
				}
				if (isTimerTriggered(combat, e, timer)) {
					TimerManager.startTimer(timer, e.getTimestamp(), e);
				}
			}
		}
	}

	private boolean isTimerTriggered(final Combat combat, final Event e, final ConfigTimer configTimer) {

		final Condition trigger = configTimer.getTrigger();
		if (trigger == null) {
			logger.warn("Attempting to trigger a timer without any condition: " + configTimer);
			return false;
		}
		switch (trigger.getType()) {
		case COMBAT_START:
			return isEffectEnterCombat(e) && isBossEligible(combat, e, trigger);

		case COMBAT_END:
			return isEffectExitCombat(e) && isBossEligible(combat, e, trigger);

		case ABILITY_ACTIVATED:
			return isBossEligible(combat, e, trigger)
					&& isEffectAbilityActivate(e)
					&& isSourceEligible(e, trigger)
					&& isAbilityEligible(e, trigger);

		case DAMAGE:
		case HEAL:
		case EFFECT_GAINED:
		case EFFECT_LOST:
			switch (trigger.getType()) {
			case DAMAGE:
				if (!isEffectDamage(e)) {
					return false;
				}
				break;
			case HEAL:
				if (!isEffectHeal(e)) {
					return false;
				}
				break;
			case EFFECT_GAINED:
				if (!isActionApply(e)) {
					return false;
				}
				break;
			case EFFECT_LOST:
				if (!isActionRemove(e)) {
					return false;
				}
				break;
			default:
			}
			return isBossEligible(combat, e, trigger)
				&& isSourceEligible(e, trigger)
				&& isTargetEligible(e, trigger)
				&& isAbilityEligible(e, trigger)
				&& isEffectEligible(e, trigger);

		case HOTKEY:
		case TIMER_FINISHED:
		case TIMER_STARTED:
			// not event dependent
		default:
			return false;
		}
	}

	private boolean isSourceEligible(final Event e, final Condition trigger) {
		if (trigger.getSource() == null) {
			return true;
		}
		if (Condition.SELF.equals(trigger.getSource())) {
			if (!isSourceThisPlayer(e)) {
				return false;
			}

		} else if (Condition.OTHER.equals(trigger.getSource())) {
			if (isSourceThisPlayer(e)) {
				return false;
			}

		} else if (!isSourceEqual(e, trigger.getSourceGuid(), trigger.getSource())) {
			return false;
		}
		return true;
	}

	private boolean isTargetEligible(final Event e, final Condition trigger) {
		if (trigger.getTarget() == null) {
			return true;
		}
		if (Condition.SELF.equals(trigger.getTarget())) {
			if (!isTargetThisPlayer(e)) {
				return false;
			}

		} else if (Condition.OTHER.equals(trigger.getTarget())) {
			if (isTargetThisPlayer(e)) {
				return false;
			}

		} else if (!isTargetEqual(e, trigger.getTargetGuid(), trigger.getTarget())) {
			return false;
		}
		return true;
	}

	private boolean isAbilityEligible(final Event e, final Condition trigger) {
		if (trigger.getAbility() == null && trigger.getAbilityGuid() == null) {
			return true;
		}
		return isAbilityEqual(e, trigger.getAbilityGuid(), trigger.getAbility());
	}

	private boolean isEffectEligible(final Event e, final Condition trigger) {
		if (trigger.getEffect() == null && trigger.getEffectGuid() == null) {
			return true;
		}
		return isEffectEqual(e, trigger.getEffectGuid(), trigger.getEffect());
	}

	private boolean isBossEligible(final Combat combat, final Event e, final Condition trigger) {
		if (trigger.getBoss() == null) {
			return true;
		}
		return combat != null && combat.getBoss() != null && combat.getBoss().getName().equals(trigger.getBoss());
	}
}
