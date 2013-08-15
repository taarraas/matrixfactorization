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
public class ThreeWordsExtractor {

    public static ArrayList<String[]> getThreeWordsB(SpaceElement se, SpaceElement ser) {
        ArrayList<String[]> twoWordsList = new ArrayList();
        if (ser.getType() == SpaceElement.Type.B) {
            String[] thisLevel = new String[6];
            thisLevel[0] = se.getLeft().getWord();
            thisLevel[1] = se.getLeft().getTag();
            thisLevel[2] = ser.getLeft().getWord();
            thisLevel[3] = ser.getLeft().getTag();
            thisLevel[4] = ser.getRight().getWord();
            thisLevel[5] = ser.getRight().getTag();
            twoWordsList.add(thisLevel);
        }
        if (ser.getLeft() instanceof SpaceElement) {
            twoWordsList.addAll(getThreeWordsB(se, (SpaceElement) ser.getLeft()));
        }

        if (ser.getRight() instanceof SpaceElement) {
            twoWordsList.addAll(getThreeWordsB(se, (SpaceElement) ser.getRight()));
        }
        return twoWordsList;
    }

    public static ArrayList<String[]> getThreeWords(SpaceElement se) {
        ArrayList<String[]> twoWordsList = new ArrayList();
        if (se.getType() == SpaceElement.Type.A) {

            if (se.getRight() instanceof SpaceElement) {
                twoWordsList.addAll(getThreeWordsB(se, (SpaceElement) se.getRight()));
            }
        }

        if (se.getLeft() instanceof SpaceElement) {
            twoWordsList.addAll(getThreeWords((SpaceElement) se.getLeft()));
        }

        if (se.getRight() instanceof SpaceElement) {
            twoWordsList.addAll(getThreeWords((SpaceElement) se.getRight()));
        }
        return twoWordsList;
    }

    public static void main(String argv[]) {
        SpaceForSentenceCreator sfsc = new SpaceForSentenceCreator();
        SpaceElement space = sfsc.getSpace("The strongest rain ever recorded in India shut down the financial hub of Mumbai, snapped communication lines, closed airports and forced thousands of people to sleep in their offices.");
        ArrayList<String[]> two = getThreeWords(space);
        for (String[] strings : two) {
            for (String string : strings) {
                System.out.print(string + " ");
            }
            System.out.println("");
        }
    }
}
