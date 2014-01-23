package com.facetoe.jreader.ui;

import com.facetoe.jreader.githubapi.apiobjects.Match;
import com.facetoe.jreader.githubapi.apiobjects.TextMatch;
import com.facetoe.jreader.helpers.Utilities;
import com.facetoe.jreader.ui.OnTextMatchItemClickedListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * JReader
 * Created by facetoe on 22/01/14.
 */
class TextMatchItem extends JPanel {
    private TextMatch textMatch;
    private OnTextMatchItemClickedListener listener;

    public TextMatchItem(TextMatch textMatch) {
        this.textMatch = textMatch;
        createLayout();
    }

    private void createLayout() {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new EmptyBorder(0, 0, 5, 5), BorderFactory.createLineBorder(Color.BLACK, 1)));
        JPanel topPanel = new JPanel(new BorderLayout());

        JButton button = createHtmlButton();
        topPanel.add(button, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);
        add(createCodeArea(), BorderLayout.CENTER);
    }

    private RSyntaxTextArea createCodeArea() {
        RSyntaxTextArea codeArea = new RSyntaxTextArea();
        Theme theme = Utilities.loadTheme();
        theme.apply(codeArea);

        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(false);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setEditable(false);
        codeArea.setText(textMatch.getFragment());
        highlightMatches(codeArea);
        return codeArea;
    }

    private void highlightMatches(RSyntaxTextArea codeArea) {
        Match[]  matches = textMatch.getMatches();
        for (Match match : matches) {
            int[] indices = match.getIndices();
            if(indices.length < 2) {
                return;
            }
            Highlighter highlighter = codeArea.getHighlighter();
            try {
                highlighter.addHighlight(indices[0], indices[1], DefaultHighlighter.DefaultPainter);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private JButton createHtmlButton() {
        String url = textMatch.getObject_url();
        String fileName = Utilities.extractFileName(url);
        String html = "<HTML><a href=\"\">" + fileName + "</a></HTML>";
        JButton button = new JButton(html);

        button.setToolTipText("Click to view " + fileName);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.textMatchItemClicked(textMatch.getObject_url());
                }
            }
        });
        return button;
    }

    public void setOnTextMatchItemClickedListener(OnTextMatchItemClickedListener listener) {
        this.listener = listener;
    }
}
