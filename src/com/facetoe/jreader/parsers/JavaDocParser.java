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

import org.apache.log4j.Logger;
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
 * Parses the Java documentation and packages it in a HashMap with the class name as key and the relative path as the value.
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
        HashMap<String, String> classNames = parseNewJavadoc(docFile);
        if ( classNames == null ) {
            classNames = parseOldJavadoc(docFile);
        }
        return classNames;
    }

    /**
     * This parses the new style javadocs that came out with Java7.
     *
     * @return the extracted class names and relative paths.
     */
    private HashMap<String, String> parseNewJavadoc(File docFile) {
        Document doc = null;
        try {
            doc = Jsoup.parse(docFile, "UTF-8");
        } catch ( IOException ex ) {
            log.error(ex.getMessage(), ex);
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
            classNames.put(link.text(), link.attr("href"));
        }
        fireEvent(ActionEvent.ACTION_LAST, "Complete", 100);
        return classNames;
    }

    /**
     * Parses the old style javadocs.
     *
     * @return the extracted class names and relative paths.
     */
    private HashMap<String, String> parseOldJavadoc(File docFile) {
        HashMap<String, String> classNames = new HashMap<String, String>();
        try {
            Document doc = Jsoup.parse(docFile, "UTF-8");
            Elements classes = doc.select("html body table tbody tr td font.FrameItemFont a");

            int numLinks;
            int count = 0;
            if ( classes.size() == 0 ) {
                return null;
            } else {
                numLinks = classes.size();
            }

            for ( Element link : classes ) {
                fireEvent(ActionEvent.ACTION_PERFORMED, link.text(), ( long ) ((count * 100.0f) / numLinks));
                count++;
                classNames.put(link.text(), link.attr("href"));
            }

        } catch ( IOException e ) {
            log.error(e.getMessage(), e);
        }
        return classNames;
    }

    /**
     * Notify the listeners of changes.
     *
     * @param eventType the event type
     * @param message   the message
     * @param progress  how far we have got
     */
    void fireEvent(int eventType, String message, long progress) {
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
