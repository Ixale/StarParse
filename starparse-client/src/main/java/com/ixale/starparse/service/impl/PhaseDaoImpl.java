package com.ixale.starparse.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Phase;
import com.ixale.starparse.service.dao.PhaseDao;

@Repository("phaseDao")
public class PhaseDaoImpl extends H2Dao implements PhaseDao {

	private static final String 
  		SQL_INSERT = "INSERT INTO phases"
				+ " (name, type, combat_id, event_id_from, event_id_to, tick_from, tick_to, phase_id)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",

		SQL_UPDATE = "UPDATE phases SET"
				+ " name = ?, type = ?, combat_id = ?, event_id_from = ?, event_id_to = ?"
				+ ", tick_from = ?, tick_to = ?"
				+ " WHERE phase_id = ?";

	private int highestStoredId = 0;

	@Override
	public void storePhases(List<Phase> phases, final Phase currentPhase) throws Exception {

		// process all (create new, update everything else)
		int highestId = 0;
		for (Phase p: phases) {
			storePhase(p, p.getPhaseId() > highestStoredId);

			highestId = Math.max(highestId, p.getPhaseId());
		}
		if (currentPhase != null) {
			storePhase(currentPhase, currentPhase.getPhaseId() > highestStoredId);

			highestId = Math.max(highestId, currentPhase.getPhaseId());
		}
		highestStoredId = highestId;
	}

	private void storePhase(final Phase p, boolean isNew) throws Exception {
		// insert or update (same structure)
		getJdbcTemplate().update(isNew ? SQL_INSERT : SQL_UPDATE, new Object[] {
				p.getName(),
				p.getType().toString(),
				p.getCombatId(),
				p.getEventIdFrom(),
				p.getEventIdTo(),
				p.getTickFrom(),
				p.getTickTo(),
				p.getPhaseId()
				});
	}

	@Override
	public List<Phase> getCombatPhases(final Combat combat) throws Exception {
		return getJdbcTemplate().query(
				"SELECT phase_id, name, type, combat_id, event_id_from, event_id_to, tick_from, tick_to"
				+ " FROM phases"
				+ " WHERE combat_id = ?"
				+ " ORDER BY event_id_from ASC",
				new Object[] {combat.getCombatId()},
				new RowMapper<Phase>() {
					@Override
					public Phase mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						final Phase p = new Phase(
								rs.getInt("phase_id"),
								rs.getString("name"),
								Phase.Type.valueOf(rs.getString("type")),
								rs.getInt("combat_id"),
								rs.getInt("event_id_from"),
								rs.getLong("tick_from"));

						p.setEventIdTo(getValueOrNull(rs, rs.getInt("event_id_to")));
						p.setTickTo(getValueOrNull(rs, rs.getLong("tick_to")));

						return p;
					}
				});
	}

	@Override
	public void reset() throws Exception {
		highestStoredId = 0;
		getJdbcTemplate().execute("TRUNCATE TABLE phases");
	}
}
