package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;

public class KaraggasPalace extends Raid {

	public KaraggasPalace() {
		super("Karagga's Palace");

		RaidBoss.add(this, RaidBossName.Bonethrasher,
			new long[]{2271801476382720L}, // SM 8m
			new long[]{2624491305828352L}, // SM 16m
			new long[]{2624474125959168L}, // HM 8m
			new long[]{2624508485697536L} // HM 16m
		);

		RaidBoss.add(this, RaidBossName.JargAndSorno,
			new long[]{2739437515571200L, 2739441810538496L},
			new long[]{2760500035190784L, 2760504330158080L},
			new long[]{2760482855321600L, 2760487150288896L},
			new long[]{2760517215059968L, 2760521510027264L});

		RaidBoss.add(this, RaidBossName.ForemanCrusher,
			new long[]{2739875602235392L},
			new long[]{2739888487137280L},
			new long[]{2760637474144256L},
			new long[]{2760693308719104L});

		RaidBoss.add(this, RaidBossName.G4B3HeavyFabricator,
			new long[]{2747344550363136L},
			new long[]{2760371186171904L},
			new long[]{2748401112317952L},
			new long[]{2760375481139200L});

		RaidBoss.add(this, RaidBossName.KaraggaTheUnyielding,
			new long[]{2740043105959936L},
			new long[]{2761200114860032L},
			new long[]{2761191524925440L},
			new long[]{2761208704794624L});
	}
}
