package com.ixale.starparse.gui;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class StatusBarAppender extends AppenderSkeleton {

	public interface Listener {
		public void onMessage(String msg, LoggingEvent event);
	}

	static Listener listener = null;

	static public void setListener(Listener l) {
		listener = l;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		if (listener == null) {
			return;
		}
		listener.onMessage(this.layout.format(event), event);		
	}
}
