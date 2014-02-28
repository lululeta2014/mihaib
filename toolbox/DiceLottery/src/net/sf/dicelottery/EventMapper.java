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

package net.sf.dicelottery;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.concurrent.CancellationException;

import net.sf.dicelottery.event.Event;
import net.sf.dicelottery.event.EventRepresConverter;
import net.sf.dicelottery.input.IndexGenerator;
import net.sf.dicelottery.input.InputSourceType;
import net.sf.dicelottery.worker.Worker;

/**
 * Maps {@link Event}s from a source {@link EventRepresConverter} to a
 * destination <code>EventRepresConverter</code> using {@link #srcRepeats}
 * source <code>Event</code>s for each {@link Mapping}.
 */
public class EventMapper {

	public final EventRepresConverter srcReprConv, destReprConv;

	/**
	 * The number of {@link Event}s from {@link #srcReprConv} required for each
	 * mapping operation
	 */
	public final int srcRepeats;

	/** (source event count)<sup>srcRepeats</sup> */
	public final BigInteger totalAggregSrcEvents;

	/** {@link #totalAggregSrcEvents} / (destination event count) */
	public final BigInteger Q;

	/** {@link #totalAggregSrcEvents} % (destination event count) */
	public final BigInteger R;

	/**
	 * The number of inputs for this EventMapper which can be mapped to a
	 * destination event. Equal to {@link #totalAggregSrcEvents} - {@link #R}
	 * and {@link #Q} * (destination event count)
	 */
	public final BigInteger validAggregatedSrcEvents; // == Q*Nd == Ns^K - R

	private final static BigInteger tenThousand = BigInteger.valueOf(10000);
	private final String percentValidStr, percentInvalidStr;

	private final static String startStr = "[", concatStr = ", ", endStr = "]";

	/**
	 * Construct a new EventMapper between the specified
	 * {@link EventRepresConverter}s using the specified number of source
	 * {@link Event}s for each mapping operation.
	 * 
	 * @param srcReprConv
	 *            the source EventRepresConverter for the new EventMapper
	 * @param destReprConv
	 *            the destination EventRepresConverter for the new EventMapper
	 * @param srcRepeats
	 *            the number of source Events used for each mapping operation
	 * @throws IllegalArgumentException
	 *             if <code>srcRepeats</code> is <code>&lt; 1</code> or if it is
	 *             too small: srcReprConv.getEventCount() ^ srcRepeats
	 *             <code>&lt;</code> destReprConv.getEventCount()
	 */
	public EventMapper(EventRepresConverter srcReprConv,
			EventRepresConverter destReprConv, int srcRepeats)
			throws IllegalArgumentException {
		if (srcRepeats < 1) {
			// TODO: make this a msg String defined elsewhere
			throw new IllegalArgumentException(
					"Invalid repeat count supplied: " + srcRepeats
							+ ". Must be at least 1.");
		}

		BigInteger totalAggregSrcEvents = srcReprConv.getEventCount().pow(
				srcRepeats);

		if (totalAggregSrcEvents.compareTo(destReprConv.getEventCount()) < 0) {
			// TODO: make this a msg String defined elsewhere
			throw new IllegalArgumentException("Repeat count too small. "
					+ "Try again.");
		}

		BigInteger[] QR = totalAggregSrcEvents.divideAndRemainder(destReprConv
				.getEventCount());

		this.srcReprConv = srcReprConv;
		this.destReprConv = destReprConv;
		this.srcRepeats = srcRepeats;
		this.totalAggregSrcEvents = totalAggregSrcEvents;
		this.Q = QR[0];
		this.R = QR[1];
		this.validAggregatedSrcEvents = totalAggregSrcEvents.subtract(R);

		int intValid = validAggregatedSrcEvents.multiply(tenThousand)
				.divide(totalAggregSrcEvents).intValue();
		int intInvalid = R.multiply(tenThousand).divide(totalAggregSrcEvents)
				.intValue();
		percentValidStr = getPercentString(intValid);
		percentInvalidStr = getPercentString(intInvalid);
	}

