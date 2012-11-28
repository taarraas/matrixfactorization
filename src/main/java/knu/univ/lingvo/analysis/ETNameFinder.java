/**
 * @fileOverview ETNameFinder.java
 * @author Kostiantyn Lyman
 * @version 1.0
 * @date March 19, 2012
 * @modified March 25, 2012
 * @modifiedby Kostiantyn Lyman
 * @param Created in Taras Shevchenko National University of Kyiv (Cybernetics) under a contract between
 * @param LLC "Samsung Electronics Ukraine Company" (Kiev Ukraine) and
 * @param Taras Shevchenko National University of Kyiv
 * @param Copyright: Samsung Electronics, Ltd. All rights reserved.
 */
/*
 * Named Entity Recognition Model wrapper
 *
 */

package knu.univ.lingvo.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.Span;

/**
 * @fileOverview ETNameFinder.java
 * @author Kostiantyn Lyman
 * @version 1.0
 * @date March 19, 2012
 * @modified March 25, 2012
 * @modifiedby Kostiantyn Lyman
 * @param Created in Taras Shevchenko National University of Kyiv (Cybernetics) under a contract between
 * @param LLC "Samsung Electronics Ukraine Company" (Kiev Ukraine) and
 * @param Taras Shevchenko National University of Kyiv
 * @param Copyright: Samsung Electronics, Ltd. All rights reserved.
 */

public class ETNameFinder
{
    public static final String EN_NER_ORGANIZATION = "en-ner-organization.bin";
    public static final String EN_NER_PERSON = "en-ner-person.bin";
    public static final String EN_NER_LOCATION = "en-ner-location.bin";
    public static final String EN_NER_DATE = "en-ner-date.bin";
    
    private static String modelHome = "data/models/";
    
    private NameFinderME[] nameFinders;
    
    public ETNameFinder()
    {
      String[] models = {getModelPath(EN_NER_ORGANIZATION), getModelPath(EN_NER_PERSON),
                           getModelPath(EN_NER_LOCATION), getModelPath(EN_NER_DATE)};
      
      nameFinders = new NameFinderME[models.length];
      for (int i = 0; i < nameFinders.length; i++) {
        TokenNameFinderModel model = new ETNameFinderModelLoader().load(new File(models[i]));
        nameFinders[i] = new NameFinderME(model);
      } 
    }
    
    public ETNameFinder(String[] models){
      nameFinders = new NameFinderME[models.length];

      for (int i = 0; i < nameFinders.length; i++) {
        TokenNameFinderModel model = new ETNameFinderModelLoader().load(new File(models[i]));
        nameFinders[i] = new NameFinderME(model);
      }
    }
    
    public NameSample[] findNames(String[] sentences)
    {
        List<NameSample> res = new ArrayList<NameSample>(sentences.length);
        for(String line: sentences){
            String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE.tokenize(line);
            if (whitespaceTokenizerLine.length == 0) {
                for (NameFinderME nameFinder : nameFinders) nameFinder.clearAdaptiveData();
                res.add(null);
                continue;
          }
          List<Span> names = new ArrayList<Span>();

          for (TokenNameFinder nameFinder : nameFinders) {
              Span[] s = nameFinder.find(whitespaceTokenizerLine);
            Collections.addAll(names, s);
                    //nameFinder.find(whitespaceTokenizerLine));
          }
          // Simple way to drop intersecting spans, otherwise the
          // NameSample is invalid
          Span reducedNames[] = NameFinderME.dropOverlappingSpans(
              names.toArray(new Span[names.size()]));
              //    names.toArray(new Span[sentences.length]));
          
          res.add(new NameSample(whitespaceTokenizerLine,
              reducedNames, false));
          //NameSample nameSample = new NameSample(whitespaceTokenizerLine,reducedNames, false);
          //System.out.println(nameSample.toString());
        }
          return res.toArray(new NameSample[sentences.length]);
    }

    public static String getModelHome()
    {
        return modelHome;
    }

    public static void setModelHome(String modelHome)
    {
        ETNameFinder.modelHome = modelHome;
    }
    
    public static String getModelPath(String model)
    {
        StringBuilder sb = new StringBuilder(modelHome);
        sb.append(model); 
        return sb.toString();
    }
    
    public static void main(String[] args)
    {
        String[] models = {modelHome+EN_NER_PERSON, modelHome+EN_NER_ORGANIZATION, modelHome+EN_NER_LOCATION, modelHome+EN_NER_DATE};
        String[] sentences = {"Pierre Vinken, 61 years old, will join the board as a nonexecutive IBM director Nov 29 2011.",
        "Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group."};
        ETNameFinder nameFinder = new ETNameFinder(models);
        NameSample[] names = nameFinder.findNames(sentences);
        for(NameSample n: names)
            System.out.println(n.toString());
    }
}
