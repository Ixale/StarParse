package com.ixale.starparse.domain.stats;

public class DamageTakenStats {

	private final String source, name;
	private final long guid;

	private final int
		ticks, ticksShield, ticksMiss,
		total, max, totalIe, totalAbsorbed,
		dtps;

	private final double percentTotal,
		averageNormal,
		percentShield, percentMiss;

	private final String damageType;

	private final Long timeFrom, timeTo;

	public DamageTakenStats(String source, String name, long guid, int ticks,
		int ticksShield, int ticksMiss, int total, int totalIe, int max,
		int totalAbsorbed, int dtps,
		double percentTotal, double averageNormal,
		double percentShield, double percentMiss, String damageType,
		Long timeFrom, Long timeTo) {
		super();
		this.source = source;
		this.name = name;
		this.guid = guid;
		this.ticks = ticks;
		this.ticksShield = ticksShield;
		this.ticksMiss = ticksMiss;
		this.total = total;
		this.totalIe = totalIe;
		this.max = max;
		this.totalAbsorbed = totalAbsorbed;
		this.dtps = dtps;
		this.percentTotal = percentTotal;
		this.averageNormal = averageNormal;
		this.percentShield = percentShield;
		this.percentMiss = percentMiss;
		this.damageType = damageType;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
	}

	public String getSource() {
		return source;
	}

	public String getName() {
		return name;
	}

	public long getGuid() {
		return guid;
	}

	public int getTicks() {
		return ticks;
	}

	public int getTicksShield() {
		return ticksShield;
	}

	public int getTicksMiss() {
		return ticksMiss;
	}

	public int getTotal() {
		return total;
	}

	public int getTotalIe() {
		return totalIe;
	}

	public int getMax() {
		return max;
	}

	public int getTotalAbsorbed() {
		return totalAbsorbed;
	}

	public int getDtps() {
		return dtps;
	}

	public double getPercentTotal() {
		return percentTotal;
	}

	public double getAverageNormal() {
		return averageNormal;
	}

	public double getPercentShield() {
		return percentShield;
	}

	public double getPercentMiss() {
		return percentMiss;
	}

	public String getDamageType() {
		return damageType;
	}

	public Long getTimeFrom() {
		return timeFrom;
	}

	public Long getTimeTo() {
		return timeTo;
	}
}
