package com.facetoe.jreader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Parses the Java documentation and packages it in a JavaClassData object.
 */
public class JavaDocParser {

    /* The class names and their associated URL */
    private HashMap<String, String> classes = new HashMap<String, String>();

    /**
     * The class name as the key with another HashMap as the value.
     * The second HashMap contains all the methods and their associated URLs
     */
    private HashMap<String, HashMap<String, String>> allClassData = new HashMap<String, HashMap<String, String>>();

    /* The base path for the Java docs: /dir/dir/dir/docs/api/ */
    private String basePath;

    /* JavaClassData object to store the results in */
    private JavaClassData classData = new JavaClassData();

    /* Action listeners for this parser */
    private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    public JavaDocParser(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Parses the Java docs by extracting the class names from the index file and
     * then visiting each class HTML file and extracting method information.
     *
     * @param indexFile should be allclasses-noframe.html
     */
    public void parse(String indexFile) {

        File inFile = new File(basePath + indexFile);
        Document doc = null;
        try {
            doc = Jsoup.parse(inFile, "UTF-8");
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

        /* The indexContainer contains links to all the Java classes */
        Elements container = doc.select("div.indexContainer");

        /* Extract all the links */
        Elements links = container.select("a");
        int numLinks = links.size();
        int count = 0;

        /* Loop over all the links, extracting the name and parsing each class file */
        for ( Element link : links ) {
            fireEvent(ActionEvent.ACTION_PERFORMED, link.text(), ( long ) ((count * 100.0f) / numLinks));
            count++;
            classes.put(link.text(), link.attr("href"));
            parseClassFile(basePath + link.attr("href"), link.text());
        }
        fireEvent(ActionEvent.ACTION_LAST, "Complete", 100);
    }

    /**
     * Parses each class file.
     *
     * @param classPath path to the file to be parsed.
     * @param className the file to be parsed.
     */
    public void parseClassFile(String classPath, String className) {
        File inFile = new File(classPath);
        Document doc = null;
        HashMap<String, String> data = new HashMap<String, String>();

        try {
            doc = Jsoup.parse(inFile, "UTF-8");
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

        /* Contains the constructor and method summaries */
        Elements elements = doc.select("div.summary ul.blockList");

        /* Contains the links for constructor and method summaries */
        Elements links = elements.select("a");

        for ( Element link : links ) {
            if ( !link.attr("href").isEmpty() && link.attr("href").contains("#") ) {
                String[] parts = link.attr("href").split("#");
                String name = parts[parts.length - 1];
                String path = parts[0].replaceAll("\\.\\./", "");
                data.put(name, path);
            }
        }
        allClassData.put(className, data);

        classData = new JavaClassData();
        classData.setAllClassData(allClassData);
        classData.setClasses(classes);
    }

    /**
     * Notify the listeners of changes.
     *
     * @param eventType the event type
     * @param message   the message
     * @param progress  how far we have got
     */
    public void fireEvent(int eventType, String message, long progress) {
        ActionEvent event = new ActionEvent(this, eventType, message, progress, 0);
        for ( ActionListener listener : listeners ) {
            listener.actionPerformed(event);
        }
    }

    /**
     * Add an action listener to receive events.
     *
     * @param listener the listener
     */
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    /**
     * Get the result of the parsing.
     *
     * @return
     */
    public JavaClassData getClassData() {
        return classData;
    }
}

/**
 * A window to display processing progress.
 *
 * @param <T> The object returned by the processing.
 */
abstract class ProgressWindow<T> extends JFrame {
    JPanel panel = new JPanel();

    /* This will be updated by the listener */
    JLabel lblProgress = new JLabel();
    JButton btnCancel = new JButton("Cancel");
    JLabel lblTitle = new JLabel("Loading...");
    JProgressBar progressBar = new JProgressBar();

    public ProgressWindow() {

        //TODO figure out what to do with the button.
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        progressBar.setIndeterminate(false);

        panel.setLayout(new BorderLayout(10, 10));
        panel.add(lblTitle, BorderLayout.WEST);
        panel.add(lblProgress, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.NORTH);
        panel.add(btnCancel, BorderLayout.EAST);
        panel.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        setTitle("JReader");
        setResizable(false);

        getContentPane().add(panel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(550, 72));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Implement this method to do the processing.
     *
     * @return the result of processing.
     */
    abstract public T execute();
}

/**
 * Parses the Java documentation and displays the progress.
 */
class ParserProgressWindow extends ProgressWindow<JavaClassData> {
    @Override
    public JavaClassData execute() {
        JavaDocParser parser = new JavaDocParser("/home/facetoe/tmp/docs/api/");
        parser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( e.getID() == ActionEvent.ACTION_PERFORMED ) {
                    progressBar.setValue(( int ) e.getWhen());
                    lblTitle.setText("Parsing: ");
                    lblProgress.setText(e.getActionCommand());
                } else if ( e.getID() == ActionEvent.ACTION_LAST ) {
                    progressBar.setValue(( int ) e.getWhen());
                    lblTitle.setText("Parsing: ");
                    lblProgress.setText(e.getActionCommand());
                }
            }
        });

        parser.parse("allclasses-noframe.html");
        setVisible(false);
        dispose();
        return parser.getClassData();
    }
}
