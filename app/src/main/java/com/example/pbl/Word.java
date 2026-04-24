package com.example.pbl;

public class Word {
    private String english;
    private String kannada;
    private String hindi;
    private int imageResId;

    public Word(String english, String kannada, int imageResId) {
        this.english = english;
        this.kannada = kannada;
        this.hindi = "";
        this.imageResId = imageResId;
    }

    public Word(String english, String kannada, String hindi, int imageResId) {
        this.english = english;
        this.kannada = kannada;
        this.hindi = hindi;
        this.imageResId = imageResId;
    }

    public String getEnglish() { return english; }
    public String getKannada() { return kannada; }
    public String getHindi() { return hindi; }
    public int getImageResId() { return imageResId; }
}
