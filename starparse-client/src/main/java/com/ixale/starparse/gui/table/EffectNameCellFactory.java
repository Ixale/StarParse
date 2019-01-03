package com.ixale.starparse.gui.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class EffectNameCellFactory<T> implements Callback<TableColumn<T, String>, TableCell<T, String>> {

	@Override
	public TableCell<T, String> call(TableColumn<T, String> p) {
        return new Cell();
    }

	class Cell extends TableCell<T, String> {
		 @Override
         public void updateItem(String item, boolean empty) {
             super.updateItem(item, empty);

             if (empty) {
            	 setText(null);
            	 return;
             }
             setText(item);
         }
	}
}; 
