package knu.univ.lingvo.matrixfactorization;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.NumberRangeFileFilter;
import edu.stanford.nlp.io.NumberRangesFileFilter;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.ViterbiParser;
import edu.stanford.nlp.parser.KBestViterbiParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.process.WhitespaceTokenizer;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.arabic.ArabicTreebankLanguagePack;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Timing;
import edu.stanford.nlp.util.ScoredObject;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.util.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.PTBTokenizer;

public class LexParser {

    /**
     *
     * @param args Arg1 - full path of the stanford parser input file
     * (englishPCFG.ser.gz), Arg2 - file to parse
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // input format: data directory, and output directory
        String strLine = "I didn't want to do this work. I go to school.";


        String parserFileOrUrl = "englishPCFG.ser.gz";
        LexicalizedParser lp = LexicalizedParser.getParserFromSerializedFile(parserFileOrUrl);
        WordStemmer ls = new WordStemmer(); // stemmer/lemmatizer object


        StringReader sr = new StringReader(strLine);
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
        Collection tdl = gs.typedDependenciesCollapsed();

        // And print!
        System.out.println("words: " + words);
        System.out.println("POStags: " + tags);
        System.out.println("stemmedWordsAndTags: " + stems);
        System.out.println("typedDependencies: " + tdl);
        

        // Output Option 2: Printing out various data using TreePrint

        // Various TreePrint options
        //	    "penn", // constituency parse
        //	    "oneline",
        //	    rootLabelOnlyFormat,
        //	    "words",
        //	    "wordsAndTags", // unstemmed words and pos tags
        //	    "dependencies", // unlabeled dependency parse
        //	    "typedDependencies", // dependency parse
        //	    "typedDependenciesCollapsed",
        //	    "latexTree",
        //	    "collocations",
        //	    "semanticGraph"

        // Print using TreePrint with various options
        //TreePrint tp = new TreePrint("wordsAndTags,typedDependencies");
        //tp.printTree(parse);

        System.out.println(); // separate output lines


    }
}