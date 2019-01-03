package com.ixale.starparse.domain.stats;

import java.io.Serializable;

import com.ixale.starparse.domain.EntityGuid;

public class AbsorptionStats implements Serializable {

	private static final long serialVersionUID = 1L;

	String source;
	EntityGuid ability;

	int total;

	AbsorptionStats() {

	}

	public AbsorptionStats(String source, EntityGuid ability, int total) {
		super();
		this.source = source;
		this.ability = ability;
		this.total = total;
	}

	public String getSource() {
		return source;
	}

	public EntityGuid getAbility() {
		return ability;
	}

	public int getTotal() {
		return total;
	}

	public String toString() {
		return source + " (" + ability + "): " + total;
	}
}
