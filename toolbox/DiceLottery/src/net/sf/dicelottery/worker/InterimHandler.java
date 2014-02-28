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

package net.sf.dicelottery.worker;

import java.util.List;

/**
 * Interface for receiving interim results from a {@link Worker}'s
 * <code>process()</code> method. This allows the same <code>Worker</code> class
 * to be used for different (but related) applications by providing an
 * <code>InterimHandler</code> for processing the interim results.
 * 
 * @param <T>
 * @param <V>
 */
// TODO remove type parameter T and use Worker<Object, V> ?
public interface InterimHandler<T, V> {

	/**
	 * Invoked by a <code>Worker</code>'s <code>process</code> method for
	 * interim results.
	 * 
	 * @param worker
	 *            the invoking worker
	 * @param chunks
	 *            the interim results
	 */
	void process(Worker<T, V> worker, List<V> chunks);

}
