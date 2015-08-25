package com.intel.bugkoops;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.intel.bugkoops.Integrations.Bugzilla.BugzillaAPI;
import com.intel.bugkoops.Integrations.Bugzilla.BugzillaAutoDetect;

public class BugzillaProgressTask extends AsyncTask<String, String, Boolean> {
    final String LOG_TAG = getClass().getSimpleName();
    final String USER_AGENT = "BugKoops";

    private OnTaskCompleted mListener;

    private ProgressDialog mDialog;
    private Activity mActivity;

    private String mTaskResult;

    private Bundle mParams;
    private Bundle mResult;

    private TextView mResultTextView;

    private String mServerUri;
    private BugzillaAPI mServer;

    private int mTask;

    private static final String DEFAULT_LOGIN = "Here comes your default account login";  //CHANGE THIS IN FINAL VERSION
    private static final String DEFAULT_PASSWORD = "Here comes your default account password";          //CHANGE THIS IN FINAL VERSION -- mai ales asta :)
    public static final String DEFAULT_SERVER = "https://landfill.bugzilla.org/bugzilla-5.0-branch/"; //CHANGE THIS IN FINAL VERSION

    //public static final String DEFAULT_SERVER = "https://bugzilla.kernel.org/"; //CHANGE THIS IN FINAL VERSION

    public static final String KEY_LOGIN = "login";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SERVER = "server";
    public static final String KEY_REPORT = "report";
    public static final String KEY_SESSION = "session";

    public static final String KEY_TASK = "task";
    public static final int TASK_SEND = 0;
    public static final int TASK_LOGIN_AND_GET_PRODUCT = 1;
    public static final int TASK_SESSION_SEND_LOGOUT = 2;
    public static final int TASK_SESSION_LOGOUT = 3;

    public BugzillaProgressTask(Activity activity, Bundle params, OnTaskCompleted listener) {
        mActivity = activity;
        mDialog = new ProgressDialog(mActivity);

        mTaskResult = null;

        mParams = params;
        if(mParams != null) {
            mTask = mParams.getInt(KEY_TASK);
        } else {
            mTask = TASK_SEND;
        }
        mResult = new Bundle();

        mServerUri = Utility.getString(mParams, KEY_SERVER, DEFAULT_SERVER);

        /*
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
        */

        mResultTextView = (TextView) mActivity.findViewById(R.id.bugzilla_send_status_textview);

        mListener = listener;
    }

    protected void onPreExecute() {
        mDialog.setTitle("Sending report to Bugzilla");
        mDialog.setMessage("Please wait ... ");
        mDialog.show();
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if(mTaskResult == null) {
            if (!success) {
                mTaskResult = "Unexpected error!";
            }
        }

        if(mTaskResult != null) {
            mResultTextView.setText(mTaskResult);
            mResultTextView.setVisibility(View.VISIBLE);
        }

        mListener.onTaskCompleted(mResult);
    }

    protected Boolean doInBackground(final String... args) {
        try {
            switch(mTask) {
                case TASK_SEND:
                    login();
                    send();
                    logout();
                    break;
                case TASK_LOGIN_AND_GET_PRODUCT:
                    login();
                    getProduct();
                    saveSession();
                    break;
                case TASK_SESSION_SEND_LOGOUT:
                    loadSession();
                    send();
                    logout();
                    break;
                case TASK_SESSION_LOGOUT:
                    loadSession();
                    logout();
                    break;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void onProgressUpdate(String... dialogText) {
        mDialog.setMessage(dialogText[0]);
    }

    private void login() throws Exception {
        publishProgress("Checking server version ... ");
        BugzillaAutoDetect bugzillaAutoDetect = new BugzillaAutoDetect(mServerUri, USER_AGENT);

        mServer = bugzillaAutoDetect.open();

        if(mServer == null) {
            setTaskResult("Unsupported server API!");
            throw new Exception();
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
            throw new Exception();
        }
    }

    private void send() throws Exception {
        publishProgress("Sending report ... ");
        boolean result = mServer.send(Utility.getBundle(mParams, KEY_REPORT));
        if(error() || !result) {
            publishProgress("Logging out ... ");
            mServer.logout();
            setTaskResult("Failed to send report!");
            throw new Exception();
        }
        int reportId = mServer.getResult().getInt(BugzillaAPI.KEY_ID);

        String bugUrl = Uri.parse(mServerUri).buildUpon()
                .appendPath("show_bug.cgi")
                .appendQueryParameter(BugzillaAPI.KEY_ID, Integer.toString(reportId))
                .build().toString();

        Log.d(LOG_TAG, "url = "+bugUrl);
    }

    private void logout() throws Exception {
        publishProgress("Logging out ... ");
        boolean result = mServer.logout();
        if(error() || !result) {
            setTaskResult("Report successfully sent but failed to logout!");
            throw new Exception();
        }
        setTaskResult("Report successfully sent!");
    }

    private void getProduct() throws Exception {
        publishProgress("Getting product list ... ");

        Thread.sleep(3000);
    }

    private void saveSession() {
        mResult.putBundle(KEY_SESSION, mServer.save());
    }

    private void loadSession() {
        BugzillaAutoDetect bugzillaAutoDetect = new BugzillaAutoDetect(mServerUri, USER_AGENT);

        mServer = bugzillaAutoDetect.restore(mParams.getBundle(KEY_SESSION));
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
