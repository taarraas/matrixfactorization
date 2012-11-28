/**
 * @fileOverview Fillmore.java
 * @author Andrey Nikonenko
 * @version 0.9
 * @date Apr 10, 2012
 * @modified Apr 26, 2012
 * @modifiedby Andrey Nikonenko
 * @param Created in Taras Shevchenko National University of Kyiv (Cybernetics) under a contract between
 * @param LLC "Samsung Electronics Ukraine Company" (Kiev Ukraine) and
 * @param Taras Shevchenko National University of Kyiv
 * @param Copyright: Samsung Electronics, Ltd. All rights reserved.
 */
/*
 * Class for extract Fillmore cases grammar elements from text.
 */

package knu.univ.lingvo.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.tree.DefaultMutableTreeNode;


/** Class contain templates created by Fillmore */

public class Fillmore {
    public class GrammarCase {
        public int start = -1;
        public int end = -1;
        public String caseType = "";
        public String patternText = "";
        public GrammarCase (String caseType, String patternText, int start, int end) {
            this.caseType = caseType;
            this.patternText = patternText;
            this.start = start;
            this.end = end;
        }

    }
    //Additional checks
    private Pattern NounCheck = Pattern.compile("(\\((?:NN|NNS|NNP|NNPS) [^\\(\\)]+\\))");
    private Pattern PrepositionalCheck = Pattern.compile("(\\((?:PRP) [^\\(\\)]+\\))");
    private Pattern notPersonalNounCheck = Pattern.compile("(\\((?:NN|NNS) [^\\(\\)]+\\))");
    
    //The route along which an entity (i.e., a theme) travels, physically or otherwise
    private Pattern PATH = Pattern.compile("(\\(PP \\(IN (?:[aA]long|[dD]own|[uU]p|[tT]hrough|[vV]ia|[bB]y way of|[aA]round)\\) (?:\\(NP (?: ?\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?: ?\\([^\\(\\)]+\\))+\\))+|(?: ?\\([^\\(\\)]+\\))+|[^\\(\\)]+)+\\))+\\))\\))");
    //The style in which something is done.
    private Pattern MANNER = Pattern.compile("(\\(ADVP (?: ?\\(RB (?![nN]ow\\b|[nN]ever\\b|[eE]ither\\b|[aA]way\\b)\\w+\\))+\\))");
    //The place where an event takes place or where an object exists
    // NN  = noun.artifact | noun.location | noun.food
    // or NN = NNP | NNPS
    private Pattern LOCATION_N = Pattern.compile("(\\(PP \\(IN (?:[oO]n|[iI]n|[aA]t|[aA]bove|[uU]nder)\\) (?:\\(NP (?: ?\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?: ?\\([^\\(\\)]+\\))+\\))+|(?: ?\\([^\\(\\)]+\\))+|[^\\(\\)]+)+\\))+\\))\\))");
    //An endpoint for various types of movement and transfer (used in verbs of motion, transfer of possession, mental transfer, etc.)
    // NN  = noun.artifact | noun.location | noun.food
    // or  NN = NNP | NNPS & noun.location
    private Pattern DESTINATION_N = Pattern.compile("(\\(PP \\((?:TO|IN) (?:[tT]o|[tT]oward|[tT]owards)\\) (?:\\(NP (?: ?\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?: ?\\([^\\(\\)]+\\))+\\))+|(?: ?\\([^\\(\\)]+\\))+|[^\\(\\)]+)+\\))+\\))\\))");
    //A starting point for various types of movement and transfer (used in verbs of motion, transfer of possession, mental transfer, etc.)
    private Pattern SOURCE = Pattern.compile("(\\(PP \\(IN (?:[fF]rom)\\) (?:\\(NP (?: ?\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?: ?\\([^\\(\\)]+\\))+\\))+|(?: ?\\([^\\(\\)]+\\))+|[^\\(\\)]+)+\\))+\\))\\))");
    //The object or event that is used in order to carry out an action.
    private Pattern INSTRUMENT = Pattern.compile("\\((?!VBN)[^\\(\\)]+ [^\\(\\)]+\\)+ (\\(PP \\(IN (?:[wW]ith|[bB]y)\\) \\([^\\(\\)]+ (?: ?\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?: ?\\([^\\(\\)]+\\))+\\))+|(?: ?\\([^\\(\\)]+\\))+|[^\\(\\)]+)+\\))|(?: ?\\([^\\(\\)]+ (?: ?\\([^\\(\\)]+\\))+\\))+|(?: ?\\([^\\(\\)]+\\)\\))+|[^\\(\\)]+\\))+)+\\)\\))");
    //The entity that causes or is responsible for an action
    // - all OBJECTS
    private Pattern AGENT__ = Pattern.compile("\\(S (\\(NP (?: ?\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?: ?\\([^\\(\\)]+\\))+\\))+|(?: ?\\([^\\(\\)]+\\))+|[^\\(\\)]+)+\\))+\\)) \\(VP ");
    // The entity that is affected by an action (noun.person)
    // NN = noun.person
    private Pattern PATIENT_N = Pattern.compile("(\\(PP \\(TO to\\) (?:\\(NP (?: ?\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?: ?\\([^\\(\\)]+\\))+\\))+|(?: ?\\([^\\(\\)]+\\))+|[^\\(\\)]+)+\\))+\\))\\))");
    // PP = for
    // NN = noun.time
    private Pattern DURATION_NN = Pattern.compile("(\\(PP \\(IN [fF]or\\) (?:\\(NP (?: ?\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?: ?\\([^\\(\\)]+\\))+\\))+|(?: ?\\([^\\(\\)]+\\))+|[^\\(\\)]+)+\\))+\\))\\))");

