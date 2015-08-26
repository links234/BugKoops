package com.intel.bugkoops;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

    public static final String DEFAULT_LOGIN = "Here comes your default account login";  //CHANGE THIS IN FINAL VERSION
    public static final String DEFAULT_PASSWORD = "Here comes your default account password";          //CHANGE THIS IN FINAL VERSION -- mai ales asta :)
    public static final String DEFAULT_SERVER = "https://landfill.bugzilla.org/bugzilla-5.0-branch/"; //CHANGE THIS IN FINAL VERSION

    //public static final String DEFAULT_SERVER = "https://bugzilla.kernel.org/"; //CHANGE THIS IN FINAL VERSION

    public static final String KEY_LOGIN = "login";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SERVER = "server";
    public static final String KEY_ATTACHMENT = "attachment";
    public static final String KEY_REPORT = "report";
    public static final String KEY_SESSION = "session";
    public static final String KEY_CREATED_BUG_URL = "created_bug_url";
    public static final String KEY_CREATED_BUG_ID = "created_bug_id";
    public static final String KEY_ERROR = "error";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_PRODUCTS = "products";
    public static final String KEY_COMPONENTS = "components";
    public static final String KEY_FIELDS = "fields";
    public static final String KEY_NAME = "name";

    public static final String KEY_TASK = "task";
    public static final int TASK_SEND = 0;
    public static final int TASK_LOGIN_GET_PRODUCTS_GET_COMPONENTS_GET_FIELDS = 1;
    public static final int TASK_SESSION_SEND_LOGOUT = 2;
    public static final int TASK_SESSION_SEND_WITH_ATTACHMENT_LOGOUT = 3;
    public static final int TASK_SESSION_LOGOUT = 4;

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
        mResult.putInt(KEY_TASK, mTask);

        mServerUri = Utility.getString(mParams, KEY_SERVER, DEFAULT_SERVER);

        mResultTextView = (TextView) mActivity.findViewById(R.id.bugzilla_send_status_textview);

        mListener = listener;
    }

    protected void onPreExecute() {
        mDialog.setTitle("Sending report to Bugzilla");
        mDialog.setMessage("Please wait ... ");
        mDialog.setCanceledOnTouchOutside(false);
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
                case TASK_LOGIN_GET_PRODUCTS_GET_COMPONENTS_GET_FIELDS:
                    login();
                    getHierarchy();
                    getFields();
                    saveSession();
                    break;
                case TASK_SESSION_SEND_LOGOUT:
                    loadSession();
                    send();
                    logout();
                    break;
                case TASK_SESSION_SEND_WITH_ATTACHMENT_LOGOUT:
                    loadSession();
                    send();
                    sendAttachment();
                    logout();
                    break;
                case TASK_SESSION_LOGOUT:
                    loadSession();
                    logout();
                    break;
            }

            return true;
        } catch (Exception e) {
            mResult.putBoolean(KEY_ERROR, true);
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

        Thread.sleep(2000);

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
            setTaskResult("Failed to send report!");
            throw new Exception();
        }
        int reportId = mServer.getResult().getInt(BugzillaAPI.KEY_ID);

        String bugUrl = Uri.parse(mServerUri).buildUpon()
                .appendPath("show_bug.cgi")
                .appendQueryParameter(BugzillaAPI.KEY_ID, Integer.toString(reportId))
                .build().toString();

        mResult.putString(KEY_CREATED_BUG_URL, bugUrl);
        mResult.putInt(KEY_CREATED_BUG_ID, reportId);
    }

    private void sendAttachment() throws Exception {
        publishProgress("Sending attachment ... ");
        boolean result = mServer.sendAttachment(Utility.getBundle(mParams, KEY_ATTACHMENT));
        if(error() || !result) {
            setTaskResult("Failed to send attachment!");
            throw new Exception();
        }
    }

    private void logout() throws Exception {
        publishProgress("Logging out ... ");
        boolean result = mServer.logout();
        if(error() || !result) {
            setTaskResult("Failed to logout!");
            throw new Exception();
        }
    }

    private void getHierarchy() throws Exception {
        publishProgress("Getting product and components list ... ");
        boolean result = mServer.getHierarchy();
        if(error() || !result) {
            setTaskResult("Failed to get product list");
            throw new Exception();
        }

        mResult.putBundle(KEY_PRODUCTS, mServer.getResult().getBundle(BugzillaAPI.KEY_RESULT_PRODUCTS));
    }

    private void getFields() throws Exception {
        publishProgress("Getting bug fields list ... ");
        boolean result = mServer.getFields();
        if(error() || !result) {
            setTaskResult("Failed to get bug fields");
            throw new Exception();
        }

        mResult.putBundle(KEY_FIELDS, mServer.getResult().getBundle(BugzillaAPI.KEY_RESULT_FIELDS));
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
            String message = bundle.getString(BugzillaAPI.KEY_RESULT_MESSAGE);
            mResult.putBoolean(KEY_ERROR, true);
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
