package com.facetoe.jreader;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

class JReader extends JFrame implements Runnable {

    private JLabel lblStatus = new JLabel();
    private JButton btnSearch = new JButton("Search");
    private JButton btnBack = new JButton("Back");
    private JButton btnNext = new JButton("Next");
    private JButton btnHome = new JButton("Home");
    private JButton btnTest = new JButton("Test");

    private HashMap<String, String> classes;
    private AutoCompleteTextField searchBar = new AutoCompleteTextField();
    private JProgressBar progressBar = new JProgressBar();
    private JTabbedPane tabbedPane = new JTabbedPane();

    private JPanel currentTab;
    private int currentTabIndex = 0;

    private ArrayList<JPanel> tabs = new ArrayList<JPanel>();

    public JReader() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch ( InstantiationException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch ( UnsupportedLookAndFeelException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if ( !JReaderSetup.isSetup() )
            JReaderSetup.setup();

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);


        /* You create 3 panels, left, right and top. The components go into the left and
           right panels with their own layout manager and they both go in the top panel.
         */
        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        JPanel leftBar = new JPanel(new BorderLayout());
        JPanel rightBar = new JPanel(new FlowLayout());

        searchBar.setPreferredSize(new Dimension(500, 15));

        leftBar.add(searchBar, BorderLayout.WEST);
        leftBar.add(btnSearch, BorderLayout.EAST);
        rightBar.add(btnBack);
        rightBar.add(btnNext);
        rightBar.add(btnHome);
        rightBar.add(btnTest);

        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));


        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                    jReaderPanel.back();
                } catch ( ClassCastException ex ) {
                }
            }
        });

        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                    jReaderPanel.next();
                } catch ( ClassCastException ex ) {
                }
            }
        });

        btnHome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                    jReaderPanel.home();
                } catch ( ClassCastException ex ) {
                }
            }
        });

        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadClass(searchBar.getText());
            }
        });

        btnTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JSourcePane jSourcePane = ( JSourcePane ) currentTab;
                    jSourcePane.findString("a");
                } catch ( ClassCastException ex ) {
                }

            }
        });

        searchBar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadClass(searchBar.getText());
            }
        });

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                currentTab = ( JPanel ) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            }
        });

        JReaderPanel readerPanel = new JReaderPanel(progressBar);
        JSourcePane sourcePane = new JSourcePane("/home/facetoe/tmp/src-jdk/java/io/FileOutputStream.java");

        tabs.add(readerPanel);
        tabs.add(sourcePane);

        tabbedPane.addTab("Test", readerPanel);
        tabbedPane.add(sourcePane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        pack();
    }

    private void loadClass(String className) {
        String path = classes.get(className);
        if ( path != null ) {
            System.out.println(path);
            String url = Config.getEntry("docDir") + "api" + File.separator + path;

            JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
            jReaderPanel.loadURL(url);
        }
    }

    private void initAutocompleteTextField() {
        //Parser p = new Parser(null);
        //TODO Fix this
        //String html = getFileAsString("/com/facetoe/jreader/docs/api/allclasses-noframe.html");
        //classes = Parser.parse(html);

        ArrayList<String> test = new ArrayList<String>(classes.keySet());
        searchBar.addWordsToTrie(test);
    }

    @Override
    public void run() {
        setPreferredSize(new Dimension(1024, 600));
        setSize(1024, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //initAutocompleteTextField();
        setTitle("Jreader");
        setVisible(true);
    }

    public static void main(String[] args) {
        new JReader().run();
    }
}
