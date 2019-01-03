package com.ixale.starparse.raid;

import com.ixale.starparse.ws.RaidCombatMessage;
import com.ixale.starparse.ws.RaidRequestMessage;
import com.ixale.starparse.ws.RaidClient.RequestIncomingCallback;

public interface RaidListener {

	void onRaidStarted();

	void onRaidStopped();

	void onPlayerJoin(String[] characterNames);

	void onPlayerQuit(String[] characterNames);

	void onCombatUpdated(RaidCombatMessage[] message);

	void onRequestIncoming(RaidRequestMessage message, RequestIncomingCallback callback);

	void onError(String message, boolean reconnecting);

}
