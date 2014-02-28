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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

class Client {

	static void run(String host, int port, List<String> fileNames,
			boolean noPrealloc, boolean yes) throws IOException,
			BadASCIIException, NumberFormatException {
		Socket sock = null;
		try {
			sock = new Socket(host, port);

			InputStream in = new BufferedInputStream(sock.getInputStream());
			OutputStream out = new BufferedOutputStream(sock.getOutputStream());

			if (!agreeDirection(in, out, fileNames))
				return;

			if (fileNames.size() > 0)
				Sender.run(in, out, fileNames);
			else
				Receiver.run(in, out, noPrealloc, yes);
		} finally {
			if (sock != null)
				sock.close();
		}
	}

	private static boolean agreeDirection(InputStream in, OutputStream out,
			List<String> fileNames) throws IOException, BadASCIIException {
		String msg;
		if (fileNames.size() == 0)
			msg = "SERVER SENDS\n";
		else
			msg = "CLIENT SENDS\n";

		out.write(msg.getBytes(ascii));
		out.flush();

		String expected = "DIRECTION OK\n";
		String got = readASCIILine(in, expected.getBytes(ascii).length);

		if (!expected.equals(got)) {
			System.err.println("Server didn't confirm transfer direction.");
			return false;
		}

		return true;
	}

}
