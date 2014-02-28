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

@SuppressWarnings("serial")
class BadASCIIException extends Exception {

	final int code;

	public BadASCIIException(int code) {
		super("Not allowed by protocol in text messages: "
				+ (32 <= code && code <= 126 ? "\"" + (char) code + "\" " : "")
				+ "code " + code);
		this.code = code;
	}

}
