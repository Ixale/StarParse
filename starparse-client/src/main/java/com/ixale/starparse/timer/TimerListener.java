package com.ixale.starparse.timer;

public interface TimerListener {

	public void onTimersTick();

	public void onTimerUpdated(final BaseTimer timer);

	public void onTimerFinished(final BaseTimer timer);

	public void onTimersReset();

}
