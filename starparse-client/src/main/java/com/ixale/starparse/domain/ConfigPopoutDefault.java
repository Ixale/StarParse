package com.ixale.starparse.domain;

import java.io.Serializable;

import javafx.scene.paint.Color;

public class ConfigPopoutDefault implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Color
		DEFAULT_BACKGROUND = Color.web("0x00000081"), // 50%
		DEFAULT_TEXT = Color.WHITE,
		DEFAULT_DAMAGE = Color.web("#ff3e3e").deriveColor(0, 1, .9, 1),
		DEFAULT_HEALING = Color.web("#32cd32").deriveColor(0, 1, .9, 1),
		DEFAULT_THREAT = Color.web("#ff963e").deriveColor(0, 1, .9, 1),
		DEFAULT_FRIENDLY = Color.web("#cd31cc").deriveColor(0, 1, .9, 1),
		DEFAULT_TIMER1 = DEFAULT_DAMAGE,
		DEFAULT_TIMER2 = DEFAULT_FRIENDLY,
		DEFAULT_TIMER3 = DEFAULT_THREAT,
		DEFAULT_TIMER4 = DEFAULT_HEALING;

	private String backgroundColor, textColor, damageColor, healingColor, threatColor, friendlyColor;

	private Boolean mouseTransparent, timersCenter, solid;

	private Integer timersFractions, dtDelay1, dtDelay2;

	private Color get(String c, Color d) {
		if (c == null) {
			return d;
		}
		try {
			return Color.web(c);
		} catch (Exception e) {
			return d;
		}
	}

	public Color getBackgroundColor() {
		return get(backgroundColor, DEFAULT_BACKGROUND);
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor.toString();
	}

	public Color getTextColor() {
		return get(textColor, DEFAULT_TEXT);
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor.toString();
	}

	public Color getDamageColor() {
		return get(damageColor, DEFAULT_DAMAGE);
	}

	public void setDamageColor(Color damageColor) {
		this.damageColor = damageColor.toString();
	}

	public Color getHealingColor() {
		return get(healingColor, DEFAULT_HEALING);
	}

	public void setHealingColor(Color healingColor) {
		this.healingColor = healingColor.toString();
	}

	public Color getThreatColor() {
		return get(threatColor, DEFAULT_THREAT);
	}

	public void setThreatColor(Color threatColor) {
		this.threatColor = threatColor.toString();
	}

	public Color getFriendlyColor() {
		return get(friendlyColor, DEFAULT_FRIENDLY);
	}

	public void setFriendlyColor(Color friendlyColor) {
		this.friendlyColor = friendlyColor.toString();
	}

	public Boolean getMouseTransparent() {
		return mouseTransparent;
	}

	public void setMouseTransparent(Boolean mouseTransparent) {
		this.mouseTransparent = mouseTransparent;
	}

	public Boolean getTimersCenter() {
		return timersCenter == null ? false : timersCenter;
	}

	public void setTimersCenter(Boolean timersCenter) {
		this.timersCenter = timersCenter;
	}

	public Integer getTimersFractions() {
		return timersFractions;
	}

	public void setTimersFractions(Integer timersFractions) {
		this.timersFractions = timersFractions;
	}

	public Boolean getSolid() {
		return solid == null ? false : solid;
	}

	public void setSolid(Boolean solid) {
		this.solid = solid;
	}

	public Integer getDtDelay1() {
		return dtDelay1;
	}

	public void setDtDelay1(Integer dtDelay1) {
		this.dtDelay1 = dtDelay1;
	}

	public Integer getDtDelay2() {
		return dtDelay2;
	}

	public void setDtDelay2(Integer dtDelay2) {
		this.dtDelay2 = dtDelay2;
	}
}
