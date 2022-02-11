package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.ixale.starparse.domain.CombatChallenge;
import com.ixale.starparse.domain.ValueType;
import com.ixale.starparse.domain.stats.ChallengeStats;
import com.ixale.starparse.ws.RaidCombatMessage;

public class ChallengesPopoutPresenter extends BaseRaidPopoutPresenter {

	private static final int CHALLENGE_TIMEOUT = 60 * 1000;

	private List<CombatChallenge> availableChallenges;
	private ChallengeStats currentChallengeStats;

	private CombatChallenge currentChallenge;
	private Long latestChallengeStart;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.offsetY = DEFAULT_OFFSET_Y;
		this.offsetX = DEFAULT_OFFSET_X + DEFAULT_WIDTH + DEFAULT_OFFSET_X;

		sets.put(ValueType.DAMAGE, new Set());
		sets.put(ValueType.HEAL, new Set());
		sets.put(ValueType.FRIENDLY, new Set());
	}

	@Override
	protected boolean isMessageEligible(final RaidCombatMessage message) {

		if (!super.isMessageEligible(message)) {
			return false;
		}

		if (message.getChallengeStats() == null || message.getChallengeStats().isEmpty()) {
			// nothing to do
			return false;
		}

		// any challenge available?
		if ((streamedCombat.getBoss() == null)
			|| ((availableChallenges = streamedCombat.getBoss().getRaid().getChallenges(streamedCombat.getBoss())) == null)) {
			return false;
		}

		currentChallenge = null;
		// try to map
		challenges: for (int i = 0; i < message.getChallengeStats().size(); i++) {
			for (final CombatChallenge ch: availableChallenges) {
				if (!message.getChallengeStats().get(i).getChallengeName().equals(ch.getChallengeName())) {
					continue;
				}
				if (i < message.getChallengeStats().size() - 1) {
					// not the last one, ensure its not displayed
					currentChallenge = ch;
					sets.get(getSetKey(message)).items.remove(getItemKey(message));
					continue;
				}

				// always pick the latest
				currentChallenge = ch;
				currentChallengeStats = message.getChallengeStats().get(i);
				// bump
				if (latestChallengeStart == null || latestChallengeStart < currentChallengeStats.getTickFrom()) {
					latestChallengeStart = message.getChallengeStats().get(i).getTickFrom();
				}
				break challenges; // we are done
			}
		}

		if (currentChallenge == null) {
			return false;
		}

		if (latestChallengeStart != null && currentChallengeStats.getTickTo() != null
			&& currentChallengeStats.getTickTo() + CHALLENGE_TIMEOUT < latestChallengeStart) {
			// this challenge ended long in a past and a newer set is available, just discard it
			sets.get(getSetKey(message)).items.remove(getItemKey(message));
			return false;
		}

		if (CombatChallenge.Type.FRIENDLY.equals(currentChallenge.getType())
			&& (currentChallengeStats.getDamage() == null || currentChallengeStats.getDamage() <= 0)) {
			// do not display negative challenges unless broken
			currentChallenge = null;
			currentChallengeStats = null;
			return false;
		}

		return true;
	}

	@Override
	protected ValueType getSetKey(RaidCombatMessage message) {
		switch (currentChallenge.getType()) {
		case HEALING:
			return ValueType.HEAL;
		case FRIENDLY:
			return ValueType.FRIENDLY;
		case DAMAGE:
		default:
			return ValueType.DAMAGE;
		}
	}

	@Override
	protected Integer getMinValueTotal() {
		// display always
		return 0;
	}

	@Override
	protected Integer getValueTotal(final RaidCombatMessage message) {
		switch (currentChallenge.getType()) {
		case HEALING:
			return currentChallengeStats.getEffectiveHeal();
		case DAMAGE:
		default:
			return currentChallengeStats.getDamage();
		}
	}

	@Override
	protected Integer getValuePerSecond(final RaidCombatMessage message) {
		switch (currentChallenge.getType()) {
		case HEALING:
			return currentChallengeStats.getEhps();
		case DAMAGE:
		default:
			return currentChallengeStats.getDps();
		}
	}

	@Override
	public void resetCombatStats() {
		latestChallengeStart = 0L;
		currentChallenge = null;
		super.resetCombatStats();
	}
}
