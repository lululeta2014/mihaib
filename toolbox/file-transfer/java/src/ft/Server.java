/*
 * Copyright © Mihai Borobocea 2010
 * 
 * This file is part of File Transfer.
 * 
 * File Transfer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * File Transfer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with File Transfer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package ft;

import static ft.Util.ascii;
import static ft.Util.readASCIILine;
import static ft.Util.scanner;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import socks.CProxy;
import socks.Socks5Proxy;
import socks.SocksServerSocket;

class Server {

	static ServerSocket getLocalServerSocket(int port) throws IOException {
		return new ServerSocket(port);
	}

	static ServerSocket getSocks5ServerSocket(String host, int port,
			String peerHost, int peerPort) throws IOException {
		CProxy proxy = new Socks5Proxy(host, port);
		ServerSocket servSock = new SocksServerSocket(proxy, host, port);

		// print reverse lookup info, if available
		System.out.println("SOCKS5 server is listening on "
				+ servSock.getInetAddress() + ":" + servSock.getLocalPort());
		System.out.println();

		// print short IP:port
		System.out.println("file-transfer client must connect to "
				+ servSock.getInetAddress().getHostAddress() + ":"
				+ servSock.getLocalPort());
		System.out.println();
		return servSock;
	}

	static void run(ServerSocket servSock, List<String> fileNames,
			boolean noPrealloc, boolean reverseLookup, boolean yes)
			throws IOException, BadASCIIException, NumberFormatException {
		Socket sock = null;
		try {
			sock = servSock.accept();

			String hostAddr = sock.getInetAddress().getHostAddress();
			System.out.print("Connection from " + hostAddr);

			if (reverseLookup) {
				String prompt = " (performing reverse lookup, please wait.. ";
				System.out.print(prompt);

				String reverseName = sock.getInetAddress().getHostName();
				System.out.print("\rConnection from " + hostAddr);
				String extra = " ";
				if (!reverseName.equals(hostAddr))
					extra += "(" + reverseName + ")";
				System.out.print(String.format("%-" + prompt.length() + "s",
						extra));
			}

			System.out.println();
			System.out.print("Proceed? [Yes/no] ");

			String answer;
			if (yes) {
				answer = "";
				System.out.println("Yes");
			} else {
				answer = scanner.nextLine().trim().toUpperCase();
			}
			if (!"YES".startsWith(answer))
				return;

			InputStream in = new BufferedInputStream(sock.getInputStream());
			OutputStream out = new BufferedOutputStream(sock.getOutputStream());

			if (!agreeDirection(in, out, fileNames))
				return;

			if (fileNames.size() > 0)
				Sender.run(in, out, fileNames);
			else
				Receiver.run(in, out, noPrealloc, yes);
		} finally {
			try {
				if (sock != null)
					sock.close();
			} finally {
				servSock.close();
			}
		}
	}

	private static boolean agreeDirection(InputStream in, OutputStream out,
			List<String> fileNames) throws IOException, BadASCIIException {
		String expected;
		if (fileNames.size() == 0)
			expected = "CLIENT SENDS\n";
		else
			expected = "SERVER SENDS\n";

		// even if the two possible messages have different lengths,
		// we're only interested in getting the correct one
		String got = readASCIILine(in, expected.getBytes(ascii).length);

		if (!expected.equals(got)) {
			System.err.println("Client didn't send correct transfer direction");
			return false;
		}

		out.write("DIRECTION OK\n".getBytes(ascii));
		out.flush();
		return true;
	}

}
