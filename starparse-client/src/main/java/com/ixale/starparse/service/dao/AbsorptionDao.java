package com.ixale.starparse.service.dao;

import java.util.List;

import com.ixale.starparse.domain.Absorption;

public interface AbsorptionDao {

	public void storeAbsorptions(final List<Absorption> absorptions) throws Exception;

	public void reset() throws Exception;
}
