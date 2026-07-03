package com.example.pbl;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleGroupAppLanguage;
    private DBHelper dbHelper;
    private MediaPlayer mediaPlayer;
    private int userStandard = 1; // Default
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration userRegistration;

    // New UI Elements
    private TextView tvStreakCount, tvXPCount, tvDailyGoal, tvLastCategory;
    private com.google.android.material.progressindicator.LinearProgressIndicator 
            progressAnimals, progressFruits, progressDaily, progressSchool, progressNumbers, progressSentences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new DBHelper(this);

        // --- Trigger Background Sync ---
        SyncManager.getInstance(this).syncProgress();
        SyncManager.getInstance(this).downloadContent();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Try to get standard from Intent first for faster load
        String intentStd = getIntent().getStringExtra("USER_STANDARD");
        if (intentStd != null && !intentStd.equals("N/A")) {
            try {
                userStandard = Integer.parseInt(intentStd);
            } catch (NumberFormatException ignored) {}
        }

        initViews();
        loadUserData();
        updateCategoryProgress();
        
        // Show Offline Indicator
        if (!SyncManager.getInstance(this).isOnline()) {
            Toast.makeText(this, "Working in Offline Mode", Toast.LENGTH_LONG).show();
            TextView tvSub = findViewById(R.id.tvSub);
            tvSub.setText(tvSub.getText() + " (Offline)");
        }

        // Animation
        androidx.core.widget.NestedScrollView rootLayout = findViewById(R.id.rootLayout);
        Animation welcomeAnim = AnimationUtils.loadAnimation(this, R.anim.welcome_animation);
        rootLayout.startAnimation(welcomeAnim);

        // Play Music
        playWelcomeMusic();

        dbHelper = new DBHelper(this);

        // Request Microphone Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        toggleGroupAppLanguage = findViewById(R.id.toggleGroupAppLanguage);

        toggleGroupAppLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                updateAppLanguage(checkedId == R.id.btnAppKannada);
            }
        });
        
        MaterialButtonToggleGroup toggleGroupLanguage = findViewById(R.id.toggleGroupLanguage);

        setupCategoryCard(R.id.cardAnimals, "Animals", toggleGroupLanguage);
        setupCategoryCard(R.id.cardFruits, "Fruits", toggleGroupLanguage);
        setupCategoryCard(R.id.cardDaily, "Daily Use", toggleGroupLanguage);
        setupCategoryCard(R.id.cardSchool, "School Objects", toggleGroupLanguage);
        setupCategoryCard(R.id.cardNumbers, "Numbers", toggleGroupLanguage);
        setupCategoryCard(R.id.cardSentences, "Sentences", toggleGroupLanguage);

        findViewById(R.id.cardProgress).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProgressActivity.class));
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void initViews() {
        tvStreakCount = findViewById(R.id.tvStreakCount);
        tvXPCount = findViewById(R.id.tvXPCount);
        tvDailyGoal = findViewById(R.id.tvDailyGoal);
        tvLastCategory = findViewById(R.id.tvLastCategory);

        progressAnimals = findViewById(R.id.progressAnimals);
        progressFruits = findViewById(R.id.progressFruits);
        progressDaily = findViewById(R.id.progressDaily);
        progressSchool = findViewById(R.id.progressSchool);
        progressNumbers = findViewById(R.id.progressNumbers);
        progressSentences = findViewById(R.id.progressSentences);

        findViewById(R.id.btnContinue).setOnClickListener(v -> resumeLastCategory());
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        userRegistration = db.collection("users").document(uid)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) return;
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            String standardStr = user.getStandard();
                            try {
                                userStandard = Integer.parseInt(standardStr != null && !standardStr.trim().isEmpty() && !standardStr.equals("N/A") ? standardStr : "1");
                            } catch (NumberFormatException e) {
                                userStandard = 1; // Fallback
                            }
                            updateDashboardUI(user);
                            checkAndUpdateStreak(user);
                        }
                    }
                });
    }

    private void updateDashboardUI(User user) {
        tvStreakCount.setText(String.valueOf(user.getStreak()));
        tvXPCount.setText(String.valueOf(user.getXp()));
        
        // Simple daily goal: 5 words/sentences today
        // This would ideally come from a sub-collection 'daily_stats'
        tvDailyGoal.setText("0/5"); 
        
        String lastCat = dbHelper.getFavoriteCategory();
        tvLastCategory.setText(lastCat.equals("None") ? "Start your first lesson!" : "Next: " + lastCat);
    }

    private void checkAndUpdateStreak(User user) {
        long now = System.currentTimeMillis();
        long lastActive = user.getLastActive();
        
        // Simple streak logic: if last active was yesterday, increment. If today, keep. If before yesterday, reset.
        java.util.Calendar calToday = java.util.Calendar.getInstance();
        java.util.Calendar calLast = java.util.Calendar.getInstance();
        calLast.setTimeInMillis(lastActive);

        boolean isSameDay = calToday.get(java.util.Calendar.YEAR) == calLast.get(java.util.Calendar.YEAR) &&
                          calToday.get(java.util.Calendar.DAY_OF_YEAR) == calLast.get(java.util.Calendar.DAY_OF_YEAR);

        if (!isSameDay) {
            calToday.add(java.util.Calendar.DAY_OF_YEAR, -1);
            boolean isYesterday = calToday.get(java.util.Calendar.YEAR) == calLast.get(java.util.Calendar.YEAR) &&
                                 calToday.get(java.util.Calendar.DAY_OF_YEAR) == calLast.get(java.util.Calendar.DAY_OF_YEAR);

            int newStreak = isYesterday ? user.getStreak() + 1 : 1;
            
            db.collection("users").document(user.getUid())
                    .update("streak", newStreak, "lastActive", now);
        }
    }

    private void updateCategoryProgress() {
        // Fetch progress for each category from local DB
        progressAnimals.setProgress(calculateProgress("Animals"));
        progressFruits.setProgress(calculateProgress("Fruits"));
        progressDaily.setProgress(calculateProgress("Daily Use"));
        progressSchool.setProgress(calculateProgress("School Objects"));
        progressNumbers.setProgress(calculateProgress("Numbers"));
        progressSentences.setProgress(calculateProgress("Sentences"));
    }

    private int calculateProgress(String category) {
        // This is a simplified progress calculation. 
        // In a real app, it would be (learned_words / total_words_in_category) * 100
        ArrayList<HashMap<String, String>> progressList = dbHelper.getAllProgress();
        int count = 0;
        for (HashMap<String, String> p : progressList) {
            if (category.equals(p.get("category"))) {
                count++;
            }
        }
        return Math.min(count * 10, 100); // 10% per word for demo
    }

    private void resumeLastCategory() {
        String lastCat = dbHelper.getFavoriteCategory();
        if (!lastCat.equals("None")) {
            MaterialButtonToggleGroup toggleGroupLanguage = findViewById(R.id.toggleGroupLanguage);
            boolean isHindi = toggleGroupLanguage.getCheckedButtonId() == R.id.btnHindi;
            
            Intent intent;
            if (lastCat.equals("Sentences")) {
                intent = new Intent(this, SentenceActivity.class);
            } else {
                intent = new Intent(this, PronunciationActivity.class);
            }
            intent.putExtra("CATEGORY", lastCat);
            intent.putExtra("IS_HINDI", isHindi);
            intent.putExtra("STANDARD", userStandard);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Select a category to begin!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserStandard() {
        // Removed as it is now handled in loadUserData
    }

    private void updateAppLanguage(boolean isKannada) {
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvSub = findViewById(R.id.tvSub);
        TextView tvAppLangLabel = findViewById(R.id.tvAppLangLabel);
        TextView tvCatLabel = findViewById(R.id.tvCatLabel);
        TextView tvLangLabel = findViewById(R.id.tvLangLabel);
        
        if (isKannada) {
            tvWelcome.setText("ಸ್ವಾಗತ!");
            tvSub.setText("ತರಬೇತಿ ಪ್ರಾರಂಭಿಸಲು ವರ್ಗವನ್ನು ಆಯ್ಕೆಮಾಡಿ");
            tvAppLangLabel.setText("ಅಪ್ಲಿಕೇಶನ್ ಭಾಷೆ");
            tvLangLabel.setText("ತರಬೇತಿ ಭಾಷೆ");
            tvCatLabel.setText("ವರ್ಗಗಳು");
            if (findViewById(R.id.toolbar) != null) {
                ((com.google.android.material.appbar.MaterialToolbar)findViewById(R.id.toolbar)).setTitle("Basha Setu");
            }
        } else {
            tvWelcome.setText("Welcome back!");
            tvSub.setText("Select a category to start practicing");
            tvAppLangLabel.setText("App Interface Language");
            tvLangLabel.setText("Practice Language");
            tvCatLabel.setText("Categories");
            if (findViewById(R.id.toolbar) != null) {
                ((com.google.android.material.appbar.MaterialToolbar)findViewById(R.id.toolbar)).setTitle("Basha Setu");
            }
        }
    }

    private void playWelcomeMusic() {
        try {
            int resId = getResources().getIdentifier("welcome_music", "raw", getPackageName());
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId);
                mediaPlayer.setLooping(false);
                mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRegistration != null) {
            userRegistration.remove();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void setupCategoryCard(int id, String category, MaterialButtonToggleGroup toggleGroup) {
        findViewById(id).setOnClickListener(v -> {
            boolean isHindi = toggleGroup.getCheckedButtonId() == R.id.btnHindi;
            
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                
                dbHelper.setFavoriteCategory(category);
                
                Intent intent;
                if (category.equals("Sentences")) {
                    intent = new Intent(MainActivity.this, SentenceActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, PronunciationActivity.class);
                }

                intent.putExtra("CATEGORY", category);
                intent.putExtra("IS_HINDI", isHindi);
                intent.putExtra("STANDARD", userStandard);
                startActivity(intent);
            }).start();
        });
    }
}
