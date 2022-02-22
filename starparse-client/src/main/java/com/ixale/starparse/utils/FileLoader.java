package com.ixale.starparse.utils;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatInfo;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.stats.CombatEventStats;
import com.ixale.starparse.service.ParselyService.ParselyCombatInfo;
import com.ixale.starparse.service.impl.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileLoader {

	private static final Logger logger = LoggerFactory.getLogger(FileLoader.class);

	public static final long getZipSize(final File sourceZip) throws Exception {
		if (sourceZip == null || !sourceZip.isFile() || !sourceZip.canRead()) {
			throw new Exception("Unable to read source file: " + sourceZip);
		}
		long size = 0;
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry ze = null;
		try {
			fis = new FileInputStream(sourceZip);
			zis = new ZipInputStream(fis);
			ze = zis.getNextEntry();

			while (ze != null) {
				size += ze.getSize();
				ze = zis.getNextEntry();
			}

		} finally {
			if (zis != null) {
				try {
					zis.closeEntry();
					zis.close();
				} catch (Exception ignored) {

				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception ignored) {

				}
			}
		}
		return size;
	}

	public static void extractZip(final File sourceZip, final File destinationDir) throws Exception {

		if (sourceZip == null || !sourceZip.isFile() || !sourceZip.canRead()) {
			throw new Exception("Unable to read source file: " + sourceZip);
		}
		if (!destinationDir.exists()) {
			if (!destinationDir.mkdir()) {
				throw new Exception("Unable to create icons dir: " + destinationDir);
			}
		}

		byte[] buffer = new byte[1024];
		ZipInputStream zis = null;
		FileOutputStream fos = null;
		ZipEntry ze;
		try (FileInputStream fis = new FileInputStream(sourceZip)) {
			zis = new ZipInputStream(fis);
			ze = zis.getNextEntry();

			while (ze != null) {
				final File newFile = new File(destinationDir, ze.getName());
				if (!newFile.exists() || (newFile.isFile() && newFile.length() == 3162 /* placeholder */)) {
					if (ze.isDirectory()) {
						if (!newFile.mkdir()) {
							throw new Exception("Unable to create icons dir: " + newFile);
						}
					} else {
						try {
							fos = new FileOutputStream(newFile);

							int len;
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}

						} finally {
							if (fos != null) {
								try {
									fos.close();
								} catch (Exception ignored) {

								}
							}
						}
					}
				}
				ze = zis.getNextEntry();
			}

		} finally {
			if (zis != null) {
				try {
					zis.closeEntry();
					zis.close();
				} catch (Exception ignored) {

				}
			}
		}
	}

	// TODO: decouple & move somewhere else?
	public static byte[] extractCombats(final String fileName,
			final List<Combat> allCombats, final List<Combat> selectedCombats,
			final List<ParselyCombatInfo> combatsInfo, final Context context)
			throws Exception {

		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);

		final File f = new File(fileName);
		if (!f.exists()) {
			throw new Exception("Combat log file does not exist anymore");
		}

		if (selectedCombats == null || selectedCombats.isEmpty()) {
			// whole log
			if (logger.isDebugEnabled()) {
				logger.debug("Returning whole combat log file " + f);
			}
			fillParselyCombatsInfo(allCombats, combatsInfo, context);
			return Files.readAllBytes(f.toPath());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Slicing combat log file " + f + ": " + selectedCombats.size() + " / " + allCombats.size());
		}

		final Set<String> areaEnteredLines = new HashSet<>();
		Long from = null, to = null;
		try {
			// selected combats
			final StringBuilder sb = new StringBuilder();

			for (int i = 0; i < allCombats.size(); i++) {
				final Combat combat = allCombats.get(i);

				// bound by the next start (or EOF)
				to = ((i + 1) < allCombats.size()) ? allCombats.get(i + 1).getTimeFrom() : null;

				for (final Combat c : selectedCombats) {
					if (c == null) {
						// selection no longer valid, can happen 
						// FIXME: investigate
						continue;
					}
					if (c.getCombatId() == combat.getCombatId()) {
						if (logger.isDebugEnabled()) {
							logger.debug("Extracting combat [" + i + "][" + from + "][" + to + "]: " + combat);
						}
						final ExtractedCombat ec = extractPortion(f, from != null ? "[" + sdf.format(from) : null, to != null ? "[" + sdf.format(to) : null);
						if (ec.lastAreaEntered != null && !areaEnteredLines.contains(ec.lastAreaEntered)) {
							sb.append(ec.lastAreaEntered).append("\r\n");
							areaEnteredLines.add(ec.lastAreaEntered);
						}
						sb.append(ec.sb);
					}
				}

				// bound by this end
				from = combat.getTimeTo();
			}

			if (sb.length() == 0) {
				throw new Exception("No selected combats found");
			}

			fillParselyCombatsInfo(selectedCombats, combatsInfo, context);
			return sb.toString().getBytes();

		} catch (NullPointerException e) {
			throw new Exception("NPE: [" + from + ", " + to + "], all: " + Arrays.asList(allCombats) + ", sel: " + Arrays.asList(selectedCombats));
		}
	}

	private static void fillParselyCombatsInfo(final List<Combat> combats, final List<ParselyCombatInfo> combatsInfo, final Context context) {
		combatsInfo.clear();
		if (combats == null) {
			return;
		}
		try {
			for (final Combat c : combats) {
				if (c == null) {
					continue;
				}
				final ParselyCombatInfo info = new ParselyCombatInfo();
				info.from = c.getTimeFrom();
				info.to = c.getTimeTo();
				info.raidBoss = c.getBoss();
				if (info.raidBoss != null && Raid.Mode.NiM.equals(info.raidBoss.getMode())) {
					for (final CombatEventStats e : context.getCombatEvents(c.getCombatId(), context.getSelectedPlayer())) {
						if (Event.Type.NIM_CRYSTAL.equals(e.getType())) {
							info.isNiMCrystal = true;
							break;
						}
					}
				}
				final CombatInfo combatInfo = context.getCombatInfo().get(c.getCombatId());
				if (combatInfo != null) {
					info.instanceName = combatInfo.getInstanceName();
					info.instanceGuid = combatInfo.getInstanceGuid();
				}
				combatsInfo.add(info);
			}
		} catch (Exception e) {
			logger.error("Unable to assemble combats info for Parsely: " + e.getMessage(), e);
		}
	}

	public static class ExtractedCombat {
		final public StringBuilder sb = new StringBuilder();
		public String lastAreaEntered = null;
	}

	public static ExtractedCombat extractPortion(final File file, final String from, final String to) throws Exception {

		final ExtractedCombat ec = new ExtractedCombat();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {

			String line;
			boolean isReading = (from == null);
			while ((line = br.readLine()) != null) {
				if (isReading) {
					if (to != null && line.startsWith(to)) {
						// exclusive, i.e. do not read last line
						// were done
						break;
					}

					// append
					ec.sb.append(line).append("\r\n");

				} else if (line.contains("836045448953664")) { // AreaEntered (>= 7.0.0b)
					ec.lastAreaEntered = line;
				}

				if (!isReading && line.startsWith(from)) {
					// exclusive, i.e. do no read first line
					isReading = true;
				}
			}

		} catch (Exception e) {
			throw new Exception("Unable to extract: " + e.getMessage(), e);

		}
		return ec;
	}
}
