package com.intel.bugkoops;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.intel.bugkoops.Network.HttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class BugzillaProgressTask extends AsyncTask<String, String, Boolean> {
    final String LOG_TAG = getClass().getSimpleName();
    final String USER_AGENT = "BugKoops";

    private ProgressDialog mDialog;
    private Activity mActivity;

    private HttpConnection mHttpConnection;

    private String mTaskResult;

    private String mServer;
    private String mToken;

    private String mUser;
    private String mPassword;

    private JSONObject mJsonReport;

    private Snackbar mResultSnackbar;

    private static final String DEFAULT_LOGIN = "Here comes your default account login";  //CHANGE THIS IN FINAL VERSION
    private static final String DEFAULT_PASSWORD = "Here comes your default account password";          //CHANGE THIS IN FINAL VERSION -- mai ales asta :)
    public static final String DEFAULT_SERVER = "https://landfill.bugzilla.org/bugzilla-5.0-branch/"; //CHANGE THIS IN FINAL VERSION
    public static final String DEFAULT_PRODUCT = "FoodReplicator";         //CHANGE THIS IN FINAL VERSION
    public static final String DEFAULT_COMPONENT = "Salt";            //CHANGE THIS IN FINAL VERSION
    public static final String DEFAULT_VERSION = "1.0";          //CHANGE THIS IN FINAL VERSION
    public static final String DEFAULT_SUMMARY = "Bug Koops generated bug report";
    public static final String DEFAULT_DESCRIPTION = "";         //CHANGE THIS IN FINAL VERSION
    public static final String DEFAULT_OS = "All";
    public static final String DEFAULT_PLATFORM = "All";
    public static final String DEFAULT_PRIORITY = "P1";
    public static final String DEFAULT_SEVERITY = "critical";

    public static final String LOGIN_KEY = "login";
    public static final String PASSWORD_KEY = "password";
    public static final String SERVER_KEY = "server";
    public static final String PRODUCT_KEY = "key";
    public static final String COMPONENT_KEY = "component";
    public static final String VERSION_KEY = "summary";
    public static final String SUMMARY_KEY = "version";
    public static final String DESCRIPTION_KEY = "description";
    public static final String OS_KEY = "op_sys";
    public static final String PLATFORM_KEY = "platform";
    public static final String PRIORITY_KEY = "priority";
    public static final String SEVERITY_KEY = "severity";

    public BugzillaProgressTask(Activity activity, JSONObject jsonReport) {
        mActivity = activity;
        mDialog = new ProgressDialog(mActivity);

        mHttpConnection = new HttpConnection(USER_AGENT);

        mTaskResult = null;

        mJsonReport = jsonReport;

        mServer = getReportAttribute(SERVER_KEY, DEFAULT_SERVER);

        mUser = getReportAttribute(LOGIN_KEY, DEFAULT_LOGIN);
        mPassword = getReportAttribute(PASSWORD_KEY, DEFAULT_PASSWORD);

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
        mDialog.setMessage("Logging in ... ");
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
            if(!login()) {
                setTaskResult("Failed to login!");
                return false;
            }

            publishProgress("Sending report ... ");
            if(!send()) {
                publishProgress("Logging out ... ");
                logout();
                setTaskResult("Failed to send report!");
                return false;
            }

            publishProgress("Logging out ... ");
            if(!logout()) {
                setTaskResult("Failed to logout!");
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error", e);
        }
        return false;
    }

    protected void onProgressUpdate(String... dialogText) {
        mDialog.setMessage(dialogText[0]);
    }

    private boolean login() {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("login")
                .appendQueryParameter("login", mUser)
                .appendQueryParameter("password", mPassword)
                .build();

        if(!mHttpConnection.get(builtUri.toString())) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

        try {
            JSONObject json = new JSONObject(mHttpConnection.getRequestResult());

            if(errorResponse(json)) {
                return false;
            }

            mToken = json.getString("token");
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return false;
        }

        return true;
    }

    private boolean logout() {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("logout")
                .appendQueryParameter("token", mToken)
                .build();

        if(!mHttpConnection.get(builtUri.toString())) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

        try {
            JSONObject json = new JSONObject(mHttpConnection.getRequestResult());

            if(errorResponse(json)) {
                return false;
            }
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return false;
        }

        return true;
    }

    private boolean send() {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("bug")
                .appendQueryParameter("token", mToken)
                .build();

        String product = getReportAttribute(PRODUCT_KEY, DEFAULT_PRODUCT);
        String component = getReportAttribute(COMPONENT_KEY, DEFAULT_COMPONENT);
        String version = getReportAttribute(VERSION_KEY, DEFAULT_VERSION);
        String summary = getReportAttribute(SUMMARY_KEY, DEFAULT_SUMMARY);
        String description = getReportAttribute(DESCRIPTION_KEY, DEFAULT_DESCRIPTION);
        String op_sys = getReportAttribute(OS_KEY, DEFAULT_OS);
        String platform = getReportAttribute(PLATFORM_KEY, DEFAULT_PLATFORM);
        String priority = getReportAttribute(PRIORITY_KEY, DEFAULT_PRIORITY);
        String severity = getReportAttribute(SEVERITY_KEY, DEFAULT_SEVERITY);

        JSONObject jsonRequest = new JSONObject();

        try {
            jsonRequest.put("product", product);
            jsonRequest.put("component", component);
            jsonRequest.put("version", version);
            jsonRequest.put("summary", summary);
            jsonRequest.put("op_sys", op_sys);
            jsonRequest.put("description", description);
            jsonRequest.put("priority", priority);
            jsonRequest.put("platform", platform);
            jsonRequest.put("severity", severity);
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return false;
        }

        Log.d(LOG_TAG, "Post body = " + jsonRequest.toString());

        if(!mHttpConnection.post(builtUri.toString(), jsonRequest.toString(), "application/json")) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

        try {
            JSONObject jsonResult = new JSONObject(mHttpConnection.getRequestResult());

            if(errorResponse(jsonResult)) {
                return false;
            }
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return false;
        }

        return true;
    }

    private String getReportAttribute(String key, String defaultValue) {
        if(mJsonReport == null) {
            return defaultValue;
        }
        try {
            if(mJsonReport.has(key)) {
                return defaultValue;
            }
            return mJsonReport.getString(key);
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return "";
        }
    }

    private boolean errorResponse(JSONObject json) {
        try {
            if(!json.has("error")) {
                return false;
            }
            if(!json.getBoolean("error")) {
                return false;
            }
            if(json.has("code")) {
                Log.e(LOG_TAG, "Bugzilla error response with code: " + Integer.toString(
                        json.getInt("code")));
            }
            if(json.has("message")) {
                setTaskResult(json.getString("message"));
            }
        } catch(JSONException e) {
            setTaskResult("Error! Server response is malformed!");
            return false;
        }
        return true;
    }

    private void setTaskResult(String result) {
        if(mTaskResult == null) {
            mTaskResult = result;
        }
    }
}
