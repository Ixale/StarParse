package com.ixale.starparse.gui.dialog;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatLog;
import com.ixale.starparse.domain.ServerName;
import com.ixale.starparse.gui.Config;
import com.ixale.starparse.gui.FlashMessage.Type;
import com.ixale.starparse.service.ParselyService;
import com.ixale.starparse.service.ParselyService.ParselyCombatInfo;
import com.ixale.starparse.service.impl.Context;
import com.ixale.starparse.utils.FileLoader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class UploadParselyDialogPresenter extends BaseDialogPresenter {

	@FXML
	private VBox dialogRoot;

	@FXML
	private Label uploadLabel, settingsLabel;

	@FXML
	private TextArea uploadNote;

	@FXML
	private RadioButton visibilityPrivate, visibilityGuildOnly, visibilityPublic;

	@FXML
	private Button settingsButton, saveButton, cancelButton;

	private UploadParselyListener listener;

	private CombatLog combatLog;
	private List<Combat> allCombats, selectedCombats;

	private Config config;
	private ParselyService parselyService;
	private Context context;

	public interface UploadParselyListener {

		void onUploadSaved(String link);

		void onUploadSettings();
	}

	@Autowired
	public void setParselyService(final ParselyService parselyService) {
		this.parselyService = parselyService;
	}

	@Autowired
	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		setContent(dialogRoot, "Combat Log Upload", null);
	}

	public void setConfig(final Config config) {
		this.config = config;
	}

	public void setListener(final UploadParselyListener listener) {
		this.listener = listener;
	}

	public void refresh() {
		validate();
	}

	private void reset() {
		this.combatLog = null;
		this.allCombats = null;
		this.selectedCombats = null;

		this.uploadNote.setText("");
		this.visibilityPublic.setSelected(true);
		clearFlash();
		disable(false);
	}

	private void validate() {
		clearFlash();
		if (combatLog == null) {
			setFlash("No combats to upload");

		} else {
			if (selectedCombats != null) {
				if (selectedCombats.size() > 1) {
					uploadLabel.setText("You are about to upload " + selectedCombats.size() + " selected combats to Parsely.");
				} else {
					uploadLabel.setText("You are about to upload the selected combat to Parsely.");
				}
			} else {
				uploadLabel.setText("You are about to upload the whole combat log to Parsely (" + allCombats.size() + " combats).");
			}
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("Using timezone ").append(config.getTimezone());
		if (context.getServerId() != null) {
			sb.append(", log server ").append(ServerName.getTitleFromCode(context.getServerId()));
		} else if (config.getCurrentCharacter().getServer() != null) {
			sb.append(", server ").append(config.getCurrentCharacter().getServer());
		} else {
			sb.append(", no server");
		}
		if (config.getCurrentCharacter().getGuild() != null) {
			sb.append(", guild ").append(config.getCurrentCharacter().getGuild());
		} else {
			sb.append(", no guild");
		}
		if (config.getParselyLogin() != null) {
			sb.append(", Parsely account ").append(config.getParselyLogin());
		} else {
			sb.append(", without specifying any Parsely account");
		}
		sb.append(".");
		settingsLabel.setText(sb.toString());
	}

	public void setUpload(final CombatLog combatLog, final List<Combat> allCombats, final List<Combat> selectedCombats) {
		reset();
		this.combatLog = combatLog;
		this.allCombats = allCombats;
		if (selectedCombats != null && !selectedCombats.isEmpty()) {
			this.selectedCombats = selectedCombats;
		} else {
			this.selectedCombats = null;
		}
	}

	@Override
	public void show() {
		validate();
		super.show();
	}

	public void handleUploadSave(final ActionEvent event) {

		clearFlash();

		// ensure we are using local references as it may go away during the process
		final CombatLog combatLog = this.combatLog;
		final List<Combat> allCombats = this.allCombats;
		final List<Combat> selectedCombats = this.selectedCombats;

		if (combatLog == null || combatLog.getFileName() == null) {
			setFlash("No combat log available");
			return;
		}

		if (allCombats == null || allCombats.isEmpty()) {
			setFlash("No combats to upload");
			return;
		}

		if (selectedCombats != null && !selectedCombats.isEmpty()) {
			boolean found = false;
			for (final Combat c : selectedCombats) {
				if (c != null) {
					found = true;
					break;
				}
			}
			if (!found) {
				setFlash("Please select the combats again");
				return;
			}
		}

		// 1) slice the log
		final byte[] content;
		final List<ParselyCombatInfo> combatsInfo = new ArrayList<>();
		try {
			content = FileLoader.extractCombats(combatLog.getFileName(), allCombats, selectedCombats, combatsInfo, context);

		} catch (Exception e) {
			//logger.error("Unable to read the log for upload: " + e.getMessage(), e);
			setFlash("Unable to read the log, please reload it and try again");
			return;
		}

		disable(true);
		setFlash("Uploading to Parsely ... ", Type.INFO);
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				String link = null;
				String error = null;
				try {
					link = parselyService.uploadLog(
							parselyService.createParams(
									config,
									visibilityPrivate.isSelected() ? 0 : (visibilityGuildOnly.isSelected() ? 2 : 1 /* public */),
									!uploadNote.getText().isEmpty() ? uploadNote.getText() : null,
									context
							),
							combatLog.getFileName(),
							content,
							combatsInfo);

				} catch (Exception e) {
					final String m = "Unable to upload to Parsely: " + e.getMessage();
					if (m.contains("Invalid user")) {
						logger.warn(m, e);
						error = "Your Parsely profile name and/or password is incorrect (" + e.getMessage() + ")";

					} else if (m.contains("Server returned non-OK status") || (e instanceof UnknownHostException)) {
						logger.warn(m, e);
						error = "Parsely seems to be down - please try again in a bit (" + e.getMessage() + ")";

					} else {
						logger.error(m, e);
						error = "Upload failed: " + e.getMessage();
					}
				}
				final String l = link;
				final String e = error;

				Platform.runLater(() -> {
					disable(false);
					if (e != null) {
						setFlash(e);
						return;
					}
					if (listener != null) {
						listener.onUploadSaved(l);
					}
					handleClose(event);
				});

			}
		}, 0);
	}

	private void disable(boolean disabled) {
		saveButton.setDisable(disabled);
		cancelButton.setDisable(disabled);
		settingsButton.setDisable(disabled);
		visibilityPublic.setDisable(disabled);
		visibilityGuildOnly.setDisable(disabled);
		visibilityPublic.setDisable(disabled);
		uploadNote.setDisable(disabled);
	}

	@Override
	public void handleClose(ActionEvent event) {
		reset();
		super.handleClose(event);
	}

	public void handleUploadSettings(final ActionEvent event) {
		if (listener != null) {
			listener.onUploadSettings();
		}
	}
}
