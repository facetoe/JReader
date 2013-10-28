package com.facetoe.jreader;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 28/10/13
 * Time: 9:48 AM
 */
public class JReaderMenuBar extends JMenuBar {

    private JReader jReader;
    private JMenuItem mnuNewSource;
    private JMenu subMenuChangeProfile;
    private JMenu subMenuDeleteProfile;
    private ProfileManager profileManager = ProfileManager.getInstance();

    public JReaderMenuBar(JReader jReader) {
        this.jReader = jReader;
        initMenus();
    }

    private void initMenus() {
        initWindowMenu();
        initFileMenu();
        initFindMenu();
    }

    private void initWindowMenu() {
        JMenu windowMenu = new JMenu("Window");
        mnuNewSource = new JMenuItem();
        mnuNewSource.setAction(new ViewSourceAction(jReader));
        windowMenu.add(mnuNewSource);

        JMenuItem mnuNewTab = new JMenuItem();
        mnuNewTab.setAction(new NewReaderTabAction(jReader));
        windowMenu.add(mnuNewTab);
        add(windowMenu);
    }

    private void initFileMenu() {
        JMenu fileMenu = new JMenu("File");
        final JMenuItem itmNewProfile = new JMenuItem("New Profile");
        itmNewProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new NewProfileWindow();
                if ( jReader.getCurrentTab() instanceof JReaderPanel ) {
                    JReaderPanel panel = ( JReaderPanel ) jReader.getCurrentTab();
                    panel.loadURL(profileManager.getDocDir() + File.separator + "overview-summary.html");
                    subMenuChangeProfile.add(Config.getString(Config.CURRENT_PROFILE));
                }
            }
        });
        fileMenu.add(itmNewProfile);

        subMenuChangeProfile = new JMenu("Change Profile");
        subMenuChangeProfile.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                subMenuChangeProfile.removeAll();
                ArrayList<String> profiles = profileManager.getProfileNames();
                for ( String profile : profiles ) {
                    JMenuItem item = new JMenuItem(profile);
                    final String profileName = profile;
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            jReader.removeJavaDocClassNames();
                            profileManager.setCurrentProfile(profileName);
                            if(jReader.getCurrentTab() instanceof JReaderPanel) {
                                JReaderPanel panel = (JReaderPanel)jReader.getCurrentTab();
                                panel.home();
                            }
                            jReader.addJavaDocCLassNames();
                        }
                    });
                    subMenuChangeProfile.add(item);
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        fileMenu.add(subMenuChangeProfile);


        subMenuDeleteProfile = new JMenu("Delete Profile");
        subMenuDeleteProfile.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                subMenuDeleteProfile.removeAll();
                ArrayList<String> profiles = profileManager.getProfileNames();
                for ( String profile : profiles ) {
                    JMenuItem item = new JMenuItem(profile);
                    if(profile.equals("Default")) {
                        item.setEnabled(false);

                    } else {

                        final String profileName = profile;
                        item.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                int result = JOptionPane.showConfirmDialog(null,
                                        "Are you sure you want to delete " + profileName + "?",
                                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                                if(result == JOptionPane.YES_OPTION) {
                                    profileManager.deleteProfile(profileName);
                                }
                            }
                        });
                    }
                    subMenuDeleteProfile.add(item);
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        fileMenu.add(subMenuDeleteProfile);

        JMenuItem itmQuit = new JMenuItem(new QuitAction(jReader));
        fileMenu.add(itmQuit);
        add(fileMenu);
    }

    private void initFindMenu() {
        JMenu mnuFind = new JMenu("Find");
        final JCheckBoxMenuItem chkWholeWord = new JCheckBoxMenuItem("Whole Word");
        chkWholeWord.setState(profileManager.wholeWordIsEnabled());
        chkWholeWord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jReader.getSearchContext().setWholeWord(chkWholeWord.getState());
                profileManager.setWholeWordEnabled(chkWholeWord.getState());
            }
        });
        mnuFind.add(chkWholeWord);

        final JCheckBoxMenuItem chkMatchCase = new JCheckBoxMenuItem("Match Case");
        chkMatchCase.setState(profileManager.matchCaseIsEnabled());
        chkMatchCase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jReader.getSearchContext().setMatchCase(chkMatchCase.getState());
                profileManager.setMatchCaseEnabled(chkMatchCase.getState());
            }
        });
        mnuFind.add(chkMatchCase);

        final JCheckBoxMenuItem chkRegexp = new JCheckBoxMenuItem("Regular Expression");
        chkRegexp.setState(profileManager.regexpIsEnabled());
        chkRegexp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jReader.getSearchContext().setRegularExpression(chkRegexp.getState());
                profileManager.setRegexpEnabled(chkRegexp.getState());
            }
        });
        mnuFind.add(chkRegexp);

        add(mnuFind);
    }

    public JMenuItem getMnuNewSource() {
        return mnuNewSource;
    }

}
