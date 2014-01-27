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
    int type;

    // The declaration
    final T typeDeclaration;

    /**
     * Full declaration including modifiers
     */
    String fullDeclaration;

    /**
     * Integer representing the modifiers for this object.
     */
    protected int modifiers;

    /**
     * Just the name and parameters
     */
    String declaration;

    /**
     * Line in the source file where this item begins
     */
    int beginLine;

    /**
     * Line in the source file where this item ends
     * Note: this includes the entire block, not just declarations
     */
    int endLine;

    /**
     * Column where this declaration begins
     */
    int beginColumn;

    /**
     * Column where this declaration ends.
     */
    int endColumn;

    /**
     * Constructor.
     *
     * @param t The type declaration.
     */
    AbstractJavaObject(T t) {
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


    public abstract int getModifiers();

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

    @Override
    public String toString() {
        return fullDeclaration;
    }
}








