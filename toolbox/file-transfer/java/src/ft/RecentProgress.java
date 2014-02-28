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

/**
 * Stores between 2 and <code>capacity</code> time stamps and transferred
 * amounts. Discards ones <code>age</code> or more milliseconds old.
 * 
 * Newly created objects know a single time stamp.
 */
public class RecentProgress {

	private final int capacity;
	private final long age;
	private final long[] times, amounts;
	private int first, len;

	RecentProgress(int capacity, long age, long timestamp, long transferred) {
		this.capacity = capacity;
		this.age = age;

		times = new long[capacity];
		amounts = new long[capacity];
		times[0] = timestamp;
		amounts[0] = transferred;

		first = 0;
		len = 1;
	}

	long getFirstTime() {
		return times[first];
	}

	long getFirstAmount() {
		return amounts[first];
	}

	long getLastTime() {
		return times[(first + len - 1) % capacity];
	}

	long getLastAmount() {
		return amounts[(first + len - 1) % capacity];
	}

	void update(long timestamp, long transferred) {
		if (len == capacity) {
			first = (first + 1) % capacity;
			len--;
		}

		while (len > 1 && times[first] <= timestamp - age) {
			first = (first + 1) % capacity;
			len--;
		}

		int last = (first + len) % capacity;
		times[last] = timestamp;
		amounts[last] = transferred;
		len++;
	}

}
