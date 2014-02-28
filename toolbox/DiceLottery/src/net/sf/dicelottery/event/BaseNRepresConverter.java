/*
 * Copyright © Mihai Borobocea 2012
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
import java.util.Deque;
import java.util.LinkedList;

import net.sf.dicelottery.element.Element;
import net.sf.dicelottery.element.ElementRepresConverter;

class BaseNRepresConverter extends LotteryRepresConverter {

	private final BigInteger eventCount;

	private final static String startStr = "(", concatStr = ", ", endStr = ")";

	BaseNRepresConverter(final ElementRepresConverter elementRepresConverter,
			final BigInteger extractedElems) throws IllegalArgumentException {

		super(elementRepresConverter, extractedElems, true, true);

		if (extractedElems.compareTo(BigInteger.ZERO) <= 0)
			throw new IllegalArgumentException("Extracted elements must be > 0");

		if (extractedElems.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
			throw new IllegalArgumentException("Extracted elements must be <= "
					+ Integer.MAX_VALUE);

		eventCount = elementRepresConverter.elementCount.pow(extractedElems
				.intValue());

		if (eventCount.compareTo(BigInteger.ONE) <= 0)
			throw new IllegalArgumentException("Bad event count " + eventCount
					+ ". Must be > 1");
	}

	@Override
	public BigInteger getEventCount() {
		return eventCount;
	}

	@Override
	public Event readEvent(Reader r) throws IllegalArgumentException,
			UnsupportedOperationException, EOFException, IOException {
		BigInteger index = BigInteger.ZERO;
		StringBuilder sb = new StringBuilder(startStr);

		BigInteger i = BigInteger.ZERO;
		while (!i.equals(extractedElems)) {
			Element e = elementRepresConverter.readElement(r);
			index = index.multiply(elementRepresConverter.elementCount).add(
					e.index);
			if (!i.equals(BigInteger.ZERO))
				sb.append(concatStr);
			sb.append(e.userRepres);
			i = i.add(BigInteger.ONE);
		}

		sb.append(endStr);

		return new Event(index, sb.toString());
	}

	@Override
	public Event getEvent(BigInteger eventIndex)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (eventIndex.compareTo(BigInteger.ZERO) < 0
				|| eventIndex.compareTo(eventCount) >= 0)
			throw new IllegalArgumentException("Invalid event index "
					+ eventIndex);

		Deque<String> q = new LinkedList<>();
		BigInteger n = eventIndex;
		while (!n.equals((BigInteger.ZERO))) {
			BigInteger[] QR = n
					.divideAndRemainder(elementRepresConverter.elementCount);
			n = QR[0];
			q.addFirst(elementRepresConverter.getElement(QR[1]).userRepres);
		}

		Element zeroElem = elementRepresConverter.getElement(BigInteger.ZERO);
		while (BigInteger.valueOf(q.size()).compareTo(extractedElems) < 0)
			q.addFirst(zeroElem.userRepres);

		boolean first = true;
		StringBuilder sb = new StringBuilder(startStr);
		for (String s : q) {
			if (first)
				first = false;
			else
				sb.append(concatStr);
			sb.append(s);
		}
		sb.append(endStr);

		return new Event(eventIndex, sb.toString());
	}

}
