package com.intel.bugkoops.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.intel.bugkoops.Data.BugKoopsContract.ReportEntry;
import com.intel.bugkoops.Data.BugKoopsContract.ProfileEntry;

public class BugKoopsDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "BugKoops.db";

    public BugKoopsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_REPORT_TABLE = "CREATE TABLE " + ReportEntry.TABLE_NAME + " (" +
                ReportEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ReportEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                ReportEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                ReportEntry.COLUMN_TEXT + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_PROFILE_TABLE = "CREATE TABLE " + ProfileEntry.TABLE_NAME + " (" +
                ProfileEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_REPORT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PROFILE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReportEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProfileEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
