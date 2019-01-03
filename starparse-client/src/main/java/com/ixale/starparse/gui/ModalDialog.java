package com.ixale.starparse.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ModalDialog extends Stage {

	private static final Effect parentEffect = new BoxBlur();

	private static int modalCounter = 0;

	private final Scene scene;
	private final StackPane rootPane;
	private final AnchorPane contentWrapper;

	private final EventHandler<ActionEvent> defaultCloseHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent actionEvent) {
			close();
		}
	};

	public ModalDialog(final Stage parent, final String title, final Parent content, final String buttonText) {
		this(parent, title, content, buttonText, null);
	}

	public ModalDialog(final Stage parent, final String title, final Parent content, final String buttonText, final EventHandler<ActionEvent> closeHandler) {

		super(StageStyle.TRANSPARENT);

		// initialize the dialog
		initOwner(parent);
		initParentEffects(parent);
		initModality(Modality.APPLICATION_MODAL);

		// title

		final Label titleLabel = new Label(title);
		StackPane.setAlignment(titleLabel, Pos.CENTER_LEFT);

		final Button closeButton = new Button("x");
		closeButton.setFocusTraversable(false);
		closeButton.setOnAction(closeHandler != null ? closeHandler : defaultCloseHandler);
		StackPane.setAlignment(closeButton, Pos.CENTER_RIGHT);

		final StackPane titlePane = new StackPane();
		titlePane.getStyleClass().add("modal-title");
		titlePane.getChildren().addAll(
				titleLabel,
				closeButton);

		// content

		content.getStyleClass().add("modal-text");

		contentWrapper = new AnchorPane();
		contentWrapper.getChildren().add(content);

		// window

		final Pane glassPane = new Pane();
		glassPane.getStyleClass().add("modal-dialog-glass");

		final VBox contentPane = new VBox();
		contentPane.getStyleClass().add("modal-dialog-content");
		contentPane.getChildren().setAll(
				titlePane,
				contentWrapper);

		if (buttonText != null) {
			final Button defaultCloseButton = new Button(buttonText);

			defaultCloseButton.setDefaultButton(true);
			defaultCloseButton.setOnAction(closeHandler != null ? closeHandler : defaultCloseHandler);

			final HBox buttons = new HBox();

			buttons.getStyleClass().add("modal-buttons");
			buttons.getChildren().add(defaultCloseButton);

			contentPane.getChildren().add(buttons);
		}

		rootPane = new StackPane();
		rootPane.getStyleClass().add("modal-dialog");
		rootPane.setMaxWidth(1200);
		rootPane.setMaxHeight(800);
		rootPane.getChildren().setAll(
				glassPane,
				contentPane
		);

		rootPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.ESCAPE) {
					close();
				}
			}
		});

		scene = new Scene(rootPane, Color.TRANSPARENT);
		if (javafx.stage.Screen.getPrimary().getDpi() > 96) { // 125% = 96
			// FIXME: workaround for JavaFX bug
			scene.getStylesheets().add("styles120.bss");
		} else {
			scene.getStylesheets().add("styles.bss");
		}

		setScene(scene);

		rootPane.layout();
	}

	public AnchorPane getContentRoot() {
		return contentWrapper;
	}

	private void initParentEffects(final Stage parent) {
		this.showingProperty().addListener(new ChangeListener<Boolean>() {
			@Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasShowing, Boolean isShowing) {
				if (!isShowing) {
					modalCounter--;
					if (modalCounter <= 0) {
						parent.getScene().getRoot().setEffect(null);
						modalCounter = 0;
					}
					return;
				} else {
					modalCounter++;
					if (modalCounter == 1) {
						parent.getScene().getRoot().setEffect(parentEffect);
					}
				}
				scene.getWindow().setX(parent.getX() + parent.getWidth() / 2 - (rootPane.getBoundsInParent().getWidth() / 2));
				scene.getWindow().setY(parent.getY() + parent.getHeight() / 2 - (rootPane.getBoundsInParent().getHeight() / 2));
				toFront();
			}
		});
	}
}
