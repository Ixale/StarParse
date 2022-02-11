package com.ixale.starparse.domain;

import com.ixale.starparse.service.RankService.RankType;

public class RankClass {

	public enum Reason {
		RANK_DISABLED(-1),
		TICK_TOO_LOW(-101),
		NO_DATA_AVAILABLE(-102),
		PENDING(-999);

		final int code;

		Reason(final int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	private Reason reason;
	private Integer percent;
	private final RankType rankType;

	public RankClass(RankType rankType) {
		this.rankType = rankType;
	}

	public RankClass(final RankType rankType, final Reason reason) {
		this.rankType = rankType;
		this.reason = reason;
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
