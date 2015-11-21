/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ultis;

import java.util.HashMap;
import java.util.HashSet;
import vn.hus.nlp.utils.UTF8FileUtility;

/**
 *
 * @author Ken
 */
public class Analyzer {

    public static HashSet<String> stopWords;

    /**
     * Load stop words set from file
     * @param path
     */
    public static void loadStopWords(String path) {
        stopWords = new HashSet<>();
        String[] lines = UTF8FileUtility.getLines(path);
        for (int i = 0; i < lines.length; ++i) {
            stopWords.add(lines[i].trim());
        }
    }

    /**
     * Analyze given string to other words and its frequency stored in a HashMap
     * @param s
     * @return
     */
    public static HashMap<String, Integer> analyze(String s) {
        HashMap<String, Integer> result = new HashMap<>();
        
        String[] words = s.split("[,\\.\\?\\;\\!\\:\\(\\)\\[\\]\\{\\}\\p{Space}/]+");
        for (int i = 0; i < words.length; ++i) {
            if (!stopWords.contains(words[i])) {
                if (result.containsKey(words[i])) {
                    result.put(words[i], result.get(words[i]) + 1);
                } else {
                    result.put(words[i], 1);
                }
            }
        }

        return result;
    }
}
