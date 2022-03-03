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

public class Iokath extends Raid {

	private static final long TYTH_SM_8M = 4078427929837568L,
			TYTH_SM_16M = 4078419339902976L,
			TYTH_HM_8M = 4078423634870272L,
			TYTH_HM_16M = 4078415044935680L;

	private static final String TYTH_PHASE_OPENING = "Opening",
			TYTH_PHASE_INVERSION = "Inversion";

	private static final long ESNE_SM_8M = 4088538282852352L,
			AIVELA_SM_8M = 4088525397950464L,
			AIVELA_SM_16M = 4114282316824576L,
			AIVELA_HM_8M = 4114286611791872L,
			AIVELA_HM_16M = 4114290906759168L;

	private static final long NAHUT_SM_8M = 4108011664572416L,
			NAHUT_SM_16M = 4122953855795200L,
			NAHUT_HM_8M = 4122949560827904L,
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
		super("Gods of the Machine");

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

		npcs.put(4078427929837568L, new Npc(NpcType.boss_raid)); // TYTH, boss.tyth.tyth, npc.operation.iokath.enemy.difficulty_1.boss.tyth.tyth
		npcs.put(4078423634870272L, new Npc(NpcType.boss_raid)); // TYTH, boss.tyth.tyth, npc.operation.iokath.enemy.difficulty_2.boss.tyth.tyth
		npcs.put(4078419339902976L, new Npc(NpcType.boss_raid)); // TYTH, boss.tyth.tyth, npc.operation.iokath.enemy.difficulty_3.boss.tyth.tyth
		npcs.put(4078415044935680L, new Npc(NpcType.boss_raid)); // TYTH, boss.tyth.tyth, npc.operation.iokath.enemy.difficulty_4.boss.tyth.tyth
//		npcs.put(4078449404674048L, new Npc(NpcToughness.boss_1)); // Lance, boss.tyth.add_beam, npc.operation.iokath.enemy.difficulty_1.boss.tyth.add_beam
//		npcs.put(4078445109706752L, new Npc(NpcToughness.boss_1)); // Guardian, boss.tyth.add_direct_damage, npc.operation.iokath.enemy.difficulty_1.boss.tyth.add_direct_damage
//		npcs.put(4078440814739456L, new Npc(NpcToughness.boss_1)); // Grace, boss.tyth.add_healing, npc.operation.iokath.enemy.difficulty_1.boss.tyth.add_healing
//		npcs.put(4179016063909888L, new Npc(NpcToughness.boss_2)); // Magma Droid, boss.tyth.add_lava, npc.operation.iokath.enemy.difficulty_1.boss.tyth.add_lava
//		npcs.put(4078432224804864L, new Npc(NpcToughness.boss_1)); // Justice, boss.tyth.add_pbae, npc.operation.iokath.enemy.difficulty_1.boss.tyth.add_pbae

		npcs.put(4088525397950464L, new Npc(NpcType.boss_raid)); // AIVELA, boss.aivelesne.aivela, npc.operation.iokath.enemy.difficulty_1.boss.aivelesne.aivela
		npcs.put(4088538282852352L, new Npc(NpcType.boss_raid)); // ESNE, boss.aivelesne.esne, npc.operation.iokath.enemy.difficulty_1.boss.aivelesne.esne
//		npcs.put(4105254295568384L, new Npc(NpcToughness.boss_1)); // Codex, boss.aivelesne.add_initial_obelisk, npc.operation.iokath.enemy.difficulty_1.boss.aivelesne.add_initial_obelisk
//		npcs.put(4101564918661120L, new Npc(NpcToughness.boss_2)); // Nexus, boss.aivelesne.add_monitor, npc.operation.iokath.enemy.difficulty_1.boss.aivelesne.add_monitor
//		npcs.put(4109905745149952L, new Npc(NpcToughness.boss_1)); // Countermeasure, boss.aivelesne.add_obelisk, npc.operation.iokath.enemy.difficulty_1.boss.aivelesne.add_obelisk
//		npcs.put(4109914335084544L, new Npc(NpcToughness.boss_1)); // Glare, boss.aivelesne.add_pyramid, npc.operation.iokath.enemy.difficulty_1.boss.aivelesne.add_pyramid

