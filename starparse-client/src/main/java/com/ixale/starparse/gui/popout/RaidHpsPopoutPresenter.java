package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.ResourceBundle;

import com.ixale.starparse.domain.ValueType;
import com.ixale.starparse.ws.RaidCombatMessage;

public class RaidHpsPopoutPresenter extends BaseRaidPopoutPresenter {

	private static final String MODE_HPS = "hps";

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.offsetY = 230;

		sets.put(ValueType.HEAL, new Set());

		addMode(new Mode(Mode.DEFAULT, "Raid Healing", null, null));
		addMode(new Mode(MODE_HPS, "Raw Healing", null, null));
	}

	@Override
	protected ValueType getSetKey(final RaidCombatMessage message) {
		return ValueType.HEAL;
	}

	@Override
	protected Integer getMinValueTotal() {
		return MIN_DISPLAY_VALUE;
	}

	@Override
	protected Integer getValueTotal(final RaidCombatMessage message) {
		final int s = raidPresenter.getShieldingTotal(message.getCharacterName());
		if (MODE_HPS.equals(getMode().mode)) {
			return message.getCombatStats().getHeal() + s;
		} else {
			return message.getCombatStats().getEffectiveHeal() + s;
		}
	}

	@Override
	protected Integer getValuePerSecond(final RaidCombatMessage message) {
		final int v;
		if (MODE_HPS.equals(getMode().mode)) {
			v = message.getCombatStats().getHps();
		} else {
			v = message.getCombatStats().getEhps();
		}
		return v + raidPresenter.getShieldingPerSecond(message.getCharacterName());
	}

}
