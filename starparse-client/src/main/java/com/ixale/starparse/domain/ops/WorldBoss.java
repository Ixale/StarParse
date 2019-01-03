package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;

public class WorldBoss extends Raid {

	private static final long ANCIENT_THREAT_BOSS = 3511870203887616L,
		COLOSSAL_MONOLITH_SM_8M = 3541140406009856L,
		COLOSSAL_MONOLITH_HM_8M = 3570947479044096L,
		COLOSSAL_MONOLITH_SM_16M = 3570951774011392L,
		COLOSSAL_MONOLITH_HM_16M = 3570956068978688L,
		WORLDBREAKER_MONOLITH_BOSS = 3547338043817984L;

	public WorldBoss() {
		super("World Boss");

		bosses.add(new RaidBoss(this, RaidBossName.AncientThreat, Mode.HM, Size.Sixteen,
			new long[]{ANCIENT_THREAT_BOSS}));

		RaidBoss.add(this, RaidBossName.ColossalMonolith,
			new long[]{COLOSSAL_MONOLITH_SM_8M}, // SM 8m
			new long[]{COLOSSAL_MONOLITH_SM_16M}, // SM 16m
			new long[]{COLOSSAL_MONOLITH_HM_8M}, // HM 8m
			new long[]{COLOSSAL_MONOLITH_HM_16M}); // HM 16m

		bosses.add(new RaidBoss(this, RaidBossName.WorldbreakerMonolith, Mode.SM, Size.Sixteen,
			new long[]{WORLDBREAKER_MONOLITH_BOSS}));

		RaidBoss.add(this, RaidBossName.GoldenFury,
			new long[]{3210174521147392L}, // SM 8m
			new long[]{3232800408862720L}, // SM 16m
			new long[]{3232735984353280L}, // HM 8m
			new long[]{3232817588731904L}); // HM 16m

		RaidBoss.add(this, RaidBossName.Eyeless,
			new long[]{3319090596806656L}, // SM 8m
			new long[]{3328376316100608L}, // SM 16m
			new long[]{3328372021133312L}, // HM 8m
			new long[]{3328380611067904L}); // HM 16m

		RaidBoss.add(this, RaidBossName.XenoanalystII,
			new long[]{3153545377349632L}, // SM 8m
			new long[]{3213924027596800L}, // SM 16m
			new long[]{3213919732629504L}, // HM 8m
			new long[]{3213928322564096L}); // HM 16m
	}

}
