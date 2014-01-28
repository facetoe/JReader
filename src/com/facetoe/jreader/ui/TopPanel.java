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

import com.facetoe.jreader.helpers.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 28/10/13
 * Time: 11:36 AM
 */

/**
 * This class creates the panel along the top of the JReader window. It provides methods for manipulating the
 * buttons and search bar.
 */
class TopPanel extends JPanel {
    private JButton btnBack;
    private JButton btnNext;
    private JButton btnHome;
    private JButton btnSearch;
    private JButton btnSource;
    private ImageIcon sourceIcon;
    private ImageIcon treeIcon;
    private static final int ICON_HEIGHT = 20;
    private static final int ICON_WIDTH = 20;
    private final AutoCompleteTextField searchBar = new AutoCompleteTextField();

    private final JReader jReader;

    public TopPanel(JReader jReader) {
        this.jReader = jReader;
        init();
        createTopPanel();
    }


    /**
     * Create the buttons and add icons.
     */
    private void init() {
        btnBack = buildButton("/com/facetoe/jreader/resources/icons/arrow-left.png");
        btnNext = buildButton("/com/facetoe/jreader/resources/icons/arrow-right.png");
        btnHome = buildButton("/com/facetoe/jreader/resources/icons/home.png");
        btnSearch = buildButton("/com/facetoe/jreader/resources/icons/search.png");
        btnSource = new JButton();
        loadIcons();
        setSourceButton(new NewSourceTabAction(this.jReader));
    }

    private JButton buildButton(String reference) {
        return new JButton(Util.readIcon(this.getClass().getResourceAsStream(reference), ICON_WIDTH, ICON_HEIGHT));
    }

    private void loadIcons() {
        sourceIcon = Util.readIcon(this.getClass().getResourceAsStream
                ("/com/facetoe/jreader/resources/icons/javaSource.png"), ICON_WIDTH, ICON_HEIGHT);

        treeIcon = Util.readIcon(this.getClass().getResourceAsStream(
                "/com/facetoe/jreader/resources/icons/tree_diagramm.png"), ICON_WIDTH, ICON_HEIGHT);
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

    public void setSourceButton(Action action) {
            btnSource.setAction(action);
            btnSource.setIcon(sourceIcon);
    }

    public void setToggleTreeButton(Action action) {
        btnSource.setAction(action);
        btnSource.setIcon(treeIcon);
    }

    public void enableBrowserButtons() {
        btnNext.setEnabled(true);
        btnBack.setEnabled(true);
        btnHome.setEnabled(true);
    }

    public void disableBrowserButtons() {
        btnNext.setEnabled(false);
        btnBack.setEnabled(false);
        btnHome.setEnabled(false);
    }

    public void addBtnBackActionListener(ActionListener listener) {
        btnBack.addActionListener(listener);
    }

    public void addBtnNextActionListener(ActionListener listener) {
        btnNext.addActionListener(listener);
    }

    public void addBtnHomeActionListener(ActionListener listener) {
        btnHome.addActionListener(listener);
    }

    public void addBtnSearchActionListener(ActionListener listener) {
        btnSearch.addActionListener(listener);
    }

    public void addSearchBarListener(ActionListener listener) {
        searchBar.addActionListener(listener);
    }

    public void resetSearchBar() {
        searchBar.setText("");
        searchBar.requestFocus();
    }

    public void enableNewSourceButton() {
        btnSource.setEnabled(true);
    }

    public void disableNewSourceButton() {
        btnSource.setEnabled(false);
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
