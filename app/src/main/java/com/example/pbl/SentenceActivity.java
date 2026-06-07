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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SentenceActivity extends AppCompatActivity {

    private static final String TAG = "SentenceActivity";
    private List<Word> sentenceList;
    private int currentIndex = 0;
    private boolean isHindiMode;
    private int selectedStandard;

    private TextView tvTargetSentence, tvTranslation, tvUserSpeech, tvFeedback;
    private ImageView ivSentenceImage;
    private TTSHelper ttsHelper;
    private DBHelper dbHelper;
    private FirebaseFirestore db;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence);

        db = FirebaseFirestore.getInstance();
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        isHindiMode = getIntent().getBooleanExtra("IS_HINDI", false);
        selectedStandard = getIntent().getIntExtra("STANDARD", 1);

        tvTargetSentence = findViewById(R.id.tvTargetSentence);
        tvTranslation = findViewById(R.id.tvTranslation);
        tvUserSpeech = findViewById(R.id.tvUserSpeech);
        tvFeedback = findViewById(R.id.tvFeedback);
        ivSentenceImage = findViewById(R.id.ivSentenceImage);
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
        sentenceList = new ArrayList<>();

        loadFirestoreSentences();

        btnListen.setOnClickListener(v -> {
            if (!sentenceList.isEmpty()) {
                String text = isHindiMode ? sentenceList.get(currentIndex).getHindi() : sentenceList.get(currentIndex).getEnglish();
                ttsHelper.speak(text);
            }
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

    private void loadFirestoreSentences() {
        registration = db.collection("sentences")
                .whereEqualTo("standard", selectedStandard)
                .addSnapshotListener((value, error) -> {
                    // Use LinkedHashMap to preserve order and merge by key
                    java.util.Map<String, Word> mergedMap = new java.util.LinkedHashMap<>();

                    // 1. Add default data first
                    List<Word> defaults = DataManager.getWordsForCategory(selectedStandard, "Sentences");
                    for (Word w : defaults) {
                        String key = (w.getEnglish().trim() + "_" + selectedStandard).toLowerCase();
                        mergedMap.put(key, w);
                    }

                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        sentenceList.clear();
                        sentenceList.addAll(mergedMap.values());
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

                    sentenceList.clear();
                    sentenceList.addAll(mergedMap.values());
                    updateUI();
                });
    }

    private void updateUI() {
        if (sentenceList.isEmpty()) {
            Toast.makeText(this, "No sentences found", Toast.LENGTH_SHORT).show();
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

        if (current.getImageUrl() != null && !current.getImageUrl().isEmpty()) {
            ivSentenceImage.setVisibility(android.view.View.VISIBLE);
            Glide.with(this)
                    .load(current.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivSentenceImage);
        } else {
            ivSentenceImage.setVisibility(android.view.View.GONE);
        }

        tvUserSpeech.setText("You said: ...");
        tvFeedback.setText("Feedback");
        tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_neutral));
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
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_perfect));
            score = 100;
        } else if (spoken.contains(target) || target.contains(spoken)) {
            tvFeedback.setText("Very Good! 👍");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_good));
            score = 80;
        } else {
            tvFeedback.setText("Try Again 🔄");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.error));
            score = 40;
        }

        dbHelper.insertProgress(target, score, "Sentences");
        saveProgressToFirestore(target, score);
    }

    private void saveProgressToFirestore(String text, int score) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> progress = new HashMap<>();
        progress.put("uid", uid);
        progress.put("word", text);
        progress.put("score", score);
        progress.put("category", "Sentences");
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
