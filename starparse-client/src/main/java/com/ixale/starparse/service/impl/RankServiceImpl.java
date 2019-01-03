package com.ixale.starparse.service.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ixale.starparse.domain.CharacterClass;
import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.RankClass;
import com.ixale.starparse.domain.RankClass.Reason;
import com.ixale.starparse.domain.Ranking;
import com.ixale.starparse.domain.Raid.Mode;
import com.ixale.starparse.domain.Ranking.Percentile;
import com.ixale.starparse.gui.Marshaller;
import com.ixale.starparse.service.RankService;
import com.ixale.starparse.utils.FileDownloader;

@Service("rankService")
public class RankServiceImpl implements RankService {

	private static final Logger logger = LoggerFactory.getLogger(RankService.class);

	private final Map<String, Ranking> rankings = new HashMap<>();

	private String host;

	@Override
	public void initialize(String host) {
		this.host = host;
	}

	@Override
	public RankClass getRank(final RaidBoss boss, final RankType type, final CharacterDiscipline discipline, int tick, int value) throws Exception {
		final Ranking ranking = getRanking(boss, type, discipline);
		if (ranking == null) {
			logger.error("Ranking missing: " + boss + ", " + type + ", " + discipline);
			throw new Exception("Unable to load ranking");
		}
		return getRank(ranking, tick, value);
	}

	private Ranking getRanking(final RaidBoss boss, final RankType type, final CharacterDiscipline discipline) throws Exception {
		final String key = buildKey(boss, type, discipline);

		if (!rankings.containsKey(key)) {
			// build remote URL
			final URL url = buildUrl(boss, host, key);
			// fetch from remote
			final String content = FileDownloader.fetchFile(url);
			// parse
			final Ranking ranking = readRanking(content);
			// cache
			rankings.put(key, ranking);

			if (logger.isDebugEnabled()) {
				logger.debug("Ranking fetched from remote (" + boss + ", " + type + ", " + discipline + "): " + ranking);
			}
		}

		return rankings.get(key);
	}

	public String buildKey(final RaidBoss boss, final RankType type, final CharacterDiscipline discipline) {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.name().toLowerCase()).append("_");
		sb.append(boss.getRaidBossName().name()).append("_");
		if (Mode.NiM.equals(boss.getMode()) && !RaidBossName.HatefulEntity.equals(boss.getRaidBossName())) {
			sb.append(Mode.HM.toString()).append("_"); // temporarily
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
		final URL ws = new URL("http://" + host);
		return new URL(ws.getProtocol(), ws.getHost(), 80, "/" + RankService.RANK_URL + "/" + boss.getRaidBossName().name() + "/" + key + ".xml");
	}

	public Ranking readRanking(final String content) {
		if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Content is empty");
		}
		return (Ranking) Marshaller.loadFromString(content);
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
		for (final Percentile p: ranking.getPercentiles()) {
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
