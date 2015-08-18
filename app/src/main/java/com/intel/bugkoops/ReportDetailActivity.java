package com.intel.bugkoops;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.intel.bugkoops.Data.BugKoopsContract;

public class ReportDetailActivity extends MenuActivity {
    private static final String LOG_TAG = ReportDetailActivity.class.getSimpleName();

    private long mId;
    private boolean mIsNew;
    private ReportDetailFragment reportDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        if (savedInstanceState == null) {
            Uri uri = getIntent().getData();
            if(uri != null) {
                Bundle arguments = new Bundle();
                arguments.putParcelable(ReportDetailFragment.DETAIL_URI, uri);

                mId = BugKoopsContract.ReportEntry.getIdFromUri(uri);

                reportDetailFragment = new ReportDetailFragment();
                reportDetailFragment.setArguments(arguments);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.detail_report_container, reportDetailFragment)
                        .commit();
            } else {
                mId = 0;
                mIsNew = true;

                reportDetailFragment = new ReportDetailFragment();

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.detail_report_container, reportDetailFragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_report_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_report_detail_save:
                if(reportDetailFragment.modified()) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    reportDetailFragment.save();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setMessage(getString(R.string.dialog_save_report_question))
                            .setPositiveButton(getString(R.string.dialog_positive), dialogClickListener)
                            .setNegativeButton(getString(R.string.dialog_negative), dialogClickListener).show();
                }
                break;
            case R.id.action_report_detail_delete:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                final ContentResolver contentResolver = getContentResolver();
                                contentResolver.delete(BugKoopsContract.ReportEntry.buildUriFromId(mId),
                                        null, null);
                                finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage(getString(R.string.dialog_delete_report_question))
                        .setPositiveButton(getString(R.string.dialog_positive), dialogClickListener)
                        .setNegativeButton(getString(R.string.dialog_negative), dialogClickListener).show();
                break;
            case R.id.action_report_detail_send:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(reportDetailFragment.modified()) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            reportDetailFragment.save();
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEUTRAL:
                            break;
                    }
                }
            };

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(getString(R.string.dialog_discard_report_change_question))
                    .setPositiveButton(getString(R.string.dialog_save), dialogClickListener)
                    .setNegativeButton(getString(R.string.dialog_discard), dialogClickListener)
                    .setNeutralButton(getString(R.string.dialog_neutral), dialogClickListener).show();
        } else {
            finish();
        }
    }
}
