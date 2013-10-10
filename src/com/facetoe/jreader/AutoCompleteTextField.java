package com.facetoe.jreader;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Auto complete text field. Uses a Trie structure internally to add and remove words to be completed.
 * Only works with two words at a time.
 */
public class AutoCompleteTextField extends JTextField {

    private AutoCompleteTextFieldDocumentFilter docFilter;

    public AutoCompleteTextField() {
        docFilter = new AutoCompleteTextFieldDocumentFilter(this);

        /* Set the document filter to the custom filter */
        AbstractDocument doc = ( AbstractDocument ) getDocument();
        doc.setDocumentFilter(docFilter);

        /* This is so we can capture the Tab key */
        setFocusTraversalKeysEnabled(false);

        /* We need to setup a key binding to trigger a method on Tab press */
        int condition = JComponent.WHEN_FOCUSED;
        InputMap inputMap = getInputMap(condition);
        ActionMap actionMap = getActionMap();
        String tab = "tab";
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), tab);
        actionMap.put(tab, new AbstractAction() {

            /* When Tab is pressed cycle through the available words */
            @Override
            public void actionPerformed(ActionEvent arg0) {
                docFilter.showNextWord();
            }
        });
    }

    public void clearWordBuffer() {
        docFilter.clearWordBuffer();
    }

    public void removeWordsFromTrie(ArrayList<String> words) {
        docFilter.prefixTrie.removeWords(words);
    }

    public void removeWordFromTrie(String word) {
        docFilter.prefixTrie.removeWord(word);
    }

    public void addWordsToTrie(ArrayList<String> words) {
        docFilter.prefixTrie.addWords(words);
    }

    public void addWordToTrie(String word) {
        docFilter.prefixTrie.addWord(word);
    }
}


/**
 * The document filter for the auto-complete text field.
 * Handles the actual auto-completion.
 */
class AutoCompleteTextFieldDocumentFilter extends DocumentFilter {
    private JTextField editor = null;
    public final Trie prefixTrie;

    private final StringBuffer wordBuffer = new StringBuffer();

    private ArrayList<String> foundWords;
    private int currentWord = 0;
    private String chosenWord;

    private FilterBypass lastFB;
    private AttributeSet lastATTR;

    /* We pass the textField in so we can manipulate it internally */
    public AutoCompleteTextFieldDocumentFilter(JTextField ed) {
        editor = ed;
        /* Load the prefix tree */
        prefixTrie = new Trie();

    }

    @Override
    /* When a character is removed, remove it from the wordBuffer also */
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
        if ( offset < wordBuffer.length() && offset >= 0 ) {
            if ( length == 1 )
                wordBuffer.deleteCharAt(offset);
            else
                wordBuffer.delete(offset, length);
        }
    }

    @Override
    /* This never seems to get called... */
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        super.insertString(fb, offset, string, attr);
        System.out.println("Insert was called");
    }

    @Override
    /* Whenever a character is entered, update the prefix tree and display predicted text in the editor */
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

        if ( text.isEmpty() ) {
            super.replace(fb, offset, length, text, attrs);
            clearWordBuffer();
            return;
        }

        /* Keep track of these so we can tab through words later */
        lastFB = fb;
        lastATTR = attrs;

        /* Append the added text to our wordBuffer */
        wordBuffer.append(text);

        /* Get all the words that start with this prefix */
        foundWords = prefixTrie.getWordsForPrefix(wordBuffer.toString());

        if ( foundWords.isEmpty() ) {
            /* We didn't find anything, put the buffer contents back */
            super.replace(fb, 0, fb.getDocument().getLength(), wordBuffer.toString(), attrs);
        } else {

            /* Set the chosen word in case the user chooses it */
            chosenWord = foundWords.get(0) + " ";

            /* Clear the text currently in the outputBox */
            super.remove(fb, 0, fb.getDocument().getLength());

            /* Add the predicted text */
            super.replace(fb, 0, fb.getDocument().getLength(), foundWords.get(0), attrs);

            highlightPredictedText();
        }
    }

    /* Highlight the predicted text */
    private void highlightPredictedText() {
        editor.setCaretPosition(editor.getText().length());
        int n = wordBuffer.length();
        editor.moveCaretPosition(n);
    }

    public void debug() {
        System.out.println("WordBuff: " + wordBuffer);
    }

    public void clearWordBuffer() {
        wordBuffer.setLength(0);
    }

    /* Display the next word in the trie */
    public void showNextWord() {
        if ( !foundWords.isEmpty() ) {
            try {
                /* Update inputBox with the next word */
                super.replace(lastFB, 0, lastFB.getDocument().getLength(), nextWord(), lastATTR);
                highlightPredictedText();

            } catch ( BadLocationException ex ) {
                System.out.println("Error: " + ex.toString());
            } catch ( Exception ex ) {
                System.err.println("Error: Buffsize: " + wordBuffer.length() + " Buff: " + wordBuffer.toString() + " Editor: " + editor.getText().length());
            }
        }
    }

    /* Returns the next word in the foundWords array. If there are no more words begins from the first */
    private String nextWord() {
        currentWord++;
        if ( currentWord >= foundWords.size() ) {
            currentWord = 0;
        }
        return foundWords.get(currentWord);
    }

    /* Reverse sort the arrayList so the longest words are first */
    private void reverseSortByLength(ArrayList<String> wordsToSort) {
        Collections.sort(wordsToSort, new Comparator<String>() {
            public int compare(String x, String y) {
                if ( x.length() < y.length() ) {
                    return 1;
                } else if ( x.length() == y.length() ) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
    }
}
