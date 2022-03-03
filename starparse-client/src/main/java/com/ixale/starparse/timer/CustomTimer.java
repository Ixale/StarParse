package com.ixale.starparse.timer;

import com.ixale.starparse.domain.ConfigTimer;
import com.ixale.starparse.domain.ConfigTimer.Condition;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.gui.SoundManager;
import com.ixale.starparse.timer.TimerManager.RaidPullTimer;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class CustomTimer extends BaseTimer {

	private final ConfigTimer timer;
	private final List<ConfigTimer> nextTimers = new ArrayList<>(), cancelTimers = new ArrayList<>();
	private final BaseTimer systemTimer;

	private Integer countdownThreshold = null, soundThreshold = null;
	private Long lastSample = null, lastSound = null;

	public CustomTimer(final ConfigTimer timer, final Event e) {
		super(getDisplayName(timer, e),
				null, (int) Math.round(timer.getInterval() * 1000),
				timer.getRepeat() != null && timer.getRepeat() > 0 && timer.getInterval() > 0 ? (int) Math.round(timer.getInterval() * 1000) : null,
				timer.getRepeat(),
				timer.getCancel() != null && Condition.Type.COMBAT_END.equals(timer.getCancel().getType()) ? Scope.COMBAT : Scope.ANY);

		this.timer = timer;
		this.systemTimer = null;
	}

//	public CustomTimer(final ConfigTimer timer, final BaseTimer systemTimer) {
//		this(timer, systemTimer, null);
//	}

	public CustomTimer(final ConfigTimer timer, final BaseTimer systemTimer, final Integer interval) {
		super(systemTimer.getName(), timer.getName(),
				interval == null ? systemTimer.getFirstInterval() : interval,
				systemTimer.getRepeatInterval(),
				null, // MAX
				systemTimer.getScope());

		this.timer = timer;
		this.systemTimer = systemTimer;
	}

	@Override
	public void start(Long timeFrom) {
		super.start(timeFrom);

		if (timer.getCountdownCount() != null) {
			countdownThreshold = timer.getCountdownCount() * 1000 + (TimerManager.POLLING / 2);
		}
		if (timer.getAudio() != null && timer.getSoundOffset() != null && timer.getSoundOffset() > 1) {
			soundThreshold = timer.getSoundOffset() * 1000 + (TimerManager.POLLING / 2);
		}
	}

	@Override
	protected void expiredRepeat(long timeTo) {
		if (timer.getAudio() != null && (lastSound == null || lastSound < timeTo)) {
			if (!TimerManager.isMuted() || Scope.ANY.equals(getScope())) {
				SoundManager.play(timer.getAudio(), timer.getVolume() == null ? null : timer.getVolume() * 1.0);
			}
			lastSound = timeTo;
		}
	}

	@SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
	@Override
	protected void runningTick(long timeTo, long timeRemaining) {
		if (countdownThreshold != null && countdownThreshold >= timeRemaining) {
			int sample = (int) Math.round((timeRemaining + 300) / 1000.0);
			if (sample > 0 && (lastSample == null || (timeTo - (sample * 1000)) > lastSample)) {
				if (!TimerManager.isMuted() || Scope.ANY.equals(getScope())) {
					SoundManager.play(sample, timer.getCountdownVoice(),
							timer.getCountdownVolume() == null ? null : timer.getCountdownVolume() * 1.0);
				}

				lastSample = timeTo - (sample * 1000); // endures random restarts from other threads etc
			}
		}
		if (soundThreshold != null && soundThreshold >= timeRemaining && (lastSound == null || lastSound < timeTo)) {
			if (!TimerManager.isMuted() || Scope.ANY.equals(getScope())) {
				SoundManager.play(timer.getAudio(), timer.getVolume() == null ? null : timer.getVolume() * 1.0);
			}
			lastSound = timeTo;
		}
	}

	@Override
	protected void expired(long timeTo) {
		expiredRepeat(timeTo);

		for (final ConfigTimer nextTimer : nextTimers) {
			TimerManager.startTimer(nextTimer, getTimeTo(), null);
		}
		for (final ConfigTimer cancelTimer : cancelTimers) {
			TimerManager.stopTimer(cancelTimer);
		}
	}

	@Override
	public boolean isVisual() {
		return timer.getColor() != null;
	}

	public Color getColor() {
		return timer.getColor();
	}

	@Override
	public ConfigTimer.Slot getSlot() {
		return timer.getSlot() == null ? ConfigTimer.Slot.A : timer.getSlot();
	}

	public List<ConfigTimer> getNextTimers() {
		return nextTimers;
	}

	public List<ConfigTimer> getCancelTimers() {
		return cancelTimers;
	}

	public BaseTimer getSystemTimer() {
		return systemTimer;
	}

	@Override
	public boolean doOverrideExpiringThreshold() {
		return systemTimer != null && (systemTimer instanceof RaidPullTimer);
	}

	public static String getDisplayName(final ConfigTimer timer, final Event e) {
		return (timer.isShowSource() && e != null && e.getSource() != null
				? getShortName(e.getSource().getName(), 15) + "\n"
				: "") + timer.getName();
	}

	public static String getShortName(final String n, int limit) {
		if (n != null && n.length() > limit) {
			if (n.contains(",")) {
				return getShortName(n.substring(0, n.indexOf(",")), limit);
			}
			if (n.startsWith("The ")) {
				return getShortName(n.substring(4), limit);
			}
//			int i = n.indexOf(" ", n.length() - limit - 1);
			int i = n.indexOf(" ");
			if (i > 0) {
				return n.charAt(0) + ". " + getShortName(n.substring(i + 1), limit - 3);
			}
			return n.substring(n.length() - limit);

		} else {
			return n;
		}
	}

}
