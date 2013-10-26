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


    /* Action listeners for this parser */
    private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    /**
     * Parses the Java docs by extracting the class names from the index file and
     * then visiting each class HTML file and extracting method information.
     *
     * @param filePath should point to allclasses-noframe.html
     */
    public HashMap<String, String> parse(String filePath) throws Exception {
        HashMap<String, String> classNames = parseNewJavadoc(filePath);
        if ( classNames == null ) {
            classNames = parseOldJavadoc(filePath);
        }

        if ( classNames == null ) {
            throw new Exception("Failed to parse index file at: " + filePath);
        }
        System.out.println(classNames.size());


        return classNames;
    }

    private HashMap<String, String> parseNewJavadoc(String filePath) {
        File inFile = new File(filePath);
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

        if ( container.size() == 0 ) {
            return null;
        }

        /* Extract all the links */
        Elements links = container.select("a");
        int numLinks = links.size();
        int count = 0;

        HashMap<String, String> classNames = new HashMap<String, String>();

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

    private HashMap<String, String> parseOldJavadoc(String filePath) {
        HashMap<String, String> classNames = new HashMap<String, String>();
        try {
            Document doc = Jsoup.parse(new File(filePath), "UTF-8");
            Elements classes = doc.select("html body table tbody tr td font.FrameItemFont a");

            if ( classes.size() == 0 ) {
                return null;
            }

            for ( Element aClass : classes ) {
                classNames.put(aClass.text(), aClass.attr("href"));
            }

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return classNames;
    }

    public static String extractPackage(String filePath) {
        String packageName = extractNewJavadocPackage(filePath);
        if ( packageName != null ) {
            return packageName;
        } else {
            return extractOldJavadocPackage(filePath);
        }
    }

    private static String extractNewJavadocPackage(String filePath) {
        Document doc;
        Elements packageName = null;
        try {
            doc = Jsoup.parse(new File(filePath), "UTF-8");
            packageName = doc.select("html body div.contentContainer ul.inheritance li ul.inheritance li ul.inheritance li ul.inheritance li ul.inheritance li");
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        if ( packageName.size() == 0 ) {
            return null;
        } else {
            return packageName.get(0).text();
        }
    }

    private static String extractOldJavadocPackage(String filePath) {
        Document doc;
        Elements packageName = null;
        try {
            doc = Jsoup.parse(new File(filePath), "UTF-8");
            packageName = doc.select("html body pre b");
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        if ( packageName.size() == 0 ) {
            return null;
        } else {
            return packageName.get(0).text();
        }
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
    public HashMap<String, String> execute() throws Exception {
        JavaDocParser parser = new JavaDocParser();
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

        System.out.println(ProfileManager.getInstance().getDocDir());
        HashMap<String, String> data = parser.parse(ProfileManager.getInstance().getDocDir() + "allclasses-noframe.html");

        setVisible(false);
        dispose();

        return data;
    }
}
