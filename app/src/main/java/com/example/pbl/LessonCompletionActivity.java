package com.example.pbl;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class LessonCompletionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_completion);

        int totalXP = getIntent().getIntExtra("TOTAL_XP", 0);
        int accuracy = getIntent().getIntExtra("ACCURACY", 0);
        int masteredCount = getIntent().getIntExtra("MASTERED_COUNT", 0);
        int totalCount = getIntent().getIntExtra("TOTAL_COUNT", 0);

        TextView tvTotalXP = findViewById(R.id.tvTotalXP);
        TextView tvAccuracy = findViewById(R.id.tvAccuracy);
        TextView tvMasteredCount = findViewById(R.id.tvMasteredCount);
        MaterialButton btnDone = findViewById(R.id.btnDone);

        tvTotalXP.setText(getString(R.string.xp_earned, totalXP));
        tvAccuracy.setText(getString(R.string.accuracy_format, accuracy));
        tvMasteredCount.setText(getString(R.string.mastery_format, masteredCount, totalCount));

        btnDone.setOnClickListener(v -> finish());
    }
}
