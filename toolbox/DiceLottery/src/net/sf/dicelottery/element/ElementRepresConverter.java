/*
 * Copyright © Mihai Borobocea 2009, 2010, 2012
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

package net.sf.dicelottery.element;

import static net.sf.dicelottery.DiceLottery.charset;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Converts between the internal and the user-friendly representation of an
 * {@link Element}.
 */
public class ElementRepresConverter {

	private final Set<Character> separators;
	private final boolean whitespaceAlwaysSep;

	private final String[] elementStrings;
	private final Map<String, BigInteger> elementMap;

	public final BigInteger elementCount;

	private final ElementRepresentation elemRepr;

	/**
	 * Creates an ElementRepresConverter according to the specified arguments.
	 * 
	 * @param elemRepr
	 *            the type of element representation
	 * @param nr
	 *            the element count, for {@link ElementRepresentation#NUMBER}
	 * @param stringsFileName
	 *            file name to load custom strings from, for
	 *            {@link ElementRepresentation#CUSTOM_STRINGS}
	 * @param caseSensitive
	 *            specifies if custom strings are case sensitive, for
	 *            {@link ElementRepresentation#CUSTOM_STRINGS} representation
	 * @param sepFileName
	 *            file name to load separator characters from. If this argument
	 *            is <code>null</code> only whitespace characters are
	 *            separators.
	 * @param whitespaceAlwaysSep
	 *            specifies if whitespace characters are separators, in addition
	 *            to the characters loaded from <code>sepFile</code> (which may
	 *            include whitespace). Ignored if <code>sepFile</code> is
	 *            <code>null</code>.
	 * @throws UnsupportedOperationException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public ElementRepresConverter(final ElementRepresentation elemRepr,
			final BigInteger nr, final String stringsFileName,
			final boolean caseSensitive, final String sepFileName,
			boolean whitespaceAlwaysSep) throws UnsupportedOperationException,
			FileNotFoundException, IOException, IllegalArgumentException {

		// get separators

		Set<Character> separators = new HashSet<Character>();
		if (sepFileName == null) {
			// only whitespace characters are separators
			whitespaceAlwaysSep = true;
		} else {
			try (Reader r = new BufferedReader(new InputStreamReader(
					new FileInputStream(sepFileName), charset))) {
				int intChar;
				while ((intChar = r.read()) != -1) {
					char c = (char) intChar;
					separators.add(c);
				}
			}
		}

		this.separators = Collections.unmodifiableSet(separators);
		this.whitespaceAlwaysSep = whitespaceAlwaysSep;

		// get elements

		switch (elemRepr) {
		case CUSTOM_STRINGS:
			List<String> elementStrings = new ArrayList<String>();
			elementMap = new TreeMap<String, BigInteger>(caseSensitive ? null
					: String.CASE_INSENSITIVE_ORDER);

			try (Reader r = new BufferedReader(new InputStreamReader(
					new FileInputStream(stringsFileName), charset))) {
				while (true) {
					String crtUserRepr = readElementUserRepr(r);
					if (!elementMap.containsKey(crtUserRepr)) {
						elementStrings.add(crtUserRepr);
						elementMap.put(crtUserRepr,
								BigInteger.valueOf(elementStrings.size() - 1));
					}
				}
			} catch (EOFException e) {
				// do nothing, happens after last word in file
			}

			if (elementStrings.size() <= 1)
				throw new IllegalArgumentException("Too few strings: "
						+ elementStrings.size() + ". Must be > 1");
			this.elementStrings = elementStrings.toArray(new String[0]);
			this.elementCount = BigInteger.valueOf(this.elementStrings.length);
			break;

		case NUMBER:
			this.elementStrings = null;
			this.elementMap = null;
			this.elementCount = nr;

			if (this.elementCount.compareTo(BigInteger.ONE) <= 0)
				throw new IllegalArgumentException("Bad element count: " + nr
						+ ". Must be > 1");
			break;

		default:
			throw new UnsupportedOperationException(
					"Unknown ElementRepresentation " + elemRepr);
		}

		this.elemRepr = elemRepr;
	}

	/**
	 * Reads the user representation of an {@link Element} from the specified
	 * Reader.
	 * 
	 * @param r
	 *            the Reader to read the Element user representation from
	 * @return the user representation of the next element read
	 * @throws EOFException
	 *             if the Reader ends before any non-separator characters are
	 *             read
	 * @throws IOException
	 */
	private String readElementUserRepr(Reader r) throws EOFException,
			IOException {
		StringBuilder sb = new StringBuilder();
		int read;
		char c;

		// skip initial separators, if any and get first character
		while ((read = r.read()) != -1) {
			c = (char) read;
			if (isSeparator(c)) {
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
			if (isSeparator(c))
				break;
			else
				sb.append(c);
		}

		// the reader's stream may have finished or not, but we don't care
		// return the retrieved element
		return sb.toString();
	}

	/**
	 * Read an Element from the supplied Reader.
	 * 
	 * @param r
	 *            the Reader to read the Element's user representation from
	 * @return the first Element read from the supplied Reader
	 * @throws IllegalArgumentException
	 * @throws UnsupportedOperationException
	 * @throws EOFException
	 * @throws IOException
	 */
	public Element readElement(Reader r) throws IllegalArgumentException,
			UnsupportedOperationException, EOFException, IOException {
		String userRepres = readElementUserRepr(r);
		BigInteger elemIndex;

		switch (elemRepr) {
		case NUMBER:
			elemIndex = new BigInteger(userRepres).subtract(BigInteger.ONE);
			if (elemIndex.compareTo(BigInteger.ZERO) < 0
					|| elemIndex.compareTo(elementCount) >= 0)
				throw new IllegalArgumentException("Bad number: " + userRepres
						+ ". Must be >=1 and <=" + elementCount);
			break;
		case CUSTOM_STRINGS:
			elemIndex = elementMap.get(userRepres);
			if (elemIndex == null)
				throw new IllegalArgumentException("Invalid input Element \""
						+ userRepres + "\"");
			break;
		default:
			throw new UnsupportedOperationException(
					"Unknown ElementRepresentation " + elemRepr);
		}

		return new Element(elemIndex, userRepres);
	}

	/**
	 * Returns the Element with the specified index.
	 * 
	 * @param elementIndex
	 *            the desired element index
	 * @return the Element with the specified index
	 * @throws IllegalArgumentException
	 * @throws UnsupportedOperationException
	 */
	public Element getElement(BigInteger elementIndex)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (elementIndex.compareTo(BigInteger.ZERO) < 0
				|| elementIndex.compareTo(elementCount) >= 0)
			throw new IllegalArgumentException("ElementIndex " + elementIndex
					+ " too big");

		switch (elemRepr) {
		case NUMBER:
			String userRepres = elementIndex.add(BigInteger.ONE).toString();
			return new Element(elementIndex, userRepres);
		case CUSTOM_STRINGS:
			return new Element(elementIndex,
					elementStrings[elementIndex.intValue()]);
		default:
			throw new UnsupportedOperationException(
					"Unknown ElementRepresentation " + elemRepr);
		}
	}

	/**
	 * Checks if the specified character is a separator for the {@link Element}s
	 * used here. When reading input data for a mapping (and when reading custom
	 * strings from a file) the specified separators are used to delimit the
	 * Strings of interest. These separators are otherwise discarded.
	 * 
	 * @param c
	 *            the char to check
	 * @return true if <code>c</code> is a separator for the
	 *         <code>Element</code>s represented here, false otherwise
	 */
	public boolean isSeparator(char c) {
		return separators.contains(c)
				|| (whitespaceAlwaysSep && Character.isWhitespace(c));
	}

	@Override
	public String toString() {
		String descr;
		descr = "Elements are: " + elementCount + " ";

		switch (elemRepr) {
		case NUMBER:
			descr += "numbers";
			break;
		case CUSTOM_STRINGS:
			descr += "custom strings";
			break;
		default:
			throw new UnsupportedOperationException(
					"Unknown ElementRepresentation " + elemRepr);
		}

		return descr;
	}

}
