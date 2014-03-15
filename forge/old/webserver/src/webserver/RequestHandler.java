/*
 * Copyright © Mihai Borobocea 2011
 * 
 * This file is part of WebServer.
 * 
 * WebServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * WebServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with WebServer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package webserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

class RequestHandler implements Runnable {

	private static final Logger logger = Logger.getLogger(RequestHandler.class
			.getName());

	private final Socket sock;
	private final String wwwroot;
	private final Properties config;
	private String method, uri, protocol;

	RequestHandler(Socket sock, String wwwroot, Properties config) {
		this.sock = sock;
		this.wwwroot = wwwroot;
		this.config = config;
	}

	/**
	 * Returns the value of a hex digit as an int.
	 * 
	 * @param c
	 * @return the value of the hex digit
	 * @throws IOException
	 */
	private static int getHexVal(char c) throws IOException {
		if ('0' <= c && c <= '9')
			return c - '0';

		if ('a' <= c && c <= 'z')
			return 10 + c - 'a';

		if ('A' <= c && c <= 'Z')
			return 10 + c - 'A';

		throw new IOException("Invalid hex digit: " + c);
	}

	/**
	 * Unescapes the URI from an HTTP request (e.g. %20).
	 * 
	 * @param escaped
	 *            the URI string as received in the HTTP request
	 * @return the URI as a string after un-escaping
	 * @throws IOException
	 */
	private static String unescapeURI(String escaped) throws IOException {
		List<Byte> bytes = new ArrayList<Byte>();

		for (int i = 0; i < escaped.length(); i++) {
			if (escaped.charAt(i) != '%') {
				bytes.add((byte) escaped.charAt(i));
				continue;
			}

			if (i + 2 >= escaped.length()) {
				logger.warning("Badly escaped URI request: " + escaped);
				bytes.add((byte) escaped.charAt(i));
				continue;
			}

			char c1 = escaped.charAt(i + 1), c2 = escaped.charAt(i + 2);
			int v1 = getHexVal(c1), v2 = getHexVal(c2);
			i += 2;

			bytes.add((byte) (v1 * 16 + v2));
		}

		byte[] byteArray = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++)
			byteArray[i] = bytes.get(i);

		return new String(byteArray, "UTF-8");
	}

	private void parseHeaders(BufferedReader in) throws IOException {
		String line = in.readLine();
		if (line == null || line.isEmpty())
			throw new IOException("First line not a request header");
		String parts[] = line.split("[ ]+");
		if (parts.length != 3)
			throw new IOException("Invalid request header");

		method = parts[0];
		uri = unescapeURI(parts[1]);
		protocol = parts[2];

		// receive remaining headers
		do {
			line = in.readLine();
		} while (line != null && !line.isEmpty());
	}

	@Override
	public void run() {
		try (Socket _close_me = sock;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						sock.getInputStream(), "ASCII"));
				BufferedOutputStream out = new BufferedOutputStream(
						sock.getOutputStream())) {
			parseHeaders(in);

			sendResponse(out);

			// the output stream is flushed when try-with-resources closes it
		} catch (IOException e) {
			logger.info(e.toString());
		}
	}

	/**
	 * Returns the user-defined Content-Type string for the file's extension or
	 * null, if not found.
	 * 
	 * @param uri
	 * @param config
	 * @return
	 */
	private static String getContentType(String uri, Properties config) {
		int pos = uri.lastIndexOf('.');
		if (pos == -1)
			return null;
		return config.getProperty(uri.substring(pos));
	}

	private void sendResponse(BufferedOutputStream out) throws IOException {
		if (!protocol.startsWith("HTTP/")) {
			out.write((protocol + " 505 HTTP Version Not Supported\r\n")
					.getBytes("UTF-8"));
			out.write("\r\n".getBytes("UTF-8"));
			out.write(("Protocol " + protocol + " not supported")
					.getBytes("UTF-8"));
			return;
		}

		if (!method.equals("GET")) {
			out.write((protocol + " 405 Method Not Allowed\r\n")
					.getBytes("UTF-8"));
			out.write(("\r\n").getBytes("UTF-8"));
			out.write(("405 Method " + method + " Not Allowed")
					.getBytes("UTF-8"));
			return;
		}

		Path path = Paths.get(wwwroot, uri);

		if (!Files.exists(path)) {
			out.write((protocol + " 404 Not Found\r\n").getBytes("UTF-8"));
			out.write("Content-Type: text/plain\r\n".getBytes("UTF-8"));
			out.write("\r\n".getBytes("UTF-8"));
			out.write(("404 Not Found\n" + uri).getBytes("UTF-8"));
			return;
		}

		if (!Files.isRegularFile(path)) {
			out.write((protocol + " 200 OK\r\n").getBytes("UTF-8"));
			out.write("Content-Type: text/plain\r\n".getBytes("UTF-8"));
			out.write("\r\n".getBytes("UTF-8"));
			out.write((uri + " is not a regular file").getBytes("UTF-8"));
			return;
		}

		long fileSize = Files.size(path);
		String contentType = getContentType(uri, config);

		out.write((protocol + " 200 OK\r\n").getBytes("UTF-8"));
		if (contentType != null) {
			out.write(("Content-Type: " + contentType + "\r\n")
					.getBytes("UTF-8"));
		} else {
			String filename = path.getFileName().toString();
			out.write(("Content-disposition: attachment; filename=" + filename + "\r\n")
					.getBytes("UTF-8"));
		}
		out.write(("Content-Length: " + fileSize + "\r\n").getBytes("UTF-8"));
		out.write("\r\n".getBytes("UTF-8"));

		sendFile(path, out);
	}

	private static void sendFile(Path path, BufferedOutputStream out)
			throws IOException {
		final int BUF_SIZE = 2048;
		byte[] bytes = new byte[BUF_SIZE];

		try (BufferedInputStream in = new BufferedInputStream(
				Files.newInputStream(path))) {
			int count;
			while ((count = in.read(bytes)) != -1)
				out.write(bytes, 0, count);
		}
	}

}
