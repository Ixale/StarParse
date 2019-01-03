package com.ixale.starparse.log;

import java.io.File;

import com.ixale.starparse.gui.FlashMessage;

public interface LogWatcherListener {

	void onNewFile(File logFile) throws Exception;

	void onNewLine(String line) throws Exception;

	void onReadComplete() throws Exception;

	void onFileComplete() throws Exception;

	void onFlashMessage(String message, FlashMessage.Type type);
}