package com.ixale.starparse.gui.table;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;

import com.ixale.starparse.domain.Event;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.table.item.EventItem;
import com.ixale.starparse.parser.Helpers;

public class EventValueCellFactory<T extends EventItem> extends NumberCellFactory<T> {

	public EventValueCellFactory(boolean isEmptyOnZero) {
		super(isEmptyOnZero, null);
	}

	@Override
	public TableCell<T, Integer> call(TableColumn<T, Integer> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, Integer> {

		public Cell() {
			setAlignment(Pos.CENTER_RIGHT);
			getStyleClass().set(0, "table-cell");
		}

		@Override
		public void updateItem(Integer item, boolean empty) {
			super.updateItem(item, empty);

			setTooltip(null);
			if (empty || (isEmptyOnZero && item == null)) {
				setText(null);
				return;
			}

			setText(Format.formatAdaptive(item));

			final Event e = getTableView().getItems().get(getIndex()).getEvent();
			if (Helpers.isEffectDamage(e)) {
				if (e.getMitigation() != null) {
					getStyleClass().set(0, "mitigated");
				} else {
					getStyleClass().set(0, "damage-dealt");
				}
			} else if (Helpers.isEffectHeal(e)) {
				getStyleClass().set(0, "healing-eff-done");
			} else {
				getStyleClass().set(0, "table-cell");
			}
		}
	};
};
