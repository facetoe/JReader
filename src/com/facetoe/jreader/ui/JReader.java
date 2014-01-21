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

import com.facetoe.jreader.helpers.ProfileManager;
import com.facetoe.jreader.helpers.Utilities;
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
import java.util.ArrayList;


/**
 * This is the main class of the application. It is responsible for building the UI and
 * responding to user input.
 */
public class JReader {
    private final Logger log = Logger.getLogger(this.getClass());

    private JFrame frame;
    private MenuBar menuBar;
    private TopPanel topPanel;
    private BottomPanel bottomPanel;
    private final ProfileManager profileManager;
    private JTabbedPane tabbedPane;
    private AbstractPanel currentTab;

    public JReader() {
        if (JReaderSetup.needsSetup()) {
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
    }

    private void constructInterface() {
        frame = new JFrame("Jreader");
        frame.setPreferredSize(new Dimension(1024, 600));
        //frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setJMenuBar(new MenuBar(this));
        menuBar = new MenuBar(this);
        topPanel = new TopPanel(this);
        frame.add(topPanel, BorderLayout.NORTH);
        bottomPanel = new BottomPanel();
        frame.add(bottomPanel, BorderLayout.SOUTH);
        tabbedPane = new JTabbedPane();
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.pack();
    }

    private void initActions() {
        addAction(new CloseTabAction(tabbedPane, this), "control W");
        addAction(new NewReaderTabAction(this), "control N");
        addAction(new NewSourceTabAction(this), "control S");
        addAction(new QuitAction(this), "control Q");
    }

    void addAction(Action action, String keystroke) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keystroke);
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStroke);
        frame.getRootPane().getActionMap().put(keyStroke, action);
    }

    public void createAndShowNewReaderTab() {
        final JReaderPanel readerPanel = createReaderPanel();
        readerPanel.addChangeListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, final String oldURL, final String newURL) {
                updateReaderUI(newURL);
            }
        });
        readerPanel.addPopupListener(new PopUpListener(this));
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

    // Updates the bottom panel with the new path, sets the title
    // and determines whether or not to show the Show Source button.
    private void updateReaderUI(final String newURL) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateReaderLabel(newURL);
                updateReaderTitle(newURL);
                maybeEnableSourceOption(newURL);
            }
        });
    }

    private void updateReaderTitle(String newURL) {
        String tabTitle = Utilities.extractTitle(newURL);
        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabTitle);
    }

    private void updateReaderLabel(String newURL) {
        String systemPath = Utilities.browserPathToSystemPath(newURL);
        bottomPanel.getLblStatus().setText(systemPath);
    }

    private void showNewReaderTab(JReaderPanel readerPanel) {
        tabbedPane.setSelectedComponent(readerPanel);
        disableNewSourceOption();
        readerPanel.home();
    }

    private void initAutoCompleteWords() {
        topPanel.addAutoCompleteWords(profileManager.getClassNames());
        currentTab = getCurrentTab();
    }

    private void initListeners() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveProfiles();
                System.exit(0);
            }
        });
        topPanel.getBtnBack().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = (JReaderPanel) currentTab;
                jReaderPanel.back();
            }
        });
        topPanel.getBtnNext().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = (JReaderPanel) currentTab;
                jReaderPanel.next();
            }
        });
        topPanel.getBtnHome().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JReaderPanel jReaderPanel = (JReaderPanel) currentTab;
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

    private void displayUI() {
        frame.setVisible(true);
    }

    /**
     * Creates a new source tab and sets it as the currently selected tab.
     */
    public void createAndShowNewSourceTab() {
        JReaderPanel panel = (JReaderPanel) currentTab;
        final String filePath = Utilities.docPathToSourcePath(panel.getCurrentPath());
        final String title = Utilities.extractFileName(filePath);

        if (!Utilities.isGoodSourcePath(filePath)) {
            Utilities.showErrorDialog(frame, filePath + " does not exist.", "File Not Found");
            log.error("No such file: " + filePath);
            return;
        }
        final JSourcePanel newTab = createNewSourceTab(filePath, title);
        new Thread(new Runnable() {
            @Override
            public void run() {
                newTab.createDisplay();
                showNewSourceTab(newTab);
            }
        }).start();
    }

    private JSourcePanel createNewSourceTab(String filePath, String title) {
        JSourcePanel newTab = new JSourcePanel(new File(filePath), topPanel);
        newTab.addActionListener(bottomPanel);
        addCloseButtonToTab(newTab, title);
        return newTab;
    }

    private void showNewSourceTab(JSourcePanel newTab) {
        // These buttons don't make sense in JSourcePanel.
        disableBrowserButtons();
        setCurrentTab(newTab);
    }

    private void setCurrentTab(AbstractPanel newTab) {
        tabbedPane.setSelectedComponent(newTab);
        resetSearchBar();
    }

    // Handle changing tabs. Remove and add autocomplete words, enable/disable buttons.
    private void handleTabChange() {
        if (currentTab == null)
            return;

        /* We want to remove the previous panels words so they don't pollute the new panels auto complete. */
        ArrayList<String> prevWords = currentTab.getAutoCompleteWords();
        currentTab = getCurrentTab();

        if (currentTab instanceof JReaderPanel) {
            handleReaderTabChange(prevWords);
        } else {
            handleSourceTabChange(prevWords);
        }

        /* Remove whatever text is there becuase it's annoying always having to delete it. */
        topPanel.clearSearchBar();
    }

    public AbstractPanel getCurrentTab() {
        return (AbstractPanel) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
    }

    private void handleSourceTabChange(ArrayList<String> prevWords) {
        disableBrowserButtons();
        topPanel.removeAutoCompleteWords(prevWords);
        topPanel.addAutoCompleteWords(currentTab.getAutoCompleteWords());
    }

    private void handleReaderTabChange(ArrayList<String> prevWords) {
        topPanel.setSourceButton(TopPanel.SOURCE_BUTTON, new NewSourceTabAction(this));
        topPanel.removeAutoCompleteWords(prevWords);
        topPanel.addAutoCompleteWords(currentTab.getAutoCompleteWords());
        String currentPath = ((JReaderPanel) currentTab).getCurrentPath();
        maybeEnableSourceOption(currentPath);
        enableBrowserButtons();
    }

    // Decide whether or not to show the New Source option.
    private void maybeEnableSourceOption(String newPath) {
        assert newPath != null;
        String srcPath = Utilities.docPathToSourcePath(newPath);
        if (Utilities.isGoodSourcePath(srcPath)) {
            enableNewSourceOption();
        } else {
            disableNewSourceOption();
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

    private void handleSearch() {
        currentTab.handleAutoComplete(topPanel.getSearchBarText());
    }

    public void addJavaDocClassNames() {
        topPanel.addAutoCompleteWords(profileManager.getClassNames());
    }

    public void removeJavaDocClassNames() {
        topPanel.removeAutoCompleteWords(profileManager.getClassNames());
    }

    public void resetSearchBar() {
        topPanel.clearSearchBar();
        topPanel.getSearchBar().requestFocus();
    }

    public void saveProfiles() {
        try {
            log.debug("Saving profiles..");
            profileManager.saveProfiles();
            log.debug("Success!");
            System.exit(0);
        } catch (IOException e) {
            Utilities.showErrorDialog(frame, e.getMessage(), "Error Saving Profiles");
            log.error(e.getMessage(), e);
        }
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







