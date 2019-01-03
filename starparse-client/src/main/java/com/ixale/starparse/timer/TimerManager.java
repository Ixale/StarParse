package com.ixale.starparse.timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ixale.starparse.domain.ConfigPopoutDefault;
import com.ixale.starparse.domain.ConfigTimer;
import com.ixale.starparse.domain.ConfigTimer.Condition;
import com.ixale.starparse.domain.ops.DreadPalace;
import com.ixale.starparse.domain.ops.Ravagers;
import com.ixale.starparse.domain.ops.ScumAndVillainy;
import com.ixale.starparse.domain.ops.TempleOfSacrifice;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.time.TimeUtils;

import javafx.scene.paint.Color;

public class TimerManager {

	public static final int POLLING = 200;
	private static final int MAX_TIMER_AGE = 30 * 60 * 1000;

	private static final Logger logger = LoggerFactory.getLogger(TimerManager.class);

	private static Config config;
	private static Worker worker = null;

	private static final ArrayList<BaseTimer> timers = new ArrayList<BaseTimer>();
	private static final ArrayList<TimerListener> listeners = new ArrayList<TimerListener>();

	private static Long startTime = null;

	private static boolean isMuted = false;

	public static void addListener(final TimerListener listener) {
		listeners.add(listener);
	}

	public static void setConfig(final Config config) {
		TimerManager.config = config;
	}

	public static void startTimer(Class<? extends BaseTimer> clazz, final Long timeFrom) {
		startTimer(clazz, timeFrom, null);
	}

	public static void startTimer(Class<? extends BaseTimer> clazz, final Long timeFrom, final Integer interval) {
		if (startTime == null || timeFrom < (startTime - MAX_TIMER_AGE)) {
			return;
		}
		try {
			// lookup enclosing configuration
			for (final String raidName: systemTimers.keySet()) {
				for (final BaseTimer systemTimer: systemTimers.get(raidName).keySet()) {
					if (systemTimer.getClass() == clazz) {
						final ConfigTimer configTimer = systemTimers.get(raidName).get(systemTimer);
						if (!configTimer.isEnabled()) {
							// sorry
							return;
						}
						if (configTimer.isSystemModified() || interval != null) {
							if (interval != null && isAlreadyRunning(configTimer, timeFrom)) {
								return;
							}
							// use this instead
							startTimer(new CustomTimer(configTimer, clazz.newInstance(), interval), timeFrom);
							return;
						}
					}
				}
			}

			final BaseTimer timer = clazz.newInstance();
			startTimer(timer, timeFrom);

		} catch (Exception e) {
			logger.error("Unable to start timer", e);
		}
	}

	public static void startTimer(final ConfigTimer configTimer, final long timeFrom) {

		if (startTime == null || timeFrom < startTime) {
			return;
		}

		if (isAlreadyRunning(configTimer, timeFrom)) {
			return;
		}

		final CustomTimer timer = new CustomTimer(configTimer);

		// any parallel / next timers?
		for (final ConfigTimer otherTimer: config.getConfigTimers().getTimers()) {
			if (otherTimer.isSystem()) {
				continue;
			}
			if (otherTimer.getTrigger() != null && otherTimer.getTrigger().getType().equals(Condition.Type.TIMER_STARTED)) {
				if (otherTimer.getTrigger().getTimer().equals(configTimer.getName())) {
					// start the timer together with this one
					startTimer(otherTimer, timeFrom);
				}
			}
			if (otherTimer.getTrigger() != null && otherTimer.getTrigger().getType().equals(Condition.Type.TIMER_FINISHED)) {
				if (otherTimer.getTrigger().getTimer().equals(configTimer.getName())) {
					// set the timer as the next one once this finishes
					timer.getNextTimers().add(otherTimer);
				}
			}
			if (otherTimer.getCancel() != null && otherTimer.getCancel().getType().equals(Condition.Type.TIMER_STARTED)) {
				if (otherTimer.getCancel().getTimer().equals(configTimer.getName())) {
					// cancel the timer as this one started
					TimerManager.stopTimer(otherTimer.getName());
				}
			}
			if (otherTimer.getCancel() != null && otherTimer.getCancel().getType().equals(Condition.Type.TIMER_FINISHED)) {
				if (otherTimer.getCancel().getTimer().equals(configTimer.getName())) {
					// set the timer for cancellation once this finishes
					timer.getCancelTimers().add(otherTimer);
				}
			}
		}

		startTimer(timer, timeFrom);
	}

	private static void startTimer(final BaseTimer timer, final Long timeFrom) {
		timer.start(timeFrom);

		if (logger.isDebugEnabled()) {
			logger.debug("Started timer: " + timer);
		}

		timers.add(timer);
	}

	private static boolean isAlreadyRunning(final ConfigTimer configTimer, final long timeFrom) {
		// running already?
		final BaseTimer running = getTimer(configTimer.getName());
		if (running != null) {
			if (configTimer.isIgnoreRepeated()) {
				// keep running, nothing to do
			} else {
				// restart
				running.start(timeFrom);
			}
			return true;
		}
		return false;
	}

