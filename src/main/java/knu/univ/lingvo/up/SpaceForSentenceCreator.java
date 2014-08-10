/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.up;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author taras
 */
public class SpaceForSentenceCreator {

    Logger log = Logger.getLogger("SpaceForSentenceCreator");
    String parserFileOrUrl = "englishPCFG.ser.gz";
    LexicalizedParser lp = LexicalizedParser.getParserFromSerializedFile(parserFileOrUrl);
    Tree root;
    GrammaticalStructure gs;
    DepencyToSpaceType dtst = DepencyToSpaceType.getInstance();

    private DepencyToSpaceType.Type getType(Tree left, Tree right) {
        for (Tree lL : left.getLeaves()) {
            Word cmL = (Word) lL.label();
            Integer iL = cmL.beginPosition();
            for (Tree rL : right.getLeaves()) {
                Word cmR = (Word) rL.label();
                Integer iR = cmR.beginPosition();

                Collection<TypedDependency> typedDeps = gs.typedDependenciesCollapsed();

                for (TypedDependency typedDependency : typedDeps) {
                    int i1 = typedDependency.gov().label().beginPosition();
                    int i2 = typedDependency.dep().label().beginPosition();
                    int i1e = typedDependency.gov().label().beginPosition();
                    int i2e = typedDependency.dep().label().beginPosition();
                    
                    if (i1 == iL && i2 == iR) {
                        DepencyToSpaceType.Type type = dtst.getTypeFor(typedDependency.reln().getShortName());
                        type.setFirstMain(true);
                        return type;
                    }
                    if (i2 == iL && i1 == iR) {
                        DepencyToSpaceType.Type type = dtst.getTypeFor(typedDependency.reln().getShortName());
                        type.setDirectOrder(!type.isDirectOrder());
                        type.setFirstMain(false);
                        return type;
                    }
                }
            }
        }
        return null;
    }
    static Set<String> PREPOSITIONS = new HashSet<String>();

    static {
        PREPOSITIONS.add("IN");
        PREPOSITIONS.add("OF");
        PREPOSITIONS.add("ON");
        PREPOSITIONS.add("AT");
        PREPOSITIONS.add("AFTER");
    }

    private SpaceElement combinePair(BaseSpaceElement s1, BaseSpaceElement s2, Tree t1, Tree t2, String rootTag) {
        DepencyToSpaceType.Type type = getType(t1, t2);

        String w1 = s1.getWord();
        String w2 = s2.getWord();
        String tag1 = t1.nodeString().split("\\ ")[0];
        String tag2 = t2.nodeString().split("\\ ")[0];        

        String word;
        String tag;
        tag = rootTag;

        if (PREPOSITIONS.contains(tag1)) {
            type = new DepencyToSpaceType.Type(SpaceElement.Type.AB, true);
            word = w1 + "_" + w2;
        } else if (PREPOSITIONS.contains(tag1)) {
            type = new DepencyToSpaceType.Type(SpaceElement.Type.AB, false);
            word = w2 + "_" + w2;
        } else {
            if (type == null) {
                return null;
            }
            if (type.isFirstMain()) {
                word = w1;
            } else {
                word = w2;
            }
        }
        
        if (type.isShouldMerge()) {
            if (type.firstMain) {
                word = w1 + "_" + w2;
            } else {
                word = w2 + "_" + w1;
            }
        }

        if (type.isDirectOrder()) {
            return new SpaceElement(s1, s2, type.getType(), tag, word);
        } else {
            return new SpaceElement(s2, s1, type.getType(), tag, word);
        }
    }

