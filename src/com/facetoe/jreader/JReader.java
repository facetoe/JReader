package com.facetoe.jreader;

import japa.parser.ParseException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;


public class JReader extends JFrame {
    private final Logger log = Logger.getLogger(this.getClass());

    private final JButton btnBack = new JButton("Back");
    private final JButton btnNext = new JButton("Next");
    private final JButton btnHome = new JButton("Home");
    private final JButton btnSearch = new JButton("Search");
    private final JButton btnSource = new JButton(new ViewSourceAction(this));
    private AutoCompleteTextField searchBar = new AutoCompleteTextField();
    private JProgressBar progressBar = new JProgressBar();

    private final JReaderMenuBar menuBar = new JReaderMenuBar(this);

    private SearchContext searchContext = new SearchContext();

    /* This is necessary to make the Swing thread wait until the javafx content is loaded on startup.
     * If it's not set then the Swing components are displayed before there is any content in them. */
    CountDownLatch javafxLoadLatch = new CountDownLatch(1);


    /* Keeps track of which profile we are using and provides access to the settings for that profile. */
    ProfileManager profileManager;
    private HashMap<String, String> classNames;

    private JavaSourceFile currentSourceFile;
    private final JTabbedPane tabbedPane = new JTabbedPane();

    private JPanel currentTab;

