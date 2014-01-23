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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static javafx.concurrent.Worker.State.FAILED;

/**
 * Displays the Java documentation.
 */
class JReaderPanel extends JPanel implements AutoCompletable, Navigatable {
    private final Logger log = Logger.getLogger(this.getClass());

    private WebEngine engine;
    private JFXPanel jfxPanel;
    private JProgressBar progressBar;

    private final ProfileManager profileManager = ProfileManager.getInstance();
    private String currentPath;

    /**
     * Create a new JReaderPanel instance. Sets the initial URL to the index of the Java documentation.
     */
    public JReaderPanel() {
        init();
    }

    private void init() {
        progressBar = new JProgressBar(); //TODO !
        jfxPanel = new JFXPanel();
        setLayout(new BorderLayout());
        currentPath = profileManager.getHome();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createScene();
            }
        });
    }

    private void createScene() {
        WebView view = new WebView();
        view.setContextMenuEnabled(false); // Need for popup
        engine = view.getEngine();
        addProgressChangeListener();
        addPathChangeListener();
        addErrorListener();
        jfxPanel.setScene(new Scene(view));
        add(jfxPanel);
    }

    private void addProgressChangeListener() {
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
    }

    private void addPathChangeListener() {
        engine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                log.debug("New Path: " + newValue);
                if (!newValue.endsWith(".java")) {
                    currentPath = newValue;
                }
            }
        });
    }

    private void addErrorListener() {
        engine.getLoadWorker()
                .exceptionProperty()
                .addListener(new ChangeListener<Throwable>() {
                    public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                        if (engine.getLoadWorker().getState() == FAILED) {
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
    }


    @Override
    public ArrayList<String> getAutoCompleteWords() {
        return profileManager.getClassNames();
    }

    @Override
    public void handleAutoComplete(String key) {
        HashMap<String, String> classData = profileManager.getClassData();
        if (classData.containsKey(key)) {
            String relativePath = classData.get(key);
            loadURL(profileManager.getDocDir() + relativePath);
        }
    }

    public void next() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getEngine().executeScript("history.forward()");
            }
        });
    }

    public void back() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getEngine().executeScript("history.back()");
            }
        });
    }

    public void home() {
        File homeFile = new File(profileManager.getHome());
        if (homeFile.exists()) {
            loadURL(homeFile.getAbsolutePath());
        } else {
            log.error("Couldn't locate home file.");
            Utilities.showErrorDialog("Couldn't locate home file", "File Not Found");
        }
    }

    void loadURL(final String url) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                /* This check is necessary because passing a url that starts with file:// to Paths.get(url).toUri()
                 * results in a mutated path on Linux - although it seemed to work fine on Windows.
                 */
                if (url.startsWith("file://")) {
                    engine.load(url);
                } else {
                    String path = url;
                    try {
                        path = Paths.get(url).toUri().toString();
                    } catch (InvalidPathException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                    engine.load(path);
                }
            }
        });
    }

    WebEngine getEngine() {
        return engine;
    }

    JFXPanel getJFXPanel() {
        return jfxPanel;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    // This is called when the user clicks a link and succesfully navigates to it.
    public void addChangeListener(final ChangeListener<String> listener) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                engine.locationProperty().addListener(listener);
            }
        });
    }

    public void addPopupListener(final PopUpListener listener) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getJFXPanel().addMouseListener(listener);
            }
        });
    }
}

