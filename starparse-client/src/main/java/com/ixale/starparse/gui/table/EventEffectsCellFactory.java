package com.ixale.starparse.gui.table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.gui.main.BaseStatsPresenter;
import com.ixale.starparse.gui.table.item.BaseItem;
import com.ixale.starparse.gui.table.item.EventItem;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.util.Callback;

public class EventEffectsCellFactory<T extends EventItem> implements Callback<TableColumn<T, String>, TableCell<T, String>> {

	private static final Map<Effect, EffectNode> nodes = new HashMap<>();
	private static final List<Color> colorsAct = Arrays.asList(Color.DARKSEAGREEN, Color.LIMEGREEN, Color.LIGHTGREEN, Color.DARKGREEN,
		Color.GOLDENROD);
	private static final List<Color> colorsGiv = Arrays.asList(Color.DARKRED, Color.MAROON, Color.PURPLE, Color.VIOLET, Color.web("0xcc3333"),
		Color.web("0x9933cc"));
	private static final Color colorAbs = Color.web("0x30cccd");

	static class EffectNode {
		Tooltip tooltip;
		Color color;

		private ImageView img;

		public ImageView getGraphicIcon(final Long guid) {
			final Image icon = BaseStatsPresenter.getAbilityIcon(guid);
			if (icon != null) {
				if (img == null) {
					img = new ImageView();
					img.setFitHeight(26);
					img.setFitWidth(26);
				}
				img.setImage(icon);
				return img;
			}
			return null;
		}
	}

	public static final void reset() {
		nodes.clear();
	}

	@Override
	public TableCell<T, String> call(TableColumn<T, String> p) {
		return new Cell();
	}

	class Cell extends TableCell<T, String> {

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
				return;
			}

			final EventItem e = getTableView().getItems().get(getIndex());
			if (e.getEffects() == null || e.getEffects().isEmpty()) {
				setText(null);
				setGraphic(null);
				return;
			}

			setText("");
			final HBox g = new HBox();
			g.setTranslateY(2);
			g.setSpacing(1);
			for (final Effect effect: e.getEffects()) {
				// filter out white noise
				if (effect.isActivated()
					|| effect.isAbsorption()
					|| Effect.Type.ACT.equals(effect.getType())) {
					// OK!

				} else {
					continue;
				}

				if (Effect.Type.GIV.equals(effect.getType())
					&& (e.getEvent().getTarget() == null || !e.getEvent().getTarget().equals(effect.getTarget()))) {
					continue;
				}

				final EffectNode en;
				if (nodes.get(effect) == null) {
					en = new EffectNode();
					final String s;
					if (effect.isAbsorption()) {
						s = "Shielding: " + effect.getEffect().getName()
							+ (effect.getSource() != null && !Actor.Type.SELF.equals(effect.getSource().getType())
								? " (" + effect.getSource().getName() + ")"
								: "");
					} else if (Effect.Type.GIV.equals(effect.getType())) {
						if (Boolean.TRUE.equals(effect.getTarget().isHostile())) {
							s = "Debuff: " + effect.getEffect().getName();
						} else {
							s = "Buff: " + effect.getEffect().getName();
						}
					} else {
						s = effect.getEffect().getName();
					}
					en.tooltip = new Tooltip(s);
					if (effect.getAbility() != null) {
						en.tooltip.setGraphic(en.getGraphicIcon(effect.getAbility().getGuid()));
					}

					// resolve color
					if (effect.isAbsorption()) {
						en.color = colorAbs;
					} else if (Effect.Type.GIV.equals(effect.getType()) && Boolean.TRUE.equals(effect.getTarget().isHostile())) {
						en.color = colorsGiv.get((int) (effect.getEffect().getGuid() % colorsGiv.size()));
					} else {
						en.color = colorsAct.get((int) (effect.getEffect().getGuid() % colorsAct.size()));
					}
					nodes.put(effect, en);
				} else {
					en = nodes.get(effect);
				}
				final Rectangle node = new Rectangle(8, 15);
				node.setStrokeType(StrokeType.INSIDE);
				node.setFill(en.color);

				node.setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						node.setStroke(Color.GRAY);
						BaseItem.showTooltip(node, en.tooltip, event);
					}
				});
				node.setOnMouseExited(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						node.setStroke(null);
						en.tooltip.hide();
					}
				});
				g.getChildren().add(node);
			}
			setGraphic(g);
		}
	};
};
