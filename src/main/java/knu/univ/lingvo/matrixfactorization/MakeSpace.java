/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import knu.univ.lingvo.analysis.Main;
import knu.univ.lingvo.wikiner.NER;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.xml.sax.SAXException;

/**
 *
 * @author taras
 */
public class MakeSpace {
    
    
    String path;
    InputStream stream;
    PageHandler handler;

    public MakeSpace(String path, PageHandler handler) throws Exception {
        this.path = path;
        this.handler = handler;
        stream = new BZip2CompressorInputStream(new FileInputStream(path));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser newSAXParser = factory.newSAXParser();
        newSAXParser.parse(stream, new WikiPageHandler(handler));
    }
       
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String PATH;
        if (args.length == 0)
            PATH = "/Users/taras/Downloads/simplewiki-20130823-pages-articles.xml.bz2";
        else
            PATH = args[0];
        
        OutputStream two = new FileOutputStream("two.txt");
        OutputStream three = new FileOutputStream("three.txt");
        
        MakeSpace mt = new MakeSpace(PATH, new Space2DBHandler(two, three));
    }
}