		npcs.put(4108011664572416L, new Npc(NpcType.boss_raid)); // NAHUT, boss.nahut.nahut, npc.operation.iokath.enemy.difficulty_1.boss.nahut.nahut
		npcs.put(4122949560827904L, new Npc(NpcType.boss_raid)); // NAHUT, boss.nahut.nahut, npc.operation.iokath.enemy.difficulty_2.boss.nahut.nahut
		npcs.put(4122953855795200L, new Npc(NpcType.boss_raid)); // NAHUT, boss.nahut.nahut, npc.operation.iokath.enemy.difficulty_3.boss.nahut.nahut
		npcs.put(4122958150762496L, new Npc(NpcType.boss_raid)); // NAHUT, boss.nahut.nahut, npc.operation.iokath.enemy.difficulty_4.boss.nahut.nahut
//		npcs.put(4122580193640448L, new Npc(NpcToughness.boss_raid)); // Singularity Chamber, boss.nahut.anchor_center, npc.operation.iokath.enemy.difficulty_1.boss.nahut.anchor_center
		npcs.put(4131728473980928L, new Npc(NpcType.boss_1)); // Conduit Coupling, boss.nahut.bridge_power_coupling, npc.operation.iokath.enemy.difficulty_1.boss.nahut.bridge_power_coupling
		npcs.put(4131857322999808L, new Npc(NpcType.boss_1)); // Power Coupling, boss.nahut.bridge_power_coupling_inner_a, npc.operation.iokath.enemy.difficulty_1.boss.nahut.bridge_power_coupling_inner_a
		npcs.put(4131861617967104L, new Npc(NpcType.boss_1)); // Power Coupling, boss.nahut.bridge_power_coupling_inner_b, npc.operation.iokath.enemy.difficulty_1.boss.nahut.bridge_power_coupling_inner_b
		npcs.put(4131865912934400L, new Npc(NpcType.boss_1)); // Power Coupling, boss.nahut.bridge_power_coupling_outer_a, npc.operation.iokath.enemy.difficulty_1.boss.nahut.bridge_power_coupling_outer_a
		npcs.put(4131870207901696L, new Npc(NpcType.boss_1)); // Power Coupling, boss.nahut.bridge_power_coupling_outer_b, npc.operation.iokath.enemy.difficulty_1.boss.nahut.bridge_power_coupling_outer_b
//		npcs.put(4151352179556352L, new Npc(NpcToughness.boss_4)); // Angry Button, boss.nahut.button, npc.operation.iokath.enemy.difficulty_1.boss.nahut.button
//		npcs.put(4132742086262784L, new Npc(NpcToughness.boss_2)); // Extermination Droid, boss.nahut.extermination_droid, npc.operation.iokath.enemy.difficulty_1.boss.nahut.extermination_droid
		npcs.put(4132746381230080L, new Npc(NpcType.boss_1)); // Rail Turret, boss.nahut.rail_turret, npc.operation.iokath.enemy.difficulty_1.boss.nahut.rail_turret
//		npcs.put(4122782057103360L, new Npc(NpcToughness.boss_1)); // Pin, boss.nahut.stealth_mine, npc.operation.iokath.enemy.difficulty_1.boss.nahut.stealth_mine
//		npcs.put(4124985375326208L, new Npc(NpcToughness.boss_1)); // Nail, boss.nahut.stealth_mine_large, npc.operation.iokath.enemy.difficulty_1.boss.nahut.stealth_mine_large

