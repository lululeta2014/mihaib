/*
 * Copyright © Mihai Borobocea 2012
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

public class NoSpaceBeforePunctuation implements ReaderFactory {

	private static final char[] EMPTY_ARR = new char[0];

	@Override
	public Reader getFilter(final Reader r) {
		return new Reader() {

			/** Whether EOF has been reached on 'r'. */
			private boolean eofReached;

			/** The last bunch of processed data. */
			private Reader processedData;

			/** Whitespace following the last bunch of processed data. */
			private char[] trailingWS = EMPTY_ARR;

			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				if (processedData != null) {
					int amount = processedData.read(cbuf, off, len);
					if (amount != -1)
						return amount;
					processedData = null;
				}

				if (eofReached) {
					if (trailingWS.length > 0) {
						processedData = new StringReader(new String(trailingWS));
						trailingWS = EMPTY_ARR;
						return read(cbuf, off, len);
					} else {
						return -1;
					}
				}

				char[] newData = trailingWS;
				trailingWS = EMPTY_ARR;
				while (processedData == null) {
					char[] tmp = new char[Converter.BUF_SIZE];
					int amount = r.read(tmp, 0, Converter.BUF_SIZE);
					if (amount <= 0) {
						eofReached = true;
						processedData = new StringReader(new String(newData));
						return read(cbuf, off, len);
					}

					newData = Arrays.copyOf(newData, newData.length + amount);
					for (int i = 0; i < amount; i++)
						newData[newData.length - 1 - i] = tmp[amount - 1 - i];

					boolean found = false;
					for (int i = 0; i < amount && !found; i++)
						if (!Character.isWhitespace(tmp[i])) {
							process(newData);
							found = true;
						}
				}

				return read(cbuf, off, len);
			}

			/** Process data, populating processedData and trailingWS fields. */
			private void process(char[] data) {
				trailingWS = EMPTY_ARR;
				StringBuilder sb = new StringBuilder();
				int start = 0, end = 0;
				while (start < data.length) {
					if (Character.isWhitespace(data[start])) {
						for (end = start + 1; end < data.length; end++)
							if (!Character.isWhitespace(data[end]))
								break;
						if (end == data.length)
							trailingWS = Arrays.copyOfRange(data, start, end);
						else if (data[end] != '!' && data[end] != '?'
								&& data[end] != '.')
							sb.append(data, start, end - start);
						start = end;
					} else {
						for (end = start + 1; end < data.length; end++)
							if (Character.isWhitespace(data[end]))
								break;
						sb.append(data, start, end - start);
						start = end;
					}
				}
				processedData = new StringReader(sb.toString());
			}

			@Override
			public void close() throws IOException {
				r.close();
			}
		};
	}

	@Override
	public String toString() {
		return "Punctuation: no whitespace before ‘!?.’";
	}

	@Override
	public String getDescription() {
		return "Removes any whitespace before ‘!’, ‘?’, ‘.’";
	}

}
