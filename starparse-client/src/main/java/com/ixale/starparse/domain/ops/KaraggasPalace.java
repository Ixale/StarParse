package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.NpcType;
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
				new long[]{2624508485697536L}, // HM 16m
				null);

		RaidBoss.add(this, RaidBossName.JargAndSorno,
				new long[]{2739437515571200L, 2739441810538496L},
				new long[]{2760500035190784L, 2760504330158080L},
				new long[]{2760482855321600L, 2760487150288896L},
				new long[]{2760517215059968L, 2760521510027264L},
				null);

		RaidBoss.add(this, RaidBossName.ForemanCrusher,
				new long[]{2739875602235392L},
				new long[]{2739888487137280L},
				new long[]{2760637474144256L},
				new long[]{2760693308719104L},
				null);

		RaidBoss.add(this, RaidBossName.G4B3HeavyFabricator,
				new long[]{2747344550363136L},
				new long[]{2760371186171904L},
				new long[]{2748401112317952L},
				new long[]{2760375481139200L},
				null);

		RaidBoss.add(this, RaidBossName.KaraggaTheUnyielding,
				new long[]{2740043105959936L},
				new long[]{2761200114860032L},
				new long[]{2761191524925440L},
				new long[]{2761208704794624L},
				null);

		npcs.put(2271801476382720L, new Npc(NpcType.boss_raid));
		npcs.put(2624491305828352L, new Npc(NpcType.boss_raid));
		npcs.put(2624474125959168L, new Npc(NpcType.boss_raid));
		npcs.put(2624508485697536L, new Npc(NpcType.boss_raid));

//		npcs.put(2740206314717184L, new Npc(NpcType.boss_1)); // Carbonizer Probe, boss_2_bh.carbonite_block, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_2_bh.carbonite_block
//		npcs.put(2760491445256192L, new Npc(NpcType.boss_1)); // Carbonizer Probe, boss_2_bh.carbonite_block, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_2.boss.boss_2_bh.carbonite_block
//		npcs.put(2760495740223488L, new Npc(NpcType.boss_1)); // Carbonizer Probe, boss_2_bh.carbonite_block, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_2_bh.carbonite_block
//		npcs.put(2760508625125376L, new Npc(NpcType.boss_1)); // Carbonizer Probe, boss_2_bh.carbonite_block, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_4.boss.boss_2_bh.carbonite_block
		npcs.put(2739437515571200L, new Npc(NpcType.boss_raid)); // Jarg, boss_2_bh.jarg, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_2_bh.jarg
		npcs.put(2760482855321600L, new Npc(NpcType.boss_raid)); // Jarg, boss_2_bh.jarg, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_2.boss.boss_2_bh.jarg
		npcs.put(2760500035190784L, new Npc(NpcType.boss_raid)); // Jarg, boss_2_bh.jarg, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_2_bh.jarg
		npcs.put(2760517215059968L, new Npc(NpcType.boss_raid)); // Jarg, boss_2_bh.jarg, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_4.boss.boss_2_bh.jarg
		npcs.put(2739441810538496L, new Npc(NpcType.boss_raid)); // Sorno, boss_2_bh.sorno, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_2_bh.sorno
		npcs.put(2760487150288896L, new Npc(NpcType.boss_raid)); // Sorno, boss_2_bh.sorno, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_2.boss.boss_2_bh.sorno
		npcs.put(2760504330158080L, new Npc(NpcType.boss_raid)); // Sorno, boss_2_bh.sorno, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_2_bh.sorno
		npcs.put(2760521510027264L, new Npc(NpcType.boss_raid)); // Sorno, boss_2_bh.sorno, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_4.boss.boss_2_bh.sorno

