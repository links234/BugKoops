package com.intel.bugkoops.Data;

import android.util.Log;
import android.widget.Toast;

import com.intel.bugkoops.ScannerActivity;

public class ReportManager {
    private static final String LOG_TAG = ReportManager.class.getSimpleName();

    public static void push(String text) {
        Log.d(LOG_TAG, "Scanned report: ");
        Log.d(LOG_TAG, text);

       // Toast.makeText(ScannerActivity.getInstance(), text,
        //        Toast.LENGTH_SHORT).show();
        Toast.makeText(ScannerActivity.getInstance(), "Time elapsed: "+
                Float.toString(MessageManager.getLastElapsedTime()), Toast.LENGTH_SHORT).show();
    }
}
