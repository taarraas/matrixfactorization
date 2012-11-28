/**
 * @fileOverview Main.java
 * @author Taras Voznuk
 * @version 0.7
 * @date March 10, 2012
 * @modified Apr 28, 2012
 * @modifiedby Andrey Nikonenko
 * @param Created in Taras Shevchenko National University of Kyiv (Cybernetics) under a contract between
 * @param LLC "Samsung Electronics Ukraine Company" (Kiev Ukraine) and
 * @param Taras Shevchenko National University of Kyiv
 * @param Copyright: Samsung Electronics, Ltd. All rights reserved.
 */
/*
 * Main class in Semantic Net Subsystem,
 * used as pipeline for all Entity Tracking modules
 */
package knu.univ.lingvo.analysis;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.lang.english.TreebankLinker;
import opennlp.tools.parser.*;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import opennlp.tools.namefind.NameSample;
import knu.univ.lingvo.analysis.Fillmore.GrammarCase;
import knu.univ.lingvo.analysis.GroupFinder;
//import semvectest.LSARoutine.LSA;
//import semvectest.LSARoutine.LSAFactory;
//import semvectest.LuceneIndexRoutine.LuceneBinding;


public class Main {
    String LSADir = "data/lsa/LuceneIndex1000"; // Dir with LSA trained data
//    LSA _lsaContent = null;
    SentenceDetector _sentenceDetector = null;
    Tokenizer _tokenizer = null;
    POSTagger _posTagger = null;
    Parser _parser = null;
    Linker _treebankLinker = null;
    ETNameFinder _nameFinder = null;
    GroupFinder _groupFinder = null;
    Fillmore _Fillmore = null;
    WordNet _wordnet = null;
    HashMap<String, String> _data = new HashMap<String, String>();

    private void resolve(ArrayList<Parse> parsedSentences) throws IOException {
        int sentenceNumber = 0;
        List<Mention> document = new ArrayList<Mention>();
        for (Parse p : parsedSentences) {
            Mention[] extents = _treebankLinker.getMentionFinder().getMentions(new DefaultParse(p, sentenceNumber));
            //construct new parses for mentions which don't have constituents.
            for (int ei = 0, en = extents.length; ei < en; ei++) {
                //System.err.println("PennTreebankLiner.main: "+ei+" "+extents[ei]);

                if (extents[ei].getParse() == null) {
                    //not sure how to get head index, but its not used at this point.
                    Parse snp = new Parse(p.getText(), extents[ei].getSpan(), "NML", 1.0, 0);
                    p.insert(snp);
                    extents[ei].setParse(new DefaultParse(snp, sentenceNumber));
                }

            }
            document.addAll(Arrays.asList(extents));
            sentenceNumber++;
        }

        if (document.size() > 0) {
            DiscourseEntity[] entities = _treebankLinker.getEntities(document.toArray(new Mention[document.size()]));
            //showEntities(entities);
            (new CorefParse(parsedSentences, entities)).show();
        }
    }

    static private int findTokenCharacterStart(final String sentence,
            final String tokens[], final int tokenIndex) {
        int offset = 0;
        if (tokenIndex > 0) {
            // iterate from the 1th token
            for (int idx = 1; idx <= tokenIndex; idx++) {
                final String tok = tokens[idx];
                // find the index of the current token
                offset = sentence.indexOf(tok,
                        // start looking at the end of the previous token
                        offset + tokens[idx - 1].length());
            }
        }
        return offset;
    }

    private Parse parseSentence(final String text, final String[] tokens) {
        final Parse p = new Parse(text,
                // a new span covering the entire text
                new Span(0, text.length()),
                // the label for the top if an incomplete node
                AbstractBottomUpParser.INC_NODE,
                // the probability of this parse...uhhh...?
                1,
                // the token index of the head of this parse
                0);

        for (int idx = 0; idx < tokens.length; idx++) {
            final String tok = tokens[idx];
            // find the index of the current token
            final int start = findTokenCharacterStart(text, tokens, idx);
            // flesh out the parse with token sub-parses
            p.insert(new Parse(text,
                    new Span(start, start + tok.length()),
                    AbstractBottomUpParser.TOK_NODE,
                    0,
                    idx));
        }

        return _parser.parse(p);
    }

