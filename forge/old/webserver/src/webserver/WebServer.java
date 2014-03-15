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

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class WebServer {

	private static final Logger logger = Logger.getLogger(WebServer.class
			.getName());

	/**
	 * Initializes a Properties object from UTF-8 configFile and returns it.
	 * 
	 * @param configFile
	 *            path to file
	 * @return a Properties object
	 * @throws IOException
	 */
	private static Properties getProperties(String configFile)
			throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(
				Paths.get(configFile), Charset.forName("UTF-8"))) {
			Properties config = new Properties();
			config.load(reader);
			return config;
		}
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: webserver config-file wwwroot");
			System.exit(1);
		}

		String wwwroot = args[1];
		Properties config = null;
		int listenPort, threads = 0;

		try {
			config = getProperties(args[0]);
			listenPort = Integer.parseInt(config.getProperty("port", "8000"));
			threads = Integer.parseInt(config.getProperty("threads", "10"));
		} catch (IOException e) {
			logger.warning("While reading properties file: " + e);
			System.exit(1);
			return;
		} catch (NumberFormatException e) {
			logger.severe(e.toString());
			System.exit(1);
			return;
		}

		try {
			ServerSocket servSock = new ServerSocket(listenPort);
			Executor executor = Executors.newFixedThreadPool(threads);

			logger.info("Listening on port " + servSock.getLocalPort());

			while (true) {
				Socket sock = servSock.accept();
				executor.execute(new RequestHandler(sock, wwwroot, config));
			}
		} catch (IOException e) {
			logger.severe(e.toString());
		}
	}
}
