package com.ixale.starparse.service.impl;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.service.dao.EffectDao;

@Repository("effectDao")
public class EffectDaoImpl extends H2Dao implements EffectDao {

	private static final String 
  		SQL_INSERT = "INSERT INTO effects"
				+ " (event_id_from, event_id_to, time_from, time_to"
				+ ", is_activated, is_absorption"
				+ ", effect_id)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?)",

  		SQL_UPDATE = "UPDATE effects SET"
				+ " event_id_from = ?, event_id_to = ?, time_from = ?, time_to = ?"
				+ ", is_activated = ?, is_absorption = ?"
				+ " WHERE effect_id = ?";

	private int highestStoredId = 0;

	@Override
	public void storeEffects(final List<Effect> effects, final List<Effect> currentEffects) throws Exception {

		// process all (create new, update everything else)
		int highestId = 0;
		for (Effect e: effects) {
			storeEffect(e, e.getEffectId() > highestStoredId);

			highestId = Math.max(highestId, e.getEffectId());
		}
		for (Effect e: currentEffects) {
			storeEffect(e, e.getEffectId() > highestStoredId);

			highestId = Math.max(highestId, e.getEffectId());
		}
		highestStoredId = highestId;
	}

	private void storeEffect(final Effect e, boolean isNew) throws Exception {
		// insert or update (same structure)
		getJdbcTemplate().update(isNew ? SQL_INSERT : SQL_UPDATE, new Object[] {
				e.getEventIdFrom(),
				e.getEventIdTo(),
				new Timestamp(e.getTimeFrom()),
				e.getTimeTo() == null ? null : new Timestamp(e.getTimeTo()),
				e.isActivated(),
				e.isAbsorption(),
				e.getEffectId()
				});
	}

	@Override
	public void reset() throws Exception {
		highestStoredId = 0;
		getJdbcTemplate().execute("TRUNCATE TABLE effects");
	}
}
