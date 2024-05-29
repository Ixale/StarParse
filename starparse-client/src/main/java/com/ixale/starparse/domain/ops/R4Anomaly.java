package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.NpcType;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.parser.Helpers;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;

public class R4Anomaly extends Raid {

	private static final long IPCPT_SM_8M = 4467247024177152L,
			IPCPT_HM_8M = 4494653210492928L;

	private static final long WATCHDOG_SM_8M = 4466177577320448L,
			WATCHDOG_HM_8M = 4494700455133184L;

	private static final long LORD_KANOTH_SM_8M = 4466190462222336L,
			LORD_KANOTH_HM_8M = 4494876548792320L;

	private static final long LORD_VALEO_SM_8M = 4466181872287744L,
			LORD_VALEO_HM_8M = 4494872253825024L;

	private static final long LADY_DOMINIQUE_SM_8M = 4466199052156928L,
			LADY_DOMINIQUE_HM_8M = 4608014577303552L;

	public R4Anomaly() {
		super("R-4 Anomaly");

		RaidBoss.add(this, RaidBossName.IPCPT,
				new long[]{IPCPT_SM_8M}, // SM 8m
				null, // SM 16m
				new long[]{IPCPT_HM_8M}, // HM 8m
				null, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.Watchdog,
				new long[]{WATCHDOG_SM_8M}, // SM 8m
				null, // SM 16m
				new long[]{WATCHDOG_HM_8M}, // HM 8m
				null, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.LordValeo,
				new long[]{LORD_VALEO_SM_8M}, // SM 8m
				null, // SM 16m
				new long[]{LORD_VALEO_HM_8M}, // HM 8m
				null, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.LordKanoth,
				new long[]{LORD_KANOTH_SM_8M}, // SM 8m
				null, // SM 16m
				new long[]{LORD_KANOTH_HM_8M}, // HM 8m
				null, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.LadyDominique,
				new long[]{LADY_DOMINIQUE_SM_8M}, // SM 8m
				null, // SM 16m
				new long[]{LADY_DOMINIQUE_HM_8M}, // HM 8m
				null, // HM 16m
				null);

		npcs.put(IPCPT_SM_8M, new Npc(NpcType.boss_raid)); // IP-CPT, deepstation, raidEncounter
		npcs.put(IPCPT_HM_8M, new Npc(NpcType.boss_raid)); // IP-CPT, deepstation, raidEncounter
		npcs.put(4480200645541888L, new Npc(NpcType.boss_4)); // Inferno Enforcer, deepstation, raidEncounter
		npcs.put(4480196350574592L, new Npc(NpcType.boss_4)); // Overload Enforcer, deepstation, raidEncounter
//		npcs.put(4470442479845376L, new Npc(NpcType.boss_1)); // Intrusion ID Unit, deepstation, raidEncounter
//		npcs.put(4470446774812672L, new Npc(NpcType.boss_1)); // Intrusion ID Unit, deepstation, raidEncounter
//		npcs.put(4470451069779968L, new Npc(NpcType.boss_1)); // Intrusion ID Unit, deepstation, raidEncounter
//		npcs.put(4470455364747264L, new Npc(NpcType.boss_1)); // Intrusion ID Unit, deepstation, raidEncounter
//		npcs.put(4471288588402688L, new Npc(NpcType.boss_1)); // Unknown, deepstation, raidEncounter

		npcs.put(WATCHDOG_SM_8M, new Npc(NpcType.boss_raid)); // Watchdog, deepstation, raidEncounter
		npcs.put(WATCHDOG_HM_8M, new Npc(NpcType.boss_raid)); // Watchdog, deepstation, raidEncounter
//		npcs.put(4473865568780288L, new Npc(NpcType.boss_raid)); // Multibeam Array, deepstation, raidEncounter

		npcs.put(LORD_KANOTH_SM_8M, new Npc(NpcType.boss_raid)); // Lord Kanoth, deepstation, raidEncounter
		npcs.put(LORD_KANOTH_HM_8M, new Npc(NpcType.boss_raid)); // Lord Kanoth, deepstation, raidEncounter
//		npcs.put(4483146993106944L, new Npc(NpcType.boss_raid)); // Lord Kanoth, deepstation, raidEncounter
//		npcs.put(4483241482387456L, new Npc(NpcType.boss_raid)); // Lord Kanoth, deepstation, raidEncounter
//		npcs.put(4483245777354752L, new Npc(NpcType.boss_raid)); // Lord Kanoth, deepstation, raidEncounter

		npcs.put(LORD_VALEO_SM_8M, new Npc(NpcType.boss_raid)); // Lord Valeo, deepstation, raidEncounter
		npcs.put(LORD_VALEO_HM_8M, new Npc(NpcType.boss_raid)); // Lord Valeo, deepstation, raidEncounter

		npcs.put(LADY_DOMINIQUE_SM_8M, new Npc(NpcType.boss_raid)); // Lady Dominique, deepstation, raidEncounter
		npcs.put(LADY_DOMINIQUE_HM_8M, new Npc(NpcType.boss_raid)); // Lady Dominique, deepstation, raidEncounter

//		npcs.put(4471292883369984L, new Npc(NpcType.boss_2)); // Unknown, deepstation, raidEncounter
//		npcs.put(4490697545613312L, new Npc(NpcType.boss_1)); // Unknown, deepstation, raidEncounter
//		npcs.put(4490701840580608L, new Npc(NpcType.boss_1)); // Unknown, deepstation, raidEncounter
//		npcs.put(4466203347124224L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4466194757189632L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter

//		npcs.put(4466186167255040L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4477262887911424L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4477275772813312L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4477280067780608L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4477284362747904L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4477288657715200L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4477292952682496L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter

//		npcs.put(4471357307879424L, new Npc(NpcType.boss_4)); // Hulking Monstrosity, deepstation, raidEncounter
//		npcs.put(4471378782715904L, new Npc(NpcType.boss_2)); // Grey Reaper, deepstation, raidEncounter
//		npcs.put(4480935084949504L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4481231437692928L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4481235732660224L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4480277954953216L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter
//		npcs.put(4494915203497984L, new Npc(NpcType.boss_raid)); // Unknown, deepstation, raidEncounter

	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {
		return null;
	}

}
