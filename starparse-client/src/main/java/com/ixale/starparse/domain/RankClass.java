package com.ixale.starparse.domain;

import com.ixale.starparse.service.RankService.RankType;

public class RankClass {

	public enum Reason {
		TICK_TOO_LOW,
		NO_DATA_AVAILABLE
	}

	private Reason reason;
	private Integer percent;
	private final RankType rankType;

	public RankClass(RankType rankType) {
		this.rankType = rankType;
	}

	public Reason getReason() {
		return reason;
	}

	public void setReason(Reason reason) {
		this.reason = reason;
	}

	public Integer getPercent() {
		return percent;
	}

	public void setPercent(Integer percent) {
		this.percent = percent;
	}

	public RankType getType() {
		return rankType;
	}
}
