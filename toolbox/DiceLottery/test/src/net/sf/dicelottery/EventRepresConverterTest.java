package net.sf.dicelottery;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;

import net.sf.dicelottery.element.ElementRepresConverter;
import net.sf.dicelottery.element.ElementRepresentation;
import net.sf.dicelottery.event.EventRepresConverter;
import net.sf.dicelottery.event.EventRepresentation;

import org.junit.Test;

public class EventRepresConverterTest {

	ElementRepresentation nr = ElementRepresentation.NUMBER;
	EventRepresentation single = EventRepresentation.SINGLE_ELEMENT;
	EventRepresentation lottery = EventRepresentation.LOTTERY;

	@Test(expected = IllegalArgumentException.class)
	public void singleEventRC() throws IOException {
		EventRepresConverter.getInstance(new ElementRepresConverter(nr,
				BigInteger.ONE, null, false, null, false), single, null, false,
				false);
	}

	@Test
	public void twoEventUniverse() throws IOException {
		EventRepresConverter eu = EventRepresConverter.getInstance(
				new ElementRepresConverter(nr, BigInteger.valueOf(2), null,
						false, null, false), single, null, false, false);
		assertTrue(eu != null);
	}

	// TODO: test with files

	// Combinations

	@Test
	public void comb_6_49() throws IOException {
		EventRepresConverter eu = EventRepresConverter.getInstance(
				new ElementRepresConverter(nr, BigInteger.valueOf(49), null,
						false, null, false), lottery, BigInteger.valueOf(6),
				false, false);
		assertTrue(eu.getEventCount().equals(BigInteger.valueOf(13983816)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void comb_1() throws IOException {
		EventRepresConverter.getInstance(new ElementRepresConverter(nr,
				BigInteger.valueOf(20), null, false, null, false), lottery,
				BigInteger.valueOf(20), false, false);
	}

	@Test
	public void arr_4_10() throws IOException {
		EventRepresConverter eu = EventRepresConverter.getInstance(
				new ElementRepresConverter(nr, BigInteger.valueOf(10), null,
						false, null, false), lottery, BigInteger.valueOf(4),
				true, false);
		assertTrue(eu.getEventCount().equals(BigInteger.valueOf(5040)));
	}

	@Test
	public void arrangementsLimitsOK() throws IOException {
		ElementRepresConverter arrElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		assertTrue(EventRepresConverter
				.getInstance(arrElem, lottery, BigInteger.valueOf(1), true,
						false).getEventCount().equals(BigInteger.valueOf(4)));

		assertTrue(EventRepresConverter
				.getInstance(arrElem, lottery, BigInteger.valueOf(4), true,
						false).getEventCount().equals(BigInteger.valueOf(24)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void arrangementsLimitsFailLow() throws IOException {
		ElementRepresConverter arrElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(arrElem, lottery,
				BigInteger.valueOf(0), true, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void arrangementsLimitsFailHigh() throws IOException {
		ElementRepresConverter arrElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(arrElem, lottery,
				BigInteger.valueOf(5), true, false);
	}

	@Test
	public void combinationsLimitsOK() throws IOException {
		ElementRepresConverter combElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		assertTrue(EventRepresConverter
				.getInstance(combElem, lottery, BigInteger.valueOf(1), false,
						false).getEventCount().equals(BigInteger.valueOf(4)));

		assertTrue(EventRepresConverter
				.getInstance(combElem, lottery, BigInteger.valueOf(3), false,
						false).getEventCount().equals(BigInteger.valueOf(4)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void combinationsLimitsFailLow() throws IOException {
		ElementRepresConverter combElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(combElem, lottery,
				BigInteger.valueOf(0), false, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void combinationsLimitsFailHigh() throws IOException {
		ElementRepresConverter combElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(combElem, lottery,
				BigInteger.valueOf(4), false, false);
	}

	@Test
	public void baseNLimitsOK() throws IOException {
		ElementRepresConverter baseNElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		assertTrue(EventRepresConverter
				.getInstance(baseNElem, lottery, BigInteger.valueOf(1), true,
						true).getEventCount().equals(BigInteger.valueOf(4)));

		assertTrue(EventRepresConverter
				.getInstance(baseNElem, lottery, BigInteger.valueOf(2), true,
						true).getEventCount().equals(BigInteger.valueOf(16)));

		assertTrue(EventRepresConverter
				.getInstance(baseNElem, lottery, BigInteger.valueOf(4), true,
						true).getEventCount().equals(BigInteger.valueOf(256)));

		assertTrue(EventRepresConverter
				.getInstance(baseNElem, lottery, BigInteger.valueOf(5), true,
						true).getEventCount().equals(BigInteger.valueOf(1024)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void baseNLimitsFailLow() throws IOException {
		ElementRepresConverter baseNElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(baseNElem, lottery,
				BigInteger.valueOf(0), true, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void baseNOverflow() throws IOException {
		ElementRepresConverter baseNElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(baseNElem, lottery,
				BigInteger.valueOf(2), true, true).getEvent(
				BigInteger.valueOf(16));
	}

	@Test(expected = IllegalArgumentException.class)
	public void baseNUnderflow() throws IOException {
		ElementRepresConverter baseNElem = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(baseNElem, lottery,
				BigInteger.valueOf(2), true, true).getEvent(
				BigInteger.valueOf(-1));
	}

	@Test
	public void sortedRepeatsLimitsOK() throws IOException {
		ElementRepresConverter sortedEl = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		assertTrue(EventRepresConverter
				.getInstance(sortedEl, lottery, BigInteger.valueOf(1), false,
						true).getEventCount().equals(BigInteger.valueOf(4)));

		assertTrue(EventRepresConverter
				.getInstance(sortedEl, lottery, BigInteger.valueOf(3), false,
						true).getEventCount().equals(BigInteger.valueOf(20)));

		// requesting first and last element doesn't throw exceptions
		EventRepresConverter.getInstance(sortedEl, lottery,
				BigInteger.valueOf(3), false, true).getEvent(
				BigInteger.valueOf(0));
		EventRepresConverter.getInstance(sortedEl, lottery,
				BigInteger.valueOf(3), false, true).getEvent(
				BigInteger.valueOf(19));

		// not 100% sure of this value
		assertTrue(EventRepresConverter
				.getInstance(sortedEl, lottery, BigInteger.valueOf(5), false,
						true).getEventCount().equals(BigInteger.valueOf(56)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void sortedRepeatsLimitsFailLow() throws IOException {
		ElementRepresConverter sortedEl = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(sortedEl, lottery,
				BigInteger.valueOf(0), false, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void sortedRepeatsOverflow() throws IOException {
		ElementRepresConverter sortedEl = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(sortedEl, lottery,
				BigInteger.valueOf(3), false, true).getEvent(
				BigInteger.valueOf(20));
	}

	@Test(expected = IllegalArgumentException.class)
	public void sortedRepeatsUnderflow() throws IOException {
		ElementRepresConverter sortedEl = new ElementRepresConverter(nr,
				BigInteger.valueOf(4), null, false, null, false);

		EventRepresConverter.getInstance(sortedEl, lottery,
				BigInteger.valueOf(3), false, true).getEvent(
				BigInteger.valueOf(-1));
	}

}
