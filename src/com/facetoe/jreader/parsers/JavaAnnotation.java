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

import japa.parser.ast.body.AnnotationDeclaration;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 3/11/13
 * Time: 10:35 AM
 */
public class JavaAnnotation extends AbstractJavaObject<AnnotationDeclaration> {

//    private ArrayList<>

    public JavaAnnotation(AnnotationDeclaration typeDec) {
        super(typeDec);
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
        type = ANNOTATION;
    }

    @Override
    void extractFullDeclaration() {
        fullDeclaration = new AnnotationDeclaration(typeDeclaration.getModifiers(), typeDeclaration.getName())
                .toString()
                .replace("{", "")
                .replace("}", "")
                .replaceAll("\\n", "");
    }

    @Override
    void extractDeclaration() {
        declaration = typeDeclaration.getName();
    }

    @Override
    public int getModifiers() {
        return typeDeclaration.getModifiers();
    }
}
