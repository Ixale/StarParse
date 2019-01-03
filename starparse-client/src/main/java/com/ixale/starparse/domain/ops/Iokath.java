package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;

public class Iokath extends Raid {

	private static final long TYTH_SM_8M = 4078427929837568L,
		TYTH_SM_16M = 4078423634870272L,
		TYTH_HM_8M = 4078419339902976L,
		TYTH_HM_16M = 4078415044935680L;

	private static final long ESNE_SM_8M = 4088538282852352L,
		AIVELA_SM_8M = 4088525397950464L,
		AIVELA_SM_16M = 4114282316824576L,
		AIVELA_HM_8M = 4114286611791872L,
		AIVELA_HM_16M = 4114290906759168L;

	private static final long NAHUT_SM_8M = 4108011664572416L,
		NAHUT_SM_16M = 4122949560827904L,
		NAHUT_HM_8M = 4122953855795200L,
		NAHUT_HM_16M = 4122958150762496L;

	private static final long SCYVA_SM_8M = 4108140513591296L,
		SCYVA_SM_16M = 4158786767945728L, // Iokath Control Center
		SCYVA_HM_8M = 4158791062913024L, // Iokath Control Center
		SCYVA_HM_16M = 4158859782389760L; // Iokath Control Center

	private static final long IZAX_SM_8M = 4108097563918336L,
		IZAX_SM_16M = 4163128979881984L, // Relay tower
		IZAX_HM_8M = 4163133274849280L, // Relay tower
		IZAX_HM_16M = 4163137569816576L; // Relay tower

	public Iokath() {
		super("Iokath");

		RaidBoss.add(this, RaidBossName.Tyth,
			new long[]{TYTH_SM_8M}, // SM 8m
			new long[]{TYTH_SM_16M}, // SM 16m
			new long[]{TYTH_HM_8M}, // HM 8m
			new long[]{TYTH_HM_16M}, // HM 16m
			null);

		RaidBoss.add(this, RaidBossName.AivelaAndEsne,
			new long[]{AIVELA_SM_8M, ESNE_SM_8M, 4109905745149952L}, // SM 8m
			new long[]{AIVELA_SM_16M}, // SM 16m
			new long[]{AIVELA_HM_8M}, // HM 8m
			new long[]{AIVELA_HM_16M}, // HM 16m
			null);

		RaidBoss.add(this, RaidBossName.Nahut,
			new long[]{NAHUT_SM_8M}, // SM 8m
			new long[]{NAHUT_SM_16M}, // SM 16m
			new long[]{NAHUT_HM_8M}, // HM 8m
			new long[]{NAHUT_HM_16M}, // HM 16m
			null);

		RaidBoss.add(this, RaidBossName.Scyva,
			new long[]{SCYVA_SM_8M}, // SM 8m
			new long[]{SCYVA_SM_16M}, // SM 16m
			new long[]{SCYVA_HM_8M}, // HM 8m
			new long[]{SCYVA_HM_16M}, // HM 16m
			null);

		RaidBoss.add(this, RaidBossName.Izax,
			new long[]{IZAX_SM_8M}, // SM 8m
			new long[]{IZAX_SM_16M}, // SM 16m
			new long[]{IZAX_HM_8M}, // HM 8m
			new long[]{IZAX_HM_16M}, // HM 16m
			null);
	}

}
