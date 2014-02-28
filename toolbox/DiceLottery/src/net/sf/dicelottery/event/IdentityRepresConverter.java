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

import net.sf.dicelottery.element.Element;
import net.sf.dicelottery.element.ElementRepresConverter;

class IdentityRepresConverter extends EventRepresConverter {

	IdentityRepresConverter(final ElementRepresConverter elementRepresConverter) {
		super(elementRepresConverter);
	}

	@Override
	public Event readEvent(Reader r) throws NumberFormatException,
			IllegalArgumentException, EOFException, IOException {
		Element elem = elementRepresConverter.readElement(r);
		return new Event(elem.index, elem.userRepres);
	}

	@Override
	public Event getEvent(BigInteger eventIndex) throws NumberFormatException {
		Element elem = elementRepresConverter.getElement(eventIndex);
		return new Event(elem.index, elem.userRepres);
	}

	@Override
	public BigInteger getEventCount() {
		return elementRepresConverter.elementCount;
	}

	@Override
	public String getEventDescription() {
		String descr;
		descr = "Selecting a single element out of "
				+ elementRepresConverter.elementCount;
		return descr;
	}

}
