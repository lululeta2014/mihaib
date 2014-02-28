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

import net.sf.dicelottery.element.ElementRepresConverter;

abstract class LotteryRepresConverter extends EventRepresConverter {

	protected final BigInteger extractedElems;
	private final boolean orderMatters, multipleOccurrences;

	LotteryRepresConverter(final ElementRepresConverter elementRepresConverter,
			final BigInteger extractedElems, final boolean orderMatters,
			final boolean multipleOccurrences) {
		super(elementRepresConverter);
		this.extractedElems = extractedElems;
		this.orderMatters = orderMatters;
		this.multipleOccurrences = multipleOccurrences;
	}

	protected BigInteger factorial(BigInteger n)
			throws IllegalArgumentException {
		if (n.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException("Can't compute factorial for "
					+ n);

		BigInteger f = BigInteger.ONE;
		while (n.compareTo(BigInteger.ONE) > 0) {
			f = f.multiply(n);
			n = n.subtract(BigInteger.ONE);
		}
		return f;
	}

	protected BigInteger rangeProduct(BigInteger first, BigInteger last)
			throws IllegalArgumentException {
		if (first.compareTo(last) > 0)
			throw new IllegalArgumentException("Invalid range supplied: ["
					+ first + ", " + last + "] for rangeProduct");

		BigInteger result = BigInteger.ONE;
		while (first.compareTo(last) <= 0) {
			result = result.multiply(first);
			first = first.add(BigInteger.ONE);
		}
		return result;
	}

	protected BigInteger combinations(BigInteger n, BigInteger k)
			throws IllegalArgumentException {
		// check args
		if (n.compareTo(BigInteger.ZERO) < 0
				|| k.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException(
					"TotalBalls and ExtractedBalls must be >= 0");
		if (k.compareTo(n) > 0)
			throw new IllegalArgumentException(
					"ExtractedBalls must be <= TotalBalls");

		// k = max(k, n-k)
		BigInteger nMinK = n.subtract(k);
		if (k.compareTo(nMinK) < 0) {
			BigInteger tmp = nMinK;
			nMinK = k;
			k = tmp;
		}

		BigInteger comb;
		if (n.equals(k))
			comb = BigInteger.ONE;
		else
			comb = rangeProduct(k.add(BigInteger.ONE), n).divide(
					factorial(nMinK));
		return comb;
	}

	protected BigInteger arrangements(BigInteger n, BigInteger k)
			throws IllegalArgumentException {
		if (n.compareTo(BigInteger.ZERO) < 0
				|| k.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException(
					"TotalBalls and ExtractedBalls must be >= 0");
		if (k.compareTo(n) > 0)
			throw new IllegalArgumentException(
					"ExtractedBalls must be <= TotalBalls");

		// n - k
		BigInteger nMinK = n.subtract(k);

		BigInteger arr;
		if (nMinK.equals(n))
			arr = BigInteger.ONE;
		else
			arr = rangeProduct(nMinK.add(BigInteger.ONE), n);
		return arr;
	}

	@Override
	public String getEventDescription() {
		String descr;
		descr = "Lottery: extracting " + extractedElems + " element(s) out of "
				+ elementRepresConverter.elementCount + "\n";
		if (orderMatters)
			descr += "order matters, ";
		else
			descr += "in any order, ";
		if (multipleOccurrences)
			descr += "duplicates allowed";
		else
			descr += "no duplicates";
		return descr;
	}

}
