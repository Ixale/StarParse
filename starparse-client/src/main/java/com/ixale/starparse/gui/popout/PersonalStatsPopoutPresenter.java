package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatChallenge;
import com.ixale.starparse.domain.ValueType;
import com.ixale.starparse.domain.stats.ChallengeStats;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.domain.stats.DamageDealtStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.main.RaidPresenter;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class PersonalStatsPopoutPresenter extends BasePopoutPresenter {

	private static final String MODE_DAMAGE = "damage", MODE_HEALING = "healing";

	@FXML
	private VBox statsGrid;

	// FIXME: duplicates MainPresenter
	@FXML
	private Label apm, time, dps, damage, hps, heal, ehps, ehpsPercent, tps, threat, aps, absorbed, dtps, damageTaken,
		hpsTaken, healTaken, ehpsTaken, ehpsTakenPercent, logTime,
		apm2, time2, dps2, damage2, critTotal2, crit2,
		apm3, time3, hps3, heal3, ehps3, ehpsPercent3, shield3, sps3;

	@FXML
	private GridPane modeDamage, modeHealing, modeAll;

	@FXML
	private AnchorPane statsWrapper;

	List<CombatChallenge> availableChallenges;
	CombatChallenge currentChallenge, lastChallenge;
	List<ChallengeStats> availableChallengeStats;
	ChallengeStats currentChallengeStats;
	final HashMap<CombatChallenge, AnchorPane> challenges = new HashMap<CombatChallenge, AnchorPane>();

	protected RaidPresenter raidPresenter;

	public void setRaidPresenter(final RaidPresenter raidPresenter) {
		this.raidPresenter = raidPresenter;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.offsetY = 790;
		this.height = 211;

		addMode(new Mode(Mode.DEFAULT, "Personal", 211, modeAll));
		addMode(new Mode(MODE_DAMAGE, "Damage", 116, modeDamage));
		addMode(new Mode(MODE_HEALING, "Healing", 139, modeHealing));
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		if (MODE_DAMAGE.equals(getMode().mode)) {
			refreshDamageStats(combat, stats);
		} else if (MODE_HEALING.equals(getMode().mode)) {
			refreshHealingStats(combat, stats);
		} else {
			refreshAllStats(combat, stats);
		}

		// any challenge available?
		if ((combat.getBoss() == null)
			|| ((availableChallenges = combat.getBoss().getRaid().getChallenges(combat.getBoss())) == null)) {
			return;
		}

		currentChallenge = null;
		availableChallengeStats = eventService.getCombatChallengeStats(combat, context.getCombatSelection(), context.getSelectedPlayer());
		if (availableChallengeStats != null && !availableChallengeStats.isEmpty()) {
			// try to map
			for (final ChallengeStats chStats: availableChallengeStats) {
				for (final CombatChallenge ch: availableChallenges) {
					if (chStats.getChallengeName().equals(ch.getChallengeName())) {
						currentChallenge = ch;
						currentChallengeStats = chStats;
					}
				}
			}
		}

		if (currentChallenge == null) {
			if (lastChallenge != null) {
				removeChallenge(lastChallenge);
				lastChallenge = null;
			}
			return;
		}

		if (CombatChallenge.Type.FRIENDLY.equals(currentChallenge.getType())
			&& (currentChallengeStats.getDamage() == null || currentChallengeStats.getDamage() <= 0)) {
			// do not display negative challenges unless broken
			currentChallenge = null;
			currentChallengeStats = null;
			return;
		}

		if (lastChallenge != null) {
			if (!lastChallenge.getPhaseName().equals(currentChallenge.getPhaseName())) {
				removeChallenge(lastChallenge);
				addChallenge(currentChallenge);
			} else {
				// nothing to do
			}
		} else {
			addChallenge(currentChallenge);
		}

		updateChallenge(currentChallenge, currentChallengeStats);

		lastChallenge = currentChallenge;
	}

	private void refreshAllStats(final Combat combat, final CombatStats stats) throws Exception {
		apm.setText(Format.formatFloat(stats.getApm()));
		time.setText(Format.formatTime(stats.getTick()));

		dps.setText(Format.formatAdaptive(stats.getDps()));
		damage.setText(Format.formatMillions(stats.getDamage()));

		hps.setText(Format.formatAdaptive(stats.getHps()));
		heal.setText(Format.formatMillions(stats.getHeal()));
		ehps.setText(Format.formatAdaptive(stats.getEhps()));
		ehpsPercent.setText(Format.formatNumber(stats.getEhpsPercent()) + " %");

		tps.setText(Format.formatAdaptive(stats.getTps()));
		threat.setText(Format.formatMillions(stats.getThreat()));

		aps.setText(Format.formatAdaptive(stats.getAps()));
		absorbed.setText(Format.formatMillions(stats.getAbsorbed()));

		dtps.setText(Format.formatAdaptive(stats.getDtps()));
		damageTaken.setText(Format.formatMillions(stats.getDamageTaken()));

		healTaken.setText(Format.formatMillions(stats.getHealTaken()));
		hpsTaken.setText(Format.formatAdaptive(stats.getHpsTaken()));
		ehpsTaken.setText(Format.formatAdaptive(stats.getEhpsTaken()));
		ehpsTakenPercent.setText(Format.formatNumber(Math.round(100d * stats.getEhpsTaken() / stats.getHpsTaken())) + " %");
	}

	private void refreshDamageStats(final Combat combat, final CombatStats stats) throws Exception {
		apm2.setText(Format.formatFloat(stats.getApm()));
		time2.setText(Format.formatTime(stats.getTick()));

		dps2.setText(Format.formatAdaptive(stats.getDps()));
		damage2.setText(Format.formatAdaptive(stats.getDamage()));

		final DamageDealtStats dds = eventService.getDamageDealtStatsSimple(combat, context.getCombatSelection(), context.getSelectedPlayer()).get(0);
		critTotal2.setText(Format.formatNumber(dds.getTotalNormal() > 0
			? ((dds.getTotalCrit() * 100.0) / (dds.getTotalNormal() + dds.getTotalCrit()))
			: (dds.getTotalCrit() > 0 ? 100 : 0)) + " %");
		crit2.setText(Format.formatNumber(dds.getPercentCrit()) + " %");
	}

	private void refreshHealingStats(final Combat combat, final CombatStats stats) throws Exception {
		apm3.setText(Format.formatFloat(stats.getApm()));
		time3.setText(Format.formatTime(stats.getTick()));

		hps3.setText(Format.formatAdaptive(stats.getHps()));
		heal3.setText(Format.formatAdaptive(stats.getHeal()));
		ehps3.setText(Format.formatAdaptive(stats.getEhps()));
		ehpsPercent3.setText(Format.formatNumber(stats.getEhpsPercent()) + " %");

		int[] shielding = raidPresenter.getShieldingSelf(stats.getTick());
		if (shielding != null) {
			shield3.setText(Format.formatAdaptive(shielding[0]));
			sps3.setText(Format.formatAdaptive(shielding[1]));
		} else {
			shield3.setText("");
			sps3.setText("");
		}
	}

	@Override
	public void resetCombatStats() {
		apm.setText("");
		time.setText("");

		dps.setText("");
		damage.setText("");

		hps.setText("");
		heal.setText("");
		ehps.setText("");
		ehpsPercent.setText("");

		tps.setText("");
		threat.setText("");

		aps.setText("");
		absorbed.setText("");

		dtps.setText("");
		damageTaken.setText("");
		healTaken.setText("");
		hpsTaken.setText("");
		ehpsTaken.setText("");
		ehpsTakenPercent.setText("");

		// damage
		apm2.setText("");
		time2.setText("");

		dps2.setText("");
		damage2.setText("");
		critTotal2.setText("");
		crit2.setText("");

		// healing
		apm3.setText("");
		time3.setText("");

		hps3.setText("");
		heal3.setText("");
		ehps3.setText("");
		ehpsPercent3.setText("");

		shield3.setText("");
		sps3.setText("");

		// challenges
		for (final CombatChallenge ch: challenges.keySet()) {
			statsGrid.getChildren().remove(challenges.get(ch));
		}
		challenges.clear();

		currentChallenge = lastChallenge = null;
		availableChallenges = null;
		availableChallengeStats = null;
		currentChallengeStats = null;
	}

	@Override
	public void setTextColor(Color color) {
		super.setTextColor(color);

		setLabelColor(color);
	}

	private void setLabelColor(Color color) {
		for (Mode m: getModes()) {
			for (final Node n: ((GridPane) m.wrapper).getChildren()) {
				if (n instanceof Text) {
					((Text) n).setFill(color);
				}
			}
		}
	}

	private Color getBarColor(final CombatChallenge challenge) {
		switch (challenge.getType()) {
		case HEALING:
			return barColors.get(ValueType.HEAL);
		case FRIENDLY:
			return barColors.get(ValueType.FRIENDLY);
		case DAMAGE:
		default:
			return barColors.get(ValueType.DAMAGE);
		}
	}

	private void addChallenge(final CombatChallenge challenge) {

		final AnchorPane pane = new AnchorPane();

		final Color fillColor = getBarColor(challenge);

		final Rectangle bar = new Rectangle(0, 0, ITEM_WIDTH, ITEM_HEIGHT);
		bar.setFill(fillColor);
		bar.setVisible(this.bars);
		bar.setOpacity(this.opacity);

		final Label title = new Label(challenge.getChallengeName().getFullName());
		title.setTextFill(this.textColor);
		title.setMaxWidth(66);

		AnchorPane.setTopAnchor(title, 0d);
		AnchorPane.setBottomAnchor(title, 0d);
		AnchorPane.setLeftAnchor(title, 5d);

		final Label total = new Label("");
		total.setTextFill(this.textColor);

		AnchorPane.setTopAnchor(total, 0d);
		AnchorPane.setRightAnchor(total, (double) LABEL_WIDTH);
		AnchorPane.setBottomAnchor(total, 0d);

		final Label ps = new Label("");
		ps.setTextFill(this.textColor);
		ps.setAlignment(Pos.CENTER_RIGHT);

		AnchorPane.setTopAnchor(ps, 0d);
		AnchorPane.setRightAnchor(ps, 5d);
		AnchorPane.setBottomAnchor(ps, 0d);

		pane.setPadding(new Insets(-1, 0, 0, 0));
		pane.getChildren().addAll(bar, title, total, ps);

		challenges.put(challenge, pane);

		statsGrid.getChildren().add(pane);
	}

	private void updateChallenge(final CombatChallenge challenge, final ChallengeStats challengeStats) {

		final Label total = (Label) challenges.get(challenge).getChildren().get(2);
		final Label ps = (Label) challenges.get(challenge).getChildren().get(3);

		switch (challenge.getType()) {
		case HEALING:
			total.setText(Format.formatThousands(challengeStats.getEffectiveHeal()));
			ps.setText(Format.formatAdaptive(challengeStats.getEhps()));
			break;
		case DAMAGE:
		default:
			total.setText(Format.formatThousands(challengeStats.getDamage()));
			ps.setText(Format.formatAdaptive(challengeStats.getDps()));
			break;
		}
	}

	private void removeChallenge(final CombatChallenge challenge) {
		statsGrid.getChildren().remove(challenges.get(challenge));

		challenges.remove(challenge);
	}

	@Override
	public void repaint(Object source) {
		super.repaint(source);

		setLabelColor(textColor);

		for (final CombatChallenge challenge: challenges.keySet()) {
			final AnchorPane pane = challenges.get(challenge);
			final Rectangle bar = ((Rectangle) pane.getChildren().get(0));

			if (this.bars) {
				// showing bars
				bar.setOpacity(opacity);
				bar.setFill(getBarColor(challenge));
				bar.setVisible(true);

			} else {
				bar.setVisible(false);
			}

			((Label) pane.getChildren().get(1)).setTextFill(this.textColor);
			((Label) pane.getChildren().get(2)).setTextFill(this.textColor);
			((Label) pane.getChildren().get(3)).setTextFill(this.textColor);
		}
	}

	@Override
	protected void setHeight(int height) {
		statsWrapper.setPrefHeight(height - 20);
		super.setHeight(height);
	}
}
