package com.ixale.starparse.gui.popout;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatActorState;
import com.ixale.starparse.domain.LocationInfo;
import com.ixale.starparse.domain.NpcType;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.timer.CustomTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.ResourceBundle;

public class RaidBossPopoutPresenter extends BasePopoutPresenter {

	@FXML
	protected VBox itemsGrid;

	final static protected int ACTOR_WIDTH = 150;

	protected final HashMap<CombatActorState, AnchorPane> combatActorStates = new HashMap<>();

	@SuppressWarnings("PointlessArithmeticExpression")
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.name = this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().length() - 15);
		this.offsetX = DEFAULT_OFFSET_X + DEFAULT_WIDTH + 25; // Actors B
		this.offsetY = 600; // Raid TPS
		this.itemGap = 1;
		this.itemHeight = ITEM_HEIGHT;
		this.height = (TITLE_HEIGHT + 4 * (itemHeight + itemGap));
		this.minH = height - 1 * (itemHeight + itemGap);
		this.maxH = height + 5 * (itemHeight + itemGap);
		this.resizeStepW = (itemWidth + 0);
		this.resizeStepH = (itemHeight + itemGap);

		resetActors();
	}

	private Color getColor(CombatActorState cas) {
		switch (cas.getNpc().getType()) {
			case boss_1:
				return Color.GOLD;
			case boss_2:
				return Color.CRIMSON;
			case boss_3:
				return Color.VIOLET;
			case boss_4:
				return Color.PURPLE;
			case boss_raid:
			default:
				return Color.RED;
		}
	}

	private AnchorPane addCombatActor(final CombatActorState cas) {

		final AnchorPane pane = new AnchorPane();

		final Rectangle barBack = new Rectangle(0, 0, ACTOR_WIDTH, this.itemHeight);
		barBack.setFill(getColor(cas).deriveColor(0, 1, .5, 1));
		barBack.setVisible(this.bars);
		barBack.setOpacity(this.opacity);

		final Rectangle bar = new Rectangle(0, 0, 0, this.itemHeight);
		bar.setFill(getColor(cas));
		bar.setVisible(this.bars);
		bar.setOpacity(this.opacity);

		final Label title = new Label("");
		title.setTextFill(this.textColor);
		title.setFont(Font.font("System", 11));

		AnchorPane.setTopAnchor(title, 0d);
		AnchorPane.setBottomAnchor(title, 0d);
		AnchorPane.setLeftAnchor(title, 5d);

		final Label perc = new Label("");
		perc.setTextFill(this.textColor);
		perc.setFont(Font.font("System", 12));

		AnchorPane.setTopAnchor(perc, 0d);
		AnchorPane.setRightAnchor(perc, 5d);
		AnchorPane.setBottomAnchor(perc, 0d);

		pane.getChildren().addAll(barBack, bar, title, perc);
//		pane.setUserData(System.currentTimeMillis());

		combatActorStates.put(cas, pane);

		itemsGrid.getChildren().add(pane);
		return pane;
	}

	public void updateCombatActor(final CombatActorState cas, long tick) {

		final boolean isVisible;
		if (cas.getCurrentHp() <= 0) {
			isVisible = tick - cas.getTick() < 5 * 1000;
		} else if (!NpcType.boss_raid.equals(cas.getNpc().getType())) {
			isVisible = tick - cas.getTick() < 30 * 1000;
		} else {
			isVisible = true;
		}

		final double pct = cas.getCurrentHp() * 1.0 / cas.getMaxHp();
		if (!isVisible || (cas.getNpc().getHidePct() != null && cas.getNpc().getHidePct() >= pct)) {
			removeActor(cas);
			return;
		}

		AnchorPane pane = combatActorStates.get(cas);
		if (pane == null) {
			if (cas.getCurrentHp() <= 0) {
				return;
			}
			final double reqSize = (combatActorStates.size() + 1) * (itemHeight + itemGap);
			if (popoutRoot.getHeight() - popoutTitle.getHeight() < reqSize) {
				return;
			}
			pane = addCombatActor(cas);
		}

		if (this.bars) {
			final Rectangle rect = (Rectangle) pane.getChildren().get(1);
			rect.setWidth(ACTOR_WIDTH * Math.max(0, Math.min(1, pct)));
		}

		final Label title = (Label) pane.getChildren().get(2);
		title.setText(getFullActorLabel(cas));

		final Label perc = (Label) pane.getChildren().get(3);
		perc.setText(Format.formatPercent(pct) + " %");
	}

	public void removeActor(CombatActorState cas) {
		final AnchorPane pane = combatActorStates.get(cas);
		if (pane == null) {
			return;
		}

		itemsGrid.getChildren().remove(pane);

		combatActorStates.remove(cas);
	}

	public void resetActors() {
		combatActorStates.clear();
		itemsGrid.getChildren().clear();
		if (hasPopout()) {
			getPopout().bringToFront();
		}
	}

	private String getFullActorLabel(final CombatActorState cas) {
		if (cas.getNpc().getName() != null) {
			return cas.getNpc().getName();
		}
		return CustomTimer.getShortName(cas.getActor().getName(), 18);
	}

	@Override
	public void setTextColor(final Color color) {
		super.setTextColor(color);
	}

	@Override
	public void repaint(Object source) {
		super.repaint(source);

		while (popoutRoot.getHeight() - popoutTitle.getHeight() < (combatActorStates.size() * (itemHeight + itemGap))) {
			combatActorStates.values().remove((AnchorPane) itemsGrid.getChildren().remove(itemsGrid.getChildren().size() - 1));
		}

		for (final CombatActorState cas : combatActorStates.keySet()) {
			final AnchorPane pane = combatActorStates.get(cas);
			final Rectangle back = ((Rectangle) pane.getChildren().get(0));
			final Rectangle bar = ((Rectangle) pane.getChildren().get(1));

			if (this.bars) {
				// showing bars
				back.setOpacity(opacity);
				back.setFill(getColor(cas).deriveColor(0, 1, .5, 1));
				back.setVisible(true);

				final double pct = cas.getCurrentHp() * 1.0 / cas.getMaxHp();

				bar.setOpacity(opacity);
				bar.setFill(getColor(cas));
				bar.setWidth(ACTOR_WIDTH * Math.max(0, Math.min(1, pct)));
				bar.setVisible(true);

			} else {
				back.setVisible(false);
				bar.setVisible(false);
			}

			((Label) pane.getChildren().get(2)).setTextFill(this.textColor);
			((Label) pane.getChildren().get(3)).setTextFill(this.textColor);
		}
	}

	@Override
	protected void refreshCombatStats(Combat combat, CombatStats stats) throws Exception {
		setTitle(combat);

		final Collection<CombatActorState> combatActorStates = context.getCombatActorStates(combat);
		if (combatActorStates == null || combatActorStates.isEmpty()) {
			resetActors();
		} else {
			combatActorStates.forEach((item) -> this.updateCombatActor(item, stats.getTick()));
		}
	}

	@Override
	public void resetCombatStats() {
		setTitle(null);
		resetActors();
	}

	private void setTitle(final Combat combat) {
		if (combat != null && combat.getBoss() != null) {
			popoutTitle.setText(getShortNameForTitle(combat.getBoss().getRaidBossName().getShortName())
					+ " " + combat.getBoss().getMode()
					+ " " + combat.getBoss().getSize());
			return;
		}
		final LocationInfo locationInfo;
		if (combat != null && context.getCombatInfo().get(combat.getCombatId()) != null) {
			locationInfo = context.getCombatInfo().get(combat.getCombatId()).getLocationInfo();
		} else {
			locationInfo = context.getLocationInfo();
		}
		if (locationInfo != null) {
			final String zone = getShortNameForTitle(locationInfo.getInstanceName());
			popoutTitle.setText(zone == null || zone.isEmpty() ? "" : (zone + " ")
					+ locationInfo.getInstanceDifficulty());
		} else {
			popoutTitle.setText("Raid Boss");
		}
	}

	private String getShortNameForTitle(final String n) {
		if (n.contains("Dxun")) {
			return "Dxun";
		}
		if (n.contains("Machine Gods")) {
			return "Machine Gods";
		}
		return CustomTimer.getShortName(n, 15);
	}

}
