package com.facetoe.jreader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

import static javafx.concurrent.Worker.State.FAILED;

/**
 * Displays the java documentation.
 */
public class JReaderPanel extends JPanel {
    private WebEngine engine;
    private WebView view;
    private JFXPanel jfxPanel;
    private JProgressBar progressBar;
    private Stack<String> nextStack = new Stack<String>();
    private Stack<String> backStack = new Stack<String>();
    private String currentPage;
    private String initialURL;


    /**
     * Create a new JReaderPanel instance with the specified URL.
     *
     * @param url to display when the panel is loaded.
     * @param jProgressBar reference to the main JReader progress bar so we can display progress.
     */
    public JReaderPanel(String url, JProgressBar jProgressBar) {
        init(url, jProgressBar);
    }

    /**
     * Create a new JReaderPanel instance. Sets the initial URL to the index of the Java documentation.
     *
     * @param jProgressBar reference to of the main JReader progress bar so we can display progress.
     */
    public JReaderPanel(JProgressBar jProgressBar) {
        init(Config.getEntry("docDir") + File.separator + "index.html", jProgressBar);
    }

    /**
     * Initialize everything and load the url
     *
     * @param url to display when the panel is loaded
     * @param jProgressBar reference to of the main JReader progress bar so we can display progress.
     */
    private void init(String url, JProgressBar jProgressBar) {
        initialURL = url;
        progressBar = jProgressBar;
        jfxPanel = new JFXPanel();
        initComponents();
        loadURL(initialURL);
    }

    /**
     * Attempts to convert string to URL.
     *
     * @param str to be converted to URL.
     * @return the URL or null if it failed.
     */
    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch ( MalformedURLException exception ) {
            return null;
        }
    }

    private void initComponents() {
        createScene();
        setLayout(new BorderLayout());
    }

    private void createScene() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                view = new WebView();
                view.setContextMenuEnabled(false);

                engine = view.getEngine();

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

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        System.out.println("New val: " + newValue);
                        if ( !newValue.endsWith(".java") ) {
                            currentPage = newValue;
                            backStack.push(newValue);
                        }
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
                                                    null,
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
                add(jfxPanel);
            }
        });
    }

    public void next() {
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

    public void back() {
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

    public void home() {
        loadURL(Config.getEntry("docDir") + File.separator + "index.html");
        nextStack.clear();
    }

    public void loadURL(final String url) {
        System.out.println("Loading: " + url);
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

    public WebEngine getEngine() {
        return engine;
    }

    public JFXPanel getJFXPanel() {
        return jfxPanel;
    }

    public String getCurrentPage() {
        return currentPage;
    }
}

