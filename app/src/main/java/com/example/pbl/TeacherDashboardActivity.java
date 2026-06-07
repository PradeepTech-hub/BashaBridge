package com.example.pbl;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class TeacherDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        findViewById(R.id.cardManageWords).setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, ManageContentActivity.class);
            intent.putExtra("TYPE", "Words");
            startActivity(intent);
        });

        findViewById(R.id.cardManageSentences).setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, ManageContentActivity.class);
            intent.putExtra("TYPE", "Sentences");
            startActivity(intent);
        });

        findViewById(R.id.cardStudentProgress).setOnClickListener(v -> {
            // Intent to view student progress - assuming ProgressActivity can be used or a new one
            Intent intent = new Intent(TeacherDashboardActivity.this, ProgressActivity.class);
            intent.putExtra("IS_TEACHER_VIEW", true);
            startActivity(intent);
        });

        findViewById(R.id.cardClassStats).setOnClickListener(v -> {
            Toast.makeText(this, "Class statistics coming soon!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(TeacherDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }
}
