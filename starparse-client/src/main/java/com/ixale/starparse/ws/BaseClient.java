package com.ixale.starparse.ws;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ixale.starparse.gui.Config;

public abstract class BaseClient {

	private static final Logger logger = LoggerFactory.getLogger(BaseClient.class);
	private static final int TIMEOUT = 5;

	public enum State {
		NEW, WAITING, RUNNING, CLOSED
	}

	private State state = State.NEW;

	private Session session;
	private ClientManager client;

	private final ResultHandler resultHandler;
	private final Config config;
	private final String endpointPath;

	private CountDownLatch messageLatch;

	public BaseClient(Config config, String endpointPath, ResultHandler resultHandler) {
		this.config = config;
		this.endpointPath = endpointPath;
		this.resultHandler = resultHandler;
	}

	public interface ResultHandler {
		public void onSuccess(String message);

		public void onError(String message);

		public void onClose(String message);
	}

	protected void sendMessage(final BaseMessage message, boolean isRequestWithReply) {

		if (isRequestWithReply) {
			messageLatch = new CountDownLatch(1);
			state = State.WAITING;
		}

		String hostname = config.getServerHost();
		boolean firstAttemptFailed = false;
		do {
			if (firstAttemptFailed) {
				// a->b, b->a
				final String other = !hostname.equals(config.getSecuredServerHost())
					? config.getSecuredServerHost() : config.getDefaultServerHost();

				logger.warn("Unable to connect to " + hostname + ", failing over to " + other);
				hostname = other;
			}

			try {
				if (isRequestWithReply) {
					getSession(hostname).getBasicRemote().sendObject(message);
				} else {
					getSession(hostname).getAsyncRemote().sendObject(message);
				}

			} catch (Exception e) {
				logger.warn("Call failed: " + hostname + " " + (isRequestWithReply ? "sync" : "async") + " " + message.toString(), e);
				if (firstAttemptFailed) {
					// nothing to do, bail out
					session = null;
					client = null;
					resultHandler.onError("Unable to contact the server: " + e.getMessage());
					return;
				}
				firstAttemptFailed = true;
				continue;
			}

			if (state == State.WAITING) {
				try {
					messageLatch.await(TIMEOUT, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
				}
			}

			if (state == State.WAITING) {
				logger.warn("Call timeouted: " + hostname + " " + (isRequestWithReply ? "sync" : "async") + " " + message.toString());
				if (firstAttemptFailed) {
					// nothing to do, bail out
					session = null;
					client = null;
					resultHandler.onError("Unable to contact the server (connection timed out)");
					return;
				}
				firstAttemptFailed = true;
				continue;
			}

			if (firstAttemptFailed) {
				// if this works, set it as default
				config.setServerHost(hostname.equals(config.getDefaultServerHost()) ? null : hostname);
			}
			// we are done
			return;

		} while (true);
	}

	private Session getSession(String host) throws Exception {
		if (session == null || !session.isOpen()) {
			client = null;
		}
		if (client == null) {
			client = ClientManager.createClient("org.glassfish.tyrus.container.jdk.client.JdkClientContainer");
			final SslEngineConfigurator ssl = new SslEngineConfigurator(SSLContext.getDefault(), true, false, false);
			ssl.setHostVerificationEnabled(false);
			client.getProperties().put("org.glassfish.tyrus.client.sslEngineConfigurator", ssl);
			// this will trigger onOpen, loading the session
			client.connectToServer(this, URI.create((host.equals(config.getSecuredServerHost()) || host.contains(":443") ? "wss://" : "ws://") + host + endpointPath));
		}
		return session;
	}

	@OnOpen
	public void onOpen(Session session) {
		if (logger.isDebugEnabled()) {
			logger.debug("Session open: " + session.getId());
		}
		this.session = session;
	}

	@OnMessage
	public void onMessage(BaseMessage message) {

		if (message instanceof ResultMessage) {
			if (logger.isDebugEnabled()) {
				logger.debug("Incoming result message [" + message + "]");
			}

			final ResultMessage result = (ResultMessage) message;
			switch (result.getType()) {
				case ERROR:
					resultHandler.onError(result.getMessage());
					break;
				case SUCCESS:
					resultHandler.onSuccess(result.getMessage());
					break;
			}

		} else {
			logger.warn("Incoming invalid message [" + message + "]");
			resultHandler.onError("Server sent invalid message");
		}

		if (state == State.WAITING) {
			state = State.RUNNING;
			messageLatch.countDown();
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		if (logger.isDebugEnabled()) {
			logger.debug("Session closed (" + closeReason.getReasonPhrase() + ")");
		}
		state = State.CLOSED;

		if (messageLatch != null) {
			messageLatch.countDown();
		}

		final String message;
		if (closeReason.getReasonPhrase().equals("The WebSocket session timeout expired")) {
			message = "Disconnected due to long inactivity";
		} else {
			message = closeReason.getReasonPhrase();
		}
		resultHandler.onClose(message);
	}

	@OnError
	public void onError(Session session, Throwable thr) {
		logger.warn("Received error", thr);
		resultHandler.onError("Error while talking to the server: " + thr.getMessage());
	}

	public void close() {
		state = State.CLOSED;

		try {
			if (session != null && session.isOpen()) {
				session.close();
			}
		} catch (IOException ignored) {
		}
	}

	public boolean isClosed() {
		return state == State.CLOSED;
	}

	protected ResultHandler getResultHandler() {
		return resultHandler;
	}
}
