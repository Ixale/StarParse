package com.ixale.starparse.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Event implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		DAMAGE_DEALT, DAMAGE_TAKEN,
		HEALING_DONE, HEALING_TAKEN,
		ACTIONS,
		EVENT_SELF, EVENT_OTHERS,
		SIMPLIFIED,
		DEATH, COMBAT_EXIT, NIM_CRYSTAL
	}

	int eventId;
	int logId;
	long timestamp;

	Actor source;
	Actor target;

	Entity ability;
	Entity action;
	Entity effect;

	Integer value;
	Boolean isCrit;

	Entity damage;
	Entity reflect;
	Entity mitigation;
	Entity absorbtion;
	Integer absorbed;

	Long threat;

	Integer guardState = null;
	Integer effectiveHeal = null;
	Long effectiveThreat = null;

	// contextual
	Integer absorptionEventId;
	Actor absorptionSource;
	Entity absorptionAbility;

	Event() {
	}

	public Event(int eventId, int logId, long timestamp) {
		this.eventId = eventId;
		this.logId = logId;
		this.timestamp = timestamp;
	}

	public int getEventId() {
		return eventId;
	}

	public int getLogId() {
		return logId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Actor getSource() {
		return source;
	}

	public void setSource(Actor source) {
		this.source = source;
	}

	public Actor getTarget() {
		return target;
	}

	public void setTarget(Actor target) {
		this.target = target;
	}

	public Entity getAbility() {
		return ability;
	}

	public void setAbility(Entity ability) {
		this.ability = ability;
	}

	public Entity getAction() {
		return action;
	}

	public void setAction(Entity action) {
		this.action = action;
	}

	public Entity getEffect() {
		return effect;
	}

	public void setEffect(Entity effect) {
		this.effect = effect;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public Boolean isCrit() {
		return isCrit;
	}

	public void setCrit(Boolean isCrit) {
		this.isCrit = isCrit;
	}

	public Entity getDamage() {
		return damage;
	}

	public void setDamage(Entity damage) {
		this.damage = damage;
	}

	public Entity getReflect() {
		return reflect;
	}

	public void setReflect(Entity reflect) {
		this.reflect = reflect;
	}

	public Entity getMitigation() {
		return mitigation;
	}

	public void setMitigation(Entity mitigation) {
		this.mitigation = mitigation;
	}

	public Entity getAbsorbtion() {
		return absorbtion;
	}

	public void setAbsorption(Entity absorb) {
		this.absorbtion = absorb;
	}

	public Integer getAbsorbed()
	{
		return this.absorbed;
	}

	public void setAbsorbed(Integer absorbed)
	{
		this.absorbed = absorbed;
	}

	public Long getThreat() {
		return threat;
	}

	public void setThreat(Long threat) {
		this.threat = threat;
	}

	public Integer getGuardState() {
		return guardState;
	}

	public void setGuardState(Integer guardState) {
		this.guardState = guardState;
	}

	public Integer getEffectiveHeal() {
		return effectiveHeal;
	}

	public void setEffectiveHeal(Integer effectiveHeal) {
		this.effectiveHeal = effectiveHeal;
	}

	public Long getEffectiveThreat() {
		return effectiveThreat;
	}

	public void setEffectiveThreat(Long effectiveThreat) {
		this.effectiveThreat = effectiveThreat;
	}

	public Integer getAbsorptionEventId() {
		return absorptionEventId;
	}

	public void setAbsorptionEventId(Integer absorptionEventId) {
		this.absorptionEventId = absorptionEventId;
	}

	public Entity getAbsorptionAbility() {
		return absorptionAbility;
	}

	public void setAbsorptionAbility(Entity absorptionAbility) {
		this.absorptionAbility = absorptionAbility;
	}

	public Actor getAbsorptionSource() {
		return absorptionSource;
	}

	public void setAbsorptionSource(Actor absorptionSource) {
		this.absorptionSource = absorptionSource;
	}

	public String getTs() {
		return formatTs(timestamp);
	}

	public static String formatTs(long timestamp) {
		return new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH).format(timestamp);
	}

	public String toString()
	{
		return formatTs(timestamp)
			+ ": [" + source + "] on [" + target + "]"
			+ " casted [" + ability + "] causing [" + action + "] of [" + effect + "]"
			+ (value != null ? " for [" + (isCrit ? '*' : "") + value + "]" : "")
			+ (effectiveHeal != null ? " ~[" + effectiveHeal + "]" : "")
			+ (damage != null ? " damage [" + damage + "]" : "")
			+ (reflect != null ? " reflect [" + reflect + "]" : "")
			+ (mitigation != null ? " mitigation [" + mitigation + "]" : "")
			+ (absorbtion != null ? " absorbed [" + absorbtion + "] for [" + absorbed + "]" : "")
			+ (threat != null ? " <" + threat + ">" : "")
			+ (guardState != null && guardState > 0 ? " while guarded [" + guardState + "]" : "");
	}
}
