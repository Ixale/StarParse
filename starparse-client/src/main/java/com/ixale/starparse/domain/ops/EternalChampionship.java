package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;

public class EternalChampionship extends Raid {

	private static final long R1_ARLAIA_A = 3636952536449024L,
		R1_ARLAIA_B = 3635105700511744L,
		R1_ARLAIA_C = 3635109995479040L,

	R2_DARUULA_A = 3636969716318208L,
		R2_DARUULA_B = 3827460105830400L,
		R2_DARUULA_C = 3636956831416320L,
		R2_DARUULA_D = 3636961126383616L,

	R3_GUNGUS_A = 3636986896187392L,
		R3_GUNGUS_B = 3827511645437952L,

	R4_CONRAAD_A = 3637016960958464L,
		R4_CONRAAD_B = 3637021255925760L,

	R5_LANOS = 3635170125021184L,

	R6_BREAKTOWN_A = 3635183009923072L,
		R6_BREAKTOWN_B = 3635187304890368L,
		R6_BREAKTOWN_C = 3637042730762240L,

	R7_NOCTURNO_A = 3635213074694144L,
		R7_NOCTURNO_B = 3635217369661440L,

	R8_LITTLE_A = 3860643023159296L,
		R8_LITTLE_B = 3635200189792256L,

	R9_DOOM = 3635230254563328L,

	R10_ZOTAR_A = 3637068500566016L,
		R10_ZOTAR_B = 3635243139465216L;

	public EternalChampionship() {
		super("Eternal Championship");

		bosses.add(new RaidBoss(this, RaidBossName.ArlaiaZayzen, Mode.SM, Size.Eight,
			new long[]{R1_ARLAIA_A, R1_ARLAIA_B, R1_ARLAIA_C}));

		bosses.add(new RaidBoss(this, RaidBossName.DaruulaGrah, Mode.SM, Size.Eight,
			new long[]{R2_DARUULA_A, R2_DARUULA_B, R2_DARUULA_C, R2_DARUULA_D}));

		bosses.add(new RaidBoss(this, RaidBossName.GungusBoga, Mode.SM, Size.Eight,
			new long[]{R3_GUNGUS_A, R3_GUNGUS_B}));

		bosses.add(new RaidBoss(this, RaidBossName.ConraadAndChompers, Mode.SM, Size.Eight,
			new long[]{R4_CONRAAD_A, R4_CONRAAD_B}));

		bosses.add(new RaidBoss(this, RaidBossName.Lanos, Mode.SM, Size.Eight,
			new long[]{R5_LANOS}));

		bosses.add(new RaidBoss(this, RaidBossName.BreaktownBrawler, Mode.SM, Size.Eight,
			new long[]{R6_BREAKTOWN_A, R6_BREAKTOWN_B, R6_BREAKTOWN_C}));

		bosses.add(new RaidBoss(this, RaidBossName.NocturnoAndDrakeRaven, Mode.SM, Size.Eight,
			new long[]{R7_NOCTURNO_A, R7_NOCTURNO_B}));

		bosses.add(new RaidBoss(this, RaidBossName.LittleGut, Mode.SM, Size.Eight,
			new long[]{R8_LITTLE_A, R8_LITTLE_B}));

		bosses.add(new RaidBoss(this, RaidBossName.DoomDroid, Mode.SM, Size.Eight,
			new long[]{R9_DOOM}));

		bosses.add(new RaidBoss(this, RaidBossName.EternalChampionZotar, Mode.SM, Size.Eight,
			new long[]{R10_ZOTAR_A, R10_ZOTAR_B}));
	}

}
