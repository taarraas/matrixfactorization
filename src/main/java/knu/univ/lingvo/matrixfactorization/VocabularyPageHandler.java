/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import java.io.IOException;
import java.sql.SQLException;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.tokenize.Tokenizer;

/**
 *
 * @author taras
 */
public class VocabularyPageHandler implements PageHandler {

    public void handle(String page, String title) {
        try {
            DB.getInstance().saveArticle(title);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
