package com.ixale.starparse.gui.dialog;

import com.ixale.starparse.gui.StarparseApp;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChangelogDialogPresenter extends BaseDialogPresenter {

	private static final String CHANGELOG_TEXT0 = "Version 2.0 (2022-02-11) - 7.0 - Legacy of the Sith\n"
			+ "\n"
			+ "Major revamp to support the new combat logging, while adding new features including:\n"
			+ "\n"
			+ "Group logging\n"
			+ "  - players (and NPCs) around you appear automatically in your parse, even in pvp (see Raid tab/overlays)\n"
			+ "  - although it does not fully replace Raiding (logging range is limited), it is a very good approximation\n"
			+ "\n"
			+ "Point-of-view toggle\n"
			+ "  - you can switch between players (and NPCs) using the upper-left drop-down (displays current character by default)\n"
			+ "  - example: you can swap to 'Bestia', open the 'Damage Taken' tab and immediately see who's dealt what damage to her\n"
			+ "\n"
			+ "Timers expansion\n"
			+ "  - timers can become more powerful now with the new data being logged (e.g. NPC abilities' activations)\n"
			+ "  - 3 separate overlays are now available to be chosen from (main, B, C)\n"
			+ "  - timers can have their source added to their display name to track CDs (e.g. Ixale: Transcendence 00:45)\n"
			+ "  - export/import is now possible (export using the right-click menu on a timer or folder, import via the new button)\n"
			+ "\n"
			+ "Dark-mode\n"
			+ "  - added by popular demand (toggled using the button labeled 'D' in the upper-right corner)\n"
			+ "\n"
			+ "This version should be fully compatible with all pre-7.0 logs (new features are available for the new logs only)."
			+ "\n"
			+ "Please bear in mind this version is based on the last iteration of PTS and may need additional fixes once the 7.0 lands.\n"
			+ "\n";

	public ChangelogDialogPresenter() {

		final VBox content = new VBox();

		content.getChildren().add(new Label(CHANGELOG_TEXT0));
		content.getStyleClass().add("changelog");

//		content.getChildren().add(new Separator());
//		content.getChildren().add(new Label(CHANGELOG_TEXT1));
//
//		content.getChildren().add(new Separator());
//		content.getChildren().add(new Label(CHANGELOG_TEXT2));
//
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
		HBox.setMargin(help, new Insets(2, 0, 0, 0));
		;
		links.getChildren().add(help);

		content.getChildren().add(new Separator());
		content.getChildren().add(links);
//		content.getChildren().add(new Label(CHANGELOG_TEXT3));
//
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
