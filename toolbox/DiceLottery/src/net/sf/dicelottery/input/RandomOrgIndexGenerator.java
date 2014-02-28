/*
 * Copyright © Mihai Borobocea 2009, 2010, 2012
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

package net.sf.dicelottery.input;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

public class RandomOrgIndexGenerator extends IndexGenerator {

	private final URL url;
	private final static BigInteger maxItemCount = new BigInteger("1000000001");

	public RandomOrgIndexGenerator(BigInteger itemCount)
			throws IllegalArgumentException, MalformedURLException {
		if (itemCount.compareTo(maxItemCount) > 0)
			throw new IllegalArgumentException(
					"Max item count for Random.org is " + maxItemCount);
		String str = "http://www.random.org/integers/?num=1&min=0&max=";
		str += itemCount.subtract(BigInteger.ONE);
		str += "&col=1&base=10&format=plain&rnd=new";
		url = new URL(str);
	}

	@Override
	public BigInteger generateIndex() throws NumberFormatException,
			EOFException, IOException {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()))) {
			return readBigInt(in);
		}
	}

}
