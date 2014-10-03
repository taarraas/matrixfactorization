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
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import knu.univ.lingvo.coref.CorefCluster;
import knu.univ.lingvo.coref.Dictionaries;
import knu.univ.lingvo.coref.Document;
import knu.univ.lingvo.coref.Mention;
import knu.univ.lingvo.coref.Semantics;
import knu.univ.lingvo.matrixfactorization.StanfordPageHandler;
import knu.univ.lingvo.up.TypedDepencyVocabulary;

/**
 *
 * @author tvozniuk
 */
public class ClassifierSieve extends DeterministicCorefSieve {

    LogisticClassifier<Boolean, String> classifier;
    LogisticClassifier<Boolean, String> classifierLocal;
    public double matchThreshol;
    public double inconsistencyThreshold;
    TypedDepencyVocabulary typedDepencyVocabulary;

    public ClassifierSieve(double matchThreshol, double inconsistencyThreshold, TypedDepencyVocabulary typed) {
        this.matchThreshol = matchThreshol;
        this.inconsistencyThreshold = inconsistencyThreshold;
        this.typedDepencyVocabulary = typed;
        try {
            FileInputStream fileIn =
                    new FileInputStream("classifier.ser.total");
            ObjectInputStream out = new ObjectInputStream(fileIn);
            classifier = (LogisticClassifier<Boolean, String>) out.readObject();
            out.close();
            fileIn.close();
            
            fileIn =
                    new FileInputStream("classifier.ser.local");
            out = new ObjectInputStream(fileIn);
            classifierLocal = (LogisticClassifier<Boolean, String>) out.readObject();
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

    public static ArrayList<String> samePhrase(CorefCluster mentionCluster,
            CorefCluster potentialAntecedent) {
        ArrayList<String> str = new ArrayList<String>();
        int totalPossible = 0;
        int totalMatched = 0;
        int maxMatch = 0;
        int maxPossibleForMax = 0;
        for (Mention mention : mentionCluster.getCorefMentions()) {
            Set<String> w1 = new TreeSet<String>();
            for (CoreLabel coreLabel : mention.originalSpan) {
                String pos = coreLabel.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (pos.equals("DT")) {
                    continue;
                }
                String text = coreLabel.getString(CoreAnnotations.LemmaAnnotation.class);
                w1.add(text);
            }
            for (Mention mention1 : potentialAntecedent.getCorefMentions()) {
                if (mention1.sentNum == mention.sentNum) {
                    continue;
                }

                Set<String> w2 = new TreeSet<String>();
                for (CoreLabel coreLabel : mention1.originalSpan) {
                    String pos = coreLabel.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String text = coreLabel.getString(CoreAnnotations.LemmaAnnotation.class);
                    if (pos.equals("DT")) {
                        continue;
                    }
                    w2.add(text);
                }

                int currMathced = 0;
                for (String string : w2) {
                    totalPossible++;
                    if (w1.contains(string)) {
                        totalMatched++;
                        currMathced++;
                    }
                }
                if (currMathced > maxMatch) {
                    maxMatch = currMathced;
                    maxPossibleForMax = Math.max(w1.size(), w2.size());
                }
            }
        }
        str.add("" + totalPossible);
        str.add("" + totalMatched);
        str.add("" + maxMatch);
        str.add("" + maxPossibleForMax);
        return str;
    }

    public static ArrayList<String> sameContexts2(CorefCluster mentionCluster,
            CorefCluster potentialAntecedent) {
        ArrayList<String> str = new ArrayList<String>();
        int totalPossible = 0;
        int totalMatched = 0;
        for (Mention mention : mentionCluster.getCorefMentions()) {
            if (mention.headIndexedWord == null) {
                continue;
            }
            Set<GrammaticalRelation> relns1 = mention.dependency.relns(mention.headIndexedWord);
            List<IndexedWord> ch1 = mention.dependency.getChildrenWithReln(mention.headIndexedWord, EnglishGrammaticalRelations.NOMINAL_SUBJECT);
            if (ch1.isEmpty()) {
                continue;
            }
            for (Mention mention1 : potentialAntecedent.getCorefMentions()) {

                if (mention1.headIndexedWord == null) {
                    continue;
                }
                List<IndexedWord> ch2 = mention1.dependency.getChildrenWithReln(mention1.headIndexedWord, EnglishGrammaticalRelations.NOMINAL_SUBJECT);
                if (ch2.isEmpty()) {
                    continue;
                }
                if (ch1 == ch2) {
                    continue;
                }

            }
        }
        str.add("" + totalPossible);
        str.add("" + totalMatched);
        return str;
    }

    public ArrayList<String> sameUsage(CorefCluster mentionCluster,
            CorefCluster potentialAntecedent, ArrayList<String> otherParams, boolean isTrue) {
        double sum = 0;
            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;
            int cnt=0;
            
        for (Mention mention : mentionCluster.getCorefMentions()) {
            if (mention.headIndexedWord == null) {
                continue;
            }

            String antPos = null;
            String type = null;
            
            boolean train=false;
            
            
            for (SemanticGraphEdge semanticGraphEdge : mention.dependency.outgoingEdgeList(mention.headIndexedWord)) {
                type = semanticGraphEdge.getRelation().getShortName();
                String w1 = semanticGraphEdge.getGovernor().getString(CoreAnnotations.TextAnnotation.class);
                String w2 = semanticGraphEdge.getDependent().getString(CoreAnnotations.TextAnnotation.class);
                for (Mention mention1 : potentialAntecedent.getCorefMentions()) {
                    if (mention1.headIndexedWord ==null)
                        continue;
                    
                    String w1Ant = mention1.headIndexedWord.getString(CoreAnnotations.TextAnnotation.class);                    
                    antPos = mention1.headIndexedWord.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
                    if (typedDepencyVocabulary.pairsByType.containsKey(type)
                            && typedDepencyVocabulary.pairsByType.get(type).contains(new AbstractMap.SimpleEntry<String, String>(w1Ant, w2))) {
                        //System.out.println(type + ": "  + w1 + " -> " + w1Ant + " " + w2);
                        ArrayList<String> merge = new ArrayList<String>();
                        merge.addAll(otherParams);
                        merge.add(antPos);
                        merge.add(type);
                        
                        if (train)
                        {
                            BasicDatum<Boolean, String> datum = new BasicDatum<Boolean, String>(merge, isTrue);
                            if (dataset.size() == 0 
                                || dataset.getDatum(dataset.size() - 1).label() 
                                || datum.label()) {
                                dataset.add(datum);
                            }
                        }
                        
                        double probability = classifierLocal.probabilityOf(merge, true);
                        sum+=probability;
                        max = Math.max(probability, max);
                        min = Math.min(probability, min);
                        cnt++;
                    }

                }

            }
            
            for (SemanticGraphEdge semanticGraphEdge : mention.dependency.incomingEdgeList(mention.headIndexedWord)) {
                type = semanticGraphEdge.getRelation().getShortName();
                String w1 = semanticGraphEdge.getGovernor().getString(CoreAnnotations.TextAnnotation.class);
                String w2 = semanticGraphEdge.getDependent().getString(CoreAnnotations.TextAnnotation.class);
                for (Mention mention1 : potentialAntecedent.getCorefMentions()) {
                    if (mention1.headIndexedWord ==null)
                        continue;
                    
                    String w2Ant = mention1.headIndexedWord.getString(CoreAnnotations.TextAnnotation.class);
                    antPos = mention1.headIndexedWord.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
                    if (typedDepencyVocabulary.pairsByType.containsKey(type)
                            && typedDepencyVocabulary.pairsByType.get(type).contains(new AbstractMap.SimpleEntry<String, String>(w1, w2Ant))) {
                        //System.out.println(type + ": "  + w1 + "  " + w2 + " -> " + w2Ant);
                        ArrayList<String> merge = new ArrayList<String>();
                        merge.addAll(otherParams);
                        merge.add(antPos);
                        merge.add(type);
                        
                        if (train)
                        {
                            BasicDatum<Boolean, String> datum = new BasicDatum<Boolean, String>(merge, isTrue);
                            if (dataset.size() == 0 
                                    || dataset.getDatum(dataset.size() - 1).label() 
                                    || datum.label()) {
                                dataset.add(datum);
                            }
                        }
                        double probability = classifierLocal.probabilityOf(merge, true);
                        sum+=probability;
                        max = Math.max(probability, max);
                        min = Math.min(probability, min);
                        cnt++;
                    }

                }

            }

        }
        
        ArrayList<String> res = new ArrayList<String>();
        int avgI = cnt ==0 ? -1 : (int)(sum / cnt * 100);
        int maxI = cnt ==0 ? -1 : (int)(max * 100);
        int minI = cnt ==0 ? -1 : (int)(min * 100);
        int sumI = cnt ==0 ? -1 : (int)(sum * 100);
        
        res.add("" + avgI);
        res.add("" + maxI);
        res.add("" + minI);
        res.add("" + sumI);

        return res;
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
            Map<Integer, Mention> goldMentions = document.allGoldMentions;
            Map<Integer, Mention> predictedMentions = document.allPredictedMentions;

            boolean qualOld = goldMentions.containsKey(mention.mentionID) && goldMentions.containsKey(ant.mentionID)
                    && goldMentions.get(mention.mentionID).goldCorefClusterID == goldMentions.get(ant.mentionID).goldCorefClusterID;

            
            

            ArrayList<String> feat1 = mention.getSingletonFeatures(dict);
            ArrayList<String> feat2 = ant.getSingletonFeatures(dict);
            ArrayList<String> merge = new ArrayList(feat1);
            merge.addAll(feat2);
            merge.add("" + mentionCluster.getCorefMentions().size());
            merge.add("" + potentialAntecedent.getCorefMentions().size());
            merge.add("" + (mention.sentenceNumber - ant.sentenceNumber));
            merge.add("" + (mention.mentionNumber - ant.mentionNumber));            
            merge.addAll(sameContexts(mentionCluster, potentialAntecedent));
            merge.addAll(samePhrase(mentionCluster, potentialAntecedent));
            merge.addAll(sameUsage(mentionCluster, potentialAntecedent, new ArrayList<String>(), qualOld));
            //merge.addAll(sameContexts2(mentionCluster, potentialAntecedent));



            if (false) {
                BasicDatum<Boolean, String> datum = new BasicDatum<Boolean, String>(merge, qualOld);
                 if (dataset.size() == 0 
//                                    || dataset.getDatum(Math.max(dataset.size() - 3, 0)).label() 
//                                    || dataset.getDatum(Math.max(dataset.size() - 2, 0)).label()
                                    || dataset.getDatum(Math.max(dataset.size() - 1, 0)).label() 
                                    || datum.label()) {
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
