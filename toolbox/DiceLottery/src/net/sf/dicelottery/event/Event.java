/*
 * Copyright © Mihai Borobocea 2009
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

package net.sf.dicelottery.event;

import java.math.BigInteger;

/**
 * Outcome from an {@link EventRepresConverter}. Has an associated
 * <code>index</code> and a String user representation. The index value
 * displayed by the {@link #toString()} method is <code>index + 1</code>.
 */
public class Event {

	/**
	 * The index of this <code>Event</code> inside its
	 * <code>EventUniverse</code>
	 */
	public final BigInteger index;

	/** User representation */
	public final String userRepres;

	public Event(BigInteger index, String userRepres) {
		this.index = index;
		this.userRepres = userRepres;
	}

	@Override
	public String toString() {
		return userRepres + '(' + index.add(BigInteger.ONE) + ')';
	}

}
