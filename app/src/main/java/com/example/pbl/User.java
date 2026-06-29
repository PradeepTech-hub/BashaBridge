package com.example.pbl;

public class User {
    private String uid;
    private String name;
    private String email;
    private String role; // "student" or "teacher"
    private String standard;
    private long createdAt;
    private int xp;
    private int streak;
    private long lastActive;
    private long lastSyncTime;
    private java.util.List<String> achievements;

    // App Settings
    private float speechSpeed = 1.0f;
    private boolean autoPronounce = true;
    private String learningLang = "English";
    private int themeMode = 2; // MODE_NIGHT_FOLLOW_SYSTEM
    private float fontSize = 16.0f;
    private boolean enableLeaderboard = true;
    private boolean enableDailyGoals = true;

    public User() {
        // Required for Firestore
    }

    public User(String uid, String name, String email, String role, String standard, long createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.standard = standard;
        this.createdAt = createdAt;
        this.xp = 0;
        this.streak = 0;
        this.lastActive = System.currentTimeMillis();
        this.lastSyncTime = System.currentTimeMillis();
        this.achievements = new java.util.ArrayList<>();
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public long getLastActive() { return lastActive; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }

    public long getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(long lastSyncTime) { this.lastSyncTime = lastSyncTime; }

    public java.util.List<String> getAchievements() { return achievements; }
    public void setAchievements(java.util.List<String> achievements) { this.achievements = achievements; }

    public float getSpeechSpeed() { return speechSpeed; }
    public void setSpeechSpeed(float speechSpeed) { this.speechSpeed = speechSpeed; }

    public boolean isAutoPronounce() { return autoPronounce; }
    public void setAutoPronounce(boolean autoPronounce) { this.autoPronounce = autoPronounce; }

    public String getLearningLang() { return learningLang; }
    public void setLearningLang(String learningLang) { this.learningLang = learningLang; }

    public int getThemeMode() { return themeMode; }
    public void setThemeMode(int themeMode) { this.themeMode = themeMode; }

    public float getFontSize() { return fontSize; }
    public void setFontSize(float fontSize) { this.fontSize = fontSize; }

    public boolean isEnableLeaderboard() { return enableLeaderboard; }
    public void setEnableLeaderboard(boolean enableLeaderboard) { this.enableLeaderboard = enableLeaderboard; }

    public boolean isEnableDailyGoals() { return enableDailyGoals; }
    public void setEnableDailyGoals(boolean enableDailyGoals) { this.enableDailyGoals = enableDailyGoals; }
}
