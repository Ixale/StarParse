package com.ixale.starparse.gui.dialog;

import java.net.URL;
import java.util.ResourceBundle;

import com.ixale.starparse.gui.Config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class RaidNotesDialogPresenter extends BaseDialogPresenter {

	@FXML
	private VBox dialogRoot;

	@FXML
	private TextArea raidNote;

	@FXML
	private Button previewButton, saveButton, cancelButton;

	private RaidNotesListener listener;

	@SuppressWarnings("unused")
	private Config config;

	private String originalNote;

	public interface RaidNotesListener {

		void onPreview(String note);

		void onSave(String note);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		setContent(dialogRoot, "Raid Notes", null);
	}

	public void setConfig(final Config config) {
		this.config = config;
	}

	public void setListener(final RaidNotesListener listener) {
		this.listener = listener;
	}

	public void refresh() {
		validate();
	}

	private void reset() {
		raidNote.setText("");
		clearFlash();
		disable(false);
	}

	private void validate() {
		clearFlash();
	}

	public void setNote(final String note) {
		reset();
		originalNote = note;
		raidNote.setText(note);
	}

	@Override
	public void show() {
		validate();
		super.show();
	}

	public void handleSave(final ActionEvent event) {

		clearFlash();

		if (listener != null) {
			listener.onSave(getNote());
		}
		super.handleClose(event);
	}

	public void handlePreview(final ActionEvent event) {

		if (listener != null) {
			listener.onPreview(getNote());
		}
	}

	@Override
	public void handleClose(ActionEvent event) {
		reset();

		if (listener != null) {
			listener.onSave(originalNote);
		}
		super.handleClose(event);
	}

	private void disable(boolean disabled) {
		saveButton.setDisable(disabled);
		previewButton.setDisable(disabled);
		cancelButton.setDisable(disabled);
	}

	private String getNote() {
		String note = raidNote.getText();
		if (note != null) {
			note = note.trim();
			raidNote.setText(note);
		}
		if (note.isEmpty()) {
			return null;
		}
		if (note.length() > 1000) {
			note = note.substring(0, 1000);
			raidNote.setText(note);
		}
		return note;
	}
}
