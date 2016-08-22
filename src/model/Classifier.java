/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class Classifier {

    private VietTokenizer tokenizer;
    // Stores map emotions with their indexes
    HashMap<String, Integer> map;
    // Maps other words with their order
    private HashMap<String, Integer> bagOfWord;
    // Stores processed testing data
    ArrayList<Item> data;

    public Classifier() {
        Analyzer.loadStopWords(Config.STOP_WORDS_FILE);
        tokenizer = new VietTokenizer();
        map = new HashMap<>();
        map();
        bagOfWord = new HashMap<>();
        loadBagOfWords();
        data = new ArrayList<>();
    }

    public Classifier(VietTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        Analyzer.loadStopWords(Config.STOP_WORDS_FILE);
        map = new HashMap<>();
        map();
        bagOfWord = new HashMap<>();
        loadBagOfWords();
        data = new ArrayList<>();
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
            if (bagOfWord.containsKey(key)) {
                int order = bagOfWord.get(key);
                int f = m.get(key);
                double w = 1 + Math.log(f);
                features.add(new Pair<Integer, Double>(order, w));
            }
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
        UTF8FileUtility.write("1");
        for (Pair feature : features) {
            UTF8FileUtility.write(" " + feature.getKey() + ":" + feature.getValue());
        }
        UTF8FileUtility.write("\n");
        UTF8FileUtility.closeWriter();

        // Call svm_multiclass_classify.exe
        Process p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "classify.bat"});
        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(Classifier.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Read output
        Scanner scanner = new Scanner(new FileInputStream(Config.SVM_OUPUT));
        String line = scanner.nextLine();
        int index = Integer.parseInt(line.substring(0, line.indexOf(" ")));
        String emotion = Config.EMOTIONS[index - 1];

        return emotion;
    }

    public String test(File testingDataFolder) throws IOException {
        String result = "";

        File[] files = testingDataFolder.listFiles();
        for (int i = 0; i < files.length; ++i) {
            ArrayList<String> lines = ReadFile.getLines(files[i]);
            process(lines);
        }
        writeTestingFile();

        // Call svm_multiclass_classify.exe
        Process p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "test.bat"});
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = br.readLine()) != null) {
            result += line + "\n";
        }
        return result;
    }

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

            if (map.get(emotion) == null) {
                System.out.println(emotion);
                continue;
            }
            int emotionIndex = map.get(emotion);
            data.add(new Item(emotionIndex, m));
        }
    }

    /**
     * Calculates values and write training file
     */
    public void writeTestingFile() {
        UTF8FileUtility.createWriter(Config.SVM_TESTING_DATA_FILE);
        int nSentences = data.size();

        for (Item item : data) {
            HashMap<String, Integer> m = item.getContent();
            UTF8FileUtility.write(String.valueOf(item.getEmotionIndex()));
            ArrayList<Pair<Integer, Double>> features = new ArrayList<>();
            for (String key : m.keySet()) {
                if (!bagOfWord.containsKey(key)) {
                    continue;
                }
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
