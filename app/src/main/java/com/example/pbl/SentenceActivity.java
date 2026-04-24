package com.example.pbl;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SentenceActivity extends AppCompatActivity {

    private static final String TAG = "SentenceActivity";
    private List<Word> sentenceList;
    private int currentIndex = 0;
    private boolean isHindiMode;
    private int selectedStandard;

    private TextView tvTargetSentence, tvTranslation, tvUserSpeech, tvFeedback;
    private TTSHelper ttsHelper;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        isHindiMode = getIntent().getBooleanExtra("IS_HINDI", false);
        selectedStandard = getIntent().getIntExtra("STANDARD", 1);
        Log.d(TAG, "Is Hindi Mode: " + isHindiMode + ", Standard: " + selectedStandard);

        tvTargetSentence = findViewById(R.id.tvTargetSentence);
        tvTranslation = findViewById(R.id.tvTranslation);
        tvUserSpeech = findViewById(R.id.tvUserSpeech);
        tvFeedback = findViewById(R.id.tvFeedback);
        MaterialButton btnListen = findViewById(R.id.btnListen);
        MaterialButton btnRecord = findViewById(R.id.btnRecord);
        MaterialButton btnNextSentence = findViewById(R.id.btnNextSentence);

        ttsHelper = new TTSHelper(this);
        if (isHindiMode) {
            ttsHelper.setLanguage(new Locale("hi", "IN"));
        } else {
            ttsHelper.setLanguage(Locale.US);
        }
        
        dbHelper = new DBHelper(this);

        sentenceList = DataManager.getWordsForCategory(selectedStandard, "Sentences");
        Log.d(TAG, "Loaded sentences: " + sentenceList.size());

        updateUI();

        btnListen.setOnClickListener(v -> {
            String text = isHindiMode ? sentenceList.get(currentIndex).getHindi() : sentenceList.get(currentIndex).getEnglish();
            ttsHelper.speak(text);
        });

        btnRecord.setOnClickListener(v -> SpeechHelper.startListening(this, isHindiMode));

        btnNextSentence.setOnClickListener(v -> {
            if (currentIndex < sentenceList.size() - 1) {
                currentIndex++;
                updateUI();
            } else {
                Toast.makeText(this, "All sentences completed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (sentenceList.isEmpty()) {
            Toast.makeText(this, "No sentences found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Word current = sentenceList.get(currentIndex);
        
        if (isHindiMode) {
            tvTargetSentence.setText(current.getHindi());
            tvTranslation.setText(current.getKannada());
            tvTranslation.setVisibility(android.view.View.VISIBLE);
        } else {
            tvTargetSentence.setText(current.getEnglish());
            tvTranslation.setVisibility(android.view.View.GONE);
        }

        tvUserSpeech.setText("You said: ...");
        tvFeedback.setText("Feedback");
        tvFeedback.setTextColor(Color.GRAY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SpeechHelper.SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                tvUserSpeech.setText("You said: " + spokenText);
                evaluatePronunciation(spokenText);
            }
        }
    }

    private void evaluatePronunciation(String spokenText) {
        Word current = sentenceList.get(currentIndex);
        String target = (isHindiMode ? current.getHindi() : current.getEnglish()).toLowerCase().trim();
        String spoken = spokenText.toLowerCase().trim();

        int score = 0;
        if (spoken.equals(target)) {
            tvFeedback.setText("Perfect! 🌟");
            tvFeedback.setTextColor(Color.parseColor("#2E7D32"));
            score = 100;
        } else if (spoken.contains(target) || target.contains(spoken)) {
            tvFeedback.setText("Very Good! 👍");
            tvFeedback.setTextColor(Color.parseColor("#F57C00"));
            score = 80;
        } else {
            tvFeedback.setText("Try Again 🔄");
            tvFeedback.setTextColor(Color.RED);
            score = 40;
        }

        dbHelper.insertProgress(target, score, "Sentences");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsHelper.shutdown();
    }
}
