/**
 * @fileOverview WordNet.java
 * @author Andrey Nikonenko
 * @version 1.0
 * @date Apr 4, 2012
 * @modified Apr 24, 2012
 * @modifiedby Andrey Nikonenko
 * @param Created in Taras Shevchenko National University of Kyiv (Cybernetics) under a contract between
 * @param LLC "Samsung Electronics Ukraine Company" (Kiev Ukraine) and
 * @param Taras Shevchenko National University of Kyiv
 * @param Copyright: Samsung Electronics, Ltd. All rights reserved.
 */
/*
 * Set of classes for Word Sense Disambiguation workflow
 * 
 */

package knu.univ.lingvo.analysis;

import com.google.common.collect.HashMultimap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.tree.DefaultMutableTreeNode;


/** Class for accuracy testing using data from Test Set*/
class WordElement {
    public String word;
    public String synsetID;
    public String POS;
    public ArrayList<Synset> synsets;
    public WordElement (String word, String synsetID, String POS){
        this.word = word;
        this.synsetID = synsetID;
        this.POS = POS;
    }
}


/** Class contain syntactic structures with WordNet info*/
class WSDTreeNode {
    private Pattern sentence_element = Pattern.compile("\\(([^\\(\\)]+ [^\\(\\)]+)\\)");
    private Pattern check_POS_type = Pattern.compile("JJ[A-Z]?|RB[A-Z]?|VB[A-Z]?|NN[A-Z]{0,2}");
    public String phrase = "";
    public String word = "";
    public String POS = "";
    public ArrayList<Synset> synsets;
    /** Convert Penn Treebank II Tags to WordNet-type Tags */
    private String WordNetStylePOS(String p){
        if (p.isEmpty()) {return "";}
        String res = p.toLowerCase().substring(0, 1);
        if (res.equals("j")) {res = "a";}
        return res;
    }

    /** Fill class instance with WordNet data */
    private void fill_node(WordNet WordNetPointer){
        Matcher m = sentence_element.matcher(phrase);
        if (m.find()) {
           String[] elements = m.group(1).split(" ");
           m = check_POS_type.matcher(phrase);
           if (m.find()){
                this.POS = elements[0];
                this.word = elements[1].toLowerCase();
                this.synsets = WordNetPointer.getAllSenses(word, WordNetStylePOS(POS));
           }
        }
    }
    public WSDTreeNode(String phrase, WordNet WordNetPointer) {
        this.phrase = phrase;
        fill_node(WordNetPointer);
    }
}


/**class for synset data */
class Synset {
    public String id;
    public String lexicalInfo;
    public Synset(String id, String lexicalInfo){
        this.id = id;
        this.lexicalInfo = lexicalInfo;
    }
}

public class WordNet {
    private final int maxPathDepth = 10; // limit max path length for bidirectional search
    private final int debugMode = 0; // 1 - for additional output, 0 - no debug info output

    private HashMap<String, ArrayList<Synset>> noun;
    private HashMap<String, ArrayList<Synset>> verb;
    private HashMap<String, ArrayList<Synset>> adj;
    private HashMap<String, ArrayList<Synset>> adv;
    private HashMap<String, ArrayList<Synset>> noun_s;
    private HashMap<String, ArrayList<Synset>> verb_s;
    private HashMap<String, ArrayList<Synset>> adj_s;
    private HashMap<String, ArrayList<Synset>> adv_s;
    private HashMultimap<String, String> relations;
    private Stemmer _stemmer;


    //* Get word stem using Potter Stemmer*/
    private String stem(String word){
         String tokens[] = word.toLowerCase().split(" ");
         String stem = "";
         for (String token:tokens) {
             _stemmer.add(token.toCharArray(),token.length());
             _stemmer.stem();
             stem = stem + _stemmer.toString() + " ";
         }
         return stem.trim();
    }

