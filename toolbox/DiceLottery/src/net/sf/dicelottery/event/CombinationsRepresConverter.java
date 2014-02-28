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
import java.util.Arrays;

import net.sf.dicelottery.element.Element;
import net.sf.dicelottery.element.ElementRepresConverter;

class CombinationsRepresConverter extends LotteryRepresConverter {

	private final BigInteger eventCount;

	private final static String startStr = "(", concatStr = ", ", endStr = ")";

	CombinationsRepresConverter(
			final ElementRepresConverter elementRepresConverter,
			final BigInteger extractedElems) throws IllegalArgumentException {

		super(elementRepresConverter, extractedElems, false, false);

		if (extractedElems.compareTo(elementRepresConverter.elementCount) >= 0)
			throw new IllegalArgumentException(
					"Extracted elements must be < Total elements");

		if (extractedElems.compareTo(BigInteger.ZERO) <= 0)
			throw new IllegalArgumentException("Extracted elements must be > 0");

		if (extractedElems.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
			throw new IllegalArgumentException("Extracted elements must be <= "
					+ Integer.MAX_VALUE);

		eventCount = combinations(elementRepresConverter.elementCount,
				extractedElems);

		if (eventCount.compareTo(BigInteger.ONE) <= 0)
			throw new IllegalArgumentException("Bad event count " + eventCount
					+ ". Must be > 1");
	}

	@Override
	public Event readEvent(Reader r) throws IllegalArgumentException,
			EOFException, IOException {
		StringBuilder sb = new StringBuilder(startStr);

		BigInteger[] elements = new BigInteger[extractedElems.intValue()];
		for (int i = 0; i < extractedElems.intValue(); i++) {
			Element crtElem = elementRepresConverter.readElement(r);
			BigInteger crtIndex = crtElem.index;
			if (crtIndex.compareTo(elementRepresConverter.elementCount) >= 0)
				throw new IllegalArgumentException("Invalid input element \""
						+ crtElem + "\"");
			elements[i] = crtIndex;
			if (i > 0)
				sb.append(concatStr);
			sb.append(crtElem.userRepres);
		}

		sb.append(endStr);

		// sort outcome
		Arrays.sort(elements);

		// check for duplicates
		for (int i = 0; i < elements.length - 1; i++) {
			if (elements[i] == elements[i + 1])
				throw new IllegalArgumentException(
						"Duplicates not allowed for this mapping: "
								+ elementRepresConverter
										.getElement(elements[i]).userRepres);
		}

		BigInteger[] copy = new BigInteger[extractedElems.intValue()];
		int i = 0;
		copy[0] = BigInteger.ZERO;

		// elapsed combinations including current one (zero-indexed)
		BigInteger elapsed = BigInteger.ZERO;
		BigInteger crtN = elementRepresConverter.elementCount
				.subtract(BigInteger.ONE);
		BigInteger crtK = extractedElems.subtract(BigInteger.ONE);
		BigInteger step = combinations(crtN, crtK);

		int extractedElemsInt = extractedElems.intValue();
		while (i < extractedElemsInt - 1) {
			if (copy[i].compareTo(elements[i]) < 0) {
				copy[i] = copy[i].add(BigInteger.ONE);
				elapsed = elapsed.add(step);
				step = step.multiply(crtN.subtract(crtK)).divide(crtN);
				crtN = crtN.subtract(BigInteger.ONE);
			} else {
				copy[i + 1] = copy[i].add(BigInteger.ONE);
				i++;
				step = step.multiply(crtK).divide(crtN);
				crtN = crtN.subtract(BigInteger.ONE);
				crtK = crtK.subtract(BigInteger.ONE);
			}
		}

		elapsed = elapsed.add(elements[i].subtract(copy[i]));

		return new Event(elapsed, sb.toString());
	}

	@Override
	public Event getEvent(BigInteger eventIndex)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (eventIndex.compareTo(BigInteger.ZERO) < 0
				|| eventIndex.compareTo(eventCount) >= 0)
			throw new IllegalArgumentException("Invalid event index "
					+ eventIndex);

		// The idea for this algorithm is taken from
		// http://www.netlib.org/toms/515

		BigInteger[] elements = new BigInteger[extractedElems.intValue()];
		int i = 0;
		elements[0] = BigInteger.ZERO;

		// elapsed combinations including current one (being built)
		// zero-indexed, like the inner event indexes
		BigInteger elapsed = BigInteger.ZERO;
		BigInteger crtN = elementRepresConverter.elementCount
				.subtract(BigInteger.ONE);
		BigInteger crtK = extractedElems.subtract(BigInteger.ONE);
		BigInteger step = combinations(crtN, crtK);

		while (elapsed.compareTo(eventIndex) < 0) {
			if (step.add(elapsed).compareTo(eventIndex) <= 0) {
				elements[i] = elements[i].add(BigInteger.ONE);
				elapsed = elapsed.add(step);
				step = step.multiply(crtN.subtract(crtK)).divide(crtN);
				crtN = crtN.subtract(BigInteger.ONE);
				// step = combinations(crtN, crtK);
			} else {
				elements[i + 1] = elements[i].add(BigInteger.ONE);
				i++;
				step = step.multiply(crtK).divide(crtN);
				crtN = crtN.subtract(BigInteger.ONE);
				crtK = crtK.subtract(BigInteger.ONE);
				// step = combinations(crtN, crtK);
			}
		}

		int extractedElemsInt = extractedElems.intValue();
		for (int j = i + 1; j < extractedElemsInt; j++)
			elements[j] = elements[j - 1].add(BigInteger.ONE);

		StringBuilder sb = new StringBuilder(startStr);
		boolean first = true;
		for (BigInteger crtElemIndex : elements) {
			Element crtElem = elementRepresConverter.getElement(crtElemIndex);
			if (!first)
				sb.append(concatStr);
			else
				first = false;
			sb.append(crtElem.userRepres);
		}
		sb.append(endStr);

		return new Event(eventIndex, sb.toString());
	}

	@Override
	public BigInteger getEventCount() {
		return eventCount;
	}

}
