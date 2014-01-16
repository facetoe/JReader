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
 * Time: 12:01 PM
 */

import japa.parser.ast.body.EnumConstantDeclaration;
import japa.parser.ast.body.EnumDeclaration;

import java.util.HashMap;

/**
 * Represents an enum.
 */
public class JavaEnum extends AbstractJavaObject<EnumDeclaration> {
    private HashMap<String, JavaEnumConstantDeclaration> constants = new HashMap<String, JavaEnumConstantDeclaration>();

    public JavaEnum(EnumDeclaration typeDec) {
        super(typeDec);
        extractFullDeclaration();
        extractDeclaration();
        extractConstants();
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
        type = ENUM;
    }

    private void extractConstants() {
        for ( EnumConstantDeclaration constant : typeDeclaration.getEntries() ) {
            constants.put(constant.getName(), new JavaEnumConstantDeclaration(constant));
        }
    }

    @Override
    void extractFullDeclaration() {
        fullDeclaration = new EnumDeclaration(typeDeclaration.getModifiers(),
                typeDeclaration.getName()).toString()
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

    public HashMap<String, JavaEnumConstantDeclaration> getConstants() {
        return constants;
    }

    public JavaEnumConstantDeclaration getConstant(String name) {
        return constants.get(name);
    }
}

class JavaEnumConstantDeclaration extends AbstractJavaObject<EnumConstantDeclaration> {

    public JavaEnumConstantDeclaration(EnumConstantDeclaration typeDec) {
        super(typeDec);
        extractDeclaration();
        extractFullDeclaration();
        beginColumn = typeDec.getBeginColumn();
        endColumn = typeDec.getEndColumn();
        beginLine = typeDec.getBeginLine();
        endLine = typeDec.getEndLine();
        type = ENUM;
    }

    @Override
    void extractFullDeclaration() {
        fullDeclaration = typeDeclaration.getName();
    }

    @Override
    void extractDeclaration() {
        declaration = typeDeclaration.getName();
    }

    @Override
    public int getModifiers() {
        return -1;  // No modifiers for constants.
    }
}