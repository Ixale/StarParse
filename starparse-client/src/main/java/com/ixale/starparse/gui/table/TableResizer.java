package com.ixale.starparse.gui.table;

import java.util.ArrayList;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.ResizeFeatures;
import javafx.util.Callback;

@SuppressWarnings("rawtypes")
public class TableResizer implements Callback<ResizeFeatures, Boolean> {

	private ArrayList<TableColumn> tableCols = null;

	private final TableColumn[] resizedCols;
	private final double[] ratios;

	public TableResizer(final TableColumn[] resizedCols, double[] ratios) {
		this.resizedCols = resizedCols;
		this.ratios = ratios;
	}

	@Override
	public Boolean call(ResizeFeatures event)
	{
		final TableView<?> tv = event.getTable();
		if (tableCols == null) {
			tableCols = new ArrayList<TableColumn>();
			for (final TableColumn tc: tv.getColumns()) {
				if (tc.getColumns().size() > 0) {
					for (final Object tcc: tc.getColumns()) {
						tableCols.add((TableColumn) tcc);						
					}
				} else {
					tableCols.add(tc);
				}
			}
		}

		double columnWidths = 0, hiddenRatios = 0;
		int hiddenCount = 0;

		cols: for (int i = 0; i < tableCols.size(); i++) {
			for (int j = 0; j < resizedCols.length; j++) {
				if (tableCols.get(i).equals(resizedCols[j])) {
					if (!resizedCols[j].isVisible()) {
						hiddenCount++;
						hiddenRatios += ratios[j];
					}
					continue cols;
				}
			}
			if (!tableCols.get(i).isVisible()) {
				continue;
			}
			columnWidths += tableCols.get(i).getWidth();
		}

		final double[] visibleRatios = ratios.clone();
		if (hiddenCount > 0) {
			for (int j = 0; j < resizedCols.length; j++) {
				if (!resizedCols[j].isVisible()) {
					visibleRatios[j] = 0;
				} else {
					visibleRatios[j] += (hiddenRatios / (ratios.length - hiddenCount));
				}
			}
		}

		for (int j = 0; j < resizedCols.length; j++) {
			resizedCols[j].setPrefWidth(Math.round(tv.widthProperty().getValue() - columnWidths - 16) * visibleRatios[j]);
		}

		return true;
	}
};
