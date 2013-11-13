/*   
*    Copyright (C) 2013  facetoe - facetoe@ymail.com
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License along
*    with this program; if not, write to the Free Software Foundation, Inc.,
*    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package com.facetoe.jreader.gui;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 30/10/13
 * Time: 11:56 AM
 */

import javax.swing.*;
import java.util.ArrayList;

/**
 * This is the class that both JReaderPanel and JSourcePanel inherit from.
 * Although there is not much here it makes life a lot easier in JReader
 * as it negates the need for endless if (instance of) checks. For example,
 * it allows the currentTab to be a AbstractPanel which is known to have these
 * methods as opposed to having it as a JPanel and constantly checking.
 */
public abstract class AbstractPanel extends JPanel {

    /**
     * This is implemented in the subclasses to define the action to take on auto complete.
     * Basically, JReaderPanel will navigate to the class page and JSourcePanel will do a search.
     *
     * @param key
     */
    public abstract void handleAutoComplete(String key);

    /**
     * Returns the autocomplete words for this panel. This makes it possible to easily add
     * and remove words from the AutoCompleteTextField as the user navigates through the panels
     * so that only that panel's words are present.
     *
     * @return ArrayList of autocomplete words.
     */
    public abstract ArrayList<String> getAutoCompleteWords();
}