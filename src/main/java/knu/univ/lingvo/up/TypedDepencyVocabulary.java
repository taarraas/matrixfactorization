/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.up;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author tvozniuk
 */
public class TypedDepencyVocabulary {
    public HashMap<String, HashSet<Map.Entry<String, String> > > pairsByType 
            = new HashMap<String, HashSet<Map.Entry<String, String>>>();
    
    public static TypedDepencyVocabulary load(String directory)
    {
        try
        {
        File folder = new File(directory);
        File[] dictionaries = folder.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return !pathname.isDirectory() && pathname.getName().endsWith(".dic");
            }
        });

        TypedDepencyVocabulary loaded = new TypedDepencyVocabulary();
        for (File file : dictionaries) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            
            
            HashSet<Map.Entry<String, String> > pairs = new HashSet<Map.Entry<String, String>>();
            String str;
            while ((str = reader.readLine()) != null)
            {
                String[] parts = str.split("\\;\\&\\;");
                if (parts.length == 2)
                {
                    pairs.add(new AbstractMap.SimpleEntry<String, String>(parts[0], parts[1]));
                }
            }
            String type = file.getName().substring(0, file.getName().length() - ".dic".length());
            loaded.pairsByType.put(type, pairs);
        }
        return loaded;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
}
