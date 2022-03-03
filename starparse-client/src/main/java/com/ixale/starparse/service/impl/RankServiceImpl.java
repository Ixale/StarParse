package com.ixale.starparse.service.impl;

import com.ixale.starparse.domain.CharacterClass;
import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.Raid.Mode;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.RankClass;
import com.ixale.starparse.domain.RankClass.Reason;
import com.ixale.starparse.domain.Ranking;
import com.ixale.starparse.domain.Ranking.Percentile;
import com.ixale.starparse.gui.Marshaller;
import com.ixale.starparse.service.RankService;
import com.ixale.starparse.utils.FileDownloader;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service("rankService")
public class RankServiceImpl implements RankService {

	private static final Logger logger = LoggerFactory.getLogger(RankService.class);

	private final Map<String, Ranking> rankings = new HashMap<>();

	private String host;

	private static ExecutorService rankingExecutor;

	@Override
	public void initialize(String host) {
		this.host = host;
	}

	@Override
	public void getRank(final RaidBoss boss, final RankType type, final CharacterDiscipline discipline, int tick, int value,
			Consumer<RankClass> callback) {
		getRanking(boss, type, discipline, (ranking) -> {
			callback.accept(getRank(ranking, tick, value));
		});
	}

	private void getRanking(final RaidBoss boss, final RankType type, final CharacterDiscipline discipline,
			final Consumer<Ranking> callback) {
		final String key = buildKey(boss, type, discipline);

		if (!rankings.containsKey(key)) {
			if (rankingExecutor == null) {
				rankingExecutor = Executors.newSingleThreadExecutor(r -> {
					final Thread worker = new Thread(r, "Ranking Worker");
					worker.setDaemon(true);
					return worker;
				});
			}
			rankingExecutor.execute(() -> {
				try {
					// build remote URL
					final URL url = buildUrl(boss, host, key);
					// fetch from remote
					final String content = FileDownloader.fetchFile(url);
					// parse
					final Ranking ranking = readRanking(content);
					// cache
					rankings.put(key, ranking);
					Platform.runLater(() -> {
						callback.accept(rankings.get(key));
					});

					if (logger.isDebugEnabled()) {
						logger.debug("Ranking fetched from remote (" + boss + ", " + type + ", " + discipline + "): " + ranking);
					}
				} catch (Exception e) {
					if (e.getMessage().equals("Read timed out") || e.getMessage().startsWith("Server returned non-OK status: 404")) {
						// local issue, silently ignore
						return;
					}
					logger.error("Failed to rank " + key + ": " + e.getMessage(), e);
				}
			});
			return;
		}

		callback.accept(rankings.get(key));
	}

	public String buildKey(final RaidBoss boss, final RankType type, final CharacterDiscipline discipline) {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.name().toLowerCase()).append("_");
		sb.append(boss.getRaidBossName().name()).append("_");
		if (Mode.NiM.equals(boss.getMode()) && !RaidBossName.HatefulEntity.equals(boss.getRaidBossName())) {
			sb.append(Mode.HM).append("_"); // temporarily
		} else {
			sb.append(boss.getMode().toString()).append("_");
		}
		sb.append(boss.getSize().toString()).append("_");
		sb.append(getClassUnified(discipline).name());
		return sb.toString();
	}

	public URL buildUrl(final RaidBoss boss, final String host, final String key) throws Exception {
		if (host == null || host.isEmpty()) {
			throw new IllegalStateException("Host not set");
		}
		//noinspection HttpUrlsUsage
		final URL ws = new URL("http://" + host);
		return new URL(ws.getProtocol(), ws.getHost(), 80, "/" + RankService.RANK_URL + "/" + boss.getRaidBossName().name() + "/" + key + ".xml");
	}

	public Ranking readRanking(final String content) {
		if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Content is empty");
		}
		return Marshaller.loadFromString(content);
	}

	public RankClass getRank(final Ranking ranking, int tick, int value) {
		if (ranking == null) {
			throw new IllegalArgumentException("No ranking set");
		}

		final RankClass rank = new RankClass(ranking.getType());
		if (ranking.getPercentiles() == null || ranking.getPercentiles().isEmpty() || ranking.getPercentiles().size() < 10) {
			rank.setReason(Reason.NO_DATA_AVAILABLE);
			return rank;
		}

		if (tick < ranking.getMinTick()) {
			rank.setReason(Reason.TICK_TOO_LOW);
			return rank;
		}

		int pct = 0;
		for (final Percentile p : ranking.getPercentiles()) {
			switch (ranking.getType()) {
				case DTPS:
					if (p.getValue() >= value && p.getPercent() > pct) {
						pct = p.getPercent();
					}
					break;
				default:
					if (p.getValue() <= value && p.getPercent() > pct) {
						pct = p.getPercent();
					}
			}

		}
		rank.setPercent(pct);

		return rank;
	}

	private CharacterClass getClassUnified(CharacterDiscipline discipline) {
		switch (discipline.getCharacterClass()) {
			case Assassin:
				return CharacterClass.Shadow;
			case Sorcerer:
				return CharacterClass.Sage;
			case Marauder:
				return CharacterClass.Sentinel;
			case Juggernaut:
				return CharacterClass.Guardian;
			case Operative:
				return CharacterClass.Scoundrel;
			case Sniper:
				return CharacterClass.Gunslinger;
			case Powertech:
				return CharacterClass.Vanguard;
			case Mercenary:
				return CharacterClass.Commando;
			case Commando:
			case Guardian:
			case Gunslinger:
			case Sage:
			case Scoundrel:
			case Sentinel:
			case Shadow:
			case Vanguard:
				return discipline.getCharacterClass();
		}
		throw new IllegalArgumentException("Missing class unification");
	}
}
