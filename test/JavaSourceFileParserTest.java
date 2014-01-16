
import com.facetoe.jreader.JavaSourceFileParser;
import japa.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by facetoe on 15/01/14.
 */
public class JavaSourceFileParserTest {

    @org.junit.Test
    public void testParse() throws Exception {
        recursiveTest(new File("/home/facetoe/.jreader/src-jdk"));
    }

    public static void recursiveTest(File dir) throws ParseException, IOException {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File content : contents) {
                if (content.isDirectory()) {
                    recursiveTest(content);
                } else {
                    if (content.getName().endsWith(".java")) {
                        JavaSourceFileParser.parse(new FileInputStream(content));
                    }
                }
            }
        }
    }

}
