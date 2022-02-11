package com.ixale.starparse.service;

import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RankClass;

import java.util.function.Consumer;

public interface RankService {

	String RANK_URL = "stats/pct";

	enum RankType {
		DPS, DTPS, EHPS
	}

	void initialize(String host);

	void getRank(RaidBoss boss, RankType type, CharacterDiscipline discipline, int tick, int value, Consumer<RankClass> callback);
}
