package com.ixale.starparse.gui.table;

import java.util.HashMap;

import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.Entity;
import com.ixale.starparse.domain.EntityGuid;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.gui.table.item.EventItem;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class EventActionCellFactory<T extends EventItem> implements Callback<TableColumn<T, Event>, TableCell<T, Event>> {

	private static final HashMap<String, Image> icons = new HashMap<>();

	@Override
	public TableCell<T, Event> call(TableColumn<T, Event> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, Event> {

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
		public void updateItem(Event item, boolean empty) {
			super.updateItem(item, empty);

			getTableRow().getStyleClass().remove("event-activated");
			if (empty) {
				setText(null);
				setGraphic(null);
				getTableRow().setStyle(null);
				return;
			}

			setGraphic(getGraphicIcon(getActionIcon(
					EntityGuid.fromGuid(item.getEffect().getGuid()),
					EntityGuid.fromGuid(item.getAction().getGuid())
			)));

			if (EntityGuid.AbilityActivate.getGuid() == item.getEffect().getGuid()) {
				getTableRow().getStyleClass().add("event-activated");
			}
		}
	}

	private String getActionIcon(final EntityGuid effect, final EntityGuid action) {
		if (effect != null) {
			switch (effect) {
				case AbilityActivate:
					return "up.png";
				case AbilityDeactivate:
					return "down.png";
				case AbilityCancel:
					return "cancel.png";
				case AbilityInterrupt:
					return "delete.png";
				case FailedEffect:
					return "cancel.png";

				case FallingDamage:
					return "fire.png";
				case Damage:
					return "target2.png";
				case Heal:
					return "heal2.png";

				case Taunt:
					return "shield.png";
				case ModifyThreat:
				case NoLongerSuspicious:
					return "shield-strike.png";
				case EnterCombat:
				case ExitCombat:
					return "lighting.png";

				case Death:
					return "fire.png";
				case Revived:
					return "heart-stroke.png";

				case Crouch:
				case LeaveCover:
					return "lighting.png";
				default:
			}
		}

		if (action != null) {
			switch (action) {
				case ApplyEffect:
					return "up2.png";
				case RemoveEffect:
					return "down2.png";
				case Spend:
					return "inject.png";
				case Restore:
					return "eject.png";
				default:
			}
		}

		return "lighting.png";
	}
}
