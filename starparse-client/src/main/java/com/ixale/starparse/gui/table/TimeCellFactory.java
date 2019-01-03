package com.ixale.starparse.gui.table;

import com.ixale.starparse.gui.Format;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class TimeCellFactory<T> implements Callback<TableColumn<T, Integer>, TableCell<T, Integer>> {

	@Override
	public TableCell<T, Integer> call(TableColumn<T, Integer> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, Integer> {

		public Cell() {
			setAlignment(Pos.CENTER_RIGHT);
		}

		@Override
		public void updateItem(Integer item, boolean empty) {
			super.updateItem(item, empty);

			if (empty || item == null) {
				setText(null);
				return;
			}
			setText(Format.formatTime(item));
		}
	};
};
