/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.wikiner;

import java.util.*;
import knu.univ.lingvo.matrixfactorization.DB;

/**
 *
 * @author tvozniuk
 */
public class NER {
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("NER");

    private static final int NER_DEPTH = 4;
    Set<String> data = new TreeSet<String>();

    public void add(String wordCombination) {
        data.add(wordCombination);
    }

    public boolean has(String wordCombination) {
        return data.contains(wordCombination);
    }

    public int size() {
        return data.size();
    }

    public String nerIt(ArrayList<String> tags, int no) {
        int maxSize = 0;
        String bestResult = null;
        for (int from = Math.max(0, no - NER_DEPTH);
                from <= no;
                from++) {

            StringBuffer sb = new StringBuffer();
            for (int k = from; k < no; k++) {
                sb.append(tags.get(k));
                sb.append(" ");
            }

            for (int to = no;
                    to < Math.min(tags.size(), no + NER_DEPTH);
                    to++) {
                if (to != no) {
                    sb.append(" ");
                }
                sb.append(tags.get(to));

                if (has(sb.toString()) && (to - from) > maxSize) {
                    maxSize = to - from;
                    bestResult = sb.toString();
                    log.info(bestResult);
                }
            }
        }

        if (maxSize == 0) {
            return tags.get(no);
        } else {
            return bestResult;
        }
    }
}
