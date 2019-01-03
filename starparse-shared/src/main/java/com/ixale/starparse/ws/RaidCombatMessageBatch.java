package com.ixale.starparse.ws;

import java.util.Collection;

public class RaidCombatMessageBatch extends BaseMessage {

	private static final long serialVersionUID = 1L;

	private RaidCombatMessage[] messages;

	public RaidCombatMessageBatch() {
		
	}

	public RaidCombatMessageBatch(final Collection<RaidCombatMessage> messages) {
		this(messages.toArray(new RaidCombatMessage[messages.size()]));
	}

	public RaidCombatMessageBatch(final RaidCombatMessage[] messages) {

		this.messages = messages;
	}

	public RaidCombatMessage[] getMessages() {
		return messages;
	}

	public String toString() {
		return "Batch: "+messages;
	}

}
