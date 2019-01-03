package com.ixale.starparse.log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ixale.starparse.gui.FlashMessage;
import com.ixale.starparse.parser.ParserException;

public class LogWatcher extends Thread
{
	private static final String LOG_CHARSET = "ISO-8859-15";
	private static final char LOG_EOL = '\n';
	private static final int LINES_LIMIT = 300000, MAX_ERROR_COUNT = 20;

	private static final Logger logger = LoggerFactory.getLogger(LogWatcher.class);

	private final File parent;
	private final Integer polling;
	private final boolean isFile;

	private String lastFiredLine;
	private File lastFile;
	private int lastFileCount = 0;

	private int errorCount = 0;
	private boolean isParseable = false;
	private boolean doFileCheck = false;

	/**
	 * Set of listeners
	 */
	private final HashSet<LogWatcherListener> listeners = new HashSet<LogWatcherListener>();

	public LogWatcher(final File parent, Integer polling, boolean isFile) {
		this.parent = parent;
		this.polling = polling;
		this.isFile = isFile;

		setName("LogWatcher");
	}

	public void addLogWatcherListener(final LogWatcherListener l) {
		listeners.add(l);
	}

	public void removeTailerListener(final LogWatcherListener l) {
		listeners.remove(l);
	}

	public void doFileCheck() {
		doFileCheck = true;
	}

	public void run() {

		logger.debug("Started for " + parent);

		File file = null;
		String lastFileName = null;
		long filePosition = 0;

		FileInputStream in = null;
		FileChannel ch = null;

		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
		CharBuffer charBuffer = null;
		Charset cs = Charset.forName(LOG_CHARSET);

		int lines = 0, pos, offset, len;

		do {
			try {
				// resolve the newest file (or explicit, if set)
				if (isFile) {
					if (!parent.exists()) {
						logger.warn("Combat log no longer exists: " + parent);
						fireError("Combat log no longer exists: " + parent);
						break;
					}
					if (!parent.canRead()) {
						logger.warn("Unable to read combat log: " + parent);
						fireError("Unable to read combat log: " + parent);
						break;
					}
					file = parent;

				} else {
					file = getNewestFile();
					if (file == null) {
						logger.debug("Waiting for a valid file");
						sleep(polling);
						continue;
						// NOTREACHED
					}
				}

				if (lastFileName == null || !lastFileName.equals(file.getName())) {
					if (lastFileName != null) {
						fireFileComplete();
					}
					logger.info("Parsing file " + file);
					isParseable = false;

					closeStream(ch);
					closeStream(in);

					lastFileName = file.getName();
					filePosition = 0;

					fireNewFile(file);

					in = new FileInputStream(file.getAbsoluteFile());
					ch = in.getChannel();
				}

				// compare the length of the file to the file pointer
				for (int i = 0; ch.size() == filePosition && i < 20; i++) { // max 4s
					Thread.sleep(200);
				}
				if (ch.size() > filePosition) {
					lines = 0;
					// load into buffer
					while ((len = ch.read(byteBuffer, filePosition)) != -1) {
						byteBuffer.rewind();
						// decode as text
						charBuffer = cs.decode(byteBuffer);

						// search for \n
						for (pos = 0, offset = 0; pos < len; pos++) {
							if (charBuffer.get(pos) == LOG_EOL) {
								// full line
								try {
									fireNewLine(String.valueOf(charBuffer.subSequence(offset, pos - 1)));
									errorCount = 0;
									isParseable = true;

								} catch (Exception e) {
									if (lastFiredLine != null && (lastFiredLine.contains("2280674878816256")
										|| lastFiredLine.contains("Manka-Katz"))
										|| lastFiredLine.contains("Aktiviert den Tobus-Hetzer")) {
										// FIXME: German client fails to log Manka Cat or ... something ... , just move on and give it bit more room
										errorCount = -20;

									} else if (errorCount < MAX_ERROR_COUNT) {
										// ok, give it another shot
										errorCount++;
										logger.warn("Invalid line silently skipped for now (" + errorCount + "): [" + lastFiredLine + "]: " + e.getMessage(), e);

									} else {
										// bye
										throw e;
									}
								}

								// bump position
								filePosition += (pos - offset) + 1;

								offset = pos + 1;
								lines++;
							}
						}

						byteBuffer.clear();
						if (lines == 0 || lines > LINES_LIMIT) {
							// no complete line found, avoid loop
							break;
						}
					}

					if (logger.isTraceEnabled()) {
						logger.trace("Finished at " + filePosition + " (" + (file.length() - filePosition) + " left) reading " + lines + " lines");
					}

					if (lines > 0) {
						fireReadComplete();
					}
				}

			} catch (ClosedByInterruptException e) {
				// stop
				break;

			} catch (InterruptedException e) {
				// stop
				break;

			} catch (ParserException e) {
				final String err = "Unable to parse combat log [" + file.getAbsolutePath() + "][" + (lastFiredLine == null ? "empty" : lastFiredLine) + "]: " + e.getMessage();
				if (!isParseable) {
					// not a single positive hit
					logger.warn(err, e);
					fireError("Current file is not a combat log:\n" + file);
					break;
				}
				logger.error(err, e);
				fireError("Problem parsing combat log: " + e.getMessage());
				break;

			} catch (IllegalArgumentException e) {
				logger.warn("Unable to read combat log [" + file.getAbsolutePath() + "][" + (lastFiredLine == null ? "empty" : lastFiredLine) + "]: " + e.getMessage(), e);
				fireError("Problem reading combat log: " + e.getMessage());
				break;

			} catch (IOException e) {
				logger.warn("Unable to read combat log file [" + file.getAbsolutePath() + "][" + (lastFiredLine == null ? "empty" : lastFiredLine) + "]: " + e.getMessage(), e);
				fireError("Problem reading combat log file: " + e.getMessage());

			} catch (Exception e) {
				if (e.getMessage() != null) {
					if (lastFiredLine != null && (
						e.getMessage().contains("Table \"LOGS\" not found")
							|| e.getMessage().contains("Table \"COMBATS\" not found")
							|| e.getMessage().contains("Table \"PHASES\" not found")
							|| e.getMessage().contains("The database has been closed"))) {
						// probably shutting down
						break;
					}
				}
				if (!file.isFile() || !file.canRead()) {
					logger.warn("Unable to read combat log file [" + file.getAbsolutePath() + "][" + (lastFiredLine == null ? "empty" : lastFiredLine) + "]: " + e.getMessage(), e);
					fireError("Problem reading combat log file: " + e.getMessage());
					break;
				}
				if (e instanceof IndexOutOfBoundsException && (!file.getName().endsWith("txt") && !file.getName().endsWith("TXT"))) {
					logger.warn("Unable to read combat log file [" + file.getAbsolutePath() + "][" + (lastFiredLine == null ? "empty" : lastFiredLine) + "]: " + e.getMessage(), e);
					fireError("Unable to read the file, are you sure it is a valid combat log?\n" + file);
					break;
				}
				logger.error("Unable to process combat log [" + file.getAbsolutePath() + "][" + (lastFiredLine == null ? "empty" : lastFiredLine) + "]: " + e.getMessage(), e);
				break;
			}

		} while (!isInterrupted() && (polling != null || file == null || file.length() > filePosition));

		closeStream(ch);
		closeStream(in);
		lastFiredLine = null;
		lastFile = null;

		if (file != null) {
			try {
				fireFileComplete();
			} catch (Exception e) {
				if (e.getMessage().contains("Table \"LOGS\" not found")) {
					// probably shutting down

				} else {
					logger.error("Firing 'file completed' event failed", e);
				}
			}
		}

		logger.debug("Done");
	}

