package com.ixale.starparse.domain;

import java.io.Serializable;

public class CombatLog implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int logId;

	private final String fileName;
	private long timeFrom;

	private String characterName;

	public CombatLog(int logId, String fileName, long timeFrom) {
		this.logId = logId;
		this.fileName = fileName;
		this.timeFrom = timeFrom;
	}

	public int getLogId() {
		return logId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setTimeFrom(long timeFrom) {
		this.timeFrom = timeFrom;
	}

	public long getTimeFrom() {
		return timeFrom;
	}

	public void setCharacterName(String characterName) {
		this.characterName = characterName;
	}

	public String getCharacterName() {
		return characterName;
	}

	public String toString() {
		return logId+" ["+fileName+"]: ["+characterName+"]";
	}
}
