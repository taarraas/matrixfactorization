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

    public SpaceElement(Object left, Object right, Type type) {
        if (left == null || right == null) {
            throw new RuntimeException();
        }
        this.left = left;
        this.right = right;
        this.type = type;
        assert(left.getClass().getName() == "String" || left.getClass().getName() == "SpaceElement");
        assert(right.getClass().getName() == "String" || right.getClass().getName() == "SpaceElement");
    }
    
    public String toString() {
        return type.toString() + "( " + left.toString() + ", " + right.toString() + " )";
    }
}
