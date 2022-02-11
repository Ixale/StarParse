package com.ixale.starparse.gui.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ixale.starparse.domain.AbilityIcon;
import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.FlashMessage;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.StarparseApp;
import com.ixale.starparse.gui.table.item.BaseItem;
import com.ixale.starparse.service.EventService;
import com.ixale.starparse.service.impl.Context;
import com.ixale.starparse.utils.FileLoader;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;

abstract public class BaseStatsPresenter implements Initializable {
	private static final Logger logger = LoggerFactory.getLogger(BaseStatsPresenter.class);

	// table cache
	private static Map<String, Image> icons;

	@FXML
	protected Node root;

	protected EventService eventService;
	protected Context context;

	private Combat currentCombat = null;
	private CombatStats currentStats = null;
	private String currentPlayerName = null;

	// context menu for interval selection
	private final MenuItem miFrom = new MenuItem("Start at selected");
	private final MenuItem miTo = new MenuItem("End at selected");

	final Runnable onBreakdownSelectAction = () -> {
		try {
			refreshCombatStats(currentCombat, currentStats);

		} catch (Exception e) {
			logger.error("General error", e);
			e.printStackTrace();
		}
	};

	protected RequestUpdateListener listener;

	public interface RequestUpdateListener {

		void onUpdateRequested();

		void setFlash(String message, FlashMessage.Type type);
	}

	public void setRequestUpdateListener(final RequestUpdateListener listener) {
		this.listener = listener;
	}

	@Autowired
	public void setContext(final Context context) {
		this.context = context;
	}

	public Node getView() {
		return root;
	}

	@Autowired
	public void setEventService(final EventService eventService) {
		this.eventService = eventService;
	}

	public void toggleBreakdown(final ActionEvent event) throws Exception {
		Platform.runLater(onBreakdownSelectAction);
	}

	protected void toggleWrapper(final Pane wrapper, boolean isVisible, @SuppressWarnings("SameParameterValue") int height) throws Exception {

		if (isVisible) {
			wrapper.setPrefHeight(height);
			wrapper.setMaxHeight(-1);
			wrapper.setPadding(new Insets(0, 0, 5, 0));
			wrapper.setVisible(true);

			if (currentCombat != null) {
				refreshCombatStats(currentCombat, currentStats);
			}

		} else {
			wrapper.setVisible(false);
			wrapper.setPrefHeight(0);
			wrapper.setMaxHeight(0);
			wrapper.setPadding(Insets.EMPTY);
		}
	}

	final public void updateCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		if ((currentCombat != null && (combat == null || combat.getCombatId() != currentCombat.getCombatId()))
				|| !Objects.equals(context.getSelectedPlayer(), currentPlayerName)) {
			currentCombat = null;
			currentStats = null;
			resetCombatStats();
		}
		currentCombat = combat;
		currentStats = stats;
		currentPlayerName = context.getSelectedPlayer();

		if (currentCombat != null) {
			refreshCombatStats(combat, stats);
		}
	}

	abstract protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception;

	abstract public void resetCombatStats();

	protected <T> void resortTable(final TableView<T> table) {
		if (table.getSortOrder().size() > 0 && !table.getItems().isEmpty()) {
			final List<TableColumn<T, ?>> sortOrder = new ArrayList<>(table.getSortOrder());
			table.getSortOrder().clear();
			table.getSortOrder().addAll(sortOrder);
		}
	}

	protected <T> void clearTable(final TableView<T> table) {
		if (table.getItems() != null && !table.getItems().isEmpty()) {
			table.getItems().clear();
		}
	}

	protected void createContextMenu(final TableView<? extends BaseItem> table) {

		miFrom.setOnAction(e -> {
			if (table.getSelectionModel().getSelectedItems().isEmpty()) {
				return;
			}
			context.setTickFrom(table.getSelectionModel().getSelectedItems().get(0).getTickFrom());
			listener.onUpdateRequested();
		});

		miTo.setOnAction(e -> {
			if (table.getSelectionModel().getSelectedItems().isEmpty()) {
				return;
			}
			context
					.setTickTo(table.getSelectionModel().getSelectedItems().get(table.getSelectionModel().getSelectedItems().size() - 1).getTickTo());
			listener.onUpdateRequested();
		});

		final ContextMenu menu = new ContextMenu(miFrom, miTo);
		menu.setOnShowing(new MenuShowingHandler(table));

		table.setContextMenu(menu);
	}

	protected class MenuShowingHandler implements EventHandler<WindowEvent> {

		final TableView<? extends BaseItem> table;

		public MenuShowingHandler(final TableView<? extends BaseItem> table) {
			this.table = table;
		}

		@Override
		public void handle(WindowEvent arg0) {
			if (table.getSelectionModel().getSelectedItems().isEmpty()
					|| table.getSelectionModel().getSelectedItem() == null) {
				miFrom.setDisable(true);
				miTo.setDisable(true);
				return;
			}
			final BaseItem i = table.getSelectionModel().getSelectedItems().get(0);
			miFrom.setDisable(false);
			miFrom.setText("Start at " + Format.formatTime(i.getTickFrom()) + " (" + i.getFullName() + ")");

			final BaseItem j = table.getSelectionModel().getSelectedItems().get(table.getSelectionModel().getSelectedItems().size() - 1);
			miTo.setDisable(false);
			miTo.setText("End at " + Format.formatTime(j.getTickTo()) + " (" + j.getFullName() + ")");
		}
	}

	public static Image getDisciplineIcon(final CharacterDiscipline discipline) {
		return getIcon(discipline.name().toLowerCase() + ".png");
	}

	public static Image getAbilityIcon(final Long guid) {
		return getIcon(AbilityIcon.ico.get(guid));
	}

	private static Image getIcon(String ico) {
		if (ico == null || ico.isEmpty()) {
			return null;
		}
		if (icons == null) {
			icons = new HashMap<>();
			for (final String zip : Arrays.asList(".zip", "2.zip", "3.zip")) {
				try {
					FileLoader.extractZip(new File(StarparseApp.ICONS_DIR + zip), new File(StarparseApp.ICONS_DIR));
				} catch (Exception e) {
					logger.error("Unable to load icons [" + zip + "]: " + e.getMessage());
				}
			}
		}
		if (!icons.containsKey(ico)) {
			try {
				icons.put(ico, new Image("file:" + StarparseApp.ICONS_DIR + "/" + ico));
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Missing icon: " + ico);
				}
				return null;
			}
		}
		return icons.get(ico);
	}
}
