/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javafx.util.Pair;
import ultis.Analyzer;
import ultis.Config;
import ultis.ReadXlsxInput;
import vn.hus.nlp.tokenizer.VietTokenizer;
import vn.hus.nlp.utils.UTF8FileUtility;

/**
 *
 * @author Ken
 */
public class Classifier {

    private VietTokenizer tokenizer;
    // Stores map emotions with their indexes
    HashMap<String, Integer> map;
    // Stores processed input data
    private ArrayList<HashMap<String, Integer>> data;
    // Maps other words with their order
    private HashMap<String, Integer> bagOfWord;

    public Classifier() {
        Analyzer.loadStopWords(Config.STOP_WORDS_FILE);
        tokenizer = new VietTokenizer();
        map = new HashMap<>();
        map();
        data = new ArrayList<>();
        bagOfWord = new HashMap<>();
        loadBagOfWords();
    }

    public Classifier(VietTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        Analyzer.loadStopWords(Config.STOP_WORDS_FILE);
        map = new HashMap<>();
        map();
        data = new ArrayList<>();
        bagOfWord = new HashMap<>();
        loadBagOfWords();
    }

    /**
     * Maps emotions with their indexes
     */
    private void map() {
        for (int i = 0; i < Config.EMOTIONS.length; ++i) {
            map.put(Config.EMOTIONS[i], i + 1);
        }
    }

    private void loadBagOfWords() {
        String[] lines = UTF8FileUtility.getLines(Config.BAG_OF_WORDS_FILE);
        for (int i = 0; i < lines.length; ++i) {
            bagOfWord.put(lines[i], i + 1);
        }
    }

    /**
     * Gets extension of given file
     *
     * @param file
     * @return String
     */
    public String getExtension(File file) {
        if (file.isDirectory()) {
            return "";
        }
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Processes data for String[]
     *
     * @param lines
     */
    public void process(String[] lines) {
        for (int i = 0; i < lines.length; ++i) {

        }
    }

    /**
     * Processes data for ArrayList<String>
     *
     * @param lines
     */
    public void process(ArrayList<String> lines) {
        for (String line : lines) {

        }
    }

    /**
     * Classifies a sentence
     *
     * @param sentence
     * @throws IOException
     */
    public String classify(String sentence) throws IOException {
        sentence = tokenizer.tokenize(sentence.toLowerCase())[0];
        HashMap<String, Integer> m = Analyzer.analyze(sentence);
        ArrayList<Pair<Integer, Double>> features = new ArrayList<>();
        for (String key : m.keySet()) {
//            System.out.println(key);
            int order = bagOfWord.get(key);
            double w = 0;
            features.add(new Pair<Integer, Double>(order, w));
        }
        // Sort features 
        Comparator<Pair<Integer, Double>> comparator = new Comparator<Pair<Integer, Double>>() {

            @Override
            public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
                if (o1.getKey() < o2.getKey()) {
                    return -1;
                } else if (o1.getKey() > o2.getKey()) {
                    return 1;
                }
                return 0;
            }
        };
        Collections.sort(features, comparator);
        // Write features
        UTF8FileUtility.createWriter(Config.SVM_CLASSIFYING_DATA_FILE);
        UTF8FileUtility.write("0 ");
        for (Pair feature : features) {
            UTF8FileUtility.write(feature.getKey() + ":" + feature.getValue() + " ");
        }
        UTF8FileUtility.closeWriter();

        // Call svm_multiclass_classify.exe
        Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "classify.bat"});
        
        return "";
    }
}
