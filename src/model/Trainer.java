/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javafx.util.Pair;
import ultis.Analyzer;
import ultis.Config;
import ultis.ReadFile;
import vn.hus.nlp.tokenizer.VietTokenizer;
import vn.hus.nlp.utils.UTF8FileUtility;

/**
 *
 * @author Ken
 */
public class Trainer {

    private VietTokenizer tokenizer;
    // Stores map emotions with their indexes
    HashMap<String, Integer> map;
    // Stores processed training data
    private ArrayList<Item> data;
    // Stores individual words in training data with their order of it in bag of word
    private HashMap<String, Integer> bagOfWord;

    public Trainer() {
        Analyzer.loadStopWords(Config.STOP_WORDS_FILE);
        tokenizer = new VietTokenizer();
        map = new HashMap<>();
        map();
        data = new ArrayList<>();
        bagOfWord = new HashMap<>();
    }

    public Trainer(VietTokenizer tokenizer) {
        Analyzer.loadStopWords(Config.STOP_WORDS_FILE);
        this.tokenizer = tokenizer;
        map = new HashMap<>();
        map();
        data = new ArrayList<>();
        bagOfWord = new HashMap<>();
    }

    /**
     * Maps emotions with their indexes
     */
    private void map() {
        for (int i = 0; i < Config.EMOTIONS.length; ++i) {
            map.put(Config.EMOTIONS[i], i + 1);
        }
    }

    /**
     * Trains data from given training data folder
     *
     * @param trainingDataFolder
     * @throws IOException
     */
    public String train(File trainingDataFolder) throws IOException {
        // Clear data in case of train many times
        data.clear();
        bagOfWord.clear();
        
        // Make vector file as input for svm_multiclass_learn.exe, creat bag of words
        File[] files = trainingDataFolder.listFiles();
        UTF8FileUtility.createWriter(Config.BAG_OF_WORDS_FILE);
        for (int i = 0; i < files.length; ++i) {
            ArrayList<String> lines = ReadFile.getLines(files[i]);
            process(lines);
        }
        UTF8FileUtility.closeWriter();
        writeTrainingFile();

        // Call svm_multiclass_learn.exe
        Process p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "learn.bat"});
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String status = "";
        String line = null;
        while ((line = br.readLine()) != null) {
            status += line + "\n";
        }
        return status;
    };

    /**
     * Processes data from input each file
     *
     * @param lines
     */
    public void process(ArrayList<String> lines) {
        for (String line : lines) {
            int index = line.indexOf("|");
            String emotion = line.substring(0, index).toLowerCase();
            String sentence = line.substring(index + 1).toLowerCase();
            sentence = tokenizer.tokenize(sentence)[0];
            HashMap<String, Integer> m = Analyzer.analyze(sentence);

            int emotionIndex = map.get(emotion);
            data.add(new Item(emotionIndex, m));

            for (String key : m.keySet()) {
                if (!bagOfWord.containsKey(key)) {
                    bagOfWord.put(key, bagOfWord.size() + 1);
                    UTF8FileUtility.write(key + "\n");
                }
            }
        }
    }

    /**
     * Calculates values and write training file
     */
    public void writeTrainingFile() {
        UTF8FileUtility.createWriter(Config.SVM_LEARNING_DATA_FILE);
        int nSentences = data.size();

        for (Item item : data) {
            HashMap<String, Integer> m = item.getContent();
            UTF8FileUtility.write(String.valueOf(item.getEmotionIndex()));
            ArrayList<Pair<Integer, Double>> features = new ArrayList<>();
            for (String key : m.keySet()) {
                int order = bagOfWord.get(key);
                int f = m.get(key);
                double w = 1 + Math.log(f);
                features.add(new Pair<>(order, w));
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
            for (Pair feature : features) {
                UTF8FileUtility.write(" " + feature.getKey() + ":" + feature.getValue());
            }
            UTF8FileUtility.write("\n");
        }

        UTF8FileUtility.closeWriter();
    }
}
