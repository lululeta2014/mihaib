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

import java.math.BigInteger;

import net.sf.dicelottery.event.Event;

/**
 * A ReverseMapping contains a desired destination {@link Event} and the source
 * Event(s) which generate it. Multiple solutions (groups of source Events) may
 * be possible. The desired number of solutions, <code>solutionCount</code> is
 * given to the constructor.
 */
public class ReverseMapping {

	/**
	 * The desired destination Event
	 */
	public final Event destEvent;

	/**
	 * The aggregated source indexes. The length of this array is
	 * <code>solutionCount</code>.
	 */
	public final Event[] aggregatedSrcEvents;

	/**
	 * Array of <code>solutionCount</code> results for the reverse mapping. Each
	 * result has {@link EventMapper#srcRepeats} source Events. This array's
	 * dimensions are: <code>Event[solutionCount][srcRepeats]</code>.
	 */
	public final Event[][] srcEventSolutions;

	private static final String setStart = "{", setConcat = ", ", setEnd = "}";

	// TODO: add solutionCount field or method (which simply returns
	// aggregatedSrcEvents.length)

	/**
	 * Construct a new ReverseMapping with the specified solution count,
	 * destination Event and EventMapper.
	 * 
	 * @param destEvent
	 *            the desired destination Event
	 * @param solutionCount
	 *            the requested solution count. Must be between 1 and
	 *            {@link EventMapper#Q}.
	 * @param mapper
	 *            the EventMapper to use
	 * @throws IllegalArgumentException
	 *             if solutionCount is &lt; 1 or &gt; {@link EventMapper#Q}
	 */
	public ReverseMapping(Event destEvent, int solutionCount, EventMapper mapper)
			throws IllegalArgumentException {
		// check that 1 <= solutionCount <= Q
		if (solutionCount < 1
				|| BigInteger.valueOf(solutionCount).compareTo(mapper.Q) > 0)
			throw new IllegalArgumentException("Invalid solution count "
					+ solutionCount + ". Must be >= 1 and <= " + mapper.Q);

		this.destEvent = destEvent;
		BigInteger[] aggregIndexes = new BigInteger[solutionCount];
		aggregIndexes[0] = destEvent.index;
		for (int i = 1; i < solutionCount; i++) {
			aggregIndexes[i] = aggregIndexes[i - 1].add(mapper.destReprConv
					.getEventCount());
		}

		aggregatedSrcEvents = new Event[solutionCount];
		srcEventSolutions = new Event[solutionCount][];
		for (int i = 0; i < solutionCount; i++) {
			BigInteger aggregIndex = aggregIndexes[i];
			srcEventSolutions[i] = mapper.deAggregateSrcEvents(aggregIndex);
			aggregatedSrcEvents[i] = new Event(aggregIndex,
					mapper.getAggregatedUserRepres(srcEventSolutions[i]));
		}
	}

	public String getSrcEventsUserRepres() {
		StringBuilder sb = new StringBuilder(setStart);
		sb.append(aggregatedSrcEvents[0].userRepres);
		for (int i = 1; i < aggregatedSrcEvents.length; i++) {
			sb.append(setConcat);
			sb.append(aggregatedSrcEvents[i].userRepres);
		}
		sb.append(setEnd);
		return sb.toString();
	}

	public String getSrcEventsToString() {
		StringBuilder sb = new StringBuilder(setStart);
		sb.append(aggregatedSrcEvents[0].toString());
		for (int i = 1; i < aggregatedSrcEvents.length; i++) {
			sb.append(setConcat);
			sb.append(aggregatedSrcEvents[i].toString());
		}
		sb.append(setEnd);
		return sb.toString();
	}

}
