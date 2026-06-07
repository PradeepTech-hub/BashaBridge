package com.example.pbl;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private static final String TAG = "ProgressActivity";
    private ListView lvProgress;
    private TextView tvFavCategory, tvWeakWords;
    private DBHelper dbHelper;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<HashMap<String, String>> displayData;
    private SimpleAdapter adapter;
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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (isTeacherView) {
            toolbar.setTitle("Class Progress");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        lvProgress = findViewById(R.id.lvProgress);
        tvFavCategory = findViewById(R.id.tvFavCategory);
        tvWeakWords = findViewById(R.id.tvWeakWords);
        Button btnClearProgress = findViewById(R.id.btnClearProgress);
        
        if (isTeacherView) {
            tvFavCategory.setVisibility(View.GONE);
            tvWeakWords.setVisibility(View.GONE);
            btnClearProgress.setVisibility(View.GONE);
            
            displayData = new ArrayList<>();
            String[] from = {"word", "score", "category", "student"};
            int[] to = {R.id.tvProgressWord, R.id.tvProgressScore, R.id.tvProgressCategory, R.id.tvStudentName};
            
            adapter = new SimpleAdapter(this, displayData, R.layout.progress_item, from, to);
            adapter.setViewBinder((view, data, textRepresentation) -> {
                if (view.getId() == R.id.tvStudentName) {
                    view.setVisibility(View.VISIBLE);
                    ((TextView) view).setText(textRepresentation);
                    return true;
                }
                return false;
            });
            lvProgress.setAdapter(adapter);
            
            loadAllStudentsProgress();
        } else {
            displayData = new ArrayList<>();
            String[] from = {"word", "score", "category"};
            int[] to = {R.id.tvProgressWord, R.id.tvProgressScore, R.id.tvProgressCategory};
            adapter = new SimpleAdapter(this, displayData, R.layout.progress_item, from, to);
            lvProgress.setAdapter(adapter);

            loadLocalProgress();
            loadFirestoreProgress();
        }

        btnClearProgress.setOnClickListener(v -> {
            dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DBHelper.TABLE_PROGRESS);
            displayData.clear();
            adapter.notifyDataSetChanged();
            loadLocalProgress();
            Toast.makeText(this, "Local progress cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadAllStudentsProgress() {
        db.collection("progress")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    displayData.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String uid = doc.getString("uid");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("word", doc.getString("word"));
                        map.put("score", "Score: " + doc.get("score"));
                        map.put("category", "Category: " + doc.getString("category"));
                        
                        // Default while loading or fallback
                        map.put("student", "Student: " + uid);
                        displayData.add(map);

                        resolveStudentInfo(uid, map);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
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
            adapter.notifyDataSetChanged();
            return;
        }

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String standard = documentSnapshot.getString("standard");
                String info = "Student: " + (name != null ? name : "Unknown") + " | Class: " + (standard != null ? standard : "N/A");
                userCache.put(uid, info);
                map.put("student", info);
            } else {
                map.put("student", "ID: " + uid);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void loadLocalProgress() {
        String fav = dbHelper.getFavoriteCategory();
        tvFavCategory.setText("Your Favorite Category: " + fav);

        List<String> weak = dbHelper.getWeakWords();
        if (!weak.isEmpty()) {
            tvWeakWords.setVisibility(View.VISIBLE);
            tvWeakWords.setText("Weak Words (Needs Practice): " + String.join(", ", weak));
        } else {
            tvWeakWords.setVisibility(View.GONE);
        }

        ArrayList<HashMap<String, String>> localData = dbHelper.getAllProgress();
        for (HashMap<String, String> item : localData) {
            HashMap<String, String> map = new HashMap<>();
            map.put("word", item.get("word"));
            map.put("score", "Score: " + item.get("score") + " (Local)");
            map.put("category", "Category: " + item.get("category"));
            displayData.add(map);
        }
        adapter.notifyDataSetChanged();
    }

    private void loadFirestoreProgress() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("progress")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("word", doc.getString("word"));
                        map.put("score", "Score: " + doc.get("score") + " (Cloud)");
                        map.put("category", "Category: " + doc.getString("category"));
                        displayData.add(0, map); // Add to top
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cloud progress", e);
                    Toast.makeText(this, "Couldn't load cloud progress", Toast.LENGTH_SHORT).show();
                });
    }
}
