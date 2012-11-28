/**
 * @fileOverview GroupFinder.java
 * @author Andrey Nikonenko
 * @version 0.9
 * @date Apr 17, 2012
 * @modified Apr 20, 2012
 * @modifiedby Andrey Nikonenko
 * @param Created in Taras Shevchenko National University of Kyiv (Cybernetics) under a contract between
 * @param LLC "Samsung Electronics Ukraine Company" (Kiev Ukraine) and
 * @param Taras Shevchenko National University of Kyiv
 * @param Copyright: Samsung Electronics, Ltd. All rights reserved.
 */
/*
 * Class contain set of different clases and function
 * for working with phrases at syntactic tree.
 */
package knu.univ.lingvo.analysis;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.tree.DefaultMutableTreeNode;

  class NER_Element {
    public String word;
    public String type;
    public NER_Element() {}
    public NER_Element(String word,String type) {
      this.word = word;
      this.type = type;
    }
  }

  class NER_Group {
    public String groupType;
    public String groupText;
    public ArrayList<NER_Element> Elements;
    public NER_Group(String groupType) {
        this.groupType = groupType;
        Elements = new ArrayList<NER_Element>();
    }
    public void Add(String word,String type) {
      NER_Element e = new NER_Element(word, type);
      Elements.add(e);
    }
  }

  class Element {
    public String word;
    public String POSTag;
    public Element() {}
    public Element(String word,String POSTag) {
      this.word = word;
      this.POSTag = POSTag;
    }
  }

  class Group {
    public String groupType;
    public String groupText;
    public ArrayList<Element> Elements;
    public Group(String groupType) {
        this.groupType = groupType;
        Elements = new ArrayList<Element>();
    }
    public void Add(String word,String POSTag) {
      Element e = new Element(word, POSTag);
      Elements.add(e);
    }
  }
  
public class GroupFinder {
    //private Pattern group = Pattern.compile("^(\\([A-Z]+ \\()");
    //private Pattern word = Pattern.compile("^(\\([A-Z]+ [^\\(\\)]+\\))");
    private Pattern group_15 = Pattern.compile("\\(([^\\(\\)]+) (\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?(\\(([^\\(\\)]+) ([^\\(\\)]+)\\) ?)?\\)");
    //\(([^\(\)]+) (\(([^\(\)]+) ([^\(\)]+)\) ?)(\(([^\(\)]+) ([^\(\)]+)\) ?)(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?(\(([^\(\)]+) ([^\(\)]+)\) ?)?\)

    public ArrayList<Group> FindGroups(String sentence){
        ArrayList<Group> groups = new ArrayList<Group>();
        if (sentence.isEmpty()) {return groups;}
        Matcher m = group_15.matcher(sentence);
        while(m.find()) {
          String group_type = m.group(1);
          Group group = new Group(group_type);
          group.groupText = m.group(0);
          String word_type = "";
          String word = "";
          for (int i=3; i<=m.groupCount(); i++){
              if (i%3 == 0) {word_type = m.group(i);}
              else if(i % 3 == 1) {
                  word = m.group(i);
                  if (word == null && word_type == null) {break;}
                  group.Add(word,word_type);
              }
          }
          groups.add(group);
        }
        return groups;
    }

    /**Syntactic tree depth-first traversal */
    private String depthFirstBuilder(String text, int currLevel, DefaultMutableTreeNode root) {
        String space = new String(new char[currLevel*4]).replace('\0', ' ');
        DefaultMutableTreeNode node = root;
        while (true) {
            if (text.isEmpty()) {return "";}
            int i = text.indexOf("(",1);
            int j = text.indexOf(")");
            int index = -1;
            if (i<0) {index = j;}
            else if (j<0) {index = i;}
            else {index = Math.min(i, j);}

            if (index < 0) {return text;}
            
            if (index == i) {
                if (!text.substring(0,index).trim().isEmpty()) {
//                if (!text.substring(0,index).trim().equals(")")) {
                    node = new DefaultMutableTreeNode(text.substring(0,index));
                    root.add(node);
//                    System.out.println(space + node.toString() +"  "+ node.getLevel());
                }
                //System.out.println("|" + space + text.substring(0,index));
                text = text.substring(index);
                text = depthFirstBuilder(text,currLevel+1, node);
            }
            else {
                if (!text.substring(0,index+1).trim().equals(")")) {
                    node = new DefaultMutableTreeNode(text.substring(0,index+1));
                    root.add(node);
//                    System.out.println(space + node.toString()+"  "+ node.getLevel());
                }
                //System.out.println("|" + space + text.substring(0,index+1));
                text = " "+text.substring(index+1);
                return text;
            }
            
        }
    }

