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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

class JavaObject implements Serializable {
    private String fullObjName;
    private String objName;
    private String path;

    private HashMap<String, String> objectItems = new HashMap<String, String>();

    public JavaObject(String objName, String fullObjName, String path) {
        this.fullObjName = fullObjName;
        this.objName = objName;
        this.path = path;
    }

    public void addItem(String objName, String fullObjName) {
        objectItems.put(objName, fullObjName);
    }

    HashMap<String, String> getObjectItems() {
        return objectItems;
    }

    public ArrayList<String> getObjectItemsShort() {
        return new ArrayList<String>(objectItems.keySet());
    }

    public ArrayList<String> getObjectItemsLong() {
        return new ArrayList<String>(objectItems.values());
    }

    public String getObjectItemLong(String itemName) {
        return objectItems.get(itemName);
    }

    String getFullObjName() {
        return fullObjName;
    }

    String getObjName() {
        return objName;
    }

    String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return fullObjName;
    }
}


/**
 * Parses the Java documentation and packages it in a HashMap with the class name as key and a JavaObject as the value.
 */
public class JavaDocParser {

    private HashMap<String, HashMap<String, String>> allClassData = new HashMap<String, HashMap<String, String>>();

    /* The base path for the Java docs: /dir/dir/dir/docs/api/ */
    private String basePath;