	/**
	 * Concatenate the user representations of the specified source
	 * {@link Event}s.
	 * 
	 * @param srcEvents
	 *            the Events whose user representations will be aggregated
	 * @return the concatenation of the user representations
	 * @throws IllegalArgumentException
	 *             if <code>srcEvents.length != {@link #srcRepeats}</code>
	 */
	public String getAggregatedUserRepres(Event[] srcEvents)
			throws IllegalArgumentException {
		if (srcEvents.length != srcRepeats)
			throw new IllegalArgumentException(
					"Aggregate called with invalid Event count: "
							+ srcEvents.length + " instead of " + srcRepeats);

		if (srcEvents.length == 1)
			return srcEvents[0].userRepres;

		StringBuilder sb = new StringBuilder(startStr);
		sb.append(srcEvents[0].userRepres);

		for (int i = 1; i < srcRepeats; i++) {
			sb.append(concatStr);
			sb.append(srcEvents[i].userRepres);
		}

		sb.append(endStr);
		return sb.toString();
	}

	/**
	 * Aggregate the specified source {@link Event}s and return the resulting
	 * index, between 0 and {@link #totalAggregSrcEvents} - 1.
	 * 
	 * @param srcEvents
	 *            the Events whose indexes will be aggregated
	 * @return the aggregated index
	 * @throws IllegalArgumentException
	 *             if <code>srcEvents.length != {@link #srcRepeats}</code>
	 */
	public BigInteger getAggregatedEventIndex(Event[] srcEvents)
			throws IllegalArgumentException {
		if (srcEvents.length != srcRepeats)
			throw new IllegalArgumentException(
					"Internal err: aggregate called with invalid Event count: "
							+ srcEvents.length + " instead of " + srcRepeats);

		BigInteger aggregIndex = srcEvents[0].index;

		for (int i = 1; i < srcRepeats; i++) {
			aggregIndex = aggregIndex.multiply(srcReprConv.getEventCount())
					.add(srcEvents[i].index);
		}

		return aggregIndex;
	}

	/**
	 * Aggregate the specified source {@link Event}s. The index and user
	 * representation of the returned Event are computed by calling
	 * {@link #getAggregatedEventIndex(Event[])} and
	 * {@link #getAggregatedUserRepres(Event[])} respectively.
	 * 
	 * @param srcEvents
	 *            the Events to aggregate
	 * @return the aggregated Event
	 * @throws IllegalArgumentException
	 *             if <code>srcEvents.length != {@link #srcRepeats}</code>
	 */
	public Event aggregateSrcEvents(Event[] srcEvents)
			throws IllegalArgumentException {
		return new Event(getAggregatedEventIndex(srcEvents),
				getAggregatedUserRepres(srcEvents));
	}

	/**
	 * Computes the source {@link Event}s which, when aggregated, produce the
	 * specified index. The returned array has a length of {@link #srcRepeats}.
	 * 
	 * @param aggregatedSrcEventIndex
	 *            the zero-based aggregated index
	 * @return an array of {@link #srcRepeats} computed source Events
	 * @throws IllegalArgumentException
	 *             if the specified parameter is >=
	 *             {@link #validAggregatedSrcEvents}
	 */
	public Event[] deAggregateSrcEvents(BigInteger aggregatedSrcEventIndex)
			throws IllegalArgumentException {
		if (aggregatedSrcEventIndex.compareTo(validAggregatedSrcEvents) >= 0) {
			throw new IllegalArgumentException("Unable to deaggregate index "
					+ aggregatedSrcEventIndex);
		}

		BigInteger srcEventCount = srcReprConv.getEventCount();
		BigInteger crtQ = aggregatedSrcEventIndex;
		Event[] result = new Event[srcRepeats];

		for (int i = 0; i < srcRepeats; i++) {
			BigInteger[] quotAndRem = crtQ.divideAndRemainder(srcEventCount);
			crtQ = quotAndRem[0];
			result[srcRepeats - 1 - i] = srcReprConv.getEvent(quotAndRem[1]);
		}

		return result;
	}

