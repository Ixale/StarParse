package com.ixale.starparse.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ixale.starparse.domain.ConfigPopout;
import com.ixale.starparse.gui.popout.BasePopoutPresenter;

import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Popout {

	private static final Logger logger = LoggerFactory.getLogger(Popout.class);

	final Stage wrapper;
	final Scene scene;
	final VBox root;
	final Config config;
	final ConfigPopout configPopout;

	final int offsetX, offsetY;
	double height, defaultHeight, width, defaultWidth;
	int resizeStepW, resizeStepH, minW, maxW, minH, maxH;
	double scale = 1.0, opacity = Config.DEFAULT_POPOUT_OPACITY;

	final BasePopoutPresenter presenter;
	final Label title;
	final Rectangle popoutBackground;
	final Pane popoutRight, popoutFooter;
	final boolean freeform;
	boolean mouseTransparent, solid, hideBackground;

	enum Resize {
		HEIGHT, WIDTH, SCALE
	}

	public Popout(final BasePopoutPresenter presenter, final Config config,
		int width, int height, int offsetX, int offsetY,
		final int resizeStepW, final int minW, final int maxW,
		final int resizeStepH, final int minH, final int maxH,
		final boolean mouseTransparent, final boolean solid, final double opacity, final boolean hideBackground, final boolean freeform) {

		this.root = presenter.popoutRoot;
		this.presenter = presenter;
		this.config = config;
		this.configPopout = presenter.characterConfig();
		this.defaultWidth = this.width = width;
		this.defaultHeight = this.height = height;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.resizeStepW = resizeStepW;
		this.resizeStepH = resizeStepH;
		this.minW = minW;
		this.maxW = maxW;
		this.minH = minH;
		this.maxH = maxH;
		this.title = presenter.popoutTitle;

		this.popoutBackground = presenter.popoutBackground;
		this.popoutFooter = presenter.popoutFooter;
		this.popoutRight = presenter.popoutRight;

		this.mouseTransparent = mouseTransparent;
		this.solid = solid;
		this.opacity = opacity;
		this.hideBackground = hideBackground;
		this.freeform = freeform;

		scene = new Scene(root, Color.TRANSPARENT);
		if (javafx.stage.Screen.getPrimary().getDpi() > 96) { // 125% = 96
			// FIXME: workaround for JavaFX bug
			scene.getStylesheets().add("popouts120.bss");
		} else {
			scene.getStylesheets().add("popouts.bss");
		}

		wrapper = new Stage();
		wrapper.setScene(scene);

		if (solid) {
			wrapper.initStyle(StageStyle.UNDECORATED);
			root.getStyleClass().add("solid");
		} else {
			wrapper.initStyle(StageStyle.TRANSPARENT);
			root.getStyleClass().remove("solid");
		}
		wrapper.setTitle(StarparseApp.TITLE + " - " + presenter.getName());

		root.layout();
		root.setManaged(false);

		addDraggableNode(title);
		wrapper.setWidth(width);

		if (presenter.resizeSW != null) {
			presenter.resizeSW.setCursor(Cursor.SW_RESIZE);
			addScaleNode(presenter.resizeSW, -1);
		}

		if (presenter.resizeSE != null) {
			presenter.resizeSE.setCursor(Cursor.SE_RESIZE);
			addScaleNode(presenter.resizeSE, 1);
		}

		if (presenter.popoutFooter != null) {
			presenter.popoutFooter.opacityProperty().bind(Bindings.when(
				Bindings.or(root.hoverProperty(), Bindings.or(title.pressedProperty(), (presenter.popoutRight != null
					? Bindings.or(presenter.popoutRight.pressedProperty(), presenter.popoutFooter.pressedProperty())
					: presenter.popoutFooter.pressedProperty()))))
				.then(1.0)
				.otherwise(0.0));
		}

		if (presenter.popoutRight != null) {
			presenter.popoutRight.opacityProperty().bind(Bindings.when(
				Bindings.or(root.hoverProperty(), Bindings.or(title.pressedProperty(), (presenter.popoutFooter != null
					? Bindings.or(presenter.popoutFooter.pressedProperty(), presenter.popoutRight.pressedProperty())
					: presenter.popoutRight.pressedProperty()))))
				.then(1.0)
				.otherwise(0.0));
		}
		wrapper.setHeight(height);

		if (presenter.resizeN != null) {
			addResizingNode(presenter.resizeN, 1);
			presenter.resizeN.setCursor(Cursor.N_RESIZE);
		}

		if (presenter.resizeE != null) {
			addResizingNode(presenter.resizeE, -1);
			presenter.resizeE.setCursor(Cursor.E_RESIZE);
		}

		doLayout();

		for (final Node ch: title.getParent().getChildrenUnmodifiable()) {
			if (!(ch instanceof Button)) {
				continue;
			}
			ch.opacityProperty().bind(Bindings.when(
				Bindings.or(root.hoverProperty(), Bindings.or(presenter.popoutFooter.pressedProperty(), title.pressedProperty())))
				.then(1.0)
				.otherwise(0.0));
		}
	}

	public void resetLocation() {

		int x, y;
		if (configPopout.getPositionX() != null) {
			x = configPopout.getPositionX().intValue();
		} else {
			x = (int) (Screen.getPrimary().getVisualBounds().getMaxX() - width - offsetX);
		}
		if (configPopout.getPositionY() != null) {
			y = configPopout.getPositionY().intValue();

		} else {
			y = offsetY;
		}

		// avoid monitor clip
		double[] bounds = StarparseApp.getScreenBounds();
		setLocation(
			Math.min(bounds[1] - width - 10, Math.max(bounds[0], x)),
			Math.max(bounds[2], Math.min(bounds[3] - 100, y)));
	}

	public void show() {
		resetLocation();

		if (configPopout.getScale() != null) {
			presenter.setScale(configPopout.getScale());
		} else {
			presenter.setScale(1.0);
		}

		presenter.setOpacity(this.opacity);

		if (configPopout.getHeight() != null) {
			height = configPopout.getHeight();
		} else {
			height = defaultHeight;
		}
		wrapper.setHeight(height * scale);

		if (configPopout.getWidth() != null) {
			width = configPopout.getWidth();
		} else {
			width = defaultWidth;
		}
		wrapper.setWidth(width * scale);

		doLayout();

		try {
			wrapper.show();
			Win32Utils.removeWindowFromTaskbar(wrapper);
			if (mouseTransparent) {
				Win32Utils.setMouseTransparency(wrapper, mouseTransparent, solid);
			}

		} catch (Exception e) {
			logger.error("Error while showing overlay: " + e.getClass() + ": " + e.getMessage(), e);
		}
	}

	public void hide() {
		wrapper.hide();
		Win32Utils.forgetWindow(wrapper);
	}

	public void setHeight(final int height) {
		this.height = defaultHeight = height;
		wrapper.setHeight(height * scale);
		doLayout();
	}

	public void setWidthAndHeight(final int width, final int height) {
		this.width = defaultWidth = width;
		wrapper.setWidth(width * scale);
		setHeight(height);
	}

	public void setScale(final double scale) {

		this.scale = scale;

		if (scale > 0.95 && scale < 1.05) {
			wrapper.setWidth(width);
			wrapper.setHeight(height);

			root.getTransforms().clear();
			this.scale = 1;

		} else {
			wrapper.setWidth(width * scale);
			wrapper.setHeight(height * scale);

			root.getTransforms().setAll(new Scale(scale, scale));
		}
	}

	public void bringToFront() {
		try {
			Win32Utils.bringWindowToFront(wrapper);

		} catch (Exception e) {
			logger.error("Error while pushing overlay to front: " + e.getClass() + ": " + e.getMessage());
		}
	}

	public void setMouseTransparent(boolean locked) {
		try {
			mouseTransparent = locked;
			if (hideBackground) {
				doLayout();
			}
			Win32Utils.setMouseTransparency(wrapper, locked, solid);

		} catch (Exception e) {
			logger.error("Error while setting locked status: " + e.getClass() + ": " + e.getMessage());
		}
	}

	public void destroy() {
		hide();
		wrapper.close();
		// cleanup so the root can be used again
		scene.setRoot(new VBox());
	}

	/**
	 * ------------------------------------------------------------------------
	 */

	private double initialMouseX = 0, initialPosX = 0,
		initialMouseY = 0, initialPosY = 0,
		initialWidth = 0;

	private void doLayout() {
		if (popoutBackground != null) {
			if (mouseTransparent && hideBackground) {
				popoutBackground.setHeight(0);
			} else {
				popoutBackground.setHeight(height - ((AnchorPane) title.getParent()).getHeight());
				popoutBackground.setWidth(width);
			}

			if (popoutRight != null) {
				((Rectangle) popoutRight.getChildren().get(0)).setHeight(popoutBackground.getHeight() - popoutFooter.getHeight());
				popoutRight.setPrefHeight(popoutBackground.getHeight() - popoutFooter.getHeight());

				AnchorPane.setLeftAnchor(popoutRight, width - popoutRight.getWidth());

				((AnchorPane) title.getParent()).setPrefWidth(width);
				((Rectangle) ((AnchorPane) title.getParent()).getChildren().get(0)).setWidth(width);

				popoutFooter.setPrefWidth(width);
				((Rectangle) popoutFooter.getChildren().get(0)).setWidth(width);
			}
		}
	}

	private void addDraggableNode(final Node node) {

		node.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				initialPosX = wrapper.getX();
				initialPosY = wrapper.getY();
				initialMouseX = me.getScreenX();
				initialMouseY = me.getScreenY();
				me.consume();
			}
		});

		node.setOnMouseDragged(new EventHandler<MouseEvent>() {
			Long last = 0l;

			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				if (last + 50 > System.currentTimeMillis()) {
					return;
				}
				last = System.currentTimeMillis();
				setLocation(initialPosX + me.getScreenX() - initialMouseX,
					initialPosY + me.getScreenY() - initialMouseY);
				configPopout.setPositionX(wrapper.getX());
				configPopout.setPositionY(wrapper.getY());

				me.consume();
			}
		});
	}

	public void setLocation(double x, double y) {
		wrapper.setX(getPosition(x, freeform ? 0 : config.getPopoutSnap()));
		wrapper.setY(getPosition(y, freeform ? 0 : config.getPopoutSnap()));
	}

	public void setResizeDimensions(final int resizeStepW, final int minW, final int maxW,
		final int resizeStepH, final int minH, final int maxH) {
		this.minW = minW;
		this.maxW = maxW;
		this.minH = minH;
		this.maxH = maxH;
		this.resizeStepW = resizeStepW;
		this.resizeStepH = resizeStepH;
	}

	private int getPosition(double pos, Integer snap) {
		if (snap == null || snap <= 0) {
			return (int) pos;
		}

		return (int) Math.round(pos / snap) * snap;
	}

	private void addResizingNode(final Node node, final int dir) {

		node.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				if (dir == 1) {
					initialPosY = wrapper.getHeight();
					initialMouseY = me.getScreenY();
				} else {
					initialPosX = wrapper.getWidth();
					initialMouseX = me.getScreenX();
				}
				me.consume();
			}
		});

		node.setOnMouseDragged(new EventHandler<MouseEvent>() {
			Long last = 0l;

			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				if (last + 50 < System.currentTimeMillis()) {
					if (dir == 1) {
						handleResizeUpdate(me, minH, maxH, resizeStepH, dir);
					} else {
						handleResizeUpdate(me, minW, maxW, resizeStepW, dir);
					}
					last = System.currentTimeMillis();
				}

				// store
				me.consume();
			}
		});

		node.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent me) {
				if (dir == 1) {
					handleResizeUpdate(me, minH, maxH, resizeStepH, dir);
					configPopout.setHeight(height);
				} else {
					handleResizeUpdate(me, minW, maxW, resizeStepW, dir);
					configPopout.setWidth(width);
				}
			}
		});
	}

	private void handleResizeUpdate(final MouseEvent me, final int min, final int max, final int step, final int dir) {

		if (dir == 1) {
			final double height = (int) Math.min(max * scale, Math.max(min * scale,
				initialPosY + getPosition(me.getScreenY() - initialMouseY, (int) (step * scale + 0.5))));
			wrapper.setHeight(height);

			this.height = Math.round(height / scale);

			if (step > 0) {
				// sync size - may be slightly off because of the scale rounding (ugh)
				int mod;
				while ((mod = ((int) (this.height - ((AnchorPane) title.getParent()).getHeight()) % (int) step)) > 0) {
					if (mod > step / 2) {
						this.height++;
					} else {
						this.height--;
					}
				}
			}

		} else {
			final double width = (int) Math.min(max * scale, Math.max(min * scale,
				initialPosX + getPosition(me.getScreenX() - initialMouseX, (int) (step * scale + 0.5))));
			wrapper.setWidth(width);

			this.width = Math.round(width / scale);

			if (step > 0) {
				// sync size - may be slightly off because of the scale rounding (ugh)
				int mod;
				while ((mod = ((int) this.width % (int) step)) > 0) {
					if (mod > step / 2) {
						this.width++;
					} else {
						this.width--;
					}
				}
			}
		}
		doLayout();

		presenter.repaint(me.getSource());
	}

	private void addScaleNode(final Node node, final int dir) {

		node.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				if (freeform) {
					initialPosY = wrapper.getHeight();
					initialMouseY = me.getScreenY();
					initialPosX = wrapper.getWidth();
					initialMouseX = me.getScreenX();

				} else {
					initialWidth = wrapper.getWidth();
					initialPosX = wrapper.getX();
					initialPosY = wrapper.getY();
				}
				me.consume();
			}
		});

		node.setOnMouseDragged(new EventHandler<MouseEvent>() {
			Long last = 0l;

			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				if (last + 50 < System.currentTimeMillis()) {
					if (freeform) {
						handleResizeUpdate(me, (int) (defaultHeight * 0.75), (int) (defaultHeight * 4), 0, 1);
						handleResizeUpdate(me, (int) (defaultWidth * 0.75), (int) (defaultWidth * 4), 0, -1);
					} else {
						handleScaleUpdate(me, dir);
					}
					last = System.currentTimeMillis();
				}

				// store
				me.consume();
			}
		});

		node.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (freeform) {
					handleResizeUpdate(me, (int) (defaultHeight * 0.75), (int) (defaultHeight * 4), 0, 1);
					handleResizeUpdate(me, (int) (defaultWidth * 0.75), (int) (defaultWidth * 4), 0, -1);

					configPopout.setHeight(height);
					configPopout.setWidth(width);

				} else {
					handleScaleUpdate(me, dir);

					configPopout.setScale(scale);
					if (dir < 0) {
						configPopout.setPositionX(wrapper.getX());
					}
				}
			}
		});
	}

	private void handleScaleUpdate(final MouseEvent me, int dir) {
		double x;
		if (dir > 0) {
			if (initialPosX < 0) {
				x = -1 * (initialPosX - me.getScreenX()) / width;
			} else {
				x = (me.getScreenX() - initialPosX) / width;
			}
		} else {
			if (initialPosX > 0) {
				x = (initialPosX + initialWidth - me.getScreenX()) / (width);
			} else {
				x = -1 * (me.getScreenX() - initialPosX - initialWidth) / width;
			}
		}
		double y = (me.getScreenY() - initialPosY) / height;

		presenter.setScale(x > y ? x : y);

		if (dir < 0) {
			wrapper.setX(initialPosX - wrapper.getWidth() + initialWidth);
		}
	}
}
