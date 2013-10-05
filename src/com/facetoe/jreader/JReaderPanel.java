package com.facetoe.jreader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

import static javafx.concurrent.Worker.State.FAILED;

public class JReaderPanel extends JPanel implements Runnable {
    private WebEngine engine;
    private JFXPanel jfxPanel;
    private JProgressBar progressBar;
    private Stack<String> nextStack = new Stack<String>();
    private Stack<String> backStack = new Stack<String>();
    private String currentPage;
    private String initialURL;


    public JReaderPanel(String url, JProgressBar jProgressBar) {
        init(url, jProgressBar);
    }

    public JReaderPanel(JProgressBar jProgressBar) {
        init(Config.getEntry("docDir") + File.separator + "index.html", jProgressBar);
    }

    private void init(String url, JProgressBar jProgressBar) {
        initialURL = url;
        progressBar = jProgressBar;
        jfxPanel = new JFXPanel();
        run();
    }

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

                final WebView view = new WebView();
                view.setContextMenuEnabled(false);

                final ContextMenu menu = new ContextMenu();
                javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem("New Tab");
                item.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                    @Override
                    public void handle(javafx.event.ActionEvent actionEvent) {
                        System.out.println("You clicked me bitch");
                    }
                });

                menu.getItems().add(item);
                view.setOnMouseClicked(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent mouse) {
                        if ( mouse.getButton() == MouseButton.SECONDARY ) {
                            //add some menu items here
                            menu.show(view, mouse.getScreenX(), mouse.getScreenY());
                        } else {
                            if ( menu != null ) {
                                menu.hide();
                            }
                        }
                    }
                });

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
                        currentPage = newValue;
                        backStack.push(newValue);
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

    @Override
    public void run() {
        initComponents();
        loadURL(initialURL);
    }
}

