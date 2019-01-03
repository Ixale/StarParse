package com.ixale.starparse.gui.table;

import java.util.HashMap;

import com.ixale.starparse.gui.table.item.RaidItem;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class CharacterCellFactory<T extends RaidItem> implements Callback<TableColumn<T, String>, TableCell<T, String>> {

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

		public Cell() {
			this.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setGraphic(null);
				setText(null);
				return;
			}

			final RaidItem i = getTableView().getItems().get(getIndex());
			setText(item);

			if (i == null || i.getMessage() == null || i.getMessage().getDiscipline() == null) {
				setGraphic(null);
				return;
			}

			switch (i.getMessage().getDiscipline().getRole()) {
				case TANK:
					setGraphic(getGraphicIcon("shield2.png"));
					break;
				case HEALER:
					setGraphic(getGraphicIcon("heal2.png"));
					break;
				default:
					setGraphic(getGraphicIcon("target2.png"));
					break;
			}
		}
	};
};
