package com.ixale.starparse.raid;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.RaidRequest;
import com.ixale.starparse.domain.stats.AbsorptionStats;
import com.ixale.starparse.domain.stats.ChallengeStats;
import com.ixale.starparse.domain.stats.CombatEventStats;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.ws.RaidClient;
import com.ixale.starparse.ws.RaidClient.RequestIncomingCallback;
import com.ixale.starparse.ws.RaidClient.RequestOutgoingCallback;
import com.ixale.starparse.ws.RaidCombatMessage;
import com.ixale.starparse.ws.RaidRequestMessage;
import com.ixale.starparse.ws.RaidResponseMessage;

public class RaidManager {

	private static final Logger logger = LoggerFactory.getLogger(RaidManager.class);

	private static final int RECONNECT_TIMEOUT = 15000;
	private static final int RECONNECT_ATTEMPTS = 10;

	public enum State {
		CONNECTING, RUNNING, STOPPING, STOPPED
	}

	private State state = State.STOPPED;
	private boolean isEnabled = false;

	private Worker worker = null;
	private CountDownLatch raidingLatch;

	private final RaidListener listener;

	private RaidClient raidClient;

	private String raidGroupName, characterName;
	private boolean isGroupAdmin = false;
	private Integer lastEventId;
	private boolean lastCombatRunning;
	private int connectionAttempts = 0;

	private final Config config;

	class RaidResultHandler implements RaidClient.RaidResultHandler {

		@Override
		public void onSuccess(String message) {
			switch (state) {
				case CONNECTING:
					// connected, notify
					fireOnRaidStarted();
					state = State.RUNNING;
					connectionAttempts = 0;
					logger.info("Successfully joined " + raidGroupName + " as " + characterName + (isGroupAdmin ? " (with admin privileges)" : ""));
					break;
				case STOPPING:
					// stopped, notify
					state = State.STOPPED;
					break;
				default:
					// unexpected
					logger.warn("Unexpected success message [" + state + "]: " + message);
			}
		}

		@Override
		public void onError(String message) {

			isEnabled = false;
			boolean tryAgain = (message == null
				|| (!message.contains("Your version appears to be outdated")));

			switch (state) {
				case STOPPING:
					// ignore, nothing to do anyway
					state = State.STOPPED;
					break;
				case RUNNING:
					if (tryAgain && connectionAttempts++ < RECONNECT_ATTEMPTS) {
						// try again
						logger.warn("Trying to reconnect on error (was running, " + connectionAttempts + ")");
						fireOnError(message + ", attempting reconnect", true);
						stopRaid(true);
						try {
							Thread.sleep(RECONNECT_TIMEOUT);
						} catch (Exception ignored) {
						}
						startRaid();
						return;
					}
				case CONNECTING:
					// failed synchronous call during raidClient.startRaiding
					if (tryAgain && connectionAttempts++ < RECONNECT_ATTEMPTS) {
						// try again
						logger.warn("Trying to reconnect on error (was connecting, " + connectionAttempts + ")");
						fireOnError(message + ", attempting reconnect", true);
						try {
							Thread.sleep(RECONNECT_TIMEOUT);
						} catch (Exception ignored) {
						}
						raidClient.startRaiding(raidGroupName, characterName, config.isStoreDataOnServerEnabled());
						return;
					}
				default:
					fireOnError(message, false);
					stopRaid(false);
			}
		}

		@Override
		public void onClose(String message) {
			boolean tryAgain = (message == null
				|| !message.contains("You have joined the raid from another StarParse window"));

			if (tryAgain && (state == State.CONNECTING || state == State.RUNNING) && connectionAttempts++ < RECONNECT_ATTEMPTS) {
				// try again
				logger.warn("Trying to reconnect on close (" + connectionAttempts + ")");
				fireOnError(message + ", attempting reconnect", true);
				stopRaid(true);
				try {
					Thread.sleep(RECONNECT_TIMEOUT);
				} catch (Exception ignored) {
				}
				startRaid();
				return;
			}
			if (state != State.STOPPED) {
				fireOnError("Connection lost" + (message != null && !message.isEmpty() ? " (" + message + ")" : ""), false);
				stopRaid(false);
				isEnabled = false;
			}
		}

		@Override
		public void onPlayerJoin(final String[] characterNames, String raidGroupName) {
			if (!raidGroupName.equals(getRaidGroupName().toLowerCase())) {
				logger.warn("Received player join for invalid group: " + Arrays.asList(characterNames) + " @ "
					+ raidGroupName);
				return;
			}
			fireOnPlayerJoin(characterNames);
		}

		@Override
		public void onPlayerQuit(final String[] characterNames, String raidGroupName) {
			if (!raidGroupName.equals(getRaidGroupName().toLowerCase())) {
				logger.warn("Received player quit for invalid group: " + Arrays.asList(characterNames) + " @ "
					+ raidGroupName);
				return;
			}
			fireOnPlayerQuit(characterNames);
		}

		@Override
		public void onCombatUpdated(final RaidCombatMessage[] messages) {
			fireOnCombatUpdated(messages);
		}

		@Override
		public void onRequestIncoming(final RaidRequestMessage message, final RequestIncomingCallback callback) {
			fireOnRequestIncoming(message, callback);
		}
	};

	public RaidManager(final Config config, final RaidListener listener) {
		this.config = config;
		this.listener = listener;
	}

	public void setRaidGroup(final String raidGroupName, final boolean isGroupAdmin) {

		if (isEnabled && this.raidGroupName != null
			&& (raidGroupName == null || !raidGroupName.equals(this.raidGroupName))) {
			stopRaid(true);
		}

		this.isGroupAdmin = isGroupAdmin;
		this.raidGroupName = raidGroupName;

		if (isEnabled) {
			// try to start
			startRaid();
		}
	}

