package com.ixale.starparse.ws;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;

public class Utils {

	public static final String
		ENDPOINT_RAID_GROUP = "/ws/raid-group",
		ENDPOINT_RAID = "/ws/raid",

		HEADER_VERSION = "version",
		HEADER_REMOTE_USER = "remote-user";

	public static String formatSession(final Session session, final EndpointConfig config) {
		return "Session #" + session.getId()
			+ " @ " + config.getUserProperties().get(HEADER_REMOTE_USER)
			+ " (" + config.getUserProperties().get(HEADER_VERSION) + ")";
	}
}
