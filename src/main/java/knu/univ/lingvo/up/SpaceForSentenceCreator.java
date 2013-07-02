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
import java.util.Collection;
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
            CoreMap cmL = (CoreMap)lL.label();
            Integer iL = cmL.get(CoreAnnotations.IndexAnnotation.class);
            for (Tree rL : right.getLeaves()) {
                CoreMap cmR = (CoreMap)rL.label();
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
    
    private SpaceElement combineOneLevel(Object[] s, Tree[] t) {
        DepencyToSpaceType.Type type = getType(t[0], t[1]);
        if (type == null)
            throw new RuntimeException("no depency between phrases");
        if (type.isDirectOrder())
            return new SpaceElement(s[0], s[1], type.getType());
        else 
            return new SpaceElement(s[1], s[0], type.getType());
    }
    
    private Object buildRecursive(Tree p) {
        if (p.isLeaf()) {
            return p.toString();
        }
        
        Tree[] children = p.children();
        if (children.length == 1) {
            return buildRecursive(children[0]);
        }
        if (children.length == 0)
            throw new RuntimeException("has no children and isn't leaf");
        if (children.length > 2 )
            throw new RuntimeException("More than 2 children");
        
        Object elems[] = new Object[children.length];
        for (int i = 0; i < children.length; i++) {
            Tree tree = children[i];
            elems[i] = buildRecursive(tree);
        }
        
        return combineOneLevel(elems, children);
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
        
        return (SpaceElement)buildRecursive(parse);
    }
    
    public static void main(String argv[]) {
        SpaceForSentenceCreator sfsc = new SpaceForSentenceCreator();
        SpaceElement space = sfsc.getSpace("The chef cooks the soup");
        System.out.println(space.toString());
    }
}
