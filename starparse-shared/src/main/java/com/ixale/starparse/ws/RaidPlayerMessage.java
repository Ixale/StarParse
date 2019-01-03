package com.ixale.starparse.ws;

import java.util.Arrays;

public class RaidPlayerMessage extends BaseMessage {

	private static final long serialVersionUID = 1L;

	public enum Action {
		JOIN, QUIT,
	}

	private Action action;

	private String raidGroupName;
	private String[] characterNames;
	private Boolean storeEnabled;

	public RaidPlayerMessage() {

	}

	public RaidPlayerMessage(final Action action, final String raidGroupName, final String characterName,
		final Boolean storeEnabled) {
		this(action, raidGroupName, new String[] { characterName }, storeEnabled);
	}

	public RaidPlayerMessage(final Action action, final String raidGroupName, final String[] characterNames,
		final Boolean storeEnabled) {
		this.action = action;
		this.raidGroupName = raidGroupName;
		this.characterNames = characterNames;
		this.storeEnabled = storeEnabled;
	}

	public Action getAction() {
		return action;
	}

	public String getRaidGroupName() {
		return raidGroupName;
	}

	public String[] getCharacterNames() {
		return characterNames;
	}

	public Boolean isStoreEnabled() {
		return storeEnabled;
	}

	public String toString() {
		return action + ": " + Arrays.asList(characterNames) + " @ " + raidGroupName;
	}

}
