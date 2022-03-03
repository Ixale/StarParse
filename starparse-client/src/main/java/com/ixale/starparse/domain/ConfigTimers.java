package com.ixale.starparse.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

import com.ixale.starparse.domain.ConfigTimer.Condition.Type;
import com.ixale.starparse.gui.Marshaller.SerializeCallback;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class ConfigTimers implements Serializable, SerializeCallback {

	private static final long serialVersionUID = 1L;

	public ConfigTimers() {
	}

	private final List<ConfigTimer> timers = new ArrayList<>();

	@XStreamOmitField
	private transient List<ConfigTimer> allTimers = null;

	public List<ConfigTimer> getTimers() {
		if (timers.isEmpty()) {
			ConfigTimer.Condition condHo = new ConfigTimer.Condition();
			condHo.setType(Type.ABILITY_ACTIVATED);
			condHo.setSource(ConfigTimer.Condition.SELF);
			condHo.setAbilityGuid(801303458480128L);

			final ConfigTimer ho = new ConfigTimer();
			ho.setName("Hold the line");
			ho.setTrigger(condHo);
			ho.setInterval(29.0);
			ho.setColor(Color.AQUA);
			ho.setAudio("Yoda - Laught.wav");

			timers.add(ho);
		}
		if (allTimers == null) {
			allTimers = new ArrayList<>(timers);
		}

		return allTimers;
	}

	@Override
	public void beforeSerialize() {
		timers.clear();
		for (ConfigTimer timer: allTimers) {
			if (Boolean.TRUE.equals(timer.getIsPreview())) {
				continue;
			}
			if (timer.isSystem()) {
				// save only if anything changed
				if (timer.isEnabled() && !timer.isSystemModified()) {
					continue;
				}
			}
			timers.add(timer);
		}
	}

	@Override
	public String toString() {
		return "Timers (" + timers.size() + ")";
	}
}
