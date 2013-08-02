/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.up;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author taras
 */
public class DepencyToSpaceType {
    public static class Type {
        SpaceElement.Type type;
        boolean directOrder;
        boolean firstMain;
        boolean shouldMerge;

        public boolean isShouldMerge() {
            return shouldMerge;
        }

        public void setShouldMerge(boolean shouldMerge) {
            this.shouldMerge = shouldMerge;
        }

        public boolean isFirstMain() {
            return firstMain;
        }

        public void setFirstMain(boolean firstMain) {
            this.firstMain = firstMain;
        }

        public boolean isDirectOrder() {
            return directOrder;
        }

        public void setDirectOrder(boolean directOrder) {
            this.directOrder = directOrder;
        }

        public void setType(SpaceElement.Type type) {
            this.type = type;
        }

        public Type(SpaceElement.Type type, boolean directOrder) {
            this.type = type;
            this.directOrder = directOrder;
            this.firstMain = true;
            this.shouldMerge = false;
        }


        public SpaceElement.Type getType() {
            return type;
        }
        
    }
    public static DepencyToSpaceType instance;
    public static DepencyToSpaceType getInstance() {
        if (instance == null)
            instance = new DepencyToSpaceType("data/spaceByDeps.map");
        return instance;
    }
    
    private DepencyToSpaceType(String filename) {
        try {
            BufferedReader fr = new BufferedReader(new FileReader(filename));
            String str;
            while ((str = fr.readLine()) != null) {
                String subs[] = str.split("\\ ");
                assert(subs.length == 2);
                boolean isDirect = !subs[1].endsWith("*");
                SpaceElement.Type t = SpaceElement.Type.Unknown;
                if (subs[1].startsWith("ab")) {
                    t = SpaceElement.Type.AB;
                } else if (subs[1].startsWith("a")) {
                    t = SpaceElement.Type.A;
                } else if (subs[1].startsWith("b")) {
                    t = SpaceElement.Type.B;
                } 
                typeMap.put(subs[0], new Type(t, isDirect));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
    }
    
    private Map<String, Type> typeMap = new HashMap<String, Type>();
    
    public Type getTypeFor(String stanfordType) {
        Type s = typeMap.get(stanfordType);
        Type copied = new Type(s.getType(), s.isDirectOrder());
        if (stanfordType.equalsIgnoreCase("prt"))
            copied.setShouldMerge(true);
        return copied;
    }
}