    //The entity manipulated by an action
    // - all PATIENT
    //Noun after verb without PP
    private Pattern OBJECT1__ = Pattern.compile("\\(VP .+?(\\(NP (?!\\(PP \\()(?: ?(?!\\(PP \\()\\([^\\(\\)]+ (?:(?: ?\\([^\\(\\)]+ (?: ?(?!\\(PP \\()\\([^\\(\\)]+\\))+\\))+|(?: ?(?!\\(PP \\()\\([^\\(\\)]+\\))+|[^\\(\\)]+)+\\))+\\))");
    //Noun before passive verb
    private Pattern OBJECT2__ = Pattern.compile(".*\\(S (\\(NP .+?) (?:\\(VP \\((?:VB[A-Z]{0,1}|MD) .+?\\) )?\\(VP \\(VB[A-Z]{0,1} (?:be|were|was|being|been|are|is|am)\\) \\(VP \\(VBN ");
    //Noun group after verbs: said, claims, ask
    private Pattern OBJECT3__ = Pattern.compile("\\(VP \\(VBD (?:said|say|claims|claimed|ask|asked)\\) (\\(SBAR \\(S .+?\\){5,})\\((?:, ,|\\. \\.)");

    // Object property stay before object
    private Pattern OBJECT_STATE1 = Pattern.compile("((?: ?\\(JJ[A-Z]{0,1} [^\\(\\)]+\\))+)((?: ?\\(NN[A-Z]{0,2} [^\\(\\)]+\\))+)\\)");
    // Object property stay after verb
    private Pattern OBJECT_STATE2 = Pattern.compile("((?: ?\\(NN[A-Z]{0,2} [^\\(\\)]+\\))+)\\) \\(VP \\([^\\(\\)]+\\) (?:\\(ADJP((?: ?\\(JJ[A-Z]{0,1} [^\\(\\)]+\\))+))");

    // Clear stop word pattern
    private String Stop_words = "( \\((CC|IN|DT|UH|TO|WDT) [^\\(\\)]+\\))";
    // Clear brackets & labels around word
    private String Word_brackets = "(\\([A-Z\\$\\.\\,\\-\\:]+ ([^\\(\\)]+)\\))"; // \\? \\!
    // Clear brackets at phrase & clause level
    private String Group_brackets = "(\\([A-Z\\$\\-\\:]+ )";
    // Clear senseless brackets
    private String Senseless_brackets = "(\\(([\\w]*)\\))";


    /** returns word type from WSD */
    private String get_group_type(String group, DefaultMutableTreeNode WSDTree){
        if (WSDTree==null) {return "";}
        Enumeration en = WSDTree.preorderEnumeration();
        String lexicograph_id = "";
        while (en.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
          WSDTreeNode WSDnode = ((WSDTreeNode)node.getUserObject());
          if (WSDnode.phrase.equals(group)) {
              if (WSDnode.synsets.size()>0) {
                lexicograph_id = WSDnode.synsets.get(0).lexicalInfo;
              }
              break;
          }
        }
//        if (lexicograph_id.isEmpty()) {System.out.println("Test Group |" + group + "| Not found");}
//        else {System.out.println("Test Group |" + group + "| " + lexicograph_id);}
        return lexicograph_id;
    }

    /** Check existens of specific template at template list */
    private String findCaseType(String templateText, ArrayList<GrammarCase> templates){
        for (GrammarCase c:templates) {
            if (c.patternText.equals(templateText)) {return c.caseType;}
        }
        return "";
    }

    /** Check existens part of specific template at template list */
    private String findPartCaseType(String templateText, ArrayList<GrammarCase> templates){
        GrammarCase delElement =null;
        for (GrammarCase c:templates) {
            if (c.patternText.contains(templateText)) {return c.caseType;}
            if (templateText.contains(c.patternText)) {
               if (c.caseType.equals("OBJECT")){
                  delElement = c;
               } else {return c.caseType;}
            }
        }
        if (delElement != null) {templates.remove(delElement);}
        return "";
    }


