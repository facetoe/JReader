package com.facetoe.jreader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class Parser {


    public static HashMap<String, String> parseLinks(String html) {
        HashMap<String, String> classes = new HashMap<String, String>();

        Document doc = Jsoup.parse(html, "UTF-8");

        Elements container = doc.select("div.indexContainer");
        Elements links = container.select("a");

        for ( Element link : links ) {
            classes.put(link.text(), link.attr("href"));
        }

        return classes;
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
}
