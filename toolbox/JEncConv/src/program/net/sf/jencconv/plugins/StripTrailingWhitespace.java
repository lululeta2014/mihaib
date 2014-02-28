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

package net.sf.jencconv.plugins;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import net.sf.jencconv.Converter;
import net.sf.jencconv.ReaderFactory;

public class StripTrailingWhitespace implements ReaderFactory {

	@Override
	public Reader getFilter(final Reader r) {
		return new Reader() {
			Reader strippedData;
			char[] trailingWS = new char[0],
					tmp = new char[Converter.BUF_SIZE];

			@Override
			public void close() throws IOException {
				r.close();
			}

			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				if (strippedData != null) {
					int amount = strippedData.read(cbuf, off, len);
					if (amount != -1)
						return amount;
					strippedData = null;
				}

				while (strippedData == null) {
					int amount = r.read(tmp);
					if (amount <= 0)
						return amount;

					/** Index of first non-whitespace or '\n' character */
					int start = 0;
					while (start < amount && tmp[start] != '\n'
							&& Character.isWhitespace(tmp[start]))
						start++;

					if (start == amount) {
						char[] moreWS = Arrays.copyOf(trailingWS,
								trailingWS.length + amount);
						for (int i = 0; i < amount; i++)
							moreWS[trailingWS.length + i] = tmp[i];

						trailingWS = moreWS;
						continue;
					}

					// strip trailing whitespace from tmp
					int to, from;
					to = from = start + 1;
					while (from < amount) {
						if (tmp[from] == '\n'
								|| !Character.isWhitespace(tmp[from])) {
							tmp[to++] = tmp[from++];
						} else {
							int next = from + 1;
							while (next < amount && tmp[next] != '\n'
									&& Character.isWhitespace(tmp[next]))
								next++;

							if (next == amount) {
								break;
							} else if (tmp[next] == '\n') {
								from = next;
							} else {
								while (from < next)
									tmp[to++] = tmp[from++];
							}
						}
					}

					if (tmp[start] == '\n') {
						strippedData = new StringReader(new String(tmp, start,
								to - start));
					} else {
						StringBuilder sb = new StringBuilder(trailingWS.length
								+ to);
						sb.append(trailingWS);
						sb.append(tmp, 0, to);
						strippedData = new StringReader(sb.toString());
					}

					trailingWS = Arrays.copyOfRange(tmp, from, tmp.length);
				}

				return strippedData.read(cbuf, off, len);
			}
		};
	}

	@Override
	public String toString() {
		return "Strip Trailing Whitespace";
	}

	@Override
	public String getDescription() {
		return "Strip whitespace at end of lines (before '\\n').\n"
				+ "Use this plugin on files with UNIX newlines.";
	}

}
