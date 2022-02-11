package com.ixale.starparse.service;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.parser.Parser;

import java.util.Map;

public interface EventServiceListener {

	void onNewFile() throws Exception;

	void onNewCombat(Combat newCombat) throws Exception;

	void onNewEvents(Combat lastCombat, final Map<Actor, Parser.ActorState> actorStates) throws Exception;
}
