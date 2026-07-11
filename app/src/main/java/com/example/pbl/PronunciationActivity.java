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
    private int totalXP = 0;
    private int masteredCount = 0;
    private List<Integer> scores = new ArrayList<>();

    private List<Word> wordList;

    private TextView tvCategoryTitle, tvTargetWord, tvTranslation, tvUserSpeech, tvFeedback;
    private ImageView ivWordImage;
    private com.google.android.material.button.MaterialButton btnSlowListen, btnRetry;
    private TTSHelper ttsHelper;
    private DBHelper dbHelper;
    private FirebaseFirestore db;
    private ListenerRegistration registration;
    private float ttsSpeed = 1.0f;

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
        
        btnSlowListen = findViewById(R.id.btnSlowListen);
        btnRetry = findViewById(R.id.btnRetry);
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
            ttsSpeed = 1.0f;
            speakCurrentWord();
        });

        btnSlowListen.setOnClickListener(v -> {
            ttsSpeed = 0.5f;
            speakCurrentWord();
        });

        btnRetry.setOnClickListener(v -> {
            tvUserSpeech.setText("You said: ...");
            tvFeedback.setText("Feedback");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_neutral));
            btnRetry.setVisibility(android.view.View.INVISIBLE);
        });
        
        btnRecord.setOnClickListener(v -> SpeechHelper.startListening(this, isHindiMode));
    }

    private void speakCurrentWord() {
        if (!wordList.isEmpty()) {
            String text = isHindiMode ? wordList.get(currentIndex).getHindi() : wordList.get(currentIndex).getEnglish();
            ttsHelper.setSpeechRate(ttsSpeed);
            ttsHelper.speak(text);
        }
    }

    private void loadFirestoreData() {
        // Initial load from local cache (merges DataManager + previously synced Firestore content)
        mergeAndDisplay(dbHelper.getOfflineContent(selectedStandard, category, "Word"));

        if (!SyncManager.getInstance(this).isOnline()) {
            return;
        }

        registration = db.collection("words")
                .whereEqualTo("standard", selectedStandard)
                .whereEqualTo("category", category)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        List<Word> firestoreWords = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            try {
                                Word word = document.toObject(Word.class);
                                word.setId(document.getId());
                                firestoreWords.add(word);
                                // Update local cache
                                dbHelper.saveContent(word, "Word");
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing Firestore document", e);
                            }
                        }
                        mergeAndDisplay(firestoreWords);
                    }
                });
    }

    private void mergeAndDisplay(List<Word> firestoreWords) {
        java.util.Map<String, Word> mergedMap = new java.util.LinkedHashMap<>();

        // 1. Add default data first
        List<Word> defaults = DataManager.getWordsForCategory(selectedStandard, category);
        for (Word w : defaults) {
            String key = (w.getEnglish().trim() + "_" + selectedStandard).toLowerCase();
            mergedMap.put(key, w);
        }

        // 2. Add/Overwrite with Firestore data
        for (Word w : firestoreWords) {
            String key = (w.getEnglish().trim() + "_" + selectedStandard).toLowerCase();
            mergedMap.put(key, w);
        }

        wordList.clear();
        wordList.addAll(mergedMap.values());
        updateUI();
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
        btnRetry.setVisibility(android.view.View.INVISIBLE);
    }

    private void nextWord() {
        if (currentIndex < wordList.size() - 1) {
            currentIndex++;
            updateUI();
        } else {
            showSummary();
        }
    }

    private void showSummary() {
        int avgScore = 0;
        if (!scores.isEmpty()) {
            int sum = 0;
            for (int s : scores) sum += s;
            avgScore = sum / scores.size();
        }

        Intent intent = new Intent(this, LessonCompletionActivity.class);
        intent.putExtra("TOTAL_XP", totalXP);
        intent.putExtra("ACCURACY", avgScore);
        intent.putExtra("MASTERED_COUNT", masteredCount);
        intent.putExtra("TOTAL_COUNT", wordList.size());
        startActivity(intent);
        finish();
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
        String input = NumberUtils.normalizeNumbers(spoken.toLowerCase().trim());
        target = NumberUtils.normalizeNumbers(target);

        int score = calculateFuzzyScore(target, input);
        scores.add(score);
        if (score >= 80) masteredCount++;
        
        updateFeedbackUI(score);
        
        String uid = FirebaseAuth.getInstance().getUid();
        
        // Save to Local DB (History)
        dbHelper.insertProgress(target, score, category);
        
        // --- Offline Sync Support ---
        if (SyncManager.getInstance(this).isOnline()) {
            saveProgressToFirestore(target, score);
            if (score >= 60) {
                int xp = score / 10;
                totalXP += xp;
                updateUserXP(xp);
            }
        } else if (uid != null) {
            dbHelper.addToSyncQueue(uid, target, score, category);
            // Update local user cache XP
            User localUser = dbHelper.getUser(uid);
            if (localUser != null && score >= 60) {
                int xp = score / 10;
                totalXP += xp;
                localUser.setXp(localUser.getXp() + xp);
                dbHelper.saveUser(localUser);
            }
        }
    }

    private void updateFeedbackUI(int score) {
        if (score >= 95) {
            tvFeedback.setText("Excellent! 🌟 (" + score + "%)");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_perfect));
            btnRetry.setVisibility(android.view.View.INVISIBLE);
        } else if (score >= 80) {
            tvFeedback.setText("Great Job! 👍 (" + score + "%)");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_perfect));
            btnRetry.setVisibility(android.view.View.VISIBLE);
        } else if (score >= 60) {
            tvFeedback.setText("Good Try! 🙂 (" + score + "%)");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_good));
            btnRetry.setVisibility(android.view.View.VISIBLE);
        } else {
            tvFeedback.setText("Keep Practicing! 🔄 (" + score + "%)");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.error));
            btnRetry.setVisibility(android.view.View.VISIBLE);
        }
    }

    private int calculateFuzzyScore(String target, String input) {
        if (target.isEmpty() || input.isEmpty()) return 0;
        if (target.equals(input)) return 100;

        int distance = LevenshteinDistance(target, input);
        int maxLength = Math.max(target.length(), input.length());
        
        float score = (1.0f - (float) distance / maxLength) * 100;
        return Math.max(0, Math.round(score));
    }

    private int LevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(Math.min(
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private void updateUserXP(int xpToAdd) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .update("xp", com.google.firebase.firestore.FieldValue.increment(xpToAdd));
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
