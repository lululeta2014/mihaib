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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

class Util {

	static final Charset ascii = Charset.forName("ASCII");

	static final Scanner scanner = new Scanner(System.in);

	/**
	 * Read bytes until '\n' is encountered, maxBytes are read or the end of the
	 * stream is reached. Return a String with all ASCII characters read
	 * (including '\n' if found). If an ASCII code not allowed by the protocol
	 * is found, a BadASCIIException is thrown.
	 * 
	 * @param in
	 *            the InputStream
	 * @param maxBytes
	 *            maximum number of bytes, including '\n', to read
	 * @return a String with all ASCII chars read (at most maxBytes), including
	 *         '\n' if found
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *             if <code>in</code> is null or maxBytes is &lt;= 0
	 * @throws BadASCIIException
	 *             if a byte not allowed by the protocol in text messages is
	 *             encountered
	 */
	static String readASCIILine(InputStream in, int maxBytes)
			throws IOException, IllegalArgumentException, BadASCIIException {
		if (in == null || maxBytes <= 0)
			throw new IllegalArgumentException(
					"InputStream null or maxBytes <= 0");

		StringBuilder sb = new StringBuilder();

		while (true) {
			int i = in.read();
			if (i == -1)
				return sb.toString();

			if (!goodASCII(i))
				throw new BadASCIIException(i);

			sb.append((char) i);
			maxBytes--;

			if (i == '\n' || maxBytes == 0)
				return sb.toString();
		}
	}

	private static boolean goodASCII(int i) {
		if (i > 127)
			return false;
		if ('A' <= i && i <= 'Z')
			return true;
		if ('a' <= i && i <= 'z')
			return true;
		if ('0' <= i && i <= '9')
			return true;

		switch (i) {
		case '\n':
		case ' ':
		case '!':
		case '#':
		case '$':
		case '%':
		case '&':
		case '\'':
		case '(':
		case ')':
		case '+':
		case ',':
		case '-':
		case '.':
		case ';':
		case '=':
		case '@':
		case '[':
		case ']':
		case '^':
		case '_':
		case '`':
		case '{':
		case '}':
		case '~':
			return true;
		}

		return false;
	}

	/**
	 * Transfer count bytes from in to out and return the number of bytes
	 * transferred (can be less than count if <code>in</code> reaches end of
	 * file).
	 * 
	 * @param in
	 *            where to read from
	 * @param out
	 *            where to write to
	 * @param count
	 *            the number of bytes to transfer (unless <code>in</code>
	 *            reaches end of file)
	 * @param transferName
	 *            the name to print in the console (for progress updates)
	 * @return the number of bytes transferred (less than count if
	 *         <code>in</code> reached end of file)
	 * @throws IOException
	 */
	static long transferBytes(InputStream in, OutputStream out, long count,
			String transferName) throws IOException {
		long done = 0;
		byte[] b = new byte[4096];
		int len;
		StatusPrinter statusPrinter = new StatusPrinter(transferName, count);

		while (done < count && (len = readBytes(in, b, count - done)) != -1) {
			out.write(b, 0, len);
			done += len;
			statusPrinter.update(done);
		}

		System.out.println();
		return done;
	}

	/**
	 * Calls in.read(b, 0, (int)Math.min(b.length, maxBytes)) and returns its
	 * return value.
	 * 
	 * @param in
	 *            the InputStream to read from
	 * @param b
	 *            the array to write to
	 * @param maxBytes
	 *            max bytes to read from the stream
	 * @return the return value of in.read(b, 0, (int)Math.min(b.length,
	 *         maxBytes))
	 * @throws IOException
	 */
	private static int readBytes(InputStream in, byte[] b, long maxBytes)
			throws IOException {
		int len = (int) Math.min(b.length, maxBytes);
		return in.read(b, 0, len);
	}

	static String approxSize(long fileSize) {
		String[] units = { "KB", "MB", "GB" };

		if (fileSize < 1024)
			return fileSize + "  B";

		long unit = 1024;
		int i = 0;
		while (fileSize / unit >= 1024 && i < units.length - 1) {
			unit *= 1024;
			i++;
		}

		return String.format("%.1f %s", ((double) fileSize) / unit, units[i]);
	}

}
