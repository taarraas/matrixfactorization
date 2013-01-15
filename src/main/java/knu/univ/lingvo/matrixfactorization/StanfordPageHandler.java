/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.WordStemmer;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import knu.univ.lingvo.analysis.Main;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.log4j.Logger;

/**
 *
 * @author taras
 */
public class StanfordPageHandler implements PageHandler {
    
    Logger log = Logger.getLogger("StanfordPageHandler");

    public static class Depency {

        public String type;
        public String w1;
        public String n1;
        public String w2;
        public String n2;

        @Override
        public String toString() {
            return type + ":" + w1 + ":" + n1 + ":" + w2 + ":" + n2;
        }
    }
    String parserFileOrUrl = "englishPCFG.ser.gz";
    public static String resultVec[] = {"root", "nsubj", "dobj", "iobj", "prep", "xcomp"};        
    SentenceDetector sentenceDetector;
    WordStemmer ls = new WordStemmer();
    LexicalizedParser lp = LexicalizedParser.getParserFromSerializedFile(parserFileOrUrl);

    public StanfordPageHandler() {
        initSentenceDetect();
    }

    void initSentenceDetect() {
        InputStream modelIn = null;
        try {
            // Loading sentence detection model
            //modelIn = getClass().getResourceAsStream("/en-sent.bin");
            modelIn = new DataInputStream(new FileInputStream("data/models/en-sent.bin"));
            final SentenceModel sentenceModel = new SentenceModel(modelIn);
            modelIn.close();

            sentenceDetector = new SentenceDetectorME(sentenceModel);

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
    private static final int MINIMALPARAGRAPH = 25;
    WikiModel wikiModel = new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");
    public static String indepTypeA[] = {"nsubj", "dobj", "iobj", "xcomp"};
    public static Set<String> indepTypes = new TreeSet<String>();

    static {
        for (String string : indepTypeA) {
            indepTypes.add(string);
        }
    }

    public void handleSentence(String sentence, int addWeight) {
        StringReader sr = new StringReader(sentence);
        PTBTokenizer tkzr = PTBTokenizer.newPTBTokenizer(sr);
        List toks = tkzr.tokenize();
	if (toks.size() > 100 || toks.size() < 3)
	{
		log.warn("Rejected by toks count : " + sentence);
		return;
	}
        Tree parse = (Tree) lp.apply(toks); // finally, we actually get to parse something

        // Output Option 1: Printing out various data by accessing it programmatically

        // Get words, stemmed words and POS tags
        ArrayList<String> words = new ArrayList();
        ArrayList<String> stems = new ArrayList();
        ArrayList<String> tags = new ArrayList();

        // Get words and Tags
        for (TaggedWord tw : parse.taggedYield()) {
            words.add(tw.word());
            tags.add(tw.tag());
        }

        // Get stems
        ls.visitTree(parse); // apply the stemmer to the tree
        for (TaggedWord tw : parse.taggedYield()) {
            stems.add(tw.word());
        }

        // Get dependency tree
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();

        TreeGraphNode tgn = gs.root();
        Set<TreeGraphNode> tgns = gs.getDependents(tgn);

        ArrayList<Depency> deps = new ArrayList<Depency>();
        for (TypedDependency typedDependency : tdl) {
            Depency cur = new Depency();
            String str = typedDependency.toString();
            int a = str.indexOf('(');
            cur.type = str.substring(0, a);
            int b = str.indexOf('-', a);
            cur.w1 = str.substring(a + 1, b);
            int c = str.indexOf(',', b);
            cur.n1 = str.substring(b + 1, c);
            int d = c + 1;
            int e = str.lastIndexOf('-');
            cur.w2 = str.substring(d + 1, e);
            int f = str.indexOf(')', e);
            cur.n2 = str.substring(e + 1, f);
            //System.out.println(str);
            //System.out.println(cur.toString());
            deps.add(cur);
        }
        String wordRoot = null;
        String noRootS = null;
        for (Depency depency : deps) {
            if (depency.type.equals("root")) {
                wordRoot = depency.w2;
                noRootS = depency.n2;
            }
        }

        if (wordRoot == null) {
            return;
        }

        int noRoot = -1;
        try {
            noRoot = Integer.valueOf(noRootS);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (noRoot == -1) {
            return;
        }

        if (!tags.get(noRoot - 1).startsWith("V")) {
            return;
        }

        //System.out.println(">> " + sentence);
        //System.out.println("root : " + wordRoot + " #" + noRoot);

        Map<String, String> byType = new TreeMap<String, String>();
        Map<String, Integer> byTypeNo = new TreeMap();
        Map<String, Integer> byPrepType = new TreeMap();

        String xCompNoS = null;

        for (Depency depency : deps) {
            int no = -1;
            try {
                no = Integer.valueOf(depency.n2);
            } catch (NumberFormatException e) {
                log.error(depency.n2 + e.toString());
                e.printStackTrace();
            }
            if (no == -1) {
                continue;
            }
            String tag = tags.get(no - 1);

            String word = depency.w2;
            if (!depency.type.startsWith("prep_")
                    && !depency.type.equals("xcomp")) {
                if (!tag.startsWith("N")) {
                    continue;
                } else {
                    if (tag.equals("NNP")) {
                        int nnFrom = no;
                        int nnTo = no;
                        while (nnFrom > 2 && tags.get(nnFrom - 2).equals("NNP")) {
                            nnFrom--;
                        }
                        while (nnTo < tags.size() - 1 && tags.get(nnTo).equals("NNP")) {
                            nnTo++;
                        }
                        if (nnTo != nnFrom) {
                            StringBuffer wordSb = new StringBuffer();
                            for (int i = nnFrom; i < nnTo; i++) {
                                wordSb.append(words.get(i - 1));
                                wordSb.append(" ");                                
                            }
                            wordSb.append(words.get(nnTo - 1));
                            word = wordSb.toString();// + " (" + word + ")";
                            //System.out.println("NNPGROUP : " + word);
                        }
                    }
                }
            }

            byTypeNo.put(depency.type, no);
            if (depency.n1.equals(noRootS)) {
                if (depency.type.startsWith("prep_")) {
                    String prep = depency.type.substring(5);
                    byPrepType.put(prep + " " + depency.w2, no);
                }
                if (indepTypes.contains(depency.type)) {
                    byType.put(depency.type, word);
                    if (depency.type.equals("xcomp")) {
                        xCompNoS = depency.n2;
                    }
                }
            }
        }
        if (xCompNoS != null) {
            for (Depency depency : deps) {
                int no = -1;
                try {
                    no = Integer.valueOf(depency.n2);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (no == -1) {
                    continue;
                }
                byTypeNo.put(depency.type, no);
                if (depency.n1.equals(xCompNoS)) {
                    if (depency.type.startsWith("prep_")) {
                        String prep = depency.type.substring(5);
                        byPrepType.put(prep + " " + depency.w2, no);
                    }
                    if (indepTypes.contains(depency.type)) {
                        byType.put(depency.type, depency.w2);
                    }
                }
            }
        }
        int count = byType.size();
        if (byPrepType.size() > 0) {
            count++;
        }

        //System.out.println("Count : " + count);
        for (Map.Entry<String, String> entry : byType.entrySet()) {
            String tag = tags.get(byTypeNo.get(entry.getKey()) - 1);
            //System.out.println(entry.getKey() + " : " + entry.getValue() + " (" + tag + ") #" + byTypeNo.get(entry.getKey()));
        }
        for (Map.Entry<String, Integer> entry : byPrepType.entrySet()) {
            String tag = tags.get(entry.getValue() - 1);
            //System.out.println("Prep : " + entry.getKey() + " (" + tag + ") #" + entry.getValue());
        }
        
        if (count < 2) {
            return;
        }
        
        Map<String, String> r = new TreeMap<String, String>();
        r.put("root", wordRoot);
        r.putAll(byType);
        Map<String, String> result[];
        if (byPrepType.size() == 0) {
            result = new Map[1];
            result[0] = r;
        } else {
            result = new Map[byPrepType.size()];

            int i = 0;
            for (String string : byPrepType.keySet()) {
                result[i] = new TreeMap<String, String>();
                result[i].putAll(r);
                result[i].put("prep", string);
                i++;
            }
        }
        
        for (int i = 0; i < result.length; i++) {
            good++;
            Map<String, String> map = result[i];
            String[] resVec = new String[6];
            for (int j = 0; j < resultVec.length; j++) {
                String string = resultVec[j];
                String val = map.get(string);
                resVec[j] = val;
            }
            saveVector(resVec, addWeight + 1);
        }

        //System.out.println("words: "+words); 
        //System.out.println("POStags: "+tags); 
        //System.out.println("all deps : " + tdl);
    }
    
    private static int overall = 0;
    private static int good = 0;
    
    void saveVector(String[] vector, int weight)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < vector.length; i++) {
            String string = vector[i];
            sb.append(string);
            sb.append(" ");            
        }
        try {
            DB.getInstance().saveVector(resultVec, vector, weight);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        log.info(sb.toString() + " " + weight);
    }

    public void handle(String rawPage) {
        String woTranslate = rawPage.replaceAll("(\\[\\[[^\\]]+\\]\\]\n)*\\[\\[[^\\]]+\\]\\]$", "");
        String plainStr = wikiModel.render(new PlainTextConverter(), woTranslate);
        String woBrackets = plainStr.replaceAll("\\{\\{[^\\}]*\\}\\}", "");
        final String[] paragraphs = woBrackets.split("\\n+");
        boolean isFirstParagraph = true;
        boolean isFirstSentence = true;
        for (String text : paragraphs) {
            if (text.isEmpty())
                continue;
            
            String sentences[] = sentenceDetector.sentDetect(text);
            for (String sentence : sentences) {
                overall++;
                handleSentence(sentence, (isFirstSentence ? 1 : 0) + (isFirstParagraph ? 1 : 0) );
                isFirstSentence = false;
            }
            isFirstParagraph = false;            
        }
        log.info("" + good + "/" + overall + " = " + ((float) good / overall));
    }

    public static void main(String argv[]) {
        StanfordPageHandler sph = new StanfordPageHandler();
        sph.handleSentence("John gave Mary a book", 0);
        sph.handleSentence("Mary moved from NY to LA", 1);
        sph.handleSentence("My dog likes eating sausage", 2);
    }
}
