package com.ixale.starparse.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ixale.starparse.domain.ConfigAttacks;
import com.ixale.starparse.domain.ConfigTimers;
import com.ixale.starparse.gui.FlashMessage.Type;
import com.ixale.starparse.gui.dialog.ChangelogDialogPresenter;
import com.ixale.starparse.gui.main.MainPresenter;
import com.ixale.starparse.time.TimeUtils;
import com.ixale.starparse.ws.SslUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class StarparseApp extends Application {
	private static final Logger logger = LoggerFactory.getLogger(StarparseApp.class);

	public static String VERSION = com.ixale.starparse.domain.Application.VERSION,
		TITLE = "Ixale's StarParse",
		CONFIG_FILE = "starparse.xml",
		CONFIG_TIMERS_FILE = "starparse-timers.xml",
		CONFIG_ATTACKS_FILE = "starparse-attacks.xml",
		SOUNDS_DIR = "sounds",
		ICONS_DIR = "icons";

	private static final int ERROR_REPORT_INTERVAL = 120 * 1000;

	private Config config;
	private MainPresenter mainPresenter;

	private static ChangelogDialogPresenter changelogPresenter;

	private boolean isGracefulStop = false;

	static {
		if (!"i".toUpperCase().equals("I")) {
			// http://hg.openjdk.java.net/jdk8/jdk8/nashorn/rev/343fd0450802
			Locale.setDefault(new Locale("en", "EN"));
		}
		// prevent bug with ipv6 connect
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("javafx.userAgentStylesheetUrl", "caspian");
		System.setProperty("prism.vsync", "false");

		SslUtils.configureSsl();
	}

	public static void main(String[] args) {
		logger.info("Application started");

		launch(args);

		logger.info("Application finished");
	}

	@Override
	@SuppressWarnings("resource")
	public void start(final Stage stage) throws Exception {
		// GUI
		final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(StarparseAppFactory.class);
		mainPresenter = context.getBean(MainPresenter.class);

		StatusBarAppender.setListener(new StatusBarAppender.Listener() {

			private Long lastErrorReported = null;

			@Override
			public void onMessage(final String msg, final LoggingEvent event) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						mainPresenter.getStatusMessageLabel().setText(msg);
					}
				});
				if (event.getLevel().equals(Level.ERROR)) {
					if (lastErrorReported != null && (lastErrorReported + ERROR_REPORT_INTERVAL > System.currentTimeMillis())) {
						// too soon
						return;
					}
					lastErrorReported = System.currentTimeMillis();
					final Thread t = new Thread() {
						public void run() {
							setName("ErrorReporter");
							reportError(msg, event.getThrowableInformation() != null ? event.getThrowableInformation().getThrowable() : null);
						}
					};
					t.setDaemon(true);
					t.start();
				}
			}
		});

		stage.initStyle(StageStyle.TRANSPARENT);

		final BorderPane contentPane = new BorderPane();
		contentPane.getStyleClass().add("wrapper");
		contentPane.setCenter(mainPresenter.getView());

		final Scene scene = new Scene(contentPane, Color.TRANSPARENT);
		if (javafx.stage.Screen.getPrimary().getDpi() > 96) { // 125% = 96 on high-DPI screens
			// FIXME: workaround for JavaFX bug
			scene.getStylesheets().add("styles120.bss");
		} else {
			scene.getStylesheets().add("styles.bss");
		}
		// FIXME: workaround for Java FX 2.2 BSS URL bug
		mainPresenter.getHeader().setStyle("-fx-background-image: url('img/top-bar.png');");
		mainPresenter.getFooter().setStyle("-fx-background-image: url('img/bottom-bar.png');");

		addDraggableNode(mainPresenter.getHeader());
		addResizingNode(mainPresenter.getResizer(), 640, 400);

		stage.setScene(scene);
		stage.setTitle(TITLE);
		stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/Star-Wars-The-Old-Republic-8-icon.png")));
		stage.show();

		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean wasFocused, Boolean isFocusing) {
				if (isFocusing) {
					Platform.runLater(new Runnable() {
						public void run() {
							mainPresenter.bringPopoutsToFront();
						};
					});
				}
			}
		});

		if ((config = Marshaller.loadFromFile(CONFIG_FILE)) == null) {
			config = new Config();
		}
		TimeUtils.setCurrentTimezone(config.getTimezone());

		config.setConfigTimers((ConfigTimers) Marshaller.loadFromFile(CONFIG_TIMERS_FILE));
		config.setConfigAttacks((ConfigAttacks) Marshaller.loadFromFile(CONFIG_ATTACKS_FILE));

		setWindowSizeAndPosition(scene);

		mainPresenter.run(config);

		logger.info("Parser ready (" + VERSION + ")");

		if (!VERSION.equals(config.getLastVersion())) {
			if (config.getLastVersion() == null
				|| config.getLastVersion().length() < 3
				|| !VERSION.substring(0, 3).equals(config.getLastVersion().substring(0, 3))) {
				showChangelog(stage);
			}
			config.setLastVersion(VERSION);
			Marshaller.storeToFile(config, CONFIG_FILE); // make sure its displayed only once
		}

		bindShutdownHooks(stage);
		setupClockSyncScheduler(config, mainPresenter, 60 * 60 * 1000);
		updateLauncherIfNeeded();
	}

	private void bindShutdownHooks(final Stage stage) {

		// this will probably not help, but well
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						emergencyStop("Shutdown hook");
					}
				});
			}
		});

		stage.setOnHidden(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				emergencyStop("Hiding event");
			}
		});

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent event) {
				emergencyStop("Close event");
			}
		});
	}

	private void emergencyStop(String source) {
		if (isGracefulStop) {
			// nothing to do
			return;
		}
		logger.warn("Emergency stop: " + source);
		try {
			gracefulStop();
		} catch (Exception e) {
			logger.error("Shutdown failed", e);
		}
	}

	private void gracefulStop() {
		if (isGracefulStop) {
			// nothing to do
			return;
		}
		if (mainPresenter != null) {
			mainPresenter.stop();
		}

		if (config != null) {
			Marshaller.storeToFile(config, CONFIG_FILE);
			Marshaller.storeToFile(config.getConfigTimers(), CONFIG_TIMERS_FILE);
			Marshaller.storeToFile(config.getConfigAttacks(), CONFIG_ATTACKS_FILE);
		}

		Win32Utils.stopHotkeyHook();

		isGracefulStop = true;
	}

	private void reportError(final String msg, final Throwable cause) {

		final StringBuilder sb = new StringBuilder();
		final byte[] payload;
		try {
			sb.append("error=");
			sb.append(URLEncoder.encode(msg, "UTF-8"));

			if (cause != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				cause.printStackTrace(pw);

				sb.append("&stack=");
				final String value = sw.toString();
				sb.append(URLEncoder.encode(value.length() > 1000 ? value.substring(0, 997) + "..." : value, "UTF-8"));

				pw.close();
				try {
					sw.close();
				} catch (IOException e) {
				}
			}
			sb.append("&version=");
			sb.append(VERSION);

			if (config != null) {
				if (!config.getCurrentCharacter().getName().equals(Config.DEFAULT_CHARACTER)) {
					sb.append("&character=");
					sb.append(URLEncoder.encode(config.getCurrentCharacter().getName(), "UTF-8"));
					if (config.getCurrentCharacter().getGuild() != null) {
						sb.append("&guild=");
						sb.append(URLEncoder.encode(config.getCurrentCharacter().getGuild(), "UTF-8"));
					}
					if (config.getCurrentCharacter().getServer() != null) {
						sb.append("&server=");
						sb.append(URLEncoder.encode(config.getCurrentCharacter().getServer(), "UTF-8"));
					}
				}
			}

			payload = sb.toString().toString().getBytes("UTF-8");

		} catch (Exception e) {
			// ignore
			return;
		}

		HttpURLConnection conn = null;
		try {
			String host;
			if (config == null || (host = config.getServerHost()) == null) {
				// fallback to default
				host = Config.DEFAULT_SERVER_HOST;
			}
			final URL ws = new URL("http://" + host);
			final URL url = new URL(ws.getProtocol(), ws.getHost(), 80, "/starparse/error/index.php");

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", "" + Integer.toString(payload.length));

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.getOutputStream().write(payload);
			conn.getOutputStream().flush();

			conn.getResponseCode();

		} catch (Exception e) {
			// ignore

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
			conn = null;
		}
	}

	@Override
	public void stop() {
		gracefulStop();
	}

	private double initialX = 0, initialY = 0;

	private void setWindowSizeAndPosition(final Scene scene) {

		double[] bounds = getScreenBounds();
		if (config.getWindowWidth() != null) {
			scene.getWindow().setWidth(config.getWindowWidth());
		}
		if (config.getWindowHeight() != null) {
			scene.getWindow().setHeight(config.getWindowHeight());
		}
		if (config.getWindowX() != null) {
			scene.getWindow().setX(Math.min(bounds[1] - 100, Math.max(bounds[0], config.getWindowX())));
		}
		if (config.getWindowY() != null) {
			scene.getWindow().setY(Math.max(bounds[2], Math.min(bounds[3] - 100, config.getWindowY())));
		}
	}

	private void addDraggableNode(final Node node) {

		node.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				initialX = me.getSceneX();
				initialY = me.getSceneY();
				me.consume();
			}
		});

		node.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				node.getScene().getWindow().setX(me.getScreenX() - initialX);
				node.getScene().getWindow().setY(me.getScreenY() - initialY);

				// store
				config.setWindowX(node.getScene().getWindow().getX());
				config.setWindowY(node.getScene().getWindow().getY());

				me.consume();
			}
		});
	}

	private void addResizingNode(final Node node, final int minW, final int minH) {

		node.setCursor(Cursor.NW_RESIZE);

		node.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				initialX = (node.getScene().getWindow().getX() + node.getScene().getWindow().getWidth()) - me.getScreenX() - 20;
				initialY = (node.getScene().getWindow().getY() + node.getScene().getWindow().getHeight()) - me.getScreenY() - 20;
				me.consume();
			}
		});

		node.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getButton() == MouseButton.MIDDLE) {
					return;
				}
				node.getScene().getWindow().setWidth(Math.max(minW, me.getScreenX() + initialX - node.getScene().getWindow().getX() + 20));
				node.getScene().getWindow().setHeight(Math.max(minH, me.getScreenY() - initialY - node.getScene().getWindow().getY() + 20));

				// store
				config.setWindowWidth(node.getScene().getWindow().getWidth());
				config.setWindowHeight(node.getScene().getWindow().getHeight());
				me.consume();
			}
		});
	}

	/**
	 * Popups
	 */

	public static void showChangelog(final Stage primaryStage) {

		new Timer(true).schedule(
			new TimerTask() {
				@Override
				public void run() {
					Platform.runLater(new Runnable() {
						public void run() {
							if (changelogPresenter == null) {
								changelogPresenter = new ChangelogDialogPresenter();
								changelogPresenter.setStage(primaryStage);
							}
							changelogPresenter.show();
						}
					});
				}
			}, 100);
	}

	private void setupClockSyncScheduler(final Config config, final MainPresenter mainPresenter, int interval) {

		new Timer(true).schedule(
			new TimerTask() {
				@Override
				public void run() {
					Thread.currentThread().setName("ClockOffsetResolver");
					int i = 3;
					Long offset = null;
					while (i-- > 0 && ((offset = TimeUtils.updateClockOffset(config.getTimeSyncHost())) == null)) {
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {
						}
					}
					if (offset == null) {
//						Platform.runLater(new Runnable() {
//							public void run() {
//								mainPresenter.setFlash(
//									"Unable to contact time server to resolve local clock difference, timers may be off",
//									Type.ERROR);
//							}
//						});
					} else if (offset > 1800000) {
						Platform.runLater(new Runnable() {
							public void run() {
								mainPresenter.setFlash(
									"Your Windows clock appears to be off by more than 30 minutes, please check your time and especially timezone (!) settings.\n"
										+ "Timers and raiding will likely not work until the time is fixed.",
									Type.ERROR);
							}
						});
					}
				}
			}, 0, interval);
	}

	private void updateLauncherIfNeeded() {
		new Thread() {
			@Override
			public void run() {
				setName("LauncherUpdater");
				final LauncherUpdater updater = new LauncherUpdater();
				try {
					updater.run();

				} catch (Exception e) {
					logger.error("Unable to perform launcher update: " + e.getMessage(), e);
					Platform.runLater(new Runnable() {
						public void run() {
							mainPresenter.setFlash(
								"Unable to update launcher - please download StarParse again and run the installer,"
									+ " otherwise you will miss future updates and will be unable to use the Raiding!",
								Type.ERROR,
								"http://ixparse.com/help/#unable-to-update");
						}
					});
				}
			}
		}.start();
	}

	public static Hyperlink createHyperlink(final String title, final String url) {
		final Hyperlink ref = new Hyperlink(title);
		ref.setFocusTraversable(false);
		ref.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent arg0) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (Exception e) {
					logger.warn("Unable to navigate to " + url + ": " + e.getMessage(), e);
				}
			}
		});
		ref.setTextFill(Paint.valueOf("#328BDB"));
		ref.setTextAlignment(TextAlignment.CENTER);
		return ref;
	}

	public static double[] getScreenBounds() {
		// avoid monitor clip
		double minX = Screen.getPrimary().getVisualBounds().getMinX();
		double maxX = Screen.getPrimary().getVisualBounds().getMaxX();
		double minY = Screen.getPrimary().getVisualBounds().getMinY();
		double maxY = Screen.getPrimary().getVisualBounds().getMaxY();
		for (final Screen s: Screen.getScreens()) {
			if (s.getVisualBounds().getMinX() < minX) {
				minX = s.getVisualBounds().getMinX();
			}
			if (s.getVisualBounds().getMaxX() > maxX) {
				maxX = s.getVisualBounds().getMaxX();
			}
			if (s.getVisualBounds().getMinY() < minY) {
				minY = s.getVisualBounds().getMinY();
			}
			if (s.getVisualBounds().getMaxY() < maxY && s.getVisualBounds().getMaxY() != 0) { // top-bottom multi-screen
				maxY = s.getVisualBounds().getMaxY();
			}
		}
		return new double[]{minX, maxX, minY, maxY};
	}

