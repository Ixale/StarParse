package com.ixale.starparse.timer;

import com.ixale.starparse.domain.ConfigTimer;
import com.ixale.starparse.gui.Format;

import javafx.scene.paint.Color;

abstract public class BaseTimer {

	private final int MAX_REPEAT = 50;

	public enum Scope {
		COMBAT, ANY
	}

	public enum State {
		NEW, RUNNING, FINISHED, STOPPED
	}

	private final String name, fullName;
	private final Scope scope;

	private final Integer firstInterval, repeatInterval, repeatCount;

	private State state = State.NEW;
	private Long timeFrom, timeTo;
	private Long timeRemaining;
	private int repeatCounter = 1, lastRepeatCounter = 1;
	private double progress = 1.0;

	private Integer colorIndex;
	private Color color;

	public BaseTimer(String name, String fullName, Integer length) {
		this(name, fullName, length, null, null, Scope.COMBAT);
	}

	public BaseTimer(String name, String fullName, Integer firstInterval, Integer repeatInterval) {
		this(name, fullName, firstInterval, repeatInterval, null, Scope.COMBAT);
	}

	public BaseTimer(String name, String fullName, Integer firstInterval, Integer repeatInterval, Integer repeatCount, Scope scope) {
		this.name = name;
		this.fullName = fullName;
		this.scope = scope;

		this.firstInterval = firstInterval;
		this.repeatInterval = repeatInterval;
		this.repeatCount = repeatCount == null ? MAX_REPEAT : repeatCount;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public Integer getFirstInterval() {
		return firstInterval;
	}

	public Integer getRepeatInterval() {
		return repeatInterval;
	}

	public Scope getScope() {
		return scope;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(int color) {
		this.colorIndex = color;
	}

	public Integer getColorIndex() {
		return colorIndex;
	}

	public void start(final Long timeFrom) {
		this.timeFrom = timeFrom;
		timeTo = timeFrom;
		state = State.NEW;
		lastRepeatCounter = 1;
	}

	public void cancel() {
		state = State.STOPPED;
	}

	public void update(final Long timeNow) {

		switch (state) {
			case NEW:
				state = State.RUNNING;
				break;
			case FINISHED:
			case STOPPED:
				return;
			case RUNNING:
				// nothing to do (avoid race condition)
		}

		if (timeNow < (timeFrom + firstInterval)) {
			timeTo = timeFrom + firstInterval;
			timeRemaining = timeTo - timeNow;
			repeatCounter = 1;
			progress = 1.0 * timeRemaining / firstInterval;
			runningTick(timeTo, timeRemaining);
			return;
		}

		if (repeatInterval != null) {
			for (int i = 1; i < repeatCount; i++) {
				if (timeNow < (timeFrom + firstInterval + i * repeatInterval)) {
					timeTo = timeFrom + firstInterval + i * repeatInterval;
					timeRemaining = timeTo - timeNow;
					repeatCounter = i + 1;
					if (lastRepeatCounter < repeatCounter) {
						expiredRepeat(timeTo - repeatInterval);
						lastRepeatCounter = repeatCounter;
					}
					progress = 1.0 * timeRemaining / repeatInterval;
					runningTick(timeTo, timeRemaining);
					return;
				}
			}
		}

		if (State.RUNNING.equals(state)) {
			state = State.FINISHED;
		}
		timeRemaining = 0L;
		progress = 0;
		expired(timeFrom + firstInterval + (repeatInterval == null ? 0 : (repeatInterval * (repeatCount - 1))));
		return;
	}

	protected void runningTick(long timeTo, long timeRemaining) {

	}

	protected void expiredRepeat(long timeTo) {

	}

	protected void expired(long timeTo) {

	}

	public boolean isVisual() {
		return colorIndex != null || color != null;
	}

	public Long getTimeTo() {
		return timeTo;
	}

	public Long getTimeRemaining() {
		return timeRemaining;
	}

	public int getRepeatCounter() {
		return repeatCounter;
	}

	public double getProgress() {
		return progress;
	}

	public boolean isNew() {
		return state.equals(State.NEW);
	}

	public boolean isFinished() {
		return state.equals(State.FINISHED) || state.equals(State.STOPPED);
	}

	public void fillConfig(ConfigTimer configTimer) {
		configTimer.setColor(TimerManager.getSystemColor(this));
		configTimer.setAudio(null);
		configTimer.setCountdownVoice(null);
	}

	public boolean doOverrideExpiringThreshold() {
		return false;
	}

	public String toString() {
		return name + " (" + (repeatInterval == null
			? "one time in " + firstInterval
			: "first in " + firstInterval + ", then each " + repeatInterval)
			+ ") @ " + (timeFrom != null ? Format.formatTime(timeFrom, true, true) : "*")
			+ (timeRemaining != null ? " (" + Format.formatTime(timeRemaining) + ")" : "")
			+ " [" + state + " " + scope + "]";
	}
}