    /**load WordNet data from files */
    private void load_data(String filename, HashMap<String, ArrayList<Synset>> collection, String POS, boolean use_stemmer) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            in.readLine();
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String[] text = line.split("\\t");
                String synsets[] = text[3].split(" ");
                String phrase = text[0];
                ArrayList<Synset> cur_synsets;
                // Check equal element existense in list
                // If stemmer unused all elements are unique
                if (use_stemmer) {
                   phrase = stem(phrase);
                   cur_synsets = collection.get(phrase);
                   if (cur_synsets == null) {cur_synsets = new ArrayList<Synset>();}
                } else {
                    cur_synsets = new ArrayList<Synset>();
                }
                String _id = "";
                String _lexicalInfo = "";
                for (int i=0; i<synsets.length; i++){
                   if (i%2 == 0) {_id = synsets[i]+POS;}
                   else {
                       _lexicalInfo = synsets[i];
                       Synset s = new Synset(_id, _lexicalInfo);
                       if (use_stemmer && cur_synsets.contains(s)) {
                          System.out.println("doublicate!!");
                       }
                       else {
                           cur_synsets.add(s); // add new synset to list
                       }
                   }
                }
                //Delete doublicates in stemmed version
                collection.put(phrase,cur_synsets); // add new entry to word collection
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**load WordNet relations data from file */
    private void load_relations(String filename, HashMultimap<String, String> collection) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            in.readLine();
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String[] text = line.split("\\t");
                collection.put(text[0], text[1]);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** return all senses of the word */
    protected ArrayList<Synset> getAllSenses(String word, String POS) {
        HashMap<String, ArrayList<Synset>> mainCollection;
        HashMap<String, ArrayList<Synset>> stemCollection;
        if (POS.equals("n")) {
           mainCollection = noun;
           stemCollection = noun_s;
        } else if (POS.equals("v")) {
           mainCollection = verb;
           stemCollection = verb_s;
        } else if (POS.equals("a")) {
           mainCollection = adj;
           stemCollection = adj_s;
        } else if (POS.equals("r")) {
           mainCollection = adv;
           stemCollection = verb_s;
        } else {
           return new ArrayList<Synset>();
        }
        ArrayList<Synset> senses = mainCollection.get(word);
        if (senses == null) {senses = stemCollection.get(stem(word));}
        if (senses == null) {return new ArrayList<Synset>();}
        return senses;
    }

    /** Load synsets which related with synsets from idList.
     *  And don't forget about synsets from idList, they should retain.
     */
    private HashSet<String> loadRelated(HashSet idList){
        Iterator it = idList.iterator();
        HashSet<String> newId = new HashSet<String>();
        while(it.hasNext()){
            newId.addAll(relations.get((String)it.next()));
        }
        newId.addAll(idList);
        return newId;
    }

    /** Find shortest path by bidirectional Dijkstra's Algorithm */
    private int findShortestWay(HashSet start, HashSet end, int currLevel, int maxLength){
        HashSet<String> tmp1 = new HashSet<String>();
        HashSet<String> tmp2 = new HashSet<String>();
        tmp1.addAll(start);
        tmp2.addAll(end);
        if (tmp1.removeAll(tmp2)) {
           return currLevel;
        } else {
           tmp1.addAll(loadRelated(tmp1));
           tmp2.addAll(loadRelated(tmp2));
           if (currLevel+2>maxLength) {return 100;} 
           else {return findShortestWay(tmp1, tmp2, currLevel+2, maxLength);}
        }
    }

    /** Build different paths between words in triplet
     *  MainWord synset_id in best path selected as result
     */
    private String getPathWeight(ArrayList<Synset> mainWord, ArrayList<Synset> additionWord, ArrayList<Synset> additionWord2) {
        HashSet<String> end = new HashSet<String>();
        HashSet<String> end2 = new HashSet<String>();
        if (additionWord != null) {
            for (Synset s2:additionWord) {
                end.add(s2.id);
            }
        }
        if (additionWord2 != null) {
            for (Synset s2:additionWord2) {
                end2.add(s2.id);
            }
        }
        int minWeight = Integer.MAX_VALUE;
        String synsetId = "";
        for (Synset s1:mainWord){
            HashSet<String> start = new HashSet<String>();
            start.add(s1.id);
            int w1 = findShortestWay(start,end, 0, Math.min(minWeight, maxPathDepth));
            int w2 = findShortestWay(start,end2, 0, Math.min(minWeight, maxPathDepth));
            if (w1+w2<minWeight) {minWeight = w1+w2; synsetId = s1.id;}
        }
        return synsetId;
    }

