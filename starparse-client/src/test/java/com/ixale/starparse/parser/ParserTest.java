package com.ixale.starparse.parser;

import static junit.framework.Assert.*;

import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.CharacterClass;
import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.EntityGuid;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid.Mode;
import com.ixale.starparse.domain.Raid.Size;
import com.ixale.starparse.service.impl.Context;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-context.xml")
public class ParserTest {


	private Parser createParser() throws Exception {
		final Parser p = new Parser(new Context());
		p.setCombatLogFile(new File("combat_2000-01-01_12_34_46_789000.txt"));
		return p;
	}
//
//	@Test
//	@SuppressWarnings("deprecation")
//	public void testCalendar() throws Exception {
//		p.setCombatLogFile(new File("combat_1472-02-28_23_57_22_258915.txt"));
//		p.getContext().reset();
//		
//		final String[] lines = new String[]{
//			"[23:29:22.931] [@Ci'korl] [@Ci'korl] [Surging Charge {948659491438592}] [RemoveEffect {836045448945478}: Static Charge {948659491438915}] ()",
//			"[00:02:22.931] [@Ci'korl] [@Ci'korl] [Surging Charge {948659491438592}] [RemoveEffect {836045448945478}: Static Charge {948659491438915}] ()",
//			};
//		
//		p.parseLogLine(lines[0]);
//		p.parseLogLine(lines[1]);
//		assertEquals(2018 - 1900, new Date(p.getEvents().get(1).getTimestamp()).getYear());
//		assertEquals(2 + 2, new Date(p.getEvents().get(1).getTimestamp()).getDay());
//	}

	@Test
	public void testPatterns() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Event e;

		final String
				LINE_COMBAT_ENTER = "[14:14:35.306] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()",
				LINE_HEAL = "[00:38:54.942] [@Ixale] [@Ixale] [Med Scan {2074812801351680}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (1643*) <366>",
				LINE_RESTORE_FOCUS = "[01:20:25.572] [@Ixale] [@Ixale] [] [Restore {836045448945476}: focus point {836045448938496}] (1)",
				LINE_COMPANION = "[17:39:03.117] [@Ixale:Doc {404577329348608}] [@Ixale:Doc {404577329348608}] [ {2531161666486272}] [ApplyEffect {836045448945477}: Lucky Shots {2531161666486533}] ()",
				LINE_HIT = "[14:41:50.161] [Dread Master Calphayus {3273946195558400}:24640000243110] [@Ixale] [Distorted Perceptions {3303504160489472}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (3089 elemental {836045448940875}) <3089>",
				LINE_MITIGATED = "[22:03:32.959] [Dread Master Calphayus {3273989145231360}:18566012716570] [@Ixale] [Melee Attack {3301081798934528}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (843 energy {836045448940874} -shield {836045448945509}) <843>",
				LINE_ABSORBED = "[02:09:56.925] [Aberrant Guardian {3295232053477376}:18481014605337] [@Ixale] [Throw {3306441918119936}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (1131 kinetic {836045448940873} -shield {836045448945509} (1131 absorbed {836045448945511})) <1131>",
				LINE_REFLECTED = "[00:53:27.723] [Palace Guardian {3295459686744064}:18566012136384] [@Ixale] [Saber Reflect {3305905047207936}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (289 internal {836045448940876}(reflected {836045448953649})) <289>",
				LINE_MISSED_UNKNOWN = "[00:53:27.723] [@Ixale] [Palace Guardian {3295459686744064}:18566012136384] [Combat Technique {979806594269184}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (0 -) <508>",
				LINE_MISSED_PARRY = "[03:03:05.421] [@Ixaar] [Dread Master Raptus {3302902865068032}:436007630766] [Strike {947362411315200}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (0 -parry {836045448945503}) <1>",
				LINE_UNKNOWN_DAMAGE = "[22:36:08.099] [@Ixale] [@Ixale] [Weight of Knowledge {3306252939559174}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (2042 )",
				LINE_GER = "[20:02:43.072] [Palasthüter {3295459686744064}:4983000110759] [@Kel'lara] [Schwertreflexion {3305905047207936}] [EffektAnwenden {836045448945477}: Schaden {836045448945501}] (186 Körperlich {836045448940876}(reflektiert {836045448953649})) <186>",
				LINE_GSF = "[18:03:09.038] [5016000057656] [5016000057656] [ {3301773288669184}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()",
				LINE_GSF2 = "[20:11:59.191] [] [35027000013151] [Ion Railgun {3301786173571072}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (854 kinetic {836045448940873})",
				LINE_EMPTY_NPC_ID = "[15:46:33.319] [@Kalcat] [Master Computer {2920848344219648}] [Attack Me {800345680773120}] [Event {836045448945472}: ModifyThreat {836045448945483}] () <0>",
				LINE_NEGATIVE_ENERGY = "[20:41:48.965] [@Rikacha] [@Rikacha] [] [Restore {836045448945476}: energy {836045448938503}] (-10)",
				LINE_DOT = "[23:39:54.713] [@Ixayla] [@Ixayla] [Plasma Grenade {2297266337480704}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()",
				LINE_EMPTY_NPC_ID2 = "[14:18:51.998] [Rival Acolyte {287749923930112}] [@Vhaerys] [Shocked {811954977374482}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (21 energy {836045448940874}) <21>",
				LINE_MITIGATION_RESIST = "[06:22:22.800] [@Ixalo] [Colossal Monolith {3570947479044096}:943000102572] [Electro Net {3066456325488640}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (0 -resist {836045448945507}) <1>",
				LINE_OVERFLOW_THREAT = "[17:53:10.893] [@Ixale] [Worldbreaker Monolith {3547338043817984}:6301003403864] [] [Event {836045448945472}: Taunt {836045448945488}] () <12278754304>",
				LINE_COMBAT_ENTER_54 = "[17:34:49.007] [@Nôfretete] [@Nôfretete] [] [Event {836045448945472}: EnterCombat {836045448945489}] (XS Freighter)",
				LINE_COMBAT_EXIT_54 = "[17:35:01.116] [@Nôfretete] [@Nôfretete] [] [Event {836045448945472}: ExitCombat {836045448945490}] (XS Freighter)",
				LINE_COMBAT_ENTER_54_OPS = "[21:56:15.395] [@Elidh] [@Elidh] [] [Event {836045448945472}: EnterCombat {836045448945489}] (Denova (8 Player Veteran))",
				LINE_COMBAT_EXIT_54_OPS = "[22:53:17.980] [@Meirín] [@Meirín] [] [Event {836045448945472}: ExitCombat {836045448945490}] (The Dread Palace (8 Player Veteran))",
				LINE_V7_ZONE_START = "[21:10:14.218] [@Lad Dominic#689199156848288|(63.84,1553.43,229.43,180.00)|(1/392212)] [] [] [AreaEntered {836045448953664}: Valley of the Machine Gods {833571547775765}] (HE600) <v7.0.0b>",
				LINE_V7_OPS_START = "[21:10:14.248] [@Lad Dominic#689199156848288|(63.84,1553.43,229.43,180.00)|(392212/406515)] [] [] [AreaEntered {836045448953664}: Valley of the Machine Gods {833571547775765} 8 Player Veteran {836045448953652}] (HE600) <v7.0.0b>",
				LINE_V7_COMBAT_START = "[21:11:27.222] [@Lad Dominic#689199156848288|(68.38,1562.25,229.28,-0.45)|(406515/406515)] [] [] [Event {836045448945472}: EnterCombat {836045448945489}]",
				LINE_V7_DISCIPLINE_CHANGED = "[21:11:27.222] [@Lad Dominic#689199156848288|(68.38,1562.25,229.28,-0.45)|(406515/406515)] [] [] [DisciplineChanged {836045448953665}: Operative {16140905232405801950}/Medicine {2031339142381596}]",
				LINE_V7_NPC = "[21:12:17.985] [Combat Droid {4077057835270144}:2869000006287|(810.47,1901.19,232.42,-85.42)|(372762/389913)] [@Notquiteenoughviable#689993934789570|(826.04,1899.97,231.97,81.90)|(92163/364564)] [Charged Rounds {1798822497878016}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (8131 energy {836045448940874} -shield {836045448945509} (29098 absorbed {836045448945511})) <8131>",
				LINE_V7_OTHER = "[21:12:21.817] [Combat Droid {4077057835270144}:2869000006199|(826.83,1901.74,232.10,23.94)|(149668/389913)] [@Notquiteenoughviable#689993934789570|(826.04,1899.97,231.97,86.26)|(0/364564)] [Armor Piercing Cell {828301622902784}] [ApplyEffect {836045448945477}: Armor Reduced {828301622903053}] (1 charges {836045448953667})",
				LINE_V7_COMBAT_EXIT = "[21:13:05.214] [@Lad Dominic#689199156848288|(814.39,1892.66,232.42,-177.54)|(406515/406515)] [] [] [Event {836045448945472}: ExitCombat {836045448945490}]",
				LINE_V7_COMPANION = "[21:52:51.302] [@Ixale#689198861050949/Lana Beniko {3600471084236800}:109012087183|(4783.91,4694.78,705.49,168.73)|(101072/105329)] [=] [ {4196681264398336}] [ApplyEffect {836045448945477}: Hunter's Boon {4196681264398649}]",
				LINE_V7_DAMAGE = "[21:31:57.629] [@Lad Dominic#689199156848288|(816.61,2355.95,228.51,47.00)|(406515/406515)] [Lance {4078449404674048}:2869000269692|(821.28,2360.98,228.36,108.95)|(188742/233686)] [Rifle Shot {948041016147968}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (2951 ~2952 energy {836045448940874}) <2951>",
				LINE_V7_ABSORBED = "[21:31:59.247] [TYTH {4078423634870272}:2869000253855|(809.10,2366.34,228.51,160.73)|(14634918/18325876)] [@Notquiteenoughviable#689993934789570|(807.38,2371.25,228.51,-19.26)|(250049/364564)] [Melee Attack {4080283355709440}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (13255 ~0 energy {836045448940874} -shield {836045448945509} (13255 absorbed {836045448945511})) <13255>",
				LINE_V7_HEAL = "[21:32:00.522] [@Riku#689411359825172|(807.42,2351.52,228.51,-179.97)|(362091/371105)] [@Notquiteenoughviable#689993934789570|(807.38,2371.25,228.51,-19.26)|(267939/364564)] [Cure Mind {987851068014592}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (6785 ~6786) <3053>",
				LINE_V7_CHARGES = "[21:45:54.269] [@Ixale#689198861050949|(4740.13,4665.02,710.01,71.03)|(72122/72122)] [] [Power Barrier {3394793690365952}] [ModifyCharges {836045448953666}: Power Barrier {3394793690365952}] (2 charges {836045448953667})",
				LINE_V7_DEATH = "[21:11:59.079] [] [@Dot#689199156849986|(60.93,1556.10,229.28,-75.43)|(0/403442)] [] [Event {836045448945472}: Death {836045448945493}]";

