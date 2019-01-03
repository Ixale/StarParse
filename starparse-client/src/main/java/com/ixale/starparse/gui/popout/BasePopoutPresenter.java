package com.ixale.starparse.gui.popout;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.ConfigPopout;
import com.ixale.starparse.domain.ConfigPopoutDefault;
import com.ixale.starparse.domain.ValueType;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.gui.Popout;
import com.ixale.starparse.service.EventService;
import com.ixale.starparse.service.impl.Context;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

abstract public class BasePopoutPresenter implements Initializable {

	// set in FXML
	final static protected int TITLE_HEIGHT = 20;
	final static protected int FOOTER_HEIGHT = 10;
	final static protected int ITEM_GAP = 1;
	final static protected int ITEM_HEIGHT = 18;
	final static protected int ITEM_COUNT = 8;

	final static protected int DEFAULT_WIDTH = 150;
	final static protected int DEFAULT_HEIGHT = (TITLE_HEIGHT + (ITEM_COUNT * (ITEM_HEIGHT + ITEM_GAP)) + ITEM_HEIGHT); // 8 rows by default
	final static protected int DEFAULT_OFFSET_X = 20;
	final static protected int DEFAULT_OFFSET_Y = 20;

	final static protected int ITEM_WIDTH = DEFAULT_WIDTH;
	final static protected int LABEL_WIDTH = 45;

	@FXML
	public VBox popoutRoot;
	@FXML
	public Rectangle popoutBackground, popoutTitleBackground, resizeSW, resizeSE, resizeN, resizeE;
	@FXML
	public Label popoutTitle;
	@FXML
	public AnchorPane popoutFooter, popoutRight;

	private Popout popout;
	private CheckMenuItem parentMenuItem;

	protected String name;
	private Config config;

	protected double scale = 1.0, opacity = Config.DEFAULT_POPOUT_OPACITY;
	protected boolean bars = true, mouseTransparent = false, solid = false, hideBackground = false, freeform = false;
	protected Color backgroundColor = ConfigPopoutDefault.DEFAULT_BACKGROUND,
		textColor = ConfigPopoutDefault.DEFAULT_TEXT;

	public class Mode {
		public final static String DEFAULT = "default";

		String mode;
		Integer height;
		Pane wrapper;
		String title;

		public Mode(String m, String t, Integer h, Pane w) {
			mode = m;
			title = t;
			height = h;
			wrapper = w;
		}

		public String getTitle() {
			return title;
		}

		public String getMode() {
			return mode;
		}
	}

	protected Mode mode = null;
	private final List<Mode> modes = new ArrayList<>();

	protected static final HashMap<ValueType, Color> barColors = new HashMap<>();
	static {
		barColors.put(ValueType.DAMAGE, ConfigPopoutDefault.DEFAULT_DAMAGE);
		barColors.put(ValueType.HEAL, ConfigPopoutDefault.DEFAULT_HEALING);
		barColors.put(ValueType.THREAT, ConfigPopoutDefault.DEFAULT_THREAT);
		barColors.put(ValueType.FRIENDLY, ConfigPopoutDefault.DEFAULT_FRIENDLY);
	}

	protected int width = DEFAULT_WIDTH;
	protected int height = DEFAULT_HEIGHT;
	protected int offsetX = DEFAULT_OFFSET_X;
	protected int offsetY = DEFAULT_OFFSET_Y;
	protected int itemWidth = ITEM_WIDTH;
	protected int itemHeight = ITEM_HEIGHT;
	protected int itemGap = ITEM_GAP;

	protected int resizeStepW = (itemWidth + itemGap);
	protected int resizeStepH = (itemHeight + itemGap);
	protected int minW = width;
	protected int maxW = width;
	protected int minH = height - 6 * resizeStepH; // 8-6 = 2
	protected int maxH = height + 8 * resizeStepH; // 8+8 = 16

	protected EventService eventService;
	protected Context context;

	private Combat currentCombat = null;
	private CombatStats currentCombatStats = null;

	private ShowingListener listener = null;

