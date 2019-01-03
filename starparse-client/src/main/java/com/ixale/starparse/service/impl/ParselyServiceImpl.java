package com.ixale.starparse.service.impl;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.ServerName;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.service.ParselyService;
import com.ixale.starparse.utils.FileUploader;

@Service("parselyService")
public class ParselyServiceImpl implements ParselyService {

	private static final Logger logger = LoggerFactory.getLogger(ParselyService.class);

	@Override
	public Params createParams(final Config config, boolean isPublic, final String notes) {
		final Params p = new Params();

		p.endpoint = config.getParselyEndpoint();
		p.timezone = config.getTimezone();

		if (config.getCurrentCharacter().getServer() != null) {
			p.serverName = ServerName.getWebalized(config.getCurrentCharacter().getServer());
		}

		if (config.getCurrentCharacter().getGuild() != null) {
			p.guild = config.getCurrentCharacter().getGuild();
		}

		if (notes != null && !notes.isEmpty()) {
			p.notes = notes;
		}

		p.isPublic = isPublic;

		if (config.getParselyLogin() != null) {
			p.username = config.getParselyLogin();
			p.password = config.getParselyPassword();
		}

		return p;
	}

	@Override
	public String uploadLog(final Params p, final String fileName, final byte[] content, final List<ParselyCombatInfo> combatsInfo) throws Exception {

		final FileUploader fu = new FileUploader(p.endpoint, "UTF-8");

		if (p.serverName != null) {
			fu.addFormField("server", p.serverName);
		}

		fu.addFormField("timezone", p.timezone);

		if (p.guild != null) {
			fu.addFormField("guild", p.guild);
		}

		if (p.notes != null) {
			fu.addFormField("notes", p.notes);
		}

		fu.addFormField("public", p.isPublic ? "1" : "0");

		if (p.username != null) {
			fu.addFormField("username", p.username);
			fu.addFormField("password", p.password);
		}

		fu.addFilePart("file", fileName, content);

		if (combatsInfo != null && !combatsInfo.isEmpty()) {
			for (final ParselyCombatInfo info: combatsInfo) {
				if (info.raidBoss == null || RaidBossName.OperationsTrainingDummy.equals(info.raidBoss.getRaidBossName())) {
					continue;
				}
				final StringBuilder sb = new StringBuilder();
				sb.append(info.from).append('|');
				sb.append(Format.formatTime(info.from, true, true)).append('|');
				sb.append(info.to == null ? "" : info.to).append('|');
				sb.append(info.to == null ? "" : Format.formatTime(info.to, true, true)).append('|');
				sb.append(info.raidBoss.getRaidBossName().name()).append('|');
				sb.append(info.raidBoss.getMode().name()).append('|');
				sb.append(info.raidBoss.getSize().toString()).append('|');
				sb.append(info.isNiMCrystal ? "y" : "n");

				fu.addFormField("combats[]", sb.toString());
			}
		}

		String xmlReply = null;
		try {
			logger.debug("Uploading " + fileName);

			try {
				xmlReply = fu.finish();

			} catch (IOException e) {
				// try again
				if (e.getMessage() != null && e.getMessage().contains("Server returned non-OK status")) {
					// try again (may be temporal 504)
					Thread.sleep(5000);
					xmlReply = fu.finish();

				} else {
					throw e;
				}
			}

			return getLink(xmlReply);

		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Upload failed: " + e.getMessage() + " (" + (xmlReply != null ? xmlReply : "no reply") + ")");
			}
			throw e;
		}
	}

	public String getLink(final String xmlReply) throws Exception {

		if (xmlReply == null || xmlReply.isEmpty()) {
			throw new IllegalStateException("Returned empty response from the server");
		}

		if (!xmlReply.contains("<response>")) {
			throw new IllegalStateException("Returned invalid response from the server: "
				+ (xmlReply.length() > 255 ? xmlReply.substring(0, 255) : xmlReply));
		}

		final String status = getValue(xmlReply, "status");
		if (status == null || status.isEmpty()) {
			throw new IllegalStateException("Missing status in response");
		}

		if (!"success".equalsIgnoreCase(status)) {
			// <response><status>error</status><error>Invalid File</error></response>
			throw new IllegalArgumentException("Parsely returned error: " + getValue(xmlReply, "error"));
		}

		final String link = getValue(xmlReply, "file");
		if (link == null || link.isEmpty()) {
			throw new IllegalStateException("Parsely did not return any URL, but the log appears uploaded");
		}

		// <response><status>success</status><file>http://parsely.io/parser/view/19063</file></response>
		logger.info("Uploaded successfully: " + link);

		return link;
	}

	private String getValue(final String xmlReply, final String tag) {
		if (!xmlReply.contains("<" + tag + ">") || !xmlReply.contains("</" + tag + ">")) {
			return null;
		}
		return xmlReply.substring(xmlReply.indexOf("<" + tag + ">") + ("<" + tag + ">").length(), xmlReply.indexOf("</" + tag + ">"));
	}
}
