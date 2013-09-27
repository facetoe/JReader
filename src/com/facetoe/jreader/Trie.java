package com.facetoe.jreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * My implementation of the Trie data structure
 */
public class Trie {
    private final Map<Character, TrieNode> root = new HashMap<Character, TrieNode>();

    public Trie() {

    }

    public Trie(File wordList) throws IOException {
        Scanner s;
        try {
            s = new Scanner(wordList);
        } catch ( IOException ex ) {
            throw new IOException("Unable to locate file: " + wordList.toString());
        }

        while ( s.hasNext() ) {
            addWord(s.next());
        }
        s.close();
    }

    public Trie(ArrayList<String> words) {
        for ( String word : words ) {
            addWord(word);
        }
    }

    /**
     * Add a word to the trie
     *
     * @param word
     */
    public void addWord(String word) {
        if ( !word.isEmpty() ) {
            addWord(word.toCharArray());
        }
    }

    /**
     * Internal method to add a word to the trie
     *
     * @param word
     */
    private void addWord(char[] word) {
        TrieNode currentNode;

        /* If we don't already have a word that starts with this char, add the char */
        if ( !root.containsKey(word[0]) ) {
            currentNode = new TrieNode(word[0]);
            root.put(word[0], currentNode);

        } else {
            /* Otherwise get the node that contains char */
            currentNode = root.get(word[0]);
        }

        for ( int i = 1; i < word.length; i++ ) {
            /* If a child has this char, walk down to the next level */
            if ( currentNode.containsChar(word[i]) ) {
                currentNode = currentNode.getNode(word[i]);
            } else {
                /* Otherwise add the char */
                currentNode.addChild(word[i]);
                currentNode = currentNode.getNode(word[i]);
            }
        }

        /* We are at the end of the word */
        currentNode.isWord = true;
    }

    /**
     * Remove words from the Trie
     *
     * @param words
     */
    public void removeWords(ArrayList<String> words) {
        for ( String word : words ) {
            removeWord(word);
        }
    }

    /**
     * Add words to the Trie
     *
     * @param words
     */
    public void addWords(ArrayList<String> words) {
        for ( String word : words ) {
            addWord(word);
        }
    }

    /**
     * Removes a word from the com.myAdventureGame.Trie. Note, it isn't actually removed, the word flag is just set to false.
     *
     * @param word to be removed
     */
    public void removeWord(String word) {
        if ( word.isEmpty() ) {
            return;
        }

        TrieNode node = getNodeByPrefix(word);
        if ( node != null ) {
            node.isWord = false;
        }
    }

    /**
     * Return all words that have prefix
     *
     * @param prefix
     * @return ArrayList containing the found words
     */
    public ArrayList<String> getWordsForPrefix(String prefix) {
        ArrayList<String> words = new ArrayList<String>();

        /* Can't do much with an empty string */
        if ( prefix.length() == 0 ) {
            return words;
        }

        TrieNode root = getNodeByPrefix(prefix);

        /* If root is null there are no words with this prefix, return empty ArrayList */
        if ( root == null ) {
            return words;
        }

        getWordsForPrefix(prefix, "", root, words);
        return words;
    }

    /**
     * Return all words that have prefix
     * Internal method for returning words with prefix
     */
    private ArrayList<String> getWordsForPrefix(String prefix, String word, TrieNode root, ArrayList<String> foundWords) {
        /* If we've got a word, add it to the list */
        if ( root.isWord ) {
            foundWords.add(prefix + word);
        }

        /* Get all the children for this node */
        ArrayList<TrieNode> children = root.getChildren();

        /* Recurse through each child gathering all the words */
        for ( TrieNode child : children ) {
            getWordsForPrefix(prefix, word + child.getChar(), child, foundWords);
        }
        return foundWords;
    }

    /**
     * Return the node at the end of the prefix
     *
     * @param prefix
     * @return com.myAdventureGame.TrieNode or null if nothing found
     */
    private TrieNode getNodeByPrefix(String prefix) {
        char[] pre = prefix.toCharArray();
        TrieNode currentNode;

        /* If root doesn't have the char, return null */
        if ( root.get(pre[0]) == null ) {
            return null;
        } else {

            /* Otherwise get the node that contains char */
            currentNode = root.get(pre[0]);
        }

        for ( int i = 1; i < pre.length; i++ ) {
            /* If we don't have the char, return null */
            if ( !currentNode.containsChar(pre[i]) ) {
                return null;
            } else {
                /* Otherwise keep walking the trie */
                currentNode = currentNode.getNode(pre[i]);
            }
        }

        /* We are at the end of the prefix, return the node */
        return currentNode;
    }

}

/**
 * This class defines a node for the com.myAdventureGame.Trie
 */
class TrieNode {
    private final Map<Character, TrieNode> children = new HashMap<Character, TrieNode>();
    public boolean isWord = false;
    private final Character ch;

    public TrieNode(char character) {
        ch = character;
    }

    /**
     * Add a child to this node
     *
     * @param character
     */
    public void addChild(char character) {
        TrieNode node = new TrieNode(character);
        children.put(character, node);
    }

    /**
     * Return a child node by character
     *
     * @param character
     * @return com.myAdventureGame.TrieNode or null if no node has this character
     */
    public TrieNode getNode(char character) {
        return children.get(character);
    }

    /**
     * Whether this node's children contain character
     *
     * @param character
     * @return Boolean
     */
    public boolean containsChar(char character) {
        return children.containsKey(character);
    }

    /**
     * Return all the children for this node
     *
     * @return Returns an ArrayList containing all the children for this node
     */
    public ArrayList<TrieNode> getChildren() {
        ArrayList<TrieNode> trieNodes = new ArrayList<TrieNode>();
        for ( TrieNode node : children.values() ) {
            trieNodes.add(node);
        }
        return trieNodes;
    }

    /**
     * Return this node's character
     *
     * @return char
     */
    public Character getChar() {
        return ch;
    }
}
