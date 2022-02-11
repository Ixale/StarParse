package com.ixale.starparse.gui.popout;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.time.TimeUtils;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.BaseTimer.Scope;
import com.ixale.starparse.timer.TimerManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public abstract class BaseTimersPopoutPresenter extends BasePopoutPresenter {

	@FXML
	protected AnchorPane popoutHeader;

	@FXML
	protected VBox timersGrid;
	@FXML
	protected Text combatTime;

	final static protected int TIMER_WIDTH = 150;

	protected Long currentCombatStart = null;
	protected Integer thresholdMs = null;

	protected final HashMap<BaseTimer, AnchorPane> timers = new HashMap<>();

	protected int getOffsetY() {
		return 440;
	}

	protected int getOffsetX() {
		return DEFAULT_OFFSET_X; // 20
	}

	protected boolean hideTitle() {
		return true;
	}

	private PauseTransition pause;

	@SuppressWarnings("PointlessArithmeticExpression")
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.name = this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().length() - 15);
		this.offsetX = getOffsetX();
		this.offsetY = getOffsetY();
		this.itemGap = 1;
		this.itemHeight = 32;
		this.height = (TITLE_HEIGHT + 3 * (itemHeight + itemGap));
		this.minH = height - 1 * (itemHeight + itemGap);
		this.maxH = height + 5 * (itemHeight + itemGap);
		this.resizeStepW = (itemWidth + 0);
		this.resizeStepH = (itemHeight + itemGap);


		if (hideTitle()) {
			popoutTitle.setVisible(false);
			popoutTitleBackground.setFill(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 0.01));
			popoutFooter.opacityProperty().addListener((a, oldVal, newVal) -> {
				if (newVal != null && newVal.intValue() > 0) {
					popoutTitleBackground.setFill(backgroundColor);
					popoutTitle.setVisible(true);
				} else {
					popoutTitleBackground.setFill(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 0.01));
					popoutTitle.setVisible(false);
				}
			});
		}

		resetTimers();
	}

	public void setFractions(Integer fractions) {
		this.thresholdMs = fractions == null ? null : fractions * 1000;
	}

	public Integer getFractionsThreshold() {
		return this.thresholdMs;
	}

	public Color getColor(final BaseTimer timer) {
		if (timer.getColor() != null) {
			return timer.getColor();
		}
		return TimerManager.getSystemColor(timer);
	}

	public void tickTimers() {
		if (currentCombatStart == null) {
			return;
		}
	}

	private AnchorPane addTimer(final BaseTimer timer) {

		final AnchorPane pane = new AnchorPane();

		final Rectangle barBack = new Rectangle(0, 0, TIMER_WIDTH, this.itemHeight);
		barBack.setFill(getColor(timer).deriveColor(0, 1, .5, 1));
		barBack.setVisible(this.bars);
		barBack.setOpacity(this.opacity);

		final Rectangle bar = new Rectangle(0, 0, 0, this.itemHeight);
		bar.setFill(getColor(timer));
		bar.setVisible(this.bars);
		bar.setOpacity(this.opacity);

		final Label title = new Label("");
		title.setTextFill(this.textColor);
		title.setFont(Font.font("System", 11));

		AnchorPane.setTopAnchor(title, 0d);
		AnchorPane.setBottomAnchor(title, 0d);
		AnchorPane.setLeftAnchor(title, 5d);

		final Label time = new Label("");
		time.setTextFill(this.textColor);
		time.setFont(Font.font("System", 18));

		AnchorPane.setTopAnchor(time, 0d);
		AnchorPane.setRightAnchor(time, 5d);
		AnchorPane.setBottomAnchor(time, 0d);

		pane.getChildren().addAll(barBack, bar, title, time);

		timers.put(timer, pane);

		timersGrid.getChildren().add(pane);
		return pane;
	}

	public void updateTimer(final BaseTimer timer) {

		// this will actually move the clock and may render it finished
		timer.update(TimeUtils.getCurrentTime());

		if (!timer.isVisual()) {
			// nothing else to do
			return;
		}

		AnchorPane pane = timers.get(timer);
		if (pane == null) {
			if (timer.isFinished()) {
				// already finished, do not bother rendering it at all
				return;
			}
			final double reqSize = (timers.size() + 1) * (itemHeight + itemGap);
			if (popoutRoot.getHeight() - popoutTitle.getHeight() < reqSize) {
				return;
			}
			pane = addTimer(timer);
		}

		if (this.bars) {
			final Rectangle rect = (Rectangle) pane.getChildren().get(1);
			rect.setWidth(TIMER_WIDTH * timer.getProgress());
		}

		final Label title = (Label) pane.getChildren().get(2);
		title.setText(getFullTimerLabel(timer));

		final Label time = (Label) pane.getChildren().get(3);
		if (thresholdMs != null && thresholdMs >= timer.getTimeRemaining()) {
			time.setText(Format.formatSeconds(timer.getTimeRemaining(), thresholdMs));
		} else {
			time.setText(Format.formatTime(timer.getTimeRemaining()));
		}

		final boolean timersVisible = !TimerManager.isMuted() || Scope.ANY.equals(timer.getScope());
		if (timersVisible != pane.isVisible()) {
			pane.setVisible(timersVisible);
		}
	}

	public void removeTimer(BaseTimer timer) {
		final AnchorPane pane = timers.get(timer);
		if (pane == null) {
			return;
		}

		timersGrid.getChildren().remove(pane);

		timers.remove(timer);
	}

	public void resetTimers() {
		timers.clear();
		timersGrid.getChildren().clear();
		if (hasPopout()) {
			getPopout().bringToFront();
		}
		currentCombatStart = null;
	}

	private String getFullTimerLabel(final BaseTimer timer) {
		return timer.getName() + (timer.getRepeatCounter() > 1 ? " #" + timer.getRepeatCounter() : "");
	}

	@Override
	public void setTextColor(final Color color) {
		super.setTextColor(color);

		combatTime.setFill(color);
	}

	@Override
	public void repaint(Object source) {
		super.repaint(source);

		while (popoutRoot.getHeight() - popoutTitle.getHeight() < (timers.size() * (itemHeight + itemGap))) {
			timers.values().remove((AnchorPane) timersGrid.getChildren().remove(timersGrid.getChildren().size() - 1));
		}

		for (final BaseTimer timer : timers.keySet()) {
			final AnchorPane pane = timers.get(timer);
			final Rectangle back = ((Rectangle) pane.getChildren().get(0));
			final Rectangle bar = ((Rectangle) pane.getChildren().get(1));

			if (this.bars) {
				// showing bars
				back.setOpacity(opacity);
				back.setFill(getColor(timer).deriveColor(0, 1, .5, 1));
				back.setVisible(true);

				bar.setOpacity(opacity);
				bar.setFill(getColor(timer));
				bar.setWidth(TIMER_WIDTH * timer.getProgress());
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
		// do nothing
		if (combat == null) {
			return;
		}

		if (TimerManager.isMuted()) {
			// out of combat (dead?), hide (temporarily?)
			currentCombatStart = null;
		} else {
			currentCombatStart = combat.getTimeFrom();
		}
	}

	@Override
	public void resetCombatStats() {
		currentCombatStart = null;
	}
}
