package com.example.pbl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager instance;
    private final Context context;
    private final DBHelper dbHelper;
    private final FirebaseFirestore db;
    private boolean isSyncing = false;

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new DBHelper(this.context);
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void syncProgress() {
        if (!isOnline() || isSyncing) return;

        ArrayList<HashMap<String, Object>> queue = dbHelper.getSyncQueue();
        if (queue.isEmpty()) return;

        isSyncing = true;
        Log.d(TAG, "Starting sync for " + queue.size() + " items");

        WriteBatch batch = db.batch();
        List<Integer> idsToRemove = new ArrayList<>();

        for (HashMap<String, Object> item : queue) {
            Map<String, Object> progress = new HashMap<>();
            progress.put("uid", item.get("uid"));
            progress.put("name", item.get("name"));
            progress.put("word", item.get("word"));
            progress.put("score", item.get("score"));
            progress.put("category", item.get("category"));
            progress.put("standard", item.get("standard"));
            progress.put("timestamp", item.get("timestamp"));

            batch.set(db.collection("progress").document(), progress);
            idsToRemove.add((Integer) item.get("id"));
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            for (int id : idsToRemove) {
                dbHelper.removeFromSyncQueue(id);
            }
            isSyncing = false;
            Log.d(TAG, "Sync successful");
            
            // After syncing progress, sync user stats and settings
            syncUserStatsAndSettings();
        }).addOnFailureListener(e -> {
            isSyncing = false;
            Log.e(TAG, "Sync failed", e);
        });
    }

    private void syncUserStatsAndSettings() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        User localUser = dbHelper.getUser(uid);
        if (localUser == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User remoteUser = doc.toObject(User.class);
                if (remoteUser != null) {
                    // Strategy: Merge logic
                    // If local has a newer sync time, push local to remote
                    // Otherwise, pull remote to local
                    if (localUser.getLastSyncTime() > remoteUser.getLastSyncTime()) {
                        localUser.setLastSyncTime(System.currentTimeMillis());
                        db.collection("users").document(uid).set(localUser);
                        dbHelper.saveUser(localUser);
                    } else {
                        remoteUser.setUid(uid);
                        dbHelper.saveUser(remoteUser);
                    }
                }
            } else {
                // First time sync for new user
                localUser.setLastSyncTime(System.currentTimeMillis());
                db.collection("users").document(uid).set(localUser);
            }
        });
    }

    public void downloadContent() {
        if (!isOnline()) return;

        // Sync Words
        db.collection("words").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Word word = doc.toObject(Word.class);
                word.setId(doc.getId());
                dbHelper.saveContent(word, "Word");
            }
        });

        // Sync Sentences
        db.collection("sentences").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Word word = doc.toObject(Word.class);
                word.setId(doc.getId());
                dbHelper.saveContent(word, "Sentence");
            }
        });
    }
}
