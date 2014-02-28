/*
 * Copyright © Mihai Borobocea 2010
 * 
 * This file is part of JEncConv.
 * 
 * JEncConv is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JEncConv is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JEncConv.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.sf.jencconv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;
import java.util.List;

public class Converter {

	public static final int BUF_SIZE = 4096;

	static Reader chainPlugins(Reader r, ReaderFactory[] plugins) {
		if (plugins == null)
			return r;

		for (ReaderFactory plg : plugins)
			r = plg.getFilter(r);

		return r;
	}

	static void convert(String inFileName, CharsetDecoder decoder,
			String outFileName, CharsetEncoder encoder, ReaderFactory[] plugins)
			throws FileNotFoundException, IOException {
		Reader in = null;
		BufferedWriter out = null;

		try {
			FileInputStream fis = new FileInputStream(inFileName);
			in = new BufferedReader(new InputStreamReader(fis, decoder));

			FileOutputStream fos = new FileOutputStream(outFileName);
			out = new BufferedWriter(new OutputStreamWriter(fos, encoder));

			in = chainPlugins(in, plugins);

			char[] buf = new char[BUF_SIZE];
			int len;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
		} finally {
			// Attempt to close both streams
			try {
				if (out != null)
					out.close();
			} finally {
				if (in != null)
					in.close();
			}
		}
	}

	static Charset[] getPossibleCharsets(String fileName)
			throws FileNotFoundException {
		List<Charset> validCharsets = new LinkedList<Charset>();
		char[] buf = new char[BUF_SIZE];

		for (Charset cs : Charset.availableCharsets().values()) {
			BufferedReader in = null;
			try {
				FileInputStream fis = new FileInputStream(fileName);
				in = new BufferedReader(new InputStreamReader(fis, cs
						.newDecoder()));
				while (in.read(buf) != -1) {
					// empty loop
				}
				validCharsets.add(cs);
			} catch (FileNotFoundException fnfe) {
				throw fnfe;
			} catch (IOException ioe) {
				// charset not valid for file
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
					System.err.println("while closing buffered reader: " + e);
				}
			}
		}

		return validCharsets.toArray(new Charset[0]);
	}

}
