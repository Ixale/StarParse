package com.ixale.starparse.service.dao;

import java.util.List;

import com.ixale.starparse.domain.CombatLog;

public interface CombatLogDao {

	public void storeCombatLog(final CombatLog log) throws Exception;

	public CombatLog getCurrentCombatLog() throws Exception;

	public List<CombatLog> getCombatLogs() throws Exception;

	public void reset() throws Exception;
}
