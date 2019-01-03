package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;

public class TrainingDummy extends Raid {

	public TrainingDummy() {
		super(RaidBossName.OperationsTrainingDummy.getFullName());

		bosses.add(new RaidBoss(this, RaidBossName.OperationsTrainingDummy, Mode.HM, Size.Eight, new long[] { 2857785339412480L }));

	}
}
