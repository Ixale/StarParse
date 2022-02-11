package com.ixale.starparse.time;

import java.net.SocketTimeoutException;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ixale.starparse.gui.Format;

public class TimeUtils {

	private static final Logger logger = LoggerFactory.getLogger(TimeUtils.class);

	private static Long localClockOffset = 0L;

	private static final TimeZone defaultTimezone;
	private static TimeZone currentTimezone;

	static {
		defaultTimezone = currentTimezone = TimeZone.getDefault();
	}

	public static Long updateClockOffset(String timeSyncHost) {
		try {
			NtpRequest.requestTime(timeSyncHost, 15000);
			localClockOffset = Math.round(NtpRequest.getLocalClockOffset() * 1000);

			long off = Math.abs(localClockOffset);
			if (off > 10000) {
				if (off > 30 * 60 * 1000) {
					// possibly incorrect time zone, cap it as it would not work anyway
					final long original = localClockOffset;
					localClockOffset %= 30 * 60 * 1000;
					logger.warn("Local clock difference resolved as: " + original + " ms (possibly incorrect Timezone, capped to " + localClockOffset + ")");
				} else {
					logger.warn("Local clock difference resolved as: " + localClockOffset + " ms");
				}

			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Local clock difference resolved as: " + localClockOffset + " ms");
				}
			}
			return off;

		} catch (Exception e) {
			logger.warn("Unable to resolve local clock difference using [" + timeSyncHost + "]: " + e.getMessage(), e instanceof SocketTimeoutException ? null : e);
			return null;
		}
	}

	public static Long getCurrentTime() {
		return System.currentTimeMillis() + localClockOffset;
	}

	public static TimeZone getDefaultTimezone() {
		return defaultTimezone;
	}

	public static TimeZone getCurrentTimezone() {
		return currentTimezone;
	}

	public static void setCurrentTimezone(final String tzId) {
		try {
			final TimeZone tz = TimeZone.getTimeZone(tzId);
			TimeZone.setDefault(tz);
			Format.resetTimezone();
			logger.info("Timezone set as: " + tz.getID()
				+ " (" + formatTimezoneOffset(tz.getRawOffset())
				+ " / " + formatTimezoneOffset(tz.getOffset(System.currentTimeMillis())) + ")");
			currentTimezone = tz;

		} catch (Exception e) {
			logger.warn("Ignored invalid timezone: " + tzId);
		}
	}

	public static String formatTimezoneOffset(final int offsetInMillis) {
		final String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
		return (offsetInMillis >= 0 ? "+" : "-") + offset;
	}

}
