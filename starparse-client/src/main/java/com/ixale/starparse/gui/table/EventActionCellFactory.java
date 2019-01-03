package com.ixale.starparse.gui.table;

import java.util.HashMap;

import com.ixale.starparse.gui.table.item.EventItem;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class EventActionCellFactory<T extends EventItem> implements Callback<TableColumn<T, String>, TableCell<T, String>> {

	private static final HashMap<String, Image> icons = new HashMap<String, Image>();

	@Override
	public TableCell<T, String> call(TableColumn<T, String> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, String> {

		private ImageView img;

		private ImageView getGraphicIcon(final String actionIcon) {
			if (img == null) {
				img = new ImageView();
				img.setFitHeight(15);
				img.setFitWidth(15);
			}
			if (!icons.containsKey(actionIcon)) {
				icons.put(actionIcon, new Image("img/icon/" + actionIcon));
			}
			img.setImage(icons.get(actionIcon));
			return img;
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			getTableRow().getStyleClass().remove("event-activated");
			if (empty) {
				setText(null);
				setGraphic(null);
				getTableRow().setStyle(null);
				return;
			}

			setGraphic(getGraphicIcon(getTableView().getItems().get(getIndex()).getActionIcon()));

			if (item.equals("Activated")) {
				getTableRow().getStyleClass().add("event-activated");
			}
		}
	};
};
