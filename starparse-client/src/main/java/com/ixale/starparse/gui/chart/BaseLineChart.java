package com.ixale.starparse.gui.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.ixale.starparse.domain.EffectKey;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.service.impl.Context;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public abstract class BaseLineChart extends BaseChart<LineChart<Number, Number>> {

	protected static final int Y_AXIS_WIDTH = 40,
		LEGEND_HEIGHT = 20,
		OVERLAY_COLORS = 6; // .selected-overlay-5 is the last one

	protected LineChart<Number, Number> chart;

	protected final NumberAxis xAxis = new NumberAxis();
	protected final NumberAxis yAxis = new NumberAxis();

	private LinkedHashMap<String, XYChart.Series<Number, Number>> series = new LinkedHashMap<String, XYChart.Series<Number, Number>>();

	private Integer lastTick = null;
	private Long lastTickFrom = null, lastTickTo = null;

	private int yUpperBoundDefault = 1000;
	private int yTickUnitDefault = 500;
	private Integer yLimit = null;
	private String className;

	private StackPane overlay = null;
	private Button overlayButton = null;

	public BaseLineChart(Context context) {
		super(context);

		chart = new LineChart<Number, Number>(xAxis, yAxis);
		chart.setCreateSymbols(false);
		chart.setAnimated(false);
		chart.setMaxHeight(100);
		chart.setLegendVisible(false);

		xAxis.setAutoRanging(false);
		xAxis.setTickUnit(10);
		xAxis.setMinorTickCount(2);
		xAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number time) {
				return Format.formatTime(Math.round(time.doubleValue() * 1000));
			}

			@Override
			public Number fromString(String arg0) {
				return null;
			}
		});

		yAxis.setAutoRanging(false);
		yAxis.setMinorTickCount(0);
		yAxis.setPrefWidth(Y_AXIS_WIDTH);

		yAxis.setUpperBound(yUpperBoundDefault);
		yAxis.setTickUnit(yTickUnitDefault);
		yAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number object) {
				return (object == null ? "0" : Math.round(object.intValue() * 1.0 / 1000)) + " k";
			}

			@Override
			public Number fromString(String string) {
				return Integer.valueOf(string.substring(0, string.lastIndexOf(" k")));
			}
		});
	}

	protected void setBoundaries(int yUpperBoundDefault, int yTickUnitDefault, Integer yLimit) {
		this.yUpperBoundDefault = yUpperBoundDefault;
		this.yTickUnitDefault = yTickUnitDefault;
		this.yLimit = yLimit;

		yAxis.setUpperBound(yUpperBoundDefault);
		yAxis.setTickUnit(yTickUnitDefault);
	}

	protected void setAsOpaque(String className) {

		this.className = className;
		yAxis.setSide(Side.RIGHT);

		chart.getStyleClass().add(className);
		chart.setPadding(new Insets(0, 0, 0, Y_AXIS_WIDTH));
		//chart.setHorizontalGridLinesVisible(false);
	}

	protected void setAsTransparent(String className) {

		this.className = className;
		xAxis.setOpacity(0);

		chart.getStyleClass().add(className);
		chart.setHorizontalGridLinesVisible(false);
		chart.setVerticalGridLinesVisible(false);
		chart.setAlternativeRowFillVisible(false);
		chart.setPadding(new Insets(0, Y_AXIS_WIDTH, 0, 0));
		chart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent");
	}

	protected void addSerie(String title) {
		series.put(title, null);
	}

	@Override
	public LineChart<Number, Number> getChartNode() {
		return chart;
	}

	public void addData(int x, double... y) {
		if (context.getTickFrom() != null && (lastTickFrom == null || !context.getTickFrom().equals(lastTickFrom))
			|| (context.getTickFrom() == null && lastTickFrom != null)
			|| context.getTickTo() != null && (lastTickTo == null || !context.getTickTo().equals(lastTickTo))
			|| (context.getTickTo() == null && lastTickTo != null)) {
			resetData();
		}

		lastTickFrom = context.getTickFrom();
		lastTickTo = context.getTickTo();

		if (lastTickFrom != null) {
			x += (lastTickFrom / 1000);
			if (xAxis.getLowerBound() < (lastTickFrom / 1000)) {
				xAxis.setLowerBound((lastTickFrom / 1000));
			}
		}

		if (lastTick == null && !series.isEmpty()) {
			for (String serieTitle: series.keySet()) {
				if (series.get(serieTitle) != null) {
					throw new RuntimeException("Serie " + serieTitle + " was not reset");
				}
				series.put(serieTitle, new XYChart.Series<Number, Number>());
				series.get(serieTitle).setName(serieTitle);
			}
		}

		if (lastTick != null && lastTick.compareTo(x) > 0) {
			// already there
			return;

		}

		int s = 0;
		for (String serieTitle: series.keySet()) {
			if (lastTick != null && lastTick.equals(x)) {
				// update very last one
				series.get(serieTitle).getData().set(series.get(serieTitle).getData().size() - 1, new XYChart.Data<Number, Number>(x, y[s]));
			} else {
				// append new one
				series.get(serieTitle).getData().add(new XYChart.Data<Number, Number>(x, y[s]));
			}

			if (yAxis.getUpperBound() < (y[s] * 1.05)) {
				// workaround for Java FX 8 label bug 
				yAxis.setUpperBound(
					Math.min(yAxis.getTickUnit() * (Math.ceil((y[s] * 1.05) / yAxis.getTickUnit())), yLimit == null ? Double.MAX_VALUE : yLimit));
			}
			s++;
		}

		xAxis.setUpperBound(x);
		// fix scaling for long combats (> 10 mins)
		if (xAxis.getTickUnit() == 10 && (x - xAxis.getLowerBound()) > 360) {
			xAxis.setTickUnit(30);
		}

		if (lastTick == null) {
			for (String serieTitle: series.keySet()) {
				chart.getData().add(series.get(serieTitle));
			}
		}
		lastTick = x;
	}

	@Override
	public void resetData() {

		chart.getData().clear();

		if (!series.isEmpty()) {
			for (String serieTitle: series.keySet()) {
				series.put(serieTitle, null);
			}
		}
		lastTick = null;
		lastTickFrom = lastTickTo = null;

		yAxis.setUpperBound(1);
		yAxis.setUpperBound(yUpperBoundDefault);
		yAxis.setTickUnit(yTickUnitDefault);

		xAxis.setLowerBound(0);

		syncOverlaysClasses();
	}

	public static Node getLegendNode(final BaseLineChart... charts) {

		final HBox legendWrapper = new HBox();
		legendWrapper.getStyleClass().add("chart-legend");
		legendWrapper.setMinHeight(LEGEND_HEIGHT);
		legendWrapper.setPrefHeight(LEGEND_HEIGHT);
		legendWrapper.setAlignment(Pos.TOP_RIGHT);
		legendWrapper.setSpacing(10);

		int i, j;
		for (i = 0; i < charts.length; i++) {
			j = 0;
			final HBox h = new HBox();
			h.getStyleClass().add(charts[i].className);
			h.setMinHeight(LEGEND_HEIGHT);
			h.setPrefHeight(LEGEND_HEIGHT);
			h.setPrefWidth(-1);
			h.setSpacing(10);

			for (String serieName: charts[i].series.keySet()) {
				final Rectangle r = new Rectangle(6, 6, Color.TRANSPARENT);
				r.getStyleClass().add("default-color" + j);
				r.getStyleClass().add("legend-symbol");
				final Label l = new Label(serieName);
				l.getStyleClass().add("default-color" + j);
				l.setGraphic(r);

				h.getChildren().add(l);
				j++;
			}
			legendWrapper.getChildren().add(h);
		}

		return legendWrapper;
	}

	public StackPane getOverlayNode() {

		if (overlay == null) {
			overlay = new StackPane();
			overlay.setAlignment(Pos.TOP_LEFT);
			overlay.setManaged(false);
		}

		return overlay;
	}

	public Node getOverlayButtonNode() {
		if (overlayButton == null) {
			overlayButton = new Button("Reset");
			overlayButton.setAlignment(Pos.TOP_RIGHT);
			overlayButton.setMinWidth(30);
			overlayButton.setTooltip(new Tooltip("Reset all active overlays"));
			overlayButton.setTranslateX(Y_AXIS_WIDTH);
			overlayButton.getStyleClass().add("chart-overlay-button");
			overlayButton.setVisible(false);
		}
		return overlayButton;
	}

	public void refreshOverlay() {

		getOverlayNode().getChildren().setAll(overlayButton);

		final Double min = xAxis.getLowerBound();
		final Double max = xAxis.getUpperBound();
		Double from, to;
		int i = 0, j = context.getSelectedEffects().size();

		for (EffectKey effectKey: context.getSelectedEffects().keySet()) {
			if (context.getSelectedEffects().get(effectKey) == null) {
				// no windows present for this event (other combat maybe?)
				continue;
			}
			for (Long[] window: context.getSelectedEffects().get(effectKey)) {
				from = Math.max(min, window[0] / 1000.0);
				if (window[1] == null) {
					to = from + 1; // 1s minimum
				} else {
					to = Math.min(max, window[1] / 1000.0);
				}
				// ensure the phase is at least a 1/50th of the interval long
				to = Math.max(to, from + (max - min) / 50);

				final Rectangle r = new Rectangle(to - from, yAxis.getHeight() / j);

				r.setTranslateY((LEGEND_HEIGHT - 5) + (i * (yAxis.getHeight() / j)));
				r.getStyleClass().add(getOverlayClass(effectKey));
				r.setOpacity(.3);
				final Tooltip t = new Tooltip(
					effectKey.getSource().getName()
						+ " @ " + effectKey.getTarget().getName()
						+ ": " + Format.formatEffectName(effectKey.getEffect(), effectKey.getAbility())
						+ " (" + Format.formatTime(window[0])
						+ (window[1] != null ? " - " + Format.formatTime(window[1]) : "") + ")");
				Tooltip.install(r, t);

				r.translateXProperty().bind(Bindings.multiply(xAxis.widthProperty(), (from - min) / (max - min)).add(Y_AXIS_WIDTH));
				if (window[1] != null) {
					r.widthProperty().bind(Bindings.multiply(xAxis.widthProperty(), (to - from) / (max - min)));
				}
				getOverlayNode().getChildren().add(r);
			}
			i++;
		}

		getOverlayButtonNode().setVisible(j != 0);
	}

	final private static HashMap<EffectKey, String> overlayColors = new HashMap<EffectKey, String>();

	public static String getOverlayClass(EffectKey effectKey) {
		if (!overlayColors.containsKey(effectKey)) {
			int i = 0;
			while (overlayColors.containsValue("selected-overlay-" + i)) {
				i++;
			}
			overlayColors.put(effectKey, "selected-overlay-" + (i % OVERLAY_COLORS));
		}
		return overlayColors.get(effectKey);
	}

	public static void removeOverlayClass(EffectKey effectKey) {
		overlayColors.remove(effectKey);
	}

	public void syncOverlaysClasses() {
		final ArrayList<EffectKey> remove = new ArrayList<EffectKey>();

		for (EffectKey effectKey: overlayColors.keySet()) {
			if (!context.getSelectedEffects().containsKey(effectKey)) {
				// obsolete color reservation (possible combat log change)
				remove.add(effectKey);
			}
		}

		for (EffectKey effectKey: remove) {
			removeOverlayClass(effectKey);
		}
	}
}
