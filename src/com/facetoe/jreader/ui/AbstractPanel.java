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
package com.facetoe.jreader.ui;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 30/10/13
 * Time: 11:56 AM
 */

import javax.swing.*;
import java.util.ArrayList;



interface AutoCompleteable {
    /**
     * This is implemented in the subclasses to define the action to take on auto complete.
     * @param key
     */
    void handleAutoComplete(String key);

    /**
     * Returns the autocomplete words for this panel.
     * @return ArrayList of autocomplete words.
     */
    ArrayList<String> getAutoCompleteWords();
}

interface Navigatable {
    void next();
    void back();
    void home();
}

public abstract class AbstractPanel extends JPanel {


}