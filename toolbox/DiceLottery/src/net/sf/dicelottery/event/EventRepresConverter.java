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

package net.sf.dicelottery.event;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;

import net.sf.dicelottery.element.ElementRepresConverter;

/**
 * Converts between the internal and the user-friendly representation of an
 * {@link Event}.
 */
public abstract class EventRepresConverter {

	public final ElementRepresConverter elementRepresConverter;

	protected EventRepresConverter(
			final ElementRepresConverter elementRepresConverter) {

		this.elementRepresConverter = elementRepresConverter;
	}

	public abstract BigInteger getEventCount();

	public abstract Event readEvent(Reader r) throws IllegalArgumentException,
			UnsupportedOperationException, EOFException, IOException;

	public abstract Event getEvent(BigInteger eventIndex)
			throws IllegalArgumentException, UnsupportedOperationException;

	protected abstract String getEventDescription();

	public static EventRepresConverter getInstance(
			final ElementRepresConverter elementRepresConverter,
			final EventRepresentation eventRepr,
			final BigInteger extractedElems, final boolean orderMatters,
			final boolean multipleOccurrences) {

		switch (eventRepr) {
		case SINGLE_ELEMENT:
			return new IdentityRepresConverter(elementRepresConverter);
		case LOTTERY:
			final LotteryRepresConverter conv;
			if (orderMatters) {
				if (multipleOccurrences) {
					conv = new BaseNRepresConverter(elementRepresConverter,
							extractedElems);
				} else {
					conv = new ArrangementsRepresConverter(
							elementRepresConverter, extractedElems);
				}
			} else {
				if (multipleOccurrences) {
					conv = new SortedRepeatsRepresConverter(
							elementRepresConverter, extractedElems);
				} else {
					conv = new CombinationsRepresConverter(
							elementRepresConverter, extractedElems);
				}
			}
			return conv;
		default:
			throw new UnsupportedOperationException(
					"Unsupported EventRepresentation: " + eventRepr);
		}
	}

	@Override
	public String toString() {
		String descr;
		descr = getEventCount() + " events";
		descr += "\n" + getEventDescription();
		descr += "\n" + elementRepresConverter;
		return descr;
	}

}
