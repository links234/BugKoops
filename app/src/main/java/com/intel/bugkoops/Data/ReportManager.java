package com.intel.bugkoops.Data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.intel.bugkoops.ScannerActivity;

import java.util.Date;

public class ReportManager {
    private static final String LOG_TAG = ReportManager.class.getSimpleName();

    public static void push(String text) {
        Log.d(LOG_TAG, "Scanned report: ");
        Log.d(LOG_TAG, text);

        ContentValues reportValues = new ContentValues();

        // Then add the data, along with the corresponding name of the data type,
        // so the content provider knows what kind of value is being inserted.
        reportValues.put(BugKoopsContract.ReportEntry.COLUMN_DATE, BugKoopsContract.dateToDB(new Date()));
        reportValues.put(BugKoopsContract.ReportEntry.COLUMN_TITLE, "Scanned report");
        reportValues.put(BugKoopsContract.ReportEntry.COLUMN_TEXT, text);

        // Finally, insert location data into the database.
        Uri insertedUri = ScannerActivity.getInstance().getContentResolver().insert(
                BugKoopsContract.ReportEntry.CONTENT_URI,
                reportValues
        );

        long reportId = ContentUris.parseId(insertedUri);

       // Toast.makeText(ScannerActivity.getInstance(), text,
        //        Toast.LENGTH_SHORT).show();
        Toast.makeText(ScannerActivity.getInstance(), "Time elapsed: "+
                Float.toString(MessageManager.getLastElapsedTime()), Toast.LENGTH_SHORT).show();
    }
}
