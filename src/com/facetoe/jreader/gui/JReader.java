/*   
*    Copyright (C) 2013  facetoe - facetoe@ymail.com
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License along
*    with this program; if not, write to the Free Software Foundation, Inc.,
*    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package com.facetoe.jreader.gui;

import com.facetoe.jreader.ProfileManager;
import com.facetoe.jreader.utilities.JReaderSetup;
import com.facetoe.jreader.utilities.Utilities;
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

/**
 * This is the main class of the application. It is responsible for building the UI and
 * responding to user input.
 */
public class JReader extends JFrame {
    private final Logger log = Logger.getLogger(this.getClass());

    /* User interface panels. */
    private final JReaderMenuBar menuBar;
    private final JReaderTopPanel topPanel;
    private final JReaderBottomPanel bottomPanel;

    /* This is necessary to make the Swing thread wait until the javafx content is loaded on startup.
     * If it's not set then the Swing components are displayed before there is any content in them. */
    private CountDownLatch javafxLoadLatch;


    /* Keeps track of which profile we are using and provides access to the settings for that profile. */
    private final ProfileManager profileManager;

    /* Tabbed pane for displaying JReaderPanels and JSourcePanel. */
    private final JTabbedPane tabbedPane = new JTabbedPane();

    /* The currently visible tab. */
    private AbstractPanel currentTab;

    /**
     * Constructor.
     */
    public JReader() {

        if ( !JReaderSetup.isSetup() ) {
            new SetupWindow();
        }

        profileManager = ProfileManager.getInstance();


        /* Setup the UI. */
        setJMenuBar(new JReaderMenuBar(this));
        menuBar = new JReaderMenuBar(this);
        topPanel = new JReaderTopPanel(this);
        add(topPanel, BorderLayout.NORTH);
        bottomPanel = new JReaderBottomPanel(this);
        add(bottomPanel, BorderLayout.SOUTH);
        add(tabbedPane, BorderLayout.CENTER);

        newJReaderTab("JReader", false);

        /* Setup listeners and actions.*/
        initActions();
        initListeners();

        setTitle("JReader");
        setPreferredSize(new Dimension(1024, 600));
        //setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        log.debug("Setting visible");
    }


    /**
     * Set up all the actions.
     */
    private void initActions() {
        Action action = new CloseTabAction(tabbedPane, this);
        String keyStrokeAndKey = "control W";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStrokeAndKey);
        getRootPane().getActionMap().put(keyStrokeAndKey, action);

