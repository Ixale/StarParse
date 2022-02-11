package com.ixale.starparse.rank;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

import org.junit.Test;

import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.Raid.Mode;
import com.ixale.starparse.domain.Raid.Size;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.RankClass;
import com.ixale.starparse.domain.RankClass.Reason;
import com.ixale.starparse.domain.Ranking;
import com.ixale.starparse.domain.ops.WorldBoss;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.service.RankService.RankType;
import com.ixale.starparse.service.impl.RankServiceImpl;
import com.ixale.starparse.utils.FileDownloader;

public class RankTest {

	private final RankServiceImpl service;
	private final byte[] dpsContent;
	private final String dpsFileName = "dps_ColossalMonolith_HM_16m_Commando.xml";

	public RankTest() {
		service = new RankServiceImpl();

		try {
			dpsContent = Files.readAllBytes(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(dpsFileName)).toURI()).toPath());
		} catch (Exception e) {
			throw new RuntimeException("Missing " + dpsFileName, e);
		}
	}

	@Test
	public void testDownload() throws Exception {
		Raid raid = new WorldBoss();
		RaidBoss boss = raid.getBosses().get(1);

		assertEquals(boss.getRaidBossName(), RaidBossName.ColossalMonolith);
		assertEquals(boss.getMode(), Mode.HM);
		assertEquals(boss.getSize(), Size.Sixteen);

		final String key = service.buildKey(boss, RankType.DPS, CharacterDiscipline.InnovativeOrdnance);

		assertEquals("dps_ColossalMonolith_HM_16m_Commando", key);

		final URL url = service.buildUrl(boss, Config.DEFAULT_SERVER_HOST, key);

		final String content = FileDownloader.fetchFile(url);

		assertNotNull(content);
		assertTrue(content.contains("<boss>ColossalMonolith HM 16m</boss>"));
		assertTrue(content.contains("<className>Commando</className>"));
		assertTrue(content.contains("<type>dps</type>"));

		final Ranking r = service.readRanking(content);

		assertNotNull(r);
		assertTrue(r.getMinTick() > 120000);

		final RankClass rc = service.getRank(r, r.getMinTick() + 1, 2485);
		assertNotNull(rc);
		assertNull(rc.getReason());
		assertTrue(rc.getPercent() >= 0 && rc.getPercent() <= 100);
	}

	@Test
	public void testPercentile() {
		Ranking r = service.readRanking(new String(dpsContent));
		assertNotNull(r);
		assertNotNull(r.getPercentiles());
		assertEquals(360000, r.getMinTick());

		RankClass rc = service.getRank(r, 360000 - 1, 2485);
		assertNotNull(rc);
		assertEquals(Reason.TICK_TOO_LOW, rc.getReason());
		assertNull(rc.getPercent());

		rc = service.getRank(r, 360000, 2485);
		assertNotNull(rc);
		assertNull(rc.getReason());
		assertEquals(18, (int) rc.getPercent());

		assertEquals(36, (int) service.getRank(r, 360000, 2669).getPercent());
		assertEquals(0, (int) service.getRank(r, 3612345, 0).getPercent());
		assertEquals(0, (int) service.getRank(r, 3612345, -1000).getPercent());
		assertEquals(98, (int) service.getRank(r, 3612345, 3665).getPercent());
		assertEquals(98, (int) service.getRank(r, 3612345, 10000).getPercent());
	}

	@Test
	public void testFull() {
		Raid raid = new WorldBoss();
		RaidBoss boss = raid.getBosses().get(4);

		assertEquals(boss.getRaidBossName(), RaidBossName.ColossalMonolith);
		assertEquals(boss.getMode(), Mode.SM);
		assertEquals(boss.getSize(), Size.Eight);

		service.initialize(Config.DEFAULT_SERVER_HOST);
		service.getRank(boss, RankType.DPS, CharacterDiscipline.Darkness, 360001, 2485, (rc) -> {
			assertNotNull(rc);
			assertNull(rc.getReason());
			assertTrue(rc.getPercent() >= 0 && rc.getPercent() <= 100);
			assertEquals(RankType.DPS, rc.getType());
		});

		try {
			service.getRank(boss, RankType.DTPS, CharacterDiscipline.CombatMedic, 360001, 2485, (rc) -> fail("Healer should not have DTPS"));

		} catch (Exception e) {
			// OK, healer should not have DTPS (yet)
		}
		service.getRank(boss, RankType.DTPS, CharacterDiscipline.Defense, 360001, 2485, (rc) -> {
			assertNotNull(rc);
			assertNull(rc.getReason());
			assertTrue(rc.getPercent() > 0 && rc.getPercent() < 100);
			assertEquals(RankType.DTPS, rc.getType());
		});

		service.getRank(boss, RankType.EHPS, CharacterDiscipline.CombatMedic, 360001, 2485, (rc) -> {
			assertNotNull(rc);
			assertNull(rc.getReason());
			assertTrue(rc.getPercent() >= 0 && rc.getPercent() <= 100);
			assertEquals(RankType.EHPS, rc.getType());
		});
	}
}
