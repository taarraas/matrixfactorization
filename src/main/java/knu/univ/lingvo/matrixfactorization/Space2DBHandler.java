/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import knu.univ.lingvo.up.SpaceElement;
import knu.univ.lingvo.up.SpaceForSentenceCreator;
import knu.univ.lingvo.up.ThreeWordsExtractor;
import knu.univ.lingvo.up.TwoWordsExtractor;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.log4j.Logger;

/**
 *
 * @author taras
 */
public class Space2DBHandler implements PageHandler {
    Logger log = Logger.getLogger("Space2DBHandler");

    SpaceForSentenceCreator ssc = new SpaceForSentenceCreator();
    WikiModel wikiModel = new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");
    SentenceDetector sentenceDetector;
    PrintStream twos;
    PrintStream threes;

    public Space2DBHandler(OutputStream twos, OutputStream threes) {
        initSentenceDetect();
        this.twos = new PrintStream(twos, false);
        this.threes =  new PrintStream(threes, false);;
    }
    
    void initSentenceDetect() {
        InputStream modelIn = null;
        try {
            // Loading sentence detection model
            //modelIn = getClass().getResourceAsStream("/en-sent.bin");
            modelIn = new DataInputStream(new FileInputStream("data/models/en-sent.bin"));
            final SentenceModel sentenceModel = new SentenceModel(modelIn);
            modelIn.close();

            sentenceDetector = new SentenceDetectorME(sentenceModel);

        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (final IOException e) {
                }
            }
        }
    }
    
    private static int overall = 0;
    private static int good = 0;
    
    public void handle(String rawPage, String title) {
        String woTranslate = rawPage.replaceAll("(\\[\\[[^\\]]+\\]\\]\n)*\\[\\[[^\\]]+\\]\\]$", "");
        String plainStr = wikiModel.render(new PlainTextConverter(), woTranslate);
        String woBrackets = plainStr.replaceAll("\\{\\{[^\\}]*\\}\\}", "");
        final String[] paragraphs = woBrackets.split("\\n+");
        boolean isFirstParagraph = true;
        boolean isFirstSentence = true;
        for (String text : paragraphs) {
            if (text.isEmpty()) {
                continue;
            }

            String sentences[] = sentenceDetector.sentDetect(text);
            for (String sentence : sentences) {
                overall++;
                try {
                    handleSentence(sentence, (isFirstSentence ? 1 : 0) + (isFirstParagraph ? 1 : 0));
                } catch (Throwable e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }
                isFirstSentence = false;
            }
            isFirstParagraph = false;
        }
        log.info("" + good + "/" + overall + " = " + ((float) good / overall));
    }
    
    public void handleSentence(String sentence, int addWeight) {
        SpaceElement se = ssc.getSpace(sentence);
        if (se == null)
            return;
        
        ArrayList<String[]> two = TwoWordsExtractor.getTwoWords(se);
        ArrayList<String[]> three = ThreeWordsExtractor.getThreeWords(se);
        for (String[] strings : two) {
            twos.print("\"");
            for (String string : strings) {
                twos.print(string);
                twos.print("\";\"");
            }
            twos.print("\"\n");
        }
        twos.flush();
        
        for (String[] strings : three) {
            threes.print("\"");
              for (String string : strings) {
                threes.print(string);
                threes.print("\";\"");
            }
            threes.print("\"\n");
        }
        threes.flush();
    }

}
