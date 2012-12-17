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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import knu.univ.lingvo.analysis.Main;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 *
 * @author taras
 */
public class StanfordPageHandler implements PageHandler {
    public static class Depency {
        public String type;
        public String w1;
        public String n1;
        public String w2;
        public String n2;

        @Override
        public String toString() {
            return type+":"+w1+":"+n1+":"+w2+":"+n2;
        }
        
    }

    String parserFileOrUrl = "englishPCFG.ser.gz";
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

    public void handle(String rawPage) {
        String plainStr = wikiModel.render(new PlainTextConverter(), rawPage);
        String woBrackets = plainStr.replaceAll("\\{\\{[^\\}]*\\}\\}", "");
        final String[] paragraphs = woBrackets.split("\\n");
        for (String text : paragraphs) {
            if (text.length() < MINIMALPARAGRAPH) {
                continue;
            }
            String sentences[] = sentenceDetector.sentDetect(text);
            for (String sentence : sentences) {
                if (sentence.length() < 15) {
                    continue;
                }
                StringReader sr = new StringReader(sentence);
                PTBTokenizer tkzr = PTBTokenizer.newPTBTokenizer(sr);
                List toks = tkzr.tokenize();
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
                    cur.w1 = str.substring(a+1, b);
                    int c = str.indexOf(',', b);
                    cur.n1 = str.substring(b+1, c);
                    int d = c + 1;
                    int e = str.indexOf('-', d);
                    cur.w2 = str.substring(d+1, e);
                    int f = str.indexOf(')', e);
                    cur.n2 = str.substring(e+1, f);
                    //System.out.println(str);
                    //System.out.println(cur.toString());
                    deps.add(cur);
                }
                String wordRoot = null;
                String noRootS = null;
                for (Depency depency : deps) {
                    if (depency.type.equals("root"))
                    {
                        wordRoot = depency.w2;
                        noRootS = depency.n2;
                    }
                }
                
                if (wordRoot == null)
                    continue;
                
                int noRoot = -1;
                try {
                    noRoot = Integer.valueOf(noRootS);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (noRoot == -1)
                    continue;
                                                
                if (!tags.get(noRoot-1).startsWith("V"))
                    continue;
                
                System.out.println(">> " + sentence);
                System.out.println("root : " + wordRoot + " #" + noRoot);
                
                //System.out.println("words: "+words); 
		//System.out.println("POStags: "+tags); 
                System.out.println("all deps : " + tdl);
                
            }
        }
    }
}
