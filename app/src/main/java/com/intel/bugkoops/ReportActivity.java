package com.intel.bugkoops;

import android.os.Bundle;

public class ReportActivity extends MenuActivity {
    final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
    }
}
