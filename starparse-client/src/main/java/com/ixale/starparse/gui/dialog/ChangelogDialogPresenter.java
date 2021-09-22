package com.ixale.starparse.gui.dialog;

import com.ixale.starparse.gui.StarparseApp;

import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChangelogDialogPresenter extends BaseDialogPresenter {

	private static final String CHANGELOG_TEXT0 = "Version 1.4 (2019-12-18) - Onslaught\n\n"
		+ "- Dxun operation support added\n"
		+ "\n"
		;

	private static final String CHANGELOG_TEXT1 = "Version 1.3 (2018-08-03) - Minor release\n\n"
		+ "- Scyva & Izax support added\n"
		+ "\n"
		;

	private static final String CHANGELOG_TEXT2 = "Version 1.2 (2017-04-10) - 5.2 compatibility, Overlays fix\n\n"
		+ "- 5.2 support, namely the Iokath/Tyth encounter\n"
		+ "- Attempt to solve the pesky 'overlays-make-taskbar-visible-in-windows-10' bug\n"
		+ "\n"
		;

	private static final String CHANGELOG_TEXT3 = "Version 1.1 (2016-04-27) - Raid Notes, Challenges Leaderboard and more\n\n"
		+ "- New feature: Raid Notes - admins of Raid Groups can set a raid-wide note via 'Notes' button (raiders see it on their 'Raid Notes' overlay)\n"
		+ "- You can now select multiple combats (SHIFT or CTRL) to get their summarized stats (only general overview supported at the moment)"
		;

	public ChangelogDialogPresenter() {

		final VBox content = new VBox();

		content.getChildren().add(new Label(CHANGELOG_TEXT0));
		content.getStyleClass().add("changelog");

		content.getChildren().add(new Separator());
		content.getChildren().add(new Label(CHANGELOG_TEXT1));

		content.getChildren().add(new Separator());
		content.getChildren().add(new Label(CHANGELOG_TEXT2));

		final HBox links = new HBox();
		links.setSpacing(0);
		links.getChildren().add(new Label("StarParse website now provides"));
		final Hyperlink challenges = StarparseApp.createHyperlink("Challenges Leaderboard", "http://ixparse.com/challenges");
		HBox.setMargin(challenges, new Insets(2, 0, 0, 0));
		links.getChildren().add(challenges);
		links.getChildren().add(new Label("in addition to"));
		final Hyperlink features = StarparseApp.createHyperlink("Features Overview", "http://ixparse.com/features");
		HBox.setMargin(features, new Insets(2, 0, 0, 0));
		links.getChildren().add(features);
		links.getChildren().add(new Label("and"));
		final Hyperlink help = StarparseApp.createHyperlink("Troubleshooting page", "http://ixparse.com/help");
		HBox.setMargin(help, new Insets(2, 0, 0, 0));;
		links.getChildren().add(help);

		content.getChildren().add(links);
		content.getChildren().add(new Separator());
		content.getChildren().add(new Label(CHANGELOG_TEXT3));

		final HBox mailtext = new HBox();
		mailtext.getChildren().add(new Label("Please report any weirdness or coolness to"));
		mailtext.setSpacing(0);
		final Hyperlink mail = StarparseApp.createHyperlink("ixale@ixparse.com", "mailto:ixale@ixparse.com");
		HBox.setMargin(mail, new Insets(2, 0, 0, 0));

		mailtext.getChildren().add(mail);
		mailtext.getChildren().add(new Label("and follow the"));

		final Hyperlink ref = StarparseApp.createHyperlink("Twitter news feed", "https://twitter.com/starparse");
		HBox.setMargin(ref, new Insets(2, 0, 0, 0));
		mailtext.getChildren().add(ref);

		content.getChildren().add(new Separator());
		content.getChildren().add(mailtext);

		setContent(content, "Changelog", "Cool, thanks");
	}

}
