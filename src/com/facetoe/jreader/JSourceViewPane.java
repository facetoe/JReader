package com.facetoe.jreader;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.*;

import jsyntaxpane.DefaultSyntaxKit;

public class JSourceViewPane extends Container {

    public JSourceViewPane() {
        JEditorPane editorPane = new JEditorPane();
        JScrollPane scrollPane = new JScrollPane(editorPane);

        DefaultSyntaxKit.initKit();
        setFont(new Font("Segoe Script", Font.PLAIN, 30));
        editorPane.setLayout(new BorderLayout());

        editorPane.setContentType("text/java");
        StringBuilder builder = new StringBuilder();

        try {
            FileInputStream input = new FileInputStream("/home/facetoe/tmp/oldParse.java");

            int ch;
            while((ch = input.read()) != -1) {
               builder.append((char)ch);
            }

        } catch ( FileNotFoundException ex ) {
            ex.printStackTrace();
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

        editorPane.setText(builder.toString());
        add(scrollPane, BorderLayout.CENTER);
    }
}