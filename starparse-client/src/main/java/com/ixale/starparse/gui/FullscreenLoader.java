package com.ixale.starparse.gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class FullscreenLoader extends Stage {

	// https://www.swtor.com/community/archive/index.php/t-736389.html
	public static String[] quotes = new String[]{
			"Mathematically impossible. --Enkay / DNT",
			"Very well; then we DIE together! --Kreshon",
			"I have been armed with 24 distinct weapons technologies --XRR-3",
			"I have waited 20 000 years, but I will wait no longer! --SOA",
			"Focus on the little one. What little one? The LITTLE one! They're all LITTLE! --Jarg & Sorno",
			"Titan 6 will accept surrender at any time. --Titan 6",
			"Titan 6 needs no other allies. Titan 6 stands alone. --Titan 6",
			"Even the force obeys the call. --Styrak",
			"Now you'll see real power. --Styrak",
			"We may fall, but fear remains eternal. --The Dread Guards",
			"Spin ever downward. Fall. Disappear. --Raptus",
			"You haven't the resolve to continue. Let go. --Raptus",
			"Sanity is a prison. Let madness release you. --Raptus",
			"I see it in your mind: you despair. --Raptus",
			"You will not win. You are not allowed to win! --Corruptor Zero",
			"Weakness must be discarded. --Brontes",
			"Witness the potential... of perfection. --Brontes",
	};

	private final String title;
	private final Label titleLabel;
	private final HBox quoteBox;
	private final Label quoteLabel;
	private final Label quoteAuthor;
	private Timer quoteTimer;
	private int safetyTimer;

	private String getQuote() {
		return quotes[ThreadLocalRandom.current().nextInt(0, quotes.length - 1)];
	}

	public FullscreenLoader(final Stage parent, final String title) {

		super(StageStyle.TRANSPARENT);

		initOwner(parent);
		initModality(Modality.APPLICATION_MODAL);

		this.title = title;
		titleLabel = new Label(title);
		StackPane.setAlignment(titleLabel, Pos.CENTER);
		titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-loader-color");

		quoteBox = new HBox();
		quoteBox.setAlignment(Pos.CENTER);

		quoteLabel = new Label();
		quoteLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: -fx-loader-color");
		quoteAuthor = new Label();
		quoteAuthor.setStyle("-fx-font-style: italic; -fx-text-fill: -fx-loader-color");

		quoteBox.getChildren().setAll(quoteLabel, quoteAuthor);

		final VBox contentPane = new VBox();
		contentPane.setAlignment(Pos.CENTER);
		contentPane.setPadding(new Insets(0, 0, 100, 0));
		contentPane.getChildren().setAll(
				new ImageView(new Image("img/icon/spinner.gif", 30, 30, true, true)),
				titleLabel,
				quoteBox);

		final StackPane rootPane = new StackPane();
		rootPane.getChildren().setAll(contentPane);
		rootPane.setStyle("-fx-background-color: -fx-backdrop");

		final Scene scene = new Scene(rootPane, Color.TRANSPARENT);
		scene.getStylesheets().setAll(parent.getScene().getStylesheets());

		final int windowInset = 10; // main window padding (shadow)
		showingProperty().addListener((observableValue, wasShowing, isShowing) -> {
			if (!wasShowing && isShowing) {
				scene.getStylesheets().setAll(parent.getScene().getStylesheets());
				scene.getWindow().setWidth(parent.getWidth() - 2 * windowInset);
				scene.getWindow().setHeight(parent.getHeight() - 2 * windowInset);
				scene.getWindow().setX(parent.getX() + windowInset);
				scene.getWindow().setY(parent.getY() + windowInset);
				toFront();
			}
		});

		setScene(scene);

		rootPane.layout();

		safetyTimer = 24; // 24 * 5 == 2 minutes
		quoteTimer = null;
		showingProperty().addListener((observable, oldValue, newValue) -> {
			if (Boolean.TRUE.equals(newValue)) {
				if (quoteTimer == null) {
					safetyTimer = 24;
					quoteTimer = new Timer(true);
					quoteTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							Platform.runLater(() -> {
								final String[] q = getQuote().split("--");
								quoteLabel.setText(q[0]);
								quoteAuthor.setText("--" + q[1]);

								if (safetyTimer-- < 0) {
									// deadlock?
									FullscreenLoader.this.hide();
								}
							});
						}
					}, 0, 5000);
				}
			} else {
				if (quoteTimer != null) {
					quoteTimer.cancel();
				}
				quoteTimer = null;
			}
		});
	}

	public void setPercent(final Integer percent) {
		titleLabel.setText(title + " (" + percent + "%)");
	}

}
