package com.facetoe.jreader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parses the Java documentation and packages it in a HashMap with the class name as key and a JavaObjectOld as the value.
 */
public class JavaDocParser {

    private HashMap<String, HashMap<String, String>> allClassData = new HashMap<String, HashMap<String, String>>();

    /* The base path for the Java docs: /dir/dir/dir/docs/api/ */
    private String basePath;

    /* HashMap containing the class name as the key and a JavaObjectOld object as the value */
    private HashMap<String, String> classNames = new HashMap<String, String>();

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
    public HashMap<String, String> parse(String indexFile) {

        File inFile = new File(basePath + indexFile);
        Document doc = null;
        try {
            doc = Jsoup.parse(inFile, "UTF-8");
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

        /* The indexContainer contains links to all the Java classes */
        Elements container;
        assert doc != null;
        container = doc.select("div.indexContainer");


        /* Extract all the links */
        Elements links = container.select("a");
        int numLinks = links.size();
        int count = 0;

        /* Loop over all the links, extracting the name and link */
        for ( Element link : links ) {
            fireEvent(ActionEvent.ACTION_PERFORMED, link.text(), ( long ) ((count * 100.0f) / numLinks));
            count++;
            String relativePath = link.attr("href");
            classNames.put(link.text(), relativePath);
        }
        fireEvent(ActionEvent.ACTION_LAST, "Complete", 100);
        return classNames;
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
}


/**
 * Parses the Java documentation and displays the progress.
 */
class ParserProgressWindow extends ProgressWindow<HashMap<String, String>> {
    @Override
    public HashMap<String, String> execute() {
        JavaDocParser parser = new JavaDocParser(Config.getInstance().getString("apiDir"));
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

        HashMap<String, String> data = parser.parse("allclasses-noframe.html");
        setVisible(false);
        dispose();

        return data;
    }
}