	/**
	 * Compute a {@link Mapping} using events from the specified
	 * {@link InputSourceType}. If the source events cannot be mapped, an
	 * <code>Exception</code> is thrown. If this method is called from the
	 * <code>doInBackground()</code> method of a {@link Worker}, a reference to
	 * the worker can be passed as an argument. It will be used to provide
	 * interim results (status updates) and to check for cancellation. If the
	 * Worker is canceled during the execution of this method, this method may
	 * check for it and throw a CancellationException.
	 * 
	 * @param inpSrcType
	 *            the input source type for this mapping operation
	 * @param r
	 *            used for reading Events if the input source type is
	 *            {@link InputSourceType#USER_READER} otherwise ignored
	 * @param worker
	 *            ignored if <code>null</code>, otherwise the method
	 *            periodically provides the worker with status updates and
	 *            checks if the worker has been canceled – in which case the
	 *            computation is aborted by throwing a CancellationException)
	 * @return the computed Mapping
	 * @throws IllegalArgumentException
	 * @throws EOFException
	 * @throws IOException
	 * @throws CancellationException
	 */
	public Mapping map(InputSourceType inpSrcType, Reader r,
			Worker<Mapping, String> worker) throws IllegalArgumentException,
			EOFException, IOException, CancellationException {
		// TODO finish documenting
		Event[] srcEvents = new Event[srcRepeats];
		IndexGenerator indexGen = null;
		if (inpSrcType != InputSourceType.USER_READER)
			indexGen = IndexGenerator.getInstance(inpSrcType,
					srcReprConv.getEventCount());

		for (int i = 0; i < srcRepeats; i++) {
			if (worker != null) {
				if (worker.isCancelled())
					throw new CancellationException("Canceled by user");
				worker.publishData("Reading events.. " + i + "/" + srcRepeats);
			}
			if (inpSrcType == InputSourceType.USER_READER)
				srcEvents[i] = srcReprConv.readEvent(r);
			else
				srcEvents[i] = srcReprConv.getEvent(indexGen.generateIndex());
		}

		if (worker != null) {
			if (worker.isCancelled())
				throw new CancellationException("Canceled by user");
			worker.publishData("Aggregating source events");
		}
		Event aggregatedSrcEvent = aggregateSrcEvents(srcEvents);
		BigInteger aggregatedSrcIndex = aggregatedSrcEvent.index;
		if (aggregatedSrcIndex.compareTo(validAggregatedSrcEvents) >= 0) {
			// TODO: make this a msg String defined elsewhere
			throw new IllegalArgumentException(
					"Src event `too big' to map. Try again.");
		}

		// map the event index from Usrc^k to Udest
		BigInteger destIndex = aggregatedSrcIndex.mod(destReprConv
				.getEventCount());
		if (worker != null) {
			if (worker.isCancelled())
				throw new CancellationException("Canceled by user");
			worker.publishData("Converting to destination representation");
		}
		Event destEvent = destReprConv.getEvent(destIndex);

		return new Mapping(aggregatedSrcEvent, destEvent);
	}

	/**
	 * Compute a {@link ReverseMapping} using events from the specified
	 * {@link InputSourceType}. If this method is called from the
	 * <code>doInBackground()</code> method of a {@link Worker}, a reference to
	 * the worker can be passed as an argument. It will be used to provide
	 * interim results (status updates) and to check for cancellation. If the
	 * Worker is canceled during the execution of this method, this method may
	 * check for it and throw a CancellationException.
	 * 
	 * @param inpSrcType
	 *            the input source type for this reverse mapping operation
	 * @param r
	 *            used for reading Events if the input source type is
	 *            {@link InputSourceType#USER_READER} otherwise ignored
	 * @param solutionCount
	 *            the number of solutions to return, between <code>1</code> and
	 *            {@link #Q}
	 * @param worker
	 *            ignored if <code>null</code>, otherwise the method
	 *            periodically provides the worker with status updates and
	 *            checks if the worker has been canceled – in which case the
	 *            computation is aborted by throwing a CancellationException)
	 * @return the computed ReverseMapping
	 * @throws IllegalArgumentException
	 * @throws EOFException
	 * @throws IOException
	 * @throws CancellationException
	 */
	public ReverseMapping reverseMap(final InputSourceType inpSrcType,
			final Reader r, final int solutionCount,
			final Worker<ReverseMapping, String> worker)
			throws IllegalArgumentException, EOFException, IOException,
			CancellationException {
		// TODO finish documenting

		IndexGenerator indexGen = null;
		if (inpSrcType != InputSourceType.USER_READER)
			indexGen = IndexGenerator.getInstance(inpSrcType,
					destReprConv.getEventCount());

		if (worker != null)
			worker.publishData("Reading destination event");

		final Event destEvent;
		if (inpSrcType == InputSourceType.USER_READER)
			destEvent = destReprConv.readEvent(r);
		else
			destEvent = destReprConv.getEvent(indexGen.generateIndex());

		if (worker != null) {
			if (worker.isCancelled())
				throw new CancellationException("Canceled by user");
			worker.publishData("Computing source event(s)");
		}

		return new ReverseMapping(destEvent, solutionCount, this);
	}

