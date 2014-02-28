/*
 * Copyright © Mihai Borobocea 2010
 * 
 * This file is part of File Transfer.
 * 
 * File Transfer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * File Transfer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with File Transfer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package ft;

import static ft.Util.approxSize;

import java.util.Date;

import javax.management.timer.Timer;

class StatusPrinter {

	private static final long updateInterval = Timer.ONE_SECOND * 3 / 4;

	private final String fileName;
	private final long fileSize;
	private final long startTime;
	private final RecentProgress recentProgress;

	long lastUpdateTime;

	StatusPrinter(String fileName, long fileSize) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.startTime = new Date().getTime();
		recentProgress = new RecentProgress(5, 5 * updateInterval, startTime, 0);
		update(0);
	}

	void update(long totalTransferred) {
		long now = new Date().getTime();

		// update incomplete transfers at a certain interval
		if (totalTransferred < fileSize) {
			if (now - lastUpdateTime < updateInterval)
				return;
		}

		recentProgress.update(now, totalTransferred);

		long duration, amount;
		if (totalTransferred < fileSize) {
			duration = now - recentProgress.getFirstTime();
			amount = totalTransferred - recentProgress.getFirstAmount();
		} else {
			duration = now - startTime;
			amount = fileSize;
		}

		long speed = 0;
		if (duration > 0)
			speed = amount / duration;

		String speedStr = "", eta = "";
		if (speed > 0) {
			speedStr = approxSize(speed * 1000) + "/s";

			if (totalTransferred < fileSize) {
				eta = eta((fileSize - totalTransferred) / speed);
			} else {
				// for completed files, print total transfer time
				eta = eta(now - startTime);
				if (eta.length() >= 4)
					eta = eta.substring(0, eta.length() - 4);
			}
		}

		long percentDone;
		if (fileSize > 0)
			percentDone = totalTransferred * 100 / fileSize;
		else
			percentDone = 100;

		System.out.print(String.format("\r%-41.41s %3d%% %9.9s %11.11s "
				+ "%10.10s", fileName, percentDone,
				approxSize(totalTransferred), speedStr, eta));

		lastUpdateTime = now;
	}

	private static String eta(long eta) {
		if (eta <= 0)
			return "";

		char[] name = { 's', 'm', 'h', 'd', 'w' };
		long[] mul = { Timer.ONE_SECOND, Timer.ONE_MINUTE, Timer.ONE_HOUR,
				Timer.ONE_DAY, Timer.ONE_WEEK };

		int i = 1;
		long v1 = 0, v2 = 0;
		while (i < name.length - 1) {
			v1 = eta / mul[i];
			v2 = (eta - v1 * mul[i]) / mul[i - 1];

			if (v1 < mul[i + 1] / mul[i])
				break;
			i++;
		}

		// stop displaying at some maximum (eg 20 weeks)
		if (i + 1 == name.length && v1 > 20)
			return "";

		if (v1 == 0) {
			if (v2 == 0)
				return "";
			return String.format("%2d%s ETA", v2, name[i - 1]);
		}

		return String.format("%2d%s%2d%s ETA", v1, name[i], v2, name[i - 1]);
	}

}
