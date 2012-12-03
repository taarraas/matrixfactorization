/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import java.util.Map;
import knu.univ.lingvo.analysis.Main;

/**
 *
 * @author taras
 */
public class FillMorePageHandler implements PageHandler {

    final Main m = new Main();
    private static final int MINIMALPARAGRAPH = 50;
    WikiModel wikiModel = new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");

    public void handle(String rawPage) {
        String plainStr = wikiModel.render(new PlainTextConverter(), rawPage);
        String woBrackets = plainStr.replaceAll("\\{\\{[^\\}]*\\}\\}", "");
        final String[] paragraphs = woBrackets.split("\\n");
        for (String text : paragraphs) {
            if (text.length() < MINIMALPARAGRAPH) {
                continue;
            }
            String sentences[] = m._sentenceDetector.sentDetect(text);
            for (String sentence : sentences) {
                if (sentence.length() < 15)
                    continue;
                final Map<String, String> wordByTypePair = m.getWordByTypePair(sentence);
                if (wordByTypePair.isEmpty())
                    continue;
                System.out.println();
                System.out.println(sentence);
                System.out.println(">>>>>");
                for (Map.Entry<String, String> entry : wordByTypePair.entrySet()) {
                    System.out.println(entry.getKey() + " - " + entry.getValue());
                }
            }
        }
    }
}
