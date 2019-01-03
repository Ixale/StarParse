package com.ixale.starparse.domain;

public class Combat {

	private final int combatId;
	private final int logId;

	private final long timeFrom;
	private Long timeTo;

	private final int eventIdFrom;
	private Integer eventIdTo;

	private boolean isRunning = true;

	private String name;
	private RaidBoss boss;
	private CharacterDiscipline discipline;

	private Boolean isPvp;

	public Combat(int combatId, int logId, long timeFrom, int eventIdFrom) {
		this.combatId = combatId;
		this.logId = logId;
		this.timeFrom = timeFrom;
		this.eventIdFrom = eventIdFrom;
	}

	public int getCombatId() {
		return combatId;
	}

	public int getLogId() {
		return logId;
	}

	public long getTimeFrom() {
		return timeFrom;
	}

	public int getEventIdFrom() {
		return eventIdFrom;
	}

	public void setTimeTo(Long timeTo) {
		this.timeTo = timeTo;
	}

	public Long getTimeTo() {
		return timeTo;
	}

	public void setEventIdTo(Integer eventIdTo) {
		this.eventIdTo = eventIdTo;
	}

	public Integer getEventIdTo() {
		return eventIdTo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RaidBoss getBoss() {
		return boss;
	}

	public void setBoss(RaidBoss boss) {
		this.boss = boss;
	}

	public CharacterDiscipline getDiscipline() {
		return discipline;
	}

	public void setDiscipline(CharacterDiscipline discipline) {
		this.discipline = discipline;
	}

	public Boolean isPvp() {
		return isPvp;
	}

	public void setIsPvp(Boolean isPvp) {
		this.isPvp = isPvp;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setIsRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public String toString() {
		return combatId + "@" + logId + " [" + timeFrom + "-" + timeTo + "]"
			+ " [" + eventIdFrom + "-" + eventIdTo + "]: [" + (boss != null ? boss : name) + "]"
			+ (discipline != null ? " (@" + discipline + ")" : "");
	}
}
