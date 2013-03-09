/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import knu.univ.lingvo.analysis.Main;
import knu.univ.lingvo.wikiner.NER;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author taras
 */
public class MakeTensorReuters {

    Logger log = Logger.getLogger("MakeTensorReuters");
    String path;
    InputStream stream;
    PageHandler handler;

    private static String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }

    public void corpusToDocuments(String mCorpusFile) throws IOException {
        System.out.println("Started:" + mCorpusFile);
        String text = readFile(mCorpusFile);
        String title, body;
        int start, end, pos;
        do {
            // get the title
            start = text.indexOf("<TITLE>");
            if (start < 0) {
                return;
            }
            end = text.indexOf("</TITLE>");
            title = text.substring(start + "<TITLE>".length(), end);
            // get the body
            start = text.indexOf("<BODY>");
            end = text.indexOf("</BODY>");
            if (start < 0 || end < 0)
                return;
            
            body = text.substring(start + "<BODY>".length(), end);
            // remove the "Reuter" word at the end of the body
            pos = body.indexOf("Reuter");
            if (pos > 0) {
                body = body.substring(0, pos);
            }
            if (body.length() > 150) {
                String formattedBody = body.replaceAll("\\n(?!\\ )", " ");
                formattedBody = formattedBody.replaceAll("\\n[\\ ]+", "\n");               
                formattedBody = formattedBody.replaceAll("\\&lt\\;", "");
                formattedBody = formattedBody.replaceAll("\\>", "");
                formattedBody = formattedBody.replaceAll("REUTER.*", "");
                System.out.println("<new article>");
                System.out.println(formattedBody);
//                handler.handle(formattedBody, title);
            }
            // ignore the part just read
            text = text.substring(end + "</BODY>".length());
        } while (text.length() > 0);
    }

    public MakeTensorReuters(String path, PageHandler handler) throws Exception {
        File files[] = (new File(path)).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".sgm");
            }
        });

        for (File file : files) {
            corpusToDocuments(file.toString());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        NER v = new NER();
        //DB.getInstance().fillVocabulary(v);
        String PATH;
        if (args.length == 0) {
            PATH = "/home/taras/Downloads/enwiki-latest-pages-articles.xml.bz2";
        } else {
            PATH = args[0];
        }
        MakeTensorReuters mt = new MakeTensorReuters(PATH, new StanfordPageHandler(v));
    }
}
