package com.ixale.starparse.domain.stats;

public class DamageDealtStats {

	private final String target, name;
	private final long guid;

	private final int actions,
		ticks, ticksNormal, ticksCrit, ticksMiss, 
		total, totalNormal, totalCrit,
		max, dps;

	private final double percentTotal,
		averageNormal, averageCrit, averageHit,
		percentCrit, percentMiss;

	private final String damageType;

	private final Long timeFrom, timeTo;

	public DamageDealtStats(String target, String name, long guid, int actions, int ticks,
			int ticksNormal, int ticksCrit, int ticksMiss, int total,
			int totalNormal, int totalCrit, int max, int dps,
			double percentTotal, double averageNormal, double averageCrit, double averageHit,
			double percentCrit, double percentMiss, String damageType,
			Long timeFrom, Long timeTo) {
		super();
		this.target = target;
		this.name = name;
		this.guid = guid;
		this.actions = actions;
		this.ticks = ticks;
		this.ticksNormal = ticksNormal;
		this.ticksCrit = ticksCrit;
		this.ticksMiss = ticksMiss;
		this.total = total;
		this.totalNormal = totalNormal;
		this.totalCrit = totalCrit;
		this.max = max;
		this.dps = dps;
		this.percentTotal = percentTotal;
		this.averageNormal = averageNormal;
		this.averageCrit = averageCrit;
		this.averageHit = averageHit;
		this.percentCrit = percentCrit;
		this.percentMiss = percentMiss;
		this.damageType = damageType;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
	}

	public String getTarget() {
		return target;
	}

	public String getName() {
		return name;
	}

	public long getGuid() {
		return guid;
	}

	public int getActions() {
		return actions;
	}

	public int getTicks() {
		return ticks;
	}

	public int getTicksNormal() {
		return ticksNormal;
	}

	public int getTicksCrit() {
		return ticksCrit;
	}

	public int getTicksMiss() {
		return ticksMiss;
	}

	public int getTotal() {
		return total;
	}

	public int getTotalNormal() {
		return totalNormal;
	}

	public int getTotalCrit() {
		return totalCrit;
	}

	public int getMax() {
		return max;
	}

	public int getDps() {
		return dps;
	}

	public double getPercentTotal() {
		return percentTotal;
	}

	public double getAverageNormal() {
		return averageNormal;
	}

	public double getAverageCrit() {
		return averageCrit;
	}

	public double getAverageHit() {
		return averageHit;
	}

	public double getPercentCrit() {
		return percentCrit;
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
