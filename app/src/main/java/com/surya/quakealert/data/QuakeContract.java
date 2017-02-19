package com.surya.quakealert.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Surya on 14-02-2017.
 */

public class QuakeContract {
    public static final String CONTENT_AUTHORITY = "com.surya.quakealert";

    //Use content authority to create the base of all URI's which apps will use to contact content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //possible paths
    public static final String PATH_QUAKE = "quake";

    //Inner class that defines table contents Quakes
    public static final class QuakeEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUAKE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUAKE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +PATH_QUAKE;

        //Table name
        public static final String TABLE_NAME = "Quakes";

        //column for magnitude
        public static final String COLUMN_QUAKE_MAGNITUDE = "mag";
        //column for title
        public static final String COLUMN_QUAKE_TITLE = "title";
        //column for time
        public static final String COLUMN_QUAKE_TIME = "time";
        //column for URL
        public static final String COLUMN_QUAKE_DETAIL_URL = "url";
        //column for Number of people felt
        public static final String COLUMN_QUAKE_COUNT = "count";
        //column for latitude
        public static final String COLUMN_QUAKE_LAT = "lat";
        //column for longitude
        public static final String COLUMN_QUAKE_LONG = "long";
        public static final String COLUMN_DAY = "day";
        public static final String COLUMN_NOTIFICATION_FLAG = "flag";

        public static Uri buildQuakeUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }

}
