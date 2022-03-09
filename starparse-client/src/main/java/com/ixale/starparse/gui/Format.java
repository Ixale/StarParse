package com.ixale.starparse.gui;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatLog;
import com.ixale.starparse.domain.Entity;
import com.ixale.starparse.domain.EntityGuid;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.time.TimeUtils;

public class Format {

	static final SimpleDateFormat
			sdfTimeHMS = new SimpleDateFormat("HH:mm:ss"),
			sdfTimeMS = new SimpleDateFormat("mm:ss"),
			sdfTimeMSM = new SimpleDateFormat("mm:ss.SSS"),
			sdfTimeHMSM = new SimpleDateFormat("HH:mm:ss.SSS"),
			sdfDateTime = new SimpleDateFormat("d.M.yy HH:mm:ss"),
			sdfDate = new SimpleDateFormat("d.M.yy");

	static final DecimalFormat
			integerFormat = new DecimalFormat("#,###,###,##0"),
			floatFormat = new DecimalFormat("#,###,###,##0.0"),
			secondsFormat = new DecimalFormat("0.0"),
			percentFormat = new DecimalFormat("0.0");

	static long timeDiff;

	public static String formatCombatName(final Combat combat) {

		if (combat.getTimeTo() == null) {
			return "Current combat";
		}
		if (combat.getBoss() != null) {
			return combat.getBoss().getName()
					+ " (" + combat.getBoss().getSize().toString().substring(0, combat.getBoss().getSize().toString().length() - 1)
					+ " " + combat.getBoss().getMode() + ")";
		}
		if (combat.getName() != null) {
			return combat.getName();
		}
		return "...";
	}

	public static String formatCombatTime(final Combat combat) {
		if (combat.getTimeTo() == null) {
			return sdfDateTime.format(combat.getTimeFrom());
		}

		return sdfTimeHMS.format(combat.getTimeFrom())
				+ " - " + sdfTimeHMS.format(combat.getTimeTo())
				+ " (" + formatTime(combat.getTimeTo() - combat.getTimeFrom()) + ")";

	}

	public static String formatCombatLogTitle(final CombatLog combatLog) {
		if (combatLog == null) {
			// has gone away
			return "";
		}
		return (combatLog.getCharacterName() == null ? "?" : combatLog.getCharacterName())
				+ " @ " + sdfDateTime.format(combatLog.getTimeFrom());
	}

	public static String formatCombatLogTime(final CombatLog combatLog, final Long timeTo) {

		timeDiff = timeTo != null ? timeTo - combatLog.getTimeFrom() : 0;

		return sdfDate.format(combatLog.getTimeFrom()) + (timeDiff == 0 ? "" : String.format(" (%d:%02d)", (timeDiff / (60000 * 60)), ((timeDiff % (60000 * 60)) / 60000)));
	}

	public static String formatTime(long time) {
		return sdfTimeMS.format(time);
	}

	public static String formatTime(long time, boolean includeMs) {
		if (!includeMs) {
			return formatTime(time);
		}
		return sdfTimeMSM.format(time);
	}

	public static String formatSeconds(long time, Integer thresholdMs) {
		if (thresholdMs == null || thresholdMs < time) {
			return String.valueOf(Math.round(time / 1000.0));
		}
		return secondsFormat.format(time / 1000.0);
	}

	public static String formatTime(long time, boolean includeMs, boolean includeH) {
		if (!includeH) {
			return formatTime(time, includeMs);
		}
		return sdfTimeHMSM.format(time);
	}

	@SuppressWarnings("unused")
	public static String formatDate(long date) {
		return sdfDate.format(date);
	}

	public static String formatNumber(final Number number) {
		return integerFormat.format(number);
	}

	public static String formatAdaptive(final Integer number) {
		return formatAdaptive(number, false);
	}

	public static String formatThousands(final Integer number) {
		return formatAdaptive(number, true);
	}

	public static String formatMillions(final Integer number) {
		if (number >= 10000000 || number <= -10000000) {
			return integerFormat.format(number / 1000000.0) + " M";
		}
		return integerFormat.format(number);
	}

	private static String formatAdaptive(final Integer number, boolean thousands) {
		if (thousands || (number > 100000 || number < -100000)) {
			if (number >= 10000000 || number <= -10000000) {
				return integerFormat.format(number / 1000000.0) + " M";
			}
			return integerFormat.format(number / 1000.0) + " k";
		}
		return integerFormat.format(number);
	}

	public static String formatFloat(final Number number) {
		return floatFormat.format(number);
	}

	public static String formatPercent(final double number) {
		return percentFormat.format(100 * number);
	}

	public static String formatEffectName(final Entity effect, final Entity ability) {
		return (ability.getName().isEmpty() || effect.getName().equals(ability.getName()))
				? (effect.getName() == null || effect.getName().trim().isEmpty() ? "(" + effect.getGuid() + ")" : effect.getName())
				: ability.getName() + ": " + effect.getName();
	}

	public static String formatAbsorptionName(final Event e) {
		if (e.getAbsorptionEventId() != null) {
			return ((e.getAbsorptionSource() != null ? e.getAbsorptionSource().getName() + ": " : "")
					+ (e.getAbsorptionAbility() != null ? e.getAbsorptionAbility().getName() : ""));
		} else if (e.getAbsorbtion() != null) {
			return e.getAbsorbtion().getName();
		} else {
			return null;
		}
	}

	final static String entityRegex = "([A-Z][a-z]+)(Warrior|Knight|Smuggler|Agent|)([0-9]+|)";
	final static String entityReplacement = "$1 ";

	public static String formatAbility(final EntityGuid ability) {

		if (ability == null) {
			// fallback, should not happen
			return "Unknown";
		}

		return ability.name().replaceAll(entityRegex, entityReplacement).trim();
	}

	public static String formatFakePlayerName(final String playerName) {
		return "(" + playerName + ")";
	}

	public static boolean isFakePlayerName(final String name) {
		return name != null && name.startsWith("(");
	}

	public static String getRealNameEvenForFakePlayer(final String playerName) {
		return isFakePlayerName(playerName) ? playerName.substring(1, playerName.length() - 1) : playerName;
	}

	public static void resetTimezone() {
		sdfTimeHMS.setTimeZone(TimeUtils.getCurrentTimezone());
		sdfTimeMS.setTimeZone(TimeUtils.getCurrentTimezone());
		sdfTimeMSM.setTimeZone(TimeUtils.getCurrentTimezone());
		sdfTimeHMSM.setTimeZone(TimeUtils.getCurrentTimezone());
		sdfDateTime.setTimeZone(TimeUtils.getCurrentTimezone());
		sdfDate.setTimeZone(TimeUtils.getCurrentTimezone());
	}
}
