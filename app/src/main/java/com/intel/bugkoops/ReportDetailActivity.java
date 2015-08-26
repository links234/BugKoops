package com.intel.bugkoops;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.intel.bugkoops.Data.BugKoopsContract;

public class ReportDetailActivity extends MenuActivity {
    private static final String LOG_TAG = ReportDetailActivity.class.getSimpleName();

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_RESULT = "result";

    public static final String KEY_REPORT_TITLE = "report_title";
    public static final String KEY_REPORT_DATE = "report_date";
    public static final String KEY_REPORT_TEXT = "report_text";

    public static final int ID_BUGZILLA_RESULT = 0;

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

                    String alertDialogMessage = getString(R.string.dialog_save_report_question);
                    if(reportDetailFragment.firstTime()) {
                        alertDialogMessage = getString(R.string.dialog_save_new_report_question);
                    }

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setMessage(alertDialogMessage)
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
            case R.id.action_report_detail_send_email:
                saveBeforeSend(new Runnable() {
                    @Override
                    public void run() {
                        String[] TO = {""};
                        String[] CC = {""};
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);

                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.setType("message/rfc822");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                        emailIntent.putExtra(Intent.EXTRA_CC, CC);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, reportDetailFragment.getTitle());
                        emailIntent.putExtra(Intent.EXTRA_TEXT, reportDetailFragment.getReport(ReportDetailFragment.REPORT_TYPE_DATE | ReportDetailFragment.REPORT_TYPE_TEXT));

                        try {
                            startActivity(Intent.createChooser(emailIntent, getString(R.string.dialog_choose_email_title)));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(ReportDetailActivity.this, getString(R.string.dialog_choose_email_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.action_report_detail_send_share:
                saveBeforeSend(new Runnable() {
                    @Override
                    public void run() {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, reportDetailFragment.getReport(ReportDetailFragment.REPORT_TYPE_ALL));
                        sendIntent.setType("text/plain");

                        try {
                            startActivity(Intent.createChooser(sendIntent, getString(R.string.dialog_choose_share_title)));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(ReportDetailActivity.this, getString(R.string.dialog_choose_share_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.action_report_detail_send_bugzilla:
                saveBeforeSend(new Runnable() {
                    @Override
                    public void run() {
                        if(Utility.isNetworkAvailable(ReportDetailActivity.this)) {
                            Intent intent = new Intent(ReportDetailActivity.this, BugzillaSendActivity.class);
                            Bundle data = new Bundle();
                            data.putString(KEY_REPORT_TITLE, reportDetailFragment.getTitle());
                            data.putLong(KEY_REPORT_DATE, reportDetailFragment.getDate());
                            data.putString(KEY_REPORT_TEXT, reportDetailFragment.getText());
                            intent.putExtras(data);
                            startActivityForResult(intent, ID_BUGZILLA_RESULT);
                        } else {
                            Toast.makeText(ReportDetailActivity.this, "You are not connected to internet!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void saveBeforeSend(final Runnable action) {
        if(reportDetailFragment.modified()) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            reportDetailFragment.save();
                            action.run();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            action.run();
                            break;
                    }
                }
            };

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(getString(R.string.dialog_save_before_send))
                    .setPositiveButton(getString(R.string.dialog_positive), dialogClickListener)
                    .setNegativeButton(getString(R.string.dialog_negative), dialogClickListener).show();
        } else {
            action.run();
        }
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

            String alertDialogMessage = getString(R.string.dialog_discard_report_change_question);
            if(reportDetailFragment.firstTime()) {
                alertDialogMessage = getString(R.string.dialog_discard_report_new_question);
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(alertDialogMessage)
                    .setPositiveButton(getString(R.string.dialog_save), dialogClickListener)
                    .setNegativeButton(getString(R.string.dialog_discard), dialogClickListener)
                    .setNeutralButton(getString(R.string.dialog_neutral), dialogClickListener).show();
        } else {
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ID_BUGZILLA_RESULT) {
            if(resultCode == RESULT_OK) {
                String message = data.getStringExtra(KEY_MESSAGE);
                reportDetailFragment.setResultSlackbar(message);

                final TextView vmessage = new TextView(this);

                final SpannableString s =
                        new SpannableString(data.getStringExtra(KEY_RESULT));
                Linkify.addLinks(s, Linkify.WEB_URLS);
                vmessage.setText(s);
                vmessage.setMovementMethod(LinkMovementMethod.getInstance());

                new AlertDialog.Builder(this)
                        .setTitle("Bugzilla bug url")
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("Dismiss", null)
                        .setView(vmessage)
                        .create()
                        .show();
            }
        }
    }
}
