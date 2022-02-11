package com.ixale.starparse.parser;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.stats.CombatMitigationStats;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.domain.stats.DamageDealtStats;
import com.ixale.starparse.domain.stats.DamageTakenStats;
import com.ixale.starparse.domain.stats.HealingTakenStats;
import com.ixale.starparse.service.EventService;
import com.ixale.starparse.service.impl.Context;
import com.ixale.starparse.utils.FileLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-context.xml")
public class EventServiceTest {

	@Autowired
	private EventService eventService;
	@Autowired
	private Context context;

	private Parser parser;

	@Before
	public void setUp() throws Exception {
		parser = new Parser(context);

		eventService.resetAll();
	}

	@Test
	public void testFull() throws Exception {

		// load test source file
		final File sourceLog = new File(getClass().getClassLoader().getResource("combat_2014-01-26_22_01_13_435268.txt").toURI());

		parser.setCombatLogFile(sourceLog);

		// check DAO
		eventService.storeCombatLog(parser.getCombatLog());

		// parse one by one, with intermediary flushes
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(sourceLog);
			br = new BufferedReader(fr);

			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				parser.parseLogLine(line);
				if (++i % 3000 == 0) {
					eventService.flushEvents(parser.getEvents(),
							parser.getCombats(), parser.getCurrentCombat(),
							parser.getEffects(), parser.getCurrentEffects(),
							parser.getPhases(), parser.getCurrentPhase(),
							parser.getAbsorptions(),
							parser.getActorStates());
				}
			}
			eventService.flushEvents(parser.getEvents(),
					parser.getCombats(), parser.getCurrentCombat(),
					parser.getEffects(), parser.getCurrentEffects(),
					parser.getPhases(), parser.getCurrentPhase(),
					parser.getAbsorptions(),
					parser.getActorStates());

		} finally {
			try {
				br.close();
			} catch (Exception ignored) {
			}
			try {
				fr.close();
			} catch (Exception ignored) {
			}
		}

		// check result
		Combat c = null;
		CombatStats stats = null;
		DamageDealtStats dds = null;
		List<DamageDealtStats> damageDealtStats = null;

		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
		List<Combat> combats = eventService.getCombats();
		assertEquals(31, combats.size());
		assertEquals("Ixale", parser.getCombatLog().getCharacterName());

		// verify combat 1
		c = combats.get(1);
		assertEquals("2014-01-27 01:10:34.526", sdf.format(c.getTimeFrom()));
		assertEquals("Dread Master Calphayus (SM 8m)", c.getBoss().toString());
		assertEquals(442874, c.getTimeTo() - c.getTimeFrom());

		stats = eventService.getCombatStats(c, null, parser.getCombatLog().getCharacterName());
		assertEquals(34.68, stats.getApm());
		assertEquals(1018954, stats.getDamage());
		assertEquals(2301, stats.getDps());
		assertEquals(199588, stats.getHeal());
		assertEquals(451, stats.getHps());
		assertEquals(204, stats.getEhps());
		assertEquals(45.31, stats.getEhpsPercent());
		assertEquals(140364, stats.getDamageTaken());
		assertEquals(317, stats.getDtps());
		assertEquals(342032, stats.getHealTaken());
		assertEquals(772, stats.getHpsTaken());
		assertEquals(295, stats.getEhpsTaken());
		assertEquals(1143416, stats.getThreat());
		assertEquals(2582, stats.getTps());
		assertEquals(1143416, stats.getThreatPositive());
		assertEquals(8358, stats.getAbsorbed());

		// verify combat 6
		c = combats.get(6);
		assertEquals("2014-01-27 01:31:59.110", sdf.format(c.getTimeFrom()));
		assertEquals("Dread Master Raptus (HM 8m)", c.getBoss().toString());
		assertEquals(394327, c.getTimeTo() - c.getTimeFrom());

		stats = eventService.getCombatStats(c, null, parser.getCombatLog().getCharacterName());
		assertEquals(32.41, stats.getApm());
		assertEquals(874441, stats.getDamage());
		assertEquals(2218, stats.getDps());
		assertEquals(132737, stats.getHeal());
		assertEquals(337, stats.getHps());
		assertEquals(253, stats.getEhps());
		assertEquals(75.21, stats.getEhpsPercent());
		assertEquals(247176, stats.getDamageTaken());
		assertEquals(627, stats.getDtps());
		assertEquals(303582, stats.getHealTaken());
		assertEquals(770, stats.getHpsTaken());
		assertEquals(509, stats.getEhpsTaken());
		assertEquals(14938, stats.getThreat());
		assertEquals(38, stats.getTps());
		assertEquals(845628, stats.getThreatPositive());

		// verify combat 14
		c = combats.get(14);
		assertEquals("2014-01-27 02:05:31.950", sdf.format(c.getTimeFrom()));
		assertEquals(null, c.getBoss());
		assertEquals(61428, c.getTimeTo() - c.getTimeFrom());
		assertEquals("Dwayna, Bartley, Dread Host Soldier (2), Dread Guard Nullifier, Dread Host Commando, Unknown", c.getName());

		// verify combat 30
		c = combats.get(30);
		assertEquals("2014-01-27 02:51:10.836", sdf.format(c.getTimeFrom()));
		assertEquals("Dread Master Brontes (SM 8m)", c.getBoss().toString());
		assertEquals(444166, c.getTimeTo() - c.getTimeFrom());

		stats = eventService.getCombatStats(c, null, parser.getCombatLog().getCharacterName());

		damageDealtStats = eventService.getDamageDealtStats(c, false, false, false, null, parser.getCombatLog().getCharacterName());

		assertEquals(1, damageDealtStats.size());

		dds = damageDealtStats.get(0);
		assertEquals("Total", dds.getName());
		assertEquals(876, dds.getTicks());
		assertEquals(2043.0, dds.getAverageCrit());
		assertEquals(1248.0, dds.getAverageNormal());
		assertEquals(33.22, dds.getPercentCrit());
		assertEquals(7.76, dds.getPercentMiss());
		assertEquals(594457, dds.getTotalCrit());
		assertEquals(571678, dds.getTotalNormal());
		assertEquals(1166135, dds.getTotal());
		assertEquals(2625, dds.getDps());
		assertEquals(100.0, dds.getPercentTotal());

		damageDealtStats = eventService.getDamageDealtStats(c, false, false, true, null, parser.getCombatLog().getCharacterName());

		assertEquals(12, damageDealtStats.size());

		dds = damageDealtStats.get(0);
		assertEquals("Overload Saber: Burning (Physical)", dds.getName());
		assertEquals(31, dds.getActions()); // dots actions
		assertEquals(144, dds.getTicks());
		assertEquals(2291.0, dds.getAverageCrit());
		assertEquals(1085.0, dds.getAverageNormal());
		assertEquals(61.11, dds.getPercentCrit());
		assertEquals(0.69, dds.getPercentMiss());
		assertEquals(201615, dds.getTotalCrit());
		assertEquals(53182, dds.getTotalNormal());
		assertEquals(254797, dds.getTotal());
		assertEquals(574, dds.getDps());
		assertEquals(21.8, dds.getPercentTotal());
		assertEquals("elemental", dds.getDamageType());


		damageDealtStats = eventService.getDamageDealtStats(c, true, true, false, null, parser.getCombatLog().getCharacterName());

		assertEquals(17, damageDealtStats.size());

		dds = damageDealtStats.get(0);
		assertEquals("Total", dds.getName());
		assertEquals(420, dds.getTicks());
		assertEquals(2065.0, dds.getAverageCrit());
		assertEquals(33.33, dds.getPercentCrit());

		damageDealtStats = eventService.getDamageDealtStatsSimple(c, null, parser.getCombatLog().getCharacterName());

		assertEquals(1, damageDealtStats.size());

		dds = damageDealtStats.get(0);
		assertEquals("Total", dds.getName());
		assertEquals(876, dds.getTicks());
		assertEquals(33.22, dds.getPercentCrit());
		assertEquals(7.76, dds.getPercentMiss());
		assertEquals(2625, dds.getDps());

		List<Effect> effects = eventService.getCombatEffects(c, null);
		Effect effect;

		assertEquals(301, effects.size());

		effect = effects.get(0);
		assertEquals("Power Surge", effect.getEffect().getName());
		assertEquals(Long.valueOf(3244362460823552L), effect.getEffect().getGuid());
		assertEquals("Ixale", effect.getSource().getName());
		assertEquals(1, effect.getSource().getType().getId());
		assertEquals("Ixale", effect.getTarget().getName());
		assertEquals(1, effect.getTarget().getType().getId());
		assertEquals("2014-01-27 02:51:12.873", sdf.format(effect.getTimeFrom()));
		assertEquals("2014-01-27 02:51:18.933", sdf.format(effect.getTimeTo()));
		assertFalse(effect.isAbsorption());
		assertFalse(effect.isActivated());

		effect = effects.get(102);
		assertEquals("Kolto Cloud", effect.getEffect().getName());
		assertEquals("Fathersheep", effect.getSource().getName());
		assertEquals("Ixale", effect.getTarget().getName());
		assertEquals("2014-01-27 02:53:49.941", sdf.format(effect.getTimeFrom()));
		assertEquals("2014-01-27 02:53:59.028", sdf.format(effect.getTimeTo()));
		assertFalse(effect.isAbsorption());
		assertFalse(effect.isActivated());

		effect = effects.get(153);
		assertEquals("Force Armor", effect.getEffect().getName());
		assertTrue(effect.isAbsorption());
		assertFalse(effect.isActivated());

		effect = effects.get(300);
		assertEquals("Burning (Physical)", effect.getEffect().getName());
		assertEquals("Ixale", effect.getSource().getName());
		assertEquals("Dread Master Brontes", effect.getTarget().getName());
		assertEquals("2014-01-27 02:58:32.268", sdf.format(effect.getTimeFrom()));
		assertEquals("2014-01-27 02:58:35.035", sdf.format(effect.getTimeTo()));
		assertFalse(effect.isAbsorption());
		assertFalse(effect.isActivated());

		/*long x = System.currentTimeMillis();
		List<Event> events = eventService.getCombatEvents(c, EnumSet.noneOf(Event.Type.class), null, null, null, null);
		System.out.println("Took: "+(System.currentTimeMillis() - x)+" ("+events.size()+")");

		for (Event e: events) {
			File f = new File("c:/x/e"+e.getEventId()+".log");
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(e);
			out.flush();
			out.close();
			fos.close();
		}*/

		/*context.reset();
		events = null;

		FileInputStream fin = new FileInputStream(f);
		ObjectInputStream in = new ObjectInputStream(fin);
		events = (List<Event>) in.readObject();
		System.out.println(events.get(0).getSource().hashCode());
		System.out.println(events.get(1).getSource().hashCode());
		System.out.println(events.get(2).getSource().hashCode());
		in.close();
		fin.close();*/

		// check combat extraction
		c = combats.get(14);
		assertEquals("2014-01-27 02:05:31.950", sdf.format(c.getTimeFrom()));
		assertEquals("2014-01-27 02:06:33.378", sdf.format(c.getTimeTo()));
		final String combat14 = FileLoader.extractPortion(sourceLog, "[" + sdfTime.format(c.getTimeFrom()), "[" + sdfTime.format(c.getTimeTo())).sb.toString();

		assertNotNull(combat14);
		assertEquals(58794, combat14.length());
	}

	@Test
	public void testStatsFull() throws Exception {

		// load test source file
		final File sourceLog = new File(getClass().getClassLoader().getResource("combat_2014-02-26_03_01_11_705182.txt").toURI());

		parser.setCombatLogFile(sourceLog);

		// check DAO
		eventService.storeCombatLog(parser.getCombatLog());

		// parse one by one, with intermediary flushes
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(sourceLog);
			br = new BufferedReader(fr);

			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				parser.parseLogLine(line);
				if (++i % 3000 == 0) {
					eventService.flushEvents(parser.getEvents(),
							parser.getCombats(), parser.getCurrentCombat(),
							parser.getEffects(), parser.getCurrentEffects(),
							parser.getPhases(), parser.getCurrentPhase(),
							parser.getAbsorptions(), parser.getActorStates());
				}
			}
			eventService.flushEvents(parser.getEvents(),
					parser.getCombats(), parser.getCurrentCombat(),
					parser.getEffects(), parser.getCurrentEffects(),
					parser.getPhases(), parser.getCurrentPhase(),
					parser.getAbsorptions(), parser.getActorStates());

		} finally {
			try {
				br.close();
			} catch (Exception ignored) {
			}
			try {
				fr.close();
			} catch (Exception ignored) {
			}
		}

		// check result
		Combat c = null;
		DamageDealtStats dds = null;
		List<DamageDealtStats> damageDealtStats = null;
		DamageTakenStats dts = null;
		List<DamageTakenStats> damageTakenStats = null;
		CombatMitigationStats cms = null;
		HealingTakenStats hts = null;
		List<HealingTakenStats> healingTakenStats = null;

		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
		List<Combat> combats = eventService.getCombats();
		assertEquals(25, combats.size());
		assertEquals("Ixaar", parser.getCombatLog().getCharacterName());

		// verify combat 25
		c = combats.get(24);
		assertEquals("2014-02-26 04:28:04.902", sdf.format(c.getTimeFrom()));
		assertEquals("Dread Master Brontes (HM 8m)", c.getBoss().toString());
		assertEquals(420490, c.getTimeTo() - c.getTimeFrom());

		// damage dealt
		damageDealtStats = eventService.getDamageDealtStats(c, false, false, true, null, parser.getCombatLog().getCharacterName());
		assertEquals(15, damageDealtStats.size());

		dds = damageDealtStats.get(0);
		assertEquals("Blade Storm", dds.getName());
		assertEquals(27, dds.getTicks());
		assertEquals(2857.0, dds.getAverageCrit());
		assertEquals(1852.0, dds.getAverageNormal());

		// damage taken
		damageTakenStats = eventService.getDamageTakenStats(c, false, false, true, null, parser.getCombatLog().getCharacterName());
		assertEquals(14, damageTakenStats.size());

		dts = damageTakenStats.get(1);
		assertEquals("Fire and Forget", dts.getName());
		assertEquals(120, dts.getTicks());
		assertEquals(12.5, dts.getPercentMiss());
		assertEquals(37.5, dts.getPercentShield());
		assertEquals(703.0, dts.getAverageNormal());
		assertEquals(6818, dts.getTotalAbsorbed());
		assertEquals(84343, dts.getTotal());

		// mitigation
		cms = eventService.getCombatMitigationStats(c, null, parser.getCombatLog().getCharacterName());
		assertEquals(275, cms.getTicks());
		assertEquals(491144, cms.getDamage());

		assertEquals(13141, cms.getInternal());
		assertEquals(2.7, cms.getInternalPercent());
		assertEquals(77469, cms.getElemental());
		assertEquals(15.8, cms.getElementalPercent());
		assertEquals(68449, cms.getEnergy());
		assertEquals(13.9, cms.getEnergyPercent());
		assertEquals(332085, cms.getKinetic());
		assertEquals(67.6, cms.getKineticPercent());

		assertEquals(22.2, cms.getMissPercent());
		assertEquals(27.3, cms.getShieldPercent());

		assertEquals(86381, cms.getAbsorbedSelf() + cms.getAbsorbedOthers());
		assertEquals(17.6, cms.getAbsorbedSelfPercent() + cms.getAbsorbedOthersPercent());
		assertEquals(205, cms.getAps());

		// absorptions & healing
		healingTakenStats = eventService.getHealingTakenStats(c, true, true, null, parser.getCombatLog().getCharacterName());
		assertEquals(15, healingTakenStats.size());

		hts = healingTakenStats.get(0);
		assertNotNull(hts);
	}

	@Test
	public void testEffectsFull() throws Exception {

		// load test source file
		final File sourceLog = new File(getClass().getClassLoader().getResource("combat_2014-02-23_20_03_40_777352.txt").toURI());

		parser.setCombatLogFile(sourceLog);

		// check DAO
		eventService.storeCombatLog(parser.getCombatLog());

		// parse one by one, with intermediary flushes
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(sourceLog);
			br = new BufferedReader(fr);

			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				parser.parseLogLine(line);
				if (++i % 3000 == 0) {
					eventService.flushEvents(parser.getEvents(),
							parser.getCombats(), parser.getCurrentCombat(),
							parser.getEffects(), parser.getCurrentEffects(),
							parser.getPhases(), parser.getCurrentPhase(),
							parser.getAbsorptions(), parser.getActorStates());
				}
			}
			eventService.flushEvents(parser.getEvents(),
					parser.getCombats(), parser.getCurrentCombat(),
					parser.getEffects(), parser.getCurrentEffects(),
					parser.getPhases(), parser.getCurrentPhase(),
					parser.getAbsorptions(), parser.getActorStates());

		} finally {
			try {
				br.close();
			} catch (Exception ignored) {
			}
			try {
				fr.close();
			} catch (Exception ignored) {
			}
		}

		// check result
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
		List<Combat> combats = eventService.getCombats();
		assertEquals(22, combats.size());
		assertEquals("Ixayly", parser.getCombatLog().getCharacterName());

		List<Effect> effects = eventService.getCombatEffects(combats.get(0), null);
		Effect effect;

		assertEquals(27, effects.size());

		effect = effects.get(0);
		assertEquals("Cool Under Pressure", effect.getEffect().getName());
		assertEquals(Long.valueOf(2303227752087552l), effect.getEffect().getGuid());
		assertEquals("Ixayly", effect.getSource().getName());
		assertEquals(Actor.Type.SELF, effect.getSource().getType());
		assertEquals("Ixayly", effect.getTarget().getName());
		assertEquals(Actor.Type.SELF, effect.getTarget().getType());
		assertEquals(304, effect.getEventIdFrom());
		assertEquals("2014-02-23 20:28:04.254", sdf.format(effect.getTimeFrom()));
		assertEquals(358, effect.getEventIdTo().intValue());
		assertEquals("2014-02-23 20:28:14.611", sdf.format(effect.getTimeTo()));

		// test immutable actor/entity cache
		assertEquals(effect.getSource(), effect.getTarget());
		assertEquals(effect.getSource(), effects.get(1).getSource());
		assertEquals(effect.getEffect(), effects.get(20).getEffect()); // another CuP
	}

	@Test
	public void testIncremental() throws Exception {

		// test data
		final Object[] samplePairs = new Object[]{
				new String[]{
						"[14:14:33.877] [@Ixayly] [@Ixayly] [Aimed Shot {1117507540746240}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()",
						"[14:14:35.306] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()",
						"[14:14:35.419] [@Ixayly] [Palace Interrogator {3295433916940288}:24640000008962] [XS Freighter Flyby {2524220999335936}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (6027* elemental {836045448940875}) <6027>",
						"[14:14:40.593] [Palace Watchman {3295455391776768}:24640000009029] [@Ixayly] [Shadow Strike {3298783991431168}] [ApplyEffect {836045448945477}: Immobilized (Physical) {3298783991431428}] ()"
				}, new Integer[][]{
				{2, null}
		}, // one combat, open from 2

				new String[]{
						"[14:14:44.554] [@Ixayly] [Palace Watchman {3295455391776768}:24640000009029] [] [Event {836045448945472}: Death {836045448945493}] ()",
						"[14:15:01.171] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()",
				}, new Integer[][]{
				{2, 6}
		}, // still the one combat, 6 is the candidate

				new String[]{
						"[14:15:04.419] [@Ixayly] [Palace Interrogator {3295433916940288}:24640000008962] [XS Freighter Flyby {2524220999335936}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (6027* elemental {836045448940875}) <6027>",
						"[14:15:28.192] [@Ixayly] [@Ixayly] [Sprint {810670782152704}] [ApplyEffect {836045448945477}: Sprint {810670782152704}] ()"
				}, new Integer[][]{
				{2, 7}
		}, // one combat, closed at 7

				new String[]{
						"[14:15:32.446] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()",
						"[14:15:33.282] [@Ixayly] [Palace Guardian {3295459686744064}:24640000009093] [Vital Shot {2115340112756736}] [ApplyEffect {836045448945477}: Bleeding (Tech) {2115340112756992}] ()",
						"[14:15:34.366] [Palace Guardian {3295459686744064}:24640000009093] [@Ixayly] [Saber Reflect {3305905047207936}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (2830 energy {836045448940874}(reflected {836045448953649})) <2830>",
						"[14:15:52.595] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()",
						"[14:15:56.783] [@Evelona] [@Ixayly] [Emergency Medpac {807518276157440}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (3464) <1558>",
						"[14:15:56.996] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: LeaveCover {836045448945486}] ()",
				}, new Integer[][]{
				{2, 7},
				{9, 12}
		}, // two combats, last from 9 to 12, already closed

				new String[]{
						"[21:14:49.052] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()",
						"[21:14:49.077] [@Ixayly] [@Ixayly] [Riot Strike {2204391964672000}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()",
						"[21:14:49.078] [@Ixayly] [@Ixayly] [Sprint {810670782152704}] [RemoveEffect {836045448945478}: Sprint {810670782152704}] ()",
						"[21:14:50.318] [@Ixayly] [Operations Training Dummy {2857785339412480}:144003109767] [Riot Strike {2204391964672000}] [Event {836045448945472}: ModifyThreat {836045448945483}] () <1>",
						"[21:14:58.358] [@Ixayly] [@Ixayly] [] [Event {836045448945472}: ExitCombat {836045448945490}] ()",
				}, new Integer[][]{
				{2, 7},
				{9, 12},
				{15, 19}
		} // new combat, ended instantly
		};

		parser.setCombatLogFile(new File("combat_2014-01-26_22_01_13_435268.txt"));

		Combat c;
		List<Combat> list;
		for (int j = 0; j < samplePairs.length; j += 2) {
			String msg = "Set " + (j / 2 + 1) + " failed";
			for (int i = 0; i < ((String[]) samplePairs[j]).length; i++) {
				parser.parseLogLine(((String[]) samplePairs[j])[i]);
			}

			// save
			eventService.flushEvents(parser.getEvents(),
					parser.getCombats(), parser.getCurrentCombat(),
					parser.getEffects(), parser.getCurrentEffects(),
					parser.getPhases(), parser.getCurrentPhase(),
					parser.getAbsorptions(), parser.getActorStates());

			// and fetch immediately
			list = eventService.getCombats();

			assertEquals(msg, ((Integer[][]) samplePairs[j + 1]).length, list.size());
			for (int k = 0; k < list.size(); k++) {
				c = list.get(k);
				assertEquals(msg + ", combat " + k, ((Integer[][]) samplePairs[j + 1])[k][0], Integer.valueOf(c.getEventIdFrom()));
				assertEquals(msg + ", combat " + k, ((Integer[][]) samplePairs[j + 1])[k][1], c.getEventIdTo());
			}
		}
	}

	@Test
	public void testEffectsIncremental() throws Exception {
		// test data
		final Object[] samplePairs = new Object[]{
				new String[]{
						"[03:14:35.306] [@Ixayla] [@Ixayla] [] [Event {836045448945472}: EnterCombat {836045448945489}] ()",

						"[03:50:49.304] [@Ixayla] [@Ixayla] [Trauma Probe {999516199190528}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()",
						"[03:50:49.304] [@Ixayla] [@Athéna] [Trauma Probe {999516199190528}] [ApplyEffect {836045448945477}: Trauma Probe {999516199190528}] ()",

						"[04:01:37.488] [@Ixayla] [@Ixayla] [Trauma Probe {999516199190528}] [Event {836045448945472}: AbilityActivate {836045448945479}] ()",
						"[04:01:37.488] [@Ixayla] [@Athéna] [Trauma Probe {999516199190528}] [ApplyEffect {836045448945477}: Trauma Probe {999516199190528}] ()",

						"[04:06:54.388] [@Ixayla] [@Athéna] [Trauma Probe {999516199190528}] [ApplyEffect {836045448945477}: Heal {836045448945500}] (2056*) <925>",

						"[04:12:54.015] [@Ixayla] [@Athéna] [Trauma Probe {999516199190528}] [RemoveEffect {836045448945478}: Trauma Probe {999516199190528}] ()"
				}, new Integer[][]{
				{3, null},
				{5, 7}
		} // two effects
		};

		parser.setCombatLogFile(new File("combat_2014-01-26_22_01_13_435268.txt"));

		List<Combat> combats;
		List<Effect> effects;
		Effect effect;
		for (int j = 0; j < samplePairs.length; j += 2) {
			String msg = "Set " + (j / 2 + 1) + " failed";

			for (int i = 0; i < ((String[]) samplePairs[j]).length; i++) {
				parser.parseLogLine(((String[]) samplePairs[j])[i]);
			}

			// save
			eventService.flushEvents(parser.getEvents(),
					parser.getCombats(), parser.getCurrentCombat(),
					parser.getEffects(), parser.getCurrentEffects(),
					parser.getPhases(), parser.getCurrentPhase(),
					parser.getAbsorptions(), parser.getActorStates());

			// and fetch immediately
			combats = eventService.getCombats();
			effects = eventService.getCombatEffects(combats.get(0), null);

			assertEquals(msg, ((Integer[][]) samplePairs[j + 1]).length, effects.size());
			for (int k = 0; k < effects.size(); k++) {
				effect = effects.get(k);
				assertEquals(msg + ", effect " + k, ((Integer[][]) samplePairs[j + 1])[k][0], Integer.valueOf(effect.getEventIdFrom()));
				assertEquals(msg + ", effect " + k, ((Integer[][]) samplePairs[j + 1])[k][1], effect.getEventIdTo());
			}
		}
	}

	@Test
	public void testOverlappingAbsorbs() throws Exception {
		// test data
		final Object[] samplePairs = new Object[]{
				new String[]{
						"[21:17:14.248] [@Senarî#689198525926986|(63.84,1553.43,229.43,180.00)|(392212/406515)] [] [] [AreaEntered {836045448953664}: Valley of the Machine Gods {833571547775765} 8 Player Veteran {836045448953652}] (HE600) <v7.0.0b>",
						"[21:17:36.495] [@Senarî#689198525926986|(810.56,2055.88,239.14,166.28)|(406515/406515)] [] [] [Event {836045448945472}: EnterCombat {836045448945489}]",
						"[21:17:36.495] [@Senarî#689198525926986|(809.48,2082.54,242.24,-179.53)|(370571/370571)] [] [] [DisciplineChanged {836045448953665}: Sniper {16141046347418927959}/Engineering {2031339142381592}]",
						// dampers up - 3
						"[21:17:39.324] [@Senarî#689198525926986|(808.77,2093.91,244.88,-191.16)|(370571/370571)] [=] [Ballistic Dampers {3394012006318080}] [ApplyEffect {836045448945477}: Ballistic Dampers {3394012006318080}] (3 charges {836045448953667})",
						// probe up
						"[21:17:39.418] [@Senarî#689198525926986|(808.77,2093.91,244.88,-191.16)|(370571/370571)] [=] [Shield Probe {784716294782976}] [Event {836045448945472}: AbilityActivate {836045448945479}]",
						"[21:17:39.418] [@Senarî#689198525926986|(808.77,2093.91,244.88,-191.16)|(370571/370571)] [=] [Shield Probe {784716294782976}] [ApplyEffect {836045448945477}: Shield Probe {784716294782976}]",
						// -- probe 60% (!?), charges incorrect
						"[21:17:41.002] [@Senarî#689198525926986|(808.76,2093.89,244.88,-191.16)|(370571/370571)] [] [Ballistic Dampers {3394012006318080}] [ModifyCharges {836045448953666}: Ballistic Dampers {3394012006318080}] (2 charges {836045448953667})",
						"[21:17:41.115] [Scour Droid {4075842359525376}:2869000007497|(810.37,2102.05,244.91,-17.07)|(338070/375723)] [@Senarî#689198525926986|(808.76,2093.89,244.88,-191.16)|(370571/370571)] [Freezing Beam {4075859539394560}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (14387 ~0 kinetic {836045448940873} (10071 absorbed {836045448945511})) <14387>",
						// probe down
						"[21:17:49.523] [@Senarî#689198525926986|(808.76,2093.89,244.88,173.53)|(370571/370571)] [=] [Shield Probe {784716294782976}] [RemoveEffect {836045448945478}: Shield Probe {784716294782976}]",
						// -- dampers 30%, charges incorrect
						"[21:17:50.799] [@Senarî#689198525926986|(808.76,2093.89,244.88,173.53)|(363046/370571)] [] [Ballistic Dampers {3394012006318080}] [ModifyCharges {836045448953666}: Ballistic Dampers {3394012006318080}] (1 charges {836045448953667})",
						"[21:17:50.888] [Scour Droid {4075842359525376}:2869000007497|(810.54,2102.73,244.91,31.30)|(137081/375723)] [@Senarî#689198525926986|(808.76,2093.89,244.88,173.29)|(363046/370571)] [Explosive Round {4075863834361856}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (10750 ~7525 kinetic {836045448940873} (3225 absorbed {836045448945511})) <10750>",
						// dampers down
						"[21:17:56.759] [@Senarî#689198525926986|(808.76,2093.89,244.88,168.44)|(363416/370571)] [=] [Ballistic Dampers {3394012006318080}] [RemoveEffect {836045448945478}: Ballistic Dampers {3394012006318080}]",
						// -- dampers 30%, charges incorrect
						"[21:17:56.977] [Magma Droid {4068373411397632}:2869000007309|(806.50,2104.71,244.92,-11.86)|(867987/1225625)] [@Senarî#689198525926986|(808.76,2093.89,244.88,168.44)|(363416/370571)] [Ranged Attack {4070503715176448}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (5111 ~3577 kinetic {836045448940873} (1533 absorbed {836045448945511})) <5111>",
						// -- dampers 30%, charges incorrect
						"[21:17:58.318] [Magma Droid {4068373411397632}:2869000007309|(807.17,2101.64,244.91,-11.74)|(833754/1225625)] [@Senarî#689198525926986|(808.76,2093.89,244.88,168.44)|(365003/370571)] [Ranged Attack {4070503715176448}] [ApplyEffect {836045448945477}: Damage {836045448945501}] (5111 ~3577 kinetic {836045448940873} (1533 absorbed {836045448945511})) <5111>"
				}, new Integer[][]{
				{2, 10},
				{4, 7}
		} // two effects
		};

		parser.setCombatLogFile(new File("combat_2021-12-01_22_01_13_435268.txt"));

		List<Combat> combats;
		List<Effect> effects;
		Effect effect;
		for (int j = 0; j < samplePairs.length; j += 2) {
			String msg = "Set " + (j / 2 + 1) + " failed";

			for (int i = 0; i < ((String[]) samplePairs[j]).length; i++) {
				parser.parseLogLine(((String[]) samplePairs[j])[i]);
			}

			// save
			eventService.flushEvents(parser.getEvents(),
					parser.getCombats(), parser.getCurrentCombat(),
					parser.getEffects(), parser.getCurrentEffects(),
					parser.getPhases(), parser.getCurrentPhase(),
					parser.getAbsorptions(), parser.getActorStates());

			// and fetch immediately
			combats = eventService.getCombats();
			effects = eventService.getCombatEffects(combats.get(0), null);

			assertEquals(msg, ((Integer[][]) samplePairs[j + 1]).length, effects.size());
			for (int k = 0; k < effects.size(); k++) {
				effect = effects.get(k);
				assertEquals(msg + ", effect " + k, ((Integer[][]) samplePairs[j + 1])[k][0], Integer.valueOf(effect.getEventIdFrom()));
				assertEquals(msg + ", effect " + k, ((Integer[][]) samplePairs[j + 1])[k][1], effect.getEventIdTo());
			}
		}
	}
}
