package com.ixale.starparse.gui.table;

import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.table.item.EventItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

public class EventTimeCellFactory<T extends EventItem> implements
		Callback<TableColumn<T, Integer>, TableCell<T, Integer>> {

	@Override
	public TableCell<T, Integer> call(TableColumn<T, Integer> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, Integer> {
		private Tooltip t;

		public Cell() {
		}

		@Override
		public void updateItem(Integer item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setTooltip(null);
				return;
			}

			final EventItem e = getTableView().getItems().get(getIndex());
			setText(Format.formatTime(e.getTickFrom(), true));
			if (t == null) {
				t = new Tooltip();
			}
			t.setText(Format.formatTime(e.getEvent().getTimestamp(), true, true));
			setTooltip(t);
		}
	}
}