package com.ixale.starparse.gui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.KeyStroke;

import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.tk.TKStage;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;

import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class Win32Utils {

	private static final Logger logger = LoggerFactory.getLogger(Win32Utils.class);

	private static final int SWP_NOSIZE = 0x0001,
		SWP_NOMOVE = 0x0002,
		SWP_NOREDRAW = 0x0008,
		SWP_NOACTIVATE = 0x0010,
		SWP_SHOWWINDOW = 0x0040,

	GWL_EXSTYLE = -20,

	SW_HIDE = 0,
		SW_SHOW = 5;

	private static final long WS_EX_APPWINDOW = 0x00040000,
		WS_EX_NOACTIVATE = 0x08000000,
		WS_EX_LAYERED = 0x00080000,
		WS_EX_TRANSPARENT = 0x00000020L,
		WS_EX_TOOLWINDOW = 0x00000080L;

	private static final HWND HWND_TOPMOST = new HWND(Pointer.createConstant(-1));

	private static final User32 user32 = User32.INSTANCE;

	private static final Map<Stage, HWND> windows = new HashMap<>();

	public static void removeWindowFromTaskbar(final Stage stage) throws Exception {

		final HWND hWnd = getWindow(stage);
		if (hWnd == null) {
			return;
		}

		int style = user32.GetWindowLong(hWnd, GWL_EXSTYLE);

		style |= WS_EX_TOOLWINDOW;
		style |= WS_EX_NOACTIVATE;
		style &= ~WS_EX_APPWINDOW;

		user32.ShowWindow(hWnd, SW_HIDE); // hide the window
		user32.SetWindowLong(hWnd, GWL_EXSTYLE, style); // set the style
		user32.ShowWindow(hWnd, SW_SHOW); // show the window for the new style to come into effect
	}

	public static void setMouseTransparency(final Stage stage, boolean mouseTransparent, boolean solid) throws Exception {

		final HWND hWnd = getWindow(stage);
		if (hWnd == null) {
			return;
		}

		int style = user32.GetWindowLong(hWnd, GWL_EXSTYLE);
		if (mouseTransparent) {
			style |= WS_EX_TRANSPARENT;
			if (solid) {
				style |= WS_EX_LAYERED;
			} else {
			}
		} else {
			style &= ~WS_EX_TRANSPARENT;
			if (solid) {
				style &= ~WS_EX_LAYERED;
			} else {
			}
		}
		if (solid) {
			style &= ~WS_EX_TOOLWINDOW;
		} else {
			style |= WS_EX_TOOLWINDOW;
		}

		user32.ShowWindow(hWnd, SW_HIDE); // hide the window
		user32.SetWindowLong(hWnd, GWL_EXSTYLE, style); // set the style
		user32.ShowWindow(hWnd, SW_SHOW); // show the window for the new style to come into effect
	}

	public static void bringWindowToFront(final Stage stage) throws Exception {

		final HWND hWnd = getWindow(stage);
		if (hWnd == null) {
			return;
		}

		user32.SetWindowPos(hWnd, HWND_TOPMOST, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_SHOWWINDOW | SWP_NOACTIVATE | SWP_NOREDRAW);
	}

	public static void forgetWindow(final Stage stage) {
		windows.put(stage, null);
	}

	private static HWND getWindow(final Stage stage) throws Exception {

		if (windows.get(stage) != null) {
			return windows.get(stage);
		}

		try {
			//@SuppressWarnings({"deprecation"})
			//final TKStage tkStage = stage.impl_getPeer();

			Method getPeer = Window.class.getDeclaredMethod("getPeer");
			getPeer.setAccessible(true);
			final TKStage tkStage = (TKStage) getPeer.invoke(stage);

			if (tkStage == null) {
				// does not exists (yet)
				return null;
			}

			final Method getPlatformWindow = tkStage.getClass().getDeclaredMethod("getPlatformWindow");
			getPlatformWindow.setAccessible(true);
			final Object platformWindow = getPlatformWindow.invoke(tkStage);

			final Method getNativeHandle = platformWindow.getClass().getMethod("getNativeHandle");
			getNativeHandle.setAccessible(true);
			final Object nativeHandle = getNativeHandle.invoke(platformWindow);

			final HWND hWnd = new HWND(new Pointer((Long) nativeHandle));
			windows.put(stage, hWnd);
			return hWnd;

		} catch (Throwable e) {
			e.printStackTrace();
			throw new Exception("Error getting handle for: " + stage.getTitle(), e);
		}
	}

	// https://github.com/tulskiy/jkeymaster
	private static boolean hotkeyListen;
	private static Boolean hotkeyReset = false;
	private static final Object hotkeyMutex = new Object();
	private static Thread hotkeyLoopThread;

	private static volatile int hotkeyIdSeq = 0;

	private static final Map<Integer, HotKey> hotkeyMap = new HashMap<>();
	private static final Queue<HotKey> hotkeyRegisterQueue = new LinkedList<>();
	private static final Queue<String> hotkeyUnregisterQueue = new LinkedList<>();
	private static ExecutorService hotkeyEventExecutor;

	static class HotKey {
		String hotkey;
		Runnable callback;
		Runnable onError;

		public HotKey(String hotkey, Runnable callback, Runnable onError) {
			this.hotkey = hotkey;
			this.callback = callback;
			this.onError = onError;
		}
	}

	private static final Map<Integer, Integer> codeExceptions = new HashMap<>();
	static {
		codeExceptions.put(KeyEvent.VK_INSERT, 0x2D);
		codeExceptions.put(KeyEvent.VK_DELETE, 0x2E);
		codeExceptions.put(KeyEvent.VK_ENTER, 0x0D);
		codeExceptions.put(KeyEvent.VK_COMMA, 0xBC);
		codeExceptions.put(KeyEvent.VK_PERIOD, 0xBE);
		codeExceptions.put(KeyEvent.VK_PLUS, 0xBB);
		codeExceptions.put(KeyEvent.VK_MINUS, 0xBD);
		codeExceptions.put(KeyEvent.VK_SLASH, 0xBF);
		codeExceptions.put(KeyEvent.VK_SEMICOLON, 0xBA);
		codeExceptions.put(KeyEvent.VK_PRINTSCREEN, 0x2C);
	};

	static final Runnable hotkeyLoop = new Runnable() {
		public void run() {
			User32.MSG msg = new User32.MSG();
			hotkeyListen = true;
			while (hotkeyListen) {
				while (user32.PeekMessage(msg, HWND_TOPMOST, WinUser.WM_HOTKEY, WinUser.WM_HOTKEY, 0x0001 /* PM_REMOVE */)) {
					if (msg.message == WinUser.WM_HOTKEY) {
						final HotKey hotKey = hotkeyMap.get(msg.wParam.intValue());

						if (hotKey != null) {
							fireCallback(hotKey.callback);
							if (logger.isDebugEnabled()) {
								logger.debug("Fired hotkey: " + hotKey.hotkey);
							}
						}
					}
				}

				synchronized (hotkeyMutex) {
					if (hotkeyReset) {
						for (Integer id: hotkeyMap.keySet()) {
							user32.UnregisterHotKey(null, id);
						}

						hotkeyMap.clear();
						hotkeyReset = false;
						hotkeyMutex.notify();
						if (logger.isDebugEnabled()) {
							logger.debug("Hotkeys reset");
						}
					}

					while (!hotkeyUnregisterQueue.isEmpty()) {
						final String hotkey = hotkeyUnregisterQueue.poll();
						Integer found = null;
						for (int id: hotkeyMap.keySet()) {
							if (hotkey.equals(hotkeyMap.get(id).hotkey)) {
								found = id;
								break;
							}
						}
						if (found != null) {
							hotkeyMap.remove(found);
							if (user32.UnregisterHotKey(null, found)) {
								if (logger.isDebugEnabled()) {
									logger.debug("Hotkey removed: " + hotkey + " (remaining " + hotkeyMap.size() + ")");
								}
							} else {
								logger.warn("Unable to remove hotkey: " + hotkey);
							}
						} else {
							logger.warn("Unable to find hotkey to remove: " + hotkey);
						}
					}

					while (!hotkeyRegisterQueue.isEmpty()) {
						final HotKey hotKey = hotkeyRegisterQueue.poll();
						int id = hotkeyIdSeq++;
						final KeyStroke keyStroke = KeyStroke.getKeyStroke(hotKey.hotkey);
						if (keyStroke == null) {
							logger.warn("Invalid hotkey to register: " + hotKey.hotkey);
							fireCallback(hotKey.onError);
							continue;
						}
						try {
							Integer keyCode = codeExceptions.get(keyStroke.getKeyCode());
							if (keyCode == null) {
								keyCode = keyStroke.getKeyCode();
							}
							int modifiers = 0;
							if (keyStroke != null) {
								if ((keyStroke.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0) {
									modifiers |= User32.MOD_SHIFT;
								}
								if ((keyStroke.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0) {
									modifiers |= User32.MOD_CONTROL;
								}
								if ((keyStroke.getModifiers() & InputEvent.META_DOWN_MASK) != 0) {
									modifiers |= User32.MOD_WIN;
								}
								if ((keyStroke.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0) {
									modifiers |= User32.MOD_ALT;
								}
							}

							if (!System.getProperty("os.name", "").startsWith("Windows XP")
								&& !System.getProperty("os.name", "").startsWith("Windows Vista")) {
								modifiers |= User32.MOD_NOREPEAT;
							}
							if (user32.RegisterHotKey(null, id, modifiers, keyCode)) {
								hotkeyMap.put(id, hotKey);
								if (logger.isDebugEnabled()) {
									logger.debug("Hotkey added: " + keyStroke + " (as " + keyCode + " + " + modifiers + ")");
								}
							} else {
								logger.warn("Unable to add hotkey: " + keyStroke + " (as " + keyCode + " + " + modifiers + ")");
								fireCallback(hotKey.onError);
							}
						} catch (Exception e) {
							logger.warn("Unable to process hotkey: " + keyStroke + ": " + e.getMessage());
							fireCallback(hotKey.onError);
						}
					}

					try {
						hotkeyMutex.wait(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	};

	private static void fireCallback(Runnable callback) {
		if (hotkeyEventExecutor == null) {
			hotkeyEventExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					final Thread worker = new Thread(r, "Hotkey Worker @ " + r.toString());
					worker.setDaemon(true);
					return worker;
				}
			});
		}
		hotkeyEventExecutor.execute(callback);
	}

	public static void registerHotkey(final String hotkey, final Runnable callback, final Runnable onError) {
		synchronized (hotkeyMutex) {
			if (hotkeyLoopThread == null) {
				hotkeyListen = true;
				hotkeyLoopThread = new Thread(hotkeyLoop);
				hotkeyLoopThread.setName("Hotkeys");
				hotkeyLoopThread.setDaemon(true);
				hotkeyLoopThread.start();
				if (logger.isDebugEnabled()) {
					logger.debug("Hotkey loop started");
				}
			}
			hotkeyRegisterQueue.add(new HotKey(hotkey, callback, onError));
		}
	}

	public static void unregisterHotkey(final String hotkey) {
		synchronized (hotkeyMutex) {
			hotkeyUnregisterQueue.add(hotkey);
		}
	}

	// unused
	public static void resetHotkeyHook() {
		hotkeyReset = true;
		try {
			hotkeyMutex.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Hotkey loop reset");
		}
	}

	public static void stopHotkeyHook() {
		hotkeyListen = false;
		if (hotkeyLoopThread != null) {
			try {
				hotkeyLoopThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			hotkeyLoopThread.interrupt();
			hotkeyLoopThread = null;
		}
		if (hotkeyEventExecutor != null) {
			hotkeyEventExecutor.shutdown();
			hotkeyEventExecutor = null;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Hotkey loop stopped");
		}
	}
}
