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
    final String USER_AGENT = "Bug Koops";

    private OnTaskCompleted mListener;

    private ProgressDialog mDialog;
    private Activity mActivity;

    private String mTaskResult;

    private Bundle mParams;
    private Bundle mResult;

    private TextView mResultTextView;

    private String mServerUri;
    private BugzillaAPI mServer;

    private boolean mReloggedFromError;

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
        if (mParams != null) {
            mTask = mParams.getInt(KEY_TASK);
        } else {
            mTask = TASK_SEND;
        }
        mResult = new Bundle();
        mResult.putInt(KEY_TASK, mTask);

        mServerUri = Utility.getString(mParams, KEY_SERVER,
                Utility.getString(mParams.getBundle(KEY_SESSION), KEY_SERVER, DEFAULT_SERVER));

        mResultTextView = (TextView) mActivity.findViewById(R.id.bugzilla_send_status_textview);

        mListener = listener;
    }

    protected void onPreExecute() {
        mDialog.setTitle("Sending report to Bugzilla");
        mDialog.setMessage("Please wait ... ");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        mResultTextView.setText("");
        mResultTextView.setVisibility(View.GONE);
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mTaskResult == null) {
            if (!success) {
                mTaskResult = mActivity.getString(R.string.bugzilla_progress_task_unexpected_error);
            }
        }

        if (mTaskResult != null) {
            mResultTextView.setText(mTaskResult);
            mResultTextView.setVisibility(View.VISIBLE);
        }

        mListener.onTaskCompleted(mResult);
    }

    protected Boolean doInBackground(final String... args) {
        try {
            switch (mTask) {
                case TASK_SEND:
                    login();
                    send(true);
                    logout();
                    break;
                case TASK_LOGIN_GET_PRODUCTS_GET_COMPONENTS_GET_FIELDS:
                    login();
                    getHierarchy(true);
                    getFields(true);
                    saveSession();
                    break;
                case TASK_SESSION_SEND_LOGOUT:
                    loadSession();
                    send(true);
                    logout();
                    break;
                case TASK_SESSION_SEND_WITH_ATTACHMENT_LOGOUT:
                    loadSession();
                    send(true);
                    sendAttachment(true);
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
        publishProgress(mActivity.getString(R.string.bugzilla_progress_task_login_checking_version));
        BugzillaAutoDetect bugzillaAutoDetect = new BugzillaAutoDetect(mServerUri, USER_AGENT);

        mServer = bugzillaAutoDetect.open();

        if (mServer == null) {
            setTaskResult(mActivity.getString(R.string.bugzilla_progress_task_error_unsupported_server_api));
            throw new Exception();
        }

        mServer.version();
        publishProgress(mActivity.getString(R.string.bugzilla_progress_task_login_word_found) +
                " " + mServer.getAPIVersion() + " " +
                mActivity.getString(R.string.bugzilla_progress_task_login_word_version) +
                " " + mServer.getResult().getString(BugzillaAPI.KEY_VERSION));

        Thread.sleep(2000);

        publishProgress(mActivity.getString(R.string.bugzilla_progress_task_logging_in));
        boolean result = mServer.login(
                Utility.getString(mParams, KEY_LOGIN, DEFAULT_LOGIN),
                Utility.getString(mParams, KEY_PASSWORD, DEFAULT_PASSWORD));
        if (error(false) || !result) {
            setTaskResult(mActivity.getString(R.string.bugzilla_progress_task_error_login));
            throw new Exception();
        }
    }

    private boolean relogin() {
        publishProgress("Re-logging in ...");
        mServer.invalidate();
        boolean result = mServer.login(
                Utility.getString(mParams, KEY_LOGIN, DEFAULT_LOGIN),
                Utility.getString(mParams, KEY_PASSWORD, DEFAULT_PASSWORD));
        if (error(false) || !result) {
            setTaskResult(mActivity.getString(R.string.bugzilla_progress_task_error_login));
            return false;
        }
        return true;
    }

    private void send(boolean relogOnSessionExpired) throws Exception {
        publishProgress(mActivity.getString(R.string.bugzilla_progress_task_sending_report));
        boolean result = mServer.send(Utility.getBundle(mParams, KEY_REPORT));
        if (error(relogOnSessionExpired) || !result) {
            if (mReloggedFromError) {
                send(false);
            } else {
                setTaskResult(mActivity.getString(R.string.bugzilla_progress_task_error_send_report));
                throw new Exception();
            }
        }
        int reportId = mServer.getResult().getInt(BugzillaAPI.KEY_ID);

        String bugUrl = Uri.parse(mServerUri).buildUpon()
                .appendPath("show_bug.cgi")
                .appendQueryParameter(BugzillaAPI.KEY_ID, Integer.toString(reportId))
                .build().toString();

        mResult.putString(KEY_CREATED_BUG_URL, bugUrl);
        mResult.putInt(KEY_CREATED_BUG_ID, reportId);
    }

    private void sendAttachment(boolean relogOnSessionExpired) throws Exception {
        publishProgress(mActivity.getString(R.string.bugzilla_progress_task_sending_attachment));
        Bundle attachment = Utility.getBundle(mParams, KEY_ATTACHMENT);
        attachment.putInt(BugzillaAPI.KEY_ATTACHMENT_BUGID, mResult.getInt(KEY_CREATED_BUG_ID));
        boolean result = mServer.sendAttachment(attachment);
        if (error(relogOnSessionExpired) || !result) {
            if (mReloggedFromError) {
                sendAttachment(false);
            } else {
                setTaskResult(mActivity.getString(R.string.bugzilla_progress_task_error_send_attachment));
                throw new Exception();
            }
        }
    }

    private void logout() throws Exception {
        publishProgress(mActivity.getString(R.string.bugzilla_progress_task_logging_out));
        boolean result = mServer.logout();
        if (error(false) || !result) {
            setTaskResult(mActivity.getString(R.string.bugzilla_progress_task_error_logout));
            throw new Exception();
        }
    }

    private void getHierarchy(boolean relogOnSessionExpired) throws Exception {
        publishProgress(mActivity.getString(R.string.bugzilla_progress_task_get_hierarchy));
        boolean result = mServer.getHierarchy();
        if (error(relogOnSessionExpired) || !result) {
            if (mReloggedFromError) {
                getHierarchy(false);
            } else {
                setTaskResult(mActivity.getString(R.string.bugzilla_progress_task_error_get_hierarchy));
                throw new Exception();
            }
        }

        mResult.putBundle(KEY_PRODUCTS, mServer.getResult().getBundle(BugzillaAPI.KEY_RESULT_PRODUCTS));
    }

    private void getFields(boolean relogOnSessionExpired) throws Exception {
        publishProgress(mActivity.getString(R.string.bugzilla_progress_task_get_fields));
        boolean result = mServer.getFields();
        if (error(relogOnSessionExpired) || !result) {
            if (mReloggedFromError) {
                getFields(false);
            } else {
                setTaskResult(mActivity.getString(R.string.bugzilla_progress_task_error_get_fields));
                throw new Exception();
            }
        }

        mResult.putBundle(KEY_FIELDS, mServer.getResult().getBundle(BugzillaAPI.KEY_RESULT_FIELDS));
    }

    private void saveSession() {
        Bundle session = mServer.save();
        session.putString(KEY_SERVER, mServerUri);
        mResult.putBundle(KEY_SESSION, session);
    }

    private void loadSession() {
        BugzillaAutoDetect bugzillaAutoDetect = new BugzillaAutoDetect(
                mParams.getBundle(KEY_SESSION).getString(KEY_SERVER), USER_AGENT);

        mServer = bugzillaAutoDetect.restore(mParams.getBundle(KEY_SESSION));
    }

    private void setTaskResult(String result) {
        if (mTaskResult == null) {
            mTaskResult = result;
        }
    }

    private boolean error(boolean relogOnExpiredSession, Bundle bundle) {
        mReloggedFromError = false;
        if (bundle.getBoolean("error")) {
            String message = bundle.getString(BugzillaAPI.KEY_RESULT_MESSAGE);
            mResult.putBoolean(KEY_ERROR, true);
            if (message != null) {
                if (relogOnExpiredSession) {
                    if (message.toLowerCase().contains("token") &&
                            (message.toLowerCase().contains("not valid")
                                    || message.toLowerCase().contains("expired"))) {
                        if (relogin()) {
                            mReloggedFromError = true;
                            return true;
                        }
                        return false;
                    }
                }
                setTaskResult(message);
            }
            return true;
        }
        return false;
    }

    private boolean error(boolean relogOnExpiredSession) {
        return error(relogOnExpiredSession, mServer.getResult());
    }
}
