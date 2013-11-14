#JReader

##What is it?
JReader is a program that allows easy navigation of Javadocs and associated source code. 



##How does it work?
There are two main components to JReader. First, there is the documentation viewer. In this view you can easily search all the classes contained in the documentation. To minimise keystrokes, JReader supports auto-completion of class names. Once you have found the class you are interested in, simply click the "View Source" button to peruse the source code in a source view. 

In the source view, JReader offers a few extra time saving features. You can search for a particular method, field, class or interface and JReader will scroll to that items definition in the source file. JReader also supports searching for strings using regular expressions. 

Additionally, if you would like to gain a birds eye view of the structure of the source code, you can open a tree view. This view displays the elements of the source file organised by class, interface, constructor, method, field and nested class. Clicking an item or navigating with the arrow keys will scroll to that items definition in the source file. 

To make life easier, there are some handy keyboard shortcuts as well:

* ctrl-s: View Source
* ctrl-t: Toggle Tree
* ctrl-w: Close tab
* ctrl-q: Quit


##How do I install JReader?
Just start it up. On first launch JReader will ask for the location of the Java7 documentation on your computer. If you don't already have it, you can get it [here](http://www.oracle.com/technetwork/java/javase/documentation/java-se-7-doc-download-435117.html). Next, JReader will download the complete Java 7 source code, extract it and you're good to go. 

##How do I build JReader?
First `git clone` the repo, then `cd` to the root directory and type `ant`. This should build JReader and create JReader.jar in `artifacts/JReader`.

Alternatively you can download a release for your operating system from the [releases](https://github.com/facetoe/JReader/releases) page.  

##What types of Java documentation does JReader support?
JReader supports both the Java7 and Java6 style Javadocs.

##What libraries can I use it with?
JReader requires `allclasses-noframe.html` and `overview-summary.html` to be present in the docs directory. Additionally, the source code must be organized in a directory structure that reflects the package structure. If these conditions are met then JReader should work fine. 

##How do I add a different library to JReader? 
Simply select File->New Profile in the menu bar and fill in the fields. 

##I found a bug, what do I do?
Bug reports are very welcome. File an [issue](https://github.com/facetoe/JReader/issues) and I will try and fix it as soon as I can.

##Can you add X feature?
Maybe. File an [issue](https://github.com/facetoe/JReader/issues) with a description of the functionality you would like to see implemented and I will try and make it happen.

##What's next for JReader?
I want to add the ability to use Github's API to search for a class and display real world examples of its use. Sometimes just reading documentation isn't enough for me and I need to see an example. 


##I would like to contribute
Awesome! Contributions are very welcome. Simply make a pull request with your changes and I'll be happy to merge them in. Also, if you have any questions about the code or want to know how something works, please don't hesitate to ask. You can contact me at facetoe@ymail.com. 



