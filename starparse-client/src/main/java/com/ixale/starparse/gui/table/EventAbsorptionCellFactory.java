package com.ixale.starparse.gui.table;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Paint;

import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.table.item.EventItem;

public class EventAbsorptionCellFactory<T extends EventItem> extends NumberCellFactory<T> {

	public EventAbsorptionCellFactory(boolean isEmptyOnZero) {
		super(isEmptyOnZero, null);
	}

	@Override
	public TableCell<T, Integer> call(TableColumn<T, Integer> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, Integer> {

		public Cell() {
			setAlignment(Pos.CENTER_RIGHT);
			getStyleClass().add("absorbed");
		}

		@Override
		public void updateItem(Integer item, boolean empty) {
			super.updateItem(item, empty);

			if (empty || (isEmptyOnZero && item == null)) {
				setText(null);
				setTooltip(null);
				return;
			}
			setText(Format.formatAdaptive(item));
		}
	};
};
