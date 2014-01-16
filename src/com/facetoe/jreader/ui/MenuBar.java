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

import com.facetoe.jreader.helpers.Config;
import com.facetoe.jreader.helpers.ProfileManager;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 28/10/13
 * Time: 9:48 AM
 */

/**
 * This class creates the menu bar along the top of JReader
 */
class MenuBar extends JMenuBar {

    private final JReader jReader;
    private JMenuItem mnuNewSourceTab;
    private JMenu subMenuChangeProfile;
    private JMenu subMenuDeleteProfile;
    private final ProfileManager profileManager = ProfileManager.getInstance();

    public MenuBar(JReader jReader) {
        this.jReader = jReader;
        createMenus();
    }

    /**
     * Build all the menus.
     */
    private void createMenus() {
        createFileMenu();
        createWindowMenu();
        createFindMenu();
    }

    private void createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem newProfileMenu = buildNewProfileItem();
        fileMenu.add(newProfileMenu);

        subMenuChangeProfile = buildChangeProfileSubMenu();
        fileMenu.add(subMenuChangeProfile);

        subMenuDeleteProfile = buildDeleteProfileSubMenu();
        fileMenu.add(subMenuDeleteProfile);

        JMenuItem quitItem = buildQuitItem();
        fileMenu.add(quitItem);
        add(fileMenu);
    }

    private JMenuItem buildNewProfileItem() {
        final JMenuItem newProfileItem = new JMenuItem("New Profile");
        newProfileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewProfileOption();
            }
        });
        return newProfileItem;
    }

    private void handleNewProfileOption() {
        jReader.removeJavaDocClassNames();
        // This is a blocking dialog
        new NewProfileWindow();
        jReader.addJavaDocClassNames();

        // If a new profile was succesfully created, navigate home to update the screen.
        navigateHome();
    }

    private JMenu buildChangeProfileSubMenu() {
        JMenu changeProfileMenu = new JMenu("Change Profile");
        changeProfileMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                handleChangeProfileSelected();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        return changeProfileMenu;
    }

    /* This method rebuilds the menu each time it is selected, otherwise
    * deleted profiles will still be visible and new profiles won't show up. */
    private void handleChangeProfileSelected() {
        subMenuChangeProfile.removeAll();
        ArrayList<String> profiles = profileManager.getProfileNames();
        for (String profile : profiles) {
            JMenuItem profMenuItem = buildProfileMenuItem(profile);
            subMenuChangeProfile.add(profMenuItem);
        }
    }

    private JMenuItem buildProfileMenuItem(String profile) {
        JMenuItem profMenuItem = new JMenuItem(profile);
        final String profileName = profile;
        profMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewProfileOptionSelected(profileName);
            }
        });
        return profMenuItem;
    }

    private void handleNewProfileOptionSelected(String profileName) {
        jReader.removeJavaDocClassNames();
        profileManager.setCurrentProfile(profileName);
        jReader.addJavaDocClassNames();

        // Navigate home to refresh the screen.
        navigateHome();
    }

    private JMenu buildDeleteProfileSubMenu() {
        JMenu deleteMenuOption = new JMenu("Delete Profile");
        /* Rebuild the delete menu each time it is selected. */
        deleteMenuOption.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                handleDeleteOptionSelected();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        return deleteMenuOption;
    }

    /* This method rebuilds the menu each time it is selected, otherwise
     * deleted profiles will still be visible. */
    private void handleDeleteOptionSelected() {
        subMenuDeleteProfile.removeAll();
        ArrayList<String> profiles = profileManager.getProfileNames();
        for (String profile : profiles) {
            JMenuItem item = buildDeleteMenuItem(profile);
            subMenuDeleteProfile.add(item);
        }
    }

    private JMenuItem buildDeleteMenuItem(String profile) {
        JMenuItem item = new JMenuItem(profile);

        // Don't allow the user to delete the default profile.
        if (profile.equals(Config.DEFAULT_PROFILE_NAME)) {
            item.setEnabled(false);

        } else {
            final String profileName = profile;
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int result = JOptionPane.showConfirmDialog(null,
                            "Are you sure you want to delete " + profileName + "?",
                            "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        // profileManager.deleteProfile sets the profile to Default.
                        profileManager.deleteProfile(profileName);

                        // Navigate home to refresh the screen with default profile.
                        navigateHome();
                    }
                }
            });
        }
        return item;
    }

    private void createWindowMenu() {
        JMenu windowMenu = new JMenu("Window");
        mnuNewSourceTab = buildSimpleMenuItem(new NewSourceTabAction(jReader));
        windowMenu.add(mnuNewSourceTab);

        JMenuItem mnuNewReaderTab = buildSimpleMenuItem(new NewReaderTabAction(jReader));
        windowMenu.add(mnuNewReaderTab);
        add(windowMenu);
    }

    private JMenuItem buildSimpleMenuItem(Action action) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.setAction(action);
        return menuItem;
    }

    private JMenuItem buildQuitItem() {
        return new JMenuItem(new QuitAction(jReader));
    }

    // TODO should all the reader panels be updated here?
    // Navigate home if in a JReader panel.
    private void navigateHome() {
        if (jReader.getCurrentTab() instanceof JReaderPanel) {
            JReaderPanel panel = (JReaderPanel) jReader.getCurrentTab();
            panel.home();
        }
    }

    private void createFindMenu() {
        final JMenu mnuFind = new JMenu("Find");
        mnuFind.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                handleFindSelected(mnuFind);
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        add(mnuFind);
    }

    // Rebuild the menu each time it is selected so the settings reflect those in the current profile.
    private void handleFindSelected(JMenu mnuFind) {
        mnuFind.removeAll();

        JCheckBoxMenuItem chkWholeWord = buildWholeWordCheckbox();
        mnuFind.add(chkWholeWord);

        JCheckBoxMenuItem chkMatchCase = buildMatchCaseCheckBox();
        mnuFind.add(chkMatchCase);

        JCheckBoxMenuItem chkRegexp = buildRegexpCheckBox();
        mnuFind.add(chkRegexp);
    }

    private JCheckBoxMenuItem buildRegexpCheckBox() {
        final JCheckBoxMenuItem chkRegexp = new JCheckBoxMenuItem("Regular Expression");
        // Don't enable regexp if matchCase or wholeWord is enabled.
        if (profileManager.matchCaseIsEnabled() || profileManager.wholeWordIsEnabled()) {
            chkRegexp.setEnabled(false);
        } else {
            chkRegexp.setState(profileManager.regexpIsEnabled());
            chkRegexp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    profileManager.getSearchContext().setRegularExpression(chkRegexp.getState());
                    profileManager.setRegexpEnabled(chkRegexp.getState());
                }
            });
        }
        return chkRegexp;
    }

    private JCheckBoxMenuItem buildMatchCaseCheckBox() {
        final JCheckBoxMenuItem chkMatchCase = new JCheckBoxMenuItem("Match Case");

        // Match case doesn't make sense with regexp.
        if (profileManager.regexpIsEnabled()) {
            chkMatchCase.setEnabled(false);
        } else {
            chkMatchCase.setState(profileManager.matchCaseIsEnabled());
            chkMatchCase.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    profileManager.getSearchContext().setWholeWord(chkMatchCase.getState());
                    profileManager.setMatchCaseEnabled(chkMatchCase.getState());
                }
            });
        }
        return chkMatchCase;
    }

    private JCheckBoxMenuItem buildWholeWordCheckbox() {
        final JCheckBoxMenuItem chkWholeWord = new JCheckBoxMenuItem("Whole Word");

        // Whole word doesn't make sense with regexp.
        if (profileManager.regexpIsEnabled()) {
            chkWholeWord.setEnabled(false);
        } else {
            chkWholeWord.setState(profileManager.wholeWordIsEnabled());
            chkWholeWord.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    profileManager.getSearchContext().setWholeWord(chkWholeWord.getState());
                    profileManager.setWholeWordEnabled(chkWholeWord.getState());
                }
            });
        }
        return chkWholeWord;
    }

    /**
     * Disable the New Source option in the menu bar.
     */
    public void disableNewSourceOption() {
        mnuNewSourceTab.setEnabled(false);
    }

    /**
     * Enable the New Source option in the menu bar.
     */
    public void enableNewSourceOption() {
        mnuNewSourceTab.setEnabled(true);
    }
}
