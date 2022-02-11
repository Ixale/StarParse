package com.ixale.starparse.gui.popout;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.time.TimeUtils;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class TimersPopoutPresenter extends BaseTimersPopoutPresenter {

	private TimersCenterPopoutPresenter timersCenterControl;

	@Override
	protected int getOffsetY() {
		return 440;
	}

	@Override
	protected boolean hideTitle() {
		return false;
	}

	public void setTimersCenterControl(final TimersCenterPopoutPresenter timersCenterPopoutPresenter) {
		// wire
		timersCenterPopoutPresenter.setTimersPopoutPresenter(this);
		this.timersCenterControl = timersCenterPopoutPresenter;
	}

	@Override
	public void tickTimers() {
		super.tickTimers();
		if (currentCombatStart == null) {
			return;
		}
		combatTime.setText(Format.formatTime(TimeUtils.getCurrentTime() - currentCombatStart));
	}

	@Override
	public void updateTimer(final BaseTimer timer) {
		super.updateTimer(timer);
		if (timersCenterControl != null && timersCenterControl.isEnabled()) {
			if (timer.doOverrideExpiringThreshold() || timer.getTimeRemaining() < timersCenterControl.getThreshold()) {
				timersCenterControl.updateTimer(timer);
			} else {
				// repeating timer expired and started again, remove for now
				timersCenterControl.removeTimer(timer);
			}
		}
	}

	@Override
	public void removeTimer(BaseTimer timer) {
		if (!timers.containsKey(timer)) {
			return;
		}
		super.removeTimer(timer);
		timersCenterControl.removeTimer(timer);
	}

	@Override
	public void resetTimers() {
		super.resetTimers();
		combatTime.setText("00:00");
		if (timersCenterControl != null) {
			timersCenterControl.resetTimers();
		}
	}


	@Override
	protected void refreshCombatStats(Combat combat, CombatStats stats) throws Exception {
		super.refreshCombatStats(combat, stats);
		if (combat != null && TimerManager.isMuted()) {
			if (combat.getTimeTo() != null && currentCombatStart != null) {
				combatTime.setText(Format.formatTime(combat.getTimeTo() - currentCombatStart));
			}
		}
	}

	@Override
	public void resetCombatStats() {
		super.resetCombatStats();
		combatTime.setText("00:00");
	}

}
