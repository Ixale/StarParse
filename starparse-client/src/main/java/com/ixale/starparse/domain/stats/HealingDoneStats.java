package com.ixale.starparse.domain.stats;

public class HealingDoneStats {

	private final String target, name;
	private final long guid;

	private final int actions,
		ticks, ticksNormal, ticksCrit, 
		total, max, totalNormal, totalCrit, totalEffective,
		hps, ehps;

	private final double percentTotal,
		averageNormal, averageCrit,
		percentCrit, percentEffective;

	private final Long timeFrom, timeTo;

	public HealingDoneStats(String target, String name, long guid, int actions, int ticks,
			int ticksNormal, int ticksCrit, int total, int max,
			int totalNormal, int totalCrit, int totalEffective, int hps, int ehps,
			double percentTotal, double averageNormal, double averageCrit,
			double percentCrit, double percentEffective,
			Long timeFrom, Long timeTo) {
		super();
		this.target = target;
		this.name = name;
		this.guid = guid;
		this.actions = actions;
		this.ticks = ticks;
		this.ticksNormal = ticksNormal;
		this.ticksCrit = ticksCrit;
		this.total = total;
		this.max = max;
		this.totalNormal = totalNormal;
		this.totalCrit = totalCrit;
		this.totalEffective = totalEffective;
		this.hps = hps;
		this.ehps = ehps;
		this.percentTotal = percentTotal;
		this.averageNormal = averageNormal;
		this.averageCrit = averageCrit;
		this.percentCrit = percentCrit;
		this.percentEffective = percentEffective;
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

	public int getTotal() {
		return total;
	}

	public int getMax() {
		return max;
	}

	public int getTotalNormal() {
		return totalNormal;
	}

	public int getTotalCrit() {
		return totalCrit;
	}

	public int getTotalEffective() {
		return totalEffective;
	}

	public int getHps() {
		return hps;
	}

	public int getEhps() {
		return ehps;
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

	public double getPercentCrit() {
		return percentCrit;
	}

	public double getPercentEffective() {
		return percentEffective;
	}

	public Long getTimeFrom() {
		return timeFrom;
	}

	public Long getTimeTo() {
		return timeTo;
	}
}
