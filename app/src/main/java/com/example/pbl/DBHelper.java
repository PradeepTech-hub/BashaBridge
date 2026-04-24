package com.example.pbl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartPronunciation.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_PROGRESS = "progress";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WORD = "word";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_CATEGORY = "category";

    public static final String TABLE_PREFERENCES = "preferences";
    public static final String COLUMN_FAV_CATEGORY = "fav_category";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createProgressTable = "CREATE TABLE " + TABLE_PROGRESS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_WORD + " TEXT, " +
                COLUMN_SCORE + " INTEGER, " +
                COLUMN_CATEGORY + " TEXT)";
        db.execSQL(createProgressTable);

        String createPrefsTable = "CREATE TABLE " + TABLE_PREFERENCES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FAV_CATEGORY + " TEXT)";
        db.execSQL(createPrefsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCES);
        onCreate(db);
    }

    public void insertProgress(String word, int score, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORD, word);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_CATEGORY, category);
        db.insert(TABLE_PROGRESS, null, values);
        db.close();
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
}
