package com.ixale.starparse.service.dao;

import java.util.List;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Phase;

public interface PhaseDao {

	public void storePhases(final List<Phase> phases, final Phase currentPhase) throws Exception;

	public List<Phase> getCombatPhases(final Combat combat) throws Exception;

	public void reset() throws Exception;
}
