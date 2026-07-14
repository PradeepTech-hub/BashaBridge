package com.example.pbl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {

    private static final String TAG = "ProgressActivity";
    private RecyclerView rvProgress;
    private LinearLayout llHistoryContainer;
    private TextView tvFavCategory, tvWeakWords, tvWeakestCategory, tvBestScore, tvLastPractice;
    private TextView tvTotalWords, tvTotalSentences, tvAvgAccuracy, tvTotalXP;
    private SwipeRefreshLayout swipeRefresh;
    
    private DBHelper dbHelper;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<HashMap<String, String>> displayData;
    private ProgressAdapter adapter;
    private final HashMap<String, String> userCache = new HashMap<>();

    private boolean isTeacherView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DBHelper(this);
        
        isTeacherView = getIntent().getBooleanExtra("IS_TEACHER_VIEW", false);
        
        // --- Role-based Access Control ---
        String uid = mAuth.getUid();
        if (uid == null) {
            finish();
            return;
        }

        if (isTeacherView) {
            User currentUser = dbHelper.getUser(uid);
            if (currentUser != null && "teacher".equalsIgnoreCase(currentUser.getRole())) {
                initializeProgressView();
            } else {
                // Fallback to Firestore
                db.collection("users").document(uid).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            User remoteUser = documentSnapshot.toObject(User.class);
                            if (remoteUser != null && "teacher".equalsIgnoreCase(remoteUser.getRole())) {
                                remoteUser.setUid(uid);
                                dbHelper.saveUser(remoteUser);
                                initializeProgressView();
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
        } else {
            initializeProgressView();
        }
    }

    private void initializeProgressView() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (isTeacherView) {
            toolbar.setTitle("Class Progress");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Overview & Insights
        tvTotalWords = findViewById(R.id.tvTotalWords);
        tvTotalSentences = findViewById(R.id.tvTotalSentences);
        tvAvgAccuracy = findViewById(R.id.tvAvgAccuracy);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        
        tvFavCategory = findViewById(R.id.tvFavCategory);
        tvWeakestCategory = findViewById(R.id.tvWeakestCategory);
        tvBestScore = findViewById(R.id.tvBestScore);
        tvLastPractice = findViewById(R.id.tvLastPractice);
        
        swipeRefresh = findViewById(R.id.swipeRefresh);
        llHistoryContainer = findViewById(R.id.llHistoryContainer);
        rvProgress = findViewById(R.id.rvProgress);
        tvWeakWords = findViewById(R.id.tvWeakWords);
        Button btnClearProgress = findViewById(R.id.btnClearProgress);
        
        if (isTeacherView) {
            findViewById(R.id.layoutStudentStats).setVisibility(View.GONE);
            findViewById(R.id.cardInsights).setVisibility(View.GONE);
            tvWeakWords.setVisibility(View.GONE);
            btnClearProgress.setVisibility(View.GONE);
            rvProgress.setVisibility(View.VISIBLE);
            
            displayData = new ArrayList<>();
            rvProgress.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ProgressAdapter(displayData);
            rvProgress.setAdapter(adapter);
            
            loadAllStudentsProgress();
        } else {
            displayData = new ArrayList<>();
            refreshProgress();
        }

        swipeRefresh.setOnRefreshListener(this::refreshProgress);

        if (btnClearProgress != null) {
            btnClearProgress.setOnClickListener(v -> {
                dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DBHelper.TABLE_PROGRESS);
                refreshProgress();
                Toast.makeText(this, "Local progress cleared", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void refreshProgress() {
        if (isTeacherView) {
            loadAllStudentsProgress();
            return;
        }
        
        displayData.clear();
        llHistoryContainer.removeAllViews();
        
        loadLocalProgress();
        loadFirestoreProgress();
        loadUserXP();
        
        swipeRefresh.setRefreshing(false);
    }

    private void loadUserXP() {
        String uid = mAuth.getUid();
        if (uid == null) return;
        
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                long xp = doc.getLong("xp") != null ? doc.getLong("xp") : 0;
                tvTotalXP.setText(String.valueOf(xp));
            }
        });
    }

    private void loadAllStudentsProgress() {
        swipeRefresh.setRefreshing(true);
        db.collection("progress")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    displayData.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String uid = doc.getString("uid");
                        String name = doc.getString("name");
                        String standard = doc.getString("standard");
                        
                        HashMap<String, String> map = new HashMap<>();
                        map.put("word", doc.getString("word"));
                        map.put("score", "Score: " + doc.get("score"));
                        map.put("category", "Category: " + doc.getString("category"));
                        
                        if (name != null) {
                            String studentInfo = "Student: " + name + (standard != null ? " (" + standard + ")" : "");
                            map.put("student", studentInfo);
                            if (uid != null) userCache.put(uid, studentInfo);
                        } else {
                            map.put("student", "Student: Loading...");
                            resolveStudentInfo(uid, map);
                        }
                        displayData.add(map);
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resolveStudentInfo(String uid, HashMap<String, String> map) {
        if (uid == null) {
            map.put("student", "Unknown Student");
            return;
        }

        if (userCache.containsKey(uid)) {
            map.put("student", userCache.get(uid));
            runOnUiThread(() -> adapter.notifyDataSetChanged());
            return;
        }

        // Try local cache first
        User cachedUser = dbHelper.getUser(uid);
        if (cachedUser != null && cachedUser.getName() != null) {
            String info = "Student: " + cachedUser.getName() + (cachedUser.getStandard() != null ? " (" + cachedUser.getStandard() + ")" : "");
            userCache.put(uid, info);
            map.put("student", info);
            runOnUiThread(() -> adapter.notifyDataSetChanged());
            return;
        }

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String standard = documentSnapshot.getString("standard");
                String info = "Student: " + (name != null ? name : "Unknown") + (standard != null ? " (" + standard + ")" : "");
                userCache.put(uid, info);
                map.put("student", info);
                
                // Save to local cache for next time
                try {
                    User newUser = documentSnapshot.toObject(User.class);
                    if (newUser != null) {
                        newUser.setUid(uid);
                        dbHelper.saveUser(newUser);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping user document for " + uid, e);
                    // Fallback: manually create user object if toObject fails
                    User newUser = new User();
                    newUser.setUid(uid);
                    newUser.setName(name);
                    newUser.setStandard(standard);
                    newUser.setRole("student");
                    dbHelper.saveUser(newUser);
                }
            } else {
                map.put("student", "Student: User Not Found");
            }
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).addOnFailureListener(e -> {
            // If it's a permission error, we still can't get it from Firestore
            // But if we already tried local and it wasn't there, we show the error or UID
            map.put("student", "Student: " + uid); // Fallback to UID if name fetch fails
            runOnUiThread(() -> adapter.notifyDataSetChanged());
            Log.e(TAG, "Failed to resolve student info for " + uid, e);
        });
    }

    private void loadLocalProgress() {
        // Update Stats
        HashMap<String, Object> stats = dbHelper.getProgressStats();
        tvTotalWords.setText(String.valueOf(stats.get("totalWords")));
        tvTotalSentences.setText(String.valueOf(stats.get("totalSentences")));
        tvAvgAccuracy.setText(stats.get("averageAccuracy") + "%");
        tvBestScore.setText("Best Score: " + stats.get("bestScore") + "%");
        tvWeakestCategory.setText("Needs Work: " + stats.get("weakestCategory"));

        String fav = dbHelper.getFavoriteCategory();
        tvFavCategory.setText("Favorite: " + fav);

        List<String> weak = dbHelper.getWeakWords();
        if (!weak.isEmpty()) {
            tvWeakWords.setVisibility(View.VISIBLE);
            tvWeakWords.setText("Weak Words: " + String.join(", ", weak));
        } else {
            tvWeakWords.setVisibility(View.GONE);
        }

        ArrayList<HashMap<String, String>> localData = dbHelper.getAllProgress();
        for (HashMap<String, String> item : localData) {
            addHistoryItem(item.get("word"), item.get("score"), item.get("category"), "Local");
        }

        // Add Category Progress Bars
        addCategoryProgress();
    }

    private void addCategoryProgress() {
        ArrayList<HashMap<String, Object>> catProgress = dbHelper.getCategoryProgress();
        if (catProgress.isEmpty()) return;

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Category Progress");
        tvTitle.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleLarge);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 32, 0, 16);
        tvTitle.setLayoutParams(params);
        llHistoryContainer.addView(tvTitle, 0);

        for (int i = 0; i < catProgress.size(); i++) {
            HashMap<String, Object> item = catProgress.get(i);
            String category = (String) item.get("category");
            int score = (int) item.get("score");

            View card = LayoutInflater.from(this).inflate(R.layout.item_category_progress, llHistoryContainer, false);
            TextView tvCatName = card.findViewById(R.id.tvCategoryName);
            TextView tvCatScore = card.findViewById(R.id.tvCategoryScore);
            com.google.android.material.progressindicator.LinearProgressIndicator progress = card.findViewById(R.id.progressIndicator);

            tvCatName.setText(category);
            tvCatScore.setText(score + "%");
            progress.setProgress(score);
            
            llHistoryContainer.addView(card, i + 1);
        }
    }

    private void loadFirestoreProgress() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("progress")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean first = true;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (first) {
                            long ts = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0;
                            if (ts > 0) {
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                                tvLastPractice.setText("Last Session: " + sdf.format(new Date(ts)));
                            }
                            first = false;
                        }
                        addHistoryItem(doc.getString("word"), String.valueOf(doc.get("score")), doc.getString("category"), "Cloud");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cloud progress", e);
                });
    }

    private void addHistoryItem(String word, String score, String category, String source) {
        View view = LayoutInflater.from(this).inflate(R.layout.progress_item, llHistoryContainer, false);
        TextView tvWord = view.findViewById(R.id.tvProgressWord);
        TextView tvScore = view.findViewById(R.id.tvProgressScore);
        TextView tvCat = view.findViewById(R.id.tvProgressCategory);
        
        tvWord.setText(word);
        tvScore.setText("Score: " + score + "% (" + source + ")");
        tvCat.setText("Category: " + category);
        
        llHistoryContainer.addView(view);
    }

    private class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ViewHolder> {
        private final ArrayList<HashMap<String, String>> data;

        public ProgressAdapter(ArrayList<HashMap<String, String>> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HashMap<String, String> item = data.get(position);
            holder.tvWord.setText(item.get("word"));
            holder.tvScore.setText(item.get("score"));
            holder.tvCategory.setText(item.get("category"));
            
            String student = item.get("student");
            if (student != null) {
                holder.tvStudent.setVisibility(View.VISIBLE);
                holder.tvStudent.setText(student);
            } else {
                holder.tvStudent.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvWord, tvScore, tvCategory, tvStudent;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvWord = itemView.findViewById(R.id.tvProgressWord);
                tvScore = itemView.findViewById(R.id.tvProgressScore);
                tvCategory = itemView.findViewById(R.id.tvProgressCategory);
                tvStudent = itemView.findViewById(R.id.tvStudentName);
            }
        }
    }
}