    private SpaceElement combineOneLevel(BaseSpaceElement[] si, Tree[] ti, String rootTag) {
        if (si.length != ti.length) {
            throw new RuntimeException("Fail");
        }
        if (si.length == 1) {
            return (SpaceElement) si[0];
        }

        ArrayList<ArrayList<Tree>> t = new ArrayList();
        ArrayList<BaseSpaceElement> s = new ArrayList<BaseSpaceElement>();
        for (int i = 0; i < ti.length; i++) {
            Tree tree = ti[i];
            ArrayList<Tree> treelist = new ArrayList<Tree>();
            treelist.add(tree);
            t.add(treelist);
            s.add(si[i]);
        }


        while (s.size() > 1) {
            boolean isChanged = false;
            for (int i = s.size() - 2; i >= 0; i--) {
                ArrayList<Tree> t1 = t.get(i);
                ArrayList<Tree> t2 = t.get(i + 1);
                SpaceElement ss = null;
                for (Tree tree : t1) {
                    for (Tree tree1 : t2) {
                        ss = combinePair(s.get(i), s.get(i + 1), tree, tree1, rootTag);
                        if (ss != null) {
                            break;
                        }
                    }
                    if (ss != null) {
                        break;
                    }
                }

                if (ss != null) {
                    isChanged = true;
                    t.remove(i + 1);
                    s.remove(i + 1);
                    t1.addAll(t2);
                    s.set(i, ss);
                    break;
                }
            }
            if (!isChanged) {
                break;
            }

        }

        while (s.size() > 1) {
            int i1 = s.size() - 2;
            int i2 = s.size() - 1;
            String word = s.get(i2).getWord();
            String tag = rootTag;
            tag = rootTag;

            s.set(i1,
                    new SpaceElement(s.get(i1), s.get(i2), SpaceElement.Type.AB, tag, word));
            s.remove(i2);
            t.remove(i2);
        }
        return (SpaceElement) s.get(0);
    }
    public static Set<String> punctuation = new HashSet<String>();

    static {
        punctuation.add("(. .)");
        punctuation.add("(, ,)");
        punctuation.add("(; ;)");
    }

    ;

    private BaseSpaceElement buildRecursive(Tree p) {
        if (p.isLeaf()) {
            return new BaseSpaceElement(p.nodeString(), p.toString());
        }

        Tree[] children = p.children();
        ArrayList<Tree> noPunctuation = new ArrayList<Tree>();
        for (int i = 0; i < children.length; i++) {
            Tree tree = children[i];
            if (!punctuation.contains(tree.toString())) {
                noPunctuation.add(tree);
            }
        }

        if (noPunctuation.isEmpty()) {
            return null;
        }

        if (noPunctuation.size() == 1) {
            BaseSpaceElement one = buildRecursive(noPunctuation.get(0));
            one.setTag(p.nodeString());
            return one;
        }

        BaseSpaceElement elems[] = new BaseSpaceElement[noPunctuation.size()];
        for (int i = 0; i < noPunctuation.size(); i++) {
            Tree tree = noPunctuation.get(i);
            elems[i] = buildRecursive(tree);
            if (elems[i] == null) {
                throw new RuntimeException("Fail");
            }
        }

        SpaceElement s = combineOneLevel(elems, noPunctuation.toArray(new Tree[0]), p.nodeString());
        //s.setTag(p.pennString());
        return s;
    }

    public SpaceElement getSpace(String sentence) {
        StringReader stringReader = new StringReader(sentence);
        PTBTokenizer tokenizer = PTBTokenizer.newPTBTokenizer(stringReader);
        List toks = tokenizer.tokenize();
        Tree parse = (Tree) lp.apply(toks); // finally, we actually get to parse something
        parse.indexLeaves();
        root = parse;
        try {
            TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            gs = gsf.newGrammaticalStructure(parse);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

        BaseSpaceElement space = buildRecursive(parse);
        if (space instanceof SpaceElement)
            return (SpaceElement) buildRecursive(parse);
        else 
            return null;
    }

    public static void main(String argv[]) {
        SpaceForSentenceCreator sfsc = new SpaceForSentenceCreator();
        SpaceElement space = sfsc.getSpace("The strongest rain ever recorded in India shut down the financial hub of Mumbai, snapped communication lines, closed airports and forced thousands of people to sleep in their offices.");
        System.out.println(space.toString());
    }
}
