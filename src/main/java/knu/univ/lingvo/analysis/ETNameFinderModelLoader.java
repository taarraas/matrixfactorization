/**
 * @fileOverview ETNameFinderModelLoader.java
 * @author Kostiantyn Lyman
 * @version 1.0
 * @date March 16, 2012
 * @modified March 16, 2012
 * @modifiedby Kostiantyn Lyman
 * @param Created in Taras Shevchenko National University of Kyiv (Cybernetics) under a contract between
 * @param LLC "Samsung Electronics Ukraine Company" (Kiev Ukraine) and
 * @param Taras Shevchenko National University of Kyiv
 * @param Copyright: Samsung Electronics, Ltd. All rights reserved.
 */
/*
 * Named Entity Recognition Model loader
 *
 */

package knu.univ.lingvo.analysis;


import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.cmdline.ModelLoader;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.InvalidFormatException;

class ETNameFinderModelLoader  extends ModelLoader<TokenNameFinderModel> {
  ETNameFinderModelLoader() {
    super("Token Name Finder");
  }
  
  @Override
  protected TokenNameFinderModel loadModel(InputStream modelIn)
      throws IOException, InvalidFormatException {
    return new TokenNameFinderModel(modelIn);
  }

}    
