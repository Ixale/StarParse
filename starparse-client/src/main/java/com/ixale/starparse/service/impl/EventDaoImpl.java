package com.ixale.starparse.service.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import com.ixale.starparse.domain.Event;
import com.ixale.starparse.service.dao.EventDao;

@Repository("eventDao")
public class EventDaoImpl extends H2Dao implements EventDao {

	private static final int VARCHAR_LIMIT = 50;

	private static final String 
		SQL_EVENT_INSERT = "INSERT INTO events"
			+ " (event_id, log_id, timestamp"
			+ ", source_type, source_name, source_guid, source_instance"
			+ ", target_type, target_name, target_guid, target_instance"
			+ ", ability_name, ability_guid"
			+ ", action_name, action_guid, effect_name, effect_guid"
			+ ", value, is_crit"
			+ ", damage_name, damage_guid"
			+ ", reflect_name, reflect_guid, mitigation_name, mitigation_guid, absorption_name, absorption_guid, absorbed"
			+ ", threat"
			+ ", guard_state, effective_heal, effective_threat)"
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	@Override
	public void storeEvents(final List<Event> events) throws Exception
	{
		final int batchSize = 5000;

		// events
		for (int j = 0; j < events.size(); j += batchSize) {

			final List<Event> batchList = events.subList(j, j + batchSize > events.size() ? events.size() : j + batchSize);

			getJdbcTemplate().batchUpdate(SQL_EVENT_INSERT,
				new BatchPreparedStatementSetter() {
					Event e;
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {

						// setup basic

						e = batchList.get(i);

						ps.clearParameters();
						ps.setInt(1, e.getEventId());
						ps.setInt(2, e.getLogId());
						ps.setTimestamp(3, new Timestamp(e.getTimestamp()));

						// source and target

						if (e.getSource() != null) {
							ps.setInt(4, e.getSource().getType().getId());
							ps.setString(5, truncate(e.getSource().getName(), VARCHAR_LIMIT));
							if (e.getSource().getGuid() != null) {
								ps.setLong(6, e.getSource().getGuid());
							}
							if (e.getSource().getInstanceId() != null) {
								ps.setLong(7, e.getSource().getInstanceId());
							}
						}

						if (e.getTarget() != null) {
							ps.setInt(8, e.getTarget().getType().getId());
							ps.setString(9, truncate(e.getTarget().getName(), VARCHAR_LIMIT));
							if (e.getTarget().getGuid() != null) {
								ps.setLong(10, e.getTarget().getGuid());
							}
							if (e.getTarget().getInstanceId() != null) {
								ps.setLong(11, e.getTarget().getInstanceId());
							}
						}

						// ability, action and resulting effect

						if (e.getAbility() != null) {
							ps.setString(12, truncate(e.getAbility().getName(), VARCHAR_LIMIT));
							if (e.getAbility().getGuid() != null) {
								ps.setLong(13, e.getAbility().getGuid());
							}
						}
						if (e.getAction() != null) {
							ps.setString(14, truncate(e.getAction().getName(), VARCHAR_LIMIT));
							if (e.getAction().getGuid() != null) {
								ps.setLong(15, e.getAction().getGuid());
							}
						}
						if (e.getEffect() != null) {
							ps.setString(16, truncate(e.getEffect().getName(), VARCHAR_LIMIT));
							if (e.getEffect().getGuid() != null) {
								ps.setLong(17, e.getEffect().getGuid());
							}
						}

						if (e.getValue() != null) {
							ps.setInt(18, e.getValue());
							ps.setBoolean(19, e.isCrit());
						}

						// damage and mitigation

						if (e.getDamage() != null) {
							ps.setString(20, truncate(e.getDamage().getName(), VARCHAR_LIMIT));
							if (e.getDamage().getGuid() != null) {
								ps.setLong(21, e.getDamage().getGuid());
							}
						}
						if (e.getReflect() != null) {
							ps.setString(22, truncate(e.getReflect().getName(), VARCHAR_LIMIT));
							if (e.getReflect().getGuid() != null) {
								ps.setLong(23, e.getReflect().getGuid());
							}
						}
						if (e.getMitigation() != null) {
							ps.setString(24, truncate(e.getMitigation().getName(), VARCHAR_LIMIT));
							if (e.getMitigation().getGuid() != null) {
								ps.setLong(25, e.getMitigation().getGuid());
							}
						}
						if (e.getAbsorbtion() != null) {
							ps.setString(26, truncate(e.getAbsorbtion().getName(), VARCHAR_LIMIT));
							if (e.getAbsorbtion().getGuid() != null) {
								ps.setLong(27, e.getAbsorbtion().getGuid());
							}
							if (e.getAbsorbed() != null) {
								ps.setInt(28, e.getAbsorbed());
							}
						}

						// contextual (computed by parser)

						if (e.getThreat() != null) {
							ps.setLong(29, e.getThreat());
						}
						if (e.getGuardState() != null) {
							ps.setInt(30, e.getGuardState());
						}
						if (e.getEffectiveHeal() != null) {
							ps.setInt(31, e.getEffectiveHeal());
						}
						if (e.getEffectiveThreat() != null) {
							ps.setLong(32, e.getEffectiveThreat());
						}
					}

					@Override
					public int getBatchSize() {
						return batchList.size();
					}
				});
		}
	}

	@Override
	public void reset() throws Exception {
		getJdbcTemplate().execute("TRUNCATE TABLE events");
	}
}