    /**Syntactic tree depth-first traversal for word-index tree*/
    private String depthFirstIndexBuilder(String text, int currLevel, DefaultMutableTreeNode root) {
        String space = new String(new char[currLevel*4]).replace('\0', ' ');
        DefaultMutableTreeNode node = root;
        while (true) {
            if (text.isEmpty()) {return "";}
            int i = text.indexOf("(",1);
            int j = text.indexOf(")");
            int index = -1;
            if (i<0) {index = j;}
            else if (j<0) {index = i;}
            else {index = Math.min(i, j);}

            if (index < 0) {return text;}

            if (index == i) {
                if (!text.substring(0,index).trim().isEmpty()) {
//                if (!text.substring(0,index).trim().equals(")")) {
                    node = new DefaultMutableTreeNode(text.substring(0,index));
                    root.add(node);
//                    System.out.println(space + node.toString() +"  "+ node.getLevel());
                }
                //System.out.println("|" + space + text.substring(0,index));
                text = text.substring(index);
                text = depthFirstIndexBuilder(text,currLevel+1, node);
            }
            else {
                node = new DefaultMutableTreeNode(text.substring(0,index+1));
                root.add(node);
//                    System.out.println(space + node.toString()+"  "+ node.getLevel());
               
                //System.out.println("|" + space + text.substring(0,index+1));
                text = " "+text.substring(index+1);
                return text;
            }

        }
    }

    /** Build sentence tree using syntactic parser output*/
    public DefaultMutableTreeNode getTree(String sentence) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        depthFirstBuilder(sentence, 0, root);
        if (root != null) {return root.getNextNode();}
        else {return root;}
    }

    /** Build sentence tree using syntactic parser output with all nodes (including ")"-node)*/
    public DefaultMutableTreeNode getFullTree(String sentence) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        depthFirstIndexBuilder(sentence, 0, root);
        if (root != null) {return root.getNextNode();}
        else {return root;}
    }

    /** Print sentence tree*/
    public void printTree(DefaultMutableTreeNode tree) {
        if (tree == null) {return;}
        Enumeration en = tree.preorderEnumeration();
        while (en.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
          String space = new String(new char[node.getLevel()*4]).replace('\0', ' ');
          System.out.println(space + node.toString());
        }
    }

    /** Print syntactic tree structure in user-friendly format */
    private void showOutput(DefaultMutableTreeNode tree) {
        if (tree == null) {return;}
        Enumeration en = tree.preorderEnumeration();
        int i=0;
        String word = "";
        String tagSet = "";
        while (en.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
           tagSet = ""; word = "";
          if (node.isLeaf()) {
              //Print word number
              i++;
              System.out.print(i+"   ");

              Object[] pathToRoot = node.getUserObjectPath();
              //Print word
              if (node.toString().endsWith(")")){
                  String[] words = node.toString().split(" |\\)");
                  word = words[1];
                  System.out.print(word +"   ");
              }

              //Print path to word (set of syntactic tags)
              for (Object pathElement:pathToRoot){
                if (pathElement!=null) {
                    // If current element word then print only first half of tag
                    if (pathElement.toString().endsWith(")")){
                        String[] element = pathElement.toString().split(" ");
                        tagSet += element[0];
                        System.out.print(element[0]);
                    } else {
                        tagSet += pathElement.toString();
                        System.out.print(pathElement.toString());
                    }
                }
              }
              System.out.println();
          }
        }
    }

    //Test tree builder
    public static void main(String[] args) {
        GroupFinder g = new GroupFinder();
//        DefaultMutableTreeNode tree = g.getTree("(TOP (S (NP (NNP John) (NNP White) (NNP Smith)) (VP (VBD has) (VBD took) (NP (PRP$ his) (NN mother)) (PP (TO to) (NP (DT the) (NN theater))))(. .)))");
//        g.printTree(tree);
//        tree = g.getTree("(TOP (S (NP (NNP Russian) (NNP Foreign) (NNP Minister) (NNP Sergei) (NNP Lavrov)) (VP (VBZ has) (VP (VBD said) (SBAR (S (NP (NP (NNP Syria)(POS 's)) (NN opposition)) (VP (MD will) (ADVP (RB never)) (VP (VB defeat) (NP (NP (DT the) (NN country)(POS 's)) (JJ armed) (NNS forces)) (SBAR (RB even) (IN if) (S (NP (PRP it)) (VP (VBZ is) (VP (VBN \"armed) (PP (TO to) (NP (DT the) (NNS teeth\")))))))))))))(. .)))");
//        g.printTree(tree);
//        DefaultMutableTreeNode tree = g.getTree("(TOP (SBAR (IN If) (S (NP (DT the) (NN weather)) (VP (VBZ is) (ADJP (JJ fine) (SBAR (S (NP (PRP she)) (VP (MD will) (VP (VB go) (PP (IN for) (NP (DT a) (NN walk))))))))))(. .)))");
        DefaultMutableTreeNode tree = g.getTree("(TOP (S (PP (IN After) (NP (JJ such) (DT a) (JJ long) (JJ hot) (NN summer) (, ,) (PP (IN with) (NP (NNS rivers) (VP (VBG running) (JJ dry)))))) (, ,) (NP (PRP it)) (VP (VBZ 's) (ADJP (JJ difficult)) (VP (TO to) (VP (VB imagine) (SBAR (WHNP (WP what)) (S (NP (DT a) (JJ good) (NN flood)) (VP (VBZ looks) (ADVP (IN like)))))))) (. .)))");
        g.printTree(tree);
        g.showOutput(tree);

    }

}
