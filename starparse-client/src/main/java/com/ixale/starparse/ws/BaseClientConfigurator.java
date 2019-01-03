package com.ixale.starparse.ws;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpointConfig;

import com.ixale.starparse.gui.StarparseApp;

public class BaseClientConfigurator extends ClientEndpointConfig.Configurator {

	private static String ipAddress;

	public void beforeRequest(Map<String, List<String>> headers) {

		headers.put(Utils.HEADER_VERSION, Arrays.asList(new String[]{StarparseApp.VERSION}));
		headers.put(Utils.HEADER_REMOTE_USER, Arrays.asList(new String[]{getIp()}));
		
	}

	private String getIp() {
		if (ipAddress == null) {
			try {
				// LAN address mostly
				ipAddress = Inet4Address.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				ipAddress = "unknown";
			}
		}
		return ipAddress;
	}
}