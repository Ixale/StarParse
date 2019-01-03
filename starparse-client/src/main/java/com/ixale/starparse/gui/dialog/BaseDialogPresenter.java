package com.ixale.starparse.gui.dialog;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.stage.Stage;

import com.ixale.starparse.gui.FlashMessage;
import com.ixale.starparse.gui.ModalDialog;

abstract public class BaseDialogPresenter implements Initializable {

	protected static final Logger logger = LoggerFactory.getLogger(BaseDialogPresenter.class);

	private Stage parent;
	private ModalDialog dialog;

	private Parent content;
	private String title, closeButtonText;

	private FlashMessage flash;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		
	}

	protected void setContent(Parent content, String title, String closeButtonText) {
		this.content = content;
		this.title = title;
		this.closeButtonText = closeButtonText;
	}

	public void setStage(final Stage parent) {
		this.parent = parent;
	}

	protected ModalDialog getDialog() {
		if (dialog == null) {
			dialog = new ModalDialog(parent, title, content, closeButtonText, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					handleClose(event);
				}
			});
		}
		return dialog;
	}

	public void show() {
		getDialog().show();
	}

	public void handleClose(final ActionEvent event) {
		if (dialog == null) {
			return;
		}
		dialog.close();
		clearFlash();
	}

	protected void setFlash(final String message) {
		setFlash(message, FlashMessage.Type.ERROR);
	}

	protected void setFlash(final String message, final FlashMessage.Type type) {
		clearFlash();
		if (message == null) {
			return;
		}
		flash = new FlashMessage(getDialog().getContentRoot(), message, type); 
	}

	protected void clearFlash() {
		if (flash != null) {
			flash.close();
			flash = null;
		}
	}
}
