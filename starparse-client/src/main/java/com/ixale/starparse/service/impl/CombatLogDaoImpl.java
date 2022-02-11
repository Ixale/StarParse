package com.ixale.starparse.service.impl;

import com.ixale.starparse.domain.CombatLog;
import com.ixale.starparse.service.dao.CombatLogDao;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository("logDao")
public class CombatLogDaoImpl extends H2Dao implements CombatLogDao {

	private static final String
			SQL_LOG_INSERT = "INSERT INTO logs"
			+ " (file_name, time_from, character_name"
			+ ", log_id)"
			+ " VALUES (?, ?, ?, ?)",

	SQL_LOG_UPDATE = "UPDATE logs SET"
			+ " file_name = ?, time_from = ?, character_name = ?"
			+ " WHERE log_id = ?";

	public void storeCombatLog(final CombatLog log) throws Exception {

		// already exists?
		Integer lastLogId = getJdbcTemplate().query("SELECT log_id FROM logs ORDER BY log_id DESC LIMIT 1", rs -> rs.next() ? rs.getInt("log_id") : null);

		// insert or update (same structure)
		getJdbcTemplate().update(lastLogId != null && lastLogId.equals(log.getLogId()) ? SQL_LOG_UPDATE : SQL_LOG_INSERT, new Object[]{
				log.getFileName(),
				new Timestamp(log.getTimeFrom()),
				log.getCharacterName(),
				log.getLogId()
		});
	}

	@Override
	public CombatLog getCurrentCombatLog() throws Exception {
		List<CombatLog> combatLogs = getCombatLogs();
		return combatLogs != null && combatLogs.size() > 0 ? combatLogs.get(combatLogs.size() - 1) : null;
	}

	@Override
	public List<CombatLog> getCombatLogs() throws Exception {
		return getJdbcTemplate().query(
				"SELECT log_id, file_name, time_from, character_name"
						+ " FROM logs"
						+ " ORDER BY log_id ASC",
				(rs, rowNum) -> {
					CombatLog log = new CombatLog(rs.getInt("log_id"), rs.getString("file_name"), rs.getTimestamp("time_from").getTime());

					if (rs.getString("character_name") != null) {
						log.setCharacterName(rs.getString("character_name"));
					}
					return log;
				});
	}

	@Override
	public void reset() throws Exception {
		getJdbcTemplate().execute("TRUNCATE TABLE logs");
	}
}