    /** Constructor, load all Wordnet data */
    public WordNet(String lib_path) {
        // Standart dictionaries
        noun = new HashMap<String, ArrayList<Synset>>();
        verb = new HashMap<String, ArrayList<Synset>>();
        adj = new HashMap<String, ArrayList<Synset>>();
        adv = new HashMap<String, ArrayList<Synset>>();
        // Stemmed version of dictionaries
        noun_s = new HashMap<String, ArrayList<Synset>>();
        verb_s = new HashMap<String, ArrayList<Synset>>();
        adj_s = new HashMap<String, ArrayList<Synset>>();
        adv_s = new HashMap<String, ArrayList<Synset>>();
        relations = HashMultimap.create();
        // Initialize Stemmer
        _stemmer = new Stemmer();
        //loading prepared dictionaries
        load_data(lib_path + "/noun.tsv", noun, "n", false);
        load_data(lib_path + "/verb.tsv", verb, "v", false);
        load_data(lib_path + "/adjective.tsv", adj, "a", false);
        load_data(lib_path + "/adverb.tsv", adv, "r", false);
        load_data(lib_path + "/noun.tsv", noun_s, "n", true);
        load_data(lib_path + "/verb.tsv", verb_s, "v", true);
        load_data(lib_path + "/adjective.tsv", adj_s, "a", true);
        load_data(lib_path + "/adverb.tsv", adv_s, "r", true);
        load_relations(lib_path + "/relations.tsv", relations);
    }

    /** Transform syntactic tree to WSD tree*/
    public DefaultMutableTreeNode transformToWSDTree(DefaultMutableTreeNode syntacticTree){
        if (syntacticTree==null) {return null;}
        Enumeration en = syntacticTree.preorderEnumeration();
        while (en.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
          WSDTreeNode WSDElement = new WSDTreeNode(node.toString(), this);
          node.setUserObject(WSDElement);
        }
        return syntacticTree;
    }

    /** Print WSD tree*/
    public void printWSDTree(DefaultMutableTreeNode tree) {
        if (tree==null) {return;}
        Enumeration en = tree.preorderEnumeration();
        while (en.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
          String space = new String(new char[node.getLevel()*4]).replace('\0', ' ');
          WSDTreeNode WSDnode = ((WSDTreeNode)node.getUserObject());
          if (!WSDnode.word.isEmpty()) {
              if (WSDnode.synsets.size()==1) {
                System.out.println(space + WSDnode.word + " | " + WSDnode.synsets.get(0).lexicalInfo);
              } else {
                System.out.println(space + WSDnode.word + " | " + WSDnode.synsets.size());
              }
           }
          //System.out.println(space + ((WSDTreeNode)node.getUserObject()).phrase);
        }
    }

    /** Delete from solved synset all data except best synset_id value */
    private void solveTriplet(WSDTreeNode node1, WSDTreeNode node2, WSDTreeNode node3){
        ArrayList<Synset> nd2 = null;
        ArrayList<Synset> nd3 = null;
        if (node2 != null) {nd2 = node2.synsets;}
        if (node3 != null) {nd3 = node3.synsets;}
        if (node1 == null) {return;}
        String res_synset = getPathWeight(node1.synsets, nd2, nd3);
        Synset choosedValue = null;
        for (Synset s:node1.synsets) {
            if (s.id.equals(res_synset)) {choosedValue = s; break;}
        }
        node1.synsets.clear();
        node1.synsets.add(choosedValue);
    }

    /** Main function in WSD
     *  Walk thru WSD tree and solve all disambiguation
     *  Result - WSD tree with sense marker at all vertex
     */
    public void solveWSDTree(DefaultMutableTreeNode tree) {
        if (tree==null) {return;}
        Enumeration en = tree.preorderEnumeration();
        WSDTreeNode prevNode1 = null;
        WSDTreeNode prevNode2 = null;
        WSDTreeNode lastSolvedNode = null;
        while (en.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
          WSDTreeNode WSDnode = ((WSDTreeNode)node.getUserObject());
          if (!WSDnode.word.isEmpty() && WSDnode.synsets.size() > 0) {
              if ((prevNode2 != null) && (prevNode1 != null)) {
//                  System.out.println(prevNode2.word + " | " + prevNode2.synsets.size());
                  solveTriplet(prevNode2, prevNode1, WSDnode);
                  lastSolvedNode = prevNode2;
                  if (debugMode == 1) {System.out.println(prevNode2.word + " | " + prevNode2.synsets.get(0).id + "  " + prevNode2.synsets.get(0).lexicalInfo);}
                  prevNode2 = null;
              }
              if (prevNode2 == null) {
                  prevNode2 = prevNode1;
                  prevNode1 = null;
              }
              if(prevNode1 == null) {
                  prevNode1 = WSDnode;
              }
          }
        }
        if (prevNode1!=null) {
            solveTriplet(prevNode1, prevNode2, lastSolvedNode);
            if (debugMode == 1) {System.out.println(prevNode1.word + " | " + prevNode1.synsets.get(0).id + "  " + prevNode1.synsets.get(0).lexicalInfo);}
        }
        if (prevNode2!=null) {
            solveTriplet(prevNode2, prevNode1, lastSolvedNode);
            if (debugMode == 1) {System.out.println(prevNode2.word + " | " + prevNode2.synsets.get(0).id + "  " + prevNode2.synsets.get(0).lexicalInfo);}
        }
    }