    /** Add semantic labels to words in sentence.
     *  Labels hold in WSD tree
     */
    private String addWSDLabels(DefaultMutableTreeNode tree, String sentence) {
        if (tree==null) {return sentence;}
        ArrayList<String> phrases = new ArrayList<String>();
        String s = sentence;
        Enumeration en = tree.preorderEnumeration();
        while (en.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
          WSDTreeNode WSDnode = ((WSDTreeNode)node.getUserObject());
          if (!WSDnode.word.isEmpty() && WSDnode.synsets.size()>0 && !phrases.contains(WSDnode.phrase)) {
              String pos = WSDnode.POS.toLowerCase().substring(0, 1);
              if (pos.equals("n")) {pos = "noun.";}
              if (pos.equals("v")) {pos = "verb.";}
              if (pos.equals("j")) {pos = "adjective.";}
              if (pos.equals("r")) {pos = "adverb.";}
              String lexicograph_id = "<" +pos+WSDnode.synsets.get(0).lexicalInfo + ">";
              s = s.replace((CharSequence)WSDnode.phrase, WSDnode.phrase+lexicograph_id);
              phrases.add(WSDnode.phrase);
          }
        }
        return s;
    }

    /** Delete stop words from sentence: conjunctions, prepositions, determiners, interjection, to, wh-determiner
     * Clear brackets near word
     * Clear brackets at phrase & clause level
    */
    private String clearSentence(String sentence){
        String s = sentence.replaceAll(Stop_words, " ");
        Matcher m = Pattern.compile(Word_brackets).matcher(s);
        StringBuffer sb = new StringBuffer(sentence.length());
        while (m.find()) {
          String text = m.group(2);
          m.appendReplacement(sb, Matcher.quoteReplacement(text));
        }
        m.appendTail(sb);
        s = sb.toString().replaceAll(Group_brackets, "(");
        s = s.replaceAll("\\( +", "(");
        s = s.replaceAll(" +", " ");

        // Clear senseless brackets
        m = Pattern.compile(Senseless_brackets).matcher(s);
        sb = new StringBuffer(s.length());
        while (m.find()) {
          String text = m.group(2);
          m.appendReplacement(sb, Matcher.quoteReplacement(text));
        }
        m.appendTail(sb);
        s = sb.toString();
        return s;
    }