    /* HashMap containing the class name as the key and a JavaObject object as the value */
    private HashMap<String, JavaObject> objectData = new HashMap<String, JavaObject>();

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
    public HashMap<String, JavaObject> parse(String indexFile) {

        File inFile = new File(basePath + indexFile);
        Document doc = null;
        try {
            doc = Jsoup.parse(inFile, "UTF-8");
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

        /* The indexContainer contains links to all the Java classes */
        Elements container = null;
        try {
            container = doc.select("div.indexContainer");
        } catch ( NullPointerException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        /* Extract all the links */
        Elements links = container.select("a");
        int numLinks = links.size();
        int count = 0;

        /* Loop over all the links, extracting the name and parsing each class file */
        for ( Element link : links ) {
            fireEvent(ActionEvent.ACTION_PERFORMED, link.text(), ( long ) ((count * 100.0f) / numLinks));
            count++;
            String relativePath = link.attr("href");
            parseClassFile(relativePath, basePath + relativePath, link.text());
        }
        fireEvent(ActionEvent.ACTION_LAST, "Complete", 100);
        return objectData;
    }

    /**
     * Parses each class file.
     *
     * @param classPath path to the file to be parsed.
     * @param className the file to be parsed.
     */
    public void parseClassFile(String relativePath, String classPath, String className) {
        File inFile = new File(classPath);
        Document doc = null;

        try {
            doc = Jsoup.parse(inFile, "UTF-8");
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        String[] nameParts = new String[0];
        try {
            nameParts = doc.select("html body div.contentContainer div.description ul.blockList li.blockList pre").get(0).text().split("\n");
        } catch ( NullPointerException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        /**
         * Sometimes before the object name there are annotations like @SomeImportantThing.
         * Keep looping until we find the actual name.
         */
        String fullObjName = null;
        if ( !nameParts[0].startsWith("@") ) {
            fullObjName = nameParts[0];
        } else {
            for ( String namePart : nameParts ) {
                if ( !namePart.startsWith("@") ) {
                    fullObjName = namePart;
                    break;
                }
            }
        }

        //TODO remove the exit and handle the error better
        if ( fullObjName == null ) {
            System.err.println("NAME WAS NULL");
            System.exit(1);
            return;
        }

        JavaObject object = new JavaObject(className, fullObjName, relativePath);

        Elements classData = doc.select("html body div.contentContainer div.summary ul.blockList li.blockList ul.blockList li.blockList table.overviewSummary tbody tr");

        for ( Element item : classData ) {
            String itemSigFull = item.select("code").text();

            if ( !itemSigFull.isEmpty() ) {

                // Is a method or constructor
                if ( itemSigFull.contains("(") ) {
                    itemSigFull = rearrangeBrackets(itemSigFull);

                    String methodSig;

                    /* Sometimes there is extra information concerning deprecated methods. We don't want that,
                     * so remove it otherwise we get messed up search functionality in JSourcePane. */
                    if ( item.getElementsByTag("i").size() > 0 ) {
                        String annoyingAddition = item.getElementsByTag("i").select("code").text();
                        itemSigFull = itemSigFull.replace(annoyingAddition, "");
                        methodSig = extractMethodName(itemSigFull);

                    } else {
                        methodSig = extractMethodName(itemSigFull);
                    }
                    object.addItem(methodSig, itemSigFull);

                    // Is a nested class
                } else if ( itemSigFull.contains(".") ) {
                    String nestedClassName = item.getElementsByClass("colLast").select("a").text();

                    int classBegin = nestedClassName.lastIndexOf(".") + 1;
                    int classEnd = nestedClassName.length();

                    nestedClassName = nestedClassName.substring(classBegin, classEnd);
                    String signature = item.getElementsByClass("colFirst").text();
                    String fullNestedClassSig = (signature + " " + nestedClassName).replaceAll("\u00A0", " ").replaceAll("\\s+", " ");

                    object.addItem(nestedClassName, fullNestedClassSig);

                    // Is a field
                } else {

                    //TOO figure out how to determine whether it is marked final or not. As it is the search fails
                    // fails if it is marked as final.
//                    String[] fieldSigParts = itemSigFull.split("^(\\w+ )+");
//                    String fieldSig = fieldSigParts[fieldSigParts.length - 1];
//                    object.addItem(fieldSig, itemSigFull);
                }
            }
        }

        objectData.put(className, object);
    }

    /**
     * Rearrange the brackets to match the Java source code. In the docs they look like:
     * String(byte[] ascii, int hibyte, int offset, int count)
     * But in the source they appear as:
     * String(byte ascii[], int hibyte, int offset, int count)
     * <p/>
     * This convoluted method shifts the brackets from the type to the variable.
     *
     * @param str to process
     * @return the brackets rearranged
     */
    private static String rearrangeBrackets(String str) {
        String params = str.substring(str.indexOf("(") + 1, str.lastIndexOf(")"));
        String[] parts = params.split("(?=\\s)");
        StringBuilder builder = new StringBuilder();
        builder.append(str.substring(0, str.indexOf("(") + 1));
        for ( int i = 0; i < parts.length; i++ ) {
            if ( parts[i].contains("[") ) {
                String tmp[] = parts[i].split("\\[\\]");
                if ( tmp.length > 1 ) {
                    if ( tmp[1].contains(",") ) {
                        builder.append(tmp[0]);
                        builder.append((tmp[1].replace(",", "") + "[]" + ","));
                    } else {
                        builder.append(tmp[0]);
                        builder.append(tmp[1] + "[]");
                    }
                } else {
                    builder.append(parts[i]);
                }
            } else {
                builder.append(parts[i]);
            }
        }
        builder.append(")");
        return builder.toString().replace("\u00A0", " ");
    }

    private String extractMethodName(String methodSig) {
        boolean inParams = false;
        boolean inMethodName = false;
        StringBuilder builder = new StringBuilder();

        for ( int i = methodSig.length() - 1; i >= 0; i-- ) {
            if ( methodSig.charAt(i) == ')' ) {
                inParams = true;
            } else if ( methodSig.charAt(i) == '(' ) {
                inParams = false;
                inMethodName = true;
            } else if ( Character.isSpaceChar(methodSig.charAt(i)) && inMethodName ) {
                break;
            }

            if ( inParams || inMethodName ) {
                builder.append(methodSig.charAt(i));
            }
        }
        return builder.reverse().toString();
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

    public HashMap<String, JavaObject> getObjectData() {
        return objectData;
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

    /* This doesn't do anything yet */
    JButton btnCancel = new JButton("Cancel");
    JLabel lblTitle = new JLabel("Loading...");

    /* Display progress */
    JProgressBar progressBar = new JProgressBar();

    public ProgressWindow() {

        //TODO figure out what to do with the button.
//        btnCancel.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//            }
//        });

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
class ParserProgressWindow extends ProgressWindow<HashMap<String, JavaObject>> {
    @Override
    public HashMap<String, JavaObject> execute() {
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

        HashMap<String, JavaObject> data = parser.parse("allclasses-noframe.html");
        setVisible(false);
        dispose();

        return data;
    }

}
