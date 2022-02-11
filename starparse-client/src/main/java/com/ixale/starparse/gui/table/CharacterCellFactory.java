package com.ixale.starparse.gui.table;

import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.CharacterRole;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.main.BaseStatsPresenter;
import com.ixale.starparse.gui.table.item.RaidItem;
import com.ixale.starparse.service.impl.Context;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.function.Supplier;

public class CharacterCellFactory<T extends RaidItem> implements Callback<TableColumn<T, String>, TableCell<T, String>> {

	private static final HashMap<String, Image> icons = new HashMap<>();

	private final Supplier<Context> context;

	public CharacterCellFactory(final Supplier<Context> context) {
		this.context = context;
	}

	@Override
	public TableCell<T, String> call(TableColumn<T, String> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, String> {

		private ImageView img;
		private ImageView img2;
		private VBox imgWrapper;

		private Node getGraphicIcon(final CharacterDiscipline discipline) {
			final HBox hbox = new HBox();
			hbox.setSpacing(5);
			if (img == null) {
				img = new ImageView();
				img.setFitHeight(20);
				img.setFitWidth(20);
				imgWrapper = new VBox();
				imgWrapper.setAlignment(Pos.CENTER);
				imgWrapper.getChildren().setAll(img);
			}
			if (img2 == null) {
				img2 = new ImageView();
				img2.setFitHeight(20);
				img2.setFitWidth(20);
			}
			final String actionIcon = CharacterRole.TANK.equals(discipline.getRole())
					? "icon_tank.png"
					: (CharacterRole.HEALER.equals(discipline.getRole())
					? "icon_heal.png" : "icon_dps.png");
			if (!icons.containsKey(actionIcon)) {
				icons.put(actionIcon, new Image("img/icon/" + actionIcon));
			}
			img.setImage(icons.get(actionIcon));
			img2.setImage(BaseStatsPresenter.getDisciplineIcon(discipline));
			hbox.getChildren().addAll(imgWrapper, img2);
			return hbox;
		}

		public Cell() {
			this.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				getStyleClass().remove("damage-dealt");
				setGraphic(null);
				setText(null);
				return;
			}
			if (context.get().isActorHostile(Format.getRealNameEvenForFakePlayer(item))) {
				if (!getStyleClass().contains("damage-dealt")) {
					getStyleClass().add("damage-dealt");
				}
			} else {
				getStyleClass().remove("damage-dealt");
			}
			setText(item);

			final RaidItem i = getTableView().getItems().get(getIndex());
			if (i == null || i.getMessage() == null || i.getMessage().getDiscipline() == null) {
				setGraphic(null);
				return;
			}

			setGraphic(getGraphicIcon(i.getMessage().getDiscipline()));
		}
	}
}
