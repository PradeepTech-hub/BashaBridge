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
    private int totalXP = 0;
    private int masteredCount = 0;
    private List<Integer> scores = new ArrayList<>();

    private TextView tvTargetSentence, tvTranslation, tvUserSpeech, tvFeedback;
    private ImageView ivSentenceImage;
    private com.google.android.material.button.MaterialButton btnSlowListen, btnRetry;
    private TTSHelper ttsHelper;
    private DBHelper dbHelper;
    private FirebaseFirestore db;
    private ListenerRegistration registration;
    private float ttsSpeed = 1.0f;

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
        
        btnSlowListen = findViewById(R.id.btnSlowListen);
        btnRetry = findViewById(R.id.btnRetry);
        com.google.android.material.button.MaterialButton btnListen = findViewById(R.id.btnListen);
        com.google.android.material.button.MaterialButton btnRecord = findViewById(R.id.btnRecord);
        com.google.android.material.button.MaterialButton btnNextSentence = findViewById(R.id.btnNextSentence);
        com.google.android.material.button.MaterialButton btnPrevSentence = findViewById(R.id.btnPrevSentence);

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
            ttsSpeed = 1.0f;
            speakCurrentSentence();
        });

        btnSlowListen.setOnClickListener(v -> {
            ttsSpeed = 0.5f;
            speakCurrentSentence();
        });

        btnRetry.setOnClickListener(v -> {
            tvUserSpeech.setText("You said: ...");
            tvFeedback.setText("Feedback");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_neutral));
            btnRetry.setVisibility(android.view.View.INVISIBLE);
        });

        btnRecord.setOnClickListener(v -> SpeechHelper.startListening(this, isHindiMode));

        btnNextSentence.setOnClickListener(v -> nextSentence());

        btnPrevSentence.setOnClickListener(v -> prevSentence());
    }

    private void nextSentence() {
        if (currentIndex < sentenceList.size() - 1) {
            currentIndex++;
            updateUI();
        } else {
            showSummary();
        }
    }

    private void prevSentence() {
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
                evaluatePronunciation(spokenText);
            }
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
        intent.putExtra("TOTAL_COUNT", sentenceList.size());
        startActivity(intent);
        finish();
    }

    private void speakCurrentSentence() {
        if (!sentenceList.isEmpty()) {
            String text = isHindiMode ? sentenceList.get(currentIndex).getHindi() : sentenceList.get(currentIndex).getEnglish();
            ttsHelper.setSpeechRate(ttsSpeed);
            ttsHelper.speak(text);
        }
    }

    private void loadFirestoreSentences() {
        // Initial load from local cache
        mergeAndDisplay(dbHelper.getOfflineContent(selectedStandard, "Sentences", "Sentence"));

        if (!SyncManager.getInstance(this).isOnline()) {
            return;
        }

        registration = db.collection("sentences")
                .whereEqualTo("standard", selectedStandard)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        List<Word> firestoreSentences = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            try {
                                Word word = document.toObject(Word.class);
                                word.setId(document.getId());
                                firestoreSentences.add(word);
                                // Cache for offline
                                dbHelper.saveContent(word, "Sentence");
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing Firestore document", e);
                            }
                        }
                        mergeAndDisplay(firestoreSentences);
                    }
                });
    }

    private void mergeAndDisplay(List<Word> firestoreSentences) {
        java.util.Map<String, Word> mergedMap = new java.util.LinkedHashMap<>();

        // 1. Add default data first
        List<Word> defaults = DataManager.getWordsForCategory(selectedStandard, "Sentences");
        for (Word w : defaults) {
            String key = (w.getEnglish().trim() + "_" + selectedStandard).toLowerCase();
            mergedMap.put(key, w);
        }

        // 2. Add/Overwrite with Firestore data
        for (Word w : firestoreSentences) {
            String key = (w.getEnglish().trim() + "_" + selectedStandard).toLowerCase();
            mergedMap.put(key, w);
        }

        sentenceList.clear();
        sentenceList.addAll(mergedMap.values());
        updateUI();
    }

    private void updateUI() {
        if (sentenceList.isEmpty()) {
            Toast.makeText(this, "No sentences available", Toast.LENGTH_SHORT).show();
            return;
        }
        Word current = sentenceList.get(currentIndex);
        
        if (isHindiMode) {
            tvTargetSentence.setText(current.getHindi());
        } else {
            tvTargetSentence.setText(current.getEnglish());
        }
        tvTranslation.setText(current.getKannada());
        
        if (current.getImageUrl() != null && !current.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(current.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivSentenceImage);
        } else {
            ivSentenceImage.setImageResource(current.getImageResId() != 0 ? current.getImageResId() : android.R.drawable.ic_menu_gallery);
        }
        
        tvUserSpeech.setText("You said: ...");
        tvFeedback.setText("Feedback");
        tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.feedback_neutral));
        btnRetry.setVisibility(android.view.View.INVISIBLE);
    }

    private void evaluatePronunciation(String spokenText) {
        Word current = sentenceList.get(currentIndex);
        String target = (isHindiMode ? current.getHindi() : current.getEnglish()).toLowerCase().trim();
        String spoken = NumberUtils.normalizeNumbers(spokenText.toLowerCase().trim());
        target = NumberUtils.normalizeNumbers(target);

        int score = calculateFuzzyScore(target, spoken);
        scores.add(score);
        if (score >= 80) masteredCount++;

        updateFeedbackUI(score);

        String uid = FirebaseAuth.getInstance().getUid();
        dbHelper.insertProgress(target, score, "Sentences");

        if (SyncManager.getInstance(this).isOnline()) {
            saveProgressToFirestore(target, score);
            if (score >= 60) {
                int xp = score / 5;
                totalXP += xp;
                updateUserXP(xp);
            }
        } else if (uid != null) {
            User localUser = dbHelper.getUser(uid);
            String userName = (localUser != null && localUser.getName() != null) ? localUser.getName() : "Student";
            String standard = (localUser != null && localUser.getStandard() != null) ? localUser.getStandard() : String.valueOf(selectedStandard);
            dbHelper.addToSyncQueue(uid, userName, standard, target, score, "Sentences");
            if (localUser != null && score >= 60) {
                int xp = score / 5;
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

    private void saveProgressToFirestore(String text, int score) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        User currentUser = dbHelper.getUser(uid);
        String userName = (currentUser != null && currentUser.getName() != null) ? currentUser.getName() : "Student";
        String standard = (currentUser != null && currentUser.getStandard() != null) ? currentUser.getStandard() : String.valueOf(selectedStandard);

        Map<String, Object> progress = new HashMap<>();
        progress.put("uid", uid);
        progress.put("name", userName);
        progress.put("standard", standard);
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
