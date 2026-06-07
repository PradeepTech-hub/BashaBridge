package com.example.pbl;

public class Word {
    private String id;
    private String english;
    private String kannada;
    private String hindi;
    private int imageResId;
    private String imageUrl; // New field for custom photos
    private String category;
    private int standard;

    public Word() {
        // Required for Firestore
    }

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getKannada() { return kannada; }
    public void setKannada(String kannada) { this.kannada = kannada; }

    public String getHindi() { return hindi; }
    public void setHindi(String hindi) { this.hindi = hindi; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStandard() { return standard; }
    public void setStandard(int standard) { this.standard = standard; }
}
