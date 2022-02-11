package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.ResourceBundle;

import com.ixale.starparse.domain.ValueType;
import com.ixale.starparse.ws.RaidCombatMessage;

public class RaidTpsPopoutPresenter extends BaseRaidPopoutPresenter {

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.offsetY = 600;

		sets.put(ValueType.THREAT, new Set());
	}

	@Override
	protected ValueType getSetKey(final RaidCombatMessage message) {
		return ValueType.THREAT;
	}

	@Override
	protected Integer getMinValueTotal() {
		return MIN_DISPLAY_VALUE;
	}

	@Override
	protected Integer getValueTotal(final RaidCombatMessage message) {
		return message.getCombatStats().getThreat();
	}

	@Override
	protected Integer getValuePerSecond(final RaidCombatMessage message) {
		return message.getCombatStats().getTps();
	}
}
