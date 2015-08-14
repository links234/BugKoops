package com.intel.bugkoops.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class BugKoopsProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private BugKoopsDbHelper mOpenHelper;

    static final int REPORT = 100;
    static final int REPORT_WITH_ID = 101;
    static final int REPORT_WITH_DATE = 102;
    static final int PROFILE = 300;

    private static final SQLiteQueryBuilder sReportQueryBuilder;

    static {
        sReportQueryBuilder = new SQLiteQueryBuilder();

        sReportQueryBuilder.setTables(BugKoopsContract.ReportEntry.TABLE_NAME);
    }

    //report._ID = ?
    private static final String sReportById =
            BugKoopsContract.ReportEntry.TABLE_NAME+
                    "." + BugKoopsContract.ReportEntry._ID + " = ? ";

    //report.date >= ?
    private static final String sReportByDateGreaterOrEqual =
            BugKoopsContract.ReportEntry.TABLE_NAME+
                    "." + BugKoopsContract.ReportEntry.COLUMN_DATE + " >= ? ";


    private Cursor getReportByDate(Uri uri, String[] projection, String sortOrder) {
        long date = BugKoopsContract.ReportEntry.getDateFromUri(uri);

        String selection = sReportByDateGreaterOrEqual;
        String[] selectionArgs = new String[]{Long.toString(date)};

        return sReportQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getReportById(
            Uri uri, String[] projection, String sortOrder) {
        int id = BugKoopsContract.ReportEntry.getIdFromUri(uri);

        String selection = sReportById;
        String[] selectionArgs = new String[]{Integer.toString(id)};

        return sReportQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BugKoopsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, BugKoopsContract.PATH_REPORT, REPORT);
        matcher.addURI(authority, BugKoopsContract.PATH_REPORT + "/id/#", REPORT_WITH_ID);
        matcher.addURI(authority, BugKoopsContract.PATH_REPORT + "/date/#", REPORT_WITH_DATE);

        matcher.addURI(authority, BugKoopsContract.PATH_PROFILE, PROFILE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new BugKoopsDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case REPORT_WITH_DATE:
                return BugKoopsContract.ReportEntry.CONTENT_TYPE;
            case REPORT_WITH_ID:
                return BugKoopsContract.ReportEntry.CONTENT_ITEM_TYPE;
            case REPORT:
                return BugKoopsContract.ReportEntry.CONTENT_TYPE;
            case PROFILE:
                return BugKoopsContract.ProfileEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "report/id/#"
            case REPORT_WITH_ID:
            {
                retCursor = getReportById(uri, projection, sortOrder);
                break;
            }
            // "report/date/#"
            case REPORT_WITH_DATE: {
                retCursor = getReportByDate(uri, projection, sortOrder);
                break;
            }
            // "report"
            case REPORT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BugKoopsContract.ReportEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case REPORT: {
                long _id = db.insert(BugKoopsContract.ReportEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BugKoopsContract.ReportEntry.buildUriFromId(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PROFILE: {
                long _id = db.insert(BugKoopsContract.ProfileEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BugKoopsContract.ProfileEntry.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if ( null == selection ) selection = "1";
        switch (match) {
            case REPORT:
                rowsDeleted = db.delete(
                        BugKoopsContract.ReportEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REPORT_WITH_ID:
                selection = "_ID = ?";
                selectionArgs = new String[]{Long.toString(
                        BugKoopsContract.ReportEntry.getIdFromUri(uri))};
                rowsDeleted = db.delete(
                        BugKoopsContract.ReportEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PROFILE:
                rowsDeleted = db.delete(
                        BugKoopsContract.ProfileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case REPORT:
                rowsUpdated = db.update(BugKoopsContract.ReportEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REPORT_WITH_ID:
                selection = "_ID = ?";
                selectionArgs = new String[]{Long.toString(
                        BugKoopsContract.ReportEntry.getIdFromUri(uri))};
                rowsUpdated = db.update(
                        BugKoopsContract.ReportEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PROFILE:
                rowsUpdated = db.update(BugKoopsContract.ProfileEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}