package com.facetoe.jreader;

import japa.parser.ParseException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;


public class JReader extends JFrame {

    private JButton btnBack = new JButton("Back");
    private JButton btnNext = new JButton("Next");
    private JButton btnHome = new JButton("Home");
    JButton btnSearch = new JButton("Search");

    private JButton btnSource;// = new JButton("View Source");

    private JMenuItem mnuNewSource;
    private JMenu subMenuChangeProfile;
    private JMenu subMenuDeleteProfile;
    private SearchContext searchContext = new SearchContext();

    /* This is necessary to make the Swing thread wait until the javafx content is loaded on startup.
     * If it's not set then the Swing components are displayed before there is any content in them. */
    CountDownLatch javafxLoadLatch = new CountDownLatch(1);

    /* Keeps track of which profile we are using and provides access to the settings for that profile. */
    ProfileManager profileManager;


    private HashMap<String, String> classNames;
    private JavaSourceFile currentSourceFile;

    private AutoCompleteTextField searchBar = new AutoCompleteTextField();
    private JProgressBar progressBar = new JProgressBar();
    private final JTabbedPane tabbedPane = new JTabbedPane();

    private JPanel currentTab;

    public JReader() {

        if(!JReaderSetup.isSetup()) {
            new SetupWindow();
        }

        profileManager = ProfileManager.getInstance();
        addJavaDocCLassNames();
        newJReaderTab("JReader", false);

        initMenus();
        initActions();
        initTopPanel();
        initListeners();

        add(tabbedPane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1024, 600));
        setSize(1024, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("JReader");

        System.out.println("Setting visible");
        pack();
        setVisible(true);
    }

    private void initMenus() {
        JMenuBar menuBar = new JMenuBar();
        JMenu windowMenu = new JMenu("Window");
        mnuNewSource = new JMenuItem();
        mnuNewSource.setAction(new ViewSourceAction(this));
        windowMenu.add(mnuNewSource);
        btnSource = new JButton(new ViewSourceAction(this));

        JMenu fileMenu = new JMenu("File");
        final JMenuItem itmNewProfile = new JMenuItem("New Profile");
        itmNewProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new NewProfileWindow();
                if ( currentTab instanceof JReaderPanel ) {
                    JReaderPanel panel = ( JReaderPanel ) currentTab;
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
                            removeJavaDocClassNames();
                            profileManager.setCurrentProfile(profileName);
                            if(currentTab instanceof JReaderPanel) {
                                JReaderPanel panel = (JReaderPanel)currentTab;
                                panel.home();
                            }
                            addJavaDocCLassNames();
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
        chkWholeWord.setState(profileManager.wholeWordIsEnabled());
        chkWholeWord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchContext.setWholeWord(chkWholeWord.getState());
                profileManager.setWholeWordEnabled(chkWholeWord.getState());
            }
        });
        mnuFind.add(chkWholeWord);

        final JCheckBoxMenuItem chkMatchCase = new JCheckBoxMenuItem("Match Case");
        chkMatchCase.setState(profileManager.matchCaseIsEnabled());
        chkMatchCase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchContext.setMatchCase(chkMatchCase.getState());
                profileManager.setMatchCaseEnabled(chkMatchCase.getState());
            }
        });
        mnuFind.add(chkMatchCase);

        final JCheckBoxMenuItem chkRegexp = new JCheckBoxMenuItem("Regular Expression");
        chkRegexp.setState(profileManager.regexpIsEnabled());
        chkRegexp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchContext.setRegularExpression(chkRegexp.getState());
                profileManager.setRegexpEnabled(chkRegexp.getState());
            }
        });
        mnuFind.add(chkRegexp);

        menuBar.add(mnuFind);
    }

    private void initActions() {
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

            System.out.println("Path: " + path);

            if ( !Utilities.isGoodSourcePath(filePath) ) {
                System.err.println("Bad File Path");
                return;
            }

            try {
                currentSourceFile = JavaSourceFileParser.parse(new FileInputStream(filePath));
            } catch ( ParseException e ) {
                e.printStackTrace();
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        } else {
            System.err.println("Not a ReaderPanel");
            return;
        }

        if ( !Utilities.isGoodSourcePath(filePath) ) {
            JOptionPane.showMessageDialog(this, "Bad File: " + filePath, "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JSourcePanel newTab = new JSourcePanel(filePath);
        addCloseButtonToTab(newTab, title);
        tabbedPane.setSelectedComponent(newTab);

        if ( currentSourceFile != null ) {
            addAutoCompleteWords();
            JavaClassOrInterface obj = currentSourceFile.getEnclosingClass();
            newTab.highlightDeclaration(obj.getBeginLine(), obj.getEndLine(), obj.beginColumn);
        } else {
            System.out.println("IT was null");
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
                    System.out.println("Waiting");
                    javafxLoadLatch.await();
                    System.out. println("Finished");
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
        mnuNewSource.setEnabled(false);
    }

    private void enableNewSourceOption() {
        btnSource.setEnabled(true);
        mnuNewSource.setEnabled(true);
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
            JavaObject method = currentSourceFile.getItem(searchBar.getText());
            if ( method != null ) {
                sourcePanel.highlightDeclaration(method.getBeginLine(), method.getEndLine(),
                        method.beginColumn);
            } else {
                sourcePanel.findString(searchBar.getText(), searchContext);
            }
        }
    }

    private void addJavaDocCLassNames() {
        try {
            File classDataFile = new File(profileManager.getClassDataFilePath());
            classNames = Utilities.readClassData(classDataFile);
            searchBar.addWordsToTrie(new ArrayList<String>(classNames.keySet()));
        } catch ( IOException e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load class data at"
                    + profileManager.getClassDataFilePath(),
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load class data at"
                    + profileManager.getClassDataFilePath(),
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void removeJavaDocClassNames() {
        searchBar.removeWordsFromTrie(new ArrayList<String>(classNames.keySet()));
    }

    public static void main(String[] args) {
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
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch ( InstantiationException e1 ) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch ( IllegalAccessException e1 ) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch ( UnsupportedLookAndFeelException e1 ) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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







