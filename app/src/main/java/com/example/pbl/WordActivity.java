package com.example.pbl;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WordActivity extends AppCompatActivity {

    private String[] words = {"Apple", "Ball", "Cat", "Dog", "Elephant"};
    private String[] meanings = {"सेब", "गेंद", "बिल्ली", "कुत्ता", "हाथी"};
    private int currentIndex = 0;

    private TextView tvWord, tvMeaning;
    private TTSHelper ttsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);

        tvWord = findViewById(R.id.tvWord);
        tvMeaning = findViewById(R.id.tvMeaning);
        Button btnSpeak = findViewById(R.id.btnSpeak);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);

        ttsHelper = new TTSHelper(this);

        updateUI();

        btnSpeak.setOnClickListener(v -> ttsHelper.speak(words[currentIndex]));

        btnNext.setOnClickListener(v -> {
            if (currentIndex < words.length - 1) {
                currentIndex++;
                updateUI();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateUI();
            }
        });
    }

    private void updateUI() {
        tvWord.setText(words[currentIndex]);
        tvMeaning.setText(meanings[currentIndex]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsHelper.shutdown();
    }
}