		// setup combat to avoid "out of combat" errors
		p.parseLogLine(LINE_COMBAT_ENTER);
		e = p.getEvents().get(i++);

		assertEquals("Ixale", e.getSource().getName());
		assertEquals(Actor.Type.SELF, e.getSource().getType());
		assertEquals(e.getSource(), e.getTarget());

		// heal
		p.parseLogLine(LINE_HEAL);
		e = p.getEvents().get(i++);

		assertEquals("Ixale", e.getSource().getName());
		assertEquals(Actor.Type.SELF, e.getSource().getType());
		assertEquals(e.getSource(), e.getTarget());
		assertEquals(Integer.valueOf(1643), e.getValue());
		assertTrue(e.isCrit());
		assertEquals(Long.valueOf(366), e.getThreat());
		assertEquals(Integer.valueOf(0), e.getGuardState()); // not guarded

		// restore
		p.parseLogLine(LINE_RESTORE_FOCUS);
		e = p.getEvents().get(i++);

		assertEquals(Integer.valueOf(1), e.getValue());
		assertEquals((long) e.getAction().getGuid(), EntityGuid.Restore.getGuid());

		// companion
		p.parseLogLine(LINE_COMPANION);
		e = p.getEvents().get(i++);

		assertEquals(Actor.Type.COMPANION, e.getSource().getType());
		assertEquals(e.getTarget(), e.getSource());
		assertEquals("Doc", e.getSource().getName());

		// mitigated damage
		p.parseLogLine(LINE_MITIGATED);
		e = p.getEvents().get(i++);

		assertEquals(Long.valueOf(3273989145231360L), e.getSource().getGuid());
		assertEquals(Actor.Type.SELF, e.getTarget().getType());
		assertEquals((long) e.getEffect().getGuid(), EntityGuid.Damage.getGuid());
		assertEquals(Integer.valueOf(843), e.getValue());
		assertEquals((long) e.getDamage().getGuid(), EntityGuid.Energy.getGuid());
		assertEquals((long) e.getMitigation().getGuid(), EntityGuid.Shield.getGuid());
		assertNull(e.getReflect());
		assertNull(e.getAbsorbtion());
		assertNull(e.getAbsorbed());

		// absorbed damage
		p.parseLogLine(LINE_ABSORBED);
		e = p.getEvents().get(i++);

		assertNull(e.getReflect());
		assertNotNull(e.getAbsorbtion());
		assertEquals(Integer.valueOf(1131), e.getAbsorbed());

		// reflected damage
		p.parseLogLine(LINE_REFLECTED);
		e = p.getEvents().get(i++);

		assertNull(e.getMitigation());
		assertNotNull(e.getReflect());
		assertNull(e.getAbsorbtion());
		assertNull(e.getAbsorbed());

		// missed - unknown
		p.parseLogLine(LINE_MISSED_UNKNOWN);
		e = p.getEvents().get(i++);

		assertEquals(Integer.valueOf(0), e.getValue());
		assertNotNull(e.getMitigation());
		assertNull(e.getReflect());
		assertNull(e.getAbsorbtion());
		assertNull(e.getAbsorbed());
		assertEquals(Long.valueOf(508), e.getThreat());

		// missed - parry
		p.parseLogLine(LINE_MISSED_PARRY);
		e = p.getEvents().get(i++);

		assertEquals(Integer.valueOf(0), e.getValue());
		assertEquals("parry", e.getMitigation().getName());
		assertNull(e.getReflect());
		assertNull(e.getAbsorbtion());
		assertNull(e.getAbsorbed());
		assertEquals(Long.valueOf(1), e.getThreat());

		// got hit
		p.parseLogLine(LINE_HIT);
		e = p.getEvents().get(i++);

		assertEquals(Long.valueOf(24640000243110L), e.getSource().getInstanceId());
		assertEquals(Integer.valueOf(3089), e.getValue());
		assertFalse(e.isCrit());
		assertEquals(Long.valueOf(3089), e.getThreat());

		// got hit - unknown type
		p.parseLogLine(LINE_UNKNOWN_DAMAGE);
		e = p.getEvents().get(i++);

		assertEquals(Integer.valueOf(2042), e.getValue());
		assertFalse(e.isCrit());
		assertNull(e.getMitigation());
		assertNull(e.getReflect());
		assertNull(e.getAbsorbtion());
		assertNull(e.getAbsorbed());
		assertNull(e.getThreat());

		// SAUERKRAUT BLITZKRIEG!
		p.getContext().reset();
		p.parseLogLine(LINE_GER);
		e = p.getEvents().get(i++);

		assertEquals(Integer.valueOf(186), e.getValue());
		assertFalse(e.isCrit());
		assertNull(e.getMitigation());
		assertNotNull(e.getReflect());
		assertNull(e.getAbsorbtion());
		assertNull(e.getAbsorbed());
		assertEquals(Long.valueOf(EntityGuid.Damage.getGuid()), e.getEffect().getGuid());
		assertEquals(Long.valueOf(EntityGuid.ApplyEffect.getGuid()), e.getAction().getGuid());

		// GSF nonsense
		p.getContext().reset();
		p.parseLogLine(LINE_GSF);
		p.parseLogLine(LINE_GSF2);
		try {
			e = p.getEvents().get(i++);
			fail();
		} catch (IndexOutOfBoundsException ex) {
			i--;
		}

		// sanity
		assertEquals(i, p.getEvents().size());

		p.getContext().reset();
		p.parseLogLine(LINE_EMPTY_NPC_ID);
		e = p.getEvents().get(i++);
		assertEquals(i, p.getEvents().size());

		p.getContext().reset();
		p.parseLogLine(LINE_NEGATIVE_ENERGY);
		e = p.getEvents().get(i++);
		assertEquals(-10, (int) e.getValue());
		assertEquals(i, p.getEvents().size());

		p.getContext().reset();
		p.parseLogLine(LINE_DOT);
		e = p.getEvents().get(i++);
		assertEquals(2297266337480704L, (long) e.getAbility().getGuid());
		assertEquals(i, p.getEvents().size());

		p.getContext().reset();
		p.parseLogLine(LINE_EMPTY_NPC_ID2);
		e = p.getEvents().get(i++);
		assertEquals(287749923930112L, (long) e.getSource().getGuid());
		assertEquals(0L, (long) e.getSource().getInstanceId());
		assertEquals(i, p.getEvents().size());

		p.getContext().reset();
		p.parseLogLine(LINE_MITIGATION_RESIST);
		e = p.getEvents().get(i++);
		assertEquals(3570947479044096L, (long) e.getTarget().getGuid());
		assertEquals(836045448945507L, (long) e.getMitigation().getGuid());
		assertEquals(i, p.getEvents().size());

		p.getContext().reset();
		p.parseLogLine(LINE_OVERFLOW_THREAT);
		e = p.getEvents().get(i++);
		assertEquals(Long.valueOf("12278754304"), e.getThreat()); // 12 278 754 304 capped
		assertEquals(Long.valueOf("12278754304"), e.getEffectiveThreat()); // 12 278 754 304 capped
		assertEquals(i, p.getEvents().size());

		p.getContext().reset();
		p.parseLogLine(LINE_COMBAT_ENTER_54);
		e = p.getEvents().get(i++);
		p.parseLogLine(LINE_COMBAT_EXIT_54);
		e = p.getEvents().get(i++);
		p.parseLogLine(LINE_COMBAT_ENTER_54_OPS);
		e = p.getEvents().get(i++);
		p.parseLogLine(LINE_COMBAT_EXIT_54_OPS);
		e = p.getEvents().get(i++);
		assertEquals(i, p.getEvents().size());

		// v7
		p.getContext().reset();
		p.parseLogLine(LINE_V7_ZONE_START);
		assertEquals("7.0.0b", p.getContext().getVersion());
		e = p.getEvents().get(i - 1 /* no increment */);

		p.parseLogLine(LINE_V7_OPS_START);
		e = p.getEvents().get(i - 1 /* no increment */);

		p.parseLogLine(LINE_V7_DISCIPLINE_CHANGED);
		e = p.getEvents().get(i - 1 /* no increment */);
		assertEquals(CharacterDiscipline.Medicine, p.getContext().getActor("Lad Dominic", Actor.Type.SELF).getDiscipline());

