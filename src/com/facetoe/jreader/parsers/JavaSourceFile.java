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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 16/10/13
 * Time: 12:49 PM
 */

/**
 * This class represents and Java source code file. It contains all the methods, constructors, fields and nested classOrInterfaces
 * as well as methods for accessing them.
 */
public class JavaSourceFile {

    /**
     * All classOrInterfaces and interfaces..
     */
    private final ArrayList<AbstractJavaObject> fileContents = new ArrayList<AbstractJavaObject>();

    /**
     * Every declaration.
     */
    private final HashMap<String, AbstractJavaObject> allObjects = new HashMap<String, AbstractJavaObject>();

    public void addObject(AbstractJavaObject object) {
        fileContents.add(object);
    }

    public void extractAllObjectData() {
        for (AbstractJavaObject object : fileContents) {
            if (object instanceof JavaClassOrInterface) {
                extractClassOrInterfaceData((JavaClassOrInterface) object);

            } else if (object instanceof JavaEnum) {
                allObjects.putAll(((JavaEnum) object).getConstants());

            } else if (object instanceof JavaAnnotation) {
                JavaAnnotation annotation = (JavaAnnotation) object;
                allObjects.put(annotation.declaration, annotation);
            }
        }
    }

    /**
     * Populate the allObjects ArrayList with all the declarations.
     *
     * @param classOrInterface The <code>JavaClassOrInterface</code> to add.
     */
    private void extractClassOrInterfaceData(JavaClassOrInterface classOrInterface) {
        allObjects.putAll(classOrInterface.getConstructors());
        allObjects.putAll(classOrInterface.getMethods());
        allObjects.putAll(classOrInterface.getFields());
        allObjects.putAll(classOrInterface.getNestedClasses());
        allObjects.putAll(classOrInterface.getAnnotations());

        /* Add all the enums and the enum constants. */
        HashMap<String, JavaEnum> enums = classOrInterface.getEnums();
        for (String enumName : enums.keySet()) {
            allObjects.put(enumName, enums.get(enumName));
            allObjects.putAll(enums.get(enumName).getConstants());
        }

        HashMap<String, JavaClassOrInterface> nestedClasses = classOrInterface.getNestedClasses();
        /* For each nested class, recurse through it and each nested class it contains gathering all the declaration data */
        for (String className : nestedClasses.keySet()) {
            JavaClassOrInterface nestedClass = nestedClasses.get(className);
            extractClassOrInterfaceData(nestedClass);
        }
    }

    /**
     * Get the object associated with a declaration.
     *
     * @param itemDeclaration Short declaration of the desired object.
     * @return The Object associated with this declaration.
     */
    public AbstractJavaObject getObject(String itemDeclaration) {
        return allObjects.get(itemDeclaration);
    }

    /**
     * Get all the declarations short.
     *
     * @return The declarations.
     */
    public ArrayList<String> getAllDeclarations() {
        return new ArrayList<String>(allObjects.keySet());
    }

    /**
     * This is the top level object in the file. It could be a Class, Interface
     * or Annotation.
     *
     * @return The enclosing object.
     */
    public AbstractJavaObject getEnclosingObject() {
        return fileContents.size() > 0 ? fileContents.get(0) : null;
    }

    public ArrayList<AbstractJavaObject> getFileContents() {
        return fileContents;
    }
}

