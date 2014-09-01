/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.coref.sievepasses;

import edu.stanford.nlp.classify.Dataset;
import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.LogisticClassifier;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import knu.univ.lingvo.coref.CorefCluster;
import knu.univ.lingvo.coref.Dictionaries;
import knu.univ.lingvo.coref.Document;
import knu.univ.lingvo.coref.Mention;
import knu.univ.lingvo.coref.Semantics;

/**
 *
 * @author tvozniuk
 */
public class ClassifierSieve extends DeterministicCorefSieve {

    LogisticClassifier<Boolean, String> classifier;
    double matchThreshol;
    double inconsistencyThreshold;

    public ClassifierSieve(double matchThreshol, double inconsistencyThreshold) {
        this.matchThreshol = matchThreshol;
        this.inconsistencyThreshold = inconsistencyThreshold;
        try {
            FileInputStream fileIn =
                    new FileInputStream("classifier.ser");
            ObjectInputStream out = new ObjectInputStream(fileIn);
            classifier = (LogisticClassifier<Boolean, String>) out.readObject();
            out.close();
            fileIn.close();
            System.out.printf("Serialized data is loaded from /tmp/employee.ser");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public GeneralDataset<Boolean, String> dataset = new Dataset<Boolean, String>();

    public static ArrayList<String> sameContexts(CorefCluster mentionCluster,
            CorefCluster potentialAntecedent) {
        ArrayList<String> str = new ArrayList<String>();
        int totalPossible = 0;
        int totalMatched = 0;
        for (Mention mention : mentionCluster.getCorefMentions()) {
            Set<String> w1 = new TreeSet<String>();
            for (CoreLabel coreLabel : mention.sentenceWords) {
                String pos = coreLabel.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
                String text = coreLabel.getString(CoreAnnotations.LemmaAnnotation.class);
                if (pos.startsWith("N") || pos.startsWith("V")) {
                    w1.add(text);
                }
            }
            for (Mention mention1 : potentialAntecedent.getCorefMentions()) {
                if (mention1.sentNum == mention.sentNum) {
                    continue;
                }

                Set<String> w2 = new TreeSet<String>();
                for (CoreLabel coreLabel : mention1.sentenceWords) {
                    String pos = coreLabel.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String text = coreLabel.getString(CoreAnnotations.LemmaAnnotation.class);
                    if (pos.startsWith("N") || pos.startsWith("V")) {
                        w2.add(text);
                    }
                }

                w2.remove("be");

                for (String string : w2) {
                    totalPossible++;
                    if (w1.contains(string)) {
                        totalMatched++;
                    }
                }
            }
        }
        str.add("" + totalPossible);
        str.add("" + totalMatched);
        return str;
    }

    /**
     * Checks if two clusters are coreferent according to our sieve pass
     * constraints
     *
     * @param document
     * @throws Exception
     */
    public boolean coreferent(Document document, CorefCluster mentionCluster,
            CorefCluster potentialAntecedent,
            Mention mention2,
            Mention ant,
            Dictionaries dict,
            Set<Mention> roleSet,
            Semantics semantics) throws Exception {

        Mention mention = mentionCluster.getRepresentativeMention();

        if (matchThreshol > inconsistencyThreshold) {
            ArrayList<String> feat1 = mention.getSingletonFeatures(dict);
            ArrayList<String> feat2 = ant.getSingletonFeatures(dict);
            ArrayList<String> merge = new ArrayList(feat1);
            merge.addAll(feat2);
            merge.add("" + mentionCluster.getCorefMentions().size());
            merge.add("" + potentialAntecedent.getCorefMentions().size());
            merge.add("" + (mention.sentenceNumber - ant.sentenceNumber));
            merge.add("" + (mention.mentionNumber - ant.mentionNumber));
            merge.addAll(sameContexts(mentionCluster, potentialAntecedent));

            Map<Integer, Mention> goldMentions = document.allGoldMentions;
            Map<Integer, Mention> predictedMentions = document.allPredictedMentions;

            boolean qualOld = goldMentions.containsKey(mention.mentionID) && goldMentions.containsKey(ant.mentionID)
                    && goldMentions.get(mention.mentionID).goldCorefClusterID == goldMentions.get(ant.mentionID).goldCorefClusterID;


            if (false) {
                BasicDatum<Boolean, String> datum = new BasicDatum<Boolean, String>(merge, qualOld);
                if (dataset.size() == 0 || dataset.getDatum(dataset.size() - 1).label() || datum.label()) {
                    dataset.add(datum);
                }
                return false;
            }
            double qual = classifier.probabilityOf(merge, true);

            if (qualOld && qual <= inconsistencyThreshold) {
                System.out.print("\ntrue!! " + qual);
            } else if (!qualOld && qual >= matchThreshol) {
                System.out.print(" !" + qual);
            }

            if (qual > matchThreshol) {
                return true;
            }

            if (qual < inconsistencyThreshold) {
                document.addIncompatible(mention, ant);
                return false;
            }
        }
        return false;
    }
}
