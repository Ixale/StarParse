package com.ixale.starparse.domain.stats;

public class HealingTakenStats {

	private final String source, name;
	private final long guid;

	private final int
		ticks, ticksNormal, ticksCrit, 
		total, totalNormal, totalCrit, totalEffective,
		aps, absorbed,
		htps, ehtps;

	private final double percentTotal,
		averageNormal, averageCrit,
		percentCrit, percentEffective;

	private final Long timeFrom, timeTo;

	public HealingTakenStats(String source, String name, long guid, int ticks,
			int ticksNormal, int ticksCrit, int total,
			int totalNormal, int totalCrit, int totalEffective, int htps, int ehtps,
			double percentTotal, double averageNormal, double averageCrit,
			double percentCrit, double percentEffective,
			int aps, int absorbed,
			Long timeFrom, Long timeTo) {
		super();
		this.source = source;
		this.name = name;
		this.guid = guid;
		this.ticks = ticks;
		this.ticksNormal = ticksNormal;
		this.ticksCrit = ticksCrit;
		this.total = total;
		this.totalNormal = totalNormal;
		this.totalCrit = totalCrit;
		this.totalEffective = totalEffective;
		this.htps = htps;
		this.ehtps = ehtps;
		this.percentTotal = percentTotal;
		this.averageNormal = averageNormal;
		this.averageCrit = averageCrit;
		this.percentCrit = percentCrit;
		this.percentEffective = percentEffective;

		this.aps = aps;
		this.absorbed = absorbed;

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

	public int getTicksNormal() {
		return ticksNormal;
	}

	public int getTicksCrit() {
		return ticksCrit;
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

	public int getTotalEffective() {
		return totalEffective;
	}

	public int getHtps() {
		return htps;
	}

	public int getEhtps() {
		return ehtps;
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

	public int getAps() {
		return aps;
	}

	public int getAbsorbed() {
		return absorbed;
	}

	public Long getTimeFrom() {
		return timeFrom;
	}

	public Long getTimeTo() {
		return timeTo;
	}
}
