package com.example.todoappwithdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyOpener extends SQLiteOpenHelper {
    protected final static String DATABASE_NAME = "todo.db";
    protected final static int VERSION_NUMBER = 1;
    public final static String TABLE_NAME = "todo";
    public final static String COL_ID = "_id";
    public final static String COL_TASK = "task";
    public final static String COL_URGENT = "urgent";

    public MyOpener(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUMBER);
        Log.d("DatabaseDebug", "Database path: " + ctx.getDatabasePath(DATABASE_NAME));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TASK + " TEXT, " +
                COL_URGENT + " INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
        android.util.Log.d("MyOpener", "Database opened successfully.");
    }
}
