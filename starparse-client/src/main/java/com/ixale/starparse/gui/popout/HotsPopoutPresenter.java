package com.ixale.starparse.gui.popout;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.parser.Parser.ActorState;
import com.ixale.starparse.time.TimeUtils;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;

public class HotsPopoutPresenter extends BasePopoutPresenter {

	private static final int DEFAULT_SLOT_WIDTH = 100,
		DEFAULT_SLOT_HEIGHT = 50,
		DEFAULT_SLOT_COLS = 2,
		DEFAULT_SLOT_ROWS = 4,
		TITLE_HEIGHT = 20,
		// timeouts
		TIMEOUT_WITH_DURATION = 5000, // 5s
		TIMEOUT_WITHOUT_DURATION = 10 * 60 * 1000; // 10min

	@FXML
	private AnchorPane frames, popoutHeader;
	@FXML
	private Button autosizeButton;

	private static class PlayerFrame {
		int col, row;
		final AnchorPane pane;
		final ActorState state;

		PlayerFrame(final AnchorPane pane, final ActorState state) {
			this.pane = pane;
			this.state = state;
		}
	}

	private final PlayerFrame[][] matrix = new PlayerFrame[6][8];
	private final Line[] lines = new Line[16];
	private final Map<String, PlayerFrame> players = new HashMap<>();
	private final Map<String, Long> ignorePlayers = new HashMap<>();
	private String currentCharacterName;

	private int slotWidth = DEFAULT_SLOT_WIDTH,
		slotHeight = DEFAULT_SLOT_HEIGHT,
		slotCols = DEFAULT_SLOT_COLS,
		slotRows = DEFAULT_SLOT_ROWS;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		offsetY = 400;
		offsetX = (int) Screen.getPrimary().getVisualBounds().getMaxX() - 290;
		itemGap = 0;

		updateDimensions();
		freeform = true; // no scaling

		for (int c = 0; c <= matrix.length; c++) {
			lines[c] = new Line();
			lines[c].setStroke(Color.RED);
			lines[c].setStrokeWidth(1);
			frames.getChildren().add(lines[c]);
		}

		for (int r = 0; r <= matrix[0].length; r++) {
			lines[matrix.length + 1 + r] = new Line();
			lines[matrix.length + 1 + r].setStroke(Color.RED);
			lines[matrix.length + 1 + r].setStrokeWidth(1);
			frames.getChildren().add(lines[matrix.length + 1 + r]);
		}

		// target node
		frames.setOnDragEntered(Event::consume);
		frames.setOnDragOver(dragEvent -> {
			if (dragEvent.getDragboard().hasString()) {
				dragEvent.acceptTransferModes(TransferMode.MOVE);
			}
			dragEvent.consume();
		});
		frames.setOnDragExited(Event::consume);
		frames.setOnDragDropped(dragEvent -> {
			// lookup who is being moved
			final PlayerFrame playerToMove = players.get(dragEvent.getDragboard().getString());
			if (playerToMove == null) {
				return;
			}

			// translate to x,y
			int toCol = (int) Math.floor(dragEvent.getX() / slotWidth);
			int toRow = (int) Math.floor(dragEvent.getY() / slotHeight);

			// anyone already there?
			for (final PlayerFrame frame: players.values()) {
				if (frame.col == toCol && frame.row == toRow) {
					if (frame == playerToMove) {
						// nothing to do
						return;
					}
					// swap places
					frame.col = playerToMove.col;
					frame.row = playerToMove.row;
					break;
				}
			}

			playerToMove.col = toCol;
			playerToMove.row = toRow;

			dragEvent.consume();
			repaintPlayers();
		});

