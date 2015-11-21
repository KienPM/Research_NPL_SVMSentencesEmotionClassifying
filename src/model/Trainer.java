/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
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
public class Trainer {

    private VietTokenizer tokenizer;
    // Stores map emotions with their indexes
    HashMap<String, Integer> map;
    // Stores processed training data
    private ArrayList<Item> data;
    /*
     Stores orther words in training data with number of sentences it appears in
     and order of it in bag of word
     */
    private HashMap<String, Pair<Integer, Integer>> bagOfWord;

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
    public void train(File trainingDataFolder) throws IOException {
        // Make training file for svm_multiclass_learn.exe, creat bag of words
        File[] files = trainingDataFolder.listFiles();
        UTF8FileUtility.createWriter(Config.BAG_OF_WORDS_FILE);
        for (int i = 0; i < files.length; ++i) {
            if (getExtension(files[i]).equals("xlsx")) {
                ArrayList<String> lines = ReadXlsxInput.getLines(files[i].getAbsolutePath());
                process(lines);
            } else {
                String[] lines = UTF8FileUtility.getLines(files[i].getAbsolutePath());
                process(lines);
            }
        }
        UTF8FileUtility.closeWriter();
        writeTrainingFile();

        // Call svm_multiclass_learn.exe
        Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "learn.bat"});
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
            int index = lines[i].indexOf("|");
            String emotion = lines[i].substring(0, index);
            String sentence = lines[i].substring(index + 1).toLowerCase();
            sentence = tokenizer.tokenize(sentence)[0];
            HashMap<String, Integer> m = Analyzer.analyze(sentence);
            int emotionIndex = map.get(emotion);
            data.add(new Item(emotionIndex, m));

            for (String key : m.keySet()) {
                if (bagOfWord.containsKey(key)) {
                    Pair<Integer, Integer> pair = bagOfWord.get(key);
                    bagOfWord.put(key, new Pair<Integer, Integer>(pair.getKey(), pair.getValue() + 1));
                } else {
                    bagOfWord.put(key, new Pair<Integer, Integer>(bagOfWord.size() + 1, 1));
                    UTF8FileUtility.write(key + "\n");
                }
            }
        }
    }

    /**
     * Processes data for ArrayList<String>
     *
     * @param lines
     */
    public void process(ArrayList<String> lines) {
        for (String line : lines) {
            int index = line.indexOf("|");
            String emotion = line.substring(0, index);
            String sentence = line.substring(index + 1).toLowerCase();
            sentence = tokenizer.tokenize(sentence)[0];
            HashMap<String, Integer> m = Analyzer.analyze(sentence);

            int emotionIndex = map.get(emotion);
            data.add(new Item(emotionIndex, m));

            for (String key : m.keySet()) {
                if (bagOfWord.containsKey(key)) {
                    Pair<Integer, Integer> pair = bagOfWord.get(key);
                    bagOfWord.put(key, new Pair<Integer, Integer>(pair.getKey(), pair.getValue() + 1));
                } else {
                    bagOfWord.put(key, new Pair<Integer, Integer>(bagOfWord.size() + 1, 1));
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
            UTF8FileUtility.write(item.getEmotionIndex() + " ");
            ArrayList<Pair<Integer, Double>> features = new ArrayList<>();
            for (String key : m.keySet()) {
                int f = m.get(key);
                Pair<Integer, Integer> pair = bagOfWord.get(key);
                int h = pair.getValue();
                double w = (1 + Math.log(f)) * Math.log(1.0 * nSentences / h);
                features.add(new Pair<Integer, Double>(pair.getKey(), w));
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
                UTF8FileUtility.write(feature.getKey() + ":" + feature.getValue() + " ");
            }
            UTF8FileUtility.write("\n");
        }

        UTF8FileUtility.closeWriter();
    }
}