//	public static class FindImage {
//
//		public static String bigBmp = "p:/Work/java/starparse/starparse-client/x/screen2.bmp";
//
//		public static int count = 0;
//
//		// The matching position if any or null
//
//		public static void doMatch() {
//			try {
//				logger.debug("Image Started");
//				//logger.debug("Image finished: " + findSubimage(ImageIO.read(new File(bigBmp)), drawFrame("THORIC")));
//				boof("IXAYLA", "THORIC", "Værdiløs", "Bezcenný", "Isaaç", "Lokî", "Thraxi", "Láncárd");
//				logger.debug("Image finished");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		public static BufferedImage drawFrame(String name) throws Exception {
//			final Canvas canvas = new Canvas(50, 15);
//			GraphicsContext gc = canvas.getGraphicsContext2D();
//
//			gc.setFont(Font.font("Arial"));
//			gc.setFill(Color.web("#7fd0ee"));
//			gc.fillText(name.toUpperCase(), 0, 15);
//			SnapshotParameters parameters = new SnapshotParameters();
//			parameters.setFill(Color.TRANSPARENT);
//			WritableImage image = canvas.snapshot(parameters, null);
//
//			return SwingFXUtils.fromFXImage(image, new BufferedImage(50, 15, BufferedImage.TYPE_INT_ARGB));
//		}
//
//		public static void boof(String... find) throws Exception {
//
//			ImageFloat32 image = UtilImageIO.loadImage(bigBmp, ImageFloat32.class);
//
//			BufferedImage output = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_BGR);
//			ConvertBufferedImage.convertTo(image, output);
//			Graphics2D g2 = output.createGraphics();
//
//			for (String n: find) {
//				BufferedImage temp = drawFrame(n);
//
//				ImageFloat32 template = ConvertBufferedImage.convertFrom(temp, new ImageFloat32(temp.getWidth(), temp.getHeight()));
//				List<Match> found = findMatches(image, template, 1);
//
//				int r = 2;
//				int w = template.width + 2 * r;
//				int h = template.height + 2 * r;
//
//				for (Match m: found) {
//					// the return point is the template's top left corner
//					int x0 = m.x - r;
//					int y0 = m.y - r;
//					int x1 = x0 + w;
//					int y1 = y0 + h;
//
//					g2.drawLine(x0, y0, x1, y0);
//					g2.drawLine(x1, y0, x1, y1);
//					g2.drawLine(x1, y1, x0, y1);
//					g2.drawLine(x0, y1, x0, y0);
//					g2.drawString("= " + n, x0, y1 + 15);
//					
//				}
//			}
//
//			ImageIO.write(output, "PNG", new File(bigBmp + ".png"));
//		}
//		final static TemplateMatching<ImageFloat32> matcher = FactoryTemplateMatching.createMatcher(TemplateScoreType.SUM_DIFF_SQ, ImageFloat32.class);
//
//		private static List<Match> findMatches(ImageFloat32 image, ImageFloat32 template, int expectedMatches) {
//
//			matcher.setTemplate(template, expectedMatches);
//			matcher.process(image);
//
//			return matcher.getResults().toList();
//
//		}
//	}
}