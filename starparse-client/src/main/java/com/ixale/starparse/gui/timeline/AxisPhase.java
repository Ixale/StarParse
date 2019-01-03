package com.ixale.starparse.gui.timeline;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import com.ixale.starparse.domain.Phase;

public class AxisPhase extends Control {

	private final String name;
	private final long tickFrom, tickTo;
	private final Phase.Type type;

	private final Pane rect;
	private final Label label;

	public AxisPhase(final Timeline tl, final String name, final Phase.Type type, final long tickFrom, final long tickTo) {
		super(tl);
		this.name = name;
		this.type = type;
		// make sure its not overflowing in case of captured delayed abilities
		this.tickFrom = Math.min(tl.getTicks(), tickFrom);
		this.tickTo = Math.min(tl.getTicks(), tickTo);

		rect = new Pane();
		rect.setPrefWidth(tl.getOffsetFromTick(this.tickTo - this.tickFrom));
		rect.setPrefHeight(Timeline.PHASE_HEIGHT);
		rect.setTranslateY(0);
		rect.getStyleClass().add("timeline-phase");
		if (type == Phase.Type.DAMAGE) {
			rect.getStyleClass().add("timeline-phase-damage");
		}
		rect.setTranslateZ(2);

		label = new Label(type == Phase.Type.DAMAGE ? "" : name);
		label.setTranslateX(5);
		label.setTranslateY(1);
		label.getStyleClass().add("timeline-phase-label");
		label.setMaxWidth(rect.getPrefWidth());

		root.getChildren().addAll(rect, label);

		if (this.type == Phase.Type.DAMAGE) {
			root.setTranslateY(Timeline.LINE_MARGIN_TOP + 2);
		} else {
			root.setTranslateY(Timeline.PHASE_MARGIN_TOP);
		}
		root.setTranslateX(tl.getOffsetFromTick(this.tickFrom));

		root.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event e) {
				select();
				e.consume();
			}
		});

		root.setOnMouseEntered(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				rect.getStyleClass().add("timeline-phase-hover");
			}
		});

		root.setOnMouseExited(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				rect.getStyleClass().remove("timeline-phase-hover");
			}
		});
	}

	public void select() {
		tl.getAxis().clearPhaseSelection();

		tl.setPreferredPhase(type == Phase.Type.BOSS ? name : null);

		tl.getLeftHandle().moveToTick(tickFrom);
		tl.getRightHandle().moveToTick(tickTo);
		for (TimelineListener l: tl.getListeners()) {
			l.onSelectedInterval(tickFrom, tickTo);
		}
		rect.getStyleClass().add("timeline-phase-selected");
		label.getStyleClass().add("timeline-phase-label-selected");
	}

	public void deselect() {
		rect.getStyleClass().remove("timeline-phase-selected");
		label.getStyleClass().remove("timeline-phase-label-selected");
	}

	public void resize(double width) {
		//rect.setWidth(tl.getOffsetFromTick(tickTo - tickFrom));
		rect.setPrefWidth(tl.getOffsetFromTick(tickTo - tickFrom));
		label.setMaxWidth(rect.getPrefWidth());
		root.setTranslateX(tl.getOffsetFromTick(tickFrom));
	}
}