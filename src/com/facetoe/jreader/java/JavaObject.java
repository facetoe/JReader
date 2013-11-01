package com.facetoe.jreader.java;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 16/10/13
 * Time: 12:41 PM
 */

/**
 * Abstract class to represent different declarations.
 *
 * @param <T>
 */
public abstract class JavaObject<T> {

    // The declaration
    final T typeDeclaration;

    /**
     * Full declaration including modifiers
     */
    public String fullDeclaration;

    /**
     * Just the name and parameters
     */
    public String declaration;

    /**
     * Line in the source file where this item begins
     */
    public int beginLine;

    /**
     * Line in the source file where this item ends
     * Note: this includes the entire block, not just declarations
     */
    public int endLine;

    /**
     * Column where this declaration begins
     */
    public int beginColumn;

    /**
     * Column where this declaration ends.
     */
    public int endColumn;

    /**
     * Constructor.
     * @param t The type declaration.
     */
    public JavaObject(T t) {
        typeDeclaration = t;
        extractDeclaration();
        extractFullDeclaration();
    }

    /**
     * Abstract method for extracting full declarations.
     */
    abstract void extractFullDeclaration();

    /**
     * Abstract method for extracting declarations.
     */
    abstract void extractDeclaration();

    /**
     * Get the full declaration.
     * @return The declaration.
     */
    public String getFullDeclaration() {
        return fullDeclaration;
    }

    /**
     * Get the short declaration.
     * @return The short declaration.
     */
    public String getDeclaration() {
        return declaration;
    }

    /**
     * Get the line where this declaration begins
     * @return The start line.
     */
    public int getBeginLine() {
        return beginLine;
    }

    /**
     * Get the line where this declaration ends
     * @return The end line.
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Get the column where this declaration begins
     * @return the start column
     */
    public int getBeginColumn() {
        return beginColumn;
    }

    /**
     * Get the column where this declaration ends
     * @return the end column.
     */
    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public String toString() {
        return fullDeclaration;
    }
}








