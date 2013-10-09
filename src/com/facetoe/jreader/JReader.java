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
import java.nio.file.Paths;
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

    private JavaClassData allClassData;

    private HashMap<String, String> classes;
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
                if ( currentTab instanceof JReaderPanel ) {
                    loadClass(searchBar.getText());
                } else {
                    JSourcePanel sourcePanel = ( JSourcePanel ) currentTab;
                    sourcePanel.findString(searchBar.getText(), new SearchContext());
                }
            }
        });

        btnSource.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( currentTab instanceof JReaderPanel ) {
                    JReaderPanel panel = ( JReaderPanel ) currentTab;
                    String path = Utilities.docPathToSourcePath(panel.getCurrentPage());
                    newSourceTab(path, null);
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
                if ( currentTab instanceof JReaderPanel ) {
                    loadClass(searchBar.getText());
                } else {
                    JSourcePanel sourcePanel = ( JSourcePanel ) currentTab;
                    //TODO Figure out a good way to deal with the search context. Maybe have preferences or something.
                    sourcePanel.findString(searchBar.getText(), new SearchContext());

                }
            }
        });

        newJReaderTab("JReader", false);

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()) instanceof JSourcePanel ) {
                    disableButtons();
                    searchBar.removeWordsFromTrie(new ArrayList<String>(classes.keySet()));
                } else {
                    enableButtons();
                    searchBar.addWordsToTrie(new ArrayList<String>(classes.keySet()));
                }
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

    private void loadClass(String className) {
        String path = classes.get(className);
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

    private void newSourceTab(String filePath, String methodToFind) {
        String title = Utilities.urlToFileName(filePath);
        String method = null;

        /**
         * Some paths look like: src-jdk/javax/imageio/ImageReader.java#readAll(int, javax.imageio.ImageReadParam)
         * Extract the method so we can scroll to it when the tab opens, and extract the actual path to avoid an error.
         */
        if ( filePath.contains("#") ) {
            method = Utilities.extractMethodNameFromPath(filePath);

            /* Get the actual path */
            String[] parts = filePath.split("#");
            filePath = parts[0];
        }

        //TODO write a method to look for common non-classes. Eg, index.html, package-summary.html
        if ( !new File(filePath).exists() ) {
            JOptionPane.showMessageDialog(this, "Unable to locate file: " + filePath, "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JSourcePanel newTab = new JSourcePanel(filePath);

        addCloseButtonToTab(newTab, title);
        tabbedPane.setSelectedComponent(newTab);

        SearchContext context = new SearchContext();
        context.setMatchCase(true);

        if ( methodToFind != null ) {
            newTab.findString(methodToFind, context);
        } else if ( method != null ) {
            newTab.findString(method, context);
        } else {
            String objName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
            String className = "class " + objName;
            String interfaceName = "interface " + objName;

            if ( newTab.findString(className, context) ) {

            } else if ( newTab.findString(interfaceName, context) ) {

            } else {
                newTab.findString(objName, context);
            }
        }

        disableButtons();
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
                                            newSourceTab(newVal.replaceAll("file:\\/\\/", ""), null);
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

    private void loadJavaDocData() {
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

    private void initAutocompleteTextField() {
        classes = allClassData.getClasses();
        ArrayList<String> test = new ArrayList<String>(classes.keySet());
        searchBar.addWordsToTrie(test);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        } catch ( InstantiationException e ) {
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();
        } catch ( UnsupportedLookAndFeelException e ) {
            e.printStackTrace();
        }

        if ( !JReaderSetup.isSetup() )
            JReaderSetup.setup();

        System.out.println(System.getProperty("user.home"));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JReader();
            }
        });
    }
}







