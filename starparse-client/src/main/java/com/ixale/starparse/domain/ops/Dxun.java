package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;

public class Dxun extends Raid {

	private static final long RED_SM_8M = 4246176467517440L,
			RED_SM_16M = -100L,
			RED_HM_8M = 4330233272467456L,
			RED_HM_16M = -200L;

	private static final long BREACH_SM_8M = 4333218274738176L,
			BREACH_SM_16M = -1123L,
			BREACH_HM_8M = -1223L,
			BREACH_HM_16M = -1323L;

	private static final long TRANDOSHAN_SQUAD_SM_8M = 4245970309087232L,
			TRANDOSHAN_SQUAD_SM_16M = -1423L,
			TRANDOSHAN_SQUAD_HM_8M = -123,
			TRANDOSHAN_SQUAD_HM_16M = -1623L;

	private static final long HUNTMASTER_SM_8M = 4265104388390912L,
			HUNTMASTER_SM_16M = -1523L,
			HUNTMASTER_HM_8M = 4330237567434752L,
			HUNTMASTER_HM_16M = -1723L;

	private static final long APEX_VG_SM_8M = 4282872668094464L, // 4285612857229312
			APEX_VG_SM_16M = -1233L,
			APEX_VG_HM_8M = 4350020186800128L,
			APEX_VG_HM_16M = -1263L;

	public Dxun() {
		super("The Nature of Progress");

		RaidBoss.add(this, RaidBossName.Red,
				new long[]{RED_SM_8M}, // SM 8m
				new long[]{RED_SM_16M}, // SM 16m
				new long[]{RED_HM_8M}, // HM 8m
				new long[]{RED_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.BreachCI004,
				new long[]{BREACH_SM_8M}, // SM 8m
				new long[]{BREACH_SM_16M}, // SM 16m
				new long[]{BREACH_HM_8M}, // HM 8m
				new long[]{BREACH_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.TrandoshanSquad,
				new long[]{TRANDOSHAN_SQUAD_SM_8M, 4245978899021824L, 4245983193989120L, 4245987488956416L}, // SM 8m
				new long[]{TRANDOSHAN_SQUAD_SM_16M}, // SM 16m
				new long[]{TRANDOSHAN_SQUAD_HM_8M}, // HM 8m
				new long[]{TRANDOSHAN_SQUAD_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.TheHuntmaster,
				new long[]{HUNTMASTER_SM_8M, 4281661487316992L}, // SM 8m
				new long[]{HUNTMASTER_SM_16M}, // SM 16m
				new long[]{HUNTMASTER_HM_8M}, // HM 8m
				new long[]{HUNTMASTER_HM_16M}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.ApexVanguard,
				new long[]{APEX_VG_SM_8M}, // SM 8m
				new long[]{APEX_VG_SM_16M}, // SM 16m
				new long[]{APEX_VG_HM_8M}, // HM 8m
				new long[]{APEX_VG_HM_16M}, // HM 16m
				null);
	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {

		if (c.getBoss() == null) {
			return null;
		}

		return null;
	}

	private String getNewPhaseNameForTyth(final Event e, final Combat c, final String currentPhaseName) {

		return null;
	}

}
