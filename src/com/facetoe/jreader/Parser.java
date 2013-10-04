package com.facetoe.jreader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class Parser extends SwingWorker<JavaClassData, String> {

    public HashMap<String, String> classes = new HashMap<String, String>();
    public HashMap<String, HashMap<String, String>> allClassData = new HashMap<String, HashMap<String, String>>();
    private String basePath = Config.getEntry("basePath");
    private String classFile = Config.getEntry("classFile");
    private JLabel classLabel;
    private JFrame jFrame;

    public Parser(JLabel label, JFrame frame) {
        classLabel = label;
        jFrame = frame;
    }

    public void parse(String path) {

        File inFile = new File(path);
        Document doc = null;
        try {
            doc = Jsoup.parse(inFile, "UTF-8");
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

        Elements container = doc.select("div.indexContainer");
        Elements links = container.select("a");
        int numLinks = links.size();
        int count = 0;
        for ( Element link : links ) {
            publish(link.text());
            count++;

            setProgress(( int ) ((count * 100.0f) / numLinks));
            classes.put(link.text(), link.attr("href"));
            parseClassFile(basePath + link.attr("href"), link.text());
        }
    }

    public void parseClassFile(String classPath, String className) {
        File inFile = new File(classPath);
        Document doc = null;
        HashMap<String, String> data = new HashMap<String, String>();

        try {
            doc = Jsoup.parse(inFile, "UTF-8");
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

        Elements elements = doc.select("div.summary ul.blockList");
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
    }

    @Override
    protected void process(List<String> chunks) {
        classLabel.setText(chunks.get(chunks.size() - 1));
    }

    @Override
    protected void done() {
        classLabel.setText("Completed");
        jFrame.dispose();
    }

    @Override
    protected JavaClassData doInBackground() throws Exception {
        parse(basePath + classFile);
        JavaClassData data = new JavaClassData();
        data.setClasses(classes);
        data.setAllClassData(allClassData);
        return data;
    }
}

class MyProgressListener implements PropertyChangeListener {
    JProgressBar progressBar;
    JFrame windowFrame;

    public MyProgressListener(JProgressBar progressBar, JFrame frame) {
        this.progressBar = progressBar;
        windowFrame = frame;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if ( event.getPropertyName().equalsIgnoreCase("progress") ) {
            progressBar.setIndeterminate(false);
            progressBar.setValue(( Integer ) event.getNewValue());

        }
    }
}

class JavaClassData implements Serializable {
    private HashMap<String, String> classes;
    private HashMap<String, HashMap<String, String>> allClassData;

    HashMap<String, String> getClasses() {
        return classes;
    }

    void setClasses(HashMap<String, String> classes) {
        this.classes = classes;
    }

    HashMap<String, HashMap<String, String>> getAllClassData() {
        return allClassData;
    }

    void setAllClassData(HashMap<String, HashMap<String, String>> allClassData) {
        this.allClassData = allClassData;
    }
}

class ParserWindow extends JFrame {
    JPanel panel = new JPanel();
    JLabel lblProgress = new JLabel();
    JLabel lblTitle = new JLabel("Parsing Class: ");
    JProgressBar progressBar = new JProgressBar();
    public Parser parser;

    public ParserWindow() {
        panel.setLayout(new BorderLayout(10, 10));
        panel.add(lblTitle, BorderLayout.WEST);
        panel.add(lblProgress, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.NORTH);
        panel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createLineBorder(Color.BLACK)));
        setTitle("JReader");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        } catch ( InstantiationException e ) {
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();
        } catch ( UnsupportedLookAndFeelException e ) {
            e.printStackTrace();
        }

        getContentPane().add(panel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(450, 100));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        parser = new Parser(lblProgress, this);
        parser.addPropertyChangeListener(new MyProgressListener(progressBar, this));
        parser.execute();
    }

    public static void main(String[] args) {
        final ParserWindow parserWindow = new ParserWindow();
        try {
            JavaClassData data = parserWindow.parser.get();

        } catch ( InterruptedException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch ( ExecutionException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}
