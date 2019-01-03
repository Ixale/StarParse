package com.ixale.starparse.gui.timeline;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

import com.ixale.starparse.gui.Format;

public class Handle extends Control {

	enum Type {
		LEFT, RIGHT
	}

	private final Type type;
	private final Polygon shape, inner;
	private final Text label;
	private final StackPane sign;

	private double initialX = 0, translateX = 0, timelineWidth = 0;

	public Handle(final Timeline tl, final Type t) {
		super(tl);
		type = t;

		if (type == Type.LEFT) {
			shape = new Polygon(
				Timeline.HANDLE_WIDTH, 0,
				Timeline.HANDLE_WIDTH / 2, 1,
				0, Timeline.HANDLE_WIDTH,
				0, Timeline.HANDLE_HEIGHT - Timeline.HANDLE_WIDTH,
				Timeline.HANDLE_WIDTH / 2, Timeline.HANDLE_HEIGHT - 1,
				Timeline.HANDLE_WIDTH, Timeline.HANDLE_HEIGHT
				);
			inner = new Polygon(
				Timeline.HANDLE_WIDTH - 1, 2,
				Timeline.HANDLE_WIDTH / 2 + 1, 3,
				1, Timeline.HANDLE_WIDTH,
				1, Timeline.HANDLE_HEIGHT - Timeline.HANDLE_WIDTH,
				Timeline.HANDLE_WIDTH / 2 + 1, Timeline.HANDLE_HEIGHT - 3,
				Timeline.HANDLE_WIDTH - 1, Timeline.HANDLE_HEIGHT - 2
				);

		} else {
			shape = new Polygon(
				0, 0,
				Timeline.HANDLE_WIDTH / 2, 1,
				Timeline.HANDLE_WIDTH, Timeline.HANDLE_WIDTH,
				Timeline.HANDLE_WIDTH, Timeline.HANDLE_HEIGHT - Timeline.HANDLE_WIDTH,
				Timeline.HANDLE_WIDTH / 2, Timeline.HANDLE_HEIGHT - 1,
				0, Timeline.HANDLE_HEIGHT
				);
			inner = new Polygon(
				1, 2,
				Timeline.HANDLE_WIDTH / 2 - 1, 2,
				Timeline.HANDLE_WIDTH - 1, Timeline.HANDLE_WIDTH,
				Timeline.HANDLE_WIDTH - 1, Timeline.HANDLE_HEIGHT - Timeline.HANDLE_WIDTH,
				Timeline.HANDLE_WIDTH / 2 - 1, Timeline.HANDLE_HEIGHT - 2,
				1, Timeline.HANDLE_HEIGHT - 2
				);
		}
		shape.setCursor(Cursor.H_RESIZE);
		shape.getStyleClass().add("timeline-handle");
		inner.setCursor(Cursor.H_RESIZE);
		inner.getStyleClass().add("timeline-handle-inner");

		label = new Text("0");
		label.getStyleClass().add("timeline-handle-label");
		label.setCursor(Cursor.H_RESIZE);

		sign = new StackPane();
		sign.setTranslateY(Timeline.HANDLE_HEIGHT / 2 - 10);
		sign.getStyleClass().add("timeline-handle-sign");
		sign.getChildren().add(label);
		sign.setCursor(Cursor.H_RESIZE);
		sign.setVisible(false);

		root.getChildren().addAll(shape, inner, sign);
		root.setTranslateY(Timeline.HANDLE_MARGIN_TOP);

		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				initialX = me.getScreenX() - translateX;
				me.consume();
			}
		});

		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				tl.getAxis().clearPhaseSelection();

				if (type == Type.LEFT) {
					translateX = Math.max(0, Math.min(
						me.getScreenX() - initialX,
						tl.getRightHandle().translateX - Timeline.HANDLE_WIDTH - 1
						));
					tl.setTickFrom(tl.getTickFromOffset(translateX));

				} else {
					translateX = Math.min(timelineWidth + Timeline.HANDLE_WIDTH, Math.max(
						me.getScreenX() - initialX,
						tl.getLeftHandle().translateX + Timeline.HANDLE_WIDTH + 1
						));
					tl.setTickTo(tl.getTickFromOffset(translateX - Timeline.HANDLE_WIDTH));

				}
				root.setTranslateX(translateX);
				updateLabel();
				me.consume();
			}
		});

		root.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				tl.setPreferredPhase(null);
				for (TimelineListener l: tl.getListeners()) {
					l.onSelectedInterval(tl.getTickFrom(), tl.getTickTo());
				}
				me.consume();
			}
		});
	}

	public double getTranslateX() {
		return translateX;
	}

	public void moveToTick(final Long tick) {
		if (type == Type.LEFT) {
			tl.setTickFrom(tick);
			translateX = tl.getOffsetFromTick(tick == null ? 0 : tick);
		} else {
			tl.setTickTo(tick);
			translateX = tl.getOffsetFromTick(tick == null ? tl.getTicks() : tick) + Timeline.HANDLE_WIDTH;
		}
		root.setTranslateX(translateX);
		updateLabel();
	}

	private void updateLabel() {
		if (type == Type.LEFT) {
			label.setText(tl.getTickFrom() != null ? Format.formatTime(tl.getTickFrom()) : "");
			sign.setTranslateX(snap(-label.getBoundsInLocal().getWidth() - Timeline.HANDLE_WIDTH / 2 - 4));

			sign.setVisible(translateX > 5);

		} else {
			label.setText(tl.getTickTo() != null ? Format.formatTime(tl.getTickTo()) : "");
			sign.setTranslateX(snap(3));

			sign.setVisible(timelineWidth - translateX > 5);
		}
	}

	public void resize(double timelineWidth) {
		this.timelineWidth = timelineWidth;

		if (type == Type.LEFT) {
			if (tl.getTickFrom() != null) {
				translateX = tl.getOffsetFromTick(tl.getTickFrom());
			} else {
				translateX = 0;
			}

		} else if (type == Type.RIGHT) {
			if (tl.getTickTo() != null) {
				translateX = tl.getOffsetFromTick(tl.getTickTo()) + Timeline.HANDLE_WIDTH;
			} else {
				translateX = timelineWidth + Timeline.HANDLE_WIDTH;
			}
		}
		root.setTranslateX(translateX);
	}

	public void reset() {
		if (type == Type.LEFT) {
			tl.setTickFrom(null);
		} else {
			tl.setTickTo(null);
		}
		resize(this.timelineWidth);
		updateLabel();
	}
}