package com.ixale.starparse.gui.table;

import com.ixale.starparse.domain.Event;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.table.item.RaidItem;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class RaidTimeCellFactory implements Callback<TableColumn<RaidItem, Integer>, TableCell<RaidItem, Integer>> {

	@Override
	public TableCell<RaidItem, Integer> call(TableColumn<RaidItem, Integer> p) {
		return new Cell();
	}

	static class Cell extends TableCell<RaidItem, Integer> {

		public Cell() {
			setAlignment(Pos.CENTER_RIGHT);
		}

		@Override
		public void updateItem(Integer item, boolean empty) {
			super.updateItem(item, empty);
			getStyleClass().remove("damage-dealt");

			if (empty || item == null) {
				setText(null);
				return;
			}
			setText(Format.formatTime(item));
			final RaidItem i = getTableView().getItems().get(getIndex());
			if (i.getMessage() != null && Event.Type.DEATH.equals(i.getMessage().getExitEvent())) {
				getStyleClass().add("damage-dealt");
			}
		}
	};
};
