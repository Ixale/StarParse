package com.ixale.starparse.gui.table;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class RankCellFactory<T> implements Callback<TableColumn<T, Integer>, TableCell<T, Integer>> {

	private static int[] bands = new int[] {
			95,
			75,
			50,
			25 };
	private static Color[] colors = new Color[] {
			Color.web("#e58738"),
			Color.web("#b82cb8"),
			Color.web("#0070FF"),
			Color.LIMEGREEN, //web("#1EFF00"),
			Color.web("#666"),
	};

	public RankCellFactory() {
	}

	@Override
	public TableCell<T, Integer> call(TableColumn<T, Integer> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, Integer> {
		public Cell() {
			setAlignment(Pos.CENTER_RIGHT);
			setTextFill(Color.BLACK);
		}

		@Override
		public void updateItem(Integer item, boolean empty) {
			super.updateItem(item, empty);

			if (empty || item == null) {
				setText(null);
				return;
			}
			if (item == -1) {
				// not supported
				setText("");
			} else if (item == -2) {
				// too low
				setText("--");
			} else {
				setText(String.valueOf(item));
			}

			for (int i = 0; i < bands.length; i++) {
				if (item >= bands[i]) {
					setTextFill(colors[i]);
					return;
				}
			}
			setTextFill(colors[4]);
		}
	};
};
