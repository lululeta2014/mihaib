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

/**
 * Task called by the {@link Worker}'s <code>doInBackground()</code> method. The
 * task computes a result or throws an Exception and may provide interim results
 * to the calling Worker.
 * 
 * @param <T>
 *            the result type of this <code>BackgroundTask</code>'s
 *            <code>compute</code> method
 * @param <V>
 *            the type used for intermediate results passed to the
 *            <code>Worker</code>'s <code>publish()</code> method
 */
public interface BackgroundTask<T, V> {

	/**
	 * Perform a (long) computation and optionally provide interim results to
	 * the specified <code>Worker</code>.
	 * 
	 * @param worker
	 *            if not <code>null</code> may receive interim results during
	 *            the computation, otherwise ignored
	 * @return the computed value
	 * @throws Exception
	 *             –
	 */
	T compute(Worker<T, V> worker) throws Exception;

}