        action = new NewSourceTabAction(this);
        keyStrokeAndKey = "control S";
        keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStrokeAndKey);
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

    /**
     * Set up all the listeners.
     */
    private void initListeners() {

        /* Make sure we save the profile state when the user clicks the close button */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleQuit();
                System.exit(0);
            }
        });

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

    /**
     * Adds a close button to a tab.
     *
     * @param tab   The tab to add a button to.
     * @param title The title to tab should display.
     */
    private void addCloseButtonToTab(JPanel tab, String title) {
        tabbedPane.add(title, tab);
        int index = tabbedPane.indexOfComponent(tab);
        ButtonTabComponent tabButton = new ButtonTabComponent(tabbedPane);
        tabbedPane.setTabComponentAt(index, tabButton);
    }

    /**
     * Creates a new source tab and sets it as the currently selected tab.
     */
    public void newSourceTab() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JReaderPanel panel = ( JReaderPanel ) currentTab;
                String filePath = Utilities.docPathToSourcePath(panel.getCurrentPage());
                String title = Utilities.extractFileName(filePath);

                if ( !Utilities.isGoodSourcePath(filePath) ) {
                    log.error("Bad file path: " + filePath);
                    return;
                }

                log.debug("newSourceTab called with: " + filePath);

                JSourcePanel newTab = new JSourcePanel(filePath, bottomPanel, topPanel);
                addCloseButtonToTab(newTab, title);

                /* These buttons don't make sense in JSourcePanel. */
                disableBrowserButtons();

                tabbedPane.setSelectedComponent(newTab);
                resetSearchBar();

                /* You need to do this here or the pane won't scroll to the highlighted text. */
                newTab.highlightEnclosingObject();
            }
        });
    }

    /**
     * Create and show a new JReaderPanel.
     *
     * @param title     The initial title for this JReaderPanel.
     * @param hasButton Whether or not it should have a button.
     */
    public void newJReaderTab(final String title, final boolean hasButton) {

        /* This is so we can pass the current instance into the PopUpListener. */
        final JReader thisReaderInstance = this;
        javafxLoadLatch = new CountDownLatch(2);

        /* Wrap everything in an invokeLater so the UI doesn't hang. */
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JReaderPanel readerPanel = new JReaderPanel(bottomPanel.getProgressBar(), javafxLoadLatch);
                try {
                    log.debug("Waiting for countdown latch");

                    /* If this isn't present the panel loads before the UI components have been initialized,
                     * and when JReader starts up it just has a blank panel until it is clicked.  */
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

                /* Becuase we are modifying JavaFX components from Swing we need to do it in a JavaFX thread. */
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        /* This is called when the user clicks a link. */
                        readerPanel.getEngine().locationProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observableValue, final String oldURL, final String newURL) {

                                /* Now we are interacting with Swing components again, so it needs to be done in a Swing thread. */
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {

                                        /* Update the label in the JReaderPanel. */
                                        String systemPath = Utilities.browserPathToSystemPath(newURL);
                                        bottomPanel.getLblStatus().setText(systemPath);

                                        /* Set the title of the tab. */
                                        String tabTitle = Utilities.extractTitle(systemPath);
                                        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabTitle);

                                        /* Determine whether or not we should show the source button. */
                                        handleSourceButton(newURL);
                                    }
                                });
                            }
                        });

                        /* Add the listener for the pop up menu. */
                        readerPanel.getJFXPanel().addMouseListener(new JReaderPopUpListener(thisReaderInstance));
                    }
                });

                tabbedPane.setSelectedComponent(readerPanel);
                disableNewSourceOption();
            }
        });
    }

    /**
     * Handle changing tabs. Remove and add autocomplete words, enable/disable buttons.
     */
    private void handleTabChange() {

        /* If the current tab is null then we are starting up.
         * Add the classs names from the profileManager this time. */
        if ( currentTab == null ) {
            topPanel.addAutoCompleteWords(profileManager.getClassNames());
            currentTab = ( AbstractPanel ) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            return;
        }

        /* We want to remove the previous panels words so they don't pollute the new panels auto complete. */
        ArrayList<String> prevWords = currentTab.getAutoCompleteWords();

        currentTab = ( AbstractPanel ) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
        if ( currentTab instanceof JReaderPanel ) {
            /* Change from Toggle Tree button to Source button */
            topPanel.setSourceButton(JReaderTopPanel.SOURCE_BUTTON, new NewSourceTabAction(this));
            topPanel.removeAutoCompleteWords(prevWords);
            topPanel.addAutoCompleteWords(currentTab.getAutoCompleteWords());

            handleSourceButton((( JReaderPanel ) currentTab).getCurrentPage());
            enableBrowserButtons();

        } else {
            disableBrowserButtons();
            topPanel.removeAutoCompleteWords(prevWords);
            topPanel.addAutoCompleteWords(currentTab.getAutoCompleteWords());
        }

        /* Remove whatever text is there becuase it's annoying always having to delete it. */
        topPanel.setSearchBarText("");
    }

    /**
     * Handle search
     */
    private void handleSearch() {
        currentTab.handleAutoComplete(topPanel.getSearchBarText());
    }

    /**
     * Add the Java class names for autocompletion.
     */
    public void addJavaDocClassNames() {
        topPanel.addAutoCompleteWords(profileManager.getClassNames());
    }

    /**
     * Remove the Java class names for autocompletion.
     */
    public void removeJavaDocClassNames() {
        topPanel.removeAutoCompleteWords(profileManager.getClassNames());
    }

    /**
     * Enable the browser buttons.
     */
    private void enableBrowserButtons() {
        topPanel.getBtnBack().setEnabled(true);
        topPanel.getBtnNext().setEnabled(true);
        topPanel.getBtnHome().setEnabled(true);
    }

    /**
     * Disable the browser buttons.
     */
    private void disableBrowserButtons() {
        topPanel.getBtnBack().setEnabled(false);
        topPanel.getBtnNext().setEnabled(false);
        topPanel.getBtnHome().setEnabled(false);
    }

    /**
     * Decide whether or not to show the New Source option.
     */
    private void handleSourceButton(String newPath) {
        String srcPath = Utilities.docPathToSourcePath(newPath);
        if ( Utilities.isGoodSourcePath(srcPath) ) {
            enableNewSourceOption();
        } else {
            disableNewSourceOption();
        }
    }

    /**
     * Enable the New Source option.
     */
    private void enableNewSourceOption() {
        topPanel.getBtnSource().setEnabled(true);
        menuBar.enableNewSourceOption();
    }

    /**
     * Disable the New Source option.
     */
    private void disableNewSourceOption() {
        topPanel.getBtnSource().setEnabled(false);
        menuBar.disableNewSourceOption();
    }

    /**
     * Clear the search bar.
     */
    public void resetSearchBar() {
        topPanel.setSearchBarText("");
        topPanel.getSearchBar().requestFocus();
    }

    /**
     * Returns the currently visible tab.
     *
     * @return The currently visible tab.
     */
    public JPanel getCurrentTab() {
        return currentTab;
    }

    /**
     * Get the SearchContext associated with the current profile.
     *
     * @return The SearchContext.
     */
    public SearchContext getSearchContext() {
        return profileManager.getSearchContext();
    }

    /**
     * Handle Quit. Saves all the profiles.
     */
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







