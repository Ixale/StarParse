package com.ixale.starparse.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Output;
import com.ixale.starparse.domain.RaidRequest;
import com.ixale.starparse.domain.stats.CombatLogStats;
import com.ixale.starparse.domain.stats.RaidCombatStats;
import com.ixale.starparse.gui.Marshaller;
import com.ixale.starparse.serialization.KryoSerialization;
import com.ixale.starparse.service.RaidService;
import com.ixale.starparse.ws.RaidCombatMessage;

@Service("raidService")
public class RaidServiceImpl implements RaidService {

	private static final Logger logger = LoggerFactory.getLogger(RaidService.class);

	private static final String STATS_DIR = "stats";
	private static final String STATS_FILE_PATTERN = STATS_DIR + "/%s.v2.xml";
	private static final String REQUEST_FILE_PATTERN = STATS_DIR + "/%s.dat";
	private static final int MAX_STORE_INTERVAL = 2 * 60 * 1000;

	final HashMap<String, HashMap<Integer, HashMap<String, RaidCombatMessage>>> localCache = new HashMap<String, HashMap<Integer, HashMap<String, RaidCombatMessage>>>();

	final HashMap<String, String> fileNames = new HashMap<String, String>();

	private Long lastStoreTime = null;
	private final ArrayList<String> dirtyLogs = new ArrayList<String>();
	private String lastCombatLogName = null;

	public RaidServiceImpl() {
		try {
			File f = new File(STATS_DIR);
			if (!f.exists()) {
				f.mkdir();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void storeCombatUpdate(final String combatLogName, int combatId, RaidCombatMessage message) {

		if (!localCache.containsKey(combatLogName)) {
			localCache.put(combatLogName, new HashMap<Integer, HashMap<String, RaidCombatMessage>>());
		}
		if (!localCache.get(combatLogName).containsKey(combatId)) {
			localCache.get(combatLogName).put(combatId, new HashMap<String, RaidCombatMessage>());
		}

		localCache.get(combatLogName).get(combatId).put(message.getCharacterName(), message);

		dirtyLogs.add(combatLogName);

		if (lastStoreTime == null || ((lastStoreTime + MAX_STORE_INTERVAL) < System.currentTimeMillis())) {
			// continuous saving (will happen anyway upon existing, changing combat or log)
			storeCombatStats(combatLogName);
		}
	}

	@Override
	public Collection<RaidCombatMessage> getCombatUpdates(final String combatLogName, int combatId) {

		if (!localCache.containsKey(combatLogName) || !localCache.get(combatLogName).containsKey(combatId)) {
			if (lastCombatLogName == null || !lastCombatLogName.equals(combatLogName)) {
				// load and try again
				loadCombatStats(combatLogName);
				return getCombatUpdates(combatLogName, combatId);
			}
			return null;
		}
		return localCache.get(combatLogName).get(combatId).values();
	}

	@Override
	public void storeCombatStats(final String combatLogName) {

		if (!localCache.containsKey(combatLogName) || !dirtyLogs.contains(combatLogName)) {
			return;
		}

		final CombatLogStats combatLogStats = new CombatLogStats();
		combatLogStats.setCombatLogName(combatLogName);

		for (Integer combatId: localCache.get(combatLogName).keySet()) {
			final RaidCombatStats stats = new RaidCombatStats();
			stats.setCombatId(combatId);
			stats.getCombatStats().addAll(localCache.get(combatLogName).get(combatId).values());

			combatLogStats.getRaids().add(stats);
		}

		if (Marshaller.storeToFile(combatLogStats, getLocalFileName(combatLogName))) {
			dirtyLogs.remove(combatLogName);
			lastStoreTime = System.currentTimeMillis();
		}
	}

	@Override
	public void loadCombatStats(final String combatLogName) {

		lastCombatLogName = combatLogName;
		final CombatLogStats combatLogStats = Marshaller.loadFromFile(getLocalFileName(combatLogName));

		if (combatLogStats == null) {
			return;
		}

		localCache.put(combatLogName, new HashMap<Integer, HashMap<String, RaidCombatMessage>>());

		for (final RaidCombatStats stats: combatLogStats.getRaids()) {
			final HashMap<String, RaidCombatMessage> messages = new HashMap<String, RaidCombatMessage>();
			for (final RaidCombatMessage message: stats.getCombatStats()) {
				messages.put(message.getCharacterName(), message);
			}
			localCache.get(combatLogName).put(stats.getCombatId(), messages);
		}
	}

	private String getLocalFileName(final String combatLogName) {
		if (!fileNames.containsKey(combatLogName)) {
			File file = new File(combatLogName);
			fileNames.put(combatLogName, String.format(STATS_FILE_PATTERN, file.getName()));
		}
		return fileNames.get(combatLogName);
	}

	private String getLocalFileNameForRequest(final String combatLogName, final RaidRequest request) {
		final String key = combatLogName + "-" + request.getCacheKey();
		if (!fileNames.containsKey(key)) {
			File file = new File(key);
			fileNames.put(key, String.format(REQUEST_FILE_PATTERN, file.getName()));
		}
		return fileNames.get(key);
	}

	@Override
	public <T> T getStoredResponses(String combatLogName, RaidRequest request) {
		// cached response available?
		final File cache = new File(getLocalFileNameForRequest(combatLogName, request));
		if (!cache.exists()) {
			return null;
		}
		try {
			// read & decode
			return decodePayload(Files.readAllBytes(cache.toPath()));

		} catch (Exception e) {
			logger.error("Unable to fetch data from [" + cache + "]: " + e.getMessage());
		}

		return null;
	}

	@Override
	public <T> T decodeAndStoreResponse(String combatLogName, RaidRequest request, byte[] payload) {
		// read the response (fails on error)
		final T response = decodePayload(payload);

		// store for future use
		final File cache = new File(getLocalFileNameForRequest(combatLogName, request));
		try {
			Files.write(cache.toPath(), payload, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		} catch (Exception e) {
			logger.error("Unable to store data to [" + cache + "]: " + e.getMessage());
		}

		return response;
	}

	@Override
	public <T> byte[] encodeResponse(T response) {
		return encodePayload(response);
	}

	@SuppressWarnings("unchecked")
	private <T> T decodePayload(byte[] payload) {
		if (payload == null || payload.length == 0) {
			throw new IllegalArgumentException("Content is empty");
		}

		ByteBufferInput bis = null;
		try {
			bis = new ByteBufferInput(payload);
			return (T) KryoSerialization.getKryo().readClassAndObject(bis);

		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to decode: " + e.getMessage(), e);

		} finally {
			if (bis != null) {
				bis.close();
			}
		}
	}

	private <T> byte[] encodePayload(T response) {
		if (response == null) {
			throw new IllegalArgumentException("Content is empty");
		}

		ByteArrayOutputStream bos = null;
		Output output = null;
		try {
			bos = new ByteArrayOutputStream();
			output = new Output(bos);

			KryoSerialization.getKryo().writeClassAndObject(output, response);
			output.close();

			return bos.toByteArray();

		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to encode: " + e.getMessage(), e);

		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
