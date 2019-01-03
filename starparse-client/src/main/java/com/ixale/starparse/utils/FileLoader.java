package com.ixale.starparse.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.stats.CombatEventStats;
import com.ixale.starparse.service.ParselyService.ParselyCombatInfo;
import com.ixale.starparse.service.impl.Context;

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

	public static final void extractZip(final File sourceZip, final File destinationDir) throws Exception {

		if (sourceZip == null || !sourceZip.isFile() || !sourceZip.canRead()) {
			throw new Exception("Unable to read source file: " + sourceZip);
		}
		if (!destinationDir.exists()) {
			destinationDir.mkdir();
		}

		byte[] buffer = new byte[1024];
		FileInputStream fis = null;
		ZipInputStream zis = null;
		FileOutputStream fos = null;
		ZipEntry ze = null;
		try {
			fis = new FileInputStream(sourceZip);
			zis = new ZipInputStream(fis);
			ze = zis.getNextEntry();

			while (ze != null) {
				final File newFile = new File(destinationDir, ze.getName());
				if (!newFile.exists()) {
					if (ze.isDirectory()) {
						newFile.mkdirs();
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
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception ignored) {

				}
			}
		}
	}

	// TODO: decouple & move somewhere else?
	public static String extractCombats(final String fileName,
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
			return extractPortion(f, null, null);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Slicing combat log file " + f + ": " + selectedCombats.size() + " / " + allCombats.size());
		}

		Long from = null, to = null;
		try {
			// selected combats
			final StringBuilder sb = new StringBuilder();

			for (int i = 0; i < allCombats.size(); i++) {
				final Combat combat = allCombats.get(i);

				// bound by the next start (or EOF)
				to = ((i + 1) < allCombats.size()) ? allCombats.get(i + 1).getTimeFrom() : null;

				for (final Combat c: selectedCombats) {
					if (c == null) {
						// selection no longer valid, can happen 
						// FIXME: investigate
						continue;
					}
					if (c.getCombatId() == combat.getCombatId()) {
						if (logger.isDebugEnabled()) {
							logger.debug("Extracting combat [" + i + "][" + from + "][" + to + "]: " + combat);
						}
						sb.append(extractPortion(f, from != null ? "[" + sdf.format(from) : null, to != null ? "[" + sdf.format(to) : null));
					}
				}

				// bound by this end
				from = combat.getTimeTo();
			}

			if (sb.length() == 0) {
				throw new Exception("No selected combats found");
			}

			fillParselyCombatsInfo(selectedCombats, combatsInfo, context);
			return sb.toString();

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
			for (final Combat c: combats) {
				if (c == null) {
					continue;
				}
				final ParselyCombatInfo info = new ParselyCombatInfo();
				info.from = c.getTimeFrom();
				info.to = c.getTimeTo();
				info.raidBoss = c.getBoss();
				if (info.raidBoss != null && Raid.Mode.NiM.equals(info.raidBoss.getMode())) {
					for (final CombatEventStats e: context.getCombatEvents(c.getCombatId())) {
						if (Event.Type.NIM_CRYSTAL.equals(e.getType())) {
							info.isNiMCrystal = true;
						}
					}
				}
				combatsInfo.add(info);
			}
		} catch (Exception e) {
			logger.error("Unable to assemble combats info for Parsely: " + e.getMessage(), e);
		}
	}

	public static String extractPortion(final File file, final String from, final String to) throws Exception {

		final StringBuilder sb = new StringBuilder();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));

			String line;
			boolean isReading = (from == null);
			while ((line = br.readLine()) != null) {
				if (isReading) {
					if (to != null && line.startsWith(to)) {
						// exclusive, i.e. do no read last line
						// were done
						break;
					}

					// append
					sb.append(line).append("\r\n"); // TODO: always?
				}

				if (!isReading && (from == null || line.startsWith(from))) {
					// exclusive, i.e. do no read first line
					isReading = true;
				}
			}

		} catch (Exception e) {
			throw new Exception("Unable to extract: " + e.getMessage(), e);

		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception ignored) {

				}
			}
		}
		return sb.toString();
	}
}
