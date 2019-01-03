package com.ixale.starparse.gui.timeline;

import com.ixale.starparse.gui.Format;

import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class AxisTick extends Control {

	private final Line line;
	private final Text label;
	private long tick;

	public AxisTick(final Timeline tl, long ticks, long tick) {
		super(tl);

		this.tick = tick;

		if (tick == ticks) {
			line = new Line(snap(0), snap(-Timeline.PHASE_HEIGHT), snap(0), snap(Timeline.TICK_HEIGHT));
		} else {
			line = new Line(snap(0), snap(0), snap(0), snap(Timeline.TICK_HEIGHT));
		}
		line.getStyleClass().add("timeline-tick-line");

		label = new Text(ticks != tick && ticks > 60000 && (ticks - tick < 20000)
			? "" // don't display last 2 ticks for long fights as they clip
			: Format.formatTime(tick));
		label.setTranslateY(Timeline.TICK_HEIGHT + 9);
		label.setWrappingWidth(22);
		label.setTextAlignment(TextAlignment.CENTER);
		label.getStyleClass().add("timeline-tick");

		root.getChildren().addAll(line, label);
		root.getStyleClass().add("timeline-tick");
		root.setTranslateY(Timeline.LINE_MARGIN_TOP);
	}

	public void resize(double width) {
		line.setTranslateX(tl.getOffsetFromTick(tick));
		label.setTranslateX(line.getTranslateX() - label.getWrappingWidth() / 2);
	}
}