package com.ixale.starparse.gui.timeline;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.Phase;
import com.ixale.starparse.domain.stats.CombatStats;

public class Timeline extends StackPane {

	public final static int 
		HEIGHT = 40,

		HANDLE_WIDTH = 5,
		HANDLE_HEIGHT = 22,
		HANDLE_MARGIN_TOP = 10,

		LINE_MARGIN_TOP = 20,
		TICK_HEIGHT = 2,

		PHASE_MARGIN_TOP = 3,
		PHASE_HEIGHT = 16;

	private final StackPane root;

	private final Handle leftHandle, rightHandle;
	private final Axis axis;

	private Combat combat = null;
	private Long tickFrom = null, tickTo = null;
	private double timelineWidth = 400;

	private String preferredPhase = null;
	private List<Phase> phases;

	final private ArrayList<TimelineListener> listeners = new ArrayList<TimelineListener>();

	public void addListener(final TimelineListener l) {
		listeners.add(l);
	}

	public ArrayList<TimelineListener> getListeners() {
		return listeners;
	}
 
	public Timeline() {

		root = new StackPane();
		root.setMinHeight(HEIGHT);
		root.setMaxHeight(HEIGHT);
		root.getStyleClass().add("timeline");

		axis = new Axis(this);

		leftHandle = new Handle(this, Handle.Type.LEFT);
		rightHandle = new Handle(this, Handle.Type.RIGHT);

		root.getChildren().addAll(
				axis.getNode(),
				leftHandle.getNode(),
				rightHandle.getNode());

		root.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getClickCount() < 2 || (getTickFrom() == null && getTickTo() == null)) {
					return;
				}
				resetSelection();
				setPreferredPhase(null);

				for (TimelineListener l: getListeners()) {
					l.onSelectedInterval(getTickFrom(), getTickTo());
				}
			}
		});

		this.getChildren().add(root);

		deactivate();
	}

	public void reset() {
		if (combat == null) {
			// already reset
			return;
		}
		combat = null;

		axis.reset();
		resetSelection();
		phases = null;

		deactivate();
	}

	public void resetSelection() {
		getAxis().clearPhaseSelection();
		leftHandle.reset();
		rightHandle.reset();
	}

	public void update(final Combat combat, final List<Phase> phases, final CombatStats stats) {

		if (this.combat == null
				|| (this.combat.getCombatId() != combat.getCombatId())
				|| (!this.combat.getTimeTo().equals(combat.getTimeTo()))) {
			// new combat, reset everything
			reset();
			// FALLTHROUGH

		} else if (!arePhasesUpdated(phases)) {
			// no change
			return;
			// NOTREACHED

		} else {
			// new phases, redraw
			axis.reset();
			// FALLTHROUGH
		}

		if (combat.getTimeTo() == null) {
			// sanity check - keep disabled
			return;
		}

		this.combat = combat;
		this.phases = phases;

		axis.createTicks(getTicks());
		axis.createPhases(phases, stats);
		activate();
	}

	private boolean arePhasesUpdated(final List<Phase> phases) {

		if (this.phases == null && phases == null) {
			return false;
		}

		if ((this.phases != null && phases == null) 
				|| (this.phases == null && phases != null)
				|| (this.phases.size() != phases.size())) {
			return true;
		}

		for (int i = 0; i < phases.size(); i++) {
			if (!this.phases.get(i).equals(phases.get(i))) {
				return true;
			}
		}

		return false;
	}

	public List<Phase> getPhases() {
		return phases;
	}

	private void activate() {
		leftHandle.getNode().setVisible(true);
		rightHandle.getNode().setVisible(true);
	}

	private void deactivate() {
		leftHandle.getNode().setVisible(false);
		rightHandle.getNode().setVisible(false);
	}

	public void resize(double width, double height) {
		super.resize(width, height);

		root.setMaxWidth(width);

		timelineWidth = width - 2 * HANDLE_WIDTH;

		axis.resize(timelineWidth);
		leftHandle.resize(timelineWidth);
		rightHandle.resize(timelineWidth);
	}

	public Axis getAxis() {
		return axis;
	}

	public Handle getLeftHandle() {
		return leftHandle;
	}

	public Handle getRightHandle() {
		return rightHandle;
	}

	public void setTickFrom(Long tick) {
		tickFrom = tick;
	}

	public Long getTickFrom() {
		return tickFrom;
	}

	public void setTickTo(Long tick) {
		tickTo = tick;
	}

	public Long getTickTo() {
		return tickTo;
	}

	public void setPreferredPhase(final String preferredPhase) {
		this.preferredPhase = preferredPhase;
	}

	public String getPreferredPhase() {
		return preferredPhase;
	}

	public Long getTickFromOffset(double offset) {
		return (offset <= 0 || offset >= timelineWidth) 
				? null 
				: Math.round(getTicks() * offset / timelineWidth);
	}

	public double getOffsetFromTick(long tick) {
		return tick * timelineWidth / getTicks();
	}

	public long getTicks() {
		if (combat == null) {
			return 0;
		}
		return combat.getTimeTo() - combat.getTimeFrom();
	}
}
