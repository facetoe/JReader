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
package com.facetoe.jreader.ui;

import com.facetoe.jreader.helpers.JReaderSetup;
import com.facetoe.jreader.helpers.ProfileManager;
import com.facetoe.jreader.helpers.Util;
import com.facetoe.jreader.listeners.TextMatchItemClickedListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * This is the main class of the application. It is responsible for building the UI and
 * responding to user input.
 */
public class JReader implements StatusUpdateListener, ChangeListener<String> {
    private final Logger log = Logger.getLogger(this.getClass());

    private JFrame frame;
    private MenuBar menuBar;
    private TopPanel topPanel;
    private BottomPanel bottomPanel;
    private final ProfileManager profileManager;
    private JTabbedPane tabbedPane;
    private JPanel currentTab;

    public JReader() {
        if (JReaderSetup.needsInstallation()) {
            new InstallationWindow();
        }
        profileManager = ProfileManager.getInstance();
        buildAndShowUI();
    }

    private void buildAndShowUI() {
        constructInterface();
        initActions();
        initListeners();
        createAndShowNewReaderTab();
        initAutoCompleteWords();
        displayUI();
        currentTab = getCurrentTab();
    }

    private void constructInterface() {
        frame = new JFrame("Jreader");
        frame.setPreferredSize(new Dimension(1024, 600));
        //frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        topPanel = new TopPanel(this);
        frame.add(topPanel, BorderLayout.NORTH);
        bottomPanel = new BottomPanel();
        frame.add(bottomPanel, BorderLayout.SOUTH);
        tabbedPane = new JTabbedPane();
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setJMenuBar(new MenuBar(this));
        menuBar = new MenuBar(this);
        frame.pack();
    }

    private void initActions() {
        addAction(new CloseTabAction(tabbedPane, this), "control W");
        addAction(new NewReaderTabAction(this), "control N");
        addAction(new NewSourceTabAction(this), "control S");
        addAction(new QuitAction(this), "control Q");
    }