	private static final FilenameFilter combatLogFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith("combat");
		}
	};

	private static final Comparator<File> combatLogComparator = new Comparator<File>() {
		public int compare(File f1, File f2)
		{
			return String.valueOf(f2.getName()).compareTo(f1.getName());
		}
	};

	private File getNewestFile() {

		if (!parent.exists()) {
			throw new IllegalArgumentException("Directory does not exist: " + parent);
		}

		if (!parent.isDirectory()) {
			throw new IllegalArgumentException("Target is not a directory: " + parent);
		}

		if (!parent.canRead()) {
			throw new IllegalArgumentException("Unable to read directory: " + parent);
		}

		// attempt to do a quick check
		final String[] files = parent.list();
		if (files == null) {
			throw new IllegalArgumentException("Unable to scan directory: " + parent);
		}

		if (lastFile != null && lastFileCount > 0 && lastFileCount == files.length) {
			// assume nothing has changed
			return lastFile;
		}

		final File[] logs = parent.listFiles(combatLogFilter);

		if (!(logs.length > 0)) {
			logger.debug("No combat logs found");
			return null;
		}

		Arrays.sort(logs, combatLogComparator);

		if (lastFile == null || !logs[0].equals(lastFile)) {
			if (logs[0].length() == 0) { // not ready yet
				return lastFile;
			}
			lastFile = logs[0];
		}

		// update quick check
		lastFileCount = files.length;

		if (doFileCheck) {
			if (logs.length > 200) {
				long t = 0;
				for (File f: logs) {
					t += f.length();
				}
				fireInfo("There are over " + logs.length + " files in your combat log folder (" + Math.round(t / 1024 / 1024) + " MB), you may consider deleting some");
			}
			doFileCheck = false;
		}

		return logs[0];
	}

	private void closeStream(final Closeable s) {
		if (s == null) {
			return;
		}
		try {
			s.close();
		} catch (Exception e) {
			logger.debug("Unable to close stream", e);
		}
	}

	private void fireNewFile(final File file) throws Exception {
		for (LogWatcherListener l: listeners) {
			l.onNewFile(file);
		}
	}

	private void fireNewLine(final String line) throws Exception {
		lastFiredLine = line;
		for (LogWatcherListener l: listeners) {
			l.onNewLine(line);
		}
	}

	private void fireReadComplete() throws Exception {
		for (LogWatcherListener l: listeners) {
			l.onReadComplete();
		}
	}

	private void fireFileComplete() throws Exception {
		for (LogWatcherListener l: listeners) {
			l.onFileComplete();
		}
	}

	private void fireError(final String message) {
		for (LogWatcherListener l: listeners) {
			l.onFlashMessage(message, FlashMessage.Type.ERROR);
		}
	}

	private void fireInfo(final String message) {
		for (LogWatcherListener l: listeners) {
			l.onFlashMessage(message, FlashMessage.Type.INFO);
		}
	}
}