		p.parseLogLine(LINE_V7_COMBAT_START);
		e = p.getEvents().get(i++);
//		assertEquals(CharacterDiscipline.Bodyguard, p.getCurrentCombat().getDiscipline());
		p.parseLogLine(LINE_V7_OTHER);
		e = p.getEvents().get(i++);
		assertEquals("Combat Droid", e.getSource().getName());
		assertEquals("Notquiteenoughviable", e.getTarget().getName());
		p.parseLogLine(LINE_V7_NPC);
		e = p.getEvents().get(i++);
		assertEquals("Combat Droid", e.getSource().getName());
		assertEquals("Notquiteenoughviable", e.getTarget().getName());
		p.parseLogLine(LINE_V7_COMBAT_EXIT);
		e = p.getEvents().get(i++);
		p.parseLogLine(LINE_V7_COMPANION);
		e = p.getEvents().get(i++);
		assertEquals("Lana Beniko", e.getSource().getName());
//		p.parseLogLine(LINE_V7_IGNORED);
		assertEquals(i, p.getEvents().size());
	}


	@Test
	public void testSelfAutoDetection() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Event e = null;

		final Object[] samplePairs = new Object[]{
				"[01:29:22.473] [@Ixale] [@Tas'ello] [Force Might {1781496599805952}] [ApplyEffect {836045448945477}: Fortification {1781496599806227}] ()", Actor.Type.SELF, // should be fixed from line 3
				"[01:29:22.473] [@Ixale] [@Daeneryys] [Force Might {1781496599805952}] [ApplyEffect {836045448945477}: Fortification {1781496599806227}] ()", Actor.Type.SELF,
				"[01:29:28.311] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", Actor.Type.SELF,
				"[01:29:28.360] [@Ixale] [@Ixale] [Sprint {810670782152704}] [RemoveEffect {836045448945478}: Sprint {810670782152704}] ()", Actor.Type.SELF,
				"[01:29:29.251] [@Dwayna] [@Ixale] [Deliverance {880116108361728}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (10792*) <4856>", Actor.Type.PLAYER,
				"[01:29:29.258] [@Ixale] [@Ixale] [Force Leap {812105301229568}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", Actor.Type.SELF,
				"[01:29:29.258] [@Ixale] [@Ixale] [] [Restore {836045448945476}: focus point {836045448938496}] (1)", Actor.Type.SELF,
		};

		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);
		}

		for (int j = 0; j < samplePairs.length; j += 2) {
			e = p.getEvents().get(i++);
			assertEquals("Line " + i + " failed", samplePairs[j + 1], e.getSource().getType());
		}

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testGuard() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Event e = null;

		final Object[] samplePairs = new Object[]{
				"[01:20:00.000] [@Ixale] [@Ixale] [] [Restore {836045448945476}: focus point {836045448938496}] (1)", 0, // nothing yet
				"[01:20:00.595] [@Tank] [@Ixale] [Guard {1775934617157632}] [ApplyEffect {836045448945477}: Guard {1775934617157632}] ()", 1, // guard up
				"[01:20:01.572] [@Ixale] [@Ixale] [] [Restore {836045448945476}: focus point {836045448938496}] (1)", 1, // still up
				"[01:20:02.095] [@Tank] [@Ixale] [Guard {1775934617157632}] [ApplyEffect {836045448945477}: Guard {1775934617157632}] ()", 2, // now in range
				"[01:20:03.572] [@Ixale] [@Ixale] [] [Restore {836045448945476}: focus point {836045448938496}] (1)", 2, // still in range
				"[01:20:04.117] [@Ixale:Doc {404577329348608}] [@Ixale:Doc {404577329348608}] [Fortification {1781488009871360}] [ApplyEffect {836045448945477}: Force Might {1781488009871651}] ()", null, // companion is not guarded
				"[01:20:04.517] [@Tank] [@Ixale] [Fortification {1781488009871360}] [ApplyEffect {836045448945477}: Force Might {1781488009871651}] ()", 2, // still guarded
				"[01:20:05.572] [@Ixale] [@Ixale] [] [Restore {836045448945476}: focus point {836045448938496}] (1)", 2, // still guarded
				"[01:20:06.203] [@Tank] [@Ixale] [Guard {1780006246154240}] [RemoveEffect {836045448945478}: Guard {1780006246154240}] ()", 1, // out of range
				"[01:20:07.572] [@Ixale] [@Ixale] [] [Restore {836045448945476}: focus point {836045448938496}] (1)", 1, // still out of range
				"[01:20:08.203] [@Tank] [@Ixale] [Guard {1780006246154240}] [RemoveEffect {836045448945478}: Guard {1780006246154240}] ()", 0, // guard down
				"[01:20:09.572] [@Ixale] [@Ixale] [] [Restore {836045448945476}: focus point {836045448938496}] (1)", 0, // no guard
		};

		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);
			e = p.getEvents().get(i++);

			assertEquals("Line " + i + " failed", samplePairs[j + 1], e.getGuardState());
		}

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testCombatCaptureLaggedDamage() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Combat c = null;

		final String[] sampleLines = new String[]{
				"[01:30:00.473] [@Ixale] [@Ixale] [Force Might {1781496599805952}] [ApplyEffect {836045448945477}: Power Surge {3244362460823552}] ()",
				"[01:30:10.311] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", // entering combat 1
				"[01:30:15.034] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236215] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (972 elemental {836045448940875}) <972>",
				"[01:30:15.382] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236215] [Strike {947362411315200}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (0 -dodge {836045448945505}) <1>",
				"[01:30:20.838] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236215] [Merciless Slash {1261715362676736}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (4223 energy {836045448940874}) <4223>",
				"[01:30:23.255] [@Ixale] [@Ixale] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", // exiting combat 1 ...

				"[01:30:23.268] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236215] [Weakening Wounds {3188029669769216}] [ApplyEffect {836045448945477}: Weakening Wound (Force) {3188029669769487}] ()",
				"[01:30:23.268] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236215] [Overload Saber {1261719657644032}] [ApplyEffect {836045448945477}: Burning (Physical) {1261719657644297}] ()",
				"[01:30:23.269] [@Ixale] [@Ixale] [Sprint {810670782152704}] [ApplyEffect {836045448945477}: Sprint {810670782152704}] ()",
				"[01:30:23.404] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236215] [Zealous Strike {996200484438016}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (1090* energy {836045448940874}) <1090>", // .. but still doing damage within DEFAULT window
				"[01:30:24.370] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236215] [Merciless Slash {1261715362676736}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (774 energy {836045448940874}) <774>",
				"[01:30:27.579] [@Ixale] [@Ixale] [Power Surge {3244362460823552}] [RemoveEffect {836045448945478}: Power Surge {3244362460823552}] ()", // this is already too late
		};

		for (i = 0; i < sampleLines.length; i++) {
			p.parseLogLine(sampleLines[i]);
		}

		assertNull(p.getCurrentCombat());
		assertEquals(1, p.getCombats().size());

		c = p.getCombats().get(0);
		assertEquals(2, c.getEventIdFrom());
		assertEquals("01:30:10.311", new Timestamp(c.getTimeFrom()).toString().split(" ")[1]);

		assertEquals(Integer.valueOf(11), c.getEventIdTo());
		assertEquals("01:30:23.255", new Timestamp(c.getTimeTo()).toString().split(" ")[1]);

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testCombatSeparateTwoDistant() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Combat c = null;

		final Object[] samplePairs = new Object[]{
				"[14:14:33.877] [@Ixayly] [@Ixayly] [Aimed Shot {1117507540746240}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", null, // no combat
				"[14:14:35.306] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1, // entering combat 1
				"[14:14:35.419] [@Ixayly] [Palace Interrogator {3295433916940288}:24640000008962] [XS Freighter Flyby {2524220999335936}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (6027* elemental {836045448940875}) <6027>", 1, // in combat 1
				"[14:14:44.554] [@Ixayly] [Palace Watchman {3295455391776768}:24640000009029] [] [Event {836045448945472}: Death {836045448945493}] ()", 1, // in combat 1
				"[14:15:28.171] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", 1, // exiting combat 1, naturally (no death or drop)

				"[14:15:28.192] [@Ixayly] [@Ixayly] [Sprint {810670782152704}] [ApplyEffect {836045448945477}: Sprint {810670782152704}] ()", 1, // out of combat, but still window open

				"[14:15:32.446] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 2, // entering combat 2 (outside DELAY window after 1 ends)
				"[14:15:33.282] [@Ixayly] [Palace Guardian {3295459686744064}:24640000009093] [Vital Shot {2115340112756736}] [ApplyEffect {836045448945477}: Bleeding (Tech) {2115340112756992}] ()", 2, // in combat 2
				"[14:15:34.366] [Palace Guardian {3295459686744064}:24640000009093] [@Ixayly] [Saber Reflect {3305905047207936}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (2830 energy {836045448940874}(reflected {836045448953649})) <2830>", 2, // in combat 2
				"[14:15:52.595] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", 2, // exiting combat 2
				"[14:15:56.783] [@Evelona] [@Ixayly] [Emergency Medpac {807518276157440}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (3464) <1558>", null, // out of combat
				"[14:15:56.996] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: LeaveCover {836045448945486}] ()", null, // out of combat
		};

		String msg;
		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);

			msg = "Line " + (i++) + " failed";
			if (samplePairs[j + 1] == null) {
				assertNull(msg, p.getCurrentCombat());
			} else {
				assertEquals(msg, samplePairs[j + 1], p.getCurrentCombat().getCombatId());
			}
		}

		// combat info
		assertNull(p.getCurrentCombat());
		assertEquals(2, p.getCombats().size());

		c = p.getCombats().get(0);
		assertEquals(2, c.getEventIdFrom());
		assertEquals(Integer.valueOf(5), c.getEventIdTo());

		c = p.getCombats().get(1);
		assertEquals(7, c.getEventIdFrom());
		assertEquals(Integer.valueOf(10), c.getEventIdTo());

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testCombatConnectTwoClose() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Combat c = null;

		final Object[] samplePairs = new Object[]{
				"[14:14:33.877] [@Ixayly] [@Ixayly] [Aimed Shot {1117507540746240}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", null, // no combat
				"[14:14:35.306] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1, // entering combat 1
				"[14:14:44.554] [@Ixayly] [Palace Watchman {3295455391776768}:24640000009029] [] [Event {836045448945472}: Death {836045448945493}] ()", 1, // in combat 1
				"[14:15:28.171] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", 1, // exiting combat 1, naturally (no death or drop)

				"[14:15:28.192] [@Ixayly] [@Ixayly] [Sprint {810670782152704}] [ApplyEffect {836045448945477}: Sprint {810670782152704}] ()", 1, // out of combat, but still window open

				"[14:15:29.446] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1, // re-entering combat 1 (within DELAY window)
				"[14:15:33.282] [@Ixayly] [Palace Guardian {3295459686744064}:24640000009093] [Vital Shot {2115340112756736}] [ApplyEffect {836045448945477}: Bleeding (Tech) {2115340112756992}] ()", 1, // in combat 2
				"[14:15:52.595] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", 1, // exiting combat 1
				"[14:15:56.996] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: LeaveCover {836045448945486}] ()", null, // out of combat
		};

		String msg;
		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);

			msg = "Line " + (i++) + " failed";
			if (samplePairs[j + 1] == null) {
				assertNull(msg, p.getCurrentCombat());
			} else {
				assertEquals(msg, samplePairs[j + 1], p.getCurrentCombat().getCombatId());
			}
		}

		// combat info
		assertNull(p.getCurrentCombat());
		assertEquals(1, p.getCombats().size());

		c = p.getCombats().get(0);
		assertEquals(2, c.getEventIdFrom());
		assertEquals("14:14:35.306", new Timestamp(c.getTimeFrom()).toString().split(" ")[1]);
		assertEquals(Integer.valueOf(8), c.getEventIdTo());
		assertEquals("14:15:52.595", new Timestamp(c.getTimeTo()).toString().split(" ")[1]);

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testCombatSeparateAfterDeath() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Combat c = null;

		final Object[] samplePairs = new Object[]{
				"[01:10:34.526] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1,
				"[01:17:57.400] [] [@Ixale] [] [Event {836045448945472}: Death {836045448945493}] ()", 1,

				"[01:18:08.164] [@Ixale] [@Ixale] [Dreadful Guard {3073061985189888}] [ApplyEffect {836045448945477}: Dreadful Guard {3073061985190144}] ()", 1,
				"[01:18:08.167] [@Ixale] [@Ixale] [Safe Login {973870949466112}] [ApplyEffect {836045448945477}: Safe Login Immunity {973870949466372}] ()", 1,
				"[01:19:24.604] [@Ixale] [@Ixale] [Force Might {1781496599805952}] [ApplyEffect {836045448945477}: Force Might {1781496599805952}] ()", null, // more than REVIVE window

				"[01:20:07.841] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 2,
				"[01:20:07.856] [@Ixale] [@Ixale] [Sprint {810670782152704}] [RemoveEffect {836045448945478}: Sprint {810670782152704}] ()", 2,
				"[01:20:09.937] [@Ixale] [@Ixale] [Force Leap {812105301229568}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 2,
				"[01:20:09.937] [@Ixale] [@Ixale] [] [Restore {836045448945476}: focus point {836045448938496}] (1)", 2, // still within current combat scope
		};

		String msg;
		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);

			msg = "Line " + (i++) + " failed";
			if (samplePairs[j + 1] == null) {
				assertNull(msg, p.getCurrentCombat());
			} else {
				assertEquals(msg, samplePairs[j + 1], p.getCurrentCombat().getCombatId());
			}
		}

		// combat info
		assertNotNull(p.getCurrentCombat()); // still running
		assertEquals(1, p.getCombats().size());

		c = p.getCombats().get(0);
		assertEquals(1, c.getEventIdFrom());
		assertEquals("01:10:34.526", new Timestamp(c.getTimeFrom()).toString().split(" ")[1]);
		assertEquals(Integer.valueOf(2), c.getEventIdTo());
		assertEquals("01:17:57.4", new Timestamp(c.getTimeTo()).toString().split(" ")[1]);

		c = p.getCurrentCombat();
		assertEquals(6, c.getEventIdFrom());
		assertEquals("01:20:07.841", new Timestamp(c.getTimeFrom()).toString().split(" ")[1]);
		assertNull(c.getEventIdTo());
		assertNull(c.getTimeTo());

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testCombatConnectAfterDeathAndRevive() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Combat c = null;

		final Object[] samplePairs = new Object[]{
				"[23:54:34.526] [@Ixayle] [@Ixayle] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1,
				"[23:54:38.472] [Dread Master Calphayus {3273946195558400}:4205003959689] [@Ixayle] [Strike {3297478321373184}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (8669 energy {836045448940874}) <8669>", 1,
				"[23:54:38.472] [Dread Master Calphayus {3273946195558400}:4205003959689] [@Ixayle] [] [Event {836045448945472}: Death {836045448945493}] ()", 1,

				"[23:54:39.726] [@Ixayle] [Dread Master Calphayus {3273946195558400}:4205003959689] [Mind Control {979772234530816}] [ApplyEffect {836045448945477}: Taunt {979772234531083}] ()", 1,

				"[23:55:16.455] [Dread Master Calphayus {3273946195558400}:4205003959689] [@Ixayle] [Strike {3297478321373184}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (8669 energy {836045448940874}) <8669>", 1,
				"[23:55:16.765] [@Ketans] [@Ixayle] [Heartrigger Patch {807217628446720}] [ApplyEffect {836045448945477}: Lucky Break {807217628447020}] ()", 1,
				"[23:55:17.657] [@Ixayle] [@Ixayle] [] [Event {836045448945472}: Revived {836045448945494}] ()", 1, // revived within REVIVE window (lagged after damage event)
				"[23:55:24.657] [@Ixayle] [@Ixayle] [] [Restore {836045448945476}: Force {836045448938502}] (25)", 1,
				"[23:55:37.167] [@Ixayle] [@Ixayle] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1, // entered within ENTER window after revive
				"[23:55:45.173] [Dread Master Calphayus {3273946195558400}:4205003959689] [@Ixayle] [Distorted Perceptions {3303504160489472}] [ApplyEffect {836045448945477}: Slowed {3303504160489739}] ()", 1,
		};

		String msg;
		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);

			msg = "Line " + (i++) + " failed";
			if (samplePairs[j + 1] == null) {
				assertNull(msg, p.getCurrentCombat());
			} else {
				assertEquals(msg, samplePairs[j + 1], p.getCurrentCombat().getCombatId());
			}
		}

		// combat info
		assertNotNull(p.getCurrentCombat()); // still running
		assertEquals(0, p.getCombats().size()); // no closed yet

		c = p.getCurrentCombat();
		assertEquals(1, c.getEventIdFrom());
		assertEquals("23:54:34.526", new Timestamp(c.getTimeFrom()).toString().split(" ")[1]);
		assertNull(c.getEventIdTo());
		assertNull(c.getTimeTo());
		// XXX assertEquals("Dread Master Calphayus (HM 8m)", c.getBoss().toString());
		// XXX assertEquals("Dread Palace", c.getBoss().getRaid().getName());

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testCombatConnectAfterCombatDrop() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Combat c = null;

		final Object[] samplePairs = new Object[]{
				"[18:54:46.377] [@Ixayle] [@Ixayle] [Force Potency {812745251356672}] [ApplyEffect {836045448945477}: Force Potency {812745251356672}] ()", null,
				"[18:56:55.904] [@Ixayle] [@Ixayle] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1,
				"[18:56:58.685] [Trandoshan Trenchcutter {2876507101855744}:33375000844641] [@Ixayle] [Melee Attack {2876975253291008}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (883 kinetic {836045448940873}) <883>", 1,
				"[18:56:58.704] [@Ixayle] [@Ixayle] [Particle Acceleration {981172393869312}] [ApplyEffect {836045448945477}: Particle Acceleration {981172393869312}] ()", 1,
				"[18:56:59.021] [@Ixayle] [@Ixayle] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", 1,

				"[18:56:59.040] [@Ixayle] [@Ixayle] [Force Cloak {2271612497821696}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 1,
				"[18:56:59.040] [@Ixayle] [@Ixayle] [Stealth {812852625539072}] [ApplyEffect {836045448945477}: Stealth {812852625539072}] ()", 1,
				"[18:56:59.042] [@Ixayle] [@Ixayle] [Stealth {812852625539072}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 1,
				"[18:56:59.043] [@Ixayle] [@Ixayle] [Sprint {810670782152704}] [ApplyEffect {836045448945477}: Sprint {810670782152704}] ()", 1,
				"[18:56:59.043] [@Ixayle] [@Ixayle] [Force Cloak {2271612497821696}] [ApplyEffect {836045448945477}: Force Cloak {2271612497821696}] ()", 1,

				"[18:56:59.092] [Trandoshan Trenchcutter {2876507101855744}:33375000844711] [@Ixayle] [Melee Attack {2876975253291008}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (0 -parry {836045448945503}) <1>", 1,
				"[18:56:59.184] [Trandoshan Trenchcutter {2876507101855744}:33375000844641] [@Ixayle] [Melee Attack {2876975253291008}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (883 kinetic {836045448940873}) <883>", 1,
				"[18:56:59.551] [@Ixayle] [@Ixayle] [Force Cloak {2271612497821696}] [RemoveEffect {836045448945478}: Force Cloak {2271612497821696}] ()", 1,
				"[18:56:59.552] [@Ixayle] [@Ixayle] [Stealth {812852625539072}] [Event {836045448945472}: AbilityDeactivate {836045448945480}] ()", 1,
				"[18:56:59.553] [@Ixayle] [@Ixayle] [Stealth {812852625539072}] [ApplyEffect {836045448945477}: Shadow Protection {812852625540068}] ()", 1,
				"[18:56:59.554] [@Ixayle] [@Ixayle] [Stealth {812852625539072}] [RemoveEffect {836045448945478}: Stealth {812852625539072}] ()", 1,
				"[18:56:59.554] [@Ixayle] [@Ixayle] [Whirling Blow {979716399955968}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 1,
				"[18:56:59.554] [@Ixayle] [@Ixayle] [] [Spend {836045448945473}: Force {836045448938502}] (40)", 1,
				"[18:56:59.619] [@Nuiri] [@Ixayle] [Healing Resonance {3234887762968576}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (169) <84>", 1,
				"[18:56:59.835] [Trandoshan Trenchcutter {2876507101855744}:33375000844711] [@Ixayle] [Melee Attack {2876975253291008}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (377 kinetic {836045448940873} -shield {836045448945509}) <377>", 1,
				"[18:56:59.904] [@Ixayle] [@Ixayle] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1,
				"[18:56:59.908] [@Ixayle] [@Ixayle] [Sprint {810670782152704}] [RemoveEffect {836045448945478}: Sprint {810670782152704}] ()", 1,
		};

		String msg;
		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);

			msg = "Line " + (i++) + " failed";
			if (samplePairs[j + 1] == null) {
				assertNull(msg, p.getCurrentCombat());
			} else {
				assertEquals(msg, samplePairs[j + 1], p.getCurrentCombat().getCombatId());
			}
		}

		// combat info
		assertNotNull(p.getCurrentCombat()); // still running
		assertEquals(0, p.getCombats().size()); // no closed yet

		c = p.getCurrentCombat();
		assertEquals(2, c.getEventIdFrom());
		assertEquals("18:56:55.904", new Timestamp(c.getTimeFrom()).toString().split(" ")[1]);
		assertNull(c.getEventIdTo());
		assertNull(c.getTimeTo());

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	/*
		@Test
		public void testCombatConnectAfterReviveOutsideWindowButWithKnownTarget() throws Exception {
			final Parser p = createParser();
			int i = 0;
			Combat c = null;

			final Object[] samplePairs = new Object[] {
				// enter combat with Raptus, instance ID 16094005392344
				"[02:29:26.006] [@Ixayle] [@Ixayle] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1,
				"[02:34:07.485] [@Ixayle] [Dread Master Raptus {3302902865068032}:16094005392344] [Force Breach {964675424485376}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (1534* internal {836045448940876}) <4604>", 1,
				"[02:34:09.403] [Dread Master Raptus {3302902865068032}:16094005392344] [@Ixayle] [Rising Slash {3299355222081536}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (33479 energy {836045448940874}) <33479>", 1,
				"[02:34:11.727] [@Ixayle] [@Ixayle] [Force Valor {875503313485824}] [RemoveEffect {836045448945478}: Fortification {875503313486158}] ()", 1,
				"[02:34:11.739] [] [@Ixayle] [] [Event {836045448945472}: Death {836045448945493}] ()", 1,

				"[02:34:37.638] [@Ixayle] [Dread Master Raptus {3302902865068032}:16094005392344] [Force Breach {964675424485376}] [RemoveEffect {836045448945478}: Accuracy Reduced {964675424485667}] ()", 1,

				// revived OUTSIDE the 40s window
				"[02:35:04.850] [@Ixayle] [@Ixayle] [ {3298019487252480}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 1,
				"[02:35:04.852] [@Ixayle] [@Ixayle] [] [Event {836045448945472}: Revived {836045448945494}] ()", 1,
				"[02:35:04.852] [@Ixayle] [@Ixayle] [] [Restore {836045448945476}: Force {836045448938502}] (27)", 1,
				"[02:35:05.140] [@Ixayle] [@Ixayle] [Meditation {812792495996928}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 1,
				"[02:35:05.140] [@Ixayle] [@Ixayle] [Meditation {812792495996928}] [ApplyEffect {836045448945477}: Meditation {812792495996928}] ()", 1,

				"[02:35:14.008] [@Ixayle] [@Ixayle] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1,
				"[02:35:14.022] [@Ixayle] [@Ixayle] [Sprint {810670782152704}] [RemoveEffect {836045448945478}: Sprint {810670782152704}] ()", 1,
				"[02:35:14.024] [@Ixayle] [@Ixayle] [Focused Defense {3302104001150976}] [ApplyEffect {836045448945477}: Focused Defense {3302104001150976}] ()", 1,
				"[02:35:14.026] [Dread Master Raptus {3302902865068032}:16094005392344] [@Ixayle] [Force Wave {3300643712270336}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (2820 energy {836045448940874} -shield {836045448945509}) <2820>", 1,
				"[02:35:14.186] [@Dway'na] [@Ixayle] [Slow-release Medpac {1143320294195200}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (1597) <718>", 1,
				};

			String msg;
			for (int j = 0; j < samplePairs.length; j += 2) {
				p.parseLogLine((String) samplePairs[j]);

				msg = "Line "+(i++)+" failed";
				if (samplePairs[j + 1] == null) {
					assertNull(msg, p.getCurrentCombat());
				} else {
					assertNotNull(msg, p.getCurrentCombat());
					assertEquals(msg, samplePairs[j + 1], p.getCurrentCombat().getCombatId());
				}
			}

			// combat info
			assertNotNull(p.getCurrentCombat()); // still running
			assertEquals(0, p.getCombats().size()); // no closed yet

			c = p.getCurrentCombat();
			assertEquals(1, c.getEventIdFrom());
			assertEquals("02:29:26.006", new Timestamp(c.getTimeFrom()).toString().split(" ")[1]);
			assertNull(c.getEventIdTo());
			assertNull(c.getTimeTo());

			// sanity
			assertEquals(i, p.getEvents().size());
		}
	*/
	@Test
	public void testCombatDeathWithDotsTicking() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Combat c = null;

		final Object[] samplePairs = new Object[]{
				"[14:14:35.306] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1, // entering combat 1
				"[15:17:09.800] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [Vital Shot {2115340112756736}] [ApplyEffect {836045448945477}: Bleeding (Tech) {2115340112756992}] ()", 1,
				"[15:18:56.602] [Dread Master Raptus {3273993440198656}:24640000594077] [@Ixayly] [] [Event {836045448945472}: Death {836045448945493}] ()", 1,
				"[15:18:57.875] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [ {3301111863705600}] [Event {836045448945472}: ModifyThreat {836045448945483}] () <0>", 1,
				"[15:18:57.875] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [Bleeding (Tech) {2115340112756992}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (723 internal {836045448940876})", 1,
				"[15:19:00.894] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [ {3301111863705600}] [Event {836045448945472}: ModifyThreat {836045448945483}] () <0>", 1,
				"[15:19:00.895] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [Bleeding (Tech) {2115340112756992}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (723 internal {836045448940876})", 1,
				"[15:19:03.790] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [ {3301111863705600}] [Event {836045448945472}: ModifyThreat {836045448945483}] () <0>", 1,
				"[15:19:03.791] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [Bleeding (Tech) {2115340112756992}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (723 internal {836045448940876})", 1,
				"[15:19:06.793] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [ {3301111863705600}] [Event {836045448945472}: ModifyThreat {836045448945483}] () <0>", 1,
				"[15:19:06.794] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [Bleeding (Tech) {2115340112756992}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (723 internal {836045448940876})", 1,
				"[15:19:09.800] [@Ixayly] [Dread Master Raptus {3273993440198656}:24640000594077] [Vital Shot {2115340112756736}] [RemoveEffect {836045448945478}: Bleeding (Tech) {2115340112756992}] ()", 1,

		};

		String msg;
		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);

			msg = "Line " + (i++) + " failed";
			if (samplePairs[j + 1] == null) {
				assertNull(msg, p.getCurrentCombat());
			} else {
				assertEquals(msg, samplePairs[j + 1], p.getCurrentCombat().getCombatId());
			}
		}

		// combat info
		assertNotNull(p.getCurrentCombat()); // still running
		assertEquals(0, p.getCombats().size()); // no closed yet

		c = p.getCurrentCombat();
		assertEquals(Integer.valueOf(11), c.getEventIdTo()); // last damage
		assertEquals("15:18:56.602", new Timestamp(c.getTimeTo()).toString().split(" ")[1]);
		// XXX assertEquals("Dread Council (HM 8m)", c.getBoss().toString());
		assertEquals("Dread Palace", c.getBoss().getRaid().getName());

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testCombatOverlappingWithoutExitEvent() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Combat c = null;

		final Object[] samplePairs = new Object[]{
				"[03:40:38.834] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1,
				"[03:40:40.793] [Dread Corruption Probe {3267130082459648}:13151059948556] [@Ixaar] [Poisoned (Physical) {2288062222565633}] [ApplyEffect {836045448945477}: Poisoned (Physical) {2288062222565633}] ()", 1,
				"[03:40:40.988] [@Ixaar] [@Ixaar] [Focused Defense {3303151973171200}] [ApplyEffect {836045448945477}: Focused Defense {3303151973171200}] ()", 1,
				"[03:40:43.794] [Dread Corruption Probe {3267130082459648}:13151059898905] [@Ixaar] [Poisoned (Physical) {2288062222565633}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (164 internal {836045448940876}) <164>", 1,
				"[03:40:46.793] [] [@Ixaar] [Poisoned (Physical) {2288062222565633}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (164 internal {836045448940876}) <164>", 1,

				"[03:42:48.702] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1, // straight into new one without EXIT (even outside DEFAULT window)
				"[03:42:48.703] [@Ixaar] [@Ixaar] [Sprint {810670782152704}] [ApplyEffect {836045448945477}: Sprint {810670782152704}] ()", 1,
				"[03:42:50.913] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", 1,

				"[03:42:50.916] [@Ixaar] [@Ixaar] [Sprint {810670782152704}] [ApplyEffect {836045448945477}: Sprint {810670782152704}] ()", 1,

				"[03:42:51.720] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1, // .. and again within DEFAULT
				"[03:42:51.722] [@Ixaar] [@Ixaar] [Sprint {810670782152704}] [RemoveEffect {836045448945478}: Sprint {810670782152704}] ()", 1,
				"[03:42:52.839] [] [@Ixaar] [Toxic Dart {2288062222565376}] [RemoveEffect {836045448945478}: Poisoned (Physical) {2288062222565633}] ()", 1,
				"[03:42:53.937] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", 1,

				"[03:42:53.940] [@Ixaar] [@Ixaar] [Sprint {810670782152704}] [ApplyEffect {836045448945477}: Sprint {810670782152704}] ()", 1,

				"[03:42:55.893] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1, // ... and again
				"[03:42:55.896] [@Ixaar] [@Ixaar] [Sprint {810670782152704}] [RemoveEffect {836045448945478}: Sprint {810670782152704}] ()", 1,
				"[03:43:06.978] [Dread Corruption Probe {3267130082459648}:13151059948556] [@Ixaar] [Poisoned (Physical) {2288062222565633}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (164 internal {836045448940876}) <164>", 1,
				"[03:43:09.759] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", 1,
				"[03:43:09.761] [@Ixaar] [@Ixaar] [Sprint {810670782152704}] [ApplyEffect {836045448945477}: Sprint {810670782152704}] ()", 1,

				"[03:43:09.953] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", 1, // .. and again ...
				"[03:43:09.955] [@Ixaar] [@Ixaar] [Sprint {810670782152704}] [RemoveEffect {836045448945478}: Sprint {810670782152704}] ()", 1,
				"[03:43:11.872] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()", 1,
		};

		String msg;
		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);

			msg = "Line " + (i++) + " failed";
			if (samplePairs[j + 1] == null) {
				assertNull(msg, p.getCurrentCombat());
			} else {
				assertNotNull(msg, p.getCurrentCombat());
				assertEquals(msg, samplePairs[j + 1], p.getCurrentCombat().getCombatId());
			}
		}

		// combat info
		assertNotNull(p.getCurrentCombat());
		assertEquals(0, p.getCombats().size());

		c = p.getCurrentCombat();
		assertEquals("03:40:38.834", new Timestamp(c.getTimeFrom()).toString().split(" ")[1]);
		assertEquals("03:43:11.872", new Timestamp(c.getTimeTo()).toString().split(" ")[1]);

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testEffectiveHealsOfHealer() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Event e = null;

		final Object[] samplePairs = new Object[]{
				"[22:21:34.526] [@Ixayla] [@Ixayla] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", null,
				"[22:21:57.281] [@Ixayla] [@Ixayla] [Hammer Shot {801299163512832}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (440) <134>", 134 / 0.45,
				"[22:22:00.850] [@Ixayla] [@Imminence] [Advanced Medical Probe {997175442014208}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (4174)", null,
				"[22:24:09.211] [@Ixayla] [@Ixayla] [Adrenaline Rush {801251918872576}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (612) <306>", 306 / 0.5, // not reduced
				"[22:22:02.239] [@Imminence] [@Ixayla] [Guard {1780006246154240}] [ApplyEffect {836045448945477}: Guard {1780006246154240}] ()", null,
				"[22:22:03.850] [@Ixayla] [@Imminence] [Advanced Medical Probe {997175442014208}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (4174)", null,
				"[22:23:13.496] [@Ixayla] [@Miranda] [Kolto Bomb {3169148993536000}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (1025*) <333>", 333 / 0.5 / (0.75 - 0.1), // guarded now
				"[22:23:13.496] [@Ixayla] [@Ixayla] [ImpeccableMedpacc {836045448945499}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (2667) <1000>", 1000 / 0.5 / (0.75), // not reduced
				"[22:28:26.544] [@Ixayla] [@Laura'sun] [Advanced Medical Probe {997175442014208}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (4393) <1427>", 4393.0, // 1427 / 0.5 / (0.75 - 0.1),
				"[22:28:26.544] [@Laura'sun] [@Ixayla] [Guard {1780006246154240}] [RemoveEffect {836045448945478}: Guard {1780006246154240}] ()", null,
				"[22:28:28.544] [@Ixayla] [@Ixayla] [Merciless Zeal {1262192104046592}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (312) <156>", 156 / 0.5, // no guard, not reduced
				"[22:28:28.544] [@Ixayla] [@Ixayla] [Merciless Zeal {1262192104046592}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (312) <156>", 156 / 0.5,
		};

		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);
			e = p.getEvents().get(i++);

			assertEquals("Line " + i + " failed", samplePairs[j + 1] == null
							? null
							: Integer.valueOf((int) (Math.ceil((double) samplePairs[j + 1])))
					, e.getEffectiveHeal());
		}

		// sanity
		assertEquals(i, p.getEvents().size());
	}


	@Test
	public void testEffectiveHealsOfHealerWithGuardBug() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Event e = null;

		final Object[] samplePairs = new Object[]{
				"[01:55:34.526] [@Ixayla] [@Ixayla] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", null,
				"[01:55:48.143] [@Dúbyah] [@Ixayla] [Guard {1780044900859904}] [ApplyEffect {836045448945477}: Guard {1780044900859904}] ()", null,
				"[01:55:48.143] [@Dúbyah] [@Ixayla] [Guard {1780044900859904}] [ApplyEffect {836045448945477}: Guard {1780044900859904}] ()", null,
				"[01:56:01.343] [@Ixayla] [@Ixayla] [Hammer Shot {801299163512832}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", null,
				"[01:56:01.440] [@Ixayla] [@Grippy] [Hammer Shot {801299163512832}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (527) <38>", 38 / 0.5 / (0.75 - 0.1),
				"[01:56:01.666] [@Dúbyah] [@Ixayla] [Guard {1780044900859904}] [RemoveEffect {836045448945478}: Guard {1780044900859904}] ()", null,
				"[01:56:01.923] [@Ixayla] [@Stofil] [Kolto Bomb {3169148993536000}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (452) <203>", 203 / 0.45, // guard removal delayed
				"[01:56:01.923] [@Ixayla] [@Thewicked'lv] [Kolto Bomb {3169148993536000}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (465) <209>", 209 / 0.45,
				"[01:56:01.923] [@Ixayla] [@Cryptica] [Kolto Bomb {3169148993536000}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (452) <203>", 203 / 0.45,
				"[01:56:01.924] [@Ixayla] [@Ixayla] [Kolto Bomb {3169148993536000}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (859*) <386>", 386 / 0.45 + 1,
				"[01:56:01.924] [@Ixayla] [@Evolixe] [Kolto Bomb {3169148993536000}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (452) <203>", 203 / 0.45,
				"[01:56:01.955] [@Ixayla] [@Grippy] [Hammer Shot {801299163512832}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (1001*) <450>", 450 / 0.45 + 1,
				"[01:56:02.343] [@Ixayla] [@Grippy] [Hammer Shot {801299163512832}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (487) <219>", 219 / 0.45,
				"[01:56:02.850] [@Ixayla] [@Grippy] [Hammer Shot {801299163512832}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (924*) <67>", 67 / 0.45,
				"[01:56:03.146] [@Dúbyah] [@Ixayla] [Guard {1780044900859904}] [RemoveEffect {836045448945478}: Guard {1780044900859904}] ()", null, // now its removed in the log
				"[01:56:03.353] [@Ixayla] [@Grippy] [Hammer Shot {801299163512832}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (924*)", null,
				"[01:56:03.750] [@Ixayla] [@Grippy] [Hammer Shot {801299163512832}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (924*)", null,
				"[01:56:04.290] [@Ixayla] [@Irisa] [Trauma Probe {999516199190528}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (2301*) <1035>", 1035 / 0.45 + 1,
				"[01:56:04.292] [@Ixayla] [@Grippy] [Hammer Shot {801299163512832}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (924*)", null,
		};

		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);
			e = p.getEvents().get(i++);

			assertEquals("Line " + i + " failed", samplePairs[j + 1] == null
							? null
							: Integer.valueOf((int) (Math.ceil((double) samplePairs[j + 1])))
					, e.getEffectiveHeal());
		}

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testEffectiveHealsOfTank() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Event e = null;

		final Object[] samplePairs = new Object[]{
				"[17:20:28.311] [@Ixaar] [@Ixaar] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", null,
				"[17:20:03.880] [@Ixaar] [@Ixaar] [Enure {2211126473392128}] [ApplyEffect {836045448945477}: Enure {2211126473392128}] ()", null,
				"[17:20:21.315] [@Ixaar] [@Ixaar] [Impeccable Medpac {3149821640704000}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (6799) <6799>", 6799.0,

				// and now trauma ...
				"[17:23:16.518] [@Ixaar] [@Ixaar] [] [Restore {836045448945476}: focus point {836045448938496}] (2)", null,
				"[17:23:19.836] [@Ixaar] [@Ixaar] [Trauma (PVP) {632919265640448}] [ApplyEffect {836045448945477}: Trauma (PVP) {632919265640448}] ()", null,
				"[17:23:30.368] [@Viika] [@Ixaar] [Snipe {814892735004672}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (0 -shield {836045448945509}) <1148>", null,
				"[17:23:34.203] [@Ixaar] [@Ixaar] [Enure {2211126473392128}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (10942)", null, // not counted as heal
				"[17:23:34.442] [@Viika] [@Ixaar] [Snipe {814892735004672}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (3164* energy {836045448940874} (1192 absorbed {836045448945511})) <3164>", null,
				"[17:23:53.880] [@Ixaar] [@Ixaar] [] [Spend {836045448945473}: health point {836045448938504}] (12360)", null, // not counted as "reversed heal"
				"[17:23:53.880] [@Ixaar] [@Ixaar] [Enure {2211126473392128}] [RemoveEffect {836045448945478}: Enure {2211126473392128}] ()", null,

				"[17:23:55.127] [@Ixaar] [@Ixaar] [ {2793704427356160}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (843)", null, // not counted as heal
				"[17:23:55.127] [@Ixaar] [@Viika] [ {2793704427356160}] [Event {836045448945472}: ModifyThreat {836045448945483}] () <-8298>", null,

				"[17:23:55.312] [@Ixaar] [@Ixaar] [Impeccable Medpac {3149821640704000}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (4502) <2701>", 2701.0,
		};

		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);
			e = p.getEvents().get(i++);

			assertEquals("Line " + i + " failed", samplePairs[j + 1] == null
							? null
							: Integer.valueOf((int) (Math.ceil((double) samplePairs[j + 1])))
					, e.getEffectiveHeal());
		}
		assertTrue(p.getCurrentCombat().isPvp());

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testEffectiveThreat() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Event e = null;

		final Object[] samplePairs = new Object[]{
				"[01:29:28.311] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()", null,
				"[01:29:29.260] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (490 elemental {836045448940875}) <490>", 490,
				"[01:29:29.353] [Enthralled Drouk {3296207011053568}:21874004236215] [@Ixale] [Smash {3298921430384640}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (5877 kinetic {836045448940873}) <5877>", null,
				"[01:29:29.929] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Force Leap {812105301229568}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (1252 energy {836045448940874}) <1252>", 1252,
				"[01:29:29.954] [Enthralled Drouk {3296207011053568}:21874004236133] [@Ixale] [Smash {3298921430384640}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (5681 kinetic {836045448940873}) <5681>", null,
				"[01:29:30.841] [@Ixale] [@Ixale] [Merciless Zeal {1262192104046592}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (390) <195>", 195,
				"[01:29:30.841] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (2009* elemental {836045448940875}) <2009>", 2009,
				"[01:29:32.362] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (1471 elemental {836045448940875}) <1471>", 1471,
				"[01:29:32.427] [Enthralled Drouk {3296207011053568}:21874004236133] [@Ixale] [Melee Attack {3298728156856320}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (5921 kinetic {836045448940873}) <5921>", null,
				"[01:29:33.049] [@Ixale] [@Ixale] [Force Camouflage {812096711294976}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", null,
				"[01:29:33.049] [@Ixale] [@Ixale] [Force Camouflage {812096711294976}] [ApplyEffect {836045448945477}: Force Camouflage {812096711294976}] ()", null,
				"[01:29:33.050] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Force Camouflage {812096711294976}] [Event {836045448945472}: ModifyThreat {836045448945483}] () <-295881>", -5417, // sum of previous
				"[01:29:33.050] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236215] [Force Camouflage {812096711294976}] [Event {836045448945472}: ModifyThreat {836045448945483}] () <-2668>", 0, // nothing to dump
				"[01:29:33.183] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Merciless Slash {1261715362676736}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (4195 energy {836045448940874}) <4195>", 4195,
				"[01:29:33.265] [@Ixale] [@Ixale] [Guarded by the Force {2528571801206784}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", null,
				"[01:29:33.265] [@Ixale] [@Ixale] [Guarded by the Force {2528571801206784}] [ApplyEffect {836045448945477}: Guarded by the Force {2528571801206784}] ()", null,
				"[01:29:33.373] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Zealous Strike {996200484438016}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (638 energy {836045448940874}) <638>", 638,
		};

		for (int j = 0; j < samplePairs.length; j += 2) {
			p.parseLogLine((String) samplePairs[j]);
			e = p.getEvents().get(i++);

			assertEquals("Line " + i + " failed", samplePairs[j + 1] == null
							? null
							: Long.valueOf(String.valueOf(samplePairs[j + 1]))
					, e.getEffectiveThreat());
		}

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testAbandonedEffects() throws Exception {
		final Parser p = createParser();
		int i = 0;
		Effect effect = null;

		final Object[] samplePairs = new Object[]{
				"[21:30:13.389] [@Ixayly] [@Ixayly] [] [Spend {836045448945473}: energy {836045448938503}] (10)", 0, 0,
				"[21:30:14.981] [@Ixayly] [@Ixayly] [Hightail It {3204719912681472}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 0, 0,
				"[21:30:14.981] [@Ixayly] [@Ixayly] [Hightail It {3204719912681472}] [ApplyEffect {836045448945477}: Hightail It {3204719912681472}] ()", 1, 0,
				"[21:30:15.714] [@Ixayly] [@Ixayly] [Crouch {807028649885696}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 1, 0,
				"[21:37:48.729] [@Ixayly] [@Ixayly] [Hightail It {3204719912681472}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 1, 0,
				"[21:37:48.729] [@Ixayly] [@Ixayly] [Hightail It {3204719912681472}] [ApplyEffect {836045448945477}: Hightail It {3204719912681472}] ()", 2, 0,
				"[21:37:49.453] [@Ixayly] [@Ixayly] [Crouch {807028649885696}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()", 2, 0,
				"[21:37:49.819] [@Ixayly] [@Ixayly] [Hightail It {3204719912681472}] [RemoveEffect {836045448945478}: Hightail It {3204719912681472}] ()", 0, 2,
		};

		for (int j = 0; j < samplePairs.length; j += 3) {
			p.parseLogLine((String) samplePairs[j]);
			i++;

			assertEquals("Line " + i + " failed", samplePairs[j + 1], p.getCurrentEffects().size());
			assertEquals("Line " + i + " failed", samplePairs[j + 2], p.getEffects().size());
		}

		effect = p.getEffects().get(0);
		assertEquals(3, effect.getEventIdFrom());
		assertNull(effect.getEventIdTo());
		assertTrue(effect.isActivated());

		effect = p.getEffects().get(1);
		assertEquals(6, effect.getEventIdFrom());
		assertEquals(8, (int) effect.getEventIdTo());
		assertTrue(effect.isActivated());

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testMidnightAndDaylightSaving() throws Exception {
		final Parser p = createParser();
		int i = 0;

		p.setCombatLogFile(new File("combat_2000-01-01_08_00_46_789000.txt"));
		p.getContext().reset();

		final String[] regularMidnight = new String[]{
				"[08:29:28.311] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()",
				"[08:29:29.260] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (490 elemental {836045448940875}) <490>",
				"[08:29:29.353] [Enthralled Drouk {3296207011053568}:21874004236215] [@Ixale] [Smash {3298921430384640}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (5877 kinetic {836045448940873}) <5877>",
				"[08:29:29.929] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Force Leap {812105301229568}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (1252 energy {836045448940874}) <1252>",
				"[08:29:29.954] [Enthralled Drouk {3296207011053568}:21874004236133] [@Ixale] [Smash {3298921430384640}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (5681 kinetic {836045448940873}) <5681>",
				"[01:29:30.841] [@Ixale] [@Ixale] [Merciless Zeal {1262192104046592}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (390) <195>",
				"[01:29:30.841] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (2009* elemental {836045448940875}) <2009>",
				"[01:29:32.362] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (1471 elemental {836045448940875}) <1471>",
				"[01:29:32.427] [Enthralled Drouk {3296207011053568}:21874004236133] [@Ixale] [Melee Attack {3298728156856320}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (5921 kinetic {836045448940873}) <5921>",
		};


		for (i = 0; i < regularMidnight.length; i++) {
			p.parseLogLine(regularMidnight[i]);
		}
		assertEquals("2000-01-01", new Timestamp(p.getEvents().get(0).getTimestamp()).toString().split(" ")[0]);
		assertEquals("2000-01-02", new Timestamp(p.getEvents().get(i - 1).getTimestamp()).toString().split(" ")[0]);

		// sanity
		assertEquals(i, p.getEvents().size());

		p.setCombatLogFile(new File("combat_2000-01-01_09_00_46_789000.txt"));
		p.getContext().reset();

		final String[] daylight = new String[]{
				"[08:29:28.311] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()",
				"[08:29:29.260] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (490 elemental {836045448940875}) <490>",
				"[09:29:30.841] [@Ixale] [@Ixale] [Merciless Zeal {1262192104046592}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (390) <195>",
				"[09:29:30.841] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (2009* elemental {836045448940875}) <2009>",
				"[08:29:33.049] [@Ixale] [@Ixale] [Force Camouflage {812096711294976}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()",
				"[08:29:33.049] [@Ixale] [@Ixale] [Force Camouflage {812096711294976}] [ApplyEffect {836045448945477}: Force Camouflage {812096711294976}] ()",
		};


		for (i = 0; i < daylight.length; i++) {
			p.parseLogLine(daylight[i]);
		}
		assertEquals("2000-01-01", new Timestamp(p.getEvents().get(0).getTimestamp()).toString().split(" ")[0]);
		assertEquals("2000-01-01", new Timestamp(p.getEvents().get(2).getTimestamp()).toString().split(" ")[0]);
		assertEquals("2000-01-01", new Timestamp(p.getEvents().get(3).getTimestamp()).toString().split(" ")[0]);
		assertEquals("2000-01-01", new Timestamp(p.getEvents().get(i - 1).getTimestamp()).toString().split(" ")[0]);

		// sanity
		assertEquals(i, p.getEvents().size());

		p.setCombatLogFile(new File("combat_2000-01-01_09_00_46_789000.txt"));
		p.getContext().reset();

		final String[] daylightAndMidnight = new String[]{
				"[08:29:28.311] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()",
				"[08:29:29.260] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (490 elemental {836045448940875}) <490>",
				"[09:29:30.841] [@Ixale] [@Ixale] [Merciless Zeal {1262192104046592}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (390) <195>",
				"[09:29:30.841] [@Ixale] [Enthralled Drouk {3296207011053568}:21874004236133] [Burning (Physical) {1261719657644297}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (2009* elemental {836045448940875}) <2009>",
				"[08:29:33.049] [@Ixale] [@Ixale] [Force Camouflage {812096711294976}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()",
				"[01:29:33.049] [@Ixale] [@Ixale] [Force Camouflage {812096711294976}] [ApplyEffect {836045448945477}: Force Camouflage {812096711294976}] ()",
		};


		for (i = 0; i < daylightAndMidnight.length; i++) {
			p.parseLogLine(daylightAndMidnight[i]);
		}
		assertEquals("2000-01-01", new Timestamp(p.getEvents().get(0).getTimestamp()).toString().split(" ")[0]);
		assertEquals("2000-01-01", new Timestamp(p.getEvents().get(2).getTimestamp()).toString().split(" ")[0]);
		assertEquals("2000-01-01", new Timestamp(p.getEvents().get(3).getTimestamp()).toString().split(" ")[0]);
		assertEquals("2000-01-02", new Timestamp(p.getEvents().get(i - 1).getTimestamp()).toString().split(" ")[0]);

		// sanity
		assertEquals(i, p.getEvents().size());
	}

	@Test
	public void testDisciplines() throws Exception {

		for (CharacterDiscipline discipline : CharacterDiscipline.values()) {
			for (long guid : discipline.getAbilities()) {
				final Parser p = createParser();
				p.parseLogLine("[14:14:35.306] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()");
				assertNull(p.getActorStates().get(p.getContext().getActor("Ixayly", Actor.Type.SELF)).discipline);
				p.parseLogLine("[21:30:14.981] [@SomeoneElse] [@Ixayly] [FooBar {" + guid + "}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()");
				assertNull(p.getActorStates().get(p.getContext().getActor("Ixayly", Actor.Type.SELF)).discipline);
				p.parseLogLine("[21:30:14.981] [@SomeoneElse] [@SomeoneElse] [FooBar {" + guid + "}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()");
				assertNull(p.getActorStates().get(p.getContext().getActor("Ixayly", Actor.Type.SELF)).discipline);
				p.parseLogLine("[21:30:14.981] [@SomeoneElse] [@SomeoneElse] [FooBar {" + guid + "}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()");
				assertNull(p.getActorStates().get(p.getContext().getActor("Ixayly", Actor.Type.SELF)).discipline);
				if (CharacterClass.Sentinel.equals(discipline.getCharacterClass())) {
					p.parseLogLine("[04:41:22.825] [@Ixayly] [@Ixayly] [Centering {2528580391141376}] [ApplyEffect {836045448945477}: Centering {2528580391141376}] ()");
				}
				if (CharacterClass.Marauder.equals(discipline.getCharacterClass())) {
					p.parseLogLine("[04:41:22.825] [@Ixayly] [@Ixayly] [Fury {2515454971084800}] [ApplyEffect {836045448945477}: Fury {2515454971084800}] ()");
				}
				p.parseLogLine("[21:30:14.981] [@Ixayly] [@Ixayly] [FooBar {" + guid + "}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()");
				assertEquals("Failed to detect discipline", discipline, p.getActorStates().get(p.getContext().getActor("Ixayly", Actor.Type.SELF)).discipline);
			}
		}
	}

	private void testCombatSequence(String name, String[] setup, Object[] tuples) throws Exception {
		final Parser p = createParser();
		for (int i = 0; i < setup.length; i++) {
			p.parseLogLine(setup[i]);
		}

		int i = 0;
		for (int j = 0; j < tuples.length; j += 4) {
			p.parseLogLine((String) tuples[j]);

			assertEquals(name + " #" + i + " mode failed", tuples[j + 1], p.getCurrentCombat() != null && p.getCurrentCombat().getBoss() != null
					? p.getCurrentCombat().getBoss().getMode() : null);
			assertEquals(name + " #" + i + " size failed", tuples[j + 2], p.getCurrentCombat() != null && p.getCurrentCombat().getBoss() != null
					? p.getCurrentCombat().getBoss().getSize() : null);
			i++;
		}
	}

	@Test
	public void testBossDetectionUpgrade() throws Exception {
		final String damage = "[Rapid Shots {814282849648640}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (797* energy {836045448940874}) <797>";
		final String enter = "[14:14:35.306] [@Ixale] [@Ixale] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()";

		// Sword Squadron
		String[] setup = new String[]{enter};
		final String u18s = "[14:14:36.306] [@Ixale] [Sword Squadron Unit 1 {3447784996864000}:123] " + damage;
		final String u18h = "[14:14:36.306] [@Ixale] [Sword Squadron Unit 1 {3468765912104960}:123] " + damage;
		final String u116s = "[14:14:36.306] [@Ixale] [Sword Squadron Unit 1 {3468770207072256}:123] " + damage;
		final String u116h = "[14:14:36.306] [@Ixale] [Sword Squadron Unit 1 {3468774502039552}:123] " + damage;
		final String u2 = "[14:14:36.306] [@Ixale] [Sword Squadron Unit 2 {3447789291831296}:123] " + damage;

		testCombatSequence("Sword Squadron 8S", setup, new Object[]{
				u2, Mode.SM, Size.Eight, true,
				u18s, Mode.SM, Size.Eight, false,
				u18h, Mode.SM, Size.Eight, false, // should not change
		});

		testCombatSequence("Sword Squadron 8H", setup, new Object[]{
				u18h, Mode.HM, Size.Eight, false,
		});

		testCombatSequence("Sword Squadron 16S", setup, new Object[]{
				u2, Mode.SM, Size.Eight, true,
				u2, Mode.SM, Size.Eight, true,
				u116s, Mode.SM, Size.Sixteen, false
		});

		testCombatSequence("Sword Squadron 16H", setup, new Object[]{
				u116h, Mode.HM, Size.Sixteen, false,
				u2, Mode.HM, Size.Sixteen, false,
				u18h, Mode.HM, Size.Sixteen, false, // should not change
		});


		// commanders
		final String deron = "[21:24:47.687] [@Ixale] [Deron Cadoruso {3456890327531520}:123] [Shiv {814884145070080}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (0 -immune {836045448945506}) <5139>";
		final String cryo1 = "[21:25:19.055] [@Ixale] [] [Cryo Grenade {3470617043009536}] [Event {836045448945472}: AbilityInterrupt {836045448945482}] ()";
		final String cryo2 = "[21:29:18.262] [Outlaw Revanite {3457818040467456}:47050005692496] [@Ixale] [Cryo Grenade {3470617043009536}] [ApplyEffect {836045448945477}: Frozen (Any) {3470617043009812}] ()";
		final String dfa = "[23:11:02.424] [Mandalorian Revanite {3457809450532864}:123] [@Buffsnipers] [Death from Above {3470050107326464}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (5829 kinetic {836045448940873}) <5829>";
		final String rc8s = "[21:38:38.134] [@Ixale] [Commanders Cache {3482806160195584}:123] " + damage;
		final String rc8h = "[21:38:38.134] [@Ixale] [Commanders Cache {3483025203527680}:123] " + damage;
		final String rc16s = "[21:38:38.134] [@Ixale] [Commanders Cache {3483029498494976}:123] " + damage;
		final String rc16h = "[21:38:38.134] [@Ixale] [Commanders Cache {3483033793462272}:123] " + damage;

		setup = new String[]{enter, deron};

		testCombatSequence("Commanders 8S", setup, new Object[]{
				rc8s, Mode.SM, Size.Eight, false,
				deron, Mode.SM, Size.Eight, false
		});

		testCombatSequence("Commanders 8H", setup, new Object[]{
				cryo1, Mode.HM, Size.Eight, true,
				rc8h, Mode.HM, Size.Eight, false,
		});

		testCombatSequence("Commanders 16S", setup, new Object[]{
				rc16s, Mode.SM, Size.Sixteen, false,
				deron, Mode.SM, Size.Sixteen, false
		});

		testCombatSequence("Commanders 16H", setup, new Object[]{
				deron, Mode.SM, Size.Eight, true,
				dfa, Mode.HM, Size.Eight, true,
				cryo2, Mode.HM, Size.Eight, true,
				rc16h, Mode.HM, Size.Sixteen, false,
		});

		// Revan
		final String revan = "[14:14:36.306] [@Ixale] [Revan {3431605855059968}:123] " + damage;
		setup = new String[]{enter, revan};

		Object[] tuples = new Object[]{
				// no change
				"[14:14:36.306] [@Ixale] [Revan {3431605855059968}:123] " + damage, Mode.SM, Size.Eight, true,
				"[21:38:38.134] [@Ixale] [Entropic Surprise Probe {3518102201434112}:47050005754355] " + damage, Mode.SM, Size.Eight, true,
				// confirm
				"[21:38:38.134] [@Ixale] [Revanite Cache {3484395298095104}:123] " + damage, Mode.SM, Size.Eight, false,
				// upgrade by NPC
				"[21:38:38.134] [@Ixale] [Revanite Cache {3484408182996992}:123] " + damage, Mode.SM, Size.Sixteen, false,
				"[21:38:38.134] [@Ixale] [Revanite Cache {3484403888029696}:123] " + damage, Mode.HM, Size.Eight, false,
				"[21:38:38.134] [@Ixale] [Revanite Cache {3484412477964288}:123] " + damage, Mode.HM, Size.Sixteen, false,
				"[21:38:38.134] [@Ixale] [Entropic Surprise Probe {3518888180449280}:123] " + damage, Mode.HM, Size.Eight, false,
				"[21:38:38.134] [@Ixale] [Entropic Surprise Probe {3518883885481984}:123] " + damage, Mode.HM, Size.Sixteen, false,
				// upgrade by effect (size inconclusive)
				"[21:38:38.134] [Revan {3431605855059968}:123] [@Ixale] [Essence Corruption {3447359795101696}] [ApplyEffect {836045448945477}: Essence Corruption {3447359795101696}] ()", Mode.HM, Size.Eight, true,
				"[21:41:08.791] [@Ixale] [@Siandra] [Malevolent Force Bond {3454111483691008}] [ApplyEffect {836045448945477}: Malevolent Force Bond {3454111483691008}] ()", Mode.HM, Size.Eight, true,
				"[21:42:33.021] [Revan {3431605855059968}:123] [@Ixale] [Trail of Agony {3443275281203200}] [ApplyEffect {836045448945477}: Agony {3443275281203461}] ()", Mode.HM, Size.Eight, true,
				// upgrade by damage (size inconclusive)
				"[22:17:44.775] [Machine Core {3447583133401088}:123] [@Ixale] [Consume Essence {3456112938451302}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (404 elemental {836045448940875}) <404>", Mode.SM, Size.Eight, true,
				"[22:17:44.775] [Machine Core {3447583133401088}:123] [@Ixale] [Consume Essence {3456112938451302}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (924 elemental {836045448940875}) <924>", Mode.HM, Size.Eight, true,
		};

		// test with resets
		int i = 0;
		for (int j = 0; j < setup.length; j += 3) {
			testCombatSequence("Revan reset " + (i++), setup, new Object[]{tuples[j], tuples[j + 1], tuples[j + 2], tuples[j + 3]});
		}

		// test sequence (2 upgrades)
		tuples = new Object[]{
				// no change
				"[14:14:36.306] [@Ixale] [Revan {3431605855059968}:10087002854648] " + damage, Mode.SM, Size.Eight, true,
				"[21:38:38.134] [@Ixale] [Entropic Surprise Probe {3518102201434112}:47050005754355] " + damage, Mode.SM, Size.Eight, true,
				// upgrade to HM
				"[21:38:38.134] [Revan {3431605855059968}:47050005754355] [@Ixale] [Essence Corruption {3447359795101696}] [ApplyEffect {836045448945477}: Essence Corruption {3447359795101696}] ()", Mode.HM, Size.Eight, true,
				// keep
				"[21:42:33.021] [Revan {3431605855059968}:47050005934850] [@Ixale] [Trail of Agony {3443275281203200}] [ApplyEffect {836045448945477}: Agony {3443275281203461}] ()", Mode.HM, Size.Eight, true,
				// upgrade to 16m
				"[21:38:38.134] [@Ixale] [Entropic Surprise Probe {3518883885481984}:47050005754355] " + damage, Mode.HM, Size.Sixteen, false,
				// keep
				"[21:42:33.021] [Revan {3431605855059968}:47050005934850] [@Ixale] [Trail of Agony {3443275281203200}] [ApplyEffect {836045448945477}: Agony {3443275281203461}] ()", Mode.HM, Size.Sixteen, false,
				"[22:17:44.775] [Machine Core {3447583133401088}:47050006439071] [@Ixale] [Consume Essence {3456112938451302}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (924 elemental {836045448940875}) <924>", Mode.HM, Size.Sixteen, false
		};
		testCombatSequence("Revan seq", setup, tuples);

		// Coratanni
		final String cora = "[15:06:52.554] [@Ixale] [Coratanni {3371437658210304}:10092001297869] [Thermal Detonator {3387565260406784}] [ApplyEffect {836045448945477}: Thermal Detonator {3387565260406784}] ()";
		final String cora8s = "[21:38:38.134] [@Ixale] [Treasure Chest {3443983950807040}:123] " + damage;
		final String cora8h = "[21:38:38.134] [@Ixale] [Treasure Chest {3468731552366592}:123] " + damage;
		final String cora16s = "[21:38:38.134] [@Ixale] [Treasure Chest {3468735847333888}:123] " + damage;
		final String cora16h = "[21:38:38.134] [@Ixale] [Treasure Chest {3468740142301184}:123] " + damage;
		final String coraBurn = "[17:20:38.911] [Coratanni {3371437658210304}:123] [@Ixale] [Burning {3441368315724050}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (6222 internal {836045448940876}) <6222>";
		final String coraBurn2 = "[17:41:48.338] [Ruugar {3371441953177600}:123] [@Ixale] [Burned {3438915889398034}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (6020 internal {836045448940876}) <6020>";

		setup = new String[]{enter, cora};

		testCombatSequence("Coratanni 8S", setup, new Object[]{
				cora8s, Mode.SM, Size.Eight, false,
				cora, Mode.SM, Size.Eight, false
		});

		testCombatSequence("Coratanni 8H", setup, new Object[]{
				coraBurn2, Mode.HM, Size.Eight, true,
				cora8h, Mode.HM, Size.Eight, false,
		});

		testCombatSequence("Coratanni 16S", setup, new Object[]{
				cora16s, Mode.SM, Size.Sixteen, false,
				cora, Mode.SM, Size.Sixteen, false
		});

		testCombatSequence("Coratanni 16H", setup, new Object[]{
				cora, Mode.SM, Size.Eight, true,
				coraBurn, Mode.HM, Size.Eight, true,
				coraBurn2, Mode.HM, Size.Eight, true,
				cora16h, Mode.HM, Size.Sixteen, false,});

		setup = new String[]{
				"[22:10:05.688] [@Memetech] [@Memetech] [] [Event {836045448945472}: EnterCombat {836045448945489}] (Valley of the Machine Gods (8 Player Master))",
				"[22:10:25.741] [@Memetech] [TYTH {4078423634870272}:16404004050109] [Flame Barrage {1708872997797888}] [ApplyEffect {836045448945477}: Overwhelmed (Mental) {1708872997798162}] ()"};

		testCombatSequence("TYTH 8NiM", setup, new Object[]{
				"[22:10:25.924] [@Memetech] [TYTH {4078423634870272}:16404004050109] [Incendiary Missile {2027022700249088}] [ApplyEffect {836045448945477}: Burning (Incendiary Missile) {2027022700249344}] ()",
				Mode.NiM, Size.Eight, false
		});
	}
}
