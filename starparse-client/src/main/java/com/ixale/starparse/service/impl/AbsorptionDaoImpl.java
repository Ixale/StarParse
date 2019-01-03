package com.ixale.starparse.service.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ixale.starparse.domain.Absorption;
import com.ixale.starparse.service.dao.AbsorptionDao;

@Repository("absorptionDao")
public class AbsorptionDaoImpl extends H2Dao implements AbsorptionDao {

	private static final String 
  		SQL_INSERT = "INSERT INTO absorptions"
				+ " (event_id, effect_id)"
				+ " VALUES (?, ?)";


	@Override
	public void storeAbsorptions(List<Absorption> absorptions) throws Exception {
		for (Absorption a: absorptions) {
			getJdbcTemplate().update(SQL_INSERT, new Object[] {
				a.getEventId(),
				a.getEffectId()
				});
		}
	}

	@Override
	public void reset() throws Exception {
		getJdbcTemplate().execute("TRUNCATE TABLE absorptions");
	}
}
