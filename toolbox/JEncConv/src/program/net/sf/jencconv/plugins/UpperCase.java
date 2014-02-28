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

public class UpperCase implements ReaderFactory {

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

				for (int i = off; i < off + len; i++)
					cbuf[i] = Character.toUpperCase(cbuf[i]);

				return len;
			}
		};
	}

	@Override
	public String toString() {
		return "CASE UPPER";
	}

	@Override
	public String getDescription() {
		return "Convert characters to UPPER CASE\n"
				+ "using the Character.toUpperCase() Java method.";
	}

}