		npcs.put(4108140513591296L, new Npc(NpcType.boss_raid)); // SCYVA, boss.scyva.scyva, npc.operation.iokath.enemy.difficulty_1.boss.scyva.scyva
		npcs.put(4126522973618176L, new Npc(NpcType.boss_1 /*boss_raid*/)); // CFA-Railgun, boss.scyva.cfa_railgun_blue, npc.operation.iokath.enemy.difficulty_1.boss.scyva.cfa_railgun_blue
		npcs.put(4126518678650880L, new Npc(NpcType.boss_1 /*boss_raid*/)); // CFA-Railgun, boss.scyva.cfa_railgun_red, npc.operation.iokath.enemy.difficulty_1.boss.scyva.cfa_railgun_red
		npcs.put(4126527268585472L, new Npc(NpcType.boss_2 /*boss_raid*/)); // Omega Protocol Droid, boss.scyva.omega_protocol_droid, npc.operation.iokath.enemy.difficulty_1.boss.scyva.omega_protocol_droid
//		npcs.put(4126578808193024L, new Npc(NpcToughness.boss_raid)); // Emergency Purge, boss.scyva.emergency_purge, npc.operation.iokath.enemy.difficulty_1.boss.scyva.emergency_purge
//		npcs.put(4148908343164928L, new Npc(NpcToughness.boss_raid)); // Explosive Swarm Droid, boss.scyva.explosive_swarm_droid, npc.operation.iokath.enemy.difficulty_1.boss.scyva.explosive_swarm_droid
//		npcs.put(4126553038389248L, new Npc(NpcToughness.boss_raid)); // Ignition Catalyst, boss.scyva.ignition_catalyst, npc.operation.iokath.enemy.difficulty_1.boss.scyva.ignition_catalyst
//		npcs.put(4126686182375424L, new Npc(NpcToughness.boss_4)); // Purge Beam Emitter, boss.scyva.purge_beam_emitter_blue, npc.operation.iokath.enemy.difficulty_1.boss.scyva.purge_beam_emitter_blue
//		npcs.put(4126690477342720L, new Npc(NpcToughness.boss_4)); // Purge Beam Emitter, boss.scyva.purge_beam_emitter_red, npc.operation.iokath.enemy.difficulty_1.boss.scyva.purge_beam_emitter_red
//		npcs.put(4159100300558336L, new Npc(NpcToughness.boss_raid)); // Scyva Decoration, boss.scyva.scyva_decoration, npc.operation.iokath.enemy.difficulty_1.boss.scyva.scyva_decoration
//		npcs.put(4126574513225728L, new Npc(NpcToughness.boss_raid)); // Shield Generator, boss.scyva.shield_generator, npc.operation.iokath.enemy.difficulty_1.boss.scyva.shield_generator
//		npcs.put(4136032031211520L, new Npc(NpcToughness.boss_raid)); // Shield Generator Damaged, boss.scyva.shield_generator_damaged_aoe, npc.operation.iokath.enemy.difficulty_1.boss.scyva.shield_generator_damaged_aoe
//		npcs.put(4134734951088128L, new Npc(NpcToughness.boss_raid)); // Shield Generator, boss.scyva.shield_generator_east, npc.operation.iokath.enemy.difficulty_1.boss.scyva.shield_generator_east
//		npcs.put(4134739246055424L, new Npc(NpcToughness.boss_raid)); // Shield Generator, boss.scyva.shield_generator_west, npc.operation.iokath.enemy.difficulty_1.boss.scyva.shield_generator_west
//		npcs.put(4126540153487360L, new Npc(NpcToughness.boss_raid)); // Stasis Swarm Droid, boss.scyva.stasis_swarm_droid, npc.operation.iokath.enemy.difficulty_1.boss.scyva.stasis_swarm_droid
//		npcs.put(4126531563552768L, new Npc(NpcToughness.boss_raid)); // Sterilization Array Module, boss.scyva.sterilization_array_module, npc.operation.iokath.enemy.difficulty_1.boss.scyva.sterilization_array_module

