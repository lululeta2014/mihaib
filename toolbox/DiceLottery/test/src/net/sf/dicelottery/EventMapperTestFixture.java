package net.sf.dicelottery;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;

import net.sf.dicelottery.element.ElementRepresConverter;
import net.sf.dicelottery.element.ElementRepresentation;
import net.sf.dicelottery.event.Event;
import net.sf.dicelottery.event.EventRepresConverter;
import net.sf.dicelottery.event.EventRepresentation;
import net.sf.dicelottery.input.InputSourceType;

import org.junit.Before;
import org.junit.Test;

public class EventMapperTestFixture {

	ElementRepresentation nr = ElementRepresentation.NUMBER;
	EventRepresentation single = EventRepresentation.SINGLE_ELEMENT;
	InputSourceType usrInp = InputSourceType.USER_READER;
	EventRepresConverter dieEventRC, coinEventRC;

	@Before
	public void prepare() throws IOException {
		ElementRepresConverter dieElemRC = new ElementRepresConverter(nr,
				BigInteger.valueOf(6), null, false, null, false);
		dieEventRC = EventRepresConverter.getInstance(dieElemRC, single, null,
				false, false);
		ElementRepresConverter coinElemRC = new ElementRepresConverter(nr,
				BigInteger.valueOf(2), null, false, null, false);
		coinEventRC = EventRepresConverter.getInstance(coinElemRC, single,
				null, false, false);
	}

	/**
	 * Convert an internal zero-based Event index to a 1-based user value (by
	 * incrementing it).
	 */
	public BigInteger int2usr(BigInteger ind) {
		return ind.add(BigInteger.ONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidRepeats() throws IOException {
		EventRepresConverter srcEventRC = EventRepresConverter.getInstance(
				new ElementRepresConverter(nr, BigInteger.valueOf(10), null,
						false, null, false), single, null, false, false);
		EventRepresConverter destEventRC = EventRepresConverter.getInstance(
				new ElementRepresConverter(nr, BigInteger.valueOf(10), null,
						false, null, false), single, null, false, false);
		new EventMapper(srcEventRC, destEventRC, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void tooFewRepeats() throws IOException {
		EventRepresConverter destEventRC = EventRepresConverter.getInstance(
				new ElementRepresConverter(nr, BigInteger.valueOf(37), null,
						false, null, false), single, null, false, false);
		new EventMapper(dieEventRC, destEventRC, 2);
	}

	@Test
	public void minRepeats() throws Exception {
		assertTrue(EventMapper.getMinRepeats(coinEventRC, dieEventRC, null).repeats == 3);
		assertTrue(EventMapper.getMinRepeats(dieEventRC, coinEventRC, null).repeats == 1);
		assertTrue(EventMapper.getMinRepeats(coinEventRC, coinEventRC, null).repeats == 1);
	}

	@Test
	public void mapping_precisely_2_dice() throws IOException {
		EventRepresConverter destEventRC = EventRepresConverter.getInstance(
				new ElementRepresConverter(nr, BigInteger.valueOf(36), null,
						false, null, false), single, null, false, false);
		EventMapper mapper = new EventMapper(dieEventRC, destEventRC, 2);
		Mapping m = mapper.map(usrInp, new StringReader("1 1"), null);
		assertTrue(int2usr(m.destEvent.index).equals(BigInteger.ONE));
		m = mapper.map(usrInp, new StringReader("3 3"), null);
		assertTrue(int2usr(m.destEvent.index).equals(BigInteger.valueOf(15)));
		m = mapper.map(usrInp, new StringReader("6 6"), null);
		assertTrue(int2usr(m.destEvent.index).equals(BigInteger.valueOf(36)));

		ReverseMapping rm = mapper.reverseMap(usrInp, new StringReader("9"), 1,
				null);
		Event[][] evSol = rm.srcEventSolutions;
		assertTrue(evSol.length == 1 && evSol[0].length == 2);
		Event e1 = evSol[0][0], e2 = evSol[0][1];
		assertTrue(int2usr(e1.index).equals(BigInteger.valueOf(2)));
		assertTrue(int2usr(e2.index).equals(BigInteger.valueOf(3)));
	}

	@Test
	public void mapping_repeats_rollover() throws IOException {
		EventMapper mapper = new EventMapper(coinEventRC, dieEventRC, 4);
		Mapping m = mapper.map(usrInp, new StringReader("2 1 1 2"), null);
		assertTrue(int2usr(m.destEvent.index).equals(BigInteger.valueOf(4)));
		ReverseMapping rm = mapper.reverseMap(usrInp, new StringReader("4"), 2,
				null);
		Event[][] evSol = rm.srcEventSolutions;
		assertTrue(evSol.length == 2);
		assertTrue(evSol[0].length == 4 && evSol[1].length == 4);
		Event e1 = evSol[0][0], e2 = evSol[0][1], e3 = evSol[0][2], e4 = evSol[0][3];
		assertTrue(int2usr(e1.index).equals(BigInteger.valueOf(1)));
		assertTrue(int2usr(e2.index).equals(BigInteger.valueOf(1)));
		assertTrue(int2usr(e3.index).equals(BigInteger.valueOf(2)));
		assertTrue(int2usr(e4.index).equals(BigInteger.valueOf(2)));
		Event f1 = evSol[1][0], f2 = evSol[1][1], f3 = evSol[1][2], f4 = evSol[1][3];
		assertTrue(int2usr(f1.index).equals(BigInteger.valueOf(2)));
		assertTrue(int2usr(f2.index).equals(BigInteger.valueOf(1)));
		assertTrue(int2usr(f3.index).equals(BigInteger.valueOf(1)));
		assertTrue(int2usr(f4.index).equals(BigInteger.valueOf(2)));
	}

}
