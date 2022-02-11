package com.ixale.starparse.gui.table;

import com.ixale.starparse.gui.main.BaseStatsPresenter;
import com.ixale.starparse.gui.table.item.BaseStatsItem;
import com.ixale.starparse.gui.table.item.EventItem;
import com.ixale.starparse.parser.Helpers;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class DamageAbilityNameCellFactory<T> implements Callback<TableColumn<T, String>, TableCell<T, String>> {

	@Override
	public TableCell<T, String> call(TableColumn<T, String> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, String> {

		private ImageView img;

		private ImageView getGraphicIcon(final long guid) {
			final Image icon = BaseStatsPresenter.getAbilityIcon(guid);
			if (icon != null) {
				if (img == null) {
					img = new ImageView();
					img.setFitHeight(19);
					img.setFitWidth(19);
					img.setTranslateY(1.0);
				}
				img.setImage(icon);
				return img;
			}
			return null;
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setGraphic(null);
				setText(null);
				return;
			}

			Long guid = null;
			final T it = getTableView().getItems().get(getIndex());
			if (it instanceof BaseStatsItem) {
				guid = ((BaseStatsItem) it).guid;
			} else if (it instanceof EventItem) {
				if (Helpers.isEffectAbilityActivate(((EventItem) it).getEvent())) {
					guid = ((EventItem) it).getEvent().getAbility().getGuid();
				}
			}
			if (guid != null) {
				setGraphic(getGraphicIcon(guid));
			} else {
				setGraphic(null);
			}

			setText(item);
		}
	};
};
