package com.facetoe.jreader;

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
abstract class AbstractPanel extends JPanel {

    /**
     * This is implemented in the subclasses to define the action to take on auto complete.
     * Basically, JReaderPanel will navigate to the class page and JSourcePanel will do a search.
     * @param key
     */
    abstract void handleAutoComplete(String key);

    /**
     * Returns the autocomplete words for this panel. This makes it possible to easily add
     * and remove words from the AutoCompleteTextField as the user navigates through the panels
     * so that only that panel's words are present.
     * @return ArrayList of autocomplete words.
     */
    abstract ArrayList<String> getAutoCompleteWords();
}