package com.intel.bugkoops;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.intel.bugkoops.Integrations.Bugzilla.BugzillaAPI;
import com.intel.bugkoops.Integrations.Bugzilla.BugzillaAutoDetect;

public class BugzillaProgressTask extends AsyncTask<String, String, Boolean> {
    final String LOG_TAG = getClass().getSimpleName();
    final String USER_AGENT = "BugKoops";

    private ProgressDialog mDialog;
    private Activity mActivity;

    private String mTaskResult;

    private Bundle mParams;

    private Snackbar mResultSnackbar;

    private String mServerUri;
    private BugzillaAPI mServer;

    private static final String DEFAULT_LOGIN = "Here comes your default account login";  //CHANGE THIS IN FINAL VERSION
    private static final String DEFAULT_PASSWORD = "Here comes your default account password";          //CHANGE THIS IN FINAL VERSION -- mai ales asta :)
    public static final String DEFAULT_SERVER = "https://landfill.bugzilla.org/bugzilla-5.0-branch/"; //CHANGE THIS IN FINAL VERSION

    //public static final String DEFAULT_SERVER = "https://bugzilla.kernel.org/"; //CHANGE THIS IN FINAL VERSION

    public static final String KEY_LOGIN = "login";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SERVER = "server";
    public static final String KEY_REPORT = "report";

    public BugzillaProgressTask(Activity activity, Bundle params) {
        mActivity = activity;
        mDialog = new ProgressDialog(mActivity);

        mTaskResult = null;

        mParams = params;

        mServerUri = Utility.getString(mParams, KEY_SERVER, DEFAULT_SERVER);

        mResultSnackbar = Snackbar.make(
                mActivity.findViewById(R.id.detail_report_snackbar),
                "",
                Snackbar.LENGTH_INDEFINITE);

        mResultSnackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultSnackbar.setDuration(0);
            }
        });
    }

    protected void onPreExecute() {
        mDialog.setTitle("Sending report to Bugzilla");
        mDialog.setMessage("Checking server version ... ");
        mDialog.show();
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if(mTaskResult == null) {
            if (success) {
                mTaskResult = "Report sent!";
            } else {
                mTaskResult = "Unexpected error while sending report!";
            }
        }

        mResultSnackbar.setText(mTaskResult).show();
    }

    protected Boolean doInBackground(final String... args) {
        try {
            BugzillaAutoDetect bugzillaAutoDetect = new BugzillaAutoDetect(mServerUri, USER_AGENT);

            mServer = bugzillaAutoDetect.open();

            if(mServer == null) {
                setTaskResult("Unsupported server API!");
                return false;
            }

            mServer.version();
            publishProgress("Found " + mServer.getAPIVersion() + " version " +
                    mServer.getResult().getString(BugzillaAPI.KEY_VERSION));

            Thread.sleep(3000);

            publishProgress("Logging in ... ");
            boolean result = mServer.login(
                    Utility.getString(mParams, KEY_LOGIN, DEFAULT_LOGIN),
                    Utility.getString(mParams, KEY_PASSWORD, DEFAULT_PASSWORD));
            if(error() || !result) {
                setTaskResult("Failed to login!");
                return false;
            }

            publishProgress("Sending report ... ");
            result = mServer.send(Utility.getBundle(mParams, KEY_REPORT));
            if(error() || !result) {
                publishProgress("Logging out ... ");
                mServer.logout();
                setTaskResult("Failed to send report!");
                return false;
            }
            int reportId = mServer.getResult().getInt(BugzillaAPI.KEY_ID);

            String bugUrl = Uri.parse(mServerUri).buildUpon()
                    .appendPath("show_bug.cgi")
                    .appendQueryParameter(BugzillaAPI.KEY_ID, Integer.toString(reportId))
                    .build().toString();

            Log.d(LOG_TAG, "url = "+bugUrl);

            publishProgress("Logging out ... ");
            result = mServer.logout();
            if(error() || !result) {
                setTaskResult("Report successfully sent but failed to logout!");
                return false;
            }
            setTaskResult("Report successfully sent!");

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error", e);
        }
        return false;
    }

    protected void onProgressUpdate(String... dialogText) {
        mDialog.setMessage(dialogText[0]);
    }

    private void setTaskResult(String result) {
        if(mTaskResult == null) {
            mTaskResult = result;
        }
    }

    private boolean error(Bundle bundle) {
        if(bundle.getBoolean("error")) {
            Log.e(LOG_TAG, "Bugzilla returned error response with code: " + Integer.toString(
                    bundle.getInt("code")));
            String message = bundle.getString(BugzillaAPI.KEY_MESSAGE);
            if (message != null) {
                setTaskResult(message);
            }
            return true;
        }
        return false;
    }

    private boolean error() {
        return error(mServer.getResult());
    }
}
