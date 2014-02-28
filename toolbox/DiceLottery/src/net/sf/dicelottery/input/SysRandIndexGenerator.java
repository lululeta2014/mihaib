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

package net.sf.dicelottery.input;

import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

public class SysRandIndexGenerator extends IndexGenerator {

	private final int itemCount;
	private final Random rand;
	private static final BigInteger maxItemCount = BigInteger
			.valueOf(Integer.MAX_VALUE);

	public SysRandIndexGenerator(BigInteger itemCount) {
		if (itemCount.compareTo(maxItemCount) > 0)
			throw new IllegalArgumentException(
					"Max item count for System Random Generator is "
							+ maxItemCount);
		this.itemCount = itemCount.intValue();
		rand = new Random();
	}

	@Override
	public BigInteger generateIndex() throws NumberFormatException,
			EOFException, IOException {
		return BigInteger.valueOf(rand.nextInt(itemCount));
	}

}
