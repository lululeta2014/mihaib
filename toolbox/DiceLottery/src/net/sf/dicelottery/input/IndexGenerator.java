/*
 * Copyright © Mihai Borobocea 2009, 2012
 * 
 * This file is part of DiceLottery.
 * 
 * DiceLottery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DiceLottery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DiceLottery.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.sf.dicelottery.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.net.MalformedURLException;

/**
 * Generate indexes from sources other than user input. Subclasses should be
 * tread safe: several threads may get the same reference via the synchronized
 * method {@link #getInstance(InputSourceType, BigInteger)}.
 */
public abstract class IndexGenerator {

	private static IndexGenerator lastInstance;
	private static InputSourceType lastInstanceType;
	private static BigInteger lastItemCount;

	/**
	 * Caches the last returned instance and reuses it if the next call has the
	 * same arguments.
	 * 
	 * @param inpSrcType
	 *            –
	 * @param itemCount
	 *            –
	 * @return –
	 * @throws IllegalArgumentException
	 *             –
	 * @throws MalformedURLException
	 *             –
	 */
	public static synchronized IndexGenerator getInstance(
			InputSourceType inpSrcType, BigInteger itemCount)
			throws IllegalArgumentException, MalformedURLException {
		if (inpSrcType == null)
			throw new IllegalArgumentException("InputSourceType cannot be null");
		if (inpSrcType == lastInstanceType && itemCount.equals(lastItemCount))
			return lastInstance;

		switch (inpSrcType) {
		case RANDOM_ORG:
			lastInstance = new RandomOrgIndexGenerator(itemCount);
			break;
		case SYS_RAND_GEN:
			lastInstance = new SysRandIndexGenerator(itemCount);
			break;
		default:
			throw new IllegalArgumentException("Unknown input source: "
					+ inpSrcType);
		}

		lastInstanceType = inpSrcType;
		lastItemCount = itemCount;
		return lastInstance;
	}

	public abstract BigInteger generateIndex() throws NumberFormatException,
			EOFException, IOException;

	protected BigInteger readBigInt(Reader r) throws EOFException, IOException,
			NumberFormatException {
		StringBuilder sb = new StringBuilder();
		int read;
		char c;

		// skip initial separators, if any and get first character
		while ((read = r.read()) != -1) {
			c = (char) read;
			if (Character.isWhitespace(c)) {
				continue;
			} else {
				sb.append(c);
				break;
			}
		}

		// stream ended (possibly after some separators) without data
		if (read == -1)
			throw new EOFException("Reader depleted, not enough data");

		// keep building the element (get the rest of its characters)
		while ((read = r.read()) != -1) {
			c = (char) read;
			if (Character.isWhitespace(c))
				break;
			else
				sb.append(c);
		}

		// the reader's stream may have finished or not, but we don't care
		// return the retrieved element

		return new BigInteger(sb.toString());
	}

}
