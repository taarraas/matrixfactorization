package knu.univ.lingvo.coref.sievepasses;

/**
 * Use name matcher - more precise match
 *
 * @author Angel Chang
 */
public class NameMatchPrecise extends NameMatch {


  public NameMatchPrecise() {
    super();
    ignoreGender = false;
    minTokens = 2;
  }



}
