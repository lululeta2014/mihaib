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
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.jencconv.ReaderFactory;

public class Ro2Ascii implements ReaderFactory {
	private static final char[] search;
	private static final char[] replacement;

	static {
		SortedMap<Character, Character> map = new TreeMap<Character, Character>();
		map.put('ă', 'a');
		map.put('Ă', 'A');
		map.put('â', 'a');
		map.put('Â', 'A');
		map.put('î', 'i');
		map.put('Î', 'I');
		map.put('ș', 's');
		map.put('Ș', 'S');
		map.put('ț', 't');
		map.put('Ț', 'T');

		search = new char[map.size()];
		replacement = new char[map.size()];
		int i = 0;
		for (Map.Entry<Character, Character> e : map.entrySet()) {
			search[i] = e.getKey();
			replacement[i] = e.getValue();
			i++;
		}
	}

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
					int index = Arrays.binarySearch(search, cbuf[i]);
					if (index >= 0)
						cbuf[i] = replacement[index];
				}

				return len;
			}
		};
	}

	@Override
	public String toString() {
		return "RO to ASCII (remove diacritics)";
	}

	@Override
	public String getDescription() {
		return "Remove Romanian diacritics\n"
				+ "and replace them with English ASCII letters";
	}

}
