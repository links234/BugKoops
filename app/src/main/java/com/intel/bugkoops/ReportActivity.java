package com.intel.bugkoops;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class ReportActivity extends MenuActivity implements ReportListFragment.Callback  {
    final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        Intent intent = new Intent(this, ReportDetailActivity.class)
                .setData(contentUri);
        startActivity(intent);
    }
}
