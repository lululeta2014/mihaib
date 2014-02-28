package net.sf.dicelottery.event;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.Arrays;

import net.sf.dicelottery.element.Element;
import net.sf.dicelottery.element.ElementRepresConverter;

/** Uses O(N*K) memory. */
class SortedRepeatsRepresConverter extends LotteryRepresConverter {

	/**
	 * m[N][K] shows the number of strings of length K that can be created with
	 * N elements. To avoid confusion (by keeping N and K in
	 * "human representation", starting from 1) the first and last column (index
	 * 0) are dummies.
	 */
	private final BigInteger[][] m;

	private final BigInteger eventCount;

	private final static String startStr = "(", concatStr = ", ", endStr = ")";

	SortedRepeatsRepresConverter(ElementRepresConverter elementRepresConverter,
			BigInteger extractedElems) {
		super(elementRepresConverter, extractedElems, false, true);

		if (extractedElems.compareTo(BigInteger.ONE) < 0)
			throw new IllegalArgumentException(
					"Extracted elements must be >= 1");

		if (extractedElems.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
			throw new IllegalArgumentException("Extracted elements must be <= "
					+ Integer.MAX_VALUE);

		if (elementRepresConverter.elementCount.compareTo(BigInteger
				.valueOf(Integer.MAX_VALUE)) > 0)
			throw new IllegalArgumentException("Total elements must be <= "
					+ Integer.MAX_VALUE);

		int N = elementRepresConverter.elementCount.intValue();
		int K = extractedElems.intValue();
		m = new BigInteger[N + 1][K + 1];

		for (int k = 1; k < K + 1; k++)
			m[1][k] = BigInteger.ONE;
		for (int n = 1; n < N + 1; n++) {
			m[n][0] = BigInteger.ONE; // used in computation
			m[n][1] = BigInteger.valueOf(n);
		}

		for (int n = 2; n < N + 1; n++)
			for (int k = 2; k < K + 1; k++)
				m[n][k] = m[n - 1][k].add(m[n][k - 1]);

		eventCount = m[N][K];
	}

	@Override
	public BigInteger getEventCount() {
		return eventCount;
	}

	@Override
	public Event readEvent(Reader r) throws IllegalArgumentException,
			UnsupportedOperationException, EOFException, IOException {
		BigInteger[] elemIndexes = new BigInteger[extractedElems.intValue()];
		StringBuilder sb = new StringBuilder(startStr);
		for (int i = 0; i < elemIndexes.length; i++) {
			Element e = elementRepresConverter.readElement(r);
			elemIndexes[i] = e.index;
			if (i != 0)
				sb.append(concatStr);
			sb.append(e.userRepres);
		}
		sb.append(endStr);

		Arrays.sort(elemIndexes);

		int N = elementRepresConverter.elementCount.intValue();
		int K = extractedElems.intValue();
		BigInteger crtEvIndex = BigInteger.ZERO;
		int i = 0;

		while (i < elemIndexes.length) {
			BigInteger x = (i == 0) ? BigInteger.ZERO : elemIndexes[i - 1];
			while (x.compareTo(elemIndexes[i]) < 0) {
				crtEvIndex = crtEvIndex.add(m[N - x.intValue()][K - i - 1]);
				x = x.add(BigInteger.ONE);
			}
			i++;
		}

		return new Event(crtEvIndex, sb.toString());
	}

	@Override
	public Event getEvent(BigInteger eventIndex)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (eventIndex.compareTo(BigInteger.ZERO) < 0
				|| eventIndex.compareTo(eventCount) >= 0)
			throw new IllegalArgumentException("Bad event index " + eventIndex
					+ ", must be >=0 and < " + eventCount);

		int N = elementRepresConverter.elementCount.intValue();
		int K = extractedElems.intValue();

		BigInteger crtEvIndex = BigInteger.ZERO;
		BigInteger x = BigInteger.ZERO;
		StringBuilder sb = new StringBuilder(startStr);
		for (int i = 0; i < K; i++) {
			while (true) {
				BigInteger step = m[N - x.intValue()][K - i - 1];
				BigInteger afterStep = crtEvIndex.add(step);
				if (afterStep.compareTo(eventIndex) <= 0) {
					crtEvIndex = afterStep;
					x = x.add(BigInteger.ONE);
				} else {
					break;
				}
			}

			if (i != 0)
				sb.append(concatStr);
			sb.append(elementRepresConverter.getElement(x).userRepres);
		}
		sb.append(endStr);

		return new Event(eventIndex, sb.toString());
	}

}
