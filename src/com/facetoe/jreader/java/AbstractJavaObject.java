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
public abstract class AbstractJavaObject<T> {

    public static final int CLASS = 0;
    public static final int INTERFACE = 1;
    public static final int CONSTRUCTOR = 2;
    public static final int METHOD = 3;
    public static final int FIELD = 4;
    public static final int ENUM = 5;
    public static final int ANNOTATION = 6;

    /**
     * What sort of object this is.
     */
    protected int type;

    // The declaration
    final T typeDeclaration;

    /**
     * Full declaration including modifiers
     */
    protected String fullDeclaration;

    /**
     * Integer representing the modifiers for this object.
     */
    protected int modifiers;

    /**
     * Just the name and parameters
     */
    protected String declaration;

    /**
     * Line in the source file where this item begins
     */
    protected int beginLine;

    /**
     * Line in the source file where this item ends
     * Note: this includes the entire block, not just declarations
     */
    protected int endLine;

    /**
     * Column where this declaration begins
     */
    protected int beginColumn;

    /**
     * Column where this declaration ends.
     */
    protected int endColumn;

    /**
     * Constructor.
     *
     * @param t The type declaration.
     */
    public AbstractJavaObject(T t) {
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


    abstract public int getModifiers();

    /**
     * Get the full declaration.
     *
     * @return The declaration.
     */
    public String getFullDeclaration() {
        return fullDeclaration;
    }

    /**
     * Get the short declaration.
     *
     * @return The short declaration.
     */
    public String getDeclaration() {
        return declaration;
    }

    public int getType() {
        return type;
    }

    /**
     * Get the line where this declaration begins
     *
     * @return The start line.
     */
    public int getBeginLine() {
        return beginLine;
    }

    /**
     * Get the line where this declaration ends
     *
     * @return The end line.
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Get the column where this declaration begins
     *
     * @return the start column
     */
    public int getBeginColumn() {
        return beginColumn;
    }

    /**
     * Get the column where this declaration ends
     *
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








