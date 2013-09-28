package com.facetoe.jreader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import static javafx.concurrent.Worker.State.FAILED;

public class JReader extends JFrame implements Runnable {
    private JFXPanel jfxPanel;
    private WebEngine engine;

    private JPanel panel = new JPanel(new BorderLayout());
    private JLabel lblStatus = new JLabel();

    private JButton btnSearch = new JButton("Search");
    private JButton btnBack = new JButton("Back");
    private JButton btnNext = new JButton("Next");
    private JButton btnHome = new JButton("Home");

    private AutoCompleteTextField txtURL = new AutoCompleteTextField();
    private JProgressBar progressBar = new JProgressBar();

    private HashMap<String, String> classes;

    private Stack<String> nextStack = new Stack<String>();
    private Stack<String> backStack = new Stack<String>();

    private String currentPage;
    private String initialURL;

    public JReader(String url) {
        initialURL = url;
    }


    private void initComponents() {
        jfxPanel = new JFXPanel();

        createScene();

        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String path = classes.get(txtURL.getText());
                    if(path != null) {
                        String url = this.getClass().getResource("/com/facetoe/jreader/docs/api/" + path)
                                .toURI().toURL().toString();

                        nextStack.clear();

                        loadURL(url);
                    }

                } catch ( URISyntaxException ex ) {
                    ex.printStackTrace();
                } catch ( MalformedURLException ex ) {
                    ex.printStackTrace();
                }
            }
        };

        ActionListener listenerNext = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( !nextStack.empty() ) {
                    String page = nextStack.pop();

                    if ( page.equalsIgnoreCase(currentPage) && !nextStack.empty() ) {
                        page = nextStack.pop();
                    }

                    loadURL(page);
                    backStack.push(currentPage);
                    currentPage = page;
                }
            }
        };

        ActionListener listenerPrev = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( !backStack.empty() ) {
                    String page = backStack.pop();

                    if ( page.equalsIgnoreCase(currentPage) && !backStack.empty() ) {
                        page = backStack.pop();
                    }

                    loadURL(page);
                    nextStack.push(currentPage);
                    currentPage = page;

                }
            }
        };

        ActionListener listenerHome = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    loadURL(this.getClass().getResource("/com/facetoe/jreader/docs/index.html").toURI().toURL().toString());
                    nextStack.clear();

                } catch ( MalformedURLException ex ) {
                    ex.printStackTrace();
                } catch ( URISyntaxException ex ) {
                    ex.printStackTrace();
                }
            }
        };

        btnSearch.addActionListener(al);
        btnBack.addActionListener(listenerPrev);
        btnNext.addActionListener(listenerNext);
        btnHome.addActionListener(listenerHome);

        txtURL.addActionListener(al);

        Parser p = new Parser();
        String html = p.getFileAsString("/com/facetoe/jreader/docs/api/allclasses-noframe.html");
        classes = Parser.parseLinks(html);

        ArrayList<String> test = new ArrayList<String>(classes.keySet());
        txtURL.addWordsToTrie(test);

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);


        /* You create 3 panels, left, right and top. The components go into the left and
           right panels with their own layout manager and they both go in the top panel.
         */
        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        JPanel leftBar = new JPanel(new BorderLayout());
        JPanel rightBar = new JPanel(new FlowLayout());

        txtURL.setPreferredSize(new Dimension(500, 15));

        leftBar.add(txtURL, BorderLayout.WEST);
        leftBar.add(btnSearch, BorderLayout.EAST);
        rightBar.add(btnBack);
        rightBar.add(btnNext);
        rightBar.add(btnHome);

        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);

        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));


        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        this.setTitle("JReader");
        this.getContentPane().add(panel);
    }

    private void createScene() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                WebView view = new WebView();
                engine = view.getEngine();

                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                lblStatus.setText(event.getData());
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                currentPage = newValue;
                                backStack.push(newValue);
                            }
                        });
                    }
                });

                engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setValue(newValue.intValue());
                            }
                        });
                    }
                });

                engine.getLoadWorker()
                        .exceptionProperty()
                        .addListener(new ChangeListener<Throwable>() {

                            public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                                if ( engine.getLoadWorker().getState() == FAILED ) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            JOptionPane.showMessageDialog(
                                                    panel,
                                                    (value != null) ?
                                                            engine.getLocation() + "\n" + value.getMessage() :
                                                            engine.getLocation() + "\nUnexpected error.",
                                                    "Loading error...",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                                }
                            }
                        });

                jfxPanel.setScene(new Scene(view));
            }
        });
    }

    public void loadURL(final String url) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String tmp = toURL(url);

                if ( tmp == null ) {
                    tmp = toURL("file://" + url);
                }

                engine.load(tmp);
            }
        });
    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch ( MalformedURLException exception ) {
            return null;
        }
    }

    @Override
    public void run() {

        this.setPreferredSize(new Dimension(1024, 600));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();

        try {
            loadURL(this.getClass().getResource(initialURL).toURI().toURL().toString());
        } catch ( MalformedURLException ex ) {
            ex.printStackTrace();
        } catch ( URISyntaxException ex ) {
            ex.printStackTrace();
        }

        this.pack();
        this.setVisible(true);
    }

    public static void main(String[] args) {
        //SwingUtilities.invokeLater(new JReader());
        JReader test = new JReader("/com/facetoe/jreader/docs/index.html");
        test.run();
    }
}

