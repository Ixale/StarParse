package com.ixale.starparse.gui.table;

import com.ixale.starparse.domain.RankClass;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class RankCellFactory<T> implements Callback<TableColumn<T, Integer>, TableCell<T, Integer>> {

	private static final int[] bands = new int[]{
			95,
			75,
			50,
			25};

	public RankCellFactory() {
	}

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

			getStyleClass().clear();
			if (empty || item == null) {
				setText(null);
				return;
			}
			if (item >= 0) {
				setText(String.valueOf(item));
				for (final int band : bands) {
					if (item >= band) {
						getStyleClass().add("rank-" + band);
						return;
					}
				}
				getStyleClass().add("rank-0");

			} else if (item == RankClass.Reason.TICK_TOO_LOW.getCode()) {
				setText("--");
				getStyleClass().add("rank");
			} else {
				// pending / not supported
				setText("");
				getStyleClass().add("rank");
			}

		}
	}
}
