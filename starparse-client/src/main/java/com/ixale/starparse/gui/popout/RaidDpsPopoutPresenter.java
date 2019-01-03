package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.ResourceBundle;

import com.ixale.starparse.domain.ValueType;
import com.ixale.starparse.ws.RaidCombatMessage;

public class RaidDpsPopoutPresenter extends BaseRaidPopoutPresenter {

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		sets.put(ValueType.DAMAGE, new Set());
	}

	@Override
	protected ValueType getSetKey(final RaidCombatMessage message) {
		return ValueType.DAMAGE;
	}

	@Override
	protected Integer getMinValueTotal() {
		return MIN_DISPLAY_VALUE;
	}

	@Override
	protected Integer getValueTotal(final RaidCombatMessage message) {
		return message.getCombatStats().getDamage();
	}

	@Override
	protected Integer getValuePerSecond(final RaidCombatMessage message) {
		return message.getCombatStats().getDps();
	}
}
