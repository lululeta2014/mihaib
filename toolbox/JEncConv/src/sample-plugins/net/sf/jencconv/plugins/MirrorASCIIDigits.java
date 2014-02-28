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

import net.sf.jencconv.ReaderFactory;

public class MirrorASCIIDigits implements ReaderFactory {

	@Override
	public Reader getFilter(final Reader r) {
		return new Reader() {
			@Override
			public void close() throws IOException {
				r.close();
			}

			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				len = r.read(cbuf, off, len);

				for (int i = off; i < off + len; i++) {
					if ('0' <= cbuf[i] && cbuf[i] <= '9')
						cbuf[i] = (char) ('0' + '9' - cbuf[i]);
				}

				return len;
			}
		};
	}

	@Override
	public String toString() {
		return "Mirror ASCII Digits";
	}

	@Override
	public String getDescription() {
		return "Replace x with 9-x for x in [0, 9]";
	}

}
