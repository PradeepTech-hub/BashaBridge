package com.example.pbl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClassStatsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvTotalStudents, tvClassAvgAccuracy, tvTotalAttempts, tvTotalClassXP;
    private TextView tvDifficultCategory, tvDifficultWords;
    private LinearLayout llRankingContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Role-based Access Control ---
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish();
            return;
        }

        try (DBHelper dbHelper = new DBHelper(this)) {
            User user = dbHelper.getUser(uid);
            if (user != null && "teacher".equalsIgnoreCase(user.getRole())) {
                initializeActivity();
            } else {
                // Fallback to Firestore
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            User remoteUser = documentSnapshot.toObject(User.class);
                            if (remoteUser != null && "teacher".equalsIgnoreCase(remoteUser.getRole())) {
                                remoteUser.setUid(uid);
                                try (DBHelper dbH = new DBHelper(this)) {
                                    dbH.saveUser(remoteUser);
                                }
                                initializeActivity();
                            } else {
                                Toast.makeText(this, "Access Denied: Teacher role required", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Access Denied: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }
        }
    }

    private void initializeActivity() {
        setContentView(R.layout.activity_class_stats);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvClassAvgAccuracy = findViewById(R.id.tvClassAvgAccuracy);
        tvTotalAttempts = findViewById(R.id.tvTotalAttempts);
        tvTotalClassXP = findViewById(R.id.tvTotalClassXP);
        tvDifficultCategory = findViewById(R.id.tvDifficultCategory);
        tvDifficultWords = findViewById(R.id.tvDifficultWords);
        llRankingContainer = findViewById(R.id.llRankingContainer);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(this::loadStats);
        loadStats();
    }

    private void loadStats() {
        swipeRefresh.setRefreshing(true);
        
        // 1. Get all students count and total XP
        db.collection("users").whereEqualTo("role", "student").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int studentCount = queryDocumentSnapshots.size();
                    tvTotalStudents.setText(String.valueOf(studentCount));
                    
                    long totalXP = 0;
                    List<User> students = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        students.add(user);
                        totalXP += user.getXp();
                    }
                    tvTotalClassXP.setText(String.valueOf(totalXP));
                    
                    // Populate Leaderboard
                    students.sort((u1, u2) -> Integer.compare(u2.getXp(), u1.getXp()));
                    updateLeaderboard(students);
                });

        // 2. Get aggregate progress stats
        db.collection("progress").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAttempts = queryDocumentSnapshots.size();
                    tvTotalAttempts.setText(String.valueOf(totalAttempts));

                    if (totalAttempts > 0) {
                        double totalScore = 0;
                        Map<String, List<Integer>> categoryScores = new HashMap<>();
                        Map<String, Integer> wordFailures = new HashMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Long scoreObj = doc.getLong("score");
                            int score = scoreObj != null ? scoreObj.intValue() : 0;
                            totalScore += score;

                            String category = doc.getString("category");
                            if (category != null) {
                                categoryScores.computeIfAbsent(category, k -> new ArrayList<>()).add(score);
                            }

                            if (score < 60) {
                                String word = doc.getString("word");
                                if (word != null) {
                                    wordFailures.put(word, wordFailures.getOrDefault(word, 0) + 1);
                                }
                            }
                        }

                        tvClassAvgAccuracy.setText(String.format(Locale.getDefault(), "%d%%", Math.round(totalScore / totalAttempts)));

                        // Calculate hardest category
                        String hardestCat = "None";
                        double lowestAvg = 100;
                        for (Map.Entry<String, List<Integer>> entry : categoryScores.entrySet()) {
                            double avg = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0);
                            if (avg < lowestAvg) {
                                lowestAvg = avg;
                                hardestCat = entry.getKey();
                            }
                        }
                        tvDifficultCategory.setText(getString(R.string.hardest_category_format, hardestCat));

                        // Calculate hardest words
                        List<Map.Entry<String, Integer>> failedWords = new ArrayList<>(wordFailures.entrySet());
                        failedWords.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
                        
                        StringBuilder sb = new StringBuilder("Common Mistakes: ");
                        for (int i = 0; i < Math.min(5, failedWords.size()); i++) {
                            sb.append(failedWords.get(i).getKey()).append(", ");
                        }
                        if (sb.length() > 17) sb.setLength(sb.length() - 2);
                        else sb.append("None yet");
                        
                        tvDifficultWords.setText(sb.toString());
                    }
                    
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLeaderboard(List<User> students) {
        llRankingContainer.removeAllViews();
        int rank = 1;
        for (User user : students) {
            if (rank > 10) break; // Top 10
            
            View itemView = LayoutInflater.from(this).inflate(R.layout.progress_item, llRankingContainer, false);
            TextView tvName = itemView.findViewById(R.id.tvProgressWord);
            TextView tvXP = itemView.findViewById(R.id.tvProgressScore);
            TextView tvStd = itemView.findViewById(R.id.tvProgressCategory);
            
            String nameText = rank + ". " + (user.getName() != null ? user.getName() : "Anonymous");
            tvName.setText(nameText);
            tvXP.setText(getString(R.string.xp_streak_format, user.getXp(), user.getStreak()));
            tvStd.setText(getString(R.string.class_format, user.getStandard() != null ? user.getStandard() : "N/A"));
            
            llRankingContainer.addView(itemView);
            rank++;
        }
    }
}
