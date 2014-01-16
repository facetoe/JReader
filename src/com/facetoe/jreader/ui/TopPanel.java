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

import com.facetoe.jreader.helpers.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 28/10/13
 * Time: 11:36 AM
 */
class TopPanel extends JPanel {

    public static final int SOURCE_BUTTON = 0;
    public static final int TREE_BUTTON = 1;

    private JButton btnBack;
    private JButton btnNext;
    private JButton btnHome;
    private JButton btnSearch;
    private JButton btnSource;
    private final AutoCompleteTextField searchBar = new AutoCompleteTextField();

    private final JReader jReader;

    public TopPanel(JReader jReader) {
        this.jReader = jReader;
        initButtons();
        createTopPanel();
    }


    /**
     * Create the buttons and add icons.
     */
    private void initButtons() {
        btnBack = new JButton(Utilities.readIcon(this.getClass().getResourceAsStream
                ("/com/facetoe/jreader/resources/icons/arrow-left.png"), 20, 20));

        btnNext = new JButton(Utilities.readIcon(this.getClass().getResourceAsStream
                ("/com/facetoe/jreader/resources/icons/arrow-right.png"), 20, 20));

        btnHome = new JButton(Utilities.readIcon(this.getClass().getResourceAsStream
                ("/com/facetoe/jreader/resources/icons/home.png"), 20, 20));

        btnSearch = new JButton(Utilities.readIcon(this.getClass().getResourceAsStream
                ("/com/facetoe/jreader/resources/icons/search.png"), 20, 20));

        btnSource = new JButton();
        setSourceButton(SOURCE_BUTTON, new NewSourceTabAction(this.jReader));
    }

    /**
     * Put the whole panel together.
     */
    private void createTopPanel() {
        setLayout(new BorderLayout(5, 0));
        searchBar.setPreferredSize(new Dimension(500, 30));

        JPanel leftBar = createLeftBar();
        JPanel rightBar = createRightBar();

        add(leftBar, BorderLayout.WEST);
        add(rightBar, BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
    }

    private JPanel createLeftBar() {
        JPanel leftBar = new JPanel(new FlowLayout());
        leftBar.add(searchBar);
        leftBar.add(btnSearch);
        return leftBar;
    }

    private JPanel createRightBar() {
        JPanel rightBar = new JPanel(new FlowLayout());
        rightBar.add(btnBack);
        rightBar.add(btnNext);
        rightBar.add(btnHome);
        rightBar.add(btnSource);
        return rightBar;
    }

    public void setSourceButton(int type, Action action) {
        if (type == SOURCE_BUTTON) {
            btnSource.setAction(action);
            btnSource.setIcon(Utilities.readIcon(this.getClass().getResourceAsStream
                    ("/com/facetoe/jreader/resources/icons/javaSource.png"), 20, 20));
        } else if (type == TREE_BUTTON) {
            btnSource.setAction(action);
            btnSource.setIcon(Utilities.readIcon(this.getClass().getResourceAsStream
                    ("/com/facetoe/jreader/resources/icons/tree_diagramm.png"), 20, 20));
        } else {
            throw new IllegalArgumentException("No such type: " + type);
        }
    }

    public JButton getBtnBack() {
        return btnBack;
    }

    public JButton getBtnNext() {
        return btnNext;
    }

    public JButton getBtnHome() {
        return btnHome;
    }

    public JButton getBtnSearch() {
        return btnSearch;
    }

    public JButton getBtnSource() {
        return btnSource;
    }

    public AutoCompleteTextField getSearchBar() {
        return searchBar;
    }

    public void addAutoCompleteWords(ArrayList<String> words) {
        searchBar.addWordsToTrie(words);
    }

    public void removeAutoCompleteWords(ArrayList<String> words) {
        searchBar.removeWordsFromTrie(words);
    }

    public void clearSearchBar() {
        searchBar.setText("");
    }

    public String getSearchBarText() {
        return searchBar.getText();
    }
}
