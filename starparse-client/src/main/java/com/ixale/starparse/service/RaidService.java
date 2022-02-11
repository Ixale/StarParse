package com.ixale.starparse.service;

import com.ixale.starparse.domain.RaidRequest;
import com.ixale.starparse.ws.RaidCombatMessage;

import java.util.Collection;

public interface RaidService {

	void loadCombatStats(String combatLogName);

	void storeCombatStats(String combatLogName);

	void storeCombatUpdate(String combatLogName, int combatId, RaidCombatMessage message);

	Collection<RaidCombatMessage> getCombatUpdates(String combatLogName, int combatId);

	<T> T getStoredResponses(String combatLogName, RaidRequest request);

	<T> T decodeAndStoreResponse(String combatLogName, RaidRequest request, byte[] payload);

	<T> byte[] encodeResponse(T response);
}
