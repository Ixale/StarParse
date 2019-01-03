package com.ixale.starparse.gui.timeline;

import javafx.scene.Group;
import javafx.scene.Node;

abstract class Control {
	final protected Timeline tl;
	final protected Group root;

	public Control(final Timeline timeline) {
		tl = timeline;

		root = new Group();
		root.setManaged(false);
	}

	public Node getNode() {
		return root;
	}

	protected double snap(double y) {
		return ((int) y) + .5;
	}

	abstract public void resize(double timelineWidth);
}