	public static BaseTimer getTimer(final Class<? extends BaseTimer> clazz) {
		final Iterator<BaseTimer> iterator = timers.iterator();
		while (iterator.hasNext()) {
			final BaseTimer timer = iterator.next();
			if (timer != null) {
				if (timer.getClass() == clazz) {
					return timer;
				}
				if (timer instanceof CustomTimer
					&& ((CustomTimer) timer).getSystemTimer() != null
					&& ((CustomTimer) timer).getSystemTimer().getClass() == clazz) {
					return timer;
				}
			}
		}
		return null;
	}

	public static void stopTimer(final Class<? extends BaseTimer> clazz) {
		final BaseTimer timer = getTimer(clazz);
		if (timer != null) {
			timer.cancel();
		}
	}

	public static BaseTimer getTimer(final String name) {
		final Iterator<BaseTimer> iterator = timers.iterator();
		while (iterator.hasNext()) {
			final BaseTimer timer = iterator.next();
			if (timer != null && (name.equals(timer.getName()) || name.equals(timer.getFullName()))) {
				return timer;
			}
		}
		return null;
	}

	public static void stopTimer(final String name) {
		final BaseTimer timer = getTimer(name);
		if (timer != null) {
			timer.cancel();
		}
	}

	public static void stopAllTimers(final BaseTimer.Scope scope) {
		final Iterator<BaseTimer> iterator = timers.iterator();
		while (iterator.hasNext()) {
			final BaseTimer timer = iterator.next();
			if (timer != null && timer.getScope().equals(scope)) {
				timer.cancel();
			}
		}
	}

	public static void start() {

		logger.debug("Enabled");

		startTime = TimeUtils.getCurrentTime();

		reset();

		worker = new Worker();
		worker.setDaemon(true);
		worker.start();
	}

	public static void stop() {

		logger.debug("Disabled");

		startTime = null;

		reset();
	}

	public static void reset() {

		if (worker != null) {
			worker.interrupt();
			try {
				worker.join();
			} catch (InterruptedException e) {
			}

			worker = null;
		}

		timers.clear();

		fireTimersReset();
	}

	public static Long getStartTime() {
		return startTime;
	}

	private static void fireTimerUpdated(final BaseTimer timer) {
		for (TimerListener listener: listeners) {
			listener.onTimerUpdated(timer);
		}
	}

	private static void fireTimerFinished(final BaseTimer timer) {
		for (TimerListener listener: listeners) {
			listener.onTimerFinished(timer);
		}
	}

	private static void fireTimersReset() {
		for (TimerListener listener: listeners) {
			listener.onTimersReset();
		}
	}

	private static void fireTimersTick() {
		for (TimerListener listener: listeners) {
			listener.onTimersTick();
		}
	}

	static class Worker extends Thread {

		public Worker() {
			setName("TimersWorker");
		}

		public void run() {

			logger.debug("Started");

			while (!isInterrupted()) {
				try {
					fireTimersTick();

					final Iterator<BaseTimer> iterator = timers.iterator();
					while (iterator.hasNext()) {
						BaseTimer timer = null;
						try {
							timer = iterator.next();
							if (timer == null) {
								logger.error("Timer collection contained empty item: " + Arrays.asList(timers));
								iterator.remove();
								continue;
							}
							processTimer(iterator, timer);

						} catch (ConcurrentModificationException e) {
							logger.debug("Concurrent modification caught, stopping");
							break;

						} catch (Exception e) {
							logger.error("Unable to evaluate timer: " + timer, e);

						}
					}

					Thread.sleep(POLLING);

				} catch (InterruptedException e) {
					// stop
					break;

				} catch (Exception e) {
					logger.error("General error", e);
				}
			}

			logger.debug("Finished");
		}

		private void processTimer(final Iterator<BaseTimer> iterator, final BaseTimer timer) {

			if (timer.isFinished()) {
				if (!timer.isNew()) {
					// do not fire finish if it has not been announced at all
					fireTimerFinished(timer);
				}
				// logger.debug("Removed timer: "+timer);
				iterator.remove();
				return;
			}

			fireTimerUpdated(timer);
		}
	}

	private static final Map<String, Map<BaseTimer, ConfigTimer>> systemTimers = new HashMap<>();
	private static final List<Color> timerColors = new ArrayList<>();
	static {
		timerColors.add(ConfigPopoutDefault.DEFAULT_TIMER1);
		timerColors.add(ConfigPopoutDefault.DEFAULT_TIMER2);
		timerColors.add(ConfigPopoutDefault.DEFAULT_TIMER3);
		timerColors.add(ConfigPopoutDefault.DEFAULT_TIMER4);
	}

