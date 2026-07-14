package com.example.pbl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
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
    private TextInputLayout tilClassFilter;
    private AutoCompleteTextView actvClassFilter;
    private TextView tvNoRecords;
    private LinearLayout llHistoryContainer;
    private TextView tvFavCategory, tvWeakWords, tvWeakestCategory, tvBestScore, tvLastPractice;
    private TextView tvTotalWords, tvTotalSentences, tvAvgAccuracy, tvTotalXP;
    private TextView tvTotalWordsLabel, tvTotalSentencesLabel, tvTotalXPLabel, tvAvgAccuracyLabel;
    private SwipeRefreshLayout swipeRefresh;
    
    private DBHelper dbHelper;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<HashMap<String, String>> displayData;
    private ArrayList<HashMap<String, String>> fullData = new ArrayList<>();
    private ProgressAdapter adapter;
    private final HashMap<String, User> userCache = new HashMap<>();
    private final java.util.Set<String> resolvingUids = new java.util.HashSet<>();

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
        tvTotalWordsLabel = findViewById(R.id.tvTotalWordsLabel);
        tvTotalSentencesLabel = findViewById(R.id.tvTotalSentencesLabel);
        tvAvgAccuracyLabel = findViewById(R.id.tvAvgAccuracyLabel);
        tvTotalXPLabel = findViewById(R.id.tvTotalXPLabel);
        
        tvFavCategory = findViewById(R.id.tvFavCategory);
        tvWeakestCategory = findViewById(R.id.tvWeakestCategory);
        tvBestScore = findViewById(R.id.tvBestScore);
        tvLastPractice = findViewById(R.id.tvLastPractice);
        
        swipeRefresh = findViewById(R.id.swipeRefresh);
        llHistoryContainer = findViewById(R.id.llHistoryContainer);
        rvProgress = findViewById(R.id.rvProgress);
        tilClassFilter = findViewById(R.id.tilClassFilter);
        actvClassFilter = findViewById(R.id.actvClassFilter);
        tvNoRecords = findViewById(R.id.tvNoRecords);
        tvWeakWords = findViewById(R.id.tvWeakWords);
        Button btnClearProgress = findViewById(R.id.btnClearProgress);
        
        if (isTeacherView) {
            findViewById(R.id.layoutStudentStats).setVisibility(View.VISIBLE);
            tvTotalWordsLabel.setText("Total Records");
            tvTotalSentencesLabel.setText("Active Users");
            tvAvgAccuracyLabel.setText("Avg Score");
            tvTotalXPLabel.setText("Class Health");

            findViewById(R.id.cardInsights).setVisibility(View.GONE);
            tvWeakWords.setVisibility(View.GONE);
            btnClearProgress.setVisibility(View.GONE);
            rvProgress.setVisibility(View.VISIBLE);
            tilClassFilter.setVisibility(View.VISIBLE);

            setupClassFilter();
            
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

    private void setupClassFilter() {
        String[] classes = {"All Classes", "Class 1", "Class 2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classes);
        actvClassFilter.setAdapter(adapter);
        actvClassFilter.setText(classes[0], false);
        actvClassFilter.setOnItemClickListener((parent, view, position, id) -> {
            applyFilter(classes[position]);
        });
        Log.d(TAG, "setupClassFilter: Dropdown options set to All Classes, Class 1, Class 2");
    }

    private void applyFilter(String selection) {
        Log.d(TAG, "FILTER_DEBUG: Selected class filter: " + selection);
        displayData.clear();

        // Robust normalization: extract only digits (e.g., "Class 1" -> "1", "1" -> "1")
        String targetNorm = selection.replaceAll("[^0-9]", "");
        Log.d(TAG, "FILTER_DEBUG: Target normalized: " + targetNorm);

        for (HashMap<String, String> item : fullData) {
            String itemStandard = item.get("standard");
            String itemNorm = (itemStandard != null) ? itemStandard.replaceAll("[^0-9]", "") : "";
            
            boolean match;
            if ("All Classes".equals(selection)) {
                match = true;
            } else {
                // Handle various formats: "Class 1", "Class1", "1", "1st", "standard 1" etc.
                match = !targetNorm.isEmpty() && targetNorm.equals(itemNorm);
            }

            if (match) {
                displayData.add(item);
            }
            
            // Log details for every record to debug mismatches
            Log.d(TAG, "FILTER_DEBUG: Item [Student: " + item.get("student") + 
                    ", UID: " + item.get("uid") + 
                    ", StdOrig: \"" + itemStandard + "\"" +
                    ", StdNorm: \"" + itemNorm + "\"" +
                    ", TargetNorm: \"" + targetNorm + "\"" +
                    ", Match: " + match + "]");
        }

        Log.d(TAG, "FILTER_DEBUG: Total records in fullData: " + fullData.size());
        Log.d(TAG, "FILTER_DEBUG: Total records matching filter: " + displayData.size());
        Log.d(TAG, "FILTER_DEBUG: Final RecyclerView adapter count: " + displayData.size());

        if (isTeacherView) {
            updateTeacherStats();
        }

        if (displayData.isEmpty()) {
            tvNoRecords.setVisibility(View.VISIBLE);
            rvProgress.setVisibility(View.GONE);
        } else {
            tvNoRecords.setVisibility(View.GONE);
            rvProgress.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void loadAllStudentsProgress() {
        swipeRefresh.setRefreshing(true);
        Log.d(TAG, "loadAllStudentsProgress: Fetching from Firestore...");
        db.collection("progress")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullData.clear();
                    Log.d(TAG, "Successfully fetched " + queryDocumentSnapshots.size() + " progress records");
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String uid = doc.getString("uid");
                        String name = doc.getString("name");
                        
                        // Robust field checking for student's class
                        Object stdObj = doc.get("standard");
                        if (stdObj == null) stdObj = doc.get("class");
                        if (stdObj == null) stdObj = doc.get("grade");
                        if (stdObj == null) stdObj = doc.get("std");
                        if (stdObj == null) stdObj = doc.get("docStandard");
                        String standard = (stdObj != null) ? String.valueOf(stdObj) : "";
                        
                        HashMap<String, String> map = new HashMap<>();
                        map.put("uid", uid);
                        map.put("word", doc.getString("word"));
                        map.put("score", "Score: " + doc.get("score"));
                        map.put("raw_score", String.valueOf(doc.get("score")));
                        map.put("category", "Category: " + doc.getString("category"));
                        map.put("standard", standard);
                        
                        Log.d(TAG, "loadAllStudentsProgress: Loaded record [Word: " + doc.getString("word") + ", UID: " + uid + ", Standard: \"" + standard + "\"]");

                        if (name != null && !standard.isEmpty()) {
                            String studentInfo = "Student: " + name + " (" + standard + ")";
                            map.put("student", studentInfo);
                            // Update cache if missing
                            if (uid != null && !userCache.containsKey(uid)) {
                                User u = new User();
                                u.setUid(uid);
                                u.setName(name);
                                u.setStandard(standard);
                                userCache.put(uid, u);
                            }
                        } else if (uid != null && userCache.containsKey(uid)) {
                            User u = userCache.get(uid);
                            String info = "Student: " + u.getName() + (u.getStandard() != null ? " (" + u.getStandard() + ")" : "");
                            map.put("student", info);
                            map.put("standard", u.getStandard());
                        } else {
                            map.put("student", name != null ? "Student: " + name : "Student: Loading...");
                            resolveStudentInfo(uid, map);
                        }
                        fullData.add(map);
                    }
                    applyFilter(actvClassFilter.getText().toString());
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    Log.e(TAG, "Error loading progress", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resolveStudentInfo(String uid, HashMap<String, String> map) {
        if (uid == null || uid.isEmpty()) {
            map.put("student", "Unknown Student");
            return;
        }

        // Cache hit (already handled in loop, but here for async consistency)
        if (userCache.containsKey(uid)) {
            User u = userCache.get(uid);
            if (u != null) {
                String info = "Student: " + u.getName() + (u.getStandard() != null ? " (" + u.getStandard() + ")" : "");
                map.put("student", info);
                map.put("standard", u.getStandard());
            }
            return;
        }

        if (resolvingUids.contains(uid)) return;
        resolvingUids.add(uid);

        Log.d(TAG, "resolveStudentInfo: Resolving UID=" + uid);

        // Try local DB cache
        User cachedUser = dbHelper.getUser(uid);
        if (cachedUser != null && cachedUser.getName() != null) {
            Log.d(TAG, "Found in local cache: " + cachedUser.getName());
            userCache.put(uid, cachedUser);
            updateRecordsWithResolvedInfo(uid, cachedUser);
            resolvingUids.remove(uid);
            return;
        }

        // Fetch from Firestore with manual field extraction for reliability
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            resolvingUids.remove(uid);
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                Object stdVal = documentSnapshot.get("standard");
                if (stdVal == null) stdVal = documentSnapshot.get("class");
                if (stdVal == null) stdVal = documentSnapshot.get("grade");
                if (stdVal == null) stdVal = documentSnapshot.get("std");
                if (stdVal == null) stdVal = documentSnapshot.get("docStandard");
                String standard = (stdVal != null) ? String.valueOf(stdVal) : "";

                User newUser = new User();
                newUser.setUid(uid);
                newUser.setName(name != null ? name : "Student");
                newUser.setStandard(standard);
                newUser.setRole("student"); // Assuming student if in progress collection

                Log.d(TAG, "resolveStudentInfo: Resolved from Firestore [UID: " + uid + ", Name: " + newUser.getName() + ", Std: \"" + standard + "\"]");
                userCache.put(uid, newUser);
                dbHelper.saveUser(newUser); // Save for offline cache
                updateRecordsWithResolvedInfo(uid, newUser);
            } else {
                Log.w(TAG, "resolveStudentInfo: No user document found for UID: " + uid);
            }
        }).addOnFailureListener(e -> {
            resolvingUids.remove(uid);
            Log.e(TAG, "resolveStudentInfo: Error fetching user " + uid, e);
        });
    }

    private void updateRecordsWithResolvedInfo(String uid, User user) {
        String info = "Student: " + user.getName() + (!user.getStandard().isEmpty() ? " (" + user.getStandard() + ")" : "");
        Log.d(TAG, "updateRecordsWithResolvedInfo: Updating all records for UID: " + uid + " with Standard: " + user.getStandard());
        
        for (HashMap<String, String> item : fullData) {
            if (uid.equals(item.get("uid"))) {
                item.put("student", info);
                item.put("standard", user.getStandard());
            }
        }
        
        runOnUiThread(() -> {
            applyFilter(actvClassFilter.getText().toString());
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

    private void updateTeacherStats() {
        int totalRecords = displayData.size();
        Log.d(TAG, "updateTeacherStats: Records count=" + totalRecords);
        tvTotalWords.setText(String.valueOf(totalRecords));

        java.util.HashSet<String> uniqueUsers = new java.util.HashSet<>();
        java.util.HashMap<String, List<Integer>> categoryScores = new java.util.HashMap<>();
        double totalScore = 0;
        
        for (HashMap<String, String> item : displayData) {
            String uid = item.get("uid");
            if (uid != null) uniqueUsers.add(uid);
            
            String categoryRaw = item.get("category");
            String category = (categoryRaw != null) ? categoryRaw.replace("Category: ", "") : "Unknown";

            String rawScoreStr = item.get("raw_score");
            int score = 0;
            if (rawScoreStr != null) {
                try {
                    // Extract numeric score from strings like "Score: 85" or "85"
                    score = Integer.parseInt(rawScoreStr.replaceAll("[^0-9]", ""));
                    totalScore += score;
                } catch (Exception ignored) {}
            }
            
            if (!category.isEmpty()) {
                if (!categoryScores.containsKey(category)) {
                    categoryScores.put(category, new ArrayList<>());
                }
                categoryScores.get(category).add(score);
            }
        }
        
        tvTotalSentences.setText(String.valueOf(uniqueUsers.size()));
        int avg = totalRecords > 0 ? (int) (totalScore / totalRecords) : 0;
        tvAvgAccuracy.setText(avg + "%");
        
        String health;
        if (avg >= 80) health = "Excellent";
        else if (avg >= 60) health = "Good";
        else if (avg >= 40) health = "Fair";
        else if (totalRecords == 0) health = "N/A";
        else health = "Critical";
        tvTotalXP.setText(health);

        Log.d(TAG, "Stats updated: Records=" + totalRecords + ", ActiveUsers=" + uniqueUsers.size() + ", AvgScore=" + avg + ", Health=" + health);

        // Update Teacher Insights based on filtered data
        if (totalRecords > 0) {
            findViewById(R.id.cardInsights).setVisibility(View.VISIBLE);
            
            String hardestCat = "N/A";
            double minAvg = 101;
            String bestCat = "N/A";
            double maxAvg = -1;

            for (java.util.Map.Entry<String, List<Integer>> entry : categoryScores.entrySet()) {
                double catAvg = 0;
                for (int s : entry.getValue()) catAvg += s;
                catAvg /= entry.getValue().size();

                if (catAvg < minAvg) {
                    minAvg = catAvg;
                    hardestCat = entry.getKey();
                }
                if (catAvg > maxAvg) {
                    maxAvg = catAvg;
                    bestCat = entry.getKey();
                }
            }

            tvFavCategory.setText("Strongest Area: " + bestCat);
            tvWeakestCategory.setText("Hardest Area: " + hardestCat);
            tvBestScore.setText("Filter: " + actvClassFilter.getText().toString());
            
            if (!displayData.isEmpty()) {
                String latestWord = displayData.get(0).get("word");
                tvLastPractice.setText("Latest Activity: " + (latestWord != null ? latestWord : "N/A"));
            }
        } else {
            findViewById(R.id.cardInsights).setVisibility(View.GONE);
        }
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
