package knu.univ.lingvo.coref.sievepasses;

public class DiscourseMatch extends DeterministicCorefSieve {
  public DiscourseMatch() {
    super();
    flags.USE_DISCOURSEMATCH = true;
  }
}
