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

public class UnixNewlines implements ReaderFactory {

	@Override
	public Reader getFilter(final Reader r) {
		return new Reader() {
			boolean skipNextNL;

			@Override
			public void close() throws IOException {
				r.close();
			}

			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				len = r.read(cbuf, off, len);

				if (len == 1 && skipNextNL && cbuf[off] == '\n') {
					skipNextNL = false;
					len = r.read(cbuf, off, 1);
				}

				if (len <= 0)
					return len;

				int src = off, dest = off;
				for (; src < off + len; src++) {
					if (skipNextNL && cbuf[src] == '\n') {
						skipNextNL = false;
						continue;
					}

					if (cbuf[src] == '\r') {
						cbuf[src] = '\n';
						skipNextNL = true;
					} else {
						skipNextNL = false;
					}
					cbuf[dest++] = cbuf[src];
				}
				return dest - off;
			}
		};
	}

	@Override
	public String toString() {
		return "UNIX Newlines (LF)";
	}

	@Override
	public String getDescription() {
		return "Convert CR and CRLF to LF";
	}

}
