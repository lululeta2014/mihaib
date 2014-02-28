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

package net.sf.dicelottery;

import net.sf.dicelottery.event.Event;
import net.sf.dicelottery.event.EventRepresConverter;

/**
 * Objects of this class represent a mapping between (one or more) source
 * {@link Event}s and a destination Event.
 */
public class Mapping {

	/**
	 * Source <code>Event</code> for the mapping, computed by aggregating
	 * {@link EventMapper#srcRepeats} <code>Event</code>s from the source
	 * {@link EventRepresConverter}.
	 */
	public final Event aggregatedSrcEvent;
	/**
	 * Destination Event for the mapping.
	 */
	public final Event destEvent;

	// TODO: should it also contain the k source events?

	// TODO: give the k src events to the constructor and build the aggregated
	// src event here (not in the EventMapper worker)
	/**
	 * Construct a new Mapping with the specified parameters
	 * 
	 * @param aggregatedSrcEvent
	 *            the aggregated source Event for this Mapping
	 * @param destEvent
	 *            the destination Event of this Mapping
	 */
	public Mapping(Event aggregatedSrcEvent, Event destEvent) {
		this.aggregatedSrcEvent = aggregatedSrcEvent;
		this.destEvent = destEvent;
	}

}
