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
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PronunciationActivity extends AppCompatActivity {

    private static final String TAG = "PronunciationActivity";
    private String category;
    private boolean isHindiMode;
    private int currentIndex = 0;
    private int selectedStandard;

    private List<Word> wordList;

    private TextView tvCategoryTitle, tvTargetWord, tvTranslation, tvUserSpeech, tvFeedback;
    private ImageView ivWordImage;
    private TTSHelper ttsHelper;
    private DBHelper dbHelper;
    private FirebaseFirestore db;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunciation);

        db = FirebaseFirestore.getInstance();
        category = getIntent().getStringExtra("CATEGORY");
        isHindiMode = getIntent().getBooleanExtra("IS_HINDI", false);
        selectedStandard = getIntent().getIntExtra("STANDARD", 1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        tvTargetWord = findViewById(R.id.tvTargetWord);
        tvTranslation = findViewById(R.id.tvHindiMeaning);
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
        wordList = new ArrayList<>();
        
        tvCategoryTitle.setText(category);
        loadFirestoreData();

        btnListen.setOnClickListener(v -> {
            if (!wordList.isEmpty()) {
                String text = isHindiMode ? wordList.get(currentIndex).getHindi() : wordList.get(currentIndex).getEnglish();
                ttsHelper.speak(text);
            }
        });
        
        btnRecord.setOnClickListener(v -> SpeechHelper.startListening(this, isHindiMode));
    }

    private void loadFirestoreData() {
        registration = db.collection("words")
                .whereEqualTo("standard", selectedStandard)
                .whereEqualTo("category", category)
                .addSnapshotListener((value, error) -> {
                    // Use LinkedHashMap to preserve order and merge by key
                    java.util.Map<String, Word> mergedMap = new java.util.LinkedHashMap<>();

                    // 1. Add default data first
                    List<Word> defaults = DataManager.getWordsForCategory(selectedStandard, category);
                    for (Word w : defaults) {
                        String key = (w.getEnglish().trim() + "_" + selectedStandard).toLowerCase();
                        mergedMap.put(key, w);
                    }

                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        wordList.clear();
                        wordList.addAll(mergedMap.values());
                        updateUI();
                        return;
                    }

                    if (value != null) {
                        // 2. Add/Overwrite with Firestore data
                        for (QueryDocumentSnapshot document : value) {
                            try {
                                Word word = document.toObject(Word.class);
                                word.setId(document.getId());
                                String key = (word.getEnglish().trim() + "_" + selectedStandard).toLowerCase();
                                mergedMap.put(key, word);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing Firestore document", e);
                            }
                        }
                    }

                    wordList.clear();
                    wordList.addAll(mergedMap.values());
                    updateUI();
                });
    }

    private void updateUI() {
        if (wordList.isEmpty()) {
            Toast.makeText(this, "No words available in this category", Toast.LENGTH_SHORT).show();
            return;
        }
        Word currentWord = wordList.get(currentIndex);
        
        if (isHindiMode) {
            tvTargetWord.setText(currentWord.getHindi());
            tvTranslation.setText(currentWord.getKannada());
        } else {
            tvTargetWord.setText(currentWord.getEnglish());
            tvTranslation.setText(currentWord.getKannada());
        }
        tvTranslation.setVisibility(android.view.View.VISIBLE);
        
        if (currentWord.getImageUrl() != null && !currentWord.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentWord.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivWordImage);
        } else {
            ivWordImage.setImageResource(currentWord.getImageResId() != 0 ? currentWord.getImageResId() : android.R.drawable.ic_menu_gallery);
        }
        
        tvUserSpeech.setText("You said: ...");
        tvFeedback.setText("Feedback");
        tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_neutral));
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
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_perfect));
        } else {
            score = 40;
            tvFeedback.setText("Try Again 🔄");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.error));
        }
        
        // Save to Local DB
        dbHelper.insertProgress(target, score, category);
        
        // Save to Firestore Progress Collection
        saveProgressToFirestore(target, score);
    }

    private void saveProgressToFirestore(String word, int score) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> progress = new HashMap<>();
        progress.put("uid", uid);
        progress.put("word", word);
        progress.put("score", score);
        progress.put("category", category);
        progress.put("timestamp", System.currentTimeMillis());

        db.collection("progress").add(progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) {
            registration.remove();
        }
        ttsHelper.shutdown();
    }
}
