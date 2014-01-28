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
package com.facetoe.jreader.parsers;

import com.facetoe.jreader.helpers.Util;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parses the Java documentation and packages it in a HashMap with the class name as key
 * and the relative path as the value.
 */
public class JavaDocParser {
    private static final Logger log = Logger.getLogger(JavaDocParser.class);

    /* Action listeners for this parser */
    private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    /**
     * Parses the Java docs by extracting the class names and relative URL's from the
     * allclasses-noframe.html file.
     *
     * @param docFile should point to allclasses-noframe.html
     */
    public HashMap<String, String> parse(File docFile) {
        assert docFile.getName().equals("allclasses-noframe.html");
        return parseInternal(docFile);
    }

    private HashMap<String, String> parseInternal(File docFile) {
        try {
            Document doc = Jsoup.parse(docFile, "UTF-8");
            if (isNewJavaDocFile(docFile)) {
                return parseNewJavadoc(doc);
            } else {
                return parseOldJavadoc(doc);
            }
        } catch (IOException e) {
            log.error(e);
        }
        return new HashMap<String, String>();
    }

    /*
     I'm not sure if this is a reliable method of determining what sort of
     javadoc file it is, but it seems to work so far. Basically the old javadocs
     use uppercase HTML while the new ones use lowercase HTML.
     */
    private boolean isNewJavaDocFile(File docFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(docFile));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("<head>")) {
                return true;
            } else if (line.contains("<SCRIPT")) { // If we this far into the file then it's the oldstyle.
                return false;
            }
        }
        return false;
    }

    // Parses the new style javadocs that came out with Java7.
    private HashMap<String, String> parseNewJavadoc(Document doc) {
        Elements container;
        container = doc.select("div.indexContainer");
        Elements links = container.select("a");
        return processLinks(links);
    }

    // Parses the old style javadocs.
    private HashMap<String, String> parseOldJavadoc(Document doc) {
        Elements links = doc.select("html body table tbody tr td font.FrameItemFont a");
        return processLinks(links);
    }

    private HashMap<String, String> processLinks(Elements links) {
        int numLinks = links.size();
        int cnt = 0;
        HashMap<String, String> classNames = new HashMap<String, String>();
        for (Element link : links) {
            fireEvent(ActionEvent.ACTION_PERFORMED, link.text(), (int)Util.percent(numLinks, ++cnt));
            classNames.put(link.text(), link.attr("href"));
        }
        return classNames;
    }

    void fireEvent(int eventType, String message, long progress) {
        ActionEvent event = new ActionEvent(this, eventType, message, progress, 0);
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }
}
