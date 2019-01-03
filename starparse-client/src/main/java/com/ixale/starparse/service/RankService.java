package com.ixale.starparse.service;

import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RankClass;

public interface RankService {

	public static final String RANK_URL = "stats/pct";

	public enum RankType {
		DPS, DTPS, EHPS
	}

	void initialize(String host);

	RankClass getRank(RaidBoss boss, RankType type, CharacterDiscipline discipline, int tick, int value) throws Exception;
}
