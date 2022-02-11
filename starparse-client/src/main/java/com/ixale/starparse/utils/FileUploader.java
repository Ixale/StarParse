package com.ixale.starparse.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPOutputStream;

import com.ixale.starparse.domain.Application;

public class FileUploader {

	private final String boundary;
	private static final String LINE_FEED = "\r\n";
	private final HttpURLConnection httpConn;
	private final String charset;
	private final OutputStream outputStream;
	private final PrintWriter writer;

	public FileUploader(String requestURL, String charset) throws Exception {
		this.charset = charset;

		// creates a unique boundary based on time stamp
		boundary = "===" + System.currentTimeMillis() + "===";

		final URL url = new URL(requestURL);
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true); // indicates POST method
		httpConn.setDoInput(true);
		httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		httpConn.setRequestProperty("User-Agent", "StarParse " + Application.VERSION);
		outputStream = httpConn.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
	}

	public void addFormField(String name, String value) {
		writer.append("--").append(boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=").append(charset).append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	public void addFilePart(final String fieldName, final String fileName, final byte[] content) throws IOException {
		writer.append("--").append(boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
			gzip.write(content);

		} catch (Exception ignore) {

		}

		if (baos.size() > 0) {
			writer.append("Content-Type: gzip").append(LINE_FEED);
		} else {
			writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
		}
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		if (baos.size() > 0) {
			outputStream.write(baos.toByteArray());
		} else if (content != null) {
			outputStream.write(content);
		}
		outputStream.flush();

		writer.append(LINE_FEED);
		writer.flush();
	}

	public void addHeaderField(String name, String value) {
		writer.append(name).append(": ").append(value).append(LINE_FEED);
		writer.flush();
	}

	public String finish() throws IOException {

		final StringBuilder sb = new StringBuilder();

		writer.append("--").append(boundary).append("--").append(LINE_FEED);
		writer.close();

		// checks server's status code first
		int status = httpConn.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append(LINE_FEED);
			}
			reader.close();
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}

		return sb.toString();
	}
}
