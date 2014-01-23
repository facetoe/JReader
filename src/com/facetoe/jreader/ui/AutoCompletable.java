package com.facetoe.jreader.ui;

import java.util.ArrayList;

/**
 * JReader
 * Created by facetoe on 23/01/14.
 */
interface AutoCompletable {
    /**
     * Handle auto-completion.
     * @param key
     */
    void handleAutoComplete(String key);

    /**
     * Returns the autocomplete words for this panel.
     * @return ArrayList of autocomplete words.
     */
    ArrayList<String> getAutoCompleteWords();
}