	/**
	 * Class used by SwingWorkers to return two values: the error percentage and
	 * the source repeats (for one mapping) for two particular source and
	 * destination {@link EventRepresConverter}s.
	 */
	public static class RepeatsError {
		public final int repeats;
		public final String error;

		private RepeatsError(int repeats, String error) {
			this.repeats = repeats;
			this.error = error;
		}
	}

	/**
	 * Computes the minimum number of source {@link Event}s required for a
	 * mapping. If this method is called from a {@link Worker}'s
	 * <code>doInBackground()</code> method, a reference to the Worker can be
	 * passed as an argument. If the worker is canceled the computation will
	 * stop and a CancellationException will be thrown. If the method is unable
	 * to find the minimum repeats for a mapping, an Exception is thrown.
	 * 
	 * @param srcReprConv
	 *            the source {@link EventRepresConverter}
	 * @param destReprConv
	 *            the destination {@link EventRepresConverter}
	 * @param worker
	 *            ignored if <code>null</code>, otherwise the method
	 *            periodically checks if the worker has been canceled (and the
	 *            computation is aborted with a CancellationException)
	 * @return a {@link RepeatsError} object for the minimum repeat count
	 * @throws IllegalArgumentException
	 *             if <code>srcReprConv</code> or <code>destReprConv</code> are
	 *             null
	 * @throws Exception
	 *             if the minimum repeat count for a valid mapping could not be
	 *             found
	 * @throws CancellationException
	 *             if <code>worker</code> is not <code>null</code> and has been
	 *             canceled during this method's execution
	 */
	public static RepeatsError getMinRepeats(
			final EventRepresConverter srcReprConv,
			final EventRepresConverter destReprConv,
			final Worker<RepeatsError, Void> worker)
			throws IllegalArgumentException, Exception, CancellationException {

		if (srcReprConv == null || destReprConv == null)
			throw new IllegalArgumentException(
					"Null EventRepresConverter passed to getMinRepeats() method");

		BigInteger aggreg = srcReprConv.getEventCount();

		for (int repeats = 1; repeats < Integer.MAX_VALUE
				&& (worker == null || !worker.isCancelled()); repeats++) {
			if (aggreg.compareTo(destReprConv.getEventCount()) >= 0) {
				BigInteger bad = aggreg.remainder(destReprConv.getEventCount());
				int percentInvalid = bad.multiply(tenThousand).divide(aggreg)
						.intValue();
				return new RepeatsError(repeats,
						getPercentString(percentInvalid));
			}
			aggreg = aggreg.multiply(srcReprConv.getEventCount());
		}

		if (worker != null && worker.isCancelled())
			throw new CancellationException("Cancelled by user");
		else
			throw new Exception("Unable to find min repeats");

	}

	public String getDescription() {
		String descr;
		descr = "Mapping " + srcRepeats + " source outcome(s)" + "\n";
		descr += totalAggregSrcEvents + " total inputs" + "\n";
		descr += validAggregatedSrcEvents + " (" + percentValidStr + "%)"
				+ " can be mapped" + "\n";
		descr += R + " (" + percentInvalidStr + "%)" + " can't be mapped"
				+ "\n";
		descr += Q + " different input(s) available for each outcome" + "\n";
		descr += "\n";
		descr += "– Source Universe –" + "\n";
		descr += srcReprConv + "\n";
		descr += "\n";
		descr += "– Destination Universe –" + "\n";
		descr += destReprConv;
		return descr;
	}

	private static String getPercentString(int i) {
		if (i < 0 || i > 10000)
			throw new IllegalArgumentException("Internal: percentArgument " + i
					+ " not in required range [0, 10000]");

		if (i == 0 || i == 10000)
			return Integer.toString(i / 100);

		// Be very careful: int arithmetic or string concatenation?
		String result = (i / 100) + ".";
		i = i % 100;
		result += Integer.toString(i / 10) + Integer.toString(i % 10);
		return result;
	}

}
