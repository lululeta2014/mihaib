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

public class AddNewlineAtEnd implements ReaderFactory {

	@Override
	public Reader getFilter(final Reader r) {
		return new Reader() {
			char lastChar = '\n';
			boolean newlineAppended;

			@Override
			public void close() throws IOException {
				r.close();
			}

			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				len = r.read(cbuf, off, len);

				if (len == -1 && lastChar != '\n' && !newlineAppended) {
					cbuf[off] = '\n';
					newlineAppended = true;
					return 1;
				}

				if (len > 0)
					lastChar = cbuf[off + len - 1];

				return len;
			}
		};
	}

	@Override
	public String toString() {
		return "Newline at end of file";
	}

	@Override
	public String getDescription() {
		return "Adds Newline (LF) at end of file if not present\n"
				+ "for non-empty files.";
	}

}