		npcs.put(4108097563918336L, new Npc(NpcType.boss_raid)); // IZAX, boss.izax.izax, npc.operation.iokath.enemy.difficulty_1.boss.izax.izax
		npcs.put(4163751750139904L, new Npc(NpcType.boss_raid, "GEMINI")); // Repurposed GEMINI Droid, boss.izax.gemini_scyva, npc.operation.iokath.enemy.difficulty_1.boss.izax.gemini_scyva
//		npcs.put(4160753862967296L, new Npc(NpcToughness.boss_1)); // Amplifier, boss.izax.amplifier_droid, npc.operation.iokath.enemy.difficulty_1.boss.izax.amplifier_droid
//		npcs.put(4163309368508416L, new Npc(NpcToughness.boss_1)); // Makeshift Amplifier Drone, boss.izax.amplifier_droid_scyva, npc.operation.iokath.enemy.difficulty_1.boss.izax.amplifier_droid_scyva
//		npcs.put(4160740978065408L, new Npc(NpcToughness.boss_2)); // Anchor Drone, boss.izax.anchor_droid, npc.operation.iokath.enemy.difficulty_1.boss.izax.anchor_droid
//		npcs.put(4164872736604160L, new Npc(NpcToughness.boss_1)); // Combat Mine, boss.izax.combat_mine, npc.operation.iokath.enemy.difficulty_1.boss.izax.combat_mine
//		npcs.put(4159851919835136L, new Npc(NpcToughness.boss_2)); // Conductor Droid, boss.izax.conductor_droid, npc.operation.iokath.enemy.difficulty_1.boss.izax.conductor_droid
//		npcs.put(4162531979427840L, new Npc(NpcToughness.boss_4)); // Augment Droid, boss.izax.dps_buffing_orb, npc.operation.iokath.enemy.difficulty_1.boss.izax.dps_buffing_orb
//		npcs.put(4161084575449088L, new Npc(NpcToughness.boss_1)); // Energy Drone, boss.izax.energy_droid, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid
//		npcs.put(4167342342799360L, new Npc(NpcToughness.boss_1)); // Energy Droid, boss.izax.energy_droid_p5, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid_p5
//		npcs.put(4167475486785536L, new Npc(NpcToughness.boss_1)); // Energy Drone, boss.izax.energy_droid_p5_01, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid_p5_01
//		npcs.put(4167479781752832L, new Npc(NpcToughness.boss_1)); // Energy Drone, boss.izax.energy_droid_p5_02, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid_p5_02
//		npcs.put(4167484076720128L, new Npc(NpcToughness.boss_1)); // Energy Drone, boss.izax.energy_droid_p5_03, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid_p5_03
//		npcs.put(4167488371687424L, new Npc(NpcToughness.boss_1)); // Energy Drone, boss.izax.energy_droid_p5_04, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid_p5_04
//		npcs.put(4167492666654720L, new Npc(NpcToughness.boss_1)); // Energy Drone, boss.izax.energy_droid_p5_05, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid_p5_05
//		npcs.put(4167496961622016L, new Npc(NpcToughness.boss_1)); // Energy Drone, boss.izax.energy_droid_p5_06, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid_p5_06
//		npcs.put(4167501256589312L, new Npc(NpcToughness.boss_1)); // Energy Drone, boss.izax.energy_droid_p5_07, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid_p5_07
//		npcs.put(4163412447723520L, new Npc(NpcToughness.boss_2)); // Makeshift Energy Drone, boss.izax.energy_droid_scyva, npc.operation.iokath.enemy.difficulty_1.boss.izax.energy_droid_scyva
//		npcs.put(4166255716073472L, new Npc(NpcToughness.boss_1)); // Hull Cutter Drone, boss.izax.hull_cutter, npc.operation.iokath.enemy.difficulty_1.boss.izax.hull_cutter
//		npcs.put(4164761067454464L, new Npc(NpcToughness.boss_4)); // Missile Target, boss.izax.missile_target, npc.operation.iokath.enemy.difficulty_1.boss.izax.missile_target
//		npcs.put(4164825491963904L, new Npc(NpcToughness.boss_1)); // Energy Drone, boss.izax.scyva_energy_drone, npc.operation.iokath.enemy.difficulty_1.boss.izax.scyva_energy_drone
//		npcs.put(4164821196996608L, new Npc(NpcToughness.boss_1)); // Tether Drone, boss.izax.scyva_tether_droid, npc.operation.iokath.enemy.difficulty_1.boss.izax.scyva_tether_droid
//		npcs.put(4160801107607552L, new Npc(NpcToughness.boss_1)); // Bypass Conduit, boss.izax.socket_hookpoint, npc.operation.iokath.enemy.difficulty_1.boss.izax.socket_hookpoint
//		npcs.put(4168841286385664L, new Npc(NpcToughness.boss_1)); // Overloaded Bypass Conduit, boss.izax.socket_hookpoint_overloaded, npc.operation.iokath.enemy.difficulty_1.boss.izax.socket_hookpoint_overloaded
//		npcs.put(4162330115964928L, new Npc(NpcToughness.boss_1)); // Unpowered Bypass Conduit, boss.izax.socket_hookpoint_unpowered, npc.operation.iokath.enemy.difficulty_1.boss.izax.socket_hookpoint_unpowered
//		npcs.put(4159890574540800L, new Npc(NpcToughness.boss_1)); // Electro-Tether Droid, boss.izax.tether_droid, npc.operation.iokath.enemy.difficulty_1.boss.izax.tether_droid
//		npcs.put(4167058874957824L, new Npc(NpcToughness.boss_1)); // Electro-Tether Droid, boss.izax.tether_droid_dps, npc.operation.iokath.enemy.difficulty_1.boss.izax.tether_droid_dps
//		npcs.put(4167063169925120L, new Npc(NpcToughness.boss_1)); // Electro-Tether Droid, boss.izax.tether_droid_healer, npc.operation.iokath.enemy.difficulty_1.boss.izax.tether_droid_healer


//		npcs.put(4075490172207104L, new Npc(NpcToughness.boss_2)); // Immolation Droid, raidTrash, flame_droid, npc.operation.iokath.enemy.difficulty_1.trash.flame_droid
//		npcs.put(4116803462627328L, new Npc(NpcToughness.boss_1)); // Deflection Droid, raidTrash, multi_deflection_droid, npc.operation.iokath.enemy.difficulty_1.trash.multi_deflection_droid
//		npcs.put(4125728404668416L, new Npc(NpcToughness.boss_1)); // Loneliest Mine Layer, raidTrash, multi_mine_layer, npc.operation.iokath.enemy.difficulty_1.trash.multi_mine_layer
//		npcs.put(4116446980341760L, new Npc(NpcToughness.boss_2)); // Optimization Droid, raidTrash, multi_optimization_droid, npc.operation.iokath.enemy.difficulty_1.trash.multi_optimization_droid
//		npcs.put(4075803704819712L, new Npc(NpcToughness.boss_1)); // Preservation Droid, raidTrash, multi_preservation_droid, npc.operation.iokath.enemy.difficulty_1.trash.multi_preservation_droid
//		npcs.put(4075842359525376L, new Npc(NpcToughness.boss_1)); // Scour Droid, raidTrash, multi_scour_droid, npc.operation.iokath.enemy.difficulty_1.trash.multi_scour_droid
//		npcs.put(4116326721257472L, new Npc(NpcToughness.boss_2)); // Reconfigured Scour Droid, raidTrash, multi_scour_droid_hardonly, npc.operation.iokath.enemy.difficulty_1.trash.multi_scour_droid_hardonly
//		npcs.put(4176529277845504L, new Npc(NpcToughness.boss_1)); // Suspicious Device, raidTrash, multi_stalking_mine_nightmare, npc.operation.iokath.enemy.difficulty_1.trash.multi_stalking_mine_nightmare
//		npcs.put(4148513206173696L, new Npc(NpcToughness.boss_1)); // Vindictive Mine Layer, raidTrash, multi_vindictive_mine_layer, npc.operation.iokath.enemy.difficulty_1.trash.multi_vindictive_mine_layer
//		npcs.put(4176456263401472L, new Npc(NpcToughness.boss_1)); // Vindictive Preservation Droid, raidTrash, multi_vindictive_preservation_droid, npc.operation.iokath.enemy.difficulty_1.trash.multi_vindictive_preservation_droid
//		npcs.put(4077057835270144L, new Npc(NpcToughness.boss_1)); // Combat Droid, raidTrash, poi1_commando_droid, npc.operation.iokath.enemy.difficulty_1.trash.poi1_commando_droid
//		npcs.put(4092558372241408L, new Npc(NpcToughness.boss_1)); // Combat Droid, raidTrash, poi1_commando_droid_hardonly, npc.operation.iokath.enemy.difficulty_1.trash.poi1_commando_droid_hardonly
//		npcs.put(4068373411397632L, new Npc(NpcToughness.boss_2)); // Magma Droid, raidTrash, poi1_magma_droid, npc.operation.iokath.enemy.difficulty_1.trash.poi1_magma_droid
//		npcs.put(4116099087990784L, new Npc(NpcToughness.boss_1)); // Plasma Droid, raidTrash, poi2_plasma_droid, npc.operation.iokath.enemy.difficulty_1.trash.poi2_plasma_droid
//		npcs.put(4116300951453696L, new Npc(NpcToughness.boss_2)); // Vindictive Plasma Droid, raidTrash, poi2_plasma_droid_hardonly, npc.operation.iokath.enemy.difficulty_1.trash.poi2_plasma_droid_hardonly
//		npcs.put(4116249411846144L, new Npc(NpcToughness.boss_1)); // Reserve Combat Droid, raidTrash, poi2_reserve_combat_droid, npc.operation.iokath.enemy.difficulty_1.trash.poi2_reserve_combat_droid
//		npcs.put(4116717563281408L, new Npc(NpcToughness.boss_1)); // Extermination Droid, raidTrash, poi3_extermination_droid, npc.operation.iokath.enemy.difficulty_1.trash.poi3_extermination_droid
//		npcs.put(4118122017587200L, new Npc(NpcToughness.boss_2)); // Cruel Extermination Droid, raidTrash, poi3_extermination_droid_hardonly, npc.operation.iokath.enemy.difficulty_1.trash.poi3_extermination_droid_hardonly
//		npcs.put(4118255161573376L, new Npc(NpcToughness.boss_1)); // Cloaking Mine, raidTrash, poi3_stealth_mine, npc.operation.iokath.enemy.difficulty_1.trash.poi3_stealth_mine
//		npcs.put(4118353945821184L, new Npc(NpcToughness.boss_1)); // Improved Cloaking Mine, raidTrash, poi3_stealth_mine_hardonly, npc.operation.iokath.enemy.difficulty_1.trash.poi3_stealth_mine_hardonly
//		npcs.put(4119088385228800L, new Npc(NpcToughness.boss_1)); // Motorized Cloaking Mine, raidTrash, poi3_stealth_mine_hardonly_mobile, npc.operation.iokath.enemy.difficulty_1.trash.poi3_stealth_mine_hardonly_mobile
//		npcs.put(4176877170196480L, new Npc(NpcToughness.boss_1)); // Armored Mine, raidTrash, poi4_armored_mine_nightmare, npc.operation.iokath.enemy.difficulty_1.trash.poi4_armored_mine_nightmare
//		npcs.put(4125161468985344L, new Npc(NpcToughness.boss_1)); // Sniper, raidTrash, poi4_drainage_room_sniper, npc.operation.iokath.enemy.difficulty_1.trash.poi4_drainage_room_sniper
//		npcs.put(4119547946729472L, new Npc(NpcToughness.boss_2)); // Scyvan Hunter, raidTrash, poi4_scyvan_hunter, npc.operation.iokath.enemy.difficulty_1.trash.poi4_scyvan_hunter
//		npcs.put(4120054752870400L, new Npc(NpcToughness.boss_2)); // Scyvan Hunter, raidTrash, poi4_scyvan_hunter_hardonly, npc.operation.iokath.enemy.difficulty_1.trash.poi4_scyvan_hunter_hardonly
//		npcs.put(4142749360062464L, new Npc(NpcToughness.boss_raid)); // Scyvan Matriarch, raidTrash, poi4_scyvan_matriarch, npc.operation.iokath.enemy.difficulty_1.trash.poi4_scyvan_matriarch
//		npcs.put(4119693975617536L, new Npc(NpcToughness.boss_1)); // Scyvan Swarmer, raidTrash, poi4_scyvan_swarmer_01, npc.operation.iokath.enemy.difficulty_1.trash.poi4_scyvan_swarmer_01
//		npcs.put(4119689680650240L, new Npc(NpcToughness.boss_1)); // Scyvan Swarmer, raidTrash, poi4_scyvan_swarmer_02, npc.operation.iokath.enemy.difficulty_1.trash.poi4_scyvan_swarmer_02
//		npcs.put(4119672500781056L, new Npc(NpcToughness.boss_2)); // Sniper Droid, raidTrash, poi4_sniper_droid, npc.operation.iokath.enemy.difficulty_1.trash.poi4_sniper_droid
//		npcs.put(4123147129323520L, new Npc(NpcToughness.boss_1)); // Sniper Droid, raidTrash, poi4_sniper_droid_hardonly, npc.operation.iokath.enemy.difficulty_1.trash.poi4_sniper_droid_hardonly
//		npcs.put(4123361877688320L, new Npc(NpcToughness.boss_1)); // Sniper Droid, raidTrash, poi4_sniper_droid_heroblade, npc.operation.iokath.enemy.difficulty_1.trash.poi4_sniper_droid_heroblade
//		npcs.put(4145992060370944L, new Npc(NpcToughness.boss_1)); // Stealth Mine, raidTrash, poi4_stealth_mine, npc.operation.iokath.enemy.difficulty_1.trash.poi4_stealth_mine
//		npcs.put(4145987765403648L, new Npc(NpcToughness.boss_1)); // Stealth Mine, raidTrash, poi4_stealth_mine_hardonly, npc.operation.iokath.enemy.difficulty_1.trash.poi4_stealth_mine_hardonly
//		npcs.put(4145996355338240L, new Npc(NpcToughness.boss_1)); // Stealth Mine, raidTrash, poi4_stealth_mine_hardonly_mobile, npc.operation.iokath.enemy.difficulty_1.trash.poi4_stealth_mine_hardonly_mobile
//		npcs.put(4176477738237952L, new Npc(NpcToughness.boss_1)); // Stealth Mine, raidTrash, poi4_stealth_mine_nightmare, npc.operation.iokath.enemy.difficulty_1.trash.poi4_stealth_mine_nightmare
//		npcs.put(4148457371598848L, new Npc(NpcToughness.boss_1)); // Vengeful Mine, raidTrash, poi4_unstealthed_mine_mobile, npc.operation.iokath.enemy.difficulty_1.trash.poi4_unstealthed_mine_mobile
//		npcs.put(4157481097887744L, new Npc(NpcToughness.boss_1)); // Beam Emitter Node, raidTrash, poi5_beam_emitter_node, npc.operation.iokath.enemy.difficulty_1.trash.poi5_beam_emitter_node
//		npcs.put(4158739523305472L, new Npc(NpcToughness.boss_1)); // Overloaded Beam Emitter, raidTrash, poi5_beam_emitter_overloaded, npc.operation.iokath.enemy.difficulty_1.trash.poi5_beam_emitter_overloaded
//		npcs.put(4158099573178368L, new Npc(NpcToughness.boss_1)); // Beam Null Receiver, raidTrash, poi5_beam_null_receiver, npc.operation.iokath.enemy.difficulty_1.trash.poi5_beam_null_receiver
//		npcs.put(4157678666383360L, new Npc(NpcToughness.boss_1)); // Beam Receiver Node, raidTrash, poi5_beam_receiver_node, npc.operation.iokath.enemy.difficulty_1.trash.poi5_beam_receiver_node
//		npcs.put(4158421695725568L, new Npc(NpcToughness.boss_1)); // Beam Receiver Node, raidTrash, poi5_beam_receiver_node_01, npc.operation.iokath.enemy.difficulty_1.trash.poi5_beam_receiver_node_01
//		npcs.put(4158425990692864L, new Npc(NpcToughness.boss_1)); // Beam Receiver Node, raidTrash, poi5_beam_receiver_node_02, npc.operation.iokath.enemy.difficulty_1.trash.poi5_beam_receiver_node_02
//		npcs.put(4156909867237376L, new Npc(NpcToughness.boss_1)); // Deflection Droid, raidTrash, poi5_deflection_droid, npc.operation.iokath.enemy.difficulty_1.trash.poi5_deflection_droid
//		npcs.put(4159259214348288L, new Npc(NpcToughness.boss_1)); // Deflection Droid, raidTrash, poi5_deflection_droid_story, npc.operation.iokath.enemy.difficulty_1.trash.poi5_deflection_droid_story
//		npcs.put(4158331501412352L, new Npc(NpcToughness.boss_1)); // Electro-Tether Droid, raidTrash, poi5_electro_tether_droid, npc.operation.iokath.enemy.difficulty_1.trash.poi5_electro_tether_droid
//		npcs.put(4159267804282880L, new Npc(NpcToughness.boss_1)); // Electro Tether Droid, raidTrash, poi5_electro_tether_droid_story, npc.operation.iokath.enemy.difficulty_1.trash.poi5_electro_tether_droid_story

	}

	@Override
	public String getNewPhaseName(final Event e, final Combat c, final String currentPhaseName) {
		switch (c.getBoss().getRaidBossName()) {
			case Tyth:
				return getNewPhaseNameForTyth(e, c, currentPhaseName);
			case Nahut:
				return getNewPhaseNameForNahut(e, c, currentPhaseName);
			default:
				return null;
		}
	}

	private String getNewPhaseNameForTyth(final Event e, final Combat c, final String currentPhaseName) {
		if (c.getBoss().getMode() == Mode.SM) return null; // only HM/NiM

		// dummy phases
		if (currentPhaseName == null) {
			phaseTimers.clear();
			phaseTimers.put(TYTH_PHASE_INVERSION + "1", c.getTimeFrom());

			// setup timers
			TimerManager.startTimer(TythInversionTimer.class, c.getTimeFrom() - 11000); // first @ 26.5s

			return TYTH_PHASE_OPENING;
		}

		if (Helpers.isAbilityEqual(e, 4071117895499776L) && Helpers.isActionApply(e) && Helpers.isTargetThisPlayer(e)) { // Inversion
			if (Helpers.isEffectEqual(e, 4071117895500098L) || Helpers.isEffectEqual(e, 4071117895500102L)) { // Short/Long Wave
				TimerManager.stopTimer(TythInversionTimer.class);
				TimerManager.startTimer(TythInversionTimer.class, e.getTimestamp());

				for (int i = 1; i <= 10; i++) {
					if (((i == 1 && TYTH_PHASE_OPENING.equals(currentPhaseName))
							|| (TYTH_PHASE_INVERSION + " " + (i - 1)).equals(currentPhaseName))) {

						phaseTimers.put(TYTH_PHASE_INVERSION + (i + 1), -1L); // dummy
						phaseTimers.remove(TYTH_PHASE_INVERSION + i);

						return (TYTH_PHASE_INVERSION + " " + i);
					}
				}
			}
		}

		return null;
	}

	private String getNewPhaseNameForNahut(final Event e, final Combat c, final String currentPhaseName) {

		// ------------------ Timers ------------------

		if (Helpers.isAbilityEqual(e, 4137075708264448L) && Helpers.isEffectAbilityActivate(e)) { // Energized Slice
			TimerManager.stopTimer(NahutSliceTimer.class);
			TimerManager.startTimer(NahutSliceTimer.class, e.getTimestamp());
		}

		return null;
	}

	public static class TythInversionTimer extends BaseTimer {
		public TythInversionTimer() {
			super("Inversion", "Tyth Inversion", 37500);
			setColor(0);
		}
	}

	public static class NahutSliceTimer extends BaseTimer {
		public NahutSliceTimer() {
			super("Slice", "Nahut Slice", 12000);
			setColor(0);
		}
	}
}
