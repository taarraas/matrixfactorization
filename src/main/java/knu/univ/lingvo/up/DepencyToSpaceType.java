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
        return new Type(s.getType(), s.isDirectOrder());
    }
}
