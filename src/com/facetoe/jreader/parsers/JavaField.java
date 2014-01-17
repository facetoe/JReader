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
 * Date: 1/11/13
 * Time: 12:02 PM
 */

import japa.parser.ast.body.FieldDeclaration;

/**
 * Represents a field.
 */
class JavaField extends AbstractJavaObject<FieldDeclaration> {

    public JavaField(FieldDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
        type = FIELD;
    }

    @Override
    void extractFullDeclaration() {
        String field = new FieldDeclaration(typeDeclaration.getModifiers(),
                typeDeclaration.getType(),
                typeDeclaration.getVariables())
                .toString()
                .replace(";", "");

        if (field.contains("=")) {
            field = field.substring(0, field.indexOf(" ="));
        }
        fullDeclaration = field.trim();
        declaration = field.substring(field.lastIndexOf(" ", field.length())).trim();
    }

    @Override
    void extractDeclaration() {
    }

    @Override
    public int getModifiers() {
        return typeDeclaration.getModifiers();
    }
}