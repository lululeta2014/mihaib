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

package net.sf.dicelottery.element;

import java.math.BigInteger;

/**
 * Basic element used to form {@link net.sf.dicelottery.event.Event}s in a
 * universe. It is identified by a zero-based <code>index</code> and a String
 * user representation. The index value displayed by the {@link #toString()}
 * method is <code>index + 1</code>.
 */
public class Element {

	/** The zero-based index of this Element */
	public final BigInteger index;

	/** The user representation of this Element */
	public final String userRepres;

	/**
	 * Constructs a new Element with the specified zero-based <code>index</code>
	 * and String user representation.
	 * 
	 * @param index
	 *            the zero-based index
	 * @param userRepres
	 *            the user representation
	 * @throws IllegalArgumentException
	 *             –
	 */
	public Element(BigInteger index, String userRepres)
			throws IllegalArgumentException {
		if (index.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException("Invalid index: " + index);
		if (userRepres.isEmpty())
			throw new IllegalArgumentException("Empty element representation");

		this.index = index;
		this.userRepres = userRepres;
	}

	@Override
	public String toString() {
		return userRepres + '(' + index.add(BigInteger.ONE) + ')';
	}

}
