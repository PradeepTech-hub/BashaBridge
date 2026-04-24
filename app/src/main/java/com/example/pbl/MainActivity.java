package com.example.pbl;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class MainActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleGroupAppLanguage;
    private DBHelper dbHelper;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            ((com.google.android.material.appbar.MaterialToolbar)findViewById(R.id.toolbar)).setTitle("ಭಾಷಾಬ್ರಿಡ್ಜ್");
        } else {
            tvWelcome.setText("Welcome back!");
            tvSub.setText("Select a category to start practicing");
            tvAppLangLabel.setText("App Interface Language");
            tvLangLabel.setText("Practice Language");
            tvCatLabel.setText("Categories");
            ((com.google.android.material.appbar.MaterialToolbar)findViewById(R.id.toolbar)).setTitle("BhashaBridge");
        }
    }

    private void playWelcomeMusic() {
        try {
            // Using a system sound or a professional simple melody if raw file not found
            // In a real app, the user would provide 'app_welcome.mp3' in res/raw
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void setupCategoryCard(int id, String category, MaterialButtonToggleGroup toggleGroup) {
        findViewById(id).setOnClickListener(v -> {
            boolean isHindi = toggleGroup.getCheckedButtonId() == R.id.btnHindi;
            
            // Animate scale
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                
                // Save as favorite category
                dbHelper.setFavoriteCategory(category);
                
                Intent intent;
                if (category.equals("Sentences")) {
                    intent = new Intent(MainActivity.this, SentenceActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, PronunciationActivity.class);
                }

                intent.putExtra("CATEGORY", category);
                intent.putExtra("IS_HINDI", isHindi);
                startActivity(intent);
            }).start();
        });
    }
}
