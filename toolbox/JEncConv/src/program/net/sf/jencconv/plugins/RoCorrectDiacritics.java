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

public class RoCorrectDiacritics implements ReaderFactory {
	private static final char[] search;
	private static final char[] replacement;

	static {
		SortedMap<Character, Character> map = new TreeMap<Character, Character>();
		map.put('ã', 'ă');
		map.put('Ã', 'Ă');
		map.put('ǎ', 'ă');
		map.put('Ǎ', 'Ă');
		map.put('ş', 'ș');
		map.put('Ş', 'Ș');
		map.put('ţ', 'ț');
		map.put('Ţ', 'Ț');

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
		return "RO Correct Diacritics";
	}

	@Override
	public String getDescription() {
		return "Use Correct Romanian diacritics\n"
				+ "S and T with comma below not cedilla\n"
				+ "A with breve not tilde or caron";
	}

}
