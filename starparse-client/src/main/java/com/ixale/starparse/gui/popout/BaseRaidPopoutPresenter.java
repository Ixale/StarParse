package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.ValueType;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.main.RaidPresenter;
import com.ixale.starparse.ws.RaidCombatMessage;

abstract public class BaseRaidPopoutPresenter extends BasePopoutPresenter implements RaidPresenter.RaidDataListener {

	final static protected int MIN_DISPLAY_VALUE = 1000;

	@FXML
	protected VBox statsGrid;

	static class Set {

		protected AnchorPane totalPane;
		protected Integer maxValueTotal = 0, sumValueTotal = 0, sumValuePerSecond = 0;

		protected final HashMap<String, AnchorPane> items = new HashMap<>();
	}

	protected final LinkedHashMap<ValueType, Set> sets = new LinkedHashMap<>();

	private final Map<Integer, List<String>> realPlayerNamesPerCombat = new HashMap<>();

	protected Combat streamedCombat = null;

	//@Inject
	protected RaidPresenter raidPresenter;

	private final Comparator<AnchorPane> itemsComparator = (o1, o2) -> getValueTotal(o2).compareTo(getValueTotal(o1));

	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);
	}

	public void setRaidPresenter(final RaidPresenter raidPresenter) {
		this.raidPresenter = raidPresenter;
	}

	@Override
	public void onRaidDataUpdate(final Combat combat, final RaidCombatMessage message) {

		// avoid unnecessary double-update upon local "refreshCombatStats"
		streamedCombat = combat;

		if (!isMessageEligible(message)) {
			// nothing to do
			return;
		}

		if (!sets.get(getSetKey(message)).items.containsKey(getItemKey(message))
			&& getValueTotal(message) < getMinValueTotal()) {
			// too low and not there yet, ignore
			return;
		}

		final ValueType setKey = getSetKey(message);
		final AnchorPane pane;

		final String playerName = getItemKey(message);
		if (Format.isFakePlayerName(playerName)) {
			if (sets.get(setKey).items.containsKey(Format.getRealNameEvenForFakePlayer(playerName))) {
				sets.get(setKey).items.remove(playerName);
				// no longer needed
				return;
			}
		} else {
			// replace fake
			sets.get(setKey).items.remove(Format.formatFakePlayerName(playerName));
		}

		if (!sets.get(setKey).items.containsKey(getItemKey(message))) {

			pane = createNode(barColors.get(setKey), getTitle(message), getValueTotal(message), getValuePerSecond(message));

			sets.get(setKey).items.put(getItemKey(message), pane);

		} else {
			pane = sets.get(setKey).items.get(getItemKey(message));
		}

		setValueData(message, pane);
	}

	@Override
	public void onRaidDataFinalize() {

		// remove panes from graph to simplify layout
		statsGrid.getChildren().clear();

		final double availableHeight;
		if (popoutRoot != null) {
			if (popoutRoot.getTransforms().isEmpty()) {
				availableHeight = popoutRoot.getHeight();
			} else {
				availableHeight = popoutRoot.getHeight() / ((Scale) popoutRoot.getTransforms().get(0)).getY();
			}
		} else {
			availableHeight = height;
		}

		final int availableSlots = (int) Math.round((availableHeight - ITEM_HEIGHT) / (ITEM_HEIGHT + ITEM_GAP));

		int totalNodesNeeded = 0;

		for (final Set set: sets.values()) {

			recompute(set);
			totalNodesNeeded += needsTotalNode(set) ? 1 : 0;
		}

		final List<AnchorPane> items = new ArrayList<>();
		for (final Set set: sets.values()) {
			final List<AnchorPane> list = new LinkedList<>(set.items.values());

			list.sort(itemsComparator);

			for (final AnchorPane pane : list) {
				if (this.bars) {
					((Rectangle) pane.getChildren().get(1)).setWidth(!(set.maxValueTotal > 0) ? 0 : ITEM_WIDTH
							* (getValueTotal(pane) * 1.0 / set.maxValueTotal));
				}
				((Label) pane.getChildren().get(3)).setText(Format.formatThousands(getValueTotal(pane)));
				((Label) pane.getChildren().get(4)).setText(Format.formatAdaptive(getValuePerSecond(pane)));

				items.add(pane);

				if (items.size() >= (availableSlots - totalNodesNeeded)) {
					break;
				}
			}

			createOrUpdateTotalNode(set, items);
		}

		statsGrid.getChildren().setAll(items);
	}

	private void recompute(final Set set) {

		set.maxValueTotal = set.sumValueTotal = set.sumValuePerSecond = 0;

		for (final AnchorPane pane: set.items.values()) {
			if (getValueTotal(pane) > set.maxValueTotal) {
				set.maxValueTotal = getValueTotal(pane);
			}
			set.sumValueTotal += getValueTotal(pane);
			set.sumValuePerSecond += getValuePerSecond(pane);
		}
	}

	private boolean needsTotalNode(final Set set) {
		if (set.totalPane != null) {
			return true;
		}
		if (set.sumValueTotal > 0 && set.items.size() > 2) {
			return true;
		}
		return false;
	}

	protected void createOrUpdateTotalNode(final Set set, final List<AnchorPane> items) {

		if (set.totalPane == null) {
			if (!needsTotalNode(set)) {
				return;
			}
			set.totalPane = createNode(backgroundColor, "", set.sumValueTotal, set.sumValuePerSecond);

		} else {
			((Label) set.totalPane.getChildren().get(3)).setText(Format.formatThousands(set.sumValueTotal));
			((Label) set.totalPane.getChildren().get(4)).setText(Format.formatAdaptive(set.sumValuePerSecond));
		}

		items.add(set.totalPane);
	}

	protected AnchorPane createNode(final Color fillColor, final String valueTitle, final Integer valueTotal,
		final Integer valuePerSecond) {

		final AnchorPane pane = new AnchorPane();

		final Rectangle barBack = new Rectangle(0, 0, ITEM_WIDTH, ITEM_HEIGHT);
		barBack.setFill(fillColor.deriveColor(0, 1, .5, 1));
		barBack.setVisible(this.bars);
		barBack.setOpacity(this.opacity);

		final Rectangle bar = new Rectangle(0, 0, 0, ITEM_HEIGHT);
		bar.setFill(fillColor);
		bar.setVisible(this.bars);
		bar.setOpacity(this.opacity);

		final Label title = new Label(valueTitle);
		title.setTextFill(this.textColor);
		title.setMaxWidth(66);

		AnchorPane.setTopAnchor(title, 0d);
		AnchorPane.setBottomAnchor(title, 0d);
		AnchorPane.setLeftAnchor(title, 5d);

		final Label total = new Label(Format.formatThousands(valueTotal));
		total.setTextFill(this.textColor);

		AnchorPane.setTopAnchor(total, 0d);
		AnchorPane.setRightAnchor(total, (double) LABEL_WIDTH);
		AnchorPane.setBottomAnchor(total, 0d);

		final Label ps = new Label(Format.formatAdaptive(valuePerSecond));
		ps.setTextFill(this.textColor);
		ps.setAlignment(Pos.CENTER_RIGHT);

		AnchorPane.setTopAnchor(ps, 0d);
		AnchorPane.setRightAnchor(ps, 5d);
		AnchorPane.setBottomAnchor(ps, 0d);

		pane.getChildren().setAll(barBack, bar, title, total, ps);

		return pane;
	}

	@Override
	public void repaint(Object source) {
		super.repaint(source);

		onRaidDataFinalize();

		for (final ValueType setKey: sets.keySet()) {
			final Set set = sets.get(setKey);
			for (final AnchorPane pane: set.items.values()) {
				final Rectangle back = ((Rectangle) pane.getChildren().get(0));
				final Rectangle bar = ((Rectangle) pane.getChildren().get(1));

				if (this.bars) {
					// showing bars
					back.setOpacity(opacity);
					back.setFill(barColors.get(setKey).deriveColor(0, 1, .5, 1));
					back.setVisible(true);

					bar.setOpacity(opacity);
					bar.setFill(barColors.get(setKey));
					bar.setWidth(!(set.maxValueTotal > 0) ? 0 : ITEM_WIDTH * getValueTotal(pane) / set.maxValueTotal);
					bar.setVisible(true);

				} else {
					back.setVisible(false);
					bar.setVisible(false);
				}

				((Label) pane.getChildren().get(2)).setTextFill(this.textColor);
				((Label) pane.getChildren().get(3)).setTextFill(this.textColor);
				((Label) pane.getChildren().get(4)).setTextFill(this.textColor);
			}
			if (set.totalPane != null) {
				final Rectangle back = ((Rectangle) set.totalPane.getChildren().get(0));
				final Rectangle bar = ((Rectangle) set.totalPane.getChildren().get(1));
				back.setOpacity(opacity);
				back.setFill(backgroundColor);
				back.setVisible(this.bars);
				bar.setOpacity(opacity);
				bar.setVisible(this.bars);

				((Label) set.totalPane.getChildren().get(2)).setTextFill(this.textColor);
				((Label) set.totalPane.getChildren().get(3)).setTextFill(this.textColor);
				((Label) set.totalPane.getChildren().get(4)).setTextFill(this.textColor);
			}
		}
	}

	abstract protected ValueType getSetKey(final RaidCombatMessage message);

	abstract protected Integer getMinValueTotal();

	abstract protected Integer getValueTotal(final RaidCombatMessage message);

	abstract protected Integer getValuePerSecond(final RaidCombatMessage message);

	protected boolean isMessageEligible(final RaidCombatMessage message) {
		return true;
	}

	protected String getItemKey(final RaidCombatMessage message) {
		return message.getCharacterName();
	}

	protected String getTitle(final RaidCombatMessage message) {
		return message.getCharacterName();
	}

	final protected void setValueData(final RaidCombatMessage message, final AnchorPane pane) {
		pane.setUserData(new Integer[] { getValueTotal(message), getValuePerSecond(message) });
	}

	final protected Integer getValueTotal(final AnchorPane pane) {
		return ((Integer[]) pane.getUserData())[0];
	}

	final protected Integer getValuePerSecond(final AnchorPane pane) {
		return ((Integer[]) pane.getUserData())[1];
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		if (raidPresenter.getCombatLogName() == null) {
			// nothing started yet, nothing to display
			return;
		}

		if (streamedCombat != null && combat.getCombatId() == streamedCombat.getCombatId()) {
			// already displaying this combat with the latest data available
			// bump to pick up boss
			streamedCombat = combat;
			return;
		}

		// other combat selected, reset the display
		streamedCombat = combat;

		// try to fetch latest data
		final Collection<RaidCombatMessage> messages = raidPresenter.getCombatUpdates(raidPresenter.getCombatLogName(), combat);
		if (messages != null) {
			for (final RaidCombatMessage message: messages) {
				onRaidDataUpdate(streamedCombat, message);
			}
			onRaidDataFinalize();
		}
	}

	@Override
	public void resetCombatStats() {
		streamedCombat = null;

		statsGrid.getChildren().clear();
		for (final Set set: sets.values()) {
			set.items.clear();
			set.totalPane = null;
			set.maxValueTotal = set.sumValuePerSecond = set.sumValueTotal = 0;
		}
	}
}
