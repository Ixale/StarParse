package com.ixale.starparse.gui;

import java.awt.Desktop;
import java.net.URI;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class FlashMessage {

	private static final double PARENT_MARGIN = 10d;

	private StackPane rootPane;
	private Label messageLabel;
	private Hyperlink link;
	// private Button closeButton;

	private final AnchorPane parent;

	public enum Type {
		INFO, SUCCESS, ERROR
	}

	public FlashMessage(final AnchorPane parent, final String message) {
		this(parent, message, Type.ERROR);
	}

	public FlashMessage(final AnchorPane parent, final String message, final Type type) {
		this(parent, message, type, null);
	}

	public FlashMessage(final AnchorPane parent, final String message, final Type type, final String url) {

		this.parent = parent;

		messageLabel = new Label(message);
		messageLabel.setWrapText(true);
		messageLabel.setMinHeight(24d);

		rootPane = new StackPane();
		rootPane.maxWidthProperty().bind(Bindings.add(-PARENT_MARGIN, parent.widthProperty()));
		rootPane.maxHeightProperty().bind(Bindings.add(-PARENT_MARGIN, parent.heightProperty()));

		rootPane.getStyleClass().add("flash-message");
		rootPane.getStyleClass().add("flash-" + type.name().toLowerCase());

		if (url != null) {
			link = new Hyperlink(url);
			link.setFocusTraversable(false);
			link.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					try {
						Desktop.getDesktop().browse(new URI(url));
					} catch (Exception e) {
					}
				}
			});
			link.setTextAlignment(TextAlignment.CENTER);
			link.setTextFill(Type.ERROR.equals(type) ? Color.MAROON : Color.GREEN);
			messageLabel.setGraphic(link);
			messageLabel.setContentDisplay(ContentDisplay.RIGHT);
		}
		rootPane.getChildren().setAll(messageLabel/* , closeButton */);
		rootPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				close();
			}
		});

		AnchorPane.setTopAnchor(rootPane, 5d);
		AnchorPane.setRightAnchor(rootPane, 5d);

		rootPane.layout();
		rootPane.toFront();

		parent.getChildren().add(rootPane);
	}

	public void close() {
		parent.getChildren().remove(rootPane);
		rootPane = null;
		messageLabel = null;
		link = null;
		// closeButton = null;
	}
}
