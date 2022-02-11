package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class TimersCPopoutPresenter extends BaseTimersPopoutPresenter {

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);
		popoutTitle.setText("Timers C");
	}

	@Override
	protected int getOffsetX() {
		return super.getOffsetX() + 2 * (DEFAULT_WIDTH + 20);
	}

}