    //Find Fillmore grammar cases in text
    public ArrayList<GrammarCase> parse (String sentence, DefaultMutableTreeNode WSDTree) {
        Matcher m, m2;
        ArrayList<GrammarCase> templates = new ArrayList<GrammarCase>();
        m = PATH.matcher(sentence);
        while (m.find()){ int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("PATH",m.group(1),start,start+m.group(1).length()); templates.add(c); 
        //  System.out.println("PATH  " + m.group(1));
        }
        m = MANNER.matcher(sentence);
        while (m.find()){ int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("MANNER",m.group(1),start,start+m.group(1).length()); templates.add(c); 
        //  System.out.println("MANNER  " + m.group(1));
        }
        m = SOURCE.matcher(sentence);
        while (m.find()){ int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("SOURCE",m.group(1),start,start+m.group(1).length()); 
        //  System.out.println("SOURCE  " + m.group(1));
        }
        m = INSTRUMENT.matcher(sentence);
        while (m.find()){ int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("INSTRUMENT",m.group(1),start,start+m.group(1).length()); templates.add(c); 
        //  System.out.println("INSTRUMENT  " + m.group(1));
        }
        m = LOCATION_N.matcher(sentence);
        while (m.find()){ // use semantic rules
            m2 = NounCheck.matcher(m.group(1));
            boolean result = false;
            if (m2.find()){
               if (m2.group(1).startsWith("(NNP")){
                 result = true;
               } else if (m2.group(1).startsWith("(NN")){
                   String word_type = get_group_type(m2.group(1),WSDTree);
                   if (word_type.equals("artifact") || word_type.equals("location") || word_type.equals("food")) {
                       result = true; 
                   }
               } else {
                 result = false;
               }
            }
            if (result) {int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("LOCATION",m.group(1),start,start+m.group(1).length()); templates.add(c); 
            //  System.out.println("LOCATION_N  " + m.group(1));
            }
        }
        m = DESTINATION_N.matcher(sentence);
        while (m.find()){ // use semantic rules
            m2 = NounCheck.matcher(m.group(1));
            boolean result = false;
            if (m2.find()){
               if (m2.group(1).startsWith("(NNP")){
                   String word_type = get_group_type(m2.group(1),WSDTree);
                   if (word_type.equals("location") ) {
                       result = true;
                   }
               } else if (m2.group(1).startsWith("(NN")){
                   String word_type = get_group_type(m2.group(1),WSDTree);
                   if (word_type.equals("artifact") || word_type.equals("location") || word_type.equals("food") || word_type.equals("substance")) {
                       result = true;
                   }
               } else {
                 result = false;
               }
            }
            if (result) { int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("DESTINATION",m.group(1),start,start+m.group(1).length()); templates.add(c); 
            //  System.out.println("DESTINATION_N  " + m.group(1));
            }
        }
        m = PATIENT_N.matcher(sentence);
        while (m.find()){ // use semantic rules
            m2 = NounCheck.matcher(m.group(1));
            boolean result = false;
            if (m2.find()){
               if (m2.group(1).startsWith("(NN")){
                   String word_type = get_group_type(m2.group(1),WSDTree);
                   if (word_type.equals("person") ) {
                       result = true;
                   }
               } else {
                 result = false;
               }
            }
            if (!result) {
                m2 = PrepositionalCheck.matcher(m.group(1));
                result = m2.find();
            }
            if (result) { int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("PATIENT",m.group(1),start,start+m.group(1).length()); templates.add(c); 
            //  System.out.println("PATIENT_N  " + m.group(1));
            }
        }
        m = DURATION_NN.matcher(sentence);
        while (m.find()){ // use semantic rules
            m2 = notPersonalNounCheck.matcher(m.group(1));
            boolean result = false;
            if (m2.find()){
               if (m2.group(1).startsWith("(NN")){
                   String word_type = get_group_type(m2.group(1),WSDTree);
                   if (word_type.equals("time") ) {
                       result = true;
                   }
               } else {
                 result = false;
               }
            }
            if (result) { int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("DURATION",m.group(1),start,start+m.group(1).length()); templates.add(c);
            //  System.out.println("DURATION_NN  " + m.group(1));
            }
        }

        m = OBJECT1__.matcher(sentence);
        while (m.find()){ // use semantic rules
            String templateType = findCaseType(m.group(1), templates);
            String templateType2 = findPartCaseType(m.group(1), templates);
            if (templateType.isEmpty() && templateType2.isEmpty()){ // _PATH.contains(object) || _SOURCE.contains(object) || _INSTRUMENT.contains(object) || _LOCATION_N.contains(object) || _DESTINATION_N.contains(object) || _PATIENT_N.contains(object) || _DURATION_NN.contains(object)){
                int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("OBJECT",m.group(1),start,start+m.group(1).length()); templates.add(c);
                //  System.out.println("OBJECT1__  " + m.group(1));
            }
        }
        m = OBJECT2__.matcher(sentence);
        while (m.find()){ // use semantic rules
            String templateType = findCaseType(m.group(1), templates);
            String templateType2 = findPartCaseType(m.group(1), templates);
            if (templateType.isEmpty() && templateType2.isEmpty()){ // _PATH.contains(object) || _SOURCE.contains(object) || _INSTRUMENT.contains(object) || _LOCATION_N.contains(object) || _DESTINATION_N.contains(object) || _PATIENT_N.contains(object) || _DURATION_NN.contains(object)){
                int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("OBJECT",m.group(1),start,start+m.group(1).length()); templates.add(c);
                //  System.out.println("OBJECT2__  " + m.group(1));
            }
        }
        m = OBJECT3__.matcher(sentence);
        while (m.find()){ // use semantic rules
            String templateType = findCaseType(m.group(1), templates);
            String templateType2 = findPartCaseType(m.group(1), templates);
            if (templateType.isEmpty() && templateType2.isEmpty()){ // _PATH.contains(object) || _SOURCE.contains(object) || _INSTRUMENT.contains(object) || _LOCATION_N.contains(object) || _DESTINATION_N.contains(object) || _PATIENT_N.contains(object) || _DURATION_NN.contains(object)){
                int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("OBJECT",m.group(1),start,start+m.group(1).length()); templates.add(c);
                //  System.out.println("OBJECT3__  " + m.group(1));
            }
        }
        m = AGENT__.matcher(sentence);
        while (m.find()){ // use semantic rules
            String templateType = findCaseType(m.group(1), templates);
            String templateType2 = findPartCaseType(m.group(1), templates);
            if (templateType.isEmpty() && templateType2.isEmpty()){ // _PATH.contains(object) || _SOURCE.contains(object) || _INSTRUMENT.contains(object) || _LOCATION_N.contains(object) || _DESTINATION_N.contains(object) || _PATIENT_N.contains(object) || _DURATION_NN.contains(object) || _OBJECT1__.contains(object) || _OBJECT2__.contains(object) || _OBJECT3__.contains(object)){
                int start = sentence.indexOf(m.group(1), m.start()); GrammarCase c = new GrammarCase("AGENT",m.group(1),start,start+m.group(1).length()); templates.add(c);
                //  System.out.println("AGENT__  " + m.group(1));
            }
        }

        int i=1;
        m = OBJECT_STATE1.matcher(sentence);
        while (m.find()){
            String templateType = findCaseType(m.group(1).trim(), templates);
            String templateType2 = findCaseType(m.group(2).trim(), templates);
            if ((templateType.isEmpty() || !templateType.startsWith("STATE")) &&
                (templateType2.isEmpty() || !templateType.endsWith("_OWNER"))){
                String group = m.group(1).trim();
                int start = sentence.indexOf(group, m.start());
                GrammarCase c = new GrammarCase("STATE"+i,group,start,start+group.length());
                templates.add(c);
                group = m.group(2).trim();
                start = sentence.indexOf(group, m.start());
                c = new GrammarCase("STATE"+i+"_OWNER",group.trim(),start,start+group.length());
                templates.add(c);
                i++;
                //  System.out.println("STATE1:  " + m.group(1) + "   OBJECT:  " + m.group(2));
            }
        }
        m = OBJECT_STATE2.matcher(sentence);
        while (m.find()){
            String templateType = findCaseType(m.group(2).trim(), templates);
            String templateType2 = findCaseType(m.group(1).trim(), templates);
            if ((templateType.isEmpty() || !templateType.startsWith("STATE")) &&
                (templateType2.isEmpty() || !templateType.endsWith("_OWNER"))){
                String group = m.group(2).trim();
                int start = sentence.indexOf(group, m.start());
                GrammarCase c = new GrammarCase("STATE"+i,group,start,start+group.length());
                templates.add(c);
                group = m.group(1).trim();
                start = sentence.indexOf(group, m.start());
                c = new GrammarCase("STATE"+i+"_OWNER",group,start,start+group.length());
                templates.add(c);
                i++;
                //  System.out.println("STATE2:  " + m.group(1) + "   OBJECT:  " + m.group(2));
            }
        }

        // Add templates to sentence syntactic structure
        String s = sentence;
        String s2 = "";
        for (GrammarCase c:templates) {
            s2 = s.replace((CharSequence)c.patternText, (CharSequence)"["+c.caseType +"]"+ c.patternText + "["+c.caseType+"_END"+"]");
            if (s2.equals(s)) {System.out.println("Parsing error, can't add pattern "+c.caseType +" "+ c.patternText + " start=" +c.start + " end="+c.end + " to sentence");}
            s = s2;
            //  System.out.println(c.caseType +" "+ c.patternText + " start=" +c.start + " end="+c.end);
        }
//        System.out.println();
//        System.out.println(s);
//        System.out.println();
//        s = addWSDLabels(WSDTree,s);
//        s = clearSentence(s);
//        System.out.println(s);
        return templates;
    }


