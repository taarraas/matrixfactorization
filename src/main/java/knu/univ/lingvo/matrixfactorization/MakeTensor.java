/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.xml.sax.SAXException;

/**
 *
 * @author taras
 */
public class MakeTensor {
    
    
    String path;
    InputStream stream;
    PageHandler handler;

    public MakeTensor(String path, PageHandler handler) throws Exception {
        this.path = path;
        this.handler = handler;
        stream = new BZip2CompressorInputStream(new FileInputStream(path));
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser newSAXParser = factory.newSAXParser();
        newSAXParser.parse(stream, new WikiPageHandler(handler));
    }
    
    public String nextPage() throws IOException {
        final byte[] buffer = new byte[10000];
        stream.read(buffer);
        return buffer.toString();
    }
        
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String PATH = "/home/taras/Downloads/enwiki-latest-pages-articles.xml.bz2";
        MakeTensor mt = new MakeTensor(PATH, new PageHandler() {

            public void handle(String page) {
                System.out.println(page);
            }
        });
    }
}
