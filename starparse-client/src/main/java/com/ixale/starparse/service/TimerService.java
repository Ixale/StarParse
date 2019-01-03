package com.ixale.starparse.service;

import java.util.List;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.ConfigTimer;
import com.ixale.starparse.domain.Event;

public interface TimerService {

	void triggerTimers(Combat combat, List<Event> events, List<ConfigTimer> configTimers);
}
