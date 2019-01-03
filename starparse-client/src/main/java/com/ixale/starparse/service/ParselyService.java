package com.ixale.starparse.service;

import java.util.List;

import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.gui.Config;

public interface ParselyService {

	public static class Params {
		public String endpoint, serverName, timezone, guild, notes, username, password;
		public boolean isPublic;
	}

	public static class ParselyCombatInfo {
		public Long from, to;
		public RaidBoss raidBoss;
		public boolean isNiMCrystal = false;
	}

	Params createParams(Config config, boolean isPublic, String notes);

	String uploadLog(Params p, String fileName, byte[] content, List<ParselyCombatInfo> combatsInfo) throws Exception;
}
