/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.tokenize.Tokenizer;

/**
 *
 * @author taras
 */
public class VocabularyPageHandler implements PageHandler {
    private static final int MINIMALPARAGRAPH = 500;
    WikiModel wikiModel = new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");
    SentenceDetector _sentenceDetector = null;
    Tokenizer _tokenizer = null;

    public void handle(String page) {
         String plainStr = wikiModel.render(new PlainTextConverter(), page);
         String noBrack = plainStr.replaceAll("\\{\\{[^\\}]*\\}\\}", "");
         
    }
    
}
