package com.ixale.starparse.service.dao;

import java.util.List;

import com.ixale.starparse.domain.Effect;

public interface EffectDao {

	public void storeEffects(final List<Effect> effects, final List<Effect> currentEffects) throws Exception;

	public void reset() throws Exception;
}
