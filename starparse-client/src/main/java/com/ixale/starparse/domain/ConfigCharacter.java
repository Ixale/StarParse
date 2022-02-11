package com.ixale.starparse.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class ConfigCharacter implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;

	private String server, guild;

	private ArrayList<ConfigPopout> popouts;

	public ConfigCharacter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getServer() {
		// fix server
		if (server != null) {
			try {
				server = Objects.requireNonNull(ServerName.getFromName(server)).getActive().getName();

			} catch (Exception e) {
				e.printStackTrace();

			}
		}
		return server;
	}

	public void setServer(final String server) {
		if (server == null || server.isEmpty()) {
			this.server = null;
		} else {
			this.server = server;
		}
	}

	public String getGuild() {
		return guild;
	}

	public void setGuild(String guild) {
		if (guild == null || guild.isEmpty()) {
			this.guild = null;
		} else {
			this.guild = guild;
		}
	}

	public ConfigPopout getPopout(String name) {
		if (popouts == null) {
			popouts = new ArrayList<>();
		}

		for (final ConfigPopout cp : popouts) {
			if (cp.getName().equals(name)) {
				return cp;
			}
		}
		final ConfigPopout cp = new ConfigPopout(name);
		popouts.add(cp);

		return cp;
	}

	public void resetAllPopouts() {
		if (popouts == null) {
			return;
		}
		for (final ConfigPopout p : popouts) {
			p.setHeight(null);
			p.setPositionX(null);
			p.setPositionY(null);
			p.setScale(null);
			// p.setMode(null);
		}
	}
}
