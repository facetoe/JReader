package com.facetoe.jreader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import jsyntaxpane.DefaultSyntaxKit;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import static javafx.concurrent.Worker.State.FAILED;

public class JReaderPanel extends JFXPanel implements Runnable {
    private WebEngine engine;
    private JProgressBar progressBar;
    private Stack<String> nextStack = new Stack<String>();
    private Stack<String> backStack = new Stack<String>();
    private String currentPage;
    private String initialURL;


    public JReaderPanel(String url, JProgressBar jProgressBar) {
        initialURL = url;
        progressBar = jProgressBar;
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

                setScene(new Scene(view));
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
        try {
            loadURL(this.getClass().getResource("/com/facetoe/jreader/docs/index.html").toURI().toURL().toString());
            nextStack.clear();

        } catch ( MalformedURLException ex ) {
            ex.printStackTrace();
        } catch ( URISyntaxException ex ) {
            ex.printStackTrace();
        }
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

        try {
            loadURL(this.getClass().getResource(initialURL).toURI().toURL().toString());
        } catch ( MalformedURLException ex ) {
            ex.printStackTrace();
        } catch ( URISyntaxException ex ) {
            ex.printStackTrace();
        }
    }
}

class JReader extends JFrame {

    private JLabel lblStatus = new JLabel();
    private JButton btnSearch = new JButton("Search");
    private JButton btnBack = new JButton("Back");
    private JButton btnNext = new JButton("Next");
    private JButton btnHome = new JButton("Home");
    private JButton btnTest = new JButton("Test");
    private HashMap<String, String> classes;
    private AutoCompleteTextField searchBar = new AutoCompleteTextField();
    private JProgressBar progressBar = new JProgressBar();
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JReaderPanel jPanel;
    private JScrollPane jPanel2;
    private JReaderPanel currentTab;

    public JReader() {

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);


        /* You create 3 panels, left, right and top. The components go into the left and
           right panels with their own layout manager and they both go in the top panel.
         */
        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        JPanel leftBar = new JPanel(new BorderLayout());
        JPanel rightBar = new JPanel(new FlowLayout());

        searchBar.setPreferredSize(new Dimension(500, 15));

        leftBar.add(searchBar, BorderLayout.WEST);
        leftBar.add(btnSearch, BorderLayout.EAST);
        rightBar.add(btnBack);
        rightBar.add(btnNext);
        rightBar.add(btnHome);
        rightBar.add(btnTest);

        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);

        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));


        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        jPanel = new JReaderPanel("/com/facetoe/jreader/docs/index.html", progressBar);
        jPanel2 = getSourcePane("/home/facetoe/tmp/oldParse.java");

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTab.back();
            }
        });

        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTab.next();
            }
        });

        btnHome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTab.home();
            }
        });

        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadClass(searchBar.getText());
            }
        });

        btnTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JViewport viewport = jPanel2.getViewport();
                JEditorPane editorPane = ( JEditorPane ) viewport.getView();

                Scanner scanner = new Scanner(editorPane.getText());
                int line;

                for ( line = 0; scanner.hasNextLine() && !scanner.nextLine().contains("public class Main"); line++ ) {

                }

                try {
                    editorPane.scrollRectToVisible(editorPane.modelToView((
                            editorPane.getDocument()).getDefaultRootElement().getElement(line).getStartOffset()));
                } catch ( BadLocationException ex ) {
                    ex.printStackTrace();
                }

            }
        });

        searchBar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadClass(searchBar.getText());
            }
        });

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()) instanceof JReaderPanel ) {
                    currentTab = ( JReaderPanel ) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
                }
            }
        });

        tabbedPane.addTab("Test", jPanel);
        tabbedPane.add("Pane2", jPanel2);

        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1024, 600));
        setSize(1024, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //initAutocompleteTextField();
        setTitle("Jreader");
        setVisible(true);
    }

    private void loadClass(String className) {
        try {
            String path = classes.get(className);
            if ( path != null ) {
                System.out.println(path);
                String url = this.getClass().getResource("/com/facetoe/jreader/docs/api/" + path)
                        .toURI().toURL().toString();
                currentTab.loadURL(url);
            }

        } catch ( URISyntaxException ex ) {
            ex.printStackTrace();
        } catch ( MalformedURLException ex ) {
            ex.printStackTrace();
        }
    }

    private void initAutocompleteTextField() {
        //Parser p = new Parser(null);
        String html = getFileAsString("/com/facetoe/jreader/docs/api/allclasses-noframe.html");
        //classes = Parser.parse(html);

        ArrayList<String> test = new ArrayList<String>(classes.keySet());
        searchBar.addWordsToTrie(test);
    }

    public String getFileAsString(String path) {

        InputStream f = this.getClass().getResourceAsStream(path);

        int ch;
        StringBuilder builder = new StringBuilder();

        try {
            while ( (ch = f.read()) != -1 ) {
                builder.append(( char ) ch);
            }

            return builder.toString();

        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
        return "";
    }

    private JScrollPane getSourcePane(String filePath) {
        final JEditorPane codePane = new JEditorPane();
        JScrollPane scrollPane = new JScrollPane(codePane);
        DefaultSyntaxKit.initKit();

        codePane.setFont(new Font("Segoe Script", Font.PLAIN, 30));
        codePane.setLayout(new BorderLayout());
        codePane.setContentType("text/java");

        codePane.setText(readFile(filePath, StandardCharsets.UTF_8));
        return scrollPane;
    }

    public String readFile(String path, Charset encoding) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return encoding.decode(ByteBuffer.wrap(encoded)).toString();
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
        return null;
    }
//
//    public static void main(String[] args) {
//        new JReader();
//
//    }
}
