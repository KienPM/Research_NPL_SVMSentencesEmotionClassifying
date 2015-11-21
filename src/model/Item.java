/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.HashMap;

/**
 *
 * @author Ken
 */
public class Item {
    private int emotionIndex;
    private HashMap<String, Integer> content;

    public Item() {
    }

    public Item(int emotionIndex, HashMap<String, Integer> content) {
        this.emotionIndex = emotionIndex;
        this.content = content;
    }

    public int getEmotionIndex() {
        return emotionIndex;
    }

    public void setEmotionIndex(int emotionIndex) {
        this.emotionIndex = emotionIndex;
    }

    public HashMap<String, Integer> getContent() {
        return content;
    }

    public void setContent(HashMap<String, Integer> content) {
        this.content = content;
    }

    
    
}
