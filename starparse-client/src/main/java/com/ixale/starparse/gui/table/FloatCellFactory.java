package com.ixale.starparse.gui.table;

import com.ixale.starparse.gui.Format;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class FloatCellFactory<T> implements Callback<TableColumn<T, Double>, TableCell<T, Double>> {
	
	@Override
	public TableCell<T, Double> call(TableColumn<T, Double> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, Double> {

		public Cell() {
            setAlignment(Pos.CENTER_RIGHT);
		}

		@Override
		public void updateItem(Double item, boolean empty) {
			super.updateItem(item, empty);

			if (empty || item == null) {
				setText(null);
				return;
			}
			setText(Format.formatFloat(item));
		}
	};
}; 
