package com.ixale.starparse.gui.table.item;

import javafx.beans.property.SimpleLongProperty;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

public abstract class BaseItem {

	public final SimpleLongProperty tickFrom = new SimpleLongProperty(),
		tickTo = new SimpleLongProperty();

	public Long getTickFrom() {
		return tickFrom.getValue();
	}

	public Long getTickTo() {
		return tickTo.getValue();
	}

	abstract public String getFullName();

	public static void showTooltip(Node row, Tooltip t, MouseEvent event) {
		t.show(row, event.getScreenX() + 11, event.getScreenY() + 11);
	}
}
