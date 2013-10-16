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
import java.util.concurrent.CountDownLatch;

class ViewSourceAction extends AbstractAction {
    JReader jReader;

    public ViewSourceAction(JReader jReader) {
        super("View Source");
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jReader.newSourceTab(null);
    }
}

class CloseTabAction extends AbstractAction {
    JTabbedPane tabbedPane;

    public CloseTabAction(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = tabbedPane.getSelectedIndex();
        ButtonTabComponent component = ( ButtonTabComponent ) tabbedPane.getTabComponentAt(index);

        if ( component != null ) {
            component.removeTab();
        }
    }
}

class NewReaderTabAction extends AbstractAction {
    JReader reader;

    public NewReaderTabAction(JReader reader) {
        super("New Reader Tab");
        this.reader = reader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        reader.newJReaderTab("Jreader", true);
    }
}

class QuitAction extends AbstractAction {

    public QuitAction() {
        super("Quit");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
}

public class JReader extends JFrame {

    private JButton btnBack = new JButton("Back");
    private JButton btnNext = new JButton("Next");
    private JButton btnHome = new JButton("Home");
    private JButton btnSource;// = new JButton("View Source");

    private JMenuItem mnuNewSource;
    private SearchContext searchContext = new SearchContext();
    private Config config = Config.getInstance();

    /* This is necessary to make the Swing thread wait until the javafx content is loaded on startup.
     * If it's not set then the Swing components are displayed before there is any content in them. */
    CountDownLatch javafxLoadLatch = new CountDownLatch(1);


    private HashMap<String, JavaObjectOld> classData;
    private JavaObjectOld currentObject;

    private AutoCompleteTextField searchBar = new AutoCompleteTextField();
    private JProgressBar progressBar = new JProgressBar();
    private final JTabbedPane tabbedPane = new JTabbedPane();

    private JPanel currentTab;

    public JReader() {

        loadJavaDocData();
        initAutocompleteTextField();

        JMenuBar menuBar = new JMenuBar();
        JMenu windowMenu = new JMenu("Window");
        mnuNewSource = new JMenuItem();
        mnuNewSource.setAction(new ViewSourceAction(this));
        windowMenu.add(mnuNewSource);
        btnSource = new JButton(new ViewSourceAction(this));

        JMenu fileMenu = new JMenu("File");
        JMenuItem itmQuit = new JMenuItem(new QuitAction());
        fileMenu.add(itmQuit);
        menuBar.add(fileMenu);

        JMenuItem mnuNewTab = new JMenuItem();
        mnuNewTab.setAction(new NewReaderTabAction(this));
        setJMenuBar(menuBar);
        windowMenu.add(mnuNewTab);
        menuBar.add(windowMenu);

        JMenu mnuFind = new JMenu("Find");
        final JCheckBoxMenuItem chkWholeWord = new JCheckBoxMenuItem("Whole Word");
        chkWholeWord.setState(config.getBool("searchWholeWord", false));
        chkWholeWord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchContext.setWholeWord(chkWholeWord.getState());
                config.setBool("searchWholeWord", chkWholeWord.getState());
            }
        });
        mnuFind.add(chkWholeWord);

        final JCheckBoxMenuItem chkMatchCase = new JCheckBoxMenuItem("Match Case");
        chkMatchCase.setState(config.getBool("searchMatchCase", false));
        chkMatchCase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchContext.setMatchCase(chkMatchCase.getState());
                config.setBool("searchMatchCase", chkMatchCase.getState());
            }
        });
        mnuFind.add(chkMatchCase);

        final JCheckBoxMenuItem chkRegexp = new JCheckBoxMenuItem("Regular Expression");
        chkRegexp.setState(config.getBool("searchRegexp", false));
        chkRegexp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchContext.setRegularExpression(chkRegexp.getState());
                config.setBool("searchRegexp", chkRegexp.getState());
            }
        });
        mnuFind.add(chkRegexp);

        menuBar.add(mnuFind);


        Action action = new CloseTabAction(tabbedPane);
        String keyStrokeAndKey = "control C";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, keyStrokeAndKey);
        getRootPane().getActionMap().put(keyStrokeAndKey, action);

        action = new NewReaderTabAction(this);
        keyStrokeAndKey = "control N";
        keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStrokeAndKey);
        getRootPane().getActionMap().put(keyStrokeAndKey, action);

        action = new QuitAction();
        keyStrokeAndKey = "control Q";
        keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStrokeAndKey);
        getRootPane().getActionMap().put(keyStrokeAndKey, action);



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
        JButton btnSearch = new JButton("Search");
        leftBar.add(btnSearch, BorderLayout.EAST);
        rightBar.add(btnBack);
        rightBar.add(btnNext);
        rightBar.add(btnHome);
        rightBar.add(btnSource);
        JButton btnCollapse = new JButton("Collapse");
        rightBar.add(btnCollapse);


        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));


        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        JLabel lblStatus = new JLabel();
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ( e.isControlDown() && e.getKeyChar() != 's' && e.getKeyCode() == KeyEvent.VK_S ) {

                }
            }
        });

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

