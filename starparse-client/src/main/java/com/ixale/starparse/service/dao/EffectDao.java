package com.ixale.starparse.service.dao;

import com.ixale.starparse.domain.Effect;

import java.util.List;

public interface EffectDao {

	void storeEffects(final List<Effect> effects, final List<Effect> currentEffects) throws Exception;

	void reset() throws Exception;
}