	public static void linkSystemTimers(final List<ConfigTimer> timers) {
		// SaV
		final Map<BaseTimer, ConfigTimer> savTimers = new HashMap<>();
		savTimers.put(new ScumAndVillainy.StyrakKnockbackTimer(), null);
		systemTimers.put("Scum & Villainy", savTimers);
		// DP
		final Map<BaseTimer, ConfigTimer> dpTimers = new HashMap<>();
		dpTimers.put(new DreadPalace.BestiaBossActivatesTimer(), null);
		dpTimers.put(new DreadPalace.BestiaLastMonsterTimer(), null);
		dpTimers.put(new DreadPalace.BestiaSoftEnrageTimer(), null);
		dpTimers.put(new DreadPalace.CouncilTyransDmP1Timer(), null);
		dpTimers.put(new DreadPalace.CouncilTyransTpP1Timer(), null);
		dpTimers.put(new DreadPalace.CouncilBrontesTpTimer(), null);
		dpTimers.put(new DreadPalace.CouncilTyransDmP3Timer(), null);
		systemTimers.put("Dread Palace", dpTimers);
		// RAV
		final Map<BaseTimer, ConfigTimer> ravTimers = new HashMap<>();
		ravTimers.put(new Ravagers.TorqueRageTimer(), null);
		ravTimers.put(new Ravagers.TorqueEnrageTimer(), null);
		ravTimers.put(new Ravagers.RuugarEnrageTimer(), null);
		systemTimers.put("Ravagers", ravTimers);
		// TOS
		final Map<BaseTimer, ConfigTimer> tosTimers = new HashMap<>();
		tosTimers.put(new TempleOfSacrifice.SwordSquadronShieldTimer(), null);
		tosTimers.put(new TempleOfSacrifice.SwordSquadronPullTimer(), null);
		tosTimers.put(new TempleOfSacrifice.SwordSquadronEnrageTimer(), null);
		tosTimers.put(new TempleOfSacrifice.UnderlurkerAddsTimer(), null);
		tosTimers.put(new TempleOfSacrifice.UnderlurkerEnrageTimer(), null);
		tosTimers.put(new TempleOfSacrifice.RevanHeaveTimer(), null);
		tosTimers.put(new TempleOfSacrifice.RevanPullTimer(), null);
		tosTimers.put(new TempleOfSacrifice.RevanPushTimer(), null);
		systemTimers.put("Temple of Sacrifice", tosTimers);

		// Raiding
		final Map<BaseTimer, ConfigTimer> raidTimers = new HashMap<>();
		raidTimers.put(new RaidPullTimer(), null);
		raidTimers.put(new RaidBreakTimer(), null);
		systemTimers.put("Raiding", raidTimers);

		// verify first
		final Iterator<ConfigTimer> items = timers.iterator();
		while (items.hasNext()) {
			final ConfigTimer item = items.next();
			if (!item.isSystem() && item.getTrigger() == null) {
				// invalid, remove // TODO: investigate
				items.remove();
				logger.warn("Removed invalid timer: " + item);
			}
		}

		for (final String raidName: systemTimers.keySet()) {
			for (final BaseTimer systemTimer: systemTimers.get(raidName).keySet()) {
				// already there?
				ConfigTimer configTimer = null;
				for (final ConfigTimer timer: timers) {
					if (timer.isSystem() && timer.getName().equals(systemTimer.getFullName())) {
						// found, link
						configTimer = timer;
						break;
					}
				}
				if (configTimer == null) {
					// setup
					configTimer = new ConfigTimer();
					configTimer.setName(systemTimer.getFullName());
					configTimer.setFolder(ConfigTimer.SYSTEM_FOLDER + " " + raidName);
					configTimer.setEnabled(true);

					systemTimer.fillConfig(configTimer);

					timers.add(configTimer);
				}
				systemTimers.get(raidName).put(systemTimer, configTimer);
			}
		}
	}

	public static BaseTimer getSystemTimer(final ConfigTimer timer) {
		for (final String raidName: systemTimers.keySet()) {
			for (final BaseTimer systemTimer: systemTimers.get(raidName).keySet()) {
				if (systemTimers.get(raidName).get(systemTimer) == timer) {
					return systemTimer;
				}
			}
		}
		return null;
	}

	public static Color getSystemColor(final BaseTimer systemTimer) {
		return timerColors.get(systemTimer.getColorIndex() == null ? 0 : systemTimer.getColorIndex());
	}

	public static void setMuted(boolean isMuted) {
		TimerManager.isMuted = isMuted;
	}

	public static boolean isMuted() {
		return isMuted;
	}

	public static class RaidPullTimer extends BaseTimer {
		public RaidPullTimer() {
			super("Pull", "Raid Pull", 10 * 1000, null, null, Scope.ANY);
			setColor(0);
		}

		@Override
		public void fillConfig(final ConfigTimer configTimer) {
			super.fillConfig(configTimer);
			configTimer.setAudio("Alert.mp3");
			configTimer.setVolume(35);
			configTimer.setCountdownCount(5);
			configTimer.setCountdownVoice("Amy");
			configTimer.setCountdownVolume(35);
		}
	}

	public static class RaidBreakTimer extends BaseTimer {
		public RaidBreakTimer() {
			super("Break", "Raid Break", 300 * 1000, null, null, Scope.ANY);
			setColor(1);
		}
	}
}
