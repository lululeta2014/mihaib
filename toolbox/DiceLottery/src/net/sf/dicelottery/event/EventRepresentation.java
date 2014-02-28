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

/**
 * Enum constants indicating how {@link Event}s are constructed from
 * {@link net.sf.dicelottery.element.Element}s. An <code>Event</code>
 * can consist of a single <code>Element</code> or of one or more
 * <code>Element</code>s selected in a lottery fashion.
 */
public enum EventRepresentation {
	SINGLE_ELEMENT, LOTTERY
}
