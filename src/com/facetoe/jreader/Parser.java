package com.facetoe.jreader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

class JavaObject {
    private String name;
    private String description;
    private String type;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }
}

class JavaClass extends JavaObject {
    private String[] subInterfaces;

    String[] getSubInterfaces() {
        return subInterfaces;
    }

    void setSubInterfaces(String[] subInterfaces) {
        this.subInterfaces = subInterfaces;
    }
}

class JavaInterface extends JavaObject {
    private String[] subInterfaces;
    private String[] implementingClasses;

    String[] getSubInterfaces() {
        return subInterfaces;
    }

    void setSubInterfaces(String[] subInterfaces) {
        this.subInterfaces = subInterfaces;
    }

    String[] getImplementingClasses() {
        return implementingClasses;
    }

    void setImplementingClasses(String[] implementingClasses) {
        this.implementingClasses = implementingClasses;
    }
}


public class Parser {

    Document doc;

    JavaObject parse(File classFile) {
        parseJsoup(classFile);

        JavaObject jObject = new JavaObject();
        Elements name = doc.getElementsByClass("title");
        String[] nameParts = name.text().split(" ");
        if ( nameParts[0].equalsIgnoreCase("interface") ) {
            return setInterface(doc);
        }
        return jObject;
    }

    JavaInterface setInterface(Document doc) {
        JavaInterface jInterface = new JavaInterface();
        Elements name = doc.getElementsByClass("title");
        String[] nameParts = name.text().split(" ");

        jInterface.setType(nameParts[0]);
        jInterface.setName(nameParts[1]);
        jInterface.setDescription(getDescription());
        jInterface.setSubInterfaces(getSubInterfaces());
        return jInterface;
    }

    private void parseJsoup(File classFile) {
        try {
            doc = Jsoup.parse(classFile, "UTF-8");
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }

    public String getDescription() {
        Element description = doc.getElementsByClass("description").first();
        return description.getElementsByClass("block").text();
    }

    public String[] getSubInterfaces() {
        Element element = doc.getElementsByClass("description").first();
        Element els = element.select(":contains(All Known Subinterfaces").first();
        System.out.println(els);
        return els.text().split(", ");
    }

    public String[] getImplementingClasses() {
        return new String[2];
    }

    private void print(JavaClass jClass) {
        System.out.println(jClass.getType() + " " + jClass.getName());

        System.out.println(jClass.getDescription());
    }
    

    private void print(JavaInterface jInterface) {
        System.out.println(jInterface.getType() + " " + jInterface.getName());
        System.out.println(jInterface.getDescription());

        if(jInterface.getSubInterfaces() != null) {
            System.out.println("SubInterfaces:");
            for ( String s : jInterface.getSubInterfaces() ) {
                System.out.println(s);
            }
        }

        if(jInterface.getImplementingClasses() != null) {
            System.out.println("Implementing Classes:");
            for ( String s : jInterface.getImplementingClasses() ) {
                System.out.println(s);
            }
        }
    }

    public void print(JavaObject jObject) {
        if ( jObject.getType().equalsIgnoreCase("Interface") ) {
            print((JavaInterface)jObject);
        } else if (jObject.getType().equalsIgnoreCase("Class")) {
            print((JavaClass)jObject);
        } else {
            System.out.println("Not an interface or class");
        }
    }

    public static void main(String[] args) {
        File input = new File("/home/facetoe/tmp/docs/api/javax/accessibility/AccessibleText.html");
//
//        Parser parser = new Parser();
//        JavaObject obj = parser.parse(input);
//        parser.print(obj);
//
//        System.exit(1);

        try {
            Document doc = Jsoup.parse(input, "UTF-8");

            Element element = doc.getElementsByClass("blocklist").first();
            Elements elements = element.getElementsByTag("dl");

            for ( Element anElement : elements ) {
                if(anElement.select(":contains(All Known Subinterfaces)").size() > 0) {
                    System.out.println(anElement.select("dd").text());
                }

                if (anElement.select(":contains(All Known Implementing Classes)").size() > 0) {
                    System.out.println(anElement.select("dd").text());
                }
            }


        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }
}
