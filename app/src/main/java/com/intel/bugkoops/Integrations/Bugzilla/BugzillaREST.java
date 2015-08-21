package com.intel.bugkoops.Integrations.Bugzilla;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.intel.bugkoops.Network.HttpConnection;
import com.intel.bugkoops.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class BugzillaREST implements BugzillaAPI{
    final String LOG_TAG = getClass().getSimpleName();

    private HttpConnection mHttpConnection;
    private String mServer;

    private String mToken;

    private String mUser;
    private String mPassword;

    private Bundle mResult;

    public BugzillaREST(String server, String userAgent) {
        mHttpConnection = new HttpConnection(userAgent);

        mServer = server;

        mToken = null;

        mUser = null;
        mPassword = null;
    }

    public boolean login(String user, String password) {
        if(mToken != null && !user.equals(mUser) && !password.equals(mPassword)) {
            logout();
        }

        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("login")
                .appendQueryParameter("login", user)
                .appendQueryParameter("password", password)
                .build();

        if(!mHttpConnection.get(builtUri.toString())) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

        if(!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        if(mResult.getString(KEY_TOKEN) == null) {
            setError("Login response does not contain any token");
            return false;
        }

        mToken = mResult.getString(KEY_TOKEN);

        mUser = user;
        mPassword = password;

        return true;
    }

    public boolean logout() {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("logout")
                .appendQueryParameter("token", mToken)
                .build();

        if(!mHttpConnection.get(builtUri.toString())) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

        if(!translate(mHttpConnection.getRequestResult()))
        {
            return false;
        }

        mToken = null;

        mUser = null;
        mPassword = null;

        return true;
    }

    public boolean send(Bundle report) {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("bug")
                .appendQueryParameter("token", mToken)
                .build();

        String product = Utility.getString(report, KEY_PRODUCT, DEFAULT_PRODUCT);
        String component = Utility.getString(report, KEY_COMPONENT, DEFAULT_COMPONENT);
        String version = Utility.getString(report, KEY_VERSION, DEFAULT_VERSION);
        String summary = Utility.getString(report, KEY_SUMMARY, DEFAULT_SUMMARY);
        String description = Utility.getString(report, KEY_DESCRIPTION, DEFAULT_DESCRIPTION);
        String op_sys = Utility.getString(report, KEY_OS, DEFAULT_OS);
        String platform = Utility.getString(report, KEY_PLATFORM, DEFAULT_PLATFORM);
        String priority = Utility.getString(report, KEY_PRIORITY, DEFAULT_PRIORITY);
        String severity = Utility.getString(report, KEY_SEVERITY, DEFAULT_SEVERITY);

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(KEY_PRODUCT, product);
            jsonRequest.put(KEY_COMPONENT, component);
            jsonRequest.put(KEY_VERSION, version);
            jsonRequest.put(KEY_SUMMARY, summary);
            jsonRequest.put(KEY_OS, op_sys);
            jsonRequest.put(KEY_DESCRIPTION, description);
            jsonRequest.put(KEY_PRIORITY, priority);
            jsonRequest.put(KEY_PLATFORM, platform);
            jsonRequest.put(KEY_SEVERITY, severity);
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return false;
        }

        Log.d(LOG_TAG, "Post body = " + jsonRequest.toString());

        if(!mHttpConnection.post(builtUri.toString(), jsonRequest.toString(), "application/json")) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

        if(!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        return true;
    }

    public Bundle getResult() {
        return mResult;
    }

    private void setError(String message) {
        mResult = new Bundle();
        Log.d(LOG_TAG, "message = " + message);
        mResult.putBoolean(KEY_ERROR, true);
        mResult.putString(KEY_MESSAGE, message);
    }

    private boolean translate(String string) {
        try {
            JSONObject jsonResult = new JSONObject(string);

            if(!translate(jsonResult)) {
                return false;
            }
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            setError("Server response is corrupted!");
            return false;
        }
        return true;
    }

    private boolean translate(JSONObject json) {
        mResult = new Bundle();
        try {
            Iterator<?> keys = json.keys();

            while( keys.hasNext() ) {
                String key = (String)keys.next();
                if(json.get(key) instanceof String) {
                    mResult.putString(key, json.getString(key));
                } else if(json.get(key) instanceof Boolean) {
                    mResult.putBoolean(key, json.getBoolean(key));
                }
            }
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            setError("Server response is corupted!");
            return false;
        }
        return true;
    }
}
