package com.facetoe.jreader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class JReader extends JFrame {

    private JLabel lblStatus = new JLabel();
    private JButton btnSearch = new JButton("Search");
    private JButton btnBack = new JButton("Back");
    private JButton btnNext = new JButton("Next");
    private JButton btnHome = new JButton("Home");
    private JButton btnSource = new JButton("View Source");
    private JButton btnCollapse = new JButton("Collapse");

    private HashMap<String, JavaObject> classData;
    private JavaObject currentObject;

    private AutoCompleteTextField searchBar = new AutoCompleteTextField();
    private JProgressBar progressBar = new JProgressBar();
    private final JTabbedPane tabbedPane = new JTabbedPane();

    private JPanel currentTab;

    public JReader() {

        loadJavaDocData();
        initAutocompleteTextField();

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
        rightBar.add(btnSource);
        rightBar.add(btnCollapse);


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
                handleSearch();
            }
        });

        btnSource.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( currentTab instanceof JReaderPanel ) {
                    JReaderPanel panel = ( JReaderPanel ) currentTab;
                    newSourceTab(new PathData(panel.getCurrentPage()));
                }
            }
        });

        btnCollapse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( currentTab instanceof JSourcePanel ) {
                    JSourcePanel sourcePanel = ( JSourcePanel ) currentTab;
                    sourcePanel.collapseAllComments();
                }
            }
        });

        searchBar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });

        newJReaderTab("JReader", false);

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()) instanceof JSourcePanel ) {
                    disableBrowserButtons();
                    searchBar.removeWordsFromTrie(new ArrayList<String>(classData.keySet()));
                } else if ( tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()) instanceof JReaderPanel ) {
                    enableBrowserButtons();
                    if ( currentObject != null ) {
                        searchBar.removeWordsFromTrie(currentObject.getObjectItemsShort());
                    }

                    searchBar.addWordsToTrie(new ArrayList<String>(classData.keySet()));
                }
                System.out.println("Fired");
                currentTab = ( JPanel ) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            }
        });

        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        pack();

        setPreferredSize(new Dimension(1024, 600));
        setSize(1024, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("JReader");
        setVisible(true);
    }

    //TODO maybe set currentObject here
    private void loadClass(String className) {
        String path = classData.get(className).getPath();
        if ( path != null && currentTab instanceof JReaderPanel ) {
            String url = Config.getEntry("docDir") + "api" + File.separator + path;

            JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
            jReaderPanel.loadURL(url);
        }
    }

    private void addCloseButtonToTab(JPanel tab, String title) {
        tabbedPane.add(title, tab);
        int index = tabbedPane.indexOfComponent(tab);
        ButtonTabComponent tabButton = new ButtonTabComponent(title, tabbedPane);
        tabbedPane.setTabComponentAt(index, tabButton);
    }

    private void newSourceTab(PathData pathData) {
        String title = pathData.getFileName();
        String filePath = pathData.getSrcPath();

        JavaObject currentObj = classData.get(pathData.getObjectName());
        if ( currentObj != null ) {
            currentObject = currentObj;
            searchBar.addWordsToTrie(currentObject.getObjectItemsShort());
        }

        if ( !Utilities.isGoodSourcePath(filePath) ) {
            JOptionPane.showMessageDialog(this, "Bad File: " + filePath, "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JSourcePanel newTab = new JSourcePanel(filePath);

        addCloseButtonToTab(newTab, title);
        tabbedPane.setSelectedComponent(newTab);

        SearchContext context = new SearchContext();
        context.setMatchCase(true);
        context.setWholeWord(true);

        String searchTerm = pathData.getSearchTerm();

        if ( searchTerm != null ) {
            String className = "class " + pathData.getSearchTerm();
            String interfaceName = "interface " + pathData.getSearchTerm();
            String enumName = "enum " + pathData.getSearchTerm();

            if ( newTab.findString(className, context) ) {
            } else if ( newTab.findString(interfaceName, context) ) {
            } else if ( newTab.findString(enumName, context) ) {
            } else {
                newTab.findString(pathData.getSearchTerm(), context);
            }

        } else if ( currentObj != null ) {
            newTab.findString(currentObj.getFullObjName(), context);
        }

        disableBrowserButtons();
    }

    private void newJReaderTab(final String title, final boolean hasButton) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JReaderPanel readerPanel = new JReaderPanel(progressBar);

                if ( hasButton ) {
                    addCloseButtonToTab(readerPanel, title);
                } else {
                    tabbedPane.add(title, readerPanel);
                }

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
                                            newSourceTab(new PathData(newVal.replaceAll("file:\\/\\/", "")));
                                        } else if ( currentTab instanceof JReaderPanel ) {
                                            JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                                            PathData pathData = new PathData(newVal);
                                            if ( Utilities.isGoodSourcePath(pathData.getSrcPath()) ) {
                                                enableSourceButton();
                                            } else {
                                                disableSourceButton();
                                            }
                                        }
                                    }
                                });
                            }
                        });

                        readerPanel.getJFXPanel().addMouseListener(new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if ( e.isPopupTrigger() )
                                    doPop(e);
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                                if ( e.isPopupTrigger() )
                                    doPop(e);
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                            }

                            private void doPop(MouseEvent e) {
                                JPopupMenu menu = new JPopupMenu();
                                JMenuItem newTab = new JMenuItem("New Tab");
                                newTab.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        newJReaderTab("JReader", true);
                                    }
                                });

                                menu.add(newTab);
                                menu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        });

                        readerPanel.getJFXPanel().addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyPressed(KeyEvent e) {
                                if ( e.isControlDown() && e.getKeyChar() != 's' && e.getKeyCode() == KeyEvent.VK_S ) {
                                    System.out.println("Select All");
                                }
                            }
                        });
                    }
                });

                tabbedPane.setSelectedComponent(readerPanel);
                disableSourceButton();
            }
        });
    }

    private void disableBrowserButtons() {
        btnBack.setEnabled(false);
        btnNext.setEnabled(false);
        btnHome.setEnabled(false);
    }

    private void enableBrowserButtons() {
        btnBack.setEnabled(true);
        btnNext.setEnabled(true);
        btnHome.setEnabled(true);
    }

    private void disableSourceButton() {
        btnSource.setEnabled(false);
    }

    private void handleSearch() {
        if ( currentTab instanceof JReaderPanel ) {
            loadClass(searchBar.getText());
            searchBar.setText("");
        } else {
            JSourcePanel sourcePanel = ( JSourcePanel ) currentTab;
            //TODO Figure out a good way to deal with the search context. Maybe have preferences or something.
            String fullMethod = currentObject.getObjectItemLong(searchBar.getText());
            System.out.println(fullMethod);
            if ( fullMethod != null ) {
                sourcePanel.findString(fullMethod, new SearchContext());
            } else {
                sourcePanel.findString(searchBar.getText(), new SearchContext());
            }
        }
    }

    private void enableSourceButton() {
        btnSource.setEnabled(true);
    }

    private void loadJavaDocData() {
        try {
            classData = Utilities.readClassData(new File(Config.getEntry("classDataFile")));
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

    private void initAutocompleteTextField() {
        ArrayList<String> test = new ArrayList<String>(classData.keySet());
        searchBar.addWordsToTrie(test);
    }

    public static void main(String[] args) {

        try {
            for ( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
                if ( "Nimbus".equals(info.getName()) ) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch ( Exception e ) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        if ( !JReaderSetup.isSetup() ) {
            JReaderSetup.setup();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JReader();
            }
        });
    }
}







