package com.facetoe.jreader.gui;

import com.facetoe.jreader.utilities.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 28/10/13
 * Time: 11:36 AM
 */
public class JReaderTopPanel extends JPanel {

    public static final int SOURCE_BUTTON = 0;
    public static final int TREE_BUTTON = 1;

    private JButton btnBack;
    private JButton btnNext;
    private JButton btnHome;
    private JButton btnSearch;
    private JButton btnSource;
    private final AutoCompleteTextField searchBar = new AutoCompleteTextField();

    private final JReader jReader;

    public JReaderTopPanel(JReader jReader) {
        this.jReader = jReader;
        initButtons();
        initTopPanel();
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
    private void initTopPanel() {
        setLayout(new BorderLayout(5, 0));
        JPanel leftBar = new JPanel(new FlowLayout());
        JPanel rightBar = new JPanel(new FlowLayout());

        searchBar.setPreferredSize(new Dimension(500, 30));

        leftBar.add(searchBar);
        leftBar.add(btnSearch);
        rightBar.add(btnBack);
        rightBar.add(btnNext);
        rightBar.add(btnHome);
        rightBar.add(btnSource);

        add(leftBar, BorderLayout.WEST);
        add(rightBar, BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
    }

    public void setSourceButton(int type, Action action) {
        if ( type == SOURCE_BUTTON ) {
            btnSource.setAction(action);
            btnSource.setIcon(Utilities.readIcon(this.getClass().getResourceAsStream
                    ("/com/facetoe/jreader/resources/icons/javaSource.png"), 20, 20));
        } else if ( type == TREE_BUTTON ) {
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

    public void setSearchBarText(String text) {
        searchBar.setText(text);
    }

    public String getSearchBarText() {
        return searchBar.getText();
    }
}