    /** Load data for Test Corpus
     *  For test purposes only
     */
    private String[] loadTestSet(String filename) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            List<String> lines = new ArrayList<String>();

            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String[] elements = line.split("<.*?>");
                if (elements.length>1) {
                    lines.add(elements[1]);
                }
            }
            in.close();
            return lines.toArray(new String[lines.size()]);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        Fillmore f = new Fillmore();
        WordNet w = new WordNet("data/wordnet");
        GroupFinder g = new GroupFinder();

//        String[] text = {"TEST_1","(TOP (SBAR (IN If) (S (NP (DT the) (NN weather)) (VP (VBZ is) (ADJP (JJ fine) (SBAR (S (NP (PRP she)) (VP (MD will) (VP (VB go) (PP (IN for) (NP (DT a) (NN walk))))))))))(. .)))","(TOP (S (SBAR (IN If) (S (NP (PRP you)) (VP (VBP put) (NP (DT a) (JJ warm) (NN water)) (PP (TO to) (NP (DT an) (NN ice))))))(, ,) (NP (PRP it)) (VP (VBZ melts))(. .)))","(TOP (S (NP (PRP He)) (VP (MD will) (VP (VB stay) (PP (IN at) (NP (NNP Moscow))) (PP (IN from) (NP (NNP Friday) (CD eleven) (NNP PM))) (PP (IN for) (NP (CD two) (NNS days)))))(. .)))",
//                         "TEST_2","(TOP (S (NP (NNP Doxo)) (VP (VBZ is) (NP (NP (DT a) (JJ free) (NN web) (NN service)) (SBAR (WHNP (WDT that)) (S (VP (VBZ enables) (S (NP (NNS people)) (VP (TO to) (VP (VB connect) (CC and) (VP (VB go) (S (VP (JJ paper-free) (PP (IN with) (NP (NP (NN household) (NN article)) (PP (IN in) (NP (CD one) (NN place))))))))))))))))(. .)))",
//                         "TEST_DESTINATION","(TOP (S (NP (NNP John)) (VP (VBD took) (NP (PRP$ his) (NN mother)) (PP (TO to) (NP (DT the) (NN theater))))(. .)))","(TOP (S (NP (NNP Cindy)) (VP (VBD brought) (NP (DT the) (NN money)) (PP (TO to) (NP (PRP me))))(. .)))","(TOP (S (NP (NNP Hilda)) (VP (VBD gave) (NP (NNP John)) (NP (DT an) (NN idea)))(. .)))","(TOP (S (NP (PRP I)) (VP (VBD turned) (PP (IN towards) (NP (NN home))))(. .)))",
//                         "TEST_INSTRUMENT","(TOP (S (NP (NNP Seymour)) (VP (VBD cut) (NP (DT the) (NNS salami)) (PP (IN with) (NP (DT a) (NN knife))))(. .)))","(TOP (S (NP (NNP Armco)) (VP (MD will) (VP (VB establish) (NP (DT a) (JJ new) (NN company)) (PP (IN by) (S (VP (VBG spinning) (PP (IN off) (NP (PRP$ its) (JJ general) (NN steel) (NN department))))))))(. .)))",
//                         "TEST_HOMOGENEOUS_ELEMENTS","(TOP (S (NP (DT The) (NNP Antiterror) (NN information) (CC and) (NN explanatory) (NN campaign)) (VP (VBZ has) (VP (VBN been) (VP (VBN launched) (PP (IN in) (NP (NNP Ukraine))) (S (VP (TO to) (VP (VB inform) (NP (NNS Ukrainians)) (PP (IN on) (SBAR (WHADVP (WRB how)) (S (VP (TO to) (VP (VP (VB behave) (PP (PP (IN in) (NP (NNS crowds))) (CC or) (PP (IN in) (NP (NP (NN case)) (PP (IN of) (NP (NNS emergencies) (CC and) (NN terrorism) (NNS threats)))))))(, ,) (CONJP (RB as) (RB well) (IN as)) (VP (VB take) (NP (NP (DT an) (JJ active) (NN position)) (PP (IN in) (NP (NP (DT the) (NN fight)) (PP (IN against) (NP (NN terrorism))))))))))))))))))(. .)))","(TOP (S (S (NP (NP (DT The) (NNS organizers)) (PP (IN of) (NP (DT the) (NN campaign)))) (VP (VBP have) (VP (VBN drawn) (PRT (RB up))))) (CC and) (S (NP (NP (NP (VBN circulated) (NN information) (NNS materials)) (PRN (-LRB- -LRB-)(NP (NP (NNS leaflets))(, ,) (NP (NNS brochures))(, ,) (NP (NNS lectures)))(-RRB- -RRB-))) (CC and) (S (VP (VBP are) (VP (VBG planning) (S (VP (TO to) (VP (VB organize) (NP (NP (NNS seminars) (CC and) (NNS lectures)) (PP (IN for) (NP (DT the) (NN public))))(, ,) (PP (PP (IN at) (NP (NNS schools)))(, ,) (CC and) (PP (IN at) (NP (NNS enterprises))))))))))))(. .)))","(TOP (S (NP (JJ Little) (NN progress)) (VP (VBZ is) (VP (VBN expected) (ADVP (RB either)) (PP (PP (IN on) (NP (DT the) (JJ Syrian) (NN front))) (CC or) (PP (IN on) (NP (NP (JJR wider) (NNS tensions)) (PP (IN between) (NP (NP (NNP Shia) (CC and) (NNP Sunni) (NNS factions)) (PP (IN in) (NP (DT the) (NN region))))))))))(. .)))",
//                         "TEST_0","(TOP (S (NP (PRP He)) (VP (MD will) (VP (VB stay) (PP (IN at) (NP (NNP Moscow))) (PP (IN from) (NP (NNP Friday) (CD eleven) (NNP P.M.))) (PP (IN for) (NP (CD two) (NNS days)))))(. .)))",
//                         "TEST_LONG_CHAIN","(TOP (S (NP (JJ Arab) (NNS leaders)) (VP (VBP have) (VP (VBN begun) (NP (NP (NNS talks)) (PP (IN on) (NP (NP (JJ UN-backed) (NN peace) (NN plan)) (PP (IN for) (NP (NNP Syria))) (PP (IN at) (NP (NP (DT the) (JJ first) (JJ major) (JJ international) (NN summit)) (SBAR (S (VP (TO to) (VP (VB be) (VP (VBN hosted) (PP (IN by) (NP (NP (NNP Iraq)) (PP (IN in) (NP (NNS decades)))))))))))))))))(. .)))",
//                         "TEST_PATIENT","(TOP (S (NP (NNP Mary)) (VP (VBD gave) (NP (DT a) (NN book)) (PP (TO to) (NP (NNP John))))(. .)))","(TOP (S (NP (NNP Fred)) (VP (VBD heard) (NP (NN music)))(. .)))","(TOP (S (NP (NNP Bill)) (VP (VBD found) (S (NP (PRP himself)) (VP (VBN entranced))))(. .)))",
//                         "TEST_PATH","(TOP (S (NP (NNP Mary)) (VP (VBD ran) (PP (IN down) (NP (DT the) (NN hill))))(. .)))","(TOP (S (NP (DT The) (NN plane)) (VP (VBD took) (NP (NP (DT the) (JJ polar) (NN route)) (PP (IN from) (NP (NNP Korea))) (PP (TO to) (NP (NNP Chicago)))))(. .)))","(TOP (S (NP (PRP He)) (VP (VBD went) (PP (IN through) (NP (NP (DT a) (NN lot)) (PP (IN of) (NP (NN adversity))))) (S (VP (TO to) (VP (VB get) (PP (TO to) (SBAR (WHADVP (WRB where)) (S (NP (PRP he)) (VP (VBZ is) (ADVP (RB now))))))))))(. .)))",
//                         "TEST_MANNER","(TOP (S (NP (PRP She)) (VP (VBZ writes) (ADVP (RB easily)))(. .)))","(TOP (S (NP (NNP Bell) (NNP Atlantic)) (VP (VBD acquired) (NP (NNP GTE)) (ADVP (RB very) (RB fast)))(. .)))",
//                         "TEST_THEME","(TOP (S (NP (NNP John)) (VP (VBD kicked) (NP (DT the) (NN ball)))(. .)))","(TOP (S (NP (DT The) (NN price)) (VP (VBZ is) (ADJP (JJ high)))(. .)))","(TOP (S (NP (DT The) (NN ball)) (VP (VBD rolled) (PP (IN down) (NP (DT the) (NN hill))))(. .)))","(TOP (S (NP (NNP John)) (VP (VBD said) (SBAR (IN that) (S (NP (NNP Mary)) (VP (VBD was) (ADVP (RB away))))))(. .)))","(TOP (S (NP (NNP Bridgestone) (NNP Sports) (NNP Co.)) (VP (VBZ has) (VP (VBN set) (PRT (RP up))  (NP (DT a) (NN company)) (PP (IN in) (NP (NNP Taiwan))) (PP (IN with) (NP (NP (DT a) (JJ local) (NN concern)) (CC and) (NP (DT a) (JJ Japanese) (NN trading) (NN house))))))(. .)))",
//                         "TEST_AGENT","(TOP (S (NP (NNP Kathy)) (VP (VBD ran) (PP (TO to) (NP (DT the) (NN store))))(. .)))","(TOP (S (NP (DT The) (NN storm)) (VP (VBD broke) (NP (DT some) (NNS windows)))(. .)))","(TOP (S (NP (NNP Du) (NNP Pont) (NNP Co.)) (VP (VBD said) (SBAR (S (NP (PRP it)) (VP (VBD agreed) (S (VP (TO to) (VP (VB form) (NP (DT a) (JJ joint) (NN venture)) (PP (IN in) (NP (NN gas) (NN separation) (NN technology))) (PP (IN with) (NP (NP (NNP L'Air) (NNP Liquide) (NNP S.A.))(, ,) (NP (NP (DT an) (JJ industrial) (NN gas) (NN company)) (VP (VBN based) (PP (IN in) (NP (NNP Paris))))))))))))))(. .)))",
//                         "TEST_LOCATION","(TOP (S (NP (DT The) (NN milk)) (VP (VBZ is) (PP (IN in) (NP (DT the) (NN refrigerator))))(. .)))","(TOP (S (NP (NP (DT The) (NN play)) (PP (IN by) (NP (NNP Marlowe)))) (VP (MD will) (VP (VB be) (VP (VBN performed) (PP (IN at) (NP (DT the) (NNP Shakespeare) (NNP Theater))))))(. .)))",
//                         "TEST_OBJECT_STATE","(TOP (S (NP (DT The) (NN car)) (VP (VBD was) (ADJP (JJ red)))(. .)))","(TOP (S (NP (DT The) (NN artifact)) (VP (VBD was) (VP (VBN burried) (PP (ADVP (JJ deep)) (IN under) (NP (NN ground)))))(. .)))",
//                         "TEST_TEXT","(TOP (S (NP (NNP Russian) (NNP Foreign) (NNP Minister) (NNP Sergei) (NNP Lavrov)) (VP (VBZ has) (VP (VBD said) (SBAR (S (NP (NP (NNP Syria)(POS 's)) (NN opposition)) (VP (MD will) (ADVP (RB never)) (VP (VB defeat) (NP (NP (DT the) (NN country)(POS 's)) (JJ armed) (NNS forces)) (SBAR (RB even) (IN if) (S (NP (PRP it)) (VP (VBZ is) (VP (VBN \"armed) (PP (TO to) (NP (DT the) (NNS teeth\")))))))))))))(. .)))","(TOP (S (NP (NNP Mr) (NNP Lavrov)) (VP (VBD warned) (SBAR (IN that) (S (NP (EX there)) (VP (MD would) (VP (VB be) (NP (JJ \")(NN slaughter)) (PP (IN for) (NP (JJ many) (NNS years)))(, \") (SBAR (IN if) (S (NP (NP (NNP Western)) (CC and) (NP (JJ Arab) (NNS states))) (VP (VP (VBD intervened) (ADVP (RB militarily))) (CC and) (VP (VBD supplied) (NP (NNS weapons)) (S (VP (TO to) (VP (VB rebel) (NP (NNS groups))))))))))))))(. .)))",
//                            "(TOP (S (NP (NNP Gulf) (NNS states)) (VP (VBD agreed) (PP (IN on) (NP (NNP Sunday))) (S (VP (TO to) (VP (VB pay) (NP (NP (DT the) (NNS salaries)) (PP (IN of) (NP (NNP Free) (NNP Syrian) (NNP Army) (NNS fighters))))))))(. .)))","(TOP (S (ADVP (RB Meanwhile))(, ,) (NP (DT the) (NNP US)) (VP (VBZ has) (VP (VBN warned) (S (NP (DT the) (JJ Syrian) (NN government)) (RB not) (VP (TO to) (VP (VB intensify) (NP (NN violence)) (ADVP (RB ahead) (PP (IN of) (NP (NP (DT a) (NN ceasefire)) (ADJP (JJ due) (PP (IN on) (NP (CD 10) (NNP April))))))))))))(. .)))","(TOP (S (NP (NP (DT The) (NNP US) (JJ permanent) (NN representative)) (PP (TO to) (NP (NP (DT the) (NNP UN))(, ,) (NP (NNP Susan) (NNP Rice)))))(, ,) (VP (VBD said) (SBAR (S (NP (NP (PRP$ its) (NNS actions)) (PP (IN since) (NP (CD 1) (NNP April)))) (VP (VBD did) (RB not) (VP (VB encourage) (VP (VB hope) (SBAR (IN that) (S (NP (PRP it)) (VP (MD would) (VP (VB comply) (PP (IN with) (NP (NP (DT the) (JJ six-point) (NN peace) (NN plan)) (VP (VBN proposed) (PP (IN by) (NP (NNP UN) (CC and) (NNP Arab) (NNP League) (NN envoy))) (NP (NNP Kofi) (NNP Annan)))))))))))))))(. .)))",
//                            "(TOP (S (NP (NNP Ms) (NNP Rice)) (VP (VBD said) (SBAR (S (NP (DT the) (NNP US)) (VP (VBD was) (VP (ADVP (RB \"))(VBN concerned) (CC and) (ADJP (RB quite) (JJ sceptical)) (SBAR (IN that) (S (S (NP (NP (DT the) (NN government)) (PP (IN of) (NP (NNP Syria)))) (VP (MD will) (ADVP (RB suddenly)) (VP (VB adhere) (PP (TO to) (NP (PRP$ its) (NNS commitments))))))(, \") (CC and) (S (NP (DT the) (NNP UN)) (VP (MD would) (VP (VB \"need) (S (VP (TO to) (VP (VB respond) (PP (TO to) (NP (DT that) (NN failure))) (PP (IN in) (NP (DT a) (ADJP (RB very) (JJ urgent) (CC and) (JJ serious)) (NN way)(NNS \"))))))))))))))))(. .)))","(TOP (S (S (NP (DT The) (JJ Syrian) (NN government)) (VP (VBZ has) (VP (VBD said) (SBAR (S (NP (PRP it)) (VP (VBZ has) (VP (VBN agreed) (PP (TO to) (NP (DT the) (NN deadline))))))))))(, ,) (CC but) (S (NP (NNS activists)) (VP (VBP accuse) (NP (PRP it)) (PP (IN of) (S (VP (VBG stalling) (PP (IN for) (NP (NN time))) (SBAR (IN so) (S (NP (PRP it)) (VP (MD can) (VP (VP (VB crush) (NP (DT the) (NN uprising)) (PP (IN before) (NP (NNP UN) (NNS monitors) (NN arrive)))) (CC and) (VP (VB say) (SBAR (S (NP (NNS attacks)) (VP (VBP are) (VP (VBG continuing)))))))))))))))(. .)))","(TOP (S (S (PP (IN On) (NP (NNP Tuesday)))(, ,) (NP (ADVP (IN at) (JJS least)) (CD 58) (NNS civilians)) (VP (VBD were) (VP (VBN killed)(, ,) (PP (VBG including) (NP (NP (CD 20)) (PP (IN in) (NP (NP (JJ military) (NNS assaults) (CC and) (NNS clashes)) (PP (IN between) (NP (NP (NNS troops) (CC and) (NNS rebels)) (PP (IN in) (NP (NNP Taftanaz)))))))))(, ,) (PP (IN in) (NP (NP (DT the) (JJ northern) (NN province)) (PP (IN of) (NP (NNP Idlib))))))))(, ,) (NP (NP (DT the) (JJ Syrian) (NNP Observatory)) (PP (IN for) (NP (NNP Human) (NNPS Rights)))) (VP (VBD said))(. .)))","(TOP (S (S (NP (CD Eighteen) (NNS soldiers)) (VP (VBD died) (PP (IN in) (NP (NNP Homs)(, ,) (NNP Idlib) (CC and) (NNP Deraa) (NNS provinces)))))(, ,) (NP (PRP it)) (VP (VBD added))(. .)))",
//                         "TEST_SOURCE","(TOP (S (NP (DT The) (NNS goods)) (VP (MD will) (VP (VB be) (VP (VBN shipped) (PP (IN from) (NP (NNP Japan))))))(. .)))","(TOP (S (NP (NNP Susan)) (VP (VBD bought) (NP (DT the) (NN book)) (PP (IN from) (NP (NNP Jane))))(. .)))","(TOP (S (NP (NNP TWA) (NNP Flight) (NN 884)) (VP (VBD left) (NP (NNP JFK)) (PP (IN at) (NP (RB about) (CD 11) (NN p.m.))))))",
//                         "TEST_TIME_PROCESSING","(TOP (S (NP (PRP He)) (VP (MD will) (VP (VB be) (PP (IN at) (NP (NNP Moscow))) (PP (IN from) (NP (NNP Friday) (NN night))) (PP (IN for) (NP (CD 2) (NNS days)))))(. .)))","(TOP (S (NP (PRP She)) (VP (VBD was) (VP (VBG travelling) (PP (IN from) (NP (NNP Paris))) (S (VP (TO to) (VP (VB home) (NP (JJ several) (NNS days)) (PP (IN until) (NP (DT the) (NNP August) (CD 11))))))))(. .)))};"};

        String[] text = f.loadTestSet("data/wordnet/test/SyntaxParsed.txt");
        int i = 0;
        for (String sentence:text){
            i++;
            //if (i < 19) {continue;}
            if (i == 3) {break;}
            System.out.println(sentence);
            // Data transformation step
            DefaultMutableTreeNode tree = g.getTree(sentence);
//            g.printTree(tree);
            DefaultMutableTreeNode WSDtree = w.transformToWSDTree(tree);
//            w.printWSDTree(WSDtree);
            w.solveWSDTree(WSDtree);
//            w.printWSDTree(WSDtree);
            //Find semantic cases
            f.parse(sentence, WSDtree);
            System.out.println();
        }
        System.out.println(i);
    }

}
