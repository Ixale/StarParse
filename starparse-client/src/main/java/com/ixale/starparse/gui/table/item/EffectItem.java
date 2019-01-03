package com.ixale.starparse.gui.table.item;

import java.util.ArrayList;

import com.ixale.starparse.domain.EffectKey;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class EffectItem {

	public final SimpleStringProperty 
		name = new SimpleStringProperty(),
		source = new SimpleStringProperty(),
		target = new SimpleStringProperty();

	public final SimpleIntegerProperty 
		count = new SimpleIntegerProperty(),
		duration = new SimpleIntegerProperty();

	public final SimpleDoubleProperty 
		pct = new SimpleDoubleProperty();

	public final SimpleBooleanProperty
		isBuff = new SimpleBooleanProperty();

	private final EffectKey effectKey;

	private final ArrayList<Long[]> windows = new ArrayList<Long[]>();

	public EffectItem(final EffectKey effectKey) {
		this.effectKey = effectKey;
	}

	public EffectKey getEffectKey() {
		return effectKey;
	}

	public String getName() {
		return name.get();
	}

	public String getSource() {
		return source.get();
	}

	public String getTarget() {
		return target.get();
	}

	public Integer getCount() {
		return count.get();
	}

	public Integer getDuration() {
		return isBuff.get() ? null : duration.get();
	}

	public Double getPct() {
		return isBuff.get() ? null : pct.get();
	}

	public Boolean getIsBuff() {
		return isBuff.get();
	}

	public void addWindow(Long timeFrom, Long timeTo) {
		windows.add(new Long[]{ timeFrom, timeTo});
	}

	public ArrayList<Long[]> getWindows() {
		return windows;
	}
}
