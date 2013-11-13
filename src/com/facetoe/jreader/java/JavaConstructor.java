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
package com.facetoe.jreader.java;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 1/11/13
 * Time: 12:01 PM
 */

import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.stmt.BlockStmt;

/**
 * Represents a constructor.
 */
public class JavaConstructor extends AbstractJavaObject<ConstructorDeclaration> {

    public JavaConstructor(ConstructorDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
        type = CONSTRUCTOR;
    }

    @Override
    void extractFullDeclaration() {
        fullDeclaration = new ConstructorDeclaration(null,
                typeDeclaration.getModifiers(),
                null,
                typeDeclaration.getTypeParameters(),
                typeDeclaration.getName(),
                typeDeclaration.getParameters(),
                typeDeclaration.getThrows(),
                new BlockStmt()) // Add a new, empty block or it throws an error.
                .toString()
                .replace("{", "")
                .replace("}", "")
                .replaceAll("\\n", "");
    }

    @Override
    void extractDeclaration() {
        if ( typeDeclaration.getParameters() == null ) {
            declaration = typeDeclaration.getName() + "()";
        } else {
            declaration = (typeDeclaration.getName()
                    + typeDeclaration.getParameters())
                    .replace("[", "(")
                    .replace("]", ")");
        }
    }

    @Override
    public int getModifiers() {
        return typeDeclaration.getModifiers();
    }
}
