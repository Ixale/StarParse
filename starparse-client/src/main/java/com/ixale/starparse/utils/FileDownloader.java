package com.ixale.starparse.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.ixale.starparse.domain.Application;

public class FileDownloader {

	private static final int TIMEOUT = 15000;

	public static String fetchFile(final URL url) throws IOException {

		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setRequestProperty("User-Agent", "StarParse " + Application.VERSION);
		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);

		// checks server's status code first
		ByteArrayOutputStream bos = null;
		ReadableByteChannel rbc = null;
		WritableByteChannel wbc = null;
		try {
			int status = conn.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				throw new IOException("Server returned non-OK status: " + status + " (" + url + ")");
			}
			bos = new ByteArrayOutputStream();
			rbc = Channels.newChannel(conn.getInputStream());
			wbc = Channels.newChannel(bos);

			final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
			while (rbc.read(buffer) != -1) {
				buffer.flip();
				wbc.write(buffer);
				buffer.compact();
			}
			buffer.flip();
			while (buffer.hasRemaining()) {
				wbc.write(buffer);
			}
			return new String(bos.toByteArray());

		} finally {
			if (wbc != null) {
				try {
					wbc.close();
				} catch (Exception ignored) {

				}
			}
			if (rbc != null) {
				try {
					rbc.close();
				} catch (Exception ignored) {

				}
			}
			conn.disconnect();
		}
	}
}
