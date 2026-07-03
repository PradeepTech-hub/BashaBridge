package com.example.pbl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class StandardSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard_selection);

        // Apply logo animation
        android.view.View logoContainer = findViewById(R.id.logoContainer);
        if (logoContainer != null) {
            android.view.animation.Animation fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.logo_fade_in);
            logoContainer.startAnimation(fadeIn);
        }

        findViewById(R.id.cardStandard1).setOnClickListener(v -> saveAndContinue(1));
        findViewById(R.id.cardStandard2).setOnClickListener(v -> saveAndContinue(2));
    }

    private void saveAndContinue(int standard) {
        SharedPreferences prefs = getSharedPreferences("BashaBridgePrefs", MODE_PRIVATE);
        prefs.edit().putInt("SELECTED_STANDARD", standard).apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
