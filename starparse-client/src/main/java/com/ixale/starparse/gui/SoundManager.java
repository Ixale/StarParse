package com.ixale.starparse.gui;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.media.AudioClip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ixale.starparse.utils.FileLoader;

public class SoundManager {

	private static final Logger logger = LoggerFactory.getLogger(SoundManager.class);

	private static final List<File> defaultSounds = new ArrayList<>();
	private static final Map<String, List<File>> defaultVoices = new HashMap<>();

	private static final Map<String, URI> uris = new HashMap<>();

	private static final Map<String, AudioClip> clips = new HashMap<>();

	public static final List<File> getDefaultSounds(final String voice) {
		if (defaultSounds.isEmpty()) {
			try {
				// scan and load
				final File soundDir = new File(StarparseApp.SOUNDS_DIR);
				// sync
				FileLoader.extractZip(new File(StarparseApp.SOUNDS_DIR + ".zip"), soundDir);
				FileLoader.extractZip(new File(StarparseApp.SOUNDS_DIR + "2.zip"), soundDir);

				if (soundDir.exists() && soundDir.isDirectory()) {
					for (final File sound: soundDir.listFiles()) {
						if (sound.isDirectory()) {
							final List<File> voiceSounds = new ArrayList<>();
							for (final File sample: sound.listFiles()) {
								voiceSounds.add(sample);
							}
							defaultVoices.put(sound.getName(), voiceSounds);
						} else {
							defaultSounds.add(sound);
						}
					}
				}

			} catch (Exception e) {
				logger.error("Unable to load default sounds: " + e.getMessage(), e);
			}
		}
		return voice == null ? defaultSounds : defaultVoices.get(voice);
	}

	public static final Set<String> getDefaultVoices() {
		return defaultVoices.keySet();
	}

	private static URI getSoundFile(final String sound, final String voice) {
		if (uris.containsKey(sound)) {
			return uris.get(sound);
		}
		// default sound?
		for (final File f: getDefaultSounds(voice)) {
			final String chunk = (voice == null ? sound : new File(sound).getName());
			if (f.getName().equals(chunk)) {
				uris.put(sound, f.toURI());
				return uris.get(sound);
			}
		}
		// probably custom
		final File custom = new File(sound);
		if (custom.isFile()) {
			uris.put(sound, custom.toURI());
			return uris.get(sound);
		}
		logger.warn("Invalid sound: " + sound + " (" + (voice == null ? "-" : voice) + ")");
		return null;
	}

	public static void play(String sound, Double volume) {
		play(sound, null, volume);
	}

	public static void play(int sample, String voice, Double volume) {
		play(voice + '/' + sample + ".mp3", voice, volume);
	}

	private static void play(String sound, String voice, Double volume) {
		final URI uri = getSoundFile(sound, voice);
		if (uri == null) {
			// invalid sound
			return;
		}
		final AudioClip clip = new AudioClip(uri.toString());
		clip.setVolume(volume == null ? 80 : Math.max(0, Math.min(1, volume / 100)));
		clip.play();

		clips.put(sound, clip);
	}

	public static void stop(String sound) {
		if (clips.containsKey(sound) && clips.get(sound).isPlaying()) {
			clips.get(sound).stop();
			clips.remove(sound);
		}
	}

	public static void stopAll() {
		for (final AudioClip clip: clips.values()) {
			if (clip.isPlaying()) {
				clip.stop();
			}
		}
		clips.clear();
	}
}
