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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

/**
 * Implementation of the common SwingWorker usage in this project. Users of this
 * class must provide a {@link BackgroundTask}, a {@link ResultHandler} and
 * optionally an {@link InterimHandler}.
 * 
 * @param <T>
 *            the result type of the background task
 * @param <V>
 *            the type of the (optional) intermediate results
 */
public class Worker<T, V> extends SwingWorker<T, V> {
	private final BackgroundTask<T, V> backgroundTask;
	private final ResultHandler<T, V> resultHandler;
	private final InterimHandler<T, V> interimHandler;

	/**
	 * Constructs a new Worker with the provided background task and handlers.
	 * 
	 * @param backgroundTask
	 *            the task to run in the background
	 * @param resultHandler
	 *            handler for the returned value or thrown exception
	 * @param interimHandler
	 *            optional handler for intermediate results, ignored if
	 *            <code>null</code>
	 * @throws IllegalArgumentException
	 *             if <code>backgroundTask</code> or <code>resultHandler</code>
	 *             is <code>null</code>
	 */
	public Worker(BackgroundTask<T, V> backgroundTask,
			ResultHandler<T, V> resultHandler,
			InterimHandler<T, V> interimHandler)
			throws IllegalArgumentException {

		if (backgroundTask == null || resultHandler == null)
			throw new IllegalArgumentException(
					"Null BackgroundTask or ResultHandler providied in Worker constructor");

		this.backgroundTask = backgroundTask;
		this.resultHandler = resultHandler;
		this.interimHandler = interimHandler;

	}

	/**
	 * Forwards to the SwingWorker's protected <code>publish</code> method.
	 * 
	 * @param chunks
	 *            intermediate results from the background computation
	 */
	@SafeVarargs
	public final void publishData(V... chunks) {
		publish(chunks);
	}

	@Override
	protected T doInBackground() throws Exception {
		return backgroundTask.compute(this);
	}

	@Override
	protected void done() {
		try {
			T result = get();
			resultHandler.result(this, result);
		} catch (InterruptedException e) {
			// TODO
			e.printStackTrace();
		} catch (ExecutionException e) {
			resultHandler.exception(this, e);
		} catch (CancellationException e) {
			// do nothing, we have been canceled
		}
	}

	@Override
	protected void process(List<V> chunks) {
		if (interimHandler != null)
			interimHandler.process(this, chunks);
	}

}
