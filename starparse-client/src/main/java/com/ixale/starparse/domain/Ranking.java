package com.ixale.starparse.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ixale.starparse.service.RankService.RankType;

public class Ranking implements Serializable {

	private static final long serialVersionUID = 1L;

	public static class Percentile {

		private int p;
		private int v;

		public Percentile(int pct, int value) {
			this.p = pct;
			this.v = value;
		}

		public int getPercent() {
			return p;
		}

		public int getValue() {
			return v;
		}

		public String toString() {
			return p + "%: " + v;
		}
	}

	public static class Description {

		private int tick;
		private Date date;
		private String type, className, boss;

		@Override
		public String toString() {
			return boss + ": " + className + " " + type + "  (" + tick + ") @ " + date;
		}
	}

	private final List<Percentile> percentiles = new ArrayList<>();
	private Description description;

	public Ranking() {
	}

	public int getMinTick() {
		return description.tick;
	}

	public RankType getType() {
		return RankType.valueOf(description.type.toUpperCase());
	}

	public Date getDate() {
		return description.date;
	}

	public List<Percentile> getPercentiles() {
		return percentiles;
	}

	@Override
	public String toString() {
		return description + " (" + percentiles.size() + ")";
	}
}
