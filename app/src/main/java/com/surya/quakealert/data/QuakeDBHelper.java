package com.surya.quakealert.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Surya on 14-02-2017.
 */

public class QuakeDBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "QuakeAlerts.db";

    public QuakeDBHelper(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_QUAKE_TABLE = "CREATE TABLE " +
                                                QuakeContract.QuakeEntry.TABLE_NAME + " (" +
                                                QuakeContract.QuakeEntry._ID + " INTEGER PRIMARY KEY," +
                                                QuakeContract.QuakeEntry.COLUMN_QUAKE_MAGNITUDE + " REAL NOT NULL, " +
                                                QuakeContract.QuakeEntry.COLUMN_QUAKE_TITLE + " TEXT NOT NULL, " +
                                                QuakeContract.QuakeEntry.COLUMN_QUAKE_TIME + " INTEGER NOT NULL, " +
                                                QuakeContract.QuakeEntry.COLUMN_QUAKE_DETAIL_URL + " TEXT NOT NULL, " +
                                                QuakeContract.QuakeEntry.COLUMN_QUAKE_COUNT + " INTEGER NOT NULL, " +
                                                QuakeContract.QuakeEntry.COLUMN_QUAKE_LAT + " REAL NOT NULL, " +
                                                QuakeContract.QuakeEntry.COLUMN_QUAKE_LONG + " REAL NOT NULL," +
                                                QuakeContract.QuakeEntry.COLUMN_DAY + " INTEGER NOT NULL," +
                                                "UNIQUE (" + QuakeContract.QuakeEntry.COLUMN_QUAKE_TITLE +","
                                                + QuakeContract.QuakeEntry.COLUMN_QUAKE_MAGNITUDE
                                                + ") ON CONFLICT REPLACE);" ;
        sqLiteDatabase.execSQL(SQL_CREATE_QUAKE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //drop the table on upgrade
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + QuakeContract.QuakeEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
