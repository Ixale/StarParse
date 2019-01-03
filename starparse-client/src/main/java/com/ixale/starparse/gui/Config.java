package com.ixale.starparse.gui;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.ixale.starparse.domain.CombatLog;
import com.ixale.starparse.domain.ConfigAttacks;
import com.ixale.starparse.domain.ConfigCharacter;
import com.ixale.starparse.domain.ConfigPopoutDefault;
import com.ixale.starparse.domain.ConfigTimers;
import com.ixale.starparse.domain.RaidGroup;

public class Config implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Hotkey {
		RAID_PULL("Raid Pull"), LOCK_OVERLAYS("Lock Overlays");

		String label;

		Hotkey(String l) {
			this.label = l;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public static final String DEFAULT_CHARACTER = "@Default",
		DEFAULT_POPOUT = "@DefaultPopout",
		DEFAULT_TIME_SYNC_HOST = "time.nist.gov",
		DEFAULT_SERVER_HOST = "ixparse.com:8080/starparse",
		SECURED_SERVER_HOST = "ixparse.com:443/starparse",
		DEFAULT_LOG_DIRECTORY = System.getProperty("user.home") + "/Documents/Star Wars - The Old Republic/CombatLogs/",
		PARSELY_UPLOAD_API = "https://parsely.io/api/upload";

	public static final int DEFAULT_RECENT_PARSED_LOGS_LIMIT = 5,
		DEFAULT_RECENT_OPENED_LOGS_LIMIT = 5,

	DEFAULT_POPOUT_SNAP = 10,

	DEFAULT_RAID_PULL_SECONDS = 10,
		DEFAULT_RAID_BREAK_MINUTES = 15;

	public static final double DEFAULT_POPOUT_OPACITY = 0.6;

	private String logDirectory;
	private int logPolling = 3000;

	private Double windowWidth, windowHeight, windowX, windowY;

	private Integer recentParsedLogsLimit;
	private Integer recentOpenedLogsLimit;

	private String lastVersion = "0.1a";

	private final ArrayList<CombatLog> recentParsedLogs = new ArrayList<CombatLog>();
	private final ArrayList<CombatLog> recentOpenedLogs = new ArrayList<CombatLog>();

	private Integer popoutSnap;
	private ConfigPopoutDefault popoutDefault;

	private ArrayList<ConfigCharacter> characters;
	private String lastCharacterName;

	private ArrayList<RaidGroup> raidGroups;
	private String lastRaidGroupName;

	private Boolean timeSyncEnabled;
	private String timeSyncHost;

	private String serverHost;

	private Boolean storeDataOnServerEnabled;

	private String timezone, parselyLogin, parselyPasswordEnc, parselyEndpoint;

	private Integer raidPullSeconds, raidBreakMinutes;
	private String raidPullHotkey, lockOverlaysHotkey;

	transient private ConfigTimers configTimers;
	transient private ConfigAttacks configAttacks;

	public Config() {
		//
	}

	public Double getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(Double windowWidth) {
		this.windowWidth = windowWidth;
	}

	public Double getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(Double windowHeight) {
		this.windowHeight = windowHeight;
	}

	public Double getWindowX() {
		return windowX;
	}

	public void setWindowX(Double windowX) {
		this.windowX = windowX;
	}

	public Double getWindowY() {
		return windowY;
	}

	public void setWindowY(Double windowY) {
		this.windowY = windowY;
	}

	public void addRecentParsedLog(CombatLog combatLog) {
		// already there?
		for (CombatLog l: recentParsedLogs) {
			if (l.getFileName().equals(combatLog.getFileName())) {
				return;
			}
		}

		recentParsedLogs.add(combatLog);
		if (recentParsedLogs.size() > getRecentParsedLogsLimit()) {
			recentParsedLogs.remove(0);
		}
	}

	public void addRecentOpenedLog(CombatLog combatLog) {
		// already there?
		for (CombatLog l: recentOpenedLogs) {
			if (l.getFileName().equals(combatLog.getFileName())) {
				return;
			}
		}

		recentOpenedLogs.add(combatLog);
		if (recentOpenedLogs.size() > getRecentOpenedLogsLimit()) {
			recentOpenedLogs.remove(0);
		}
	}

	public ConfigCharacter getDefaultCharacter() {
		return getCharacter(DEFAULT_CHARACTER);
	}

	public ConfigCharacter getCurrentCharacter() {
		if (lastCharacterName == null) {
			return getDefaultCharacter();
		}
		return getCharacter(lastCharacterName);
	}

	public boolean isKnownCharacter(final String characterName) {
		if (characters == null) {
			return false;
		}
		for (final ConfigCharacter ch: characters) {
			if (ch.getName().equals(characterName)) {
				return true;
			}
		}
		return false;
	}

	private ConfigCharacter getCharacter(final String characterName) {
		if (characters == null) {
			characters = new ArrayList<ConfigCharacter>();
		}

		for (final ConfigCharacter ch: characters) {
			if (ch.getName().equals(characterName)) {
				return ch;
			}
		}
		final ConfigCharacter ch = new ConfigCharacter(characterName);
		characters.add(ch);

		return ch;
	}

	public List<CombatLog> getRecentParsedLogs() {
		return recentParsedLogs;
	}

	public List<CombatLog> getRecentOpenedLogs() {
		return recentOpenedLogs;
	}

	public void setLogDirectory(String logDirectory) {
		this.logDirectory = logDirectory;
	}

	public String getLogDirectory() {
		return logDirectory != null ? logDirectory : DEFAULT_LOG_DIRECTORY;
	}

	public int getLogPolling() {
		return logPolling;
	}

	public String getLastVersion() {
		return lastVersion;
	}

	public void setLastVersion(String lastVersion) {
		this.lastVersion = lastVersion;
	}

	public int getRecentParsedLogsLimit() {
		return recentParsedLogsLimit != null ? recentParsedLogsLimit : DEFAULT_RECENT_PARSED_LOGS_LIMIT;
	}

	public void setRecentParsedLogsLimit(int recentParsedLogsLimit) {
		this.recentParsedLogsLimit = recentParsedLogsLimit;
	}

	public int getRecentOpenedLogsLimit() {
		return recentOpenedLogsLimit != null ? recentOpenedLogsLimit : DEFAULT_RECENT_OPENED_LOGS_LIMIT;
	}

	public void setRecentOpenedLogsLimit(int recentOpenedLogsLimit) {
		this.recentOpenedLogsLimit = recentOpenedLogsLimit;
	}

	public void setLastCharacterName(String name) {
		lastCharacterName = name;
	}

	public Boolean isTimeSyncEnabled() {
		return timeSyncEnabled != null ? timeSyncEnabled : true;
	}

	public void setTimeSyncEnabled(Boolean tymSyncEnabled) {
		this.timeSyncEnabled = tymSyncEnabled;
	}

	public Boolean isStoreDataOnServerEnabled() {
		return storeDataOnServerEnabled != null ? storeDataOnServerEnabled : true;
	}

	public void setStoreDataOnServerEnabled(Boolean storeDataOnServerEnabled) {
		this.storeDataOnServerEnabled = storeDataOnServerEnabled;
	}

	public ArrayList<RaidGroup> getRaidGroups() {
		if (raidGroups == null) {
			raidGroups = new ArrayList<RaidGroup>();
		}
		return raidGroups;
	}

	public String getTimeSyncHost() {
		return timeSyncHost != null ? timeSyncHost : DEFAULT_TIME_SYNC_HOST;
	}

	public void setTimeSyncHost(String timeSyncHost) {
		this.timeSyncHost = timeSyncHost;
	}

	public String getServerHost() {
		if (serverHost != null) {
			return serverHost;
		}
		return getDefaultServerHost();
	}

	public String getSecuredServerHost() {
		return SECURED_SERVER_HOST;
	}

	public String getDefaultServerHost() {
		return DEFAULT_SERVER_HOST;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public String getLastRaidGroupName() {
		return lastRaidGroupName;
	}

	public boolean isRaidGroupAdmin(final String raidGroupName) {
		for (RaidGroup group: getRaidGroups()) {
			if (group.getName() != null && group.getName().equals(raidGroupName)) {
				return group.getAdminPassword() != null && !group.getAdminPassword().isEmpty();
			}
		}
		return false;
	}

	public void setLastRaidGroupName(String lastRaidGroupName) {
		this.lastRaidGroupName = lastRaidGroupName;
	}

	public Integer getPopoutSnap() {
		return popoutSnap == null ? DEFAULT_POPOUT_SNAP : popoutSnap;
	}

	public void setPopoutSnap(Integer popoutSnap) {
		this.popoutSnap = popoutSnap;
	}

	public ConfigPopoutDefault getPopoutDefault() {
		if (popoutDefault == null) {
			popoutDefault = new ConfigPopoutDefault();
		}
		return popoutDefault;
	}

	public String getTimezone() {
		return (timezone == null ? TimeZone.getDefault() : TimeZone.getTimeZone(timezone)).getID();
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getParselyLogin() {
		return parselyLogin;
	}

	public void setParselyLogin(String parselyLogin) {
		if (parselyLogin == null || parselyLogin.isEmpty()) {
			this.parselyLogin = null;
		} else {
			this.parselyLogin = parselyLogin;
		}
	}

	@SuppressWarnings("restriction")
	public String getParselyPassword() {
		if (this.parselyPasswordEnc == null || this.parselyPasswordEnc.isEmpty()) {
			return null;
		}
		try {
			return new String(new sun.misc.BASE64Decoder().decodeBuffer(this.parselyPasswordEnc));
		} catch (IOException e) {
			return null;
		}
	}

	@SuppressWarnings("restriction")
	public void setParselyPassword(String parselyPassword) {
		if (parselyPassword == null || parselyPassword.isEmpty()) {
			this.parselyPasswordEnc = null;
			return;
		}
		this.parselyPasswordEnc = new sun.misc.BASE64Encoder().encode(parselyPassword.getBytes());
	}

	public String getParselyEndpoint() {
		return parselyEndpoint != null ? parselyEndpoint : PARSELY_UPLOAD_API;
	}

	public Integer getRaidBreakMinutes() {
		return raidBreakMinutes != null ? raidBreakMinutes : DEFAULT_RAID_BREAK_MINUTES;
	}

	public void setRaidBreakMinutes(Integer raidBreakMinutes) {
		this.raidBreakMinutes = raidBreakMinutes;
	}

	public Integer getRaidPullSeconds() {
		return raidPullSeconds != null ? raidPullSeconds : DEFAULT_RAID_PULL_SECONDS;
	}

	public void setRaidPullSeconds(Integer raidPullSeconds) {
		this.raidPullSeconds = raidPullSeconds;
	}

	public String getRaidPullHotkey() {
		return raidPullHotkey;
	}

	public void setRaidPullHotkey(String raidPullHotkey) {
		this.raidPullHotkey = raidPullHotkey;
	}

	public String getlockOverlaysHotkey() {
		return lockOverlaysHotkey;
	}

	public void setlockOverlaysHotkey(String lockOverlaysHotkey) {
		this.lockOverlaysHotkey = lockOverlaysHotkey;
	}

	public ConfigTimers getConfigTimers() {
		if (configTimers == null) {
			configTimers = new ConfigTimers();
		}
		return configTimers;
	}

	public void setConfigTimers(final ConfigTimers configTimers) {
		this.configTimers = configTimers;
	}

	public void setConfigAttacks(final ConfigAttacks configAttacks) {
		this.configAttacks = configAttacks;
	}

	public ConfigAttacks getConfigAttacks() {
		if (configAttacks == null) {
			configAttacks = new ConfigAttacks();
		}
		return configAttacks;
	}

	public String toString() {
		return "Config [" + lastCharacterName + "] (" + lastVersion + ")";
	}
}
