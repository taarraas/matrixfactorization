/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.up;

/**
 *
 * @author taras
 */
public class SpaceElement {
    enum Type {
        A,
        B,
        AB,
        Unknown
    };
    private Object left;
    private Object right;
    private Type type;
    private String tag;
    private String word;

    public SpaceElement(Object left, Object right, Type type, String tag, String word) {
        if (left == null || right == null) {
            throw new RuntimeException();
        }
        this.left = left;
        this.right = right;
        this.type = type;
        assert(left.getClass().getName() == "String" || left.getClass().getName() == "SpaceElement");
        assert(right.getClass().getName() == "String" || right.getClass().getName() == "SpaceElement");
        
        this.tag = tag.split("\\ ")[0];
        this.word = word;
        System.out.println(toString());
    }
    
    public String toString() {
        return type.toString() + ":" + word + ":" + tag + "( " + left.toString() + ", " + right.toString() + " )";
    }

    public String getWord() {
        return word;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Object getLeft() {
        return left;
    }

    public Object getRight() {
        return right;
    }

    public Type getType() {
        return type;
    }
}
