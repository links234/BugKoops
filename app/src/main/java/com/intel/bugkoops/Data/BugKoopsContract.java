package com.intel.bugkoops.Data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

public class BugKoopsContract {
    public static final String CONTENT_AUTHORITY = "com.intel.bugkoops";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_REPORT = "report";
    public static final String PATH_PROFILE = "profile";

    public static long normalizeDate(long startDate) {
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class ReportEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPORT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REPORT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REPORT;

        public static final String TABLE_NAME = "report";

        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TEXT = "text";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }
    }

    public static final class ProfileEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;

        public static final String TABLE_NAME = "profile";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}

