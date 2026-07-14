package com.example.pbl;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DBHelper dbHelper;
    private SharedPreferences prefs;

    private ImageView ivProfilePhoto;
    private TextView tvUserName, tvUserEmail, tvRoleSpecificInfo, tvStorageUsage;
    private LinearLayout containerRoleSettings;
    private MaterialButtonToggleGroup toggleTheme;
    private Slider sliderFontSize;
    private TextView tvAppVersion;
    private MaterialButton btnSyncNow, btnResetDefaults, btnClearCache;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new DBHelper(this);
        prefs = getSharedPreferences("BhashaSetuPrefs", Context.MODE_PRIVATE);

        initCommonViews();
        loadUserData();
        calculateStorageUsage();
    }

    private void initCommonViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvRoleSpecificInfo = findViewById(R.id.tvRoleSpecificInfo);
        tvStorageUsage = findViewById(R.id.tvStorageUsage);
        containerRoleSettings = findViewById(R.id.containerRoleSettings);
        toggleTheme = findViewById(R.id.toggleTheme);
        sliderFontSize = findViewById(R.id.sliderFontSize);
        tvAppVersion = findViewById(R.id.tvAppVersion);
        btnSyncNow = findViewById(R.id.btnSyncNow);
        btnResetDefaults = findViewById(R.id.btnResetDefaults);
        btnClearCache = findViewById(R.id.btnClearCache);

        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
        findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> confirmDeleteAccount());
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());
        
        btnSyncNow.setOnClickListener(v -> syncAllData());
        btnResetDefaults.setOnClickListener(v -> confirmResetDefaults());
        btnClearCache.setOnClickListener(v -> clearAppCache());

        setupThemeToggle();
        setupFontSizeSlider();
        
        updateAboutSection();
    }

    private void updateAboutSection() {
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            String androidVersion = android.os.Build.VERSION.RELEASE;
            String lastSync = "Never";
            if (currentUser != null && currentUser.getLastSyncTime() > 0) {
                lastSync = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(currentUser.getLastSyncTime()));
            }
            
            tvAppVersion.setText(String.format("App Version: %s\nAndroid Version: %s\nLast Sync: %s", 
                    versionName, androidVersion, lastSync));
        } catch (Exception e) {
            tvAppVersion.setText("App Version: 1.0.0");
        }
    }

    private void loadUserData() {
        String uid = mAuth.getUid();
        if (uid == null) {
            finish();
            return;
        }

        // Try local first for offline support
        currentUser = dbHelper.getUser(uid);
        if (currentUser != null) {
            updateUIWithUser(currentUser);
            loadPrefsFromUser(currentUser);
        }

        // Then sync from Firestore if online
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User remoteUser = documentSnapshot.toObject(User.class);
                if (remoteUser != null) {
                    dbHelper.saveUser(remoteUser);
                    currentUser = remoteUser;
                    updateUIWithUser(currentUser);
                    loadPrefsFromUser(currentUser);
                    updateAboutSection();
                }
            }
        });
    }

    private void loadPrefsFromUser(User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("speech_speed", user.getSpeechSpeed());
        editor.putBoolean("auto_pronounce", user.isAutoPronounce());
        editor.putString("learning_lang", user.getLearningLang());
        editor.putInt("theme_mode", user.getThemeMode());
        editor.putFloat("font_size", user.getFontSize());
        editor.putBoolean("enable_leaderboard", user.isEnableLeaderboard());
        editor.putBoolean("enable_daily_goals", user.isEnableDailyGoals());
        editor.apply();
        
        // Refresh UI components
        sliderFontSize.setValue(user.getFontSize());
        // Theme is handled in setupThemeToggle or by activity restart
    }

    private void syncSettingsToFirestore() {
        if (currentUser == null || mAuth.getUid() == null) return;

        currentUser.setSpeechSpeed(prefs.getFloat("speech_speed", 1.0f));
        currentUser.setAutoPronounce(prefs.getBoolean("auto_pronounce", true));
        currentUser.setLearningLang(prefs.getString("learning_lang", "English"));
        currentUser.setThemeMode(prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
        currentUser.setFontSize(prefs.getFloat("font_size", 16.0f));
        currentUser.setEnableLeaderboard(prefs.getBoolean("enable_leaderboard", true));
        currentUser.setEnableDailyGoals(prefs.getBoolean("enable_daily_goals", true));
        currentUser.setLastSyncTime(System.currentTimeMillis());

        // Save locally
        dbHelper.saveUser(currentUser);

        // Save to Firestore
        db.collection("users").document(mAuth.getUid()).set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    updateAboutSection();
                })
                .addOnFailureListener(e -> {
                    // Fail silently, will sync later
                    dbHelper.addToSyncQueue(mAuth.getUid(), currentUser.getName(), currentUser.getStandard(), "settings_update", 0, "update_settings");
                });
    }

    private void updateUIWithUser(User user) {
        tvUserName.setText(user.getName());
        tvUserEmail.setText(user.getEmail());
        
        if ("teacher".equals(user.getRole())) {
            tvRoleSpecificInfo.setText("Role: Teacher");
            setupTeacherSettings();
        } else {
            tvRoleSpecificInfo.setText("Standard: " + (user.getStandard() != null ? user.getStandard() : "1"));
            setupStudentSettings();
        }
    }

    private void setupStudentSettings() {
        containerRoleSettings.removeAllViews();
        View studentView = LayoutInflater.from(this).inflate(R.layout.layout_settings_student, containerRoleSettings, false);
        containerRoleSettings.addView(studentView);

        MaterialButton btnStudentLang = studentView.findViewById(R.id.btnStudentLang);
        Slider sliderSpeechSpeed = studentView.findViewById(R.id.sliderSpeechSpeed);
        MaterialSwitch switchAutoPronounce = studentView.findViewById(R.id.switchAutoPronounce);
        MaterialButton btnDownloadLessons = studentView.findViewById(R.id.btnDownloadLessons);
        MaterialButton btnSyncProgress = studentView.findViewById(R.id.btnSyncProgress);
        MaterialButton btnClearLessons = studentView.findViewById(R.id.btnClearLessons);

        // Load saved values
        btnStudentLang.setText(prefs.getString("learning_lang", "English"));
        sliderSpeechSpeed.setValue(prefs.getFloat("speech_speed", 1.0f));
        switchAutoPronounce.setChecked(prefs.getBoolean("auto_pronounce", true));

        // Set listeners
        btnStudentLang.setOnClickListener(v -> showLanguageDialog(btnStudentLang));
        sliderSpeechSpeed.addOnChangeListener((slider, value, fromUser) -> {
            prefs.edit().putFloat("speech_speed", value).apply();
            syncSettingsToFirestore();
        });
        switchAutoPronounce.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_pronounce", isChecked).apply();
            syncSettingsToFirestore();
        });
        
        btnDownloadLessons.setOnClickListener(v -> {
            Toast.makeText(this, "Downloading lessons for offline use...", Toast.LENGTH_SHORT).show();
            SyncManager.getInstance(this).downloadContent();
        });

        btnSyncProgress.setOnClickListener(v -> {
            SyncManager.getInstance(this).syncProgress();
            Toast.makeText(this, "Syncing progress...", Toast.LENGTH_SHORT).show();
            syncSettingsToFirestore();
        });

        btnClearLessons.setOnClickListener(v -> confirmClearLessons());
    }

    private void setupTeacherSettings() {
        containerRoleSettings.removeAllViews();
        View teacherView = LayoutInflater.from(this).inflate(R.layout.layout_settings_teacher, containerRoleSettings, false);
        containerRoleSettings.addView(teacherView);

        MaterialSwitch switchLeaderboard = teacherView.findViewById(R.id.switchLeaderboard);
        MaterialSwitch switchDailyGoals = teacherView.findViewById(R.id.switchTeacherDailyGoals);
        MaterialButton btnBackup = teacherView.findViewById(R.id.btnBackupContent);
        MaterialButton btnRestore = teacherView.findViewById(R.id.btnRestoreContent);

        // Load saved values
        switchLeaderboard.setChecked(prefs.getBoolean("enable_leaderboard", true));
        switchDailyGoals.setChecked(prefs.getBoolean("enable_daily_goals", true));

        // Set listeners
        switchLeaderboard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("enable_leaderboard", isChecked).apply();
            syncSettingsToFirestore();
        });

        switchDailyGoals.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("enable_daily_goals", isChecked).apply();
            syncSettingsToFirestore();
        });
        
        btnBackup.setOnClickListener(v -> Toast.makeText(this, "Backing up content to cloud...", Toast.LENGTH_SHORT).show());
        btnRestore.setOnClickListener(v -> Toast.makeText(this, "Restoring content from backup...", Toast.LENGTH_SHORT).show());
    }

    private void setupThemeToggle() {
        int currentTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) toggleTheme.check(R.id.btnThemeLight);
        else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) toggleTheme.check(R.id.btnThemeDark);
        else toggleTheme.check(R.id.btnThemeSystem);

        toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int mode;
                if (checkedId == R.id.btnThemeLight) mode = AppCompatDelegate.MODE_NIGHT_NO;
                else if (checkedId == R.id.btnThemeDark) mode = AppCompatDelegate.MODE_NIGHT_YES;
                else mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

                prefs.edit().putInt("theme_mode", mode).apply();
                syncSettingsToFirestore();
                AppCompatDelegate.setDefaultNightMode(mode);
            }
        });
    }

    private void setupFontSizeSlider() {
        float fontSize = prefs.getFloat("font_size", 16.0f);
        sliderFontSize.setValue(fontSize);
        sliderFontSize.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                prefs.edit().putFloat("font_size", value).apply();
                syncSettingsToFirestore();
            }
        });
    }

    private void syncAllData() {
        Toast.makeText(this, "Manual sync started...", Toast.LENGTH_SHORT).show();
        SyncManager.getInstance(this).syncProgress();
        SyncManager.getInstance(this).downloadContent();
        syncSettingsToFirestore();
    }

    private void confirmResetDefaults() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Settings")
                .setMessage("Are you sure you want to reset all settings to default values?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    // Re-apply essential app defaults if any
                    syncSettingsToFirestore();
                    recreate();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void calculateStorageUsage() {
        long internalSize = getDirSize(getCacheDir()) + getDirSize(getFilesDir());
        String usageText = String.format(Locale.getDefault(), "Total Usage: %.2f MB\nIncludes lessons, images, and app cache.", 
                internalSize / (1024.0 * 1024.0));
        tvStorageUsage.setText(usageText);
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) size += file.length();
                else size += getDirSize(file);
            }
        } else if (dir != null && dir.isFile()) {
            size += dir.length();
        }
        return size;
    }

    private void clearAppCache() {
        try {
            deleteDir(getCacheDir());
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
            calculateStorageUsage();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) return false;
            }
        }
        return dir.delete();
    }

    private void confirmClearLessons() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Lessons")
                .setMessage("This will delete all offline content. You will need internet to access lessons.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    // Implementation depends on how DBHelper stores content
                    // For now, assume clearing content table
                    SQLiteDatabase sdb = dbHelper.getWritableDatabase();
                    sdb.delete("content", null, null);
                    Toast.makeText(this, "Offline lessons cleared", Toast.LENGTH_SHORT).show();
                    calculateStorageUsage();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLanguageDialog(MaterialButton btn) {
        String[] langs = {"English", "Hindi", "Kannada"};
        new AlertDialog.Builder(this)
                .setTitle("Select Learning Language")
                .setItems(langs, (dialog, which) -> {
                    String selected = langs[which];
                    btn.setText(selected);
                    prefs.edit().putString("learning_lang", selected).apply();
                    syncSettingsToFirestore();
                })
                .show();
    }

    private void showChangePasswordDialog() {
        if (currentUser == null) return;
        Toast.makeText(this, "Password reset email sent to " + currentUser.getEmail(), Toast.LENGTH_LONG).show();
        mAuth.sendPasswordResetEmail(currentUser.getEmail());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void confirmDeleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("For security, please enter your password to confirm account deletion.");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Delete Permanently", (dialog, which) -> {
            String password = input.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
                return;
            }
            reauthenticateAndDelete(password);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void reauthenticateAndDelete(String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleteUserDataAndAccount(user);
            } else {
                Toast.makeText(SettingsActivity.this, "Authentication failed. Incorrect password.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserDataAndAccount(FirebaseUser user) {
        String uid = user.getUid();
        // 1. Delete from Firestore
        db.collection("users").document(uid).delete()
                .addOnSuccessListener(aVoid -> {
                    // 2. Delete from Firebase Auth
                    user.delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(SettingsActivity.this, "Account Deleted Successfully", Toast.LENGTH_SHORT).show();
                                logout();
                            })
                            .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Failed to delete account: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

