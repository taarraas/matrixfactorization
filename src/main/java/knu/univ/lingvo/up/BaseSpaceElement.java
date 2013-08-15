/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.up;

/**
 *
 * @author taras
 */
public class BaseSpaceElement {
    private String tag;
    private String word;

    public BaseSpaceElement(String tag, String word) {
        setTag(tag);
        this.word = word;
    }    

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag.split("\\ ")[0];;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return getWord() + ":" + getTag();
    }
    
    
}