	public String getRaidGroupName() {
		return raidGroupName;
	}

	public boolean isGroupAdmin() {
		return isGroupAdmin;
	}

	public String getCharacterName() {
		return characterName;
	}

	public void setCharacterName(String characterName) {

		if (isEnabled && this.characterName != null
			&& (characterName == null || !characterName.equals(this.characterName))) {
			stopRaid(true);
		}

		this.characterName = characterName;

		if (isEnabled) {
			// try to start
			startRaid();
		}
	}

	public void enable() {
		isEnabled = true;
		startRaid();
	}

	public void disable() {
		isEnabled = false;
		stopRaid(true);
	}

	private void startRaid() {

		if (raidGroupName == null) {
			// fail
			if (config.getRaidGroups().isEmpty()) {
				fireOnError("No raid group selected, create or join them from the 'Raiding > Settings' menu", false);
			} else {
				fireOnError("No raid group selected, select one from the 'Raiding' menu first", false);
			}
			isEnabled = false;
			return;
		}
		if (characterName == null) {
			// wait
			return;
		}

		if (raidClient != null) {
			// already started
			return;
		}

		logger.debug("Attempting to start: " + characterName + " @ " + raidGroupName);

		lastEventId = null;
		lastCombatRunning = false;
		raidClient = new RaidClient(config, new RaidResultHandler());

		raidingLatch = new CountDownLatch(1);
		worker = new Worker();
		worker.setDaemon(true);
		worker.start();
	}

	private void stopRaid(boolean doWait) {

		if (worker != null && Thread.currentThread() == worker) {
			// failed during synchronous call
			worker.interrupt();
			return;

		} else {
			// failed as asynchronous reply from server
		}

		if (worker != null) {

			logger.debug("Attempting to stop [" + (doWait ? "sync" : "async") + "]): " + characterName + " @ "
				+ raidGroupName);

			try {
				raidingLatch.countDown();
			} catch (Exception e) {
			} // might be gone

			if (doWait) {
				try {
					worker.join();
				} catch (Exception e) {
				} // might be gone
			}
		}

		worker = null;
	}

	public RaidCombatMessage sendCombatUpdate(final Combat combat,
		final CombatStats combatStats,
		final List<AbsorptionStats> absorptionStats,
		final List<ChallengeStats> challengeStats,
		final List<CombatEventStats> combatEventStats) {

		if (!isEnabled()) {
			// should not happen
			logger.debug("Trying to update combat while raiding inactive, ignoring");
			return null;
		}

		if (!isRunning()) {
			logger.debug("Trying to update combat while raid client not ready (" + state + "), ignoring");
			return null;
		}

		if (combat.getEventIdTo() != null) {
			lastEventId = combat.getEventIdTo();
		}
		lastCombatRunning = combat.isRunning();

		return raidClient.updateCombat(combat, characterName, combatStats, absorptionStats, challengeStats, combatEventStats);
	}

	public void sendRequest(final RaidRequest request, final RequestOutgoingCallback callback) {

		if (!isEnabled() || !isRunning()) {
			// should not happen
			logger.debug("Trying to send request while raiding inactive, ignoring (" + request + ")");
			callback.onResponseIncoming(new RaidResponseMessage(null, "No longer raiding"));
			return;
		}

		raidClient.sendRequest(request, callback);
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isRunning() {
		return isEnabled() && raidClient != null && state == State.RUNNING;
	}

	public boolean isUpdateNeeded(final Combat combat) {
		if (!isRunning()) {
			return false;
		}
		// running or after last event ID recorded
		return lastEventId == null
			|| combat.getEventIdTo() == null
			|| combat.getEventIdTo() > lastEventId
			|| combat.isRunning() != lastCombatRunning;
	}

	class Worker extends Thread {

		public Worker() {
			setName("RaidWorker");
		}

		public void run() {

			logger.debug("Starting raid: " + characterName + " @ " + raidGroupName);

			state = RaidManager.State.CONNECTING;
			raidClient.startRaiding(raidGroupName, characterName, config.isStoreDataOnServerEnabled());

			if (!isInterrupted()) {
				// just wait
				try {
					raidingLatch.await();
				} catch (Exception e) {
				}
			}

			if (state != RaidManager.State.CONNECTING && !raidClient.isClosed()) {

				logger.debug("Stopping raid: " + characterName + " @ " + raidGroupName);

				state = RaidManager.State.STOPPING;
				raidClient.stopRaiding(raidGroupName, characterName);

			}

			state = RaidManager.State.STOPPED;

			// flood limit
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}

			if (!raidClient.isClosed()) {
				raidClient.close();
			}

			// cleanup
			raidClient = null;
			raidingLatch = null;

			logger.debug("Raid stopped: " + characterName + " @ " + raidGroupName);
			fireOnRaidStopped();
		}
	}

	private void fireOnRaidStarted() {
		listener.onRaidStarted();
	}

	private void fireOnRaidStopped() {
		listener.onRaidStopped();
	}

	private void fireOnError(String message, boolean reconnecting) {
		listener.onError(message, reconnecting);
	}

	private void fireOnPlayerJoin(final String[] characterNames) {
		listener.onPlayerJoin(characterNames);
	}

	private void fireOnPlayerQuit(final String[] characterNames) {
		listener.onPlayerQuit(characterNames);
	}

	private void fireOnCombatUpdated(final RaidCombatMessage[] messages) {
		listener.onCombatUpdated(messages);
	}

	private void fireOnRequestIncoming(final RaidRequestMessage message, final RequestIncomingCallback callback) {
		listener.onRequestIncoming(message, callback);
	}
}
