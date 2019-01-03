package com.ixale.starparse.domain;

import javafx.scene.paint.Color;

import com.ixale.starparse.timer.TimerManager;

public class ConfigTimer {

	public static final String SYSTEM_FOLDER = "Built-in:";

	public static class Condition {

		public static final String SELF = "@Self", OTHER = "@Other";

		public enum Type {
			ABILITY_ACTIVATED("Ability activated"),
			DAMAGE("Damage"),
			HEAL("Healing"),
			EFFECT_GAINED("Effect gained"),
			EFFECT_LOST("Effect lost"),
			COMBAT_START("Combat started"),
			COMBAT_END("Combat finished"),
			TIMER_STARTED("Timer started"),
			TIMER_FINISHED("Timer finished"),
			HOTKEY("Hotkey");

			final String label;

			Type(String label) {
				this.label = label;
			}

			public String getLabel() {
				return label;
			}

			public static Type parse(String label) {
				for (final Type t: values()) {
					if (t.label.equals(label)) {
						return t;
					}
				}
				return null;
			}
		}

		private Type type;
		private String source, target, ability, effect;
		private Long sourceGuid, targetGuid, abilityGuid, effectGuid;
		private String timer;
		private Integer hotkeyMod, hotkeyKey;
		private String boss, mode, size;

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public String getAbility() {
			return ability;
		}

		public void setAbility(String ability) {
			this.ability = ability;
		}

		public String getEffect() {
			return effect;
		}

		public void setEffect(String effect) {
			this.effect = effect;
		}

		public String getTimer() {
			return timer;
		}

		public void setTimer(String timer) {
			this.timer = timer;
		}

		public Integer getHotkeyMod() {
			return hotkeyMod;
		}

		public void setHotkeyMod(Integer hotkeyMod) {
			this.hotkeyMod = hotkeyMod;
		}

		public Integer getHotkeyKey() {
			return hotkeyKey;
		}

		public void setHotkeyKey(Integer hotkeyKey) {
			this.hotkeyKey = hotkeyKey;
		}

		public String getBoss() {
			return boss;
		}

		public void setBoss(String boss) {
			this.boss = boss;
		}

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public Long getSourceGuid() {
			return sourceGuid;
		}

		public void setSourceGuid(Long sourceGuid) {
			this.sourceGuid = sourceGuid;
		}

		public Long getTargetGuid() {
			return targetGuid;
		}

		public void setTargetGuid(Long targetGuid) {
			this.targetGuid = targetGuid;
		}

		public Long getAbilityGuid() {
			return abilityGuid;
		}

		public void setAbilityGuid(Long abilityGuid) {
			this.abilityGuid = abilityGuid;
		}

		public Long getEffectGuid() {
			return effectGuid;
		}

		public void setEffectGuid(Long effectGuid) {
			this.effectGuid = effectGuid;
		}

	}

	private String name, folder;
	private Condition trigger, cancel;
	private Integer repeat, volume, soundOffset;
	private Double interval;

	private String color;
	private String audio;
	private Boolean showCenter;

	private String countdownVoice;
	private Integer countdownCount, countdownVolume;

	private Boolean enabled, ignoreRepeated;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public Condition getTrigger() {
		return trigger;
	}

	public void setTrigger(Condition trigger) {
		this.trigger = trigger;
	}

	public Condition getCancel() {
		return cancel;
	}

	public void setCancel(Condition cancel) {
		this.cancel = cancel;
	}

	public Double getInterval() {
		return interval;
	}

	public void setInterval(Double interval) {
		this.interval = interval;
	}

	public Integer getRepeat() {
		return repeat;
	}

	public void setRepeat(Integer repeat) {
		this.repeat = repeat;
	}

	public Color getColor() {
		try {
			return color != null ? Color.web(color) : null;
		} catch (Exception e) {
			return null;
		}
	}

	public void setColor(Color color) {
		this.color = color == null ? null : color.toString();
	}

	public String getAudio() {
		return audio;
	}

	public void setAudio(String audio) {
		this.audio = audio;
	}

	public Integer getSoundOffset() {
		return soundOffset;
	}

	public void setSoundOffset(Integer soundOffset) {
		this.soundOffset = soundOffset;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public Boolean isShowCenter() {
		return showCenter;
	}

	public void setShowCenter(Boolean showCenter) {
		this.showCenter = showCenter;
	}

	public boolean isEnabled() {
		return enabled == null ? false : enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isIgnoreRepeated() {
		return ignoreRepeated == null ? false : ignoreRepeated;
	}

	public void setIgnoreRepeated(Boolean ignoreRepeated) {
		this.ignoreRepeated = ignoreRepeated;
	}

	public String getCountdownVoice() {
		return countdownVoice;
	}

	public void setCountdownVoice(String CountdownVoice) {
		this.countdownVoice = CountdownVoice;
	}

	public Integer getCountdownCount() {
		return countdownCount;
	}

	public void setCountdownCount(Integer countdownCount) {
		this.countdownCount = countdownCount;
	}

	public Integer getCountdownVolume() {
		return countdownVolume;
	}

	public void setCountdownVolume(Integer countdownVolume) {
		this.countdownVolume = countdownVolume;
	}

	public boolean isSystem() {
		return folder != null && folder.startsWith(SYSTEM_FOLDER);
	}

	public boolean isSystemModified() {
		final ConfigTimer original = new ConfigTimer();
		TimerManager.getSystemTimer(this).fillConfig(original);
		return !(isSystem()
			&& ((original.getAudio() == null && getAudio() == null) || (original.getAudio() != null && original.getAudio().equals(getAudio())))
			&& ((original.getCountdownVoice() == null && getCountdownVoice() == null) || (original.getCountdownVoice() != null && original.getCountdownVoice().equals(getCountdownVoice())))
			&& getColor() != null
			&& original.getColor().toString().equals(getColor().toString()));
	}

	public String toString() {
		return name + (folder == null ? "" : " (" + folder + ")");
	}
}
