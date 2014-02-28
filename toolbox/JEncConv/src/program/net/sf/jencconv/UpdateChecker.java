/*
 * Copyright © Mihai Borobocea 2010
 * 
 * This file is part of JEncConv.
 * 
 * JEncConv is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JEncConv is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JEncConv.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.sf.jencconv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.SwingWorker;

class UpdateChecker extends SwingWorker<String, Void> {

	private final GUI gui;
	final boolean manuallyStarted;

	UpdateChecker(GUI gui, boolean manuallyStarted) {
		this.gui = gui;
		this.manuallyStarted = manuallyStarted;
	}

	@Override
	protected String doInBackground() throws Exception {
		BufferedReader in = null;
		try {
			URL url = new URL("http://jencconv.sourceforge.net/version");
			in = new BufferedReader(new InputStreamReader(url.openStream(),
					"UTF-8"));
			return in.readLine();
		} finally {
			if (in != null)
				in.close();
		}
	}

	@Override
	protected void done() {
		try {
			gui.updateCheckResult(this, get());
		} catch (Exception e) {
			// InterruptedException, ExecutionException or CancellationException
			gui.updateCheckException(this, e);
		}
	}

}
