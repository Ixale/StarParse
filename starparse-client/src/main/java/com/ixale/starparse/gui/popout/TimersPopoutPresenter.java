package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.time.TimeUtils;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;
import com.ixale.starparse.timer.BaseTimer.Scope;

public class TimersPopoutPresenter extends BasePopoutPresenter {

	final static private int TIMER_WIDTH = 150;
	final static private int TIMER_HEIGHT = 25;

	@FXML
	private VBox timersGrid;
	@FXML
	private Text combatTime;

	private Long currentCombatStart = null;
	private Integer thresholdMs = null;

	private final HashMap<BaseTimer, AnchorPane> timers = new HashMap<BaseTimer, AnchorPane>();

	private TimersCenterPopoutPresenter timersCenterControl;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.offsetY = 440;
		this.height = (TITLE_HEIGHT + 3 * (TIMER_HEIGHT + ITEM_GAP));
		this.itemHeight = TIMER_HEIGHT;
		this.minH = height - 1 * (TIMER_HEIGHT + ITEM_GAP);
		this.maxH = height + 2 * (TIMER_HEIGHT + ITEM_GAP);

		resetTimers();
	}

	public void setTimersCenterControl(final TimersCenterPopoutPresenter timersCenterPopoutPresenter) {
		// wire
		timersCenterPopoutPresenter.setTimersPopoutPresenter(this);
		this.timersCenterControl = timersCenterPopoutPresenter;
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
		combatTime.setText(Format.formatTime(TimeUtils.getCurrentTime() - currentCombatStart));
	}

	private void addTimer(final BaseTimer timer) {

		final AnchorPane pane = new AnchorPane();

		final Rectangle barBack = new Rectangle(0, 0, TIMER_WIDTH, TIMER_HEIGHT);
		barBack.setFill(getColor(timer).deriveColor(0, 1, .5, 1));
		barBack.setVisible(this.bars);
		barBack.setOpacity(this.opacity);

		final Rectangle bar = new Rectangle(0, 0, 0, TIMER_HEIGHT);
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
	}

	public void updateTimer(final BaseTimer timer) {

		// this will actually move the clock and may render it finished
		timer.update(TimeUtils.getCurrentTime());

		if (!timer.isVisual()) {
			// nothing else to do
			return;
		}

		if (!timers.containsKey(timer)) {
			if (timer.isFinished()) {
				// already finished, do not bother rendering it at all
				return;
			}
			addTimer(timer);
		}

		if (this.bars) {
			final Rectangle rect = (Rectangle) timers.get(timer).getChildren().get(1);
			rect.setWidth(TIMER_WIDTH * timer.getProgress());
		}

		final Label title = (Label) timers.get(timer).getChildren().get(2);
		title.setText(getFullTimerLabel(timer));

		final Label time = (Label) timers.get(timer).getChildren().get(3);
		if (thresholdMs != null && thresholdMs >= timer.getTimeRemaining()) {
			time.setText(Format.formatSeconds(timer.getTimeRemaining(), thresholdMs));
		} else {
			time.setText(Format.formatTime(timer.getTimeRemaining()));
		}

		final boolean timersVisible = !TimerManager.isMuted() || Scope.ANY.equals(timer.getScope());
		if (timersVisible != timers.get(timer).isVisible()) {
			timers.get(timer).setVisible(timersVisible);
		}

		if (timersCenterControl != null && timersCenterControl.isEnabled()) {
			if (timer.doOverrideExpiringThreshold() || timer.getTimeRemaining() < timersCenterControl.getThreshold()) {
				timersCenterControl.updateTimer(timer);
			} else {
				// repeating timer expired and started again, remove for now
				timersCenterControl.removeTimer(timer);
			}
		}
	}

	public void removeTimer(BaseTimer timer) {
		if (!timers.containsKey(timer)) {
			return;
		}

		timersGrid.getChildren().remove(timers.get(timer));

		timersCenterControl.removeTimer(timer);

		timers.remove(timer);
	}

	public void resetTimers() {
		timers.clear();
		timersGrid.getChildren().clear();
		if (hasPopout()) {
			getPopout().bringToFront();
		}
		combatTime.setText("00:00");
		currentCombatStart = null;

		if (timersCenterControl != null) {
			timersCenterControl.resetTimers();
		}
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

		for (final BaseTimer timer: timers.keySet()) {
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
			if (combat.getTimeTo() != null && currentCombatStart != null) {
				combatTime.setText(Format.formatTime(combat.getTimeTo() - currentCombatStart));
			}
			currentCombatStart = null;
		} else {
			currentCombatStart = combat.getTimeFrom();
		}
	}

	@Override
	public void resetCombatStats() {
		currentCombatStart = null;
		combatTime.setText("00:00");
	}
}
