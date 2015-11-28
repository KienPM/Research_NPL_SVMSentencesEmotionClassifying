/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Classifier;
import model.Trainer;
import ultis.ReadFile;

/**
 *
 * @author Ken
 */
public class Test {

    public static void main(String[] args) {
        Trainer trainer = new Trainer();
        try {
            System.out.println(trainer.train(new File("G:\\Training data\\SVM Sentence Emotion Classifying")));
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }

//        Classifier classifier = new Classifier();
//        try {
//            classifier.classify("Nhìn ra ngoài cửa sổ phòng ngủ");
//        } catch (IOException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        try {
//            ArrayList<String> lines = ReadFile.getLines(new File("G:\\Training data\\SVM Sentence Emotion Classifying\\training_data.xlsx"));
//            for (int i = 0; i < 3; ++i) {
//                System.out.println(lines.get(i));
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

}
