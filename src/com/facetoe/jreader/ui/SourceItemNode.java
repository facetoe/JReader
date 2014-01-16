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
package com.facetoe.jreader.ui;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 2/11/13
 * Time: 12:30 PM
 */

import com.facetoe.jreader.parsers.AbstractJavaObject;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class to hold item data.
 */
class SourceItemNode extends DefaultMutableTreeNode {
    private AbstractJavaObject javaObject;
    private final String title;
    private int type;

    public SourceItemNode(String title, AbstractJavaObject object) {
        this.javaObject = object;
        this.title = title;
        setUserObject(object);
    }

    public SourceItemNode(String title, int type) {
        this.title = title;
        this.type = type;
    }

    public int getType() {
        return javaObject == null ? type : javaObject.getType();
    }

    public int getModifiers() {
        return javaObject == null ? -1 : javaObject.getModifiers();
    }

    public String getTitle() {
        return title;
    }

    public AbstractJavaObject getJavaObject() {
        return javaObject;
    }

    @Override
    public String toString() {
        return javaObject == null ? title : javaObject.getDeclaration();
    }
}