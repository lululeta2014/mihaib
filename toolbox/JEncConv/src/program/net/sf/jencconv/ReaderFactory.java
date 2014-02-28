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

import java.io.Reader;

/**
 * Plugin service for JEncConv.
 */
public interface ReaderFactory {

	/**
	 * Return a new Reader which acts as a filter for the given Reader.
	 * 
	 * @param r
	 *            the Reader to be filtered
	 * @return a newly created Reader which filters <code>r</code>
	 */
	public Reader getFilter(Reader r);

	/**
	 * Return the name of this ReaderFactory for displaying in the plugin list
	 * 
	 * @return the transformation name as displayed in the plugin list
	 */
	@Override
	public String toString();

	/**
	 * Return a description of this plugin.
	 * 
	 * @return this plugin's description
	 */
	public String getDescription();

}
