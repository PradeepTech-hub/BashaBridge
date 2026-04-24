package com.example.pbl;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private ListView lvProgress;
    private TextView tvFavCategory, tvWeakWords;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        lvProgress = findViewById(R.id.lvProgress);
        tvFavCategory = findViewById(R.id.tvFavCategory);
        tvWeakWords = findViewById(R.id.tvWeakWords);
        Button btnClearProgress = findViewById(R.id.btnClearProgress);
        
        dbHelper = new DBHelper(this);

        loadProgress();

        btnClearProgress.setOnClickListener(v -> {
            dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DBHelper.TABLE_PROGRESS);
            loadProgress();
        });
    }

    private void loadProgress() {
        // Load favorite category
        String fav = dbHelper.getFavoriteCategory();
        tvFavCategory.setText("Your Favorite Category: " + fav);

        // Load history
        ArrayList<HashMap<String, String>> data = dbHelper.getAllProgress();
        
        // Load Weak Words
        List<String> weak = dbHelper.getWeakWords();
        if (!weak.isEmpty()) {
            tvWeakWords.setVisibility(View.VISIBLE);
            tvWeakWords.setText("Weak Words (Needs Practice): " + String.join(", ", weak));
        } else {
            tvWeakWords.setVisibility(View.GONE);
        }

        String[] from = {"word", "score", "category"};
        int[] to = {R.id.tvProgressWord, R.id.tvProgressScore, R.id.tvProgressCategory};

        ArrayList<HashMap<String, String>> displayData = new ArrayList<>();
        for (HashMap<String, String> item : data) {
            HashMap<String, String> map = new HashMap<>();
            map.put("word", item.get("word"));
            map.put("score", "Score: " + item.get("score"));
            map.put("category", "Category: " + item.get("category"));
            displayData.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, displayData,
                R.layout.progress_item, from, to);
        lvProgress.setAdapter(adapter);
    }
}