    /** Load data from Test Corpus*/
    private void readTestFile(String filename, ArrayList<WordElement> goldSet, ArrayList<WordElement> resultSet){
        String[] text = {""};
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            WordElement preWord = null;
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                text = line.split("\\t");
                String word = text[3].toLowerCase();
                String result = text[7].replaceAll("-", "");
                String POS = "";
                if (!result.isEmpty()) {
                    POS = result.substring(0,1);
                    //convert POS to WordNet style
                    if (POS.equals("j")) {POS="a";}
                    result = result.substring(1) + POS;
                }
                if (preWord!=null && preWord.synsetID.equals(result)) { //adapting phrase, this WSD version work only with words, not phrases
                    goldSet.get(goldSet.size()-1).word = goldSet.get(goldSet.size()-1).word + " " + word;
                    resultSet.get(resultSet.size()-1).word = resultSet.get(resultSet.size()-1).word + " " + word;
                } else {
                    WordElement w = new WordElement(word, result, POS);
                    preWord = w;
                    // Gold Set contains full info from test set: word, synset_id, synset_type
                    goldSet.add(w);
                    // Test Set contains only word and synset_type, system should compute synset_id using this data
                    w = new WordElement(word, "", POS);
                    resultSet.add(w);
                }
            }
        } catch (Exception ioe) {
            System.out.println(text[0]);
            ioe.printStackTrace();
        }        
    }

    /** Load data for Test Corpus mapping*/
    private void loadTestMapping(String filename, String POS, HashMultimap<String,String> mapping){
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String[] text = line.split("\\s");
                // Clear leading zeros
                String id16 = text[0].replaceFirst("^0+", "");
                for (int i=1; i<text.length; i++) {
                    if (i%2 == 1) {
                        String id30 = text[i].replaceFirst("^0+", "");;
                        mapping.put(id16+POS, id30+POS);
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** (Test modification) Delete from solved synset all data except best synset_id value */
    private void solveTestTriplet(WordElement node1, WordElement node2, WordElement node3){
        ArrayList<Synset> nd2 = null;
        ArrayList<Synset> nd3 = null;
        if (node2 != null) {nd2 = node2.synsets;}
        if (node3 != null) {nd3 = node3.synsets;}
        if (node1 == null) {return;}
        String res_synset = getPathWeight(node1.synsets, nd2, nd3);
        Synset choosedValue = null;
        for (Synset s:node1.synsets) {
            if (s.id.equals(res_synset)) {choosedValue = s; break;}
        }
        node1.synsets.clear();
        node1.synsets.add(choosedValue);
    }

    /** For testing purposes only
     *  Compute F1 measure
     */
    private void evaluate(String filename, HashMultimap<String,String> mapping) {
        ArrayList<WordElement> goldSet = new ArrayList<WordElement>();
        ArrayList<WordElement> resultSet = new ArrayList<WordElement>();
        readTestFile(filename, goldSet, resultSet);
        System.out.println("Test file: "+filename);
        WordElement prevNode1 = null, prevNode2 = null, lastSolvedNode = null;
        for (WordElement word_:resultSet){
            String _pos = word_.POS;
            word_.synsets = getAllSenses(word_.word,_pos);

            if (!word_.word.isEmpty() && word_.synsets.size() > 0) {
                if ((prevNode2 != null) && (prevNode1 != null)) {
                     solveTestTriplet(prevNode2, prevNode1, lastSolvedNode);
                     prevNode2.synsetID = prevNode2.synsets.get(0).id;
                     lastSolvedNode = prevNode2;
                     if (debugMode == 1) {System.out.println(prevNode2.word + " | " + prevNode2.synsets.get(0).id + "  " + prevNode2.synsets.get(0).lexicalInfo);}
                     prevNode2 = null;
                }
                if (prevNode2 == null) {
                      prevNode2 = prevNode1;
                      prevNode1 = null;
                }
                if(prevNode1 == null) {
                      prevNode1 = word_;
                }
            }
        }
        //end of file
        if (prevNode1!=null) {
            solveTestTriplet(prevNode1, prevNode2, lastSolvedNode);
            if (debugMode == 1) {System.out.println(prevNode1.word + " | " + prevNode1.synsets.get(0).id + "  " + prevNode1.synsets.get(0).lexicalInfo);}
        }
        if (prevNode2!=null) {
            solveTestTriplet(prevNode2, prevNode1, lastSolvedNode);
            if (debugMode == 1) {System.out.println(prevNode2.word + " | " + prevNode2.synsets.get(0).id + "  " + prevNode2.synsets.get(0).lexicalInfo);}
        }
            
        // Compare Result set with Golden Set
        int correct=0, incorrect=0, true_negative=0;
        System.out.println("Gold Set size: "+goldSet.size()+" Test Set size: "+resultSet.size());
        for (int i = 0; i< goldSet.size(); i++) {
            WordElement gold = goldSet.get(i);
            WordElement test = resultSet.get(i);
            // Check only words with WordNet ID in Gold Set & Test Set
            // Some words missing in a test set because of stemmer normalization issues
            if (!gold.synsetID.isEmpty()){
                if (test.synsetID.isEmpty()) {true_negative++; continue;}
                if (mapping.get(gold.synsetID).contains(test.synsetID)) {
                    correct++;
                    if (debugMode == 1) {System.out.println("1 " +gold.word + "  " + gold.synsetID + " -> Mapping >> " + mapping.get(gold.synsetID) +" << "  + test.synsetID);}
                }
                else {incorrect++;
                    if (debugMode == 1) {System.out.println("0 " +gold.word + "  " + gold.synsetID + " -> Mapping >> " + mapping.get(gold.synsetID) +" << "  + test.synsetID);}
                }
            }
        }

        float precision = (float)correct/(float)(correct+incorrect);
        System.out.println("Precision: "+ precision);
        float recall = ((float)(correct+incorrect)/(float)(true_negative+correct+incorrect));
        System.out.println("Recall: "+recall);
        System.out.println("F1: "+ 2*precision*recall/(precision+recall));
    }

    public static void main(String[] args) {
        WordNet w = new WordNet("data/wordnet");
//        //Test WSD workflow
//        GroupFinder g = new GroupFinder();
//        DefaultMutableTreeNode tree = g.getTree("(TOP (S (NP (NNP John) (NNP White) (NNP Smith)) (VP (VBD has) (VBD took) (NP (PRP$ his) (NN mother)) (PP (TO to) (NP (DT the) (NN theater))))(. .)))");
//        //DefaultMutableTreeNode tree = g.getTree("(TOP (S (NP (NNP John))))");
//        DefaultMutableTreeNode WSDtree = w.transformToWSDTree(tree);
//        w.printWSDTree(WSDtree);
//        w.solveWSDTree(WSDtree);
//        tree = g.getTree("(TOP (S (NP (NNP Russian) (NNP Foreign) (NNP Minister) (NNP Sergei) (NNP Lavrov)) (VP (VBZ has) (VP (VBD said) (SBAR (S (NP (NP (NNP Syria)(POS 's)) (NN opposition)) (VP (MD will) (ADVP (RB never)) (VP (VB defeat) (NP (NP (DT the) (NN country)(POS 's)) (JJ armed) (NNS forces)) (SBAR (RB even) (IN if) (S (NP (PRP it)) (VP (VBZ is) (VP (VBN \"armed) (PP (TO to) (NP (DT the) (NNS teeth\")))))))))))))(. .)))");
//        WSDtree = w.transformToWSDTree(tree);
//        w.printWSDTree(WSDtree);
//        w.solveWSDTree(WSDtree);

        //TEST WSD Accuracy using Gold Set
        HashMultimap<String,String> mapping = HashMultimap.create();
        w.loadTestMapping("data/wordnet/test/wn16-30.adj", "a", mapping);
        w.loadTestMapping("data/wordnet/test/wn16-30.adv", "r", mapping);
        w.loadTestMapping("data/wordnet/test/wn16-30.noun", "n", mapping);
        w.loadTestMapping("data/wordnet/test/wn16-30.verb", "v", mapping);

        w.evaluate("data/wordnet/test/N05_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/N09_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/N10_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/N11_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/N12_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/N14_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/N15_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J05_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J06_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J07_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J08_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J09_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J10_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J12_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J17_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J22_SNS.dat", mapping);
        w.evaluate("data/wordnet/test/J23_SNS.dat", mapping);
    }
}
        

