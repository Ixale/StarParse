package com.ixale.starparse.domain;

import java.io.Serializable;

public class Absorption implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int eventId;
	private final int effectId;

	public Absorption(int eventId, int effectId) {
		super();
		this.eventId = eventId;
		this.effectId = effectId;
	}

	public int getEventId() {
		return eventId;
	}

	public int getEffectId() {
		return effectId;
	}
}