//        btnSource.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if ( currentTab instanceof JReaderPanel ) {
//                    JReaderPanel panel = ( JReaderPanel ) currentTab;
//                    newSourceTab(null);
//                }
//            }
//        });

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
                    disableNewSourceOption();
                    searchBar.removeWordsFromTrie(new ArrayList<String>(classData.keySet()));
                } else if ( tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()) instanceof JReaderPanel ) {
                    enableBrowserButtons();
                    enableNewSourceOption();
                    if ( currentObject != null ) {
                        searchBar.removeWordsFromTrie(currentObject.getObjectItemsShort());
                    }

                    searchBar.addWordsToTrie(new ArrayList<String>(classData.keySet()));
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

        System.out.println("Setting visible");

        setVisible(true);
    }

    private void loadClass() {
        if ( currentObject != null && currentTab != null && currentTab instanceof JReaderPanel ) {
            String url = config.getString("apiDir") + currentObject.getPath();
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

    public void newSourceTab(String path) {
        String title;
        String filePath;
        if ( currentTab instanceof JReaderPanel ) {
            if ( path != null && path.endsWith(".java") ) {
                filePath = path;
                title = Utilities.extractFileName(filePath);
            } else {
                JReaderPanel panel = ( JReaderPanel ) currentTab;
                path = panel.getCurrentPage();
                filePath = Utilities.docPathToSourcePath(panel.getCurrentPage());
                title = Utilities.extractFileName(filePath);
            }

            System.out.println("Path: " + path);

            if ( !Utilities.isGoodSourcePath(filePath) ) {
                System.err.println("Bad File Path");
                return;
            }

        } else {
            System.err.println("Not a ReaderPanel");
            return;
        }

        if ( currentObject != null ) {
            searchBar.addWordsToTrie(currentObject.getObjectItemsShort());
        } else {
            System.out.println("IT was null");
        }

        if ( !Utilities.isGoodSourcePath(filePath) ) {
            JOptionPane.showMessageDialog(this, "Bad File: " + filePath, "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JSourcePanel newTab = new JSourcePanel(filePath);
        addCloseButtonToTab(newTab, title);
        tabbedPane.setSelectedComponent(newTab);

        if ( currentObject != null ) {
            newTab.findString(currentObject.getFullObjName(), searchContext);
        }

        disableBrowserButtons();
        disableNewSourceOption();
        searchBar.requestFocus();
        searchBar.setText("");
    }

    public void newJReaderTab(final String title, final boolean hasButton) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JReaderPanel readerPanel = new JReaderPanel(progressBar, javafxLoadLatch);
                try {
                    javafxLoadLatch.await();
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }

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
                                            newSourceTab(newVal.replace("file://", ""));
                                        } else if ( currentTab instanceof JReaderPanel ) {
                                            //TODO clean this up.
                                            String tabTitle = Utilities.extractTitle(newVal);
                                            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabTitle);
                                            if ( Utilities.isGoodSourcePath(newVal) ) {
                                                enableNewSourceOption();
                                            } else {
                                                disableNewSourceOption();
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
                                if ( e.isPopupTrigger() )
                                    doPop(e);
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

                        //TODO remove this and put it on the JFrame
                        readerPanel.getJFXPanel().addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyPressed(KeyEvent e) {
                                if ( e.isControlDown() && e.getKeyChar() != 's' && e.getKeyCode() == KeyEvent.VK_S ) {
                                    System.out.println("Select All");
                                    int index = tabbedPane.getSelectedIndex();
                                    ButtonTabComponent component = ( ButtonTabComponent ) tabbedPane.getTabComponentAt(index);

                                    if ( component != null ) {
                                        component.removeTab();
                                    }
                                }
                            }
                        });
                    }
                });

                tabbedPane.setSelectedComponent(readerPanel);
                disableNewSourceOption();
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

    private void disableNewSourceOption() {
        btnSource.setEnabled(false);
        mnuNewSource.setEnabled(false);
    }

    private void enableNewSourceOption() {
        btnSource.setEnabled(true);
        mnuNewSource.setEnabled(true);
    }

    private void handleSearch() {
        if ( currentTab instanceof JReaderPanel ) {
            if ( classData.get(searchBar.getText()) != null ) {
                currentObject = classData.get(searchBar.getText());
                loadClass();
            }

        } else {
            JSourcePanel sourcePanel = ( JSourcePanel ) currentTab;
            String fullMethod = currentObject.getObjectItemLong(searchBar.getText());
            System.out.println("Method: " + fullMethod);
            if ( fullMethod != null ) {
                sourcePanel.findString(fullMethod, searchContext);
            } else {
                sourcePanel.findString(searchBar.getText(), searchContext);
            }
        }
    }

    private void loadJavaDocData() {
        try {
            classData = Utilities.readClassData(new File(config.getString("classDataFile")));
        } catch ( IOException e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load class data at" + config.getString("classDataFile"),
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load class data at" + config.getString("classDataFile"),
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initAutocompleteTextField() {
        ArrayList<String> test = new ArrayList<String>(classData.keySet());
        searchBar.addWordsToTrie(test);
    }

//    public static void main(String[] args) {
//        try {
//            for ( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
//                if ( "Nimbus".equals(info.getName()) ) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch ( Exception e ) {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch ( ClassNotFoundException e1 ) {
//                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            } catch ( InstantiationException e1 ) {
//                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            } catch ( IllegalAccessException e1 ) {
//                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            } catch ( UnsupportedLookAndFeelException e1 ) {
//                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//        }
//
//        if ( !JReaderSetup.isSetup() ) {
//            JReaderSetup.setup();
//        }
//
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                new JReader();
//            }
//        });
//    }
}







