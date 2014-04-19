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

import java.util.concurrent.ExecutionException;

/**
 * Interface for receiving the value returned or exception thrown by a
 * {@link Worker}'s <code>doInBackground()</code> method. The calling
 * <code>Worker</code> will invoke at most one of the methods below from its
 * <code>done()</code> method on the Event Dispatch Thread.
 * 
 * @param <T>
 *            –
 * @param <V>
 *            –
 */
public interface ResultHandler<T, V> {
	// TODO remove type parameter V

	/**
	 * Invoked by <code>worker</code> after successfully completing the
	 * background computation.
	 * 
	 * @param worker
	 *            the invoking Worker
	 * @param result
	 *            the computed result
	 */
	void result(Worker<T, V> worker, T result);

	/**
	 * Invoked by <code>worker</code> if an Exception was thrown by the
	 * background computation.
	 * 
	 * @param worker
	 *            the invoking Worker
	 * @param e
	 *            wrapper obtained by <code>worker</code> for the exception
	 */
	void exception(Worker<T, V> worker, ExecutionException e);

}
