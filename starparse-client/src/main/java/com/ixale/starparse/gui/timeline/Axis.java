package com.ixale.starparse.gui.timeline;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.shape.Line;

import com.ixale.starparse.domain.Phase;
import com.ixale.starparse.domain.stats.CombatStats;

public class Axis extends Control {

	private final static int DAMAGE_PHASE_THRESHOLD = 2000; // minimum DPS to show damage phases

	private final Line line;
	private final List<AxisPhase> axisPhases = new ArrayList<AxisPhase>();
	private final List<AxisTick> axisTicks = new ArrayList<AxisTick>();

	private long step = 1;
	private double timelineWidth = 0;

	public Axis(final Timeline tl) {
		super(tl);

		root.setTranslateX(Timeline.HANDLE_WIDTH);

		line = new Line(snap(0), snap(0), snap(1), snap(0));
		line.setTranslateY(Timeline.LINE_MARGIN_TOP);
		line.getStyleClass().add("timeline-axis");

		root.getChildren().add(line);
	}

	public void createTicks(long ticks) {

		axisTicks.clear();

		step = (ticks < 60 * 1000 ? 10 : (ticks < 300 * 1000 ? 30 : (ticks < 600 * 1000 ? 60 : 120))) * 1000;

		for (long i = 0; i < ticks + step; i += step) {
			final AxisTick at = new AxisTick(tl, ticks, Math.min(i, ticks));

			axisTicks.add(at);

			root.getChildren().add(at.getNode());

			at.resize(timelineWidth);
		}
	}

	public void createPhases(final List<Phase> phases, final CombatStats stats) {

		axisPhases.clear();

		if (phases == null || phases.size() == 0) {
			return;
		}

		for (final Phase p: phases) {
			if (p.getType() == Phase.Type.DAMAGE && (stats == null || stats.getDps() < DAMAGE_PHASE_THRESHOLD)) {
				continue;
			}

			final AxisPhase ap = new AxisPhase(tl, p.getName(), p.getType(),
				p.getTickFrom(),
				p.getTickTo() != null ? p.getTickTo() : tl.getTicks());

			axisPhases.add(ap);

			root.getChildren().add(ap.getNode());

			if (tl.getPreferredPhase() != null && tl.getPreferredPhase().equals(p.getName())) {
				// preserve selection
				ap.select();
			}
		}
	}

	public void clearPhaseSelection() {
		for (AxisPhase ap: axisPhases) {
			ap.deselect();
		}
	}

	public void resize(double timelineWidth) {
		this.timelineWidth = timelineWidth;

		line.setEndX(snap(timelineWidth));

		for (final AxisTick at: axisTicks) {
			at.resize(timelineWidth);
		}

		for (final AxisPhase ap: axisPhases) {
			ap.resize(timelineWidth);
		}
	}

	public void reset() {
		axisPhases.clear();
		axisTicks.clear();
		root.getChildren().retainAll(line);
	}
}
