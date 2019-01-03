package com.ixale.starparse.upload;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ixale.starparse.domain.ServerName;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.service.ParselyService;
import com.ixale.starparse.service.ParselyService.Params;
import com.ixale.starparse.service.ParselyService.ParselyCombatInfo;
import com.ixale.starparse.service.impl.ParselyServiceImpl;

public class ParselyTest {

	private ParselyService service;
	private byte[] content;
	private String fileName = "combat14.txt";
	private List<ParselyCombatInfo> combatsInfo = new ArrayList<>();

	public ParselyTest() {
		service = new ParselyServiceImpl();

		try {
			content = Files.readAllBytes(new File(getClass().getClassLoader().getResource(fileName).toURI()).toPath());
		} catch (Exception e) {
			throw new RuntimeException("Missing " + fileName, e);
		}
	}

	private Params getParams() {
		Params p = new Params();

		p.endpoint = Config.PARSELY_UPLOAD_API;
		p.serverName = ServerName.TombOfFreedonNadd.getName();
		p.timezone = "Europe/Prague";
		p.guild = "Ixale and Friends (tm)";
		p.isPublic = false;
		p.notes = "test\nsecond\r\nthird +ìšèøžý";
		p.username = "Ixale";
		p.password = "mariparse";

		return p;
	}

	@Test
	public void testMissingLog() throws Exception {

		try {
			service.uploadLog(getParams(), fileName, null, combatsInfo);
			fail();
		} catch (Exception e) {
			assertEquals("Parsely returned error: Empty File", e.getMessage());
		}
	}

	@Test
	public void testInvalidUser() throws Exception {

		Params p = getParams();
		p.username = "foobar";

		try {
			service.uploadLog(p, fileName, content, combatsInfo);
			fail();
		} catch (Exception e) {
			assertEquals("Parsely returned error: Invalid user", e.getMessage());
		}
	}

	//@Test
	public void testUploadAnonymous() throws Exception {

		Params p = getParams();
		p.username = null;

		final String url = service.uploadLog(p, fileName, content, combatsInfo);
		assertNotNull(url);
		assertTrue(url.contains("/parser/view/"));
		System.out.println(url);
	}

	@Test
	public void testUploadLogged() throws Exception {

		Params p = getParams();

		final String url = service.uploadLog(p, fileName, content, combatsInfo);
		assertNotNull(url);
		assertTrue(url.contains("/parser/view/"));
		System.out.println(url);
	}
}
