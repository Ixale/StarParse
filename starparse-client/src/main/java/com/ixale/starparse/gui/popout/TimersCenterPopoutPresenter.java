package com.ixale.starparse.gui.popout;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.BaseTimer.Scope;
import com.ixale.starparse.timer.CustomTimer;
import com.ixale.starparse.timer.TimerManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class TimersCenterPopoutPresenter extends BasePopoutPresenter {

	final static private double TIMER_WIDTH = 150;
	final static private double TIMER_HEIGHT = 50;

	@FXML
	private VBox timersGrid;
	@FXML
	private Text combatTime;

	private final HashMap<BaseTimer, AnchorPane> timers = new HashMap<BaseTimer, AnchorPane>();

	private int threshold = 7000;
	private boolean isShowing = false;

	private TimersPopoutPresenter timersPopoutPresenter;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.offsetX = getDefaultX();
		this.offsetY = getDefaultY();
		this.height = 100;

		this.mouseTransparent = true;
	}

	public static int getDefaultX() {
		return (int) (Screen.getPrimary().getVisualBounds().getMaxX() / 2 - TIMER_WIDTH / 2);
	}

	public static int getDefaultY() {
		return (int) (Screen.getPrimary().getVisualBounds().getMaxY() / 3 * 2);
	}

	public void setTimersPopoutPresenter(final TimersPopoutPresenter timersPopoutPresenter) {
		this.timersPopoutPresenter = timersPopoutPresenter;
	}

	public int getThreshold() {
		return threshold;
	}

	@Override
	public void setMouseTransparent(boolean mouseTransparent) {
		// never change
	}

	@Override
	public void setSolid(boolean solid) {
		// keep false all the time
	}

	@Override
	public void showPopout() {
		getPopout().show();
	}

	private void addTimer(final BaseTimer timer) {

		final AnchorPane pane = new AnchorPane();

		final Rectangle barBack = new Rectangle(0, 0, TIMER_WIDTH, TIMER_HEIGHT);
		barBack.setFill(timersPopoutPresenter.getColor(timer).deriveColor(0, 1, .5, 1));
		barBack.setOpacity(timersPopoutPresenter.opacity);

		final Label title = new Label("");
		title.setTextFill(timersPopoutPresenter.textColor);
		title.setFont(Font.font("System", 18));

		AnchorPane.setTopAnchor(title, 0d);
		AnchorPane.setBottomAnchor(title, 0d);
		AnchorPane.setLeftAnchor(title, 5d);

		final Label time = new Label("");
		time.setTextFill(timersPopoutPresenter.textColor);
		time.setFont(Font.font("System", FontWeight.BOLD, 30));

		AnchorPane.setTopAnchor(time, 0d);
		AnchorPane.setRightAnchor(time, 5d);
		AnchorPane.setBottomAnchor(time, 0d);

		pane.getChildren().addAll(barBack, title, time);

		timers.put(timer, pane);

		timersGrid.getChildren().add(pane);

		if (isEnabled() && !isShowing) {
			if (!hasPopout()) {
				showPopout();
			}
			getPopout().bringToFront();
			isShowing = true;
		}
	}

	public void updateTimer(final BaseTimer timer) {

		if (!timers.containsKey(timer)) {
			if (timer.isFinished()) {
				// already finished, do not bother rendering it at all
				return;
			}
			addTimer(timer);
		}

		final Label title = (Label) timers.get(timer).getChildren().get(1);
		title.setText(getFullTimerLabel(timer));

		final Label time = (Label) timers.get(timer).getChildren().get(2);
		time.setText(Format.formatSeconds(timer.getTimeRemaining(), timersPopoutPresenter.getFractionsThreshold()));

		final boolean timerVisible = !TimerManager.isMuted() || Scope.ANY.equals(timer.getScope());
		if (timerVisible != timers.get(timer).isVisible()) {
			timers.get(timer).setVisible(timerVisible);
		}
	}

	public void removeTimer(BaseTimer timer) {
		final AnchorPane pane = timers.get(timer);
		if (pane == null) {
			return;
		}
		timersGrid.getChildren().remove(pane);
		timers.remove(timer);

		if (timers.isEmpty()) {
			isShowing = false;
		}
	}

	public void resetTimers() {
		timers.clear();
		timersGrid.getChildren().clear();
		isShowing = false;
	}

	private String getFullTimerLabel(final BaseTimer timer) {
		return getShortName(timer.getName(), 15) + (timer.getRepeatCounter() > 1 ? " #" + timer.getRepeatCounter() : "");
	}

	private String getShortName(final String n, final int limit) {
		if (n != null && n.contains("\n")) {
			return CustomTimer.getShortName(n.split("\n")[0], limit)
					+ "\n"
					+ getShortName(n.split("\n")[1], limit);
		}
		return CustomTimer.getShortName(n, limit);
	}

	@Override
	protected void refreshCombatStats(Combat combat, CombatStats stats) throws Exception {
		// by TimersPopoutPresenter
	}

	@Override
	public void resetCombatStats() {
		// by TimersPopoutPresenter
	}

}
