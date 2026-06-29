package com.example.pbl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartPronunciation.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_PROGRESS = "progress";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WORD = "word";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public static final String TABLE_PREFERENCES = "preferences";
    public static final String COLUMN_FAV_CATEGORY = "fav_category";

    // New Tables for Offline Mode
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_XP = "xp";
    public static final String COLUMN_STREAK = "streak";
    public static final String COLUMN_STANDARD = "standard";
    public static final String COLUMN_LAST_ACTIVE = "last_active";

    // New columns for Settings sync
    public static final String COLUMN_SPEECH_SPEED = "speech_speed";
    public static final String COLUMN_AUTO_PRONOUNCE = "auto_pronounce";
    public static final String COLUMN_LEARNING_LANG = "learning_lang";
    public static final String COLUMN_THEME_MODE = "theme_mode";
    public static final String COLUMN_FONT_SIZE = "font_size";
    public static final String COLUMN_ENABLE_LEADERBOARD = "enable_leaderboard";
    public static final String COLUMN_ENABLE_DAILY_GOALS = "enable_daily_goals";
    public static final String COLUMN_LAST_SYNC_TIME = "last_sync_time";

    public static final String TABLE_CONTENT = "content";
    public static final String COLUMN_CONTENT_ID = "content_id"; // Firestore Doc ID
    public static final String COLUMN_ENGLISH = "english";
    public static final String COLUMN_KANNADA = "kannada";
    public static final String COLUMN_HINDI = "hindi";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_TYPE = "type"; // 'Word' or 'Sentence'

    public static final String TABLE_SYNC_QUEUE = "sync_queue";
    public static final String COLUMN_SYNCED = "synced"; // 0 for no, 1 for yes

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PROGRESS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_WORD + " TEXT, " +
                COLUMN_SCORE + " INTEGER, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_PREFERENCES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FAV_CATEGORY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_UID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_ROLE + " TEXT, " +
                COLUMN_XP + " INTEGER, " +
                COLUMN_STREAK + " INTEGER, " +
                COLUMN_STANDARD + " TEXT, " +
                COLUMN_LAST_ACTIVE + " INTEGER, " +
                COLUMN_SPEECH_SPEED + " REAL DEFAULT 1.0, " +
                COLUMN_AUTO_PRONOUNCE + " INTEGER DEFAULT 1, " +
                COLUMN_LEARNING_LANG + " TEXT DEFAULT 'English', " +
                COLUMN_THEME_MODE + " INTEGER DEFAULT 2, " +
                COLUMN_FONT_SIZE + " REAL DEFAULT 16.0, " +
                COLUMN_ENABLE_LEADERBOARD + " INTEGER DEFAULT 1, " +
                COLUMN_ENABLE_DAILY_GOALS + " INTEGER DEFAULT 1, " +
                COLUMN_LAST_SYNC_TIME + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_CONTENT + " (" +
                COLUMN_CONTENT_ID + " TEXT PRIMARY KEY, " +
                COLUMN_ENGLISH + " TEXT, " +
                COLUMN_KANNADA + " TEXT, " +
                COLUMN_HINDI + " TEXT, " +
                COLUMN_IMAGE_URL + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_STANDARD + " INTEGER, " +
                COLUMN_TYPE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_SYNC_QUEUE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_UID + " TEXT, " +
                COLUMN_WORD + " TEXT, " +
                COLUMN_SCORE + " INTEGER, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYNC_QUEUE);
            onCreate(db);
        } else if (oldVersion == 3) {
            // Add new columns for version 4
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_SPEECH_SPEED + " REAL DEFAULT 1.0");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_AUTO_PRONOUNCE + " INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_LEARNING_LANG + " TEXT DEFAULT 'English'");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_THEME_MODE + " INTEGER DEFAULT 2");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_FONT_SIZE + " REAL DEFAULT 16.0");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ENABLE_LEADERBOARD + " INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ENABLE_DAILY_GOALS + " INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_LAST_SYNC_TIME + " INTEGER DEFAULT 0");
        }
    }

    public void saveUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, user.getUid());
        values.put(COLUMN_NAME, user.getName());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_ROLE, user.getRole());
        values.put(COLUMN_XP, user.getXp());
        values.put(COLUMN_STREAK, user.getStreak());
        values.put(COLUMN_STANDARD, user.getStandard());
        values.put(COLUMN_LAST_ACTIVE, user.getLastActive());
        
        // Settings persistence
        values.put(COLUMN_SPEECH_SPEED, user.getSpeechSpeed());
        values.put(COLUMN_AUTO_PRONOUNCE, user.isAutoPronounce() ? 1 : 0);
        values.put(COLUMN_LEARNING_LANG, user.getLearningLang());
        values.put(COLUMN_THEME_MODE, user.getThemeMode());
        values.put(COLUMN_FONT_SIZE, user.getFontSize());
        values.put(COLUMN_ENABLE_LEADERBOARD, user.isEnableLeaderboard() ? 1 : 0);
        values.put(COLUMN_ENABLE_DAILY_GOALS, user.isEnableDailyGoals() ? 1 : 0);
        values.put(COLUMN_LAST_SYNC_TIME, user.getLastSyncTime());

        db.replace(TABLE_USERS, null, values);
    }

    public User getUser(String uid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_UID + "=?", new String[]{uid}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setUid(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UID)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)));
            user.setXp(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_XP)));
            user.setStreak(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STREAK)));
            user.setStandard(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STANDARD)));
            user.setLastActive(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_ACTIVE)));
            
            // Settings recovery
            user.setSpeechSpeed(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SPEECH_SPEED)));
            user.setAutoPronounce(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AUTO_PRONOUNCE)) == 1);
            user.setLearningLang(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEARNING_LANG)));
            user.setThemeMode(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_THEME_MODE)));
            user.setFontSize(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_FONT_SIZE)));
            user.setEnableLeaderboard(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENABLE_LEADERBOARD)) == 1);
            user.setEnableDailyGoals(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENABLE_DAILY_GOALS)) == 1);
            user.setLastSyncTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_SYNC_TIME)));

            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setUid(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UID)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)));
            user.setXp(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_XP)));
            user.setStreak(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STREAK)));
            user.setStandard(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STANDARD)));
            user.setLastActive(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_ACTIVE)));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public void saveContent(Word word, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT_ID, word.getId());
        values.put(COLUMN_ENGLISH, word.getEnglish());
        values.put(COLUMN_KANNADA, word.getKannada());
        values.put(COLUMN_HINDI, word.getHindi());
        values.put(COLUMN_IMAGE_URL, word.getImageUrl());
        values.put(COLUMN_CATEGORY, word.getCategory());
        values.put(COLUMN_STANDARD, word.getStandard());
        values.put(COLUMN_TYPE, type);
        db.replace(TABLE_CONTENT, null, values);
    }

    public List<Word> getOfflineContent(int standard, String category, String type) {
        List<Word> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_STANDARD + "=? AND " + COLUMN_CATEGORY + "=? AND " + COLUMN_TYPE + "=?";
        String[] args = {String.valueOf(standard), category, type};
        Cursor cursor = db.query(TABLE_CONTENT, null, selection, args, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Word w = new Word(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KANNADA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HINDI)),
                        0
                );
                w.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT_ID)));
                w.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
                w.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                w.setStandard(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STANDARD)));
                list.add(w);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void addToSyncQueue(String uid, String word, int score, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, uid);
        values.put(COLUMN_WORD, word);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        db.insert(TABLE_SYNC_QUEUE, null, values);
    }

    public ArrayList<HashMap<String, Object>> getSyncQueue() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SYNC_QUEUE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                map.put("uid", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UID)));
                map.put("word", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD)));
                map.put("score", cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE)));
                map.put("category", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                map.put("timestamp", cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void removeFromSyncQueue(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC_QUEUE, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void insertProgress(String word, int score, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORD, word);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        db.insert(TABLE_PROGRESS, null, values);
    }


    public void setFavoriteCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PREFERENCES);
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAV_CATEGORY, category);
        db.insert(TABLE_PREFERENCES, null, values);
        db.close();
    }

    public String getFavoriteCategory() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_FAV_CATEGORY + " FROM " + TABLE_PREFERENCES, null);
        String fav = "None";
        if (cursor.moveToFirst()) {
            fav = cursor.getString(0);
        }
        cursor.close();
        return fav;
    }

    public ArrayList<HashMap<String, String>> getAllProgress() {
        ArrayList<HashMap<String, String>> progressList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROGRESS + " ORDER BY " + COLUMN_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> progress = new HashMap<>();
                progress.put("word", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD)));
                progress.put("score", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE))));
                progress.put("category", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                progressList.add(progress);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return progressList;
    }

    public ArrayList<String> getWeakWords() {
        ArrayList<String> weakWords = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_WORD + " FROM " + TABLE_PROGRESS + " WHERE " + COLUMN_SCORE + " < 70", null);
        if (cursor.moveToFirst()) {
            do {
                weakWords.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return weakWords;
    }

    public HashMap<String, Object> getProgressStats() {
        HashMap<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Total Words (Category != 'Sentences')
        Cursor cursorWords = db.rawQuery("SELECT COUNT(DISTINCT " + COLUMN_WORD + ") FROM " + TABLE_PROGRESS + " WHERE " + COLUMN_CATEGORY + " != 'Sentences'", null);
        int totalWords = 0;
        if (cursorWords.moveToFirst()) totalWords = cursorWords.getInt(0);
        cursorWords.close();

        // Total Sentences (Category == 'Sentences')
        Cursor cursorSentences = db.rawQuery("SELECT COUNT(DISTINCT " + COLUMN_WORD + ") FROM " + TABLE_PROGRESS + " WHERE " + COLUMN_CATEGORY + " == 'Sentences'", null);
        int totalSentences = 0;
        if (cursorSentences.moveToFirst()) totalSentences = cursorSentences.getInt(0);
        cursorSentences.close();

        // Average Accuracy
        Cursor cursorAvg = db.rawQuery("SELECT AVG(" + COLUMN_SCORE + ") FROM " + TABLE_PROGRESS, null);
        float avgScore = 0;
        if (cursorAvg.moveToFirst()) avgScore = cursorAvg.getFloat(0);
        cursorAvg.close();

        // Best Score
        Cursor cursorMax = db.rawQuery("SELECT MAX(" + COLUMN_SCORE + ") FROM " + TABLE_PROGRESS, null);
        int maxScore = 0;
        if (cursorMax.moveToFirst()) maxScore = cursorMax.getInt(0);
        cursorMax.close();

        // Total Sessions (Estimated by unique entries)
        Cursor cursorCount = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PROGRESS, null);
        int totalAttempts = 0;
        if (cursorCount.moveToFirst()) totalAttempts = cursorCount.getInt(0);
        cursorCount.close();

        // Weakest Category
        Cursor cursorWeakCat = db.rawQuery("SELECT " + COLUMN_CATEGORY + ", AVG(" + COLUMN_SCORE + ") as avg_score FROM " + TABLE_PROGRESS + " GROUP BY " + COLUMN_CATEGORY + " ORDER BY avg_score ASC LIMIT 1", null);
        String weakestCat = "N/A";
        if (cursorWeakCat.moveToFirst()) weakestCat = cursorWeakCat.getString(0);
        cursorWeakCat.close();

        stats.put("totalWords", totalWords);
        stats.put("totalSentences", totalSentences);
        stats.put("averageAccuracy", Math.round(avgScore));
        stats.put("bestScore", maxScore);
        stats.put("totalAttempts", totalAttempts);
        stats.put("weakestCategory", weakestCat);

        return stats;
    }

    public ArrayList<HashMap<String, Object>> getCategoryProgress() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_CATEGORY + ", AVG(" + COLUMN_SCORE + ") as avg_score, COUNT(*) as count FROM " + TABLE_PROGRESS + " GROUP BY " + COLUMN_CATEGORY, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, Object> map = new HashMap<>();
                map.put("category", cursor.getString(0));
                map.put("score", Math.round(cursor.getFloat(1)));
                map.put("count", cursor.getInt(2));
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
