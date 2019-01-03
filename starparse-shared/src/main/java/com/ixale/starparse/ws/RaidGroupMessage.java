package com.ixale.starparse.ws;

import com.ixale.starparse.domain.RaidGroup;

public class RaidGroupMessage extends BaseMessage {

	private static final long serialVersionUID = 1L;

	public enum Action {
		CREATE, JOIN, REMOVE
	}

	private Action action;

	private RaidGroup raidGroup;

	public RaidGroupMessage() {
		
	}

	public RaidGroupMessage(Action action, final RaidGroup raidGroup) {
		this.action = action;
		this.raidGroup = raidGroup;
	}

	public Action getAction() {
		return action;
	}

	public RaidGroup getRaidGroup() {
		return raidGroup;
	}

	public String toString() {
		return action+": "+raidGroup;
	}
}