//		npcs.put(2748216428724224L, new Npc(NpcType.boss_2)); // Hutt Cartel Warmonger, boss_3_kintan.wave_beast_master, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_beast_master
//		npcs.put(2760658948980736L, new Npc(NpcType.boss_2)); // Hutt Cartel Warmonger, boss_3_kintan.wave_beast_master, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_beast_master
//		npcs.put(2763377663279104L, new Npc(NpcType.boss_4)); // Hutt Cartel Infiltrator, boss_3_kintan.wave_covert_assassin, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_covert_assassin
//		npcs.put(2766736327704576L, new Npc(NpcType.boss_4)); // Hutt Cartel Infiltrator, boss_3_kintan.wave_covert_assassin, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_covert_assassin
//		npcs.put(2763115670274048L, new Npc(NpcType.boss_2)); // Gamorrean Machinesmith, boss_3_kintan.wave_gamorrean_mechanic, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_gamorrean_mechanic
//		npcs.put(2766749212606464L, new Npc(NpcType.boss_2)); // Gamorrean Machinesmith, boss_3_kintan.wave_gamorrean_mechanic, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_gamorrean_mechanic
//		npcs.put(2748237903560704L, new Npc(NpcType.boss_2)); // Hutt Cartel Assassin, boss_3_kintan.wave_hired_elite_agent, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_hired_elite_agent
//		npcs.put(2760663243948032L, new Npc(NpcType.boss_2)); // Hutt Cartel Assassin, boss_3_kintan.wave_hired_elite_agent, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_hired_elite_agent
//		npcs.put(2763111375306752L, new Npc(NpcType.boss_2)); // "Monster Legion" Commander, boss_3_kintan.wave_hutt_cartel_wrangler, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_hutt_cartel_wrangler
//		npcs.put(2766766392475648L, new Npc(NpcType.boss_2)); // "Monster Legion" Commander, boss_3_kintan.wave_hutt_cartel_wrangler, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_hutt_cartel_wrangler
//		npcs.put(2748242198528000L, new Npc(NpcType.boss_2)); // Hutt Cartel Poisoner, boss_3_kintan.wave_hutt_operative, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_hutt_operative
//		npcs.put(2760667538915328L, new Npc(NpcType.boss_2)); // Hutt Cartel Poisoner, boss_3_kintan.wave_hutt_operative, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_hutt_operative
//		npcs.put(2759477832974336L, new Npc(NpcType.boss_2)); // Kintan Warhound, boss_3_kintan.wave_kintan_beast_slave, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_kintan_beast_slave
//		npcs.put(2760676128849920L, new Npc(NpcType.boss_2)); // Kintan Warhound, boss_3_kintan.wave_kintan_beast_slave, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_kintan_beast_slave
//		npcs.put(2762965346418688L, new Npc(NpcType.boss_3)); // Kintan Behemoth, boss_3_kintan.wave_kintan_behemoth, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_kintan_behemoth
//		npcs.put(2766783572344832L, new Npc(NpcType.boss_3)); // Kintan Behemoth, boss_3_kintan.wave_kintan_behemoth, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_kintan_behemoth
//		npcs.put(2763102785372160L, new Npc(NpcType.boss_2)); // Kintan Rock Thrower, boss_3_kintan.wave_kintan_rockthrower, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_kintan_rockthrower
//		npcs.put(2766796457246720L, new Npc(NpcType.boss_2)); // Kintan Rock Thrower, boss_3_kintan.wave_kintan_rockthrower, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_kintan_rockthrower
//		npcs.put(2763132850143232L, new Npc(NpcType.boss_2)); // Veteran Destroyer Droid, boss_3_kintan.wave_rusty_assassin_droid, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.wave_rusty_assassin_droid
//		npcs.put(2776464428630016L, new Npc(NpcType.boss_2)); // Veteran Destroyer Droid, boss_3_kintan.wave_rusty_assassin_droid, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.wave_rusty_assassin_droid
//
		npcs.put(2747344550363136L, new Npc(NpcType.boss_raid, "G4-B3")); // G4-B3 Heavy Fabricator, boss_factory_droid, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_4_hanoi.boss_factory_droid
		npcs.put(2748401112317952L, new Npc(NpcType.boss_raid, "G4-B3")); // G4-B3 Heavy Fabricator, boss_factory_droid, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_2.boss.boss_4_hanoi.boss_factory_droid
		npcs.put(2760371186171904L, new Npc(NpcType.boss_raid, "G4-B3")); // G4-B3 Heavy Fabricator, boss_factory_droid, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_4_hanoi.boss_factory_droid
		npcs.put(2760375481139200L, new Npc(NpcType.boss_raid, "G4-B3")); // G4-B3 Heavy Fabricator, boss_factory_droid, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_4.boss.boss_4_hanoi.boss_factory_droid

		npcs.put(2739875602235392L, new Npc(NpcType.boss_raid)); // Foreman Crusher, boss_kintan, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_3_kintan.boss_kintan
		npcs.put(2760637474144256L, new Npc(NpcType.boss_raid)); // Foreman Crusher, boss_kintan, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_2.boss.boss_3_kintan.boss_kintan
		npcs.put(2739888487137280L, new Npc(NpcType.boss_raid)); // Foreman Crusher, boss_kintan, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_3_kintan.boss_kintan
		npcs.put(2760693308719104L, new Npc(NpcType.boss_raid)); // Foreman Crusher, boss_kintan, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_4.boss.boss_3_kintan.boss_kintan

		npcs.put(2740043105959936L, new Npc(NpcType.boss_raid, "Karagga")); // Karagga the Unyielding, boss_karagga, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_1.boss.boss_5_karagga.boss_karagga
		npcs.put(2761191524925440L, new Npc(NpcType.boss_raid, "Karagga")); // Karagga the Unyielding, boss_karagga, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_2.boss.boss_5_karagga.boss_karagga
		npcs.put(2761200114860032L, new Npc(NpcType.boss_raid, "Karagga")); // Karagga the Unyielding, boss_karagga, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_3.boss.boss_5_karagga.boss_karagga
		npcs.put(2761208704794624L, new Npc(NpcType.boss_raid, "Karagga")); // Karagga the Unyielding, boss_karagga, npc.qtr.1x1.raid.karaggas_palace.enemy.difficulty_4.boss.boss_5_karagga.boss_karagga
	}
}
