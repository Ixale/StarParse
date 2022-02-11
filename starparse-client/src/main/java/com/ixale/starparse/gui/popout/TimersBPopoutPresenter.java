package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class TimersBPopoutPresenter extends BaseTimersPopoutPresenter {

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);
		popoutTitle.setText("Timers B");
	}

	@Override
	protected int getOffsetX() {
		return super.getOffsetX() + DEFAULT_WIDTH + 25;
	}

}
