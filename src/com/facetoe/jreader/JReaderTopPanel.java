package com.facetoe.jreader;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 28/10/13
 * Time: 11:36 AM
 */
public class JReaderTopPanel extends JPanel {

    private final JButton btnBack;
    private final JButton btnNext;
    private final JButton btnHome;
    private final JButton btnSearch;
    private final JButton btnSource;
    private AutoCompleteTextField searchBar = new AutoCompleteTextField();

    private JReader jReader;

    public JReaderTopPanel(JReader jReader) {
        this.jReader = jReader;
        btnBack = new JButton("Back");
        btnSource = new JButton(new ViewSourceAction(this.jReader));
        btnSearch = new JButton("Search");
        btnHome = new JButton("Home");
        btnNext = new JButton("Next");
        initTopPanel();
    }

    private void initTopPanel() {
                  /* You create 3 panels, left, right and top. The components go into the left and
           right panels with their own layout manager and they both go in the top panel.
         */
        setLayout(new BorderLayout(5, 0));
        JPanel leftBar = new JPanel(new BorderLayout());
        JPanel rightBar = new JPanel(new FlowLayout());

        searchBar.setPreferredSize(new Dimension(500, 15));

        leftBar.add(searchBar, BorderLayout.WEST);
        leftBar.add(btnSearch, BorderLayout.EAST);
        rightBar.add(btnBack);
        rightBar.add(btnNext);
        rightBar.add(btnHome);
        rightBar.add(btnSource);


        add(leftBar, BorderLayout.WEST);
        add(rightBar, BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
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
}
