/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.up;

import java.util.ArrayList;
import knu.univ.lingvo.wikiner.NER;

/**
 *
 * @author taras
 */
public class TwoWordsExtractor {

    public static ArrayList<String[]> getTwoWords(SpaceElement se) {
        ArrayList<String[]> twoWordsList = new ArrayList();


        if (se.getType() == SpaceElement.Type.AB) {
            String[] thisLevel = new String[6];
            if (se.getLeft() instanceof SpaceElement) {
                thisLevel[0] = ((SpaceElement) se.getLeft()).getWord();
                thisLevel[1] = ((SpaceElement) se.getLeft()).getTag();
            } else {
                thisLevel[0] = (String) se.getLeft();
                thisLevel[1] = "unk";
            }
            if (se.getRight() instanceof SpaceElement) {
                thisLevel[2] = ((SpaceElement) se.getRight()).getWord();
                thisLevel[3] = ((SpaceElement) se.getRight()).getTag();
            } else {
                thisLevel[2] = (String) se.getRight();
                thisLevel[3] = "unk";
            }

            thisLevel[4] = se.getWord();
            thisLevel[5] = se.getTag();

            twoWordsList.add(thisLevel);
        }

        if (se.getLeft() instanceof SpaceElement) {
            twoWordsList.addAll(getTwoWords((SpaceElement) se.getLeft()));
        }

        if (se.getRight() instanceof SpaceElement) {
            twoWordsList.addAll(getTwoWords((SpaceElement) se.getRight()));
        }

        return twoWordsList;
    }

    public static void main(String argv[]) {
        SpaceForSentenceCreator sfsc = new SpaceForSentenceCreator();
        SpaceElement space = sfsc.getSpace("The strongest rain ever recorded in India shut down the financial hub of Mumbai, snapped communication lines, closed airports and forced thousands of people to sleep in their offices.");
        ArrayList<String[]> two = getTwoWords(space);
        for (String[] strings : two) {
            for (String string : strings) {
                System.out.print(string + " ");
            }
            System.out.println("");
        }
    }
}
