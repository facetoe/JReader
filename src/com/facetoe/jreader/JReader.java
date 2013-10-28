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

    /* User interface panels. */
    private final JReaderMenuBar menuBar = new JReaderMenuBar(this);
    private final JReaderTopPanel topPanel = new JReaderTopPanel(this);
    private final JReaderBottomPanel bottomPanel = new JReaderBottomPanel(this);

    /* SearchContext for the JSourcePanel. */
    private final SearchContext searchContext = new SearchContext();

    /* This is necessary to make the Swing thread wait until the javafx content is loaded on startup.
     * If it's not set then the Swing components are displayed before there is any content in them. */
    private final CountDownLatch javafxLoadLatch = new CountDownLatch(1);


    /* Keeps track of which profile we are using and provides access to the settings for that profile. */
    private final ProfileManager profileManager;

    /* Class names for auto-completion in JReaderPanel. */
    private HashMap<String, String> classNames;

    //TODO Figure out how to deal with multiple tabs of JSourcePanel.
    private JavaSourceFile currentSourceFile;

    /* Tabbed pane for displaying JReaderPanels and JSourcePanel. */
    private final JTabbedPane tabbedPane = new JTabbedPane();

    /* The currently visible tab. */
    private JPanel currentTab;

    public JReader() {

        if(!JReaderSetup.isSetup()) {
            new SetupWindow();
        }

        profileManager = ProfileManager.getInstance();
        loadJavaClassNames();
        newJReaderTab("JReader", false);

        setJMenuBar(new JReaderMenuBar(this));
        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

        initActions();
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

    private void initListeners() {
        topPanel.getBtnBack().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                jReaderPanel.back();
            }
        });

        topPanel.getBtnNext().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                jReaderPanel.next();
            }
        });

        topPanel.getBtnHome().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = ( JReaderPanel ) currentTab;
                jReaderPanel.home();
            }
        });

        topPanel.getBtnSearch().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });

        topPanel.getSearchBar().addActionListener(new ActionListener() {
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
                    topPanel.getSearchBar().removeWordsFromTrie(new ArrayList<String>(classNames.keySet()));
                } else if ( tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()) instanceof JReaderPanel ) {
                    enableBrowserButtons();
                    enableNewSourceOption();
                    if ( currentSourceFile != null ) {
                        topPanel.getSearchBar().removeWordsFromTrie(currentSourceFile.getAllDeclarations());
                    }

                    topPanel.getSearchBar().addWordsToTrie(new ArrayList<String>(classNames.keySet()));
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

        } else {
            log.warn("newSourceTab not called from ReaderPanel");
            return;
        }

        if ( !Utilities.isGoodSourcePath(filePath) ) {
            log.error("Bad file path: " + filePath);
            return;
        }

        try {
            currentSourceFile = JavaSourceFileParser.parse(new FileInputStream(filePath));
        } catch ( ParseException e ) {
            log.error(e.getMessage(), e);
        } catch ( IOException e ) {
            log.error(e.getMessage(), e);
        }

        JSourcePanel newTab = new JSourcePanel(filePath);
        addCloseButtonToTab(newTab, title);
        disableBrowserButtons();
        disableNewSourceOption();

        addAutoCompleteDeclarations();
        JavaClassOrInterface obj = currentSourceFile.getEnclosingClass();
        newTab.highlightDeclaration(obj.getBeginLine(), obj.getEndLine(), obj.beginColumn);

        resetSearchBar();
        tabbedPane.setSelectedComponent(newTab);
    }

    public void newJReaderTab(final String title, final boolean hasButton) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JReaderPanel readerPanel = new JReaderPanel(bottomPanel.getProgressBar(), javafxLoadLatch);
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
                                            String systemPath = Utilities.browserPathToSystemPath(newURL);
                                            bottomPanel.getLblStatus().setText(systemPath);

                                            String tabTitle = Utilities.extractTitle(systemPath);
                                            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabTitle);

                                            String srcPath = Utilities.docPathToSourcePath(newURL);
                                            if ( !newURL.startsWith("http")
                                                    && !newURL.startsWith("www.")
                                                    && Utilities.isGoodSourcePath(srcPath) ) {
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

    private void handleSearch() {
        if ( currentTab instanceof JReaderPanel ) {
            String relativePath = classNames.get(topPanel.getSearchBar().getText());
            if ( relativePath != null ) {
                String path = profileManager.getDocDir() + relativePath;
                JReaderPanel panel = (JReaderPanel) currentTab;
                panel.loadURL(path);
            }
        } else {
            JSourcePanel sourcePanel = ( JSourcePanel ) currentTab;
            JavaObject method = currentSourceFile.getItem(topPanel.getSearchBar().getText());
            if ( method != null ) {
                sourcePanel.highlightDeclaration(method.getBeginLine(), method.getEndLine(),
                        method.beginColumn);
            } else {
                sourcePanel.findString(topPanel.getSearchBar().getText(), searchContext);
            }
        }
    }

    public void loadJavaClassNames() {
        try {
            File classDataFile = new File(profileManager.getClassDataFilePath());
            classNames = Utilities.readClassData(classDataFile);
            addJavaDocClassNames();
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

    public void addJavaDocClassNames() {
        topPanel.getSearchBar().addWordsToTrie(new ArrayList<String>(classNames.keySet()));
    }

    public void removeJavaDocClassNames() {
        topPanel.getSearchBar().removeWordsFromTrie(new ArrayList<String>(classNames.keySet()));
    }

    private void addAutoCompleteDeclarations() {
        if ( currentSourceFile != null ) {
            topPanel.getSearchBar().addWordsToTrie(currentSourceFile.getAllDeclarations());
        }
    }

    private void removeAutoCompleteDeclarations() {
        if ( currentSourceFile != null ) {
            topPanel.getSearchBar().removeWordsFromTrie(currentSourceFile.getAllDeclarations());
        }
    }

    private void enableBrowserButtons() {
        topPanel.getBtnBack().setEnabled(true);
        topPanel.getBtnNext().setEnabled(true);
        topPanel.getBtnHome().setEnabled(true);
    }

    private void disableBrowserButtons() {
        topPanel.getBtnBack().setEnabled(false);
        topPanel.getBtnNext().setEnabled(false);
        topPanel.getBtnHome().setEnabled(false);
    }

    private void enableNewSourceOption() {
        topPanel.getBtnSource().setEnabled(true);
        menuBar.enableNewSourceOption();
    }

    private void disableNewSourceOption() {
        topPanel.getBtnSource().setEnabled(false);
        menuBar.disableNewSourceOption();
    }

    public void resetSearchBar() {
        topPanel.getSearchBar().setText("");
        topPanel.getSearchBar().requestFocus();
    }

    public JPanel getCurrentTab() {
        return currentTab;
    }

    public SearchContext getSearchContext() {
        return searchContext;
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







