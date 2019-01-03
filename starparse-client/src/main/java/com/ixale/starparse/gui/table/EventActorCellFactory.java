package com.ixale.starparse.gui.table;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.gui.table.item.EventItem;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class EventActorCellFactory<T extends EventItem> implements
	Callback<TableColumn<T, String>, TableCell<T, String>> {

	public enum Type {
		SOURCE, TARGET
	}

	final Type type;

	public EventActorCellFactory(Type type) {
		this.type = type;
	}

	@Override
	public TableCell<T, String> call(TableColumn<T, String> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, String> {

		public Cell() {
			this.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			getStyleClass().removeAll("event-actor-enemy", "event-actor-ally");

			if (empty) {
				setText(null);
				return;
			}

			final EventItem e = getTableView().getItems().get(getIndex());
			final Actor actor;

			if (type == Type.SOURCE && e.getEvent().getSource() != null) {
				actor = e.getEvent().getSource();
			} else if (type == Type.TARGET && e.getEvent().getTarget() != null) {
				actor = e.getEvent().getTarget();
			} else {
				setText(item);
				return;
			}

			if (actor.getType() == Actor.Type.SELF) {
				setText("You");
			} else {
				setText(item);
			}

			switch (actor.getType()) {
			case NPC:
				getStyleClass().add("event-actor-enemy");
				break;
			case PLAYER:
				if (Boolean.TRUE.equals(actor.isHostile())) {
					getStyleClass().add("event-actor-enemy");
				} else {
					getStyleClass().add("event-actor-ally");
				}
				break;
			default:
				break;
			}
		}
	};
};
