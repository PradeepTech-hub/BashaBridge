package com.example.pbl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Check local DB first
        User user = new DBHelper(this).getUser(uid);
        if (user != null && "teacher".equalsIgnoreCase(user.getRole())) {
            initUI();
        } else {
            // Fallback to Firestore to ensure we have the latest role
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User remoteUser = documentSnapshot.toObject(User.class);
                        if (remoteUser != null) {
                            remoteUser.setUid(uid);
                            if ("teacher".equalsIgnoreCase(remoteUser.getRole())) {
                                new DBHelper(this).saveUser(remoteUser);
                                initUI();
                                return;
                            }
                        }
                        
                        String roleStr = (remoteUser != null) ? remoteUser.getRole() : "none";
                        Toast.makeText(this, "Access Denied: Teacher role required. (Found: " + roleStr + ")", Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Access Denied: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void initUI() {
        setContentView(R.layout.activity_teacher_dashboard);
        
        // Ensure layouts are initialized before setting listeners
        View cardWords = findViewById(R.id.cardManageWords);
        if (cardWords != null) {
            cardWords.setClickable(true);
            cardWords.setFocusable(true);
            cardWords.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherDashboardActivity.this, ManageContentActivity.class);
                intent.putExtra("TYPE", "Words");
                startActivity(intent);
            });
        }

        View cardSentences = findViewById(R.id.cardManageSentences);
        if (cardSentences != null) {
            cardSentences.setClickable(true);
            cardSentences.setFocusable(true);
            cardSentences.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherDashboardActivity.this, ManageContentActivity.class);
                intent.putExtra("TYPE", "Sentences");
                startActivity(intent);
            });
        }

        View cardProgress = findViewById(R.id.cardStudentProgress);
        if (cardProgress != null) {
            cardProgress.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherDashboardActivity.this, ProgressActivity.class);
                intent.putExtra("IS_TEACHER_VIEW", true);
                startActivity(intent);
            });
        }

        View cardStats = findViewById(R.id.cardClassStats);
        if (cardStats != null) {
            cardStats.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherDashboardActivity.this, ClassStatsActivity.class);
                startActivity(intent);
            });
        }

        View btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                startActivity(new Intent(TeacherDashboardActivity.this, SettingsActivity.class));
            });
        }

        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(TeacherDashboardActivity.this, LoginActivity.class));
                finish();
            });
        }
    }
}