    public JReader() {

        if(!JReaderSetup.isSetup()) {
            new SetupWindow();
        }

        profileManager = ProfileManager.getInstance();
        addJavaDocCLassNames();
        newJReaderTab("JReader", false);

        //initMenus();
        setJMenuBar(new JReaderMenuBar(this));

        initActions();
        initTopPanel();
        initListeners();

        add(tabbedPane, BorderLayout.CENTER);
        setTitle("JReader");
        setPreferredSize(new Dimension(1024, 600));
        setSize(1024, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        /* Make sure we save the profile state when the user clicks the close button */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleQuit();
                System.exit(0);
            }
        });

        log.debug("Setting visible");
        pack();
        setVisible(true);
    }



    private void initActions() {
        Action action = new CloseTabAction(tabbedPane, this);
        String keyStrokeAndKey = "control C";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, keyStrokeAndKey);
        getRootPane().getActionMap().put(keyStrokeAndKey, action);

        action = new NewReaderTabAction(this);
        keyStrokeAndKey = "control N";
        keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStrokeAndKey);
        getRootPane().getActionMap().put(keyStrokeAndKey, action);

        action = new QuitAction(this);
        keyStrokeAndKey = "control Q";
        keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStrokeAndKey);
        getRootPane().getActionMap().put(keyStrokeAndKey, action);
    }

    private void initTopPanel() {
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


        add(topBar, BorderLayout.NORTH);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void initListeners() {
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

        searchBar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });


        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()) instanceof JSourcePanel ) {
                    disableBrowserButtons();
                    disableNewSourceOption();
                    searchBar.removeWordsFromTrie(new ArrayList<String>(classNames.keySet()));
                } else if ( tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()) instanceof JReaderPanel ) {
                    enableBrowserButtons();
                    enableNewSourceOption();
                    if ( currentSourceFile != null ) {
                        searchBar.removeWordsFromTrie(currentSourceFile.getAllDeclarations());
                    }

                    searchBar.addWordsToTrie(new ArrayList<String>(classNames.keySet()));
                }
                currentTab = ( JPanel ) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            }
        });
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

            log.debug("newSourceTab called with: " + path);

            if ( !Utilities.isGoodSourcePath(filePath) ) {
                System.err.println("Bad File Path");
                return;
            }

            try {
                currentSourceFile = JavaSourceFileParser.parse(new FileInputStream(filePath));
            } catch ( ParseException e ) {
                log.error(e.getMessage(), e);
            } catch ( IOException e ) {
                log.error(e.getMessage(), e);
            }

        } else {
            log.warn("newSouceTab not called from ReaderPanel");
            return;
        }

        if ( !Utilities.isGoodSourcePath(filePath) ) {
            log.warn("Bad file path: " + filePath);
            JOptionPane.showMessageDialog(this, "Bad File: " + filePath, "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JSourcePanel newTab = new JSourcePanel(filePath);
        addCloseButtonToTab(newTab, title);
        disableBrowserButtons();
        disableNewSourceOption();

        if ( currentSourceFile != null ) {
            addAutoCompleteWords();
            JavaClassOrInterface obj = currentSourceFile.getEnclosingClass();
            newTab.highlightDeclaration(obj.getBeginLine(), obj.getEndLine(), obj.beginColumn);
        } else {
            log.warn("Source file was null");
        }

        resetSearchBar();
        tabbedPane.setSelectedComponent(newTab);
    }

    public void newJReaderTab(final String title, final boolean hasButton) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JReaderPanel readerPanel = new JReaderPanel(progressBar, javafxLoadLatch);
                try {
                    log.debug("Waiting for countdown latch");
                    javafxLoadLatch.await();
                    log.debug("Latch released");
                } catch ( InterruptedException e ) {
                    log.error(e.getMessage(), e);
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
                            public void changed(ObservableValue<? extends String> observableValue, final String oldURL, final String newURL) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if ( newURL.endsWith(".java") ) {
                                            newSourceTab(newURL.replace("file://", ""));
                                        } else if ( currentTab instanceof JReaderPanel ) {
                                            //TODO clean this up.
                                            String tabTitle = Utilities.extractTitle(newURL);
                                            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabTitle);
                                            if ( !newURL.startsWith("http")
                                                    && !newURL.startsWith("www.")
                                                    && Utilities.isGoodSourcePath(Utilities.docPathToSourcePath(newURL)) ) {
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
                    }
                });

                tabbedPane.setSelectedComponent(readerPanel);
                disableNewSourceOption();
            }
        });
    }

    private void addAutoCompleteWords() {
        if ( currentSourceFile != null ) {
            searchBar.addWordsToTrie(currentSourceFile.getAllDeclarations());
        }
    }

    private void removeAutoCompleteWords() {
        if ( currentSourceFile != null ) {
            searchBar.removeWordsFromTrie(currentSourceFile.getAllDeclarations());
        }
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
        menuBar.disableNewSourceOption();
    }

    private void enableNewSourceOption() {
        btnSource.setEnabled(true);
        menuBar.enableNewSourceOption();
    }

    private void handleSearch() {
        if ( currentTab instanceof JReaderPanel ) {
            String relativePath = classNames.get(searchBar.getText());
            if ( relativePath != null ) {
                String path = profileManager.getDocDir() + relativePath;
                JReaderPanel panel = (JReaderPanel) currentTab;
                panel.loadURL(path);
            }
        } else {
            JSourcePanel sourcePanel = ( JSourcePanel ) currentTab;
            System.out.println(currentTab);
            JavaObject method = currentSourceFile.getItem(searchBar.getText());
            if ( method != null ) {
                sourcePanel.highlightDeclaration(method.getBeginLine(), method.getEndLine(),
                        method.beginColumn);
            } else {
                sourcePanel.findString(searchBar.getText(), searchContext);
            }
        }
    }

    public void addJavaDocCLassNames() {
        try {
            File classDataFile = new File(profileManager.getClassDataFilePath());
            classNames = Utilities.readClassData(classDataFile);
            searchBar.addWordsToTrie(new ArrayList<String>(classNames.keySet()));
        } catch ( IOException e ) {
            log.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Failed to load class data at"
                    + profileManager.getClassDataFilePath(),
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        } catch ( ClassNotFoundException e ) {
            log.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Failed to load class data at"
                    + profileManager.getClassDataFilePath(),
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public JPanel getCurrentTab() {
        return currentTab;
    }

    public SearchContext getSearchContext() {
        return searchContext;
    }

    public void removeJavaDocClassNames() {
        searchBar.removeWordsFromTrie(new ArrayList<String>(classNames.keySet()));
    }

    public void resetSearchBar() {
        searchBar.setText("");
        searchBar.requestFocus();
    }

    public void handleQuit() {
        try {
            log.debug("Saving profiles..");
            profileManager.saveProfiles();
            log.debug("Success!");
            System.exit(0);
        } catch ( IOException e ) {
            log.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        Logger log = Logger.getLogger(JReader.class);

        try {
            for ( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
                if ( "Nimbus".equals(info.getName()) ) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch ( Exception e ) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch ( ClassNotFoundException e1 ) {
                log.error(e1.getMessage(), e1);  //To change body of catch statement use File | Settings | File Templates.
            } catch ( InstantiationException e1 ) {
                log.error(e1.getMessage(), e1);  //To change body of catch statement use File | Settings | File Templates.
            } catch ( IllegalAccessException e1 ) {
                log.error(e1.getMessage(), e1);  //To change body of catch statement use File | Settings | File Templates.
            } catch ( UnsupportedLookAndFeelException e1 ) {
                log.error(e1.getMessage(), e1);  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JReader();
            }
        });
    }
}







