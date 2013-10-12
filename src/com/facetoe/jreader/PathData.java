package com.facetoe.jreader;

/**
 * Accepts a local path and extracts various information from it.
 */

//TODO refactor this or remove it entirely as most of what it does is unecessary now I've improved the parsing.
class PathData {
    private String docPath;

    /**
     * The name of the html file this path points to
     */
    private String fileName;

    /**
     * The docPath converted to point to the relevant Java source code
     */
    private String srcPath;

    /**
     * The name of this Java object
     */
    private String objectName;

    /**
     * Either a CONSTANT or method, or null
     */
    private String searchTerm;


    public PathData(String docPath) {
        this.docPath = docPath;

        /* If path is pointing to a java file or a website just extract the name and we're done  */
        if ( docPath.endsWith(".java")
                || docPath.contains("http:/")
                || docPath.contains("https:/")
                || docPath.contains("www.") ) {

            srcPath = docPath;
            extractFileName();
        } else {
            parsePath();
            extractFileName();
        }
    }

    /**
     * Extract all the information we can get out of the path
     */
    private void parsePath() {
        /* Chop off the section of path that points to the Java docs */
        String path = docPath.substring(docPath.lastIndexOf("api") + 3, docPath.length());

        /* If the user is browsing with frames enabled then this will appear, remove it */
        if ( path.contains("index.html?") ) {
            path = path.replace("index.html?", "");
        }

        /* The path is in the form /dir/dir/objectName.html */
        objectName = path.substring(path.lastIndexOf("/") + 1, path.indexOf("."));

        String srcDir = Config.getEntry("srcDir");
        srcPath = srcDir + path.replace(".html", ".java");

        /* If there are more than 2 periods it's probably a nested class like: /dir/dir/SomeClass.SomeNestedClass.html */
        if ( path.split("\\.").length > 2 ) {
            String[] parts = path.split("\\.");
            String nestedClassName = parts[parts.length - 2];
            String newPath = path.substring(0, path.lastIndexOf("/") + 1) + objectName + ".java";
            srcPath = srcDir + newPath;
            searchTerm = nestedClassName;
        }

        /* If there is a '#' character it's either a method or a constant like:
         * /dir/dir/SomeClass.html#methodName(int foo, int bar)
         * or
         * /dir/dir/SomeClass.html#CONSTANT */
        if ( docPath.contains("#") ) {
            String[] parts = path.split("#");
            String methodName = parts[1];
            if ( methodName.contains("(") ) {
                methodName = methodName.substring(0, methodName.indexOf("(") + 1);
            } else {
                methodName = " " + methodName;
            }

            searchTerm = methodName;

            /* Finally, convert the .html to .java so we can load it up */
            srcPath = srcDir + parts[0].replace(".html", ".java");
            System.out.println("SrcPath: " + srcPath);
        }
    }

    /**
     * Extracts the file name from the url.
     */
    private void extractFileName() {

        //TODO Make it handle nested classes like SomeClass.NestedClass
        /* Sometimes the urls contain "path/filename.html#methodname" so extract the file name substring */
        if ( docPath.contains("#") ) {
            fileName = docPath.substring(docPath.lastIndexOf("/") + 1, docPath.indexOf("#"));
        } else {
            /* Otherwise just grab the filename at the end */
            String[] parts = docPath.split("/");
            fileName = parts[parts.length - 1];
        }
        fileName = fileName.replace(".html", ".java");
    }

    String getFileName() {
        return fileName;
    }

    String getSrcPath() {
        return srcPath;
    }

    String getObjectName() {
        return objectName;
    }
}