package com.facetoe.jreader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;




public class JReader extends JFrame {
    private final Logger log = Logger.getLogger(this.getClass());

    /* User interface panels. */
    private JReaderMenuBar menuBar;
    private JReaderTopPanel topPanel;
    private JReaderBottomPanel bottomPanel;

    /* This is necessary to make the Swing thread wait until the javafx content is loaded on startup.
     * If it's not set then the Swing components are displayed before there is any content in them. */
    private final CountDownLatch javafxLoadLatch = new CountDownLatch(1);


    /* Keeps track of which profile we are using and provides access to the settings for that profile. */
    private final ProfileManager profileManager;

    /* Tabbed pane for displaying JReaderPanels and JSourcePanel. */
    private final JTabbedPane tabbedPane = new JTabbedPane();

    /* The currently visible tab. */
    private AbstractPanel currentTab;

    public JReader() {

        if(!JReaderSetup.isSetup()) {
            new SetupWindow();
        }

        profileManager = ProfileManager.getInstance();
        newJReaderTab("JReader", false);

        setJMenuBar(new JReaderMenuBar(this));
        topPanel = new JReaderTopPanel(this);
        add(topPanel, BorderLayout.NORTH);
        bottomPanel = new JReaderBottomPanel(this);
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
        menuBar = new JReaderMenuBar(this);
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
                handleTabChange();
            }
        });
    }

    private void addCloseButtonToTab(JPanel tab, String title) {
        tabbedPane.add(title, tab);
        int index = tabbedPane.indexOfComponent(tab);
        ButtonTabComponent tabButton = new ButtonTabComponent(tabbedPane);
        tabbedPane.setTabComponentAt(index, tabButton);
    }

    public void newSourceTab() {
        JReaderPanel panel = ( JReaderPanel ) currentTab;
        String filePath = Utilities.docPathToSourcePath(panel.getCurrentPage());
        String title = Utilities.extractFileName(filePath);

        if ( !Utilities.isGoodSourcePath(filePath) ) {
            log.error("Bad file path: " + filePath);
            return;
        }

        log.debug("newSourceTab called with: " + filePath);

        JSourcePanel newTab = new JSourcePanel(filePath);
        addCloseButtonToTab(newTab, title);
        disableBrowserButtons();
        disableNewSourceOption();

        resetSearchBar();
        tabbedPane.setSelectedComponent(newTab);

        /* You need to do this here or the pane won't scroll to the highlighted text. */
        newTab.highlightEnclosingObject();
    }

    public void newJReaderTab(final String title, final boolean hasButton) {
        final JReader thisReaderInstance = this;

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
                                        System.out.println("Called with: " + newURL);
                                            String systemPath = Utilities.browserPathToSystemPath(newURL);
                                            bottomPanel.getLblStatus().setText(systemPath);

                                            String tabTitle = Utilities.extractTitle(systemPath);
                                            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabTitle);

                                            handleSourceButton(newURL);
                                        }
                                });
                            }
                        });

                        readerPanel.getJFXPanel().addMouseListener(new JReaderPopUpListener(thisReaderInstance));
                    }
                });

                tabbedPane.setSelectedComponent(readerPanel);
                disableNewSourceOption();
            }
        });
    }

    private void handleTabChange() {

        /* If the current tab is null then we are starting up.
         * Add the get the classs names from the profileManager this time. */
        if(currentTab == null) {
            topPanel.getSearchBar().addWordsToTrie(profileManager.getClassNames());
            currentTab = (AbstractPanel) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            return;
        }

        /* We want to remove the previous panels words so they don't pollute the new panels auto complete. */
        ArrayList<String> prevWords = currentTab.getAutoCompleteWords();

        currentTab = (AbstractPanel) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
        if(currentTab instanceof JReaderPanel) {
            handleSourceButton((( JReaderPanel ) currentTab).getCurrentPage());
            enableBrowserButtons();
            topPanel.getSearchBar().removeWordsFromTrie(prevWords);
            topPanel.getSearchBar().addWordsToTrie(currentTab.getAutoCompleteWords());

        } else {
            disableBrowserButtons();
            topPanel.getSearchBar().removeWordsFromTrie(prevWords);
            topPanel.getSearchBar().addWordsToTrie(currentTab.getAutoCompleteWords());
        }
    }

    private void handleSearch() {
        currentTab.handleAutoComplete(topPanel.getSearchBar().getText());
    }

    public void addJavaDocClassNames() {
        topPanel.getSearchBar().addWordsToTrie(profileManager.getClassNames());
    }

    public void removeJavaDocClassNames() {
        topPanel.getSearchBar().removeWordsFromTrie(profileManager.getClassNames());
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

    private void handleSourceButton(String newPath) {
        String srcPath = Utilities.docPathToSourcePath(newPath);
        if ( Utilities.isGoodSourcePath(srcPath) ) {
            enableNewSourceOption();
        } else {
            disableNewSourceOption();
        }
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
        return profileManager.getSearchContext();
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
                log.error(e1.getMessage(), e1);
            } catch ( InstantiationException e1 ) {
                log.error(e1.getMessage(), e1);
            } catch ( IllegalAccessException e1 ) {
                log.error(e1.getMessage(), e1);
            } catch ( UnsupportedLookAndFeelException e1 ) {
                log.error(e1.getMessage(), e1);
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







