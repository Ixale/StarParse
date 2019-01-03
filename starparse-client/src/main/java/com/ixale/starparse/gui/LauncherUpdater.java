package com.ixale.starparse.gui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LauncherUpdater {

	private static final Logger logger = LoggerFactory.getLogger(LauncherUpdater.class);

	public LauncherUpdater() {
	}

	public boolean run() throws Exception {
		final File newLauncher = new File("32".equals(System.getProperty("sun.arch.data.model")) ? "starparse-launcherx86.zip" : "starparse-launcher.zip");
		final File oldLauncher = new File("../../starparse-launcher.jar");

		if (!newLauncher.exists()) {
			// nothing to do
			return false;
		}
		if (!oldLauncher.exists()) {
			throw new IllegalStateException("Current launcher not found: " + oldLauncher.getAbsolutePath() + " (running from " + System.getProperty("user.dir") + ")");
		}
		if (newLauncher.length() == oldLauncher.length()) {
			// no update needed (TODO: hash?)
			return false;
		}

		// ok, wait a bit to ensure the launcher is already closed
		Thread.sleep(30000);

		if (!oldLauncher.canWrite()) {
			throw new IllegalStateException("Current launcher not writable: " + oldLauncher.getAbsolutePath());
		}
		logger.debug("Updating launcher - new size: " + newLauncher.length() + ", old size: " + oldLauncher.length());

		// create backup
		final File tempLauncher = new File(oldLauncher.getParentFile(), "starparse-launcher.bak");
		tempLauncher.deleteOnExit();
		Files.copy(oldLauncher.toPath(), tempLauncher.toPath(), StandardCopyOption.REPLACE_EXISTING);

		try {
			// copy over
			Files.copy(newLauncher.toPath(), oldLauncher.toPath(), StandardCopyOption.REPLACE_EXISTING);

		} catch (Exception e) {
			// try to fix it
			logger.warn("Trying to fix failed update: " + oldLauncher.toPath());
			Files.copy(tempLauncher.toPath(), oldLauncher.toPath(), StandardCopyOption.REPLACE_EXISTING);
			throw e;
		}
		logger.info("Launcher updated");
		return true;
	}
}
