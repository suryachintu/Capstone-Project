package com.surya.quakealert.data;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Surya on 14-02-2017.
 */

public class QuakeProvider extends ContentProvider{

    //URI Matcher used by this content provider
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TAG = QuakeProvider.class.getSimpleName();
    private QuakeDBHelper dbHelper;

    static final int QUAKE = 100;


    static {

        uriMatcher.addURI(QuakeContract.CONTENT_AUTHORITY,QuakeContract.PATH_QUAKE,QUAKE);

    }

    @Override
    public boolean onCreate() {
        dbHelper = new QuakeDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor retCursor = null;
        switch (uriMatcher.match(uri)){

            case QUAKE:
                        retCursor = db.query(QuakeContract.QuakeEntry.TABLE_NAME,
                                    projection,
                                    selection,
                                    selectionArgs,
                                    null,
                                    null,
                                    sortOrder);
                break;

            default:
                Log.d(TAG,"Unsupported uri " + uri);
        }

        if (retCursor != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        }

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)){

            case QUAKE:
                return QuakeContract.QuakeEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Uri contentUri = null;
        long rowId = -1;

        switch (uriMatcher.match(uri)){
            case QUAKE:
                rowId = db.insert(QuakeContract.QuakeEntry.TABLE_NAME,null,contentValues);

                if (rowId > 0)
                    contentUri = QuakeContract.QuakeEntry.buildQuakeUri(rowId);
                break;
            default:
                rowId = -1;
                Log.d(TAG,"Unsupported uri");
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return contentUri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (uriMatcher.match(uri) == QUAKE){

            int rowId = db.delete(QuakeContract.QuakeEntry.TABLE_NAME,where,whereArgs);

            Log.d(TAG,"Deleted from content provider " + rowId);

            getContext().getContentResolver().notifyChange(uri,null);

            return rowId;
        }

        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count = 0;
        long rowId = 0;

        switch (uriMatcher.match(uri)){

            case QUAKE:
                for (ContentValues contentValues : values) {
                    rowId = db.insert(QuakeContract.QuakeEntry.TABLE_NAME,null,contentValues);
                    if (rowId > 0)
                        count++;
                }
                break;
            default:
                rowId = -1;
                Log.e("xxx","Unsupported uri " + uri);
                return super.bulkInsert(uri, values);
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return count;
    }

    @Override
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }
}