    void initSentenceDetect() {
        InputStream modelIn = null;
        try {
            // Loading sentence detection model
            //modelIn = getClass().getResourceAsStream("/en-sent.bin");
            modelIn = new DataInputStream(new FileInputStream("data/models/en-sent.bin"));
            final SentenceModel sentenceModel = new SentenceModel(modelIn);
            modelIn.close();

            _sentenceDetector = new SentenceDetectorME(sentenceModel);

        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (final IOException e) {
                }
            }
        }
    }

    void initTokenizer() {

        InputStream modelIn = null;
        try {
            // Loading tokenizer model
            //modelIn = getClass().getResourceAsStream("/en-token.bin");
            modelIn = new DataInputStream(new FileInputStream("data/models/en-token.bin"));
            final TokenizerModel tokenModel = new TokenizerModel(modelIn);
            modelIn.close();

            _tokenizer = new TokenizerME(tokenModel);

        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (final IOException e) {
                } // oh well!
            }
        }
    }

    void initPOSTagger() {

        InputStream modelIn = null;
        try {
            // Loading tokenizer model
            //modelIn = getClass().getResourceAsStream("/en-pos-maxent.bin");
            modelIn = new DataInputStream(new FileInputStream("data/models/en-pos-maxent.bin"));
            final POSModel posModel = new POSModel(modelIn);
            modelIn.close();

            _posTagger = new POSTaggerME(posModel);

        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (final IOException e) {
                } // oh well!
            }
        }
    }

    private void initParser() {
        InputStream modelIn = null;
        try {
            // Loading the parser model
            //modelIn = getClass().getResourceAsStream("/en-parser-chunking.bin");
            modelIn = new DataInputStream(new FileInputStream("data/models/en-parser-chunking.bin"));
            final ParserModel parseModel = new ParserModel(modelIn);
            modelIn.close();

            _parser = ParserFactory.create(parseModel);
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (final IOException e) {
                } // oh well!
            }
        }
    }

    private void initLinker() {
        try {
            String dataDir = "data/coref/";
            _treebankLinker = new TreebankLinker(dataDir, LinkerMode.TEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initNER(){
        String modelHome = "data/models/";
        String[] models = {modelHome+ETNameFinder.EN_NER_PERSON, modelHome+ETNameFinder.EN_NER_ORGANIZATION, modelHome+ETNameFinder.EN_NER_LOCATION, modelHome+ETNameFinder.EN_NER_DATE};
        _nameFinder.setModelHome(modelHome);
        _nameFinder = new ETNameFinder(models);
    }

    private void initGroupFinder(){
        _groupFinder = new GroupFinder();
    }

    private void initFillmore(){
        _Fillmore = new Fillmore();
    }

    private void InitLSA(){
//        try {
//           _lsaContent = LSAFactory.createFromLSAIndex(LSADir, LuceneBinding.NAMED_ENTITY_FIELD);
//        } catch (Exception e){
//            e.printStackTrace();
//        }
    }

    private void initWordNet(){
        _wordnet = new WordNet("data/wordnet");
    }

    /** Initialize all submodules */
    private void start_all_handlers() {
        long startTime = System.nanoTime();
        initSentenceDetect();
        System.out.println("Sentence Detector started "+(System.nanoTime() - startTime)/1000000000 + " s");
        startTime = System.nanoTime();
        initTokenizer();
        System.out.println("Tokenizer started "+(System.nanoTime() - startTime)/1000000000 + " s");
        startTime = System.nanoTime();
        initPOSTagger();
        System.out.println("POSTagger started "+(System.nanoTime() - startTime)/1000000000 + " s");
        startTime = System.nanoTime();
        initParser();
        System.out.println("Syntactic Parser started "+(System.nanoTime() - startTime)/1000000000 + " s");
        startTime = System.nanoTime();
        initLinker();
        System.out.println("Coreference started "+(System.nanoTime() - startTime)/1000000000 + " s");
        startTime = System.nanoTime();
        initNER();
        System.out.println("Named Entity Recognizer started "+(System.nanoTime() - startTime)/1000000000 + " s");
        initGroupFinder();
        initFillmore();
        startTime = System.nanoTime();
        initWordNet();
        System.out.println("WordNet dictionaries loaded "+(System.nanoTime() - startTime)/1000000000 + " s");

    }

    /** Add text for processing */
    public void add_text(String text_name, String text) {
       String new_text_name = text_name;
       int i=1;
       while (_data.containsKey(new_text_name)) {
            new_text_name = text_name+i;
            i++;
       }
       _data.put(text_name, text);
    }

    /** Add file for processing */
    public void add_file(String filename) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String text = "";
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                text = text + line + " ";
            }
            add_text(filename, text.trim());
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** Load Test texts for processing */
    private void add_test_suit() {
       add_text("Agent","Kathy ran to the store. The storm broke some windows. Du Pont Co. said it agreed to form a joint venture in gas separation technology with L'Air Liquide S.A., an industrial gas company based in Paris.");
       add_text("Theme","John kicked the ball. The price is high. The ball rolled down the hill. John said that Mary was away. Bridgestone Sports Co. has set up a company in Taiwan with a local concern and a Japanese trading house.");
       add_text("Patient","Mary gave a book to John. Fred heard music. Bill found himself entranced.");
       add_text("Instrument","Seymour cut the salami with a knife. Armco will establish a new company by spinning off its general steel department.");
       add_text("Source","The goods will be shipped from Japan. Susan bought the book from Jane. TWA Flight 884 left JFK at about 11 p.m.");
       add_text("Destination","John took his mother to the theater. Cindy brought the money to me. Hilda gave John an idea.");
       add_text("Location","The milk is in the refrigerator. The play by Marlowe will be performed at the Shakespeare Theater.");
       add_text("Path","Mary ran down the hill. The plane took the polar route from Korea to Chicago. He went through a lot of adversity to get to where he is now.");
       add_text("Manner","She writes easily. Bell Atlantic acquired GTE very fast.");
       add_text("State","The car was red. The artifact was burried deep under ground.");
       add_text("Time","He will be at Moscow from Friday night for 2 days. She was travelling from Paris to home several days until the August 11.");
       add_text("Other","Arab leaders have begun talks on UN-backed peace plan for Syria at the first major international summit to be hosted by Iraq in decades. The organizers of the campaign have drawn up and circulated information materials (leaflets, brochures, lectures) and are planning to organize seminars and lectures for the public, at schools, and at enterprises. Little progress is expected either on the Syrian front or on wider tensions between Shia and Sunni factions in the region.");

       add_file("data/texts/10.txt");
    }

    /** Print syntactic tree structure in user-friendly format */
    private ArrayList<String> buildOutput(DefaultMutableTreeNode simpleTree, DefaultMutableTreeNode fullTree, DefaultMutableTreeNode WSDtree, ArrayList<GrammarCase> patterns, String parsedSentence){
        if (simpleTree == null || WSDtree == null) {return new ArrayList<String>();}
        Enumeration en = simpleTree.preorderEnumeration();
        Enumeration wn_en = WSDtree.preorderEnumeration();
        Enumeration f_en = fullTree.preorderEnumeration();
        int i=0, stringIndex = 0;
        String word = ""; //Word
        String tagSet = ""; // Syntactic Tag
        String lexicograph_id =""; //WordNet Tag
        String semTag ="";
        ArrayList<String> XML = new ArrayList<String>();
        XML.add("<sentence>"+parsedSentence+"</sentence>\r\n");
        XML.add("<sentence_words>\r\n");
        // Iterate thru syntactic tree
        while (en.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)en.nextElement();
          tagSet = ""; word = ""; lexicograph_id = ""; semTag = "";
          if (node.isLeaf()) {
              // search index of the current word (inaccuracy with punctuation marks
              String currEl="";
              while (f_en.hasMoreElements()) {
                DefaultMutableTreeNode f_node = (DefaultMutableTreeNode)f_en.nextElement();
                if (f_node.toString().equals(node.toString())) {
//                    System.out.println(stringIndex);
                    currEl = f_node.toString().trim();
                    if (currEl.startsWith("(")) {currEl += " ";}
                    stringIndex += currEl.length();
                    break;
                }
                else {
                    currEl = f_node.toString().trim();
                    if (currEl.startsWith("(")) {currEl += " ";}
                    stringIndex += currEl.length();
                }
              }
//              System.out.println();
              //Print word number
              i++;
              XML.add("<word_id>"+i+"</word_id>\r\n");
//              System.out.print(i+"   ");

              Object[] pathToRoot = node.getUserObjectPath();
              //Print word
              if (node.toString().endsWith(")")){
                  String[] words = node.toString().split(" |\\)");
                  word = words[1];
                  XML.add("<word>"+word+"</word>\r\n");
//                  System.out.print(word +"   ");
              }

              //Print path to word (set of syntactic tags)
              for (Object pathElement:pathToRoot){
                if (pathElement!=null) {
                    // If current element word then print only first half of tag
                    if (pathElement.toString().endsWith(")")){
                        String[] element = pathElement.toString().split(" ");
                        tagSet += element[0];
//                        System.out.print(element[0] + "   ");
                        
                    } else {
                        tagSet += pathElement.toString();
//                        System.out.print(pathElement.toString());
                    }
                }
              }
              XML.add("<syntax_tag>"+tagSet+"</syntax_tag>\r\n");
              // Print Semantic Label
              boolean exist = false;
              for (GrammarCase c:patterns) {
                  if (c.start<=stringIndex-currEl.length() && stringIndex-currEl.length() <= c.end) {
                      semTag = "["+c.caseType +"]";
                      XML.add("<semantic_tag>"+semTag+"</semantic_tag>\r\n");
                      exist = true;
//                      System.out.print("["+c.caseType +"]");
                  }
              }
              if (!exist) {XML.add("<semantic_tag>"+"</semantic_tag>\r\n");}
          }
          // Print WordNet Label
          WSDTreeNode WSDnode = null;
          DefaultMutableTreeNode wn_node = null;
          if (wn_en.hasMoreElements()) {
              wn_node = (DefaultMutableTreeNode)wn_en.nextElement();
              WSDnode = (WSDTreeNode)(wn_node).getUserObject();
          }
          if (wn_node.isLeaf()){
              if (!WSDnode.word.isEmpty() && WSDnode.synsets.size()>0) {
                  String pos = WSDnode.POS.toLowerCase().substring(0, 1);
                  if (pos.equals("n")) {pos = "noun.";}
                  if (pos.equals("v")) {pos = "verb.";}
                  if (pos.equals("j")) {pos = "adjective.";}
                  if (pos.equals("r")) {pos = "adverb.";}
                  lexicograph_id = "<" +pos+WSDnode.synsets.get(0).lexicalInfo + ">";
    //              System.out.print(lexicograph_id+"  ");
                  XML.add("<wordnet_tag>"+lexicograph_id+"</wordnet_tag>\r\n");
              } else {
                  XML.add("<wordnet_tag>"+"</wordnet_tag>\r\n");
    //              System.out.print("      ");
              }
           }
        }
        XML.add("</sentence_words>\r\n");
        return XML;
    }
    
    public Map<String, String>[] getWordByTypePair(String text) {
        String sentences[] = _sentenceDetector.sentDetect(text);
        Map<String, String>[] ret = new Map[sentences.length];
        
        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i];
            String tokens[] = _tokenizer.tokenize(sentence);
            Parse p = parseSentence(sentence, tokens);
            StringBuffer sb = new StringBuffer();
            p.show(sb);
            String parsedSentence = sb.toString();
            DefaultMutableTreeNode tree = _groupFinder.getTree(parsedSentence);
            DefaultMutableTreeNode WSDtree = _wordnet.transformToWSDTree(tree);
            _wordnet.solveWSDTree(WSDtree);
            ArrayList<GrammarCase> patterns = _Fillmore.parse(parsedSentence, WSDtree);
            
            Map<String, String> textByType = new HashMap<String, String>();
            for (GrammarCase grammarCase : patterns) {
                 textByType.put(grammarCase.caseType, grammarCase.patternText);                 
            }
            ret[i] = textByType;
         }
        return ret;
    }

    /** Function execute all semantic routines and return Semantic Net of text wuth thematic labels*/
    public String getSemanticTaggedXML(String text){
        String sentences[] = _sentenceDetector.sentDetect(text);
        ArrayList<String> XML = new ArrayList<String>();
        XML.add("<?xml version=\"1.0\"?>\r\n");

        for (String sentence : sentences) {
            String tokens[] = _tokenizer.tokenize(sentence);
            //String posTags[] = _posTagger.tag(tokens);
            Parse p = parseSentence(sentence, tokens);
            StringBuffer sb = new StringBuffer();
            p.show(sb);
            String parsedSentence = sb.toString();
//            System.out.println(parsedSentence);
//            System.out.println("012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
            // Data transformation step
            DefaultMutableTreeNode tree = _groupFinder.getTree(parsedSentence);
            DefaultMutableTreeNode simpleTree = _groupFinder.getTree(parsedSentence);
            DefaultMutableTreeNode fullTree = _groupFinder.getFullTree(parsedSentence);
//            _groupFinder.printTree(fullTree);
            DefaultMutableTreeNode WSDtree = _wordnet.transformToWSDTree(tree);
//                _wordnet.printWSDTree(WSDtree);
            _wordnet.solveWSDTree(WSDtree);
//            _wordnet.printWSDTree(WSDtree);
            //Find semantic cases
            ArrayList<GrammarCase> patterns = _Fillmore.parse(parsedSentence, WSDtree);
            ArrayList<String> XML_part = buildOutput(simpleTree, fullTree, WSDtree, patterns, parsedSentence);

            //Thematic Tagging
            String label="";
//           label = lsa.getNearestTopic(String[] tokens_of_document_or_sentence);

            if (!label.isEmpty()){XML_part.add("<new_thematic_tag>"+label+"</new_thematic_tag>\r\n");}
            XML.addAll(XML_part);
            /*
            // Find groups
            ArrayList<Group> groups = _groupFinder.FindGroups(parsedSentence);
            for (Group g:groups){
                System.out.println(g.groupText);
                System.out.print("Group type = "+g.groupType + ":   ");
                for (Element e:g.Elements){
                   System.out.print(e.word+" ");
                }
                 System.out.println();
            }

        /* // coreference
        try {
            resolve(parsedSentences);
        } catch (IOException e) {
            e.printStackTrace();
        }
             * /
            // NER
            String[] tmp = new String[] {sentence};
            NameSample[] names = _nameFinder.findNames(tmp);
            ArrayList<NER_Group> ner_groups = new ArrayList<NER_Group>();
            for(NameSample n: names) {
               for (Span s:n.getNames()){
                    String NamedEntity = "";
                    NER_Group ng = new NER_Group(s.getType());
                    for (int i=s.getStart();i<s.getEnd();i++){
                       NamedEntity += n.getSentence()[i] + " ";
                       ng.Add(n.getSentence()[i], s.getType());
                    }
                    ng.groupText = NamedEntity.trim();
                    ner_groups.add(ng);
                    //System.out.println(s.getType() + "   " + NamedEntity);
                }
            }
            //NER output
            for (NER_Group g:ner_groups){
                System.out.print("NER Group type = "+g.groupType + ":   ");
                for (NER_Element e:g.Elements){
                   System.out.print(e.word+" ");
                }
                 System.out.println();
            }
            */

        }
        String result = "";
        for (String line:XML){
            result += line;
        }
        return result;
    }

    /** Start text analyzing process */
    private void processTest() {
//        ArrayList<Parse> parsedSentences = new ArrayList();
        for (String key:_data.keySet()) {
            System.out.println("Processing "+key);
            String text = _data.get(key);
            System.out.println(getSemanticTaggedXML(text));
            break;

        }
    }

    public Main() {
        start_all_handlers();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main m = new Main();
        m.add_test_suit();
        m.processTest();
        // TODO code application logic here
    }

}
