package net.sf.dicelottery;

import static org.junit.Assert.assertTrue;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import net.sf.dicelottery.element.ElementRepresConverter;
import net.sf.dicelottery.element.ElementRepresentation;
import net.sf.dicelottery.event.Event;
import net.sf.dicelottery.event.EventRepresConverter;
import net.sf.dicelottery.event.EventRepresentation;
import net.sf.dicelottery.input.InputSourceType;

import org.junit.Test;

public class EventMapperTest {

	private static Event[] readAllEvents(EventRepresConverter erc, String s)
			throws IOException {
		List<Event> result = new ArrayList<>();
		Reader r = new StringReader(s);
		try {
			while (true) {
				result.add(erc.readEvent(r));
			}
		} catch (EOFException e) {
		}
		return result.toArray(new Event[0]);
	}

	private static boolean sameIndexes(Event[] e1, Event[] e2) {
		if (e1.length != e2.length)
			return false;
		for (int i = 0; i < e1.length; i++)
			if (!e1[i].index.equals(e2[i].index))
				return false;
		return true;
	}

	@Test
	public void singleToComb() throws IOException {
		ElementRepresConverter singleEl = new ElementRepresConverter(
				ElementRepresentation.NUMBER, BigInteger.valueOf(2), null,
				false, null, false);
		EventRepresConverter singleEv = EventRepresConverter.getInstance(
				singleEl, EventRepresentation.SINGLE_ELEMENT, null, false,
				false);

		ElementRepresConverter combEl = new ElementRepresConverter(
				ElementRepresentation.NUMBER, BigInteger.valueOf(4), null,
				false, null, false);
		EventRepresConverter combEv = EventRepresConverter.getInstance(combEl,
				EventRepresentation.LOTTERY, BigInteger.valueOf(3), false,
				false);

		EventMapper mapper = new EventMapper(singleEv, combEv, 3);

		Mapping m = mapper.map(InputSourceType.USER_READER, new StringReader(
				"1 1 1"), null);
		assertTrue(m.destEvent.index.equals(combEv.readEvent(new StringReader(
				"1 2 3")).index));

		m = mapper.map(InputSourceType.USER_READER, new StringReader("1 1 2"),
				null);
		assertTrue(m.destEvent.index.equals(combEv.readEvent(new StringReader(
				"1 2 4")).index));

		m = mapper.map(InputSourceType.USER_READER, new StringReader("2 2 1"),
				null);
		assertTrue(m.destEvent.index.equals(combEv.readEvent(new StringReader(
				"1 3 4")).index));

		ReverseMapping rm = mapper.reverseMap(InputSourceType.USER_READER,
				new StringReader("1 2 4"), 2, null);
		assertTrue(rm.srcEventSolutions.length == 2);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "1 1 2")));
		assertTrue(sameIndexes(rm.srcEventSolutions[1],
				readAllEvents(singleEv, "2 1 2")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"2 3 4"), 2, null);
		assertTrue(rm.srcEventSolutions.length == 2);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "1 2 2")));
		assertTrue(sameIndexes(rm.srcEventSolutions[1],
				readAllEvents(singleEv, "2 2 2")));
	}

	@Test
	public void singleToArrangements() throws IOException {
		ElementRepresConverter singleEl = new ElementRepresConverter(
				ElementRepresentation.NUMBER, BigInteger.valueOf(4), null,
				false, null, false);
		EventRepresConverter singleEv = EventRepresConverter.getInstance(
				singleEl, EventRepresentation.SINGLE_ELEMENT, null, false,
				false);

		ElementRepresConverter arrEl = new ElementRepresConverter(
				ElementRepresentation.NUMBER, BigInteger.valueOf(3), null,
				false, null, false);
		EventRepresConverter arrEv = EventRepresConverter
				.getInstance(arrEl, EventRepresentation.LOTTERY,
						BigInteger.valueOf(2), true, false);

		EventMapper mapper = new EventMapper(singleEv, arrEv, 2);

		Mapping m = mapper.map(InputSourceType.USER_READER, new StringReader(
				"2 1"), null);
		assertTrue(m.destEvent.index.equals(arrEv.readEvent(new StringReader(
				"3 1")).index));

		m = mapper.map(InputSourceType.USER_READER, new StringReader("3 4"),
				null);
		assertTrue(m.destEvent.index.equals(arrEv.readEvent(new StringReader(
				"3 2")).index));

		ReverseMapping rm = mapper.reverseMap(InputSourceType.USER_READER,
				new StringReader("1 3"), 2, null);
		assertTrue(rm.srcEventSolutions.length == 2);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "1 2")));
		assertTrue(sameIndexes(rm.srcEventSolutions[1],
				readAllEvents(singleEv, "2 4")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"3 2"), 2, null);
		assertTrue(rm.srcEventSolutions.length == 2);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "2 2")));
		assertTrue(sameIndexes(rm.srcEventSolutions[1],
				readAllEvents(singleEv, "3 4")));
	}

	@Test
	public void singleToBaseN() throws IOException {
		ElementRepresConverter singleEl = new ElementRepresConverter(
				ElementRepresentation.NUMBER, BigInteger.valueOf(5), null,
				false, null, false);
		EventRepresConverter singleEv = EventRepresConverter.getInstance(
				singleEl, EventRepresentation.SINGLE_ELEMENT, null, false,
				false);

		ElementRepresConverter baseNEl = new ElementRepresConverter(
				ElementRepresentation.NUMBER, BigInteger.valueOf(3), null,
				false, null, false);
		EventRepresConverter baseNEv = EventRepresConverter.getInstance(
				baseNEl, EventRepresentation.LOTTERY, BigInteger.valueOf(2),
				true, true);

		EventMapper mapper = new EventMapper(singleEv, baseNEv, 2);

		Mapping m = mapper.map(InputSourceType.USER_READER, new StringReader(
				"2 1"), null);
		assertTrue(m.destEvent.index.equals(baseNEv.readEvent(new StringReader(
				"2 3")).index));

		m = mapper.map(InputSourceType.USER_READER, new StringReader("3 2"),
				null);
		assertTrue(m.destEvent.index.equals(baseNEv.readEvent(new StringReader(
				"1 3")).index));

		ReverseMapping rm = mapper.reverseMap(InputSourceType.USER_READER,
				new StringReader("3 2"), 2, null);
		assertTrue(rm.srcEventSolutions.length == 2);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "2 3")));
		assertTrue(sameIndexes(rm.srcEventSolutions[1],
				readAllEvents(singleEv, "4 2")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"2 1"), 2, null);
		assertTrue(rm.srcEventSolutions.length == 2);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "1 4")));
		assertTrue(sameIndexes(rm.srcEventSolutions[1],
				readAllEvents(singleEv, "3 3")));

		// first
		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"1 1"), 2, null);
		assertTrue(rm.srcEventSolutions.length == 2);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "1 1")));
		assertTrue(sameIndexes(rm.srcEventSolutions[1],
				readAllEvents(singleEv, "2 5")));

		// last
		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"3 3"), 2, null);
		assertTrue(rm.srcEventSolutions.length == 2);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "2 4")));
		assertTrue(sameIndexes(rm.srcEventSolutions[1],
				readAllEvents(singleEv, "4 3")));
	}

	@Test
	public void singleToSortedRepeats() throws IOException {
		ElementRepresConverter singleEl = new ElementRepresConverter(
				ElementRepresentation.NUMBER, BigInteger.valueOf(4), null,
				false, null, false);
		EventRepresConverter singleEv = EventRepresConverter.getInstance(
				singleEl, EventRepresentation.SINGLE_ELEMENT, null, false,
				false);

		ElementRepresConverter sortedEl = new ElementRepresConverter(
				ElementRepresentation.NUMBER, BigInteger.valueOf(2), null,
				false, null, false);
		EventRepresConverter sortedEv = EventRepresConverter.getInstance(
				sortedEl, EventRepresentation.LOTTERY, BigInteger.valueOf(3),
				false, true);
		assertTrue(sortedEv.getEventCount().equals(BigInteger.valueOf(4)));

		EventMapper mapper = new EventMapper(singleEv, sortedEv, 1);

		Mapping m = mapper.map(InputSourceType.USER_READER, new StringReader(
				"1"), null);
		assertTrue(m.destEvent.index.equals(sortedEv
				.readEvent(new StringReader("1 1 1")).index));

		m = mapper
				.map(InputSourceType.USER_READER, new StringReader("2"), null);
		assertTrue(m.destEvent.index.equals(sortedEv
				.readEvent(new StringReader("1 1 2")).index));

		// make sure order doesn't matter
		m = mapper
				.map(InputSourceType.USER_READER, new StringReader("3"), null);
		assertTrue(m.destEvent.index.equals(sortedEv
				.readEvent(new StringReader("2 2 1")).index));

		m = mapper
				.map(InputSourceType.USER_READER, new StringReader("4"), null);
		assertTrue(m.destEvent.index.equals(sortedEv
				.readEvent(new StringReader("2 2 2")).index));

		ReverseMapping rm = mapper.reverseMap(InputSourceType.USER_READER,
				new StringReader("1 1 1"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "1")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"1 1 2"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "2")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"1 2 2"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "3")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"2 2 2"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "4")));

		singleEl = new ElementRepresConverter(ElementRepresentation.NUMBER,
				BigInteger.valueOf(5), null, false, null, false);
		singleEv = EventRepresConverter.getInstance(singleEl,
				EventRepresentation.SINGLE_ELEMENT, null, false, false);

		sortedEl = new ElementRepresConverter(ElementRepresentation.NUMBER,
				BigInteger.valueOf(4), null, false, null, false);
		sortedEv = EventRepresConverter
				.getInstance(sortedEl, EventRepresentation.LOTTERY,
						BigInteger.valueOf(3), false, true);
		assertTrue(sortedEv.getEventCount().equals(BigInteger.valueOf(20)));

		mapper = new EventMapper(singleEv, sortedEv, 2);

		m = mapper.map(InputSourceType.USER_READER, new StringReader("1 1"),
				null);
		assertTrue(m.destEvent.index.equals(sortedEv
				.readEvent(new StringReader("1 1 1")).index));

		m = mapper.map(InputSourceType.USER_READER, new StringReader("2 4"),
				null);
		assertTrue(m.destEvent.index.equals(sortedEv
				.readEvent(new StringReader("1 3 4")).index));

		m = mapper.map(InputSourceType.USER_READER, new StringReader("4 1"),
				null);
		assertTrue(m.destEvent.index.equals(sortedEv
				.readEvent(new StringReader("2 4 4")).index));

		m = mapper.map(InputSourceType.USER_READER, new StringReader("4 2"),
				null);
		assertTrue(m.destEvent.index.equals(sortedEv
				.readEvent(new StringReader("3 3 3")).index));

		m = mapper.map(InputSourceType.USER_READER, new StringReader("4 5"),
				null);
		assertTrue(m.destEvent.index.equals(sortedEv
				.readEvent(new StringReader("4 4 4")).index));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"1 1 1"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "1 1")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"1 2 3"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "2 1")));

		// make sure order doesn't matter
		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"3 1 2"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "2 1")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"2 4 4"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "4 1")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"3 3 3"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "4 2")));

		rm = mapper.reverseMap(InputSourceType.USER_READER, new StringReader(
				"4 4 4"), 1, null);
		assertTrue(rm.srcEventSolutions.length == 1);
		assertTrue(sameIndexes(rm.srcEventSolutions[0],
				readAllEvents(singleEv, "4 5")));
	}

}