		autosizeButton.setVisible(false);
	}

	private void updateDimensions() {
		height = slotHeight * slotRows + TITLE_HEIGHT;
		width = slotWidth * slotCols;
		itemHeight = slotHeight;
		itemWidth = slotWidth;

		resizeStepW = slotWidth;
		resizeStepH = slotHeight;
		minW = slotWidth * 2;
		maxW = slotWidth * 6;
		minH = slotHeight * 4 + TITLE_HEIGHT;
		maxH = slotHeight * 8 + TITLE_HEIGHT;

		if (hasPopout()) {
			getPopout().setResizeDimensions(
				resizeStepW, minW, maxW,
				resizeStepH, minH, maxH);
		}
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {
		// nothing here
	}

	@Override
	public void resetCombatStats() {
		// nothing here
	}

	public void setActorStates(final Map<Actor, ActorState> actorStates, final String currentCharacterName) {

		this.currentCharacterName = currentCharacterName;
		autosizeButton.setVisible(currentCharacterName != null);

		if (!isEnabled()) {
			return;
		}

		if (players.size() > matrix.length * matrix[0].length) {
			// safety
			return;
		}

		final List<Object[]> newPlayers = new ArrayList<>();
		for (final Actor actor: actorStates.keySet()) {
			if (!(Actor.Type.SELF.equals(actor.getType()) || Actor.Type.PLAYER.equals(actor.getType()))) {
				continue;
			}
			final ActorState state = actorStates.get(actor);
			if (state.hotLast != null) {
				// ignore extremely old
				if (state.hotLast < TimeUtils.getCurrentTime() - TIMEOUT_WITHOUT_DURATION) {
					continue;
				}

				if (!players.containsKey(actor.getName())) {
					if (ignorePlayers.containsKey(actor.getName())) {
						if (ignorePlayers.get(actor.getName()) >= state.hotLast) {
							continue;
						}
						ignorePlayers.remove(actor.getName());
					}
					newPlayers.add(new Object[]{actor.getName(), state});
				}
			}
		}
		if (newPlayers.isEmpty()) {
			return;
		}
		// ensure the players are added according to their HOT application to allow simpler overlay setup
		newPlayers.sort(Comparator.comparing(o -> ((ActorState) o[1]).hotLast));
		for (final Object[] pair: newPlayers) {
			addPlayer((String) pair[0], (ActorState) pair[1]);
		}
	}

	public void resetPlayers() {
		for (final PlayerFrame frame: players.values()) {
			frames.getChildren().remove(frame.pane);
		}
		players.clear();
		for (final PlayerFrame[] playerFrames : matrix) {
			Arrays.fill(playerFrames, null);
		}
		ignorePlayers.clear();
	}

	public void tickHots() {
		for (final PlayerFrame frame: players.values()) {
			boolean hotActive = false;
			int hotStacks = 0;
			if (frame.state.hotEffect != null && frame.state.hotSince != null) {
				hotActive = repaintHot(frame, null);
				if (hotActive) {
					hotStacks = frame.state.hotStacks;
				}
			}
			if (hotActive != frame.pane.getChildren().get(1).isVisible()) {
				frame.pane.getChildren().get(1).setVisible(hotActive);
			}
			if ((hotStacks > 0) != frame.pane.getChildren().get(2).isVisible()) {
				frame.pane.getChildren().get(2).setVisible(hotStacks > 0);
			}
			if (hotStacks > 0) {
				((Label) frame.pane.getChildren().get(2)).setText(String.valueOf(hotStacks));
			}
		}
	}

	public void setSolid(boolean solid) {
		// always transparent
	}

	public void setBackgroundColor(final Color color) {
		super.setBackgroundColor(color);
		this.backgroundColor = Color.web("#000000aa");

		if (popoutBackground != null) {
			popoutBackground.setFill(backgroundColor);
		}
	}

	private void addPlayer(final String characterName, final ActorState state) {

		if (state == null || characterName == null || characterName.isEmpty()) {
			return; // safety
		}

		final AnchorPane pane = new AnchorPane();
		pane.setPrefWidth(slotWidth);
		pane.setPrefHeight(slotHeight);
		pane.setCursor(Cursor.MOVE);
		pane.backgroundProperty().bind(Bindings.when(pane.hoverProperty()).then(
			new Background(new BackgroundFill(Color.web("#00000066"), CornerRadii.EMPTY, Insets.EMPTY)))
			.otherwise((Background) null));

		final Label title = new Label(characterName);
		title.setTextFill(Color.WHITE);
		title.setFont(Font.font("System", 14));
		title.setVisible(!mouseTransparent);

		AnchorPane.setLeftAnchor(title, 15d);

		final Canvas hot = new Canvas(1, 1);
		hot.setOpacity(0.7);

		AnchorPane.setTopAnchor(hot, 1d);
		AnchorPane.setLeftAnchor(hot, 1d);

		final Label stacks = new Label("");
		stacks.setFont(Font.font("System", FontWeight.BOLD, 13));
		stacks.setTextFill(Color.WHITE);
		stacks.getStyleClass().add("outlined");
		stacks.setPrefWidth(15);
		stacks.setPrefHeight(15);
		stacks.setAlignment(Pos.CENTER);

		AnchorPane.setTopAnchor(stacks, 1d);
		AnchorPane.setLeftAnchor(stacks, 1d);

		final Button button = new Button("X");
		button.opacityProperty().bind(Bindings.when(pane.hoverProperty()).then(1.0).otherwise(0.0));
		button.setFont(Font.font("System", 12));
		button.setOnAction(event -> removePlayer(characterName));

		AnchorPane.setTopAnchor(button, 5d);
		AnchorPane.setRightAnchor(button, 18d);

		pane.getChildren().addAll(title, hot, stacks, button);

		// source node
		pane.setOnDragDetected(event -> {
			final Dragboard dragBoard = pane.startDragAndDrop(TransferMode.MOVE);
			dragBoard.setDragView(pane.snapshot(null, null));
			final ClipboardContent content = new ClipboardContent();
			content.put(DataFormat.PLAIN_TEXT, characterName);
			dragBoard.setContent(content);
			event.consume();
		});
		pane.setOnDragDone(dragEvent -> dragEvent.consume());

		// try to find space in visible area
		final PlayerFrame frame = new PlayerFrame(pane, state);

		Integer col = null, row = null;
		m: for (int c = 0; c < slotCols; c++) {
			for (int r = 0; r < slotRows; r++) {
				if (matrix[c][r] == null) {
					col = c;
					row = r;
					matrix[c][r] = frame;
					break m;
				}
			}
		}
		// fall back to anything available
		m: if (col == null) {
			for (int r = 0; r < matrix[0].length; r++) {
				for (int c = 0; c < matrix.length; c++) {
					if (matrix[c][r] == null) {
						col = c;
						row = r;
						matrix[c][r] = frame;
						break m;
					}
				}
			}
		}
		if (col == null) {
			// error?
			return;
		}
		frame.col = col;
		frame.row = row;

		players.put(characterName, frame);

		frames.getChildren().add(pane);

		repaintPlayer(frame);
	}

	public void removePlayer(final String characterName) {
		final PlayerFrame frame = players.remove(characterName);
		if (frame == null) {
			return;
		}
		frames.getChildren().remove(frame.pane);
		matrix[frame.col][frame.row] = null;

		if (frame.col < slotCols && frame.row < slotRows) {
			// use the space for someone else
			m: for (int c = 0; c < matrix.length; c++) {
				for (int r = 0; r < matrix[c].length; r++) {
					if ((c >= slotCols || r >= slotRows) && matrix[c][r] != null) {
						final PlayerFrame other = matrix[c][r];
						other.col = frame.col;
						other.row = frame.row;
						repaintPlayer(other);
						matrix[frame.col][frame.row] = other;
						matrix[c][r] = null;
						break m;
					}
				}
			}
		}

		ignorePlayers.put(characterName, TimeUtils.getCurrentTime());
	}

	@Override
	public void repaint(Object source) {
		super.repaint(source);

		final double width = popoutBackground.getWidth();
		final double height = popoutBackground.getHeight();

		if (source != null) {
			if (resizeN == source || resizeE == source) {
				// slot count change
				slotCols = (int) Math.round(width / slotWidth);
				slotRows = (int) Math.round(height / slotHeight);

				characterConfig().setCols(slotCols);
				characterConfig().setRows(slotRows);

			} else {
				// slot width change
				slotWidth = (int) Math.round(width / slotCols);
				slotHeight = (int) Math.round(height / slotRows);
			}
			updateDimensions();
		}

		frames.setPrefWidth(width);
		frames.setPrefHeight(height);

		for (int c = 0; c <= matrix.length; c++) {
			lines[c].setStartX(c == 0 ? 1 : (c == matrix.length ? frames.getPrefWidth() - 1 : c * slotWidth));
			lines[c].setStartY(0);
			lines[c].setEndX(c == 0 ? 1 : (c == matrix.length ? frames.getPrefWidth() - 1 : c * slotWidth));
			lines[c].setEndY(height);
		}

		for (int r = 0; r <= matrix[0].length; r++) {
			lines[matrix.length + 1 + r].setStartX(0);
			lines[matrix.length + 1 + r].setStartY(r == 0 ? 1 : (r == matrix[0].length ? frames.getPrefHeight() - 1 : r * slotHeight));
			lines[matrix.length + 1 + r].setEndX(width);
			lines[matrix.length + 1 + r].setEndY(r == 0 ? 1 : (r == matrix[0].length ? frames.getPrefHeight() - 1 : r * slotHeight));
		}

		repaintPlayers();
	}

	private void repaintPlayers() {

		for (final PlayerFrame frame: players.values()) {
			frame.pane.setPrefWidth(slotWidth);
			frame.pane.setPrefHeight(slotHeight);

			repaintPlayer(frame);
		}
	}

	private void repaintPlayer(PlayerFrame frame) {
		if (frame.col >= slotCols || frame.row >= slotRows) {
			frame.pane.setVisible(false);
			return;
		}

		// title width
		((Label) frame.pane.getChildren().get(0)).setMaxWidth(slotWidth - 15d);

		// align frame
		AnchorPane.setTopAnchor(frame.pane, (double) frame.row * slotHeight);
		AnchorPane.setLeftAnchor(frame.pane, (double) frame.col * slotWidth);

		// align label
		final Label l = (Label) frame.pane.getChildren().get(0);
		AnchorPane.setBottomAnchor(l, Math.min(slotHeight - 35, 21d));

		// align stacks
		final int w = Math.max(15, Math.min(slotHeight / 4, 30));
		if (w != ((Canvas) frame.pane.getChildren().get(1)).getWidth()) {
			repaintHot(frame, w);
		}
		final Label s = (Label) frame.pane.getChildren().get(2);
		s.setPrefHeight(w);
		s.setPrefWidth(w);

		if (!frame.pane.isVisible()) {
			frame.pane.setVisible(true);
		}
	}

	private boolean repaintHot(final PlayerFrame frame, final Integer newSize) {
		final Integer duration = frame.state.hotDuration;
		final Long since = frame.state.hotSince;
		final Canvas canvas = (Canvas) frame.pane.getChildren().get(1);
		if (newSize != null) {
			canvas.setHeight(newSize);
			canvas.setWidth(newSize);
		}
		if (since == null || (duration != null && duration < (TimeUtils.getCurrentTime() - since - TIMEOUT_WITH_DURATION))) { // arbitrary tolerance
			return false;
		}
		if (TimeUtils.getCurrentTime() - since > TIMEOUT_WITHOUT_DURATION) {
			// expired (out of range etc.)
			return false;
		}

		final GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		if (duration == null) {
			gc.setFill(Color.LIMEGREEN);
		} else {
			gc.setFill(Color.DARKGREEN);
		}
		gc.fillRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), 0, 0);
		if (duration != null) {
			gc.setFill(Color.LIMEGREEN);
			double a = 360 * ((TimeUtils.getCurrentTime() - since) * 1.0 / duration);
			if (a > 360) {
				a = 360;
			}
			double x, y;
			double b = a % 90;
			double r = canvas.getWidth() / 2;
			double offset = r + (r * Math.sin(Math.toRadians(b > 45 ? 90 - b : b)) / Math.sin(Math.toRadians(b > 45 ? b : 90 - b))
				* (b > 45 ? -1 : 1));
			if (a < 45) {
				x = offset;
				y = 0;
				gc.fillPolygon(
					new double[]{0, r, r, x, canvas.getWidth(), canvas.getWidth(), 0},
					new double[]{0, 0, r, y, 0, canvas.getHeight(), canvas.getHeight()}, 7);
			} else if (a < 135) {
				x = canvas.getWidth();
				y = offset;
				gc.fillPolygon(
					new double[]{0, r, r, x, canvas.getWidth(), 0},
					new double[]{0, 0, r, y, canvas.getHeight(), canvas.getHeight()}, 6);
			} else if (a < 225) {
				x = canvas.getWidth() - offset;
				y = canvas.getHeight();
				gc.fillPolygon(
					new double[]{0, r, r, x, 0},
					new double[]{0, 0, r, y, canvas.getHeight()}, 5);
			} else if (a < 315) {
				x = 0;
				y = canvas.getHeight() - offset;
				gc.fillPolygon(
					new double[]{0, r, r, x},
					new double[]{0, 0, r, y}, 4);
			} else {
				x = offset;
				y = 0;
				gc.fillPolygon(
					new double[]{r, r, x},
					new double[]{0, r, y, canvas.getHeight()}, 3);
			}

		}
		return true;
	}

	@Override
	public void showPopout() {

		super.showPopout();

		if (characterConfig().getCols() != null) {
			slotCols = characterConfig().getCols();
		}
		slotWidth = (int) Math.round(popoutBackground.getWidth() / slotCols);
		if (characterConfig().getRows() != null) {
			slotRows = characterConfig().getRows();
		}
		slotHeight = (int) Math.round(popoutBackground.getHeight() / slotRows);

		updateDimensions();
		repaint(null);
	}

	private static class UiEntry {
		final int type;
		final double value;

		UiEntry(int t, double v) {
			type = t;
			value = v;
		}

		public String toString() {
			return type + " (" + value + ")";
		}
	}

	private void detectPosition(final String characterName) throws Exception {
		final File settingsDir = new File(System.getenv("LOCALAPPDATA") + "/SWTOR/swtor/settings/");
		final File uiDir = new File(settingsDir, "GUIProfiles");
		if (!settingsDir.exists() || !settingsDir.isDirectory()
			|| !uiDir.exists() || !uiDir.isDirectory()) {
			throw new Exception("Missing dirs " + settingsDir.exists() + " " + uiDir.exists());
		}

		File clientConfig = null;
		for (String item: Objects.requireNonNull(settingsDir.list((dir, name) -> name != null && !name.isEmpty() && name.contains(characterName)))) {
			final File f = new File(settingsDir, item);
			if (clientConfig == null || (clientConfig.lastModified() < f.lastModified())) {
				clientConfig = f;
			}
		}
		if (clientConfig == null) {
			throw new Exception("Missing client config");
		}

		String profileName = null;
		try {
			for (String line: Files.readAllLines(clientConfig.toPath())) {
				if (line != null && line.startsWith("GUI_Current_Profile = ")) {
					profileName = line.substring("GUI_Current_Profile = ".length()).trim();
					break;
				}
			}
		} catch (Exception e) {
			throw new Exception("Unable to read config: " + e.getMessage(), e);
		}

		if (profileName == null) {
			return;
		}

		final Map<String, UiEntry> ui = new HashMap<>();

		final File profileFile = new File(uiDir, profileName + ".xml");
		if (profileFile.exists()) {
			loadProfile(profileFile, ui);

		} else if ("loc:160030:preferences".equals(profileName)) { // Extended QB
			ui.put("anchorAlignment", new UiEntry(3, 2));
			ui.put("anchorXOffset", new UiEntry(3, 144));
			ui.put("anchorYOffset", new UiEntry(3, -184));
			ui.put("scale", new UiEntry(3, 1));
			ui.put("NumPerRow", new UiEntry(3, 3));
			ui.put("GroupsVisible", new UiEntry(3, 6));
			ui.put("HealthWidth", new UiEntry(3, 102));
			ui.put("HealthHeight", new UiEntry(3, 4));
			ui.put("PartySpacing", new UiEntry(3, 0));

		} else if ("loc:160031:preferences".equals(profileName)) { // Retro
			ui.put("anchorAlignment", new UiEntry(3, 3));
			ui.put("anchorXOffset", new UiEntry(3, 0));
			ui.put("anchorYOffset", new UiEntry(3, 17));
			ui.put("scale", new UiEntry(3, 1));
			ui.put("NumPerRow", new UiEntry(3, 3));
			ui.put("GroupsVisible", new UiEntry(3, 6));
			ui.put("HealthWidth", new UiEntry(3, 102));
			ui.put("HealthHeight", new UiEntry(3, 4));
			ui.put("PartySpacing", new UiEntry(3, 0));

		} else { // Default (loc:160029:preferences)
			ui.put("anchorAlignment", new UiEntry(3, 2));
			ui.put("anchorXOffset", new UiEntry(3, 84));
			ui.put("anchorYOffset", new UiEntry(3, -184));
			ui.put("scale", new UiEntry(3, 1));
			ui.put("NumPerRow", new UiEntry(3, 3));
			ui.put("GroupsVisible", new UiEntry(3, 6));
			ui.put("HealthWidth", new UiEntry(3, 102));
			ui.put("HealthHeight", new UiEntry(3, 4));
			ui.put("PartySpacing", new UiEntry(3, 0));

		}

		// position
		double minX = Screen.getPrimary().getVisualBounds().getMinX();
		double maxX = Screen.getPrimary().getVisualBounds().getMaxX();
		double minY = Screen.getPrimary().getVisualBounds().getMinY();
		double maxY = Screen.getPrimary().getVisualBounds().getMaxY();

		slotWidth = (int) Math.round((13 + ui.get("HealthWidth").value + ui.get("PartySpacing").value * 1.25) * ui.get("scale").value);
		slotCols = 2; //(int) (ui.get("GroupsVisible").value > ui.get("NumPerRow").value ? ui.get("NumPerRow").value : ui.get("GroupsVisible").value);
		slotHeight = (int) Math.round((41 + ui.get("HealthHeight").value + ui.get("PartySpacing").value * 1.2) * ui.get("scale").value);
		slotRows = 4; //(ui.get("GroupsVisible").value > ui.get("NumPerRow").value ? 8 : 4);

		double x = ui.get("anchorXOffset").value + 1;
		double y = ui.get("anchorYOffset").value;
		double h = slotHeight * (ui.get("GroupsVisible").value > ui.get("NumPerRow").value ? 8 : 4); /* raid control button */
		double w = slotWidth * Math.min(ui.get("GroupsVisible").value, ui.get("NumPerRow").value);

		int pos = (int) ui.get("anchorAlignment").value;
		switch (pos) {
			case 1:
				// top left
				break;
			case 7:
				// top mid
				x += getCenterX(w, x, minX, maxX);
				break;
			case 4:
				// top right
				x += maxX - w;
				break;
			case 6:
				// center right
				x += maxX - w;
				y += getCenterY(h, y, minY, maxY);
				break;
			case 5:
				// bottom right
				x += maxX - w;
				y += maxY - h;
				break;
			case 8:
				// bottom mid
				x += getCenterX(w, x, minX, maxX);
				y += maxY - h;
				break;
			case 2:
				// bottom left
				y += maxY - h;
				break;
			case 3:
				// center left
				y += getCenterY(h, y, minY, maxY);
				break;
			case 9:
				// center
				x += getCenterX(w, x, minX, maxX);
				y += getCenterY(h, y, minY, maxY);
				break;
			default:
				throw new Exception("Unknown position: " + ui.get("anchorAlignment").value);
		}
		y += -21 + (30 /* * ui.get("scale").value) */); /* popout title - ops frames title */

		characterConfig().setPositionX(x);
		characterConfig().setPositionY(y);
		characterConfig().setWidth((double) slotWidth * slotCols);
		characterConfig().setHeight((double) slotHeight * slotRows + TITLE_HEIGHT);
		characterConfig().setCols(slotCols);
		characterConfig().setRows(slotRows);
		updateDimensions();
	}

	private void loadProfile(final File profileFile, final Map<String, UiEntry> ui) throws Exception {

		final Pattern p = Pattern.compile("^\\<(?<name>[a-z]+) Type=\"(?<type>[0-9]+)\" Value=\"(?<value>[\\-0-9.]+)\" /\\>$",
			Pattern.CASE_INSENSITIVE);
		try {
			Boolean read = null;
			for (String line: Files.readAllLines(profileFile.toPath())) {
				if (Boolean.TRUE.equals(read)) {
					if (line != null && line.contains("</RaidFrames>")) {
						break;
					}
					assert line != null;
					final Matcher m = p.matcher(line.trim());
					if (!m.matches()) {
						throw new Exception("Unable to match: " + line);
					}
					ui.put(m.group("name"), new UiEntry(Integer.parseInt(m.group("type")), Double.parseDouble(m.group("value"))));
					continue;
				}
				if (line != null && line.contains("<RaidFrames>")) {
					read = true;
				}
			}
		} catch (Exception e) {
			throw new Exception("Unable to read config: " + e.getMessage(), e);
		}
	}

	private long getCenterX(double w, double x, double minX, double maxX) {
		double offset = w / 2;
		double anchor = (maxX - minX) / 2;
		if ((offset + x) > anchor) { // overflow
			offset = offset + (offset + x - anchor);
		}
		return Math.round(anchor - offset);
	}

	private long getCenterY(double h, double y, double minY, double maxY) {
		double offset = h / 2;
		double anchor = (maxY - minY) / 2;
		if ((offset + y) > anchor) { // overflow
			offset = offset + (offset + y - anchor);
		}
		return Math.round(anchor - offset);
	}

	public void handleAutosize(ActionEvent event) {
		if (currentCharacterName == null) {
			return;
		}
		try {
			detectPosition(currentCharacterName);
			hidePopout();
			showPopout();
			bringPopoutToFront();

		} catch (Exception e) {
			// tough luck
		}

	}

	public void setMouseTransparent(boolean mouseTransparent) {

		if (mouseTransparent == this.mouseTransparent) {
			return;
		}

		popoutBackground.setVisible(!mouseTransparent);
		popoutHeader.setVisible(!mouseTransparent);

		for (final PlayerFrame frame: players.values()) {
			frame.pane.getChildren().get(0).setVisible(!mouseTransparent); // title

		}
		for (final Line line: lines) {
			line.setVisible(!mouseTransparent);
		}
		repaintPlayers();

		super.setMouseTransparent(mouseTransparent);
	}
}
