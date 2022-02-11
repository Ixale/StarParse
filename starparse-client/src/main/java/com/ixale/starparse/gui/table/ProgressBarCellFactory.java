package com.ixale.starparse.gui.table;

import com.ixale.starparse.gui.Format;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class ProgressBarCellFactory<T> implements Callback<TableColumn<T, Double>, TableCell<T, Double>> {

	@Override
	public TableCell<T, Double> call(TableColumn<T, Double> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, Double> {

		private ProgressBar pb = new ProgressBar();
		private Text txt = new Text();
		private StackPane pane = new StackPane(pb, txt);

		public Cell() {
			txt.getStyleClass().add("progress-bar-text");
		}

		@Override
		public void updateItem(Double item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				pb.setPrefHeight(15);
				pb.setPrefWidth(USE_COMPUTED_SIZE);
				pb.setProgress(item / 100.0);
				txt.setText(Format.formatFloat(item));
				setGraphic(pane);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			}
		}
	}
}