	@Autowired
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	@Autowired
	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		// set once
		name = popoutTitle.getText();
	}

	public void setParentMenuItem(final CheckMenuItem parentMenuItem) {
		this.parentMenuItem = parentMenuItem;
	}

	public void setConfig(final Config config) {
		this.config = config;
	}

	public void setListener(final ShowingListener listener) {
		this.listener = listener;
	}

	public CheckMenuItem getParentMenuItem() {
		return parentMenuItem;
	}

	protected boolean hasPopout() {
		return !(popout == null);
	}

	protected Popout getPopout() {
		if (popout == null) {
			popout = new Popout(this, config,
				width, height, offsetX, offsetY,
				resizeStepW, minW, maxW,
				resizeStepH, minH, maxH,
				mouseTransparent, solid, opacity, hideBackground, freeform);
		}
		return popout;
	}

	public boolean isEnabled() {
		return characterConfig().isEnabled();
	}

	public void setEnabled(boolean isEnabled) {
		characterConfig().setEnabled(isEnabled);
	}

	public void showPopout() {

		if (defaultConfig().getBars() != null) {
			setBars(defaultConfig().getBars());
		} else {
			setBars(true);
		}

		if (!modes.isEmpty()) {
			mode = null;
			if (characterConfig().getMode() != null) {
				setMode(characterConfig().getMode());
			}
			if (mode == null) {
				setMode(Mode.DEFAULT);
			}
		}

		if (defaultConfig().getOpacity() != null) {
			setOpacity(defaultConfig().getOpacity());
		}
		if (config.getPopoutDefault().getSolid() != null) {
			setSolid(config.getPopoutDefault().getSolid());
		}
		setBackgroundColor(config.getPopoutDefault().getBackgroundColor());
		setTextColor(config.getPopoutDefault().getTextColor());
		setBarColors(config.getPopoutDefault().getDamageColor(),
			config.getPopoutDefault().getHealingColor(),
			config.getPopoutDefault().getThreatColor(),
			config.getPopoutDefault().getFriendlyColor());

		getPopout().show();

		if (listener != null) {
			listener.onPopoutShowing(this);
		}
	}

	public void setLocation(final Double x, final Double y) {

		if (!hasPopout() || !isEnabled()) {
			return;
		}

		if (x == null || y == null) {
			getPopout().resetLocation();
		} else {
			getPopout().setLocation(x, y);
		}
	}

	public void setOpacity(final double opacity) {
		this.opacity = Math.min(1, Math.max(0.05, opacity));

		if (popoutBackground != null) {
			popoutBackground.setOpacity(solid ? 1 : this.opacity);
		}
		if (popoutTitleBackground != null) {
			popoutTitleBackground.setOpacity(solid ? 1 : (this.opacity < 0.05 ? this.opacity : Math.min(1, this.opacity * 1.5)));
		}
	}

	public void setScale(final double scale) {
		this.scale = Math.min(5, Math.max(0.7, scale));

		if (!hasPopout() || !isEnabled()) {
			return;
		}

		getPopout().setScale(this.scale);
	}

	public void setBars(final boolean bars) {
		this.bars = bars;
	}

	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;

		if (popoutBackground != null) {
			if (!mouseTransparent && backgroundColor.getOpacity() <= 0) {
				// avoid full transparency as it breaks hover (i.e. menu)
				color = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 0.01);
			}
			popoutBackground.setFill(color);
		}
		if (popoutTitleBackground != null) {
			popoutTitleBackground.setFill(color);
		}
	}

	public void setTextColor(final Color color) {
		this.textColor = color;

		popoutTitle.setTextFill(textColor);
	}

	public void setBarColors(final Color damageColor, final Color healingColor, final Color threatColor, final Color friendlyColor) {
		barColors.put(ValueType.DAMAGE, damageColor);
		barColors.put(ValueType.HEAL, healingColor);
		barColors.put(ValueType.THREAT, threatColor);
		barColors.put(ValueType.FRIENDLY, friendlyColor);
	}

	public void repaint(Object source) {

	}

	public void setMouseTransparent(boolean mouseTransparent) {
		if (mouseTransparent == this.mouseTransparent) {
			return;
		}
		this.mouseTransparent = mouseTransparent;
		// refresh background (transparency)
		setBackgroundColor(backgroundColor);

		if (hasPopout()) {
			getPopout().setMouseTransparent(mouseTransparent);
		}
	}

	public void setSolid(boolean solid) {
		if (this.solid == solid) {
			// no change
			return;
		}
		this.solid = solid;
		setOpacity(opacity);

		if (hasPopout()) {
			destroyPopout();
		}
		if (isEnabled()) {
			getPopout().show();
		}
	}

	public void bringPopoutToFront() {
		if (hasPopout() && isEnabled()) {
			getPopout().bringToFront();
		}
	}

	public void hidePopout() {
		if (hasPopout()) {
			getPopout().hide();
		}
	}

	public void destroyPopout() {
		hidePopout();
		if (hasPopout()) {
			getPopout().destroy();
		}
		popout = null;
	}

	final public void updateCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		if (currentCombat != null && combat.getCombatId() != currentCombat.getCombatId()) {
			currentCombat = null;
			currentCombatStats = null;
			resetCombatStats();
		}
		currentCombat = combat;
		currentCombatStats = stats;

		refreshCombatStats(combat, stats);

		bringPopoutToFront();
	}

	abstract protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception;

	abstract public void resetCombatStats();

	public interface ShowingListener {
		public void onPopoutShowing(final BasePopoutPresenter popoutPres);
	}

	public void handleClose(final ActionEvent event) {
		setEnabled(false);
		parentMenuItem.setSelected(false);
		hidePopout();
	}

	public void handleIncreaseOpacity(final ActionEvent event) {
		if (popoutBackground != null) {
			setOpacity((solid ? opacity : popoutBackground.getOpacity()) + 0.05);
		}
		repaint(event.getSource());

		defaultConfig().setOpacity(opacity);
	}

	public void handleDecreaseOpacity(final ActionEvent event) {
		if (popoutBackground != null) {
			setOpacity((solid ? opacity : popoutBackground.getOpacity()) - 0.05);
		}
		repaint(event.getSource());

		defaultConfig().setOpacity(opacity);
	}

	public void handleToggleBars(final ActionEvent event) {
		setBars(!bars);
		repaint(event.getSource());

		defaultConfig().setBars(bars);
	}

	public void handleToggleMode(final ActionEvent event) {
		for (int i = 0; i < modes.size(); i++) {
			if (modes.get(i).equals(mode)) {
				if (i + 1 < modes.size()) {
					setMode(modes.get(i + 1));
				} else {
					setMode(modes.get(0));
				}
				break;
			}
		}

		if (this.mode == null || Mode.DEFAULT.equals(this.mode.mode)) {
			characterConfig().setMode(null);
		} else {
			characterConfig().setMode(this.mode.mode);
		}
	}

	public void setMode(String mode) {
		for (Mode m: modes) {
			if (m.mode.equals(mode)) {
				setMode(m);
				return;
			}
		}
	}

	protected void setMode(Mode mode) {

		if (mode == this.mode) {
			return;
		}
		this.mode = mode;

		for (Mode m: modes) {
			if (m.wrapper == null) {
				continue;
			}
			if (m == this.mode) {
				m.wrapper.setVisible(true);
				m.wrapper.setPrefHeight(m.height - 40);
				setHeight(m.height);
			} else {
				m.wrapper.setVisible(false);
				m.wrapper.setPrefHeight(0);
			}
		}

		if (currentCombat != null && currentCombatStats != null) {
			try {
				resetCombatStats();
				refreshCombatStats(currentCombat, currentCombatStats);
			} catch (Exception e) {
				// silently fail
			}
		}

		if (this.mode == null || Mode.DEFAULT.equals(this.mode.mode)) {
			popoutTitle.setText(name);
		} else {
			popoutTitle.setText(this.mode.title);
		}
	}

	protected Mode getMode() {
		return mode;
	}

	protected List<Mode> getModes() {
		return modes;
	}

	protected void addMode(Mode mode) {
		this.modes.add(mode);
		if (this.mode == null) {
			this.mode = mode;
		}
		if (!popoutModes.containsKey(getName())) {
			popoutModes.put(getName(), new ArrayList<Mode>());
		}
		popoutModes.get(getName()).add(mode);
	}

	protected void setHeight(int height) {
		if (this.height == height) {
			return;
		}
		this.height = height;
		if (hasPopout()) {
			this.popout.setHeight(height);
		}
	}

	public String getName() {
		return name;
	}

	public ConfigPopout defaultConfig() {
		return config.getDefaultCharacter().getPopout(name);
	}

	public ConfigPopout characterConfig() {
		return config.getCurrentCharacter().getPopout(name);
	}

	private final static Map<String, List<Mode>> popoutModes = new HashMap<>();

	public static List<Mode> getPopoutModes(String popoutName) {
		return popoutModes.get(popoutName);
	}
}