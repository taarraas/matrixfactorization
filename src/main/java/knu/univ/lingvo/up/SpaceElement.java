/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.up;

/**
 *
 * @author taras
 */
public class SpaceElement extends BaseSpaceElement{
    enum Type {
        A,
        B,
        AB,
        Unknown
    };
    private BaseSpaceElement left;
    private BaseSpaceElement right;
    private Type type;

    public SpaceElement(BaseSpaceElement left, BaseSpaceElement right, Type type, String tag, String word) {
        super(tag, word);
        if (left == null || right == null) {
            throw new RuntimeException();
        }
        this.left = left;
        this.right = right;
        this.type = type;
        assert(left.getClass().getName() == "String" || left.getClass().getName() == "SpaceElement");
        assert(right.getClass().getName() == "String" || right.getClass().getName() == "SpaceElement");        
        System.out.println(toString());
    }
    
    public String toString() {
        return type.toString() + ":" + getWord() + ":" + getTag() + "( " + left.toString() + ", " + right.toString() + " )";
    }

    public BaseSpaceElement getLeft() {
        return left;
    }

    public BaseSpaceElement getRight() {
        return right;
    }

    public Type getType() {
        return type;
    }
}
