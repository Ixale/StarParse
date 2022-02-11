package com.ixale.starparse.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.*;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ixale.starparse.gui.FlashMessage.Type;
import com.ixale.starparse.log.LogWatcher;
import com.ixale.starparse.log.LogWatcherListener;
import com.ixale.starparse.service.impl.Context;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-context.xml")
public class LogWatcherTest {

	@Autowired
	private Context context;

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testTailingIncremental() throws Exception {

		final int POLLING = 200;

		final ArrayList<String> tempLines = new ArrayList<>(), flushedLines = new ArrayList<>();

		// load test source file & temporary fake log
		String n = "combat_2014-01-26_22_01_13_435268.txt";
		final File sourceLog = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(n)).toURI()), tempLog = testFolder.newFile(n);

		// prepare tailer
		final Parser parser = new Parser(context);
		final LogWatcher tailer = new LogWatcher(testFolder.getRoot(), POLLING, false);

		int[] sizes = {20, 100, 0, 5, 1500, 133, 0, 0, 1, 0};
		CountDownLatch l = new CountDownLatch(sizes.length * 2);

		tailer.addLogWatcherListener(new LogWatcherListener() {
			@Override
			public void onNewFile(File logFile) throws Exception {
				parser.setCombatLogFile(logFile);
			}

			@Override
			public boolean onNewLine(String line) throws Exception {
				final boolean doFlush = parser.parseLogLine(line);
				tempLines.add(line);
				return doFlush;
			}

			@Override
			public void onReadComplete(Integer percent) {
				flushedLines.addAll(tempLines);
				l.countDown();

				try {
					if (l.await(POLLING + 1000, TimeUnit.MILLISECONDS)) {
						Assert.fail("Read complete timed out at " + tempLines.size());
					}
				} catch (Exception ignored) {

				}
			}

			@Override
			public void onFileComplete() {
			}

			@Override
			public void onFlashMessage(String message, Type type) {
				if (Type.ERROR.equals(type)) {
					fail(message);
				}
			}
		});

		// start
		tailer.start();

		assertEquals(0, tempLines.size());
		assertEquals(0, flushedLines.size());

		// simulate streaming and check for correct lines read
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		int linesTotal = 0;
		try {
			fr = new FileReader(sourceLog);
			br = new BufferedReader(fr);

			fw = new FileWriter(tempLog);
			bw = new BufferedWriter(fw);

			for (int s = 0; s < sizes.length; s++) {
				for (int i = 0; i < sizes[s]; i++) {
					bw.write(br.readLine());
					bw.newLine();
					linesTotal++;
				}
				bw.flush();

				// ... now wait (polling + 200ms extra for processing)
//				Thread.sleep(POLLING);
				if (l.await(POLLING + 1000, TimeUnit.MILLISECONDS)) {
					Assert.fail("Cycle " + s + " timed out");
				}

				assertEquals("Cycle " + s, sizes[s], tempLines.size());
				assertEquals("Cycle " + s, linesTotal, flushedLines.size());
				assertEquals("Cycle " + s, linesTotal, parser.getEvents().size());

				tempLines.clear();
			}

			// sanity
			assertEquals("2014-01-27", new SimpleDateFormat("YYYY-MM-d").format(parser.getEvents().get(0).getTimestamp()));
			assertEquals(0, tempLines.size());
			assertEquals(linesTotal, flushedLines.size());
			assertEquals(linesTotal, parser.getEvents().size());

			// intended incomplete line (without newline) ...
			bw.write("[22:15:00.669] [@Ixale] [@Ixale] [] [Spend {836045448945473}: focus point {836045");
			bw.flush();

			Thread.sleep(POLLING + 200);

			// ... now completed
			bw.write("448938496}] (3)");
			bw.flush();
			bw.newLine();

			Thread.sleep(POLLING + 200);

			// intended overflow (midnight)
			bw.write("[00:00:00.669] [@Ixale] [@Ixale] [] [Spend {836045448945473}: focus point {836045448938496}] (3)");
			bw.newLine();
			bw.flush();

			Thread.sleep(POLLING + 200);

			assertEquals("2014-01-28", new SimpleDateFormat("YYYY-MM-d").format(parser.getEvents().get(parser.getEvents().size() - 1).getTimestamp()));

			tempLines.clear();
			parser.getEvents().clear();
			parser.getCombats().clear();

			// now newer file
			n = "combat_2014-01-26_22_15_13_435268.txt";
			File newFile = testFolder.newFile(n);

			Thread.sleep(POLLING + 200);

			// should not be there yet as empty
			assertFalse(parser.getCombatLog().getFileName().contains(n));
			assertNotNull(parser.getCombatLog().getCharacterName());

			FileWriter fw2 = new FileWriter(newFile);
			fw2.write("random bytes, not full line yet");
			fw2.flush();
			fw2.close();

			Thread.sleep(POLLING + 4200);

			// now should be picked and reset
			assertTrue(parser.getCombatLog().getFileName().contains(n));
			assertNull(parser.getCombatLog().getCharacterName());
			assertEquals(0, parser.getEvents().size());

		} finally {
			try {
				bw.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			try {
				fw.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			try {
				br.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			try {
				fr.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
		}

		// finish
		tailer.interrupt();
		try {
			tailer.join();
		} catch (InterruptedException e) {
		}
	}

	@Test
	public void testTailingFull() throws Exception {

		final int POLLING = 200;

		// load test source file & temporary fake log
		String n = "combat_2014-01-26_22_01_13_435268.txt";
		final File sourceLog = new File(getClass().getClassLoader().getResource(n).toURI()), tempLog = testFolder.newFile(n);

		int lines = 0;
		final int[] result = new int[]{0, 0, 0};

		// fill in complete log
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		String line;
		try {
			fr = new FileReader(sourceLog);
			br = new BufferedReader(fr);

			fw = new FileWriter(tempLog);
			bw = new BufferedWriter(fw);

			while ((line = br.readLine()) != null) {
				bw.write(line);
				bw.newLine();
				lines++;
			}
			bw.flush();

		} finally {
			try {
				bw.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			try {
				fw.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			try {
				br.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			try {
				fr.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
		}

		// prepare tailer
		final LogWatcher tailer = new LogWatcher(testFolder.getRoot(), POLLING, false);
		tailer.addLogWatcherListener(new LogWatcherListener() {
			@Override
			public void onNewFile(File logFile) throws Exception {
				result[0]++;
			}

			@Override
			public boolean onNewLine(String line) throws Exception {
				result[1]++;
				return false;
			}

			@Override
			public void onReadComplete(Integer percent) {
				result[2]++;
			}

			@Override
			public void onFileComplete() {

			}

			@Override
			public void onFlashMessage(String message, Type type) {
				if (Type.ERROR.equals(type)) {
					fail(message);
				}
			}
		});

		// start - should pick up the file immediately
		tailer.start();

		Thread.sleep(POLLING + 200);

		// finish
		tailer.interrupt();
		try {
			tailer.join();
		} catch (InterruptedException e) {
		}

		assertEquals(result[0], 1); // 1 open file
		assertEquals(result[1], lines); // lines
		assertEquals(result[2], 1); // 1 completed file
	}

	@Test
	public void testInvalidLines() throws Exception {

		final int POLLING = 200;

		// load test source file & temporary fake log
		String n = "combat_2015-01-01_00_01_02_123456.txt";
		final File sourceLog = new File(getClass().getClassLoader().getResource(n).toURI()), tempLog = testFolder.newFile(n);

		int lines = 0;
		final int[] result = new int[]{0, 0, 0};

		// fill in complete log
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		String line;
		try {
			fr = new FileReader(sourceLog);
			br = new BufferedReader(fr);

			fw = new FileWriter(tempLog);
			bw = new BufferedWriter(fw);

			while ((line = br.readLine()) != null) {
				bw.write(line);
				bw.newLine();
				lines++;
			}
			bw.flush();

		} finally {
			try {
				bw.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			try {
				fw.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			try {
				br.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			try {
				fr.close();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
		}

		// prepare tailer
		final Parser parser = new Parser(context);
		final LogWatcher tailer = new LogWatcher(testFolder.getRoot(), POLLING, false);
		tailer.addLogWatcherListener(new LogWatcherListener() {
			@Override
			public void onNewFile(File logFile) throws Exception {
				parser.setCombatLogFile(logFile);
				result[0]++;
			}

			@Override
			public boolean onNewLine(String line) throws Exception {
				result[1]++;
				return parser.parseLogLine(line);
			}

			@Override
			public void onReadComplete(Integer percent) {
				result[2]++;
			}

			@Override
			public void onFileComplete() {

			}

			@Override
			public void onFlashMessage(String message, Type type) {
				if (Type.ERROR.equals(type)) {
					fail(message);
				}
			}
		});

		// start - should pick up the file immediately
		tailer.start();

		Thread.sleep(POLLING + 200);

		// finish
		tailer.interrupt();
		try {
			tailer.join();
		} catch (InterruptedException e) {
		}

		assertEquals(result[0], 1); // 1 open file
		assertEquals(result[1], lines); // lines (12 in total, 3 valid)
		assertEquals(result[2], 1); // 1 completed file
	}
}