    private void addAction(Action action, String keystroke) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keystroke);
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStroke);
        frame.getRootPane().getActionMap().put(keyStroke, action);
    }

    public void createAndShowNewReaderTab() {
        final JReaderPanel readerPanel = createReaderPanel();
        readerPanel.addStatusUpdateListener(this);
        readerPanel.addPopupListener(new JReaderPanelPopUpListener(this));
        readerPanel.addChangeListener(this);
        showNewReaderTab(readerPanel);
    }

    private JReaderPanel createReaderPanel() {
        final String title = "Jreader";
        final JReaderPanel readerPanel = new JReaderPanel();

        // If it's the first tab don't add a close button.
        if (tabbedPane.getTabCount() == 0) {
            tabbedPane.add(title, readerPanel);
        } else {
            addCloseButtonToTab(readerPanel, title);
        }
        return readerPanel;
    }

    private void addCloseButtonToTab(JPanel tab, String title) {
        tabbedPane.add(title, tab);
        int index = tabbedPane.indexOfComponent(tab);
        ButtonTabComponent tabButton = new ButtonTabComponent(tabbedPane);
        tabbedPane.setTabComponentAt(index, tabButton);
    }

    private void showNewReaderTab(JReaderPanel readerPanel) {
        tabbedPane.setSelectedComponent(readerPanel);
        disableNewSourceOption();
        readerPanel.home();
    }

    private void initAutoCompleteWords() {
        topPanel.addAutoCompleteWords(profileManager.getClassNames());
    }

    private void initListeners() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveProfiles();
                System.exit(0);
            }
        });
        topPanel.addBtnBackActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = (JReaderPanel) currentTab;
                jReaderPanel.back();
            }
        });
        topPanel.addBtnNextActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = (JReaderPanel) currentTab;
                jReaderPanel.next();
            }
        });
        topPanel.addBtnHomeActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = (JReaderPanel) currentTab;
                jReaderPanel.home();
            }
        });
        topPanel.addBtnSearchActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });
        topPanel.addSearchBarListener(new ActionListener() {
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

    private void displayUI() {
        frame.setVisible(true);
    }

    /**
     * Creates a new source tab and sets it as the currently selected tab.
     */
    public void createAndShowNewSourceTab() {
        JReaderPanel panel = (JReaderPanel) currentTab;
        final String filePath = Util.docPathToSourcePath(panel.getCurrentPath());
        final String title = Util.extractFileName(panel.getCurrentPath());

        if (!Util.isGoodSourcePath(filePath)) {
            Util.showErrorDialog(frame, filePath + " does not exist.", "File Not Found");
            log.error("No such file: " + filePath);
            return;
        }
        final JSourcePanel newTab = createNewSourcePanel(filePath, title);
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                newTab.createDisplay();
                return null;
            }

            @Override
            protected void done() {
                showNewSourceTab(newTab);
                newTab.scrollToEnclosingObject();
            }
        }.execute();
    }

    public void createAndShowNewSourceTab(URL url, String title, final String fragment) {
        final JSourcePanel newSourcePanel = new JSourcePanel(url, this);
        newSourcePanel.addStatusUpdateListener(this);
        addCloseButtonToTab(newSourcePanel, title);
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                newSourcePanel.createDisplay();
                return null;
            }

            @Override
            protected void done() {
                setCurrentTab(newSourcePanel);
                newSourcePanel.find(fragment);
            }
        }.execute();
    }

    private JSourcePanel createNewSourcePanel(String filePath, String title) {
        JSourcePanel newSourcePanel = new JSourcePanel(new File(filePath), this);
        newSourcePanel.addStatusUpdateListener(this);
        addCloseButtonToTab(newSourcePanel, title);
        return newSourcePanel;
    }

    private void showNewSourceTab(JSourcePanel newTab) {
        // These buttons don't make sense in JSourcePanel.
        disableBrowserButtons();
        setCurrentTab(newTab);
    }

    private void setCurrentTab(JPanel newTab) {
        tabbedPane.setSelectedComponent(newTab);
        resetSearchBar();
    }

    public void createAndShowNewGithubSearchPanel(final String searchTerm) {
        GithubSearchPanel gitPanel = new GithubSearchPanel();
        gitPanel.setOnTextMatchItemClickedListener(new TextMatchItemClickedListener(this));
        gitPanel.addStatusUpdateListener(this);
        addCloseButtonToTab(gitPanel, "Github Search: " + searchTerm);
        gitPanel.searchGithub(searchTerm);
        setCurrentTab(gitPanel);
    }

    // Handle changing tabs. Remove and add autocomplete words, enable/disable buttons.
    private void handleTabChange() {
        if (currentTab == null)
            return;

        removeAutoCompleteWords();

        currentTab = getCurrentTab();
        if (currentTab instanceof AutoCompletable) {
            AutoCompletable newTab = (AutoCompletable) currentTab;
            if (newTab instanceof JReaderPanel) {
                handleReaderTabChange(newTab);
            } else {
                handleSourceTabChange(newTab);
            }
            /* Remove whatever text is there becuase it's annoying always having to delete it. */
            topPanel.clearSearchBar();
        }
    }

    public JPanel getCurrentTab() {
        int index = tabbedPane.getSelectedIndex();
        return index != -1 ? (JPanel) tabbedPane.getComponentAt(index): null;
    }

    private void handleSourceTabChange(AutoCompletable tab) {
        disableBrowserButtons();
        setToggleTreeButton((JSourcePanel) tab);
        topPanel.addAutoCompleteWords(tab.getAutoCompleteWords());
    }

    private void handleReaderTabChange(AutoCompletable tab) {
        topPanel.setSourceButton(new NewSourceTabAction(this));
        topPanel.addAutoCompleteWords(tab.getAutoCompleteWords());
        String currentPath = ((JReaderPanel) currentTab).getCurrentPath();
        maybeEnableSourceOption(currentPath);
        enableBrowserButtons();
    }

    // Decide whether or not to show the New Source option.
    private void maybeEnableSourceOption(String newPath) {
        assert newPath != null;
        String srcPath = Util.docPathToSourcePath(newPath);
        if (Util.isGoodSourcePath(srcPath)) {
            enableNewSourceOption();
        } else {
            disableNewSourceOption();
        }
    }

    // Set the toggle tree action.
    private void setToggleTreeButton(JSourcePanel newTab) {
        topPanel.setToggleTreeButton(new ToggleSourceTreeAction(newTab.getTreePane()));
    }

    private void enableBrowserButtons() {
       topPanel.enableBrowserButtons();
    }

    private void disableBrowserButtons() {
       topPanel.disableBrowserButtons();
    }

    private void enableNewSourceOption() {
        topPanel.enableNewSourceButton();
        menuBar.enableNewSourceOption();
    }

    private void disableNewSourceOption() {
        topPanel.disableNewSourceButton();
        menuBar.disableNewSourceOption();
    }

    private void handleSearch() {
        if (currentTab instanceof AutoCompletable) {
            AutoCompletable tab = (AutoCompletable) currentTab;
            tab.handleAutoComplete(topPanel.getSearchBarText());
        }
    }

    public void addAutoCompleteWords() {
        topPanel.addAutoCompleteWords(profileManager.getClassNames());
    }

    public void removeAutoCompleteWords() {
        AutoCompletable autoCompleteableTab;
        if (currentTab instanceof AutoCompletable) {
            autoCompleteableTab = (AutoCompletable) currentTab;
            ArrayList<String> prevWords = autoCompleteableTab.getAutoCompleteWords();
            topPanel.removeAutoCompleteWords(prevWords);
        }
    }

    public void resetSearchBar() {
        topPanel.resetSearchBar();
    }

    public void saveProfiles() {
        try {
            log.debug("Saving profiles..");
            profileManager.saveProfiles();
            log.debug("Success!");
            System.exit(0);
        } catch (IOException e) {
            Util.showErrorDialog(frame, e.getMessage(), "Error Saving Profiles");
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void updateStatus(String message) {
        bottomPanel.getLblStatus().setText(message);
    }

    @Override
    public void updateProgressBar(int progress) {
        bottomPanel.getProgressBar().setValue(progress);
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldPath, String newPath) {
        maybeEnableSourceOption(newPath);
        updateReaderLabel(newPath);
        updateReaderTitle(newPath);
    }

    private void updateReaderTitle(String newURL) {
        String tabTitle = Util.extractTitle(newURL);
        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabTitle);
    }

    private void updateReaderLabel(String newURL) {
        String systemPath = Util.browserPathToSystemPath(newURL);
        updateStatus(systemPath);
    }

    public static void main(String[] args) {
        setLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JReader();
            }
        });
    }

    // If we can't use Numbus fall back to system look and feel
    private static void setLookAndFeel() {
        try {
            setNimbusLookAndFeel();
        } catch (Exception e) {
            setSystemLookAndFeel();
        }
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            Logger.getLogger(JReader.class).error(e);
        }
    }

    private static void setNimbusLookAndFeel() throws Exception {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    }
}







