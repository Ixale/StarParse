package com.ixale.starparse.service;

import java.util.List;

import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.service.impl.Context;

public interface ParselyService {

	class Params {
		public String endpoint, serverName, timezone, guild, notes, username, password, version;
		public int visibility;
	}

	class ParselyCombatInfo {
		public Long from, to;
		public RaidBoss raidBoss;
		public boolean isNiMCrystal = false;
		public String instanceName;
		public Long instanceGuid;
	}

	Params createParams(Config config, int visibility, String notes, Context context);

	String uploadLog(Params p, String fileName, byte[] content, List<ParselyCombatInfo> combatsInfo) throws Exception;
}
