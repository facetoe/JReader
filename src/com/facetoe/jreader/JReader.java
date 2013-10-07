package com.facetoe.jreader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class JReader extends JFrame {

    private JLabel lblStatus = new JLabel();
    private JButton btnSearch = new JButton("Search");
    private JButton btnBack = new JButton("Back");
    private JButton btnNext = new JButton("Next");
    private JButton btnHome = new JButton("Home");
    private JButton btnTest = new JButton("Test");

    private JavaClassData allClassData;

    private HashMap<String, String> classes;
    private AutoCompleteTextField searchBar = new AutoCompleteTextField();
    private JProgressBar progressBar = new JProgressBar();
    private final JTabbedPane tabbedPane = new JTabbedPane();

    private JPanel currentTab;
    private int currentTabIndex = 0;

    private ArrayList<JPanel> tabs = new ArrayList<JPanel>();

    public JReader() {


        /* You create 3 panels, left, right and top. The components go into the left and
           right panels with their own layout manager and they both go in the top panel.
         */
        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        JPanel leftBar = new JPanel(new BorderLayout());
        JPanel rightBar = new JPanel(new FlowLayout());

        searchBar.setPreferredSize(new Dimension(500, 15));
        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

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
                JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                jReaderPanel.back();
            }
        });

        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                jReaderPanel.next();
            }
        });

        btnHome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                jReaderPanel.home();
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
                JSourcePanel jSourcePane = ( JSourcePanel ) currentTab;
                jSourcePane.findString("a");
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
                if ( tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()) instanceof JSourcePanel ) {
                    disableButtons();
                } else {
                    enableButtons();
                }
                currentTab = ( JPanel ) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            }
        });

        newJReaderTab();
        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        pack();

        setPreferredSize(new Dimension(1024, 600));
        setSize(1024, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        loadClassData();
        initAutocompleteTextField();
        setTitle("JReader");
        setVisible(true);
    }

    private void loadClassData() {
        try {
            allClassData = Utilities.readClassData(new File(Config.getEntry("classDataFile")));
        } catch ( IOException e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load class data at" + Config.getEntry("classDataFile"),
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load class data at" + Config.getEntry("classDataFile"),
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
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

    private void newSourceTab(String filePath) {
        String[] parts = filePath.split("\\/");
        String title = parts[parts.length - 1];
        JSourcePanel newTab = new JSourcePanel(filePath);
        tabbedPane.add(title, newTab);
        tabbedPane.setSelectedComponent(newTab);
        disableButtons();
    }

    private void newJReaderTab() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JReaderPanel readerPanel = new JReaderPanel(progressBar);
                tabs.add(readerPanel);
                tabbedPane.addTab("JReader", readerPanel);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        readerPanel.getEngine().locationProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observableValue, final String oldVal, final String newVal) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if ( newVal.endsWith(".java") ) {
                                            newSourceTab(newVal.replaceAll("file:\\/\\/", ""));
                                        }
                                    }
                                });
                            }
                        });

                        readerPanel.getJFXPanel().addMouseListener(new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                System.out.println("Clicked");
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                                //To change body of implemented methods use File | Settings | File Templates.
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                //To change body of implemented methods use File | Settings | File Templates.
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                //To change body of implemented methods use File | Settings | File Templates.
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                //To change body of implemented methods use File | Settings | File Templates.
                            }
                        });
                    }
                });
            }
        });
    }

    private void disableButtons() {
        btnBack.setEnabled(false);
        btnNext.setEnabled(false);
        btnHome.setEnabled(false);
    }

    private void enableButtons() {
        btnBack.setEnabled(true);
        btnNext.setEnabled(true);
        btnHome.setEnabled(true);
    }

    private void initAutocompleteTextField() {
        classes = allClassData.getClasses();
        ArrayList<String> test = new ArrayList<String>(classes.keySet());
        searchBar.addWordsToTrie(test);
    }

    public static void main(String[] args) {
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

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JReader();
            }
        });
    }
}
