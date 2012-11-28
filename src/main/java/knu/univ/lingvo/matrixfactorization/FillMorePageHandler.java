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
    private static final int MINIMALPARAGRAPH = 500;
    WikiModel wikiModel = new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");

    public void handle(String rawPage) {
        String plainStr = wikiModel.render(new PlainTextConverter(), rawPage);
        String page = plainStr.replaceAll("\\{\\{[^\\}]*\\}\\}", "");
        final Map<String, String>[] wordByTypePair = m.getWordByTypePair(page);
        System.out.println("----------- page");
        for (Map<String, String> map : wordByTypePair) {
            System.out.println(">>>>>");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }
        }
    }
}
