package com.intel.bugkoops;

import android.os.Bundle;
import android.view.Menu;


public class ReportDetailActivity extends MenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ReportDetailFragment.DETAIL_URI, getIntent().getData());

            ReportDetailFragment fragment = new ReportDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_report_container, fragment)
                    .commit();
        }
    }
}

