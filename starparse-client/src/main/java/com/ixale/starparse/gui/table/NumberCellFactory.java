package com.ixale.starparse.gui.table;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;

import com.ixale.starparse.gui.Format;

public class NumberCellFactory<T> implements Callback<TableColumn<T, Integer>, TableCell<T, Integer>> {

	final protected boolean isEmptyOnZero, isThousands;
	final protected String className;

	public NumberCellFactory() {
		this(false, null, false);
	}

	public NumberCellFactory(boolean isEmptyOnZero, final String className) {
		this(isEmptyOnZero, className, false);
	}

	public NumberCellFactory(boolean isEmptyOnZero, final String className, boolean isThousands) {
		this.isEmptyOnZero = isEmptyOnZero;
		this.isThousands = isThousands;
		this.className = className;
	}

	@Override
	public TableCell<T, Integer> call(TableColumn<T, Integer> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, Integer> {
		public Cell() {
			setAlignment(Pos.CENTER_RIGHT);
			getStyleClass().set(0, className);
		}

		@Override
		public void updateItem(Integer item, boolean empty) {
			super.updateItem(item, empty);

			if (empty || (isEmptyOnZero && item == null)) {
				setText(null);
			} else {
				if (isThousands) {
					setText(Format.formatThousands(item));

				} else {
					setText(Format.formatAdaptive(item));
				}
			}
		}
	}

	;
};
