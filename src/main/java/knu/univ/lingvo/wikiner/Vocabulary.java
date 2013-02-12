/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.wikiner;

import java.util.HashSet;
import knu.univ.lingvo.matrixfactorization.DB;

/**
 *
 * @author tvozniuk
 */
public class Vocabulary {
    HashSet<String> data = new HashSet<String>();
    public void add(String wordCombination) {
        data.add(wordCombination);
    }
    
    public boolean has(String wordCombination) {
        return data.contains(wordCombination);
    }
}
