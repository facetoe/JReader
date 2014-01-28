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

##Screenshots


The default profile home page. Users can browse with the links or search for a particular class.

![Default Profile Home](http://imgur.com/dep2f3K.png)

Here we have the documentation for StringBuffer. Note that JReader auto-completed the class name. All text boxes in JReader support auto-completion.

![Class Documentation](http://imgur.com/CePY1IT.png)

After clicking "View Source" or pressing ctrl-c a source view opens in a new tab and highlights the class declaration.

![Source Code View](http://imgur.com/rVbmkEC.png)

When "Toggle Tree" or ctrl-t is clicked, a source tree panel expands from the left displaying the structure of the source file as a tree. 
 
![Source Code View With TreeView](http://imgur.com/HsR3KYq.png)

Clicking an item in the source tree scrolls to and highlights that items definition in the source file.

![Source Code View With Expanded TreeView](http://imgur.com/SvFApeQ.png)

JReader supports several search options - including regular expressions.

![Search Options](http://imgur.com/ROW38eh.png)

Here we see the result of a regexp search.

![Regexp Search](http://imgur.com/l2uVomg.png)

JReader supports searching Github for examples of code use.

![Github Search](http://imgur.com/8sZXRsT.png)

Here we see a Github search result.

![Github Search Results](http://imgur.com/5fwV0gM.png)

Clicking an item will download the file from Github, display it in a source view and highlight the fragment. All of the usual source view features are available - such as tree view and search.

![Github Search Result File View](http://imgur.com/RZPDTYf.png)

Jreader is not restricted to the official docs. Here we make a new profile for SwingX.

![Create New Profile](http://imgur.com/Wgy8fMk.png)

Once the profile is completed JReader navigates to the profiles home.

![SwingX Profile](http://imgur.com/lVWZ4q8.png)

Here we see the documentation for CollapsiblePane, used to expand the tree view in JReader.

![CollapsiblePane Docs](http://imgur.com/NbVoIVl.png)

All the normal source view options are available for non-official documentations as well.

![CollapsiblePane Source](http://imgur.com/angGDe2.png)



##How do I install JReader?
Simply execute the JReader.jar file. JReader will extract some files into a hidden folder in the users home, create the default profile and then launch.

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



