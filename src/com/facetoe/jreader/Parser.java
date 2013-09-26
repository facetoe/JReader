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
    private String[] superInterfaces;
    private String[] implementingClasses;

    String[] getSuperInterfaces() {
        return superInterfaces;
    }

    void setSuperInterfaces(String[] superInterfaces) {
        this.superInterfaces = superInterfaces;
    }

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

    public JavaObject parse(File classFile) {
        parseJsoup(classFile);

        Elements name = doc.getElementsByClass("title");
        String[] nameParts = name.text().split(" ");
        if ( nameParts[0].equalsIgnoreCase("interface") ) {
            return parseInterface();
        }
        return null;
    }

    private JavaInterface parseInterface() {
        JavaInterface jInterface = new JavaInterface();
        Elements name = doc.getElementsByClass("title");
        String[] nameParts = name.text().split(" ");

        jInterface.setType(nameParts[0]);
        jInterface.setName(nameParts[1]);
        jInterface.setDescription(getDescription());

        getImplementationDetails(jInterface);

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

    private String[] getSubInterfaces() {
        Element element = doc.getElementsByClass("description").first();
        Element els = element.select(":contains(All Known Subinterfaces").first();
        System.out.println(els);
        return els.text().split(", ");
    }

    private void getImplementationDetails(JavaInterface jInterface) {
        Element element = doc.getElementsByClass("blocklist").first();
        Elements elements = element.getElementsByTag("dl");

        for ( Element anElement : elements ) {
            if ( anElement.select(":contains(All Known Subinterfaces)").size() > 0 ) {
                jInterface.setSubInterfaces(anElement.select("dd").text().split(", "));

            } else if ( anElement.select(":contains(All Known Implementing Classes)").size() > 0 ) {
                jInterface.setImplementingClasses(anElement.select("dd").text().split(", "));

            } else if (anElement.select(":contains(All Superinterfaces)").size() > 0  ) {
                jInterface.setSuperInterfaces(anElement.select("dd").text().split(", "));
            }
        }
    }

    private void print(JavaClass jClass) {
        System.out.println(jClass.getType() + " " + jClass.getName());

        System.out.println(jClass.getDescription());
    }


    private void print(JavaInterface jInterface) {
        System.out.println(jInterface.getType() + " " + jInterface.getName());
        System.out.println(jInterface.getDescription());

        if ( jInterface.getSubInterfaces() != null ) {
            System.out.println("Sub Interfaces:");
            for ( String s : jInterface.getSubInterfaces() ) {
                System.out.println(s);
            }
        }

        if ( jInterface.getImplementingClasses() != null ) {
            System.out.println("Implementing Classes:");
            for ( String s : jInterface.getImplementingClasses() ) {
                System.out.println(s);
            }
        }

        if( jInterface.getSuperInterfaces() != null) {
            System.out.println("Super Interfaces:");
            for ( String s : jInterface.getSuperInterfaces() ) {
                System.out.println(s);
            }
        }
    }

    public void print(JavaObject jObject) {
        if ( jObject.getType().equalsIgnoreCase("Interface") ) {
            print(( JavaInterface ) jObject);
        } else if ( jObject.getType().equalsIgnoreCase("Class") ) {
            print(( JavaClass ) jObject);
        } else {
            System.out.println("Not an interface or class");
        }
    }

    public static void main(String[] args) {

        File input = new File("/home/facetoe/tmp/docs/api/javax/accessibility/AccessibleText.html");

//        Parser parser = new Parser();
//        JavaObject obj = parser.parse(input);
//        parser.print(obj);
//
//        System.exit(1);

        try {
            Document doc = Jsoup.parse(input, "UTF-8");

            Elements summaries = doc.select("table.overviewSummary");

            String[] fieldTypes = null;
            String[] field;
            String[] fieldDescription;

            for ( Element summary : summaries ) {
                if(summary.select(":contains(Field)").size() > 0) {
                    Elements fieldTypeData = summary.select("td.colFirst");
                    Elements fieldsData = summary.select("td.colLast");

                    int size = fieldTypeData.size();
                    fieldTypes = new String[size];
                    field = new String[size];
                    fieldDescription = new String[size];

                    for ( int i = 0; i < size; i++ ) {
                        fieldTypes[i] = fieldTypeData.get(i).text();
                        fieldDescription[i] = fieldsData.get(i).select("div.block").text();
                        field[i] = fieldsData.get(i).select("a").text();
                    }

                    for ( int i = 0; i < size; i++ ) {
                        System.out.println(String.format("%s %s\n%s", fieldTypes[i], field[i], fieldDescription[i]));
                    }

                }


//                else if (summary.select(":contains(Field").size() > 0) {
//                    System.out.println(summary.text());
//                }
            }

        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }
}
