/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.up;

import edu.stanford.nlp.ling.CoreAnnotations;
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
import knu.univ.lingvo.analysis.Main;
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
            CoreMap cmL = (CoreMap) lL.label();
            Integer iL = cmL.get(CoreAnnotations.IndexAnnotation.class);
            for (Tree rL : right.getLeaves()) {
                CoreMap cmR = (CoreMap) rL.label();
                Integer iR = cmR.get(CoreAnnotations.IndexAnnotation.class);

                Collection<TypedDependency> typedDeps = gs.typedDependenciesCollapsed();

                for (TypedDependency typedDependency : typedDeps) {
                    int i1 = typedDependency.gov().label().get(CoreAnnotations.IndexAnnotation.class);
                    int i2 = typedDependency.dep().label().get(CoreAnnotations.IndexAnnotation.class);

                    if (i1 == iL && i2 == iR) {
                        DepencyToSpaceType.Type type = dtst.getTypeFor(typedDependency.reln().getShortName());
                        return type;
                    }
                    if (i2 == iL && i1 == iR) {
                        DepencyToSpaceType.Type type = dtst.getTypeFor(typedDependency.reln().getShortName());
                        type.setDirectOrder(!type.isDirectOrder());
                        return type;
                    }
                }
            }
        }
        return null;
    }

    private SpaceElement combinePair(Object s1, Object s2, Tree t1, Tree t2) {
        DepencyToSpaceType.Type type = getType(t1, t2);
        if (type == null) {
            return null;
        }

        if (type.isDirectOrder()) {
            return new SpaceElement(s1, s2, type.getType());
        } else {
            return new SpaceElement(s2, s1, type.getType());
        }
    }

    private SpaceElement combineOneLevel(Object[] si, Tree[] ti) {
        if (si.length != ti.length) {
            throw new RuntimeException("Fail");
        }
        if (si.length == 1) {
            return (SpaceElement) si[0];
        }
        
        ArrayList<ArrayList<Tree> > t = new ArrayList();
        ArrayList s = new ArrayList();
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
                ArrayList<Tree> t2 = t.get(i+1);
                SpaceElement ss = null;
                for (Tree tree : t1) {
                    for (Tree tree1 : t2) {
                        ss = combinePair(s.get(i), s.get(i+1), tree, tree1);
                        if (ss != null)
                            break;
                    }
                    if (ss != null)
                        break;
                }
                
                if (ss != null) {
                    isChanged = true;
                    t.remove(i+1);
                    s.remove(i+1);
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
            s.set(i1, new SpaceElement(s.get(i1), s.get(i2), SpaceElement.Type.Unknown));
            s.remove(i2);
        }
        return (SpaceElement)s.get(0);
    }
    
    public static Set<String> punctuation = new HashSet<String>();
    static {
        punctuation.add("(. .)");
        punctuation.add("(, ,)");
        punctuation.add("(; ;)");
    };

    private Object buildRecursive(Tree p) {
        if (p.isLeaf()) {
            return p.toString();
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
            return buildRecursive(noPunctuation.get(0));
        }

        Object elems[] = new Object[noPunctuation.size()];
        for (int i = 0; i < noPunctuation.size(); i++) {
            Tree tree = noPunctuation.get(i);
            elems[i] = buildRecursive(tree);
            if (elems[i] == null) {
                throw new RuntimeException("Fail");
            }
        }

        SpaceElement s = combineOneLevel(elems, noPunctuation.toArray(new Tree[0]));        
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

        return (SpaceElement) buildRecursive(parse);
    }

    public static void main(String argv[]) {
        SpaceForSentenceCreator sfsc = new SpaceForSentenceCreator();
        SpaceElement space = sfsc.getSpace("The strongest rain ever recorded in India shut down the financial hub of Mumbai, snapped communication lines, closed airports and forced thousands of people to sleep in their offices or walk home during the night, officials said today.");
        System.out.println(space.toString());
    }
}
