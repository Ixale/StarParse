package com.ixale.starparse.gui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ixale.starparse.domain.Ranking;
import com.ixale.starparse.domain.stats.AbsorptionStats;
import com.ixale.starparse.domain.stats.ChallengeStats;
import com.ixale.starparse.domain.stats.CombatEventStats;
import com.ixale.starparse.domain.stats.RaidCombatStats;
import com.ixale.starparse.ws.RaidCombatMessage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class Marshaller {

	private static final Logger logger = LoggerFactory.getLogger(Marshaller.class);

	private static final XStream xmlReader = new XStream(new StaxDriver());

	static {
		// raiding
		xmlReader.alias("raidCombatStats", RaidCombatStats.class);
		xmlReader.alias("raidCombatMessage", RaidCombatMessage.class);
		xmlReader.alias("absorb", AbsorptionStats.class);
		xmlReader.alias("challenge", ChallengeStats.class);
		xmlReader.alias("combatEvent", CombatEventStats.class);
		// rankings
		xmlReader.alias("pct", Ranking.Percentile.class);
		xmlReader.alias("description", Ranking.Description.class);
		xmlReader.useAttributeFor(Ranking.Percentile.class, "p");
		xmlReader.useAttributeFor(Ranking.Percentile.class, "v");
		xmlReader.ignoreUnknownElements();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T loadFromFile(String fileName) {
		final File f = new File(fileName);
		if (f.exists()) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Data loaded from " + fileName);
				}

				return (T) xmlReader.fromXML(f);

			} catch (Exception e) {
				if (StarparseApp.CONFIG_FILE.equals(fileName)) {
					logger.warn("Unable to load configuration file (corrupted)");
					return null;
				}
				logger.warn("Unable to load data from " + fileName, e);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T loadFromString(String xmlContent) {
		try {
			return (T) xmlReader.fromXML(xmlContent);

		} catch (Exception e) {
			logger.warn("Unable to load data", e);
			return null;
		}
	}

	public static boolean storeToFile(final Serializable object, String fileName) {
		final File f = new File(fileName + ".tmp");

		try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
			 OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
			 FileOutputStream fos = new FileOutputStream(f)) {

			if (object instanceof SerializeCallback) {
				((SerializeCallback) object).beforeSerialize();
			}
			xmlReader.marshal(object, new PrettyPrintWriter(writer));

			stream.writeTo(fos);
			stream.close();
			fos.close();

			try {
				Files.move(f.toPath(), new File(fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception ignored) {
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Data from " + object + " saved into " + fileName);
			}
			return true;

		} catch (FileNotFoundException e) {
			logger.warn("Unable to store the data, it appears you are running two Star Parse windows simultaneously");
			return false;

		} catch (IOException e) {
			logger.warn("Unable to store the data, please check disk capacity");
			return false;

		} catch (Exception e) {
			logger.error("Unable save " + object + " into " + fileName + ": " + e.getMessage(), e);
			return false;

		}
	}

	public static String storeToString(final Serializable object) throws Exception {
		try (StringWriter writer = new StringWriter()) {
			if (object instanceof SerializeCallback) {
				((SerializeCallback) object).beforeSerialize();
			}
			xmlReader.marshal(object, new PrettyPrintWriter(writer));

			return writer.toString();
		}
	}

	public interface SerializeCallback {

		void beforeSerialize();
	}
}
