/**
 * @fileOverview CorefParse.java
 * @author Taras Voznuk
 * @version 1.0
 * @date Apr 01, 2012
 * @modified Apr 17, 2012
 * @modifiedby Taras Voznuk
 * @param Created in Taras Shevchenko National University of Kyiv (Cybernetics) under a contract between
 * @param LLC "Samsung Electronics Ukraine Company" (Kiev Ukraine) and
 * @param Taras Shevchenko National University of Kyiv
 * @param Copyright: Samsung Electronics, Ltd. All rights reserved.
 */
/*
 * Coreference wrapper
 */
package knu.univ.lingvo.analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.MentionContext;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.chunking.Parser;
import opennlp.tools.util.Span;

public class CorefParse {
    private Map<Parse, Integer> parseMap;
  private List<Parse> parses;

  public CorefParse(List<Parse> parses, DiscourseEntity[] entities) {
    this.parses = parses;
    parseMap = new HashMap<Parse, Integer>();
    for (int ei=0,en=entities.length;ei<en;ei++) {
      if (entities[ei].getNumMentions() > 1) {
        for (Iterator<MentionContext> mi = entities[ei].getMentions(); mi.hasNext();) {
          MentionContext mc = mi.next();
          Parse mentionParse = ((DefaultParse) mc.getParse()).getParse();
          parseMap.put(mentionParse,ei+1);
          //System.err.println("CorefParse: "+mc.getParse().hashCode()+" -> "+ (ei+1));
        }
      }
    }
  }

  public void show() {
    for (int pi=0,pn=parses.size();pi<pn;pi++) {
      Parse p = parses.get(pi);
      show(p);
      System.out.println();
    }
  }

  private void show(Parse p) {
    int start;
    start = p.getSpan().getStart();
    if (!p.getType().equals(Parser.TOK_NODE) && parseMap.containsKey(p)) {
      System.out.print("(");
        System.out.print("#"+parseMap.get(p));
      System.out.print(" ");
    }
    Parse[] children = p.getChildren();
    for (int pi=0,pn=children.length;pi<pn;pi++) {
      Parse c = children[pi];
      Span s = c.getSpan();
      if (start < s.getStart()) {
        System.out.print(p.getText().substring(start, s.getStart()));
      }
      show(c);
      start = s.getEnd();
    }
    System.out.print(p.getText().substring(start, p.getSpan().getEnd()));
    if (!p.getType().equals(Parser.TOK_NODE) && parseMap.containsKey(p)) {
      System.out.print(")");
    }
  }
}
