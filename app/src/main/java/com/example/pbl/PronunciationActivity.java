package com.example.pbl;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PronunciationActivity extends AppCompatActivity {

    private static final String TAG = "PronunciationActivity";
    private String category;
    private boolean isHindiMode;
    private int currentIndex = 0;

    private List<Word> wordList;

    private TextView tvCategoryTitle, tvTargetWord, tvTranslation, tvUserSpeech, tvFeedback;
    private ImageView ivWordImage;
    private TTSHelper ttsHelper;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunciation);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        category = getIntent().getStringExtra("CATEGORY");
        isHindiMode = getIntent().getBooleanExtra("IS_HINDI", false);

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        tvTargetWord = findViewById(R.id.tvTargetWord);
        tvTranslation = findViewById(R.id.tvHindiMeaning); // Reusing ID for simplicity
        tvUserSpeech = findViewById(R.id.tvUserSpeech);
        tvFeedback = findViewById(R.id.tvFeedback);
        ivWordImage = findViewById(R.id.ivWordImage);
        
        FloatingActionButton btnListen = findViewById(R.id.btnListen);
        FloatingActionButton btnRecord = findViewById(R.id.btnRecord);
        findViewById(R.id.btnNextWord).setOnClickListener(v -> nextWord());
        findViewById(R.id.btnPrevWord).setOnClickListener(v -> prevWord());

        ttsHelper = new TTSHelper(this);
        if (isHindiMode) {
            ttsHelper.setLanguage(new Locale("hi", "IN"));
        } else {
            ttsHelper.setLanguage(Locale.ENGLISH);
        }
        
        dbHelper = new DBHelper(this);

        wordList = DataManager.getWordsForCategory(1, category);
        
        updateUI();

        tvCategoryTitle.setText(category);

        btnListen.setOnClickListener(v -> {
            String text = isHindiMode ? wordList.get(currentIndex).getHindi() : wordList.get(currentIndex).getEnglish();
            ttsHelper.speak(text);
        });
        
        btnRecord.setOnClickListener(v -> {
            SpeechHelper.startListening(this, isHindiMode);
        });
    }

    private void updateUI() {
        if (wordList.isEmpty()) {
            Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Word currentWord = wordList.get(currentIndex);
        
        if (isHindiMode) {
            tvTargetWord.setText(currentWord.getHindi());
            tvTranslation.setText(currentWord.getKannada());
            tvTranslation.setVisibility(android.view.View.VISIBLE);
        } else {
            tvTargetWord.setText(currentWord.getEnglish());
            tvTranslation.setText(currentWord.getKannada());
            // Optionally hide or show translation in English mode
            tvTranslation.setVisibility(android.view.View.VISIBLE);
        }

        ivWordImage.setImageResource(currentWord.getImageResId());
        
        tvUserSpeech.setText("You said: ...");
        tvFeedback.setText("Feedback");
        tvFeedback.setTextColor(Color.GRAY);
    }

    private void nextWord() {
        if (currentIndex < wordList.size() - 1) {
            currentIndex++;
            updateUI();
        }
    }

    private void prevWord() {
        if (currentIndex > 0) {
            currentIndex--;
            updateUI();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SpeechHelper.SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                tvUserSpeech.setText("You said: " + spokenText);
                evaluate(spokenText);
            }
        }
    }

    private void evaluate(String spoken) {
        Word currentWord = wordList.get(currentIndex);
        String target = (isHindiMode ? currentWord.getHindi() : currentWord.getEnglish()).toLowerCase().trim();
        String input = spoken.toLowerCase().trim();

        int score = 0;
        if (input.equals(target)) {
            score = 100;
            tvFeedback.setText("Excellent! 🌟");
            tvFeedback.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            score = 40;
            tvFeedback.setText("Try Again 🔄");
            tvFeedback.setTextColor(Color.RED);
        }
        dbHelper.insertProgress(target, score, category);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsHelper.shutdown();
    }
}
