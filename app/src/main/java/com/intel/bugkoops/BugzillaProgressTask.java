package com.intel.bugkoops;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class BugzillaProgressTask extends AsyncTask<String, String, Boolean> {
    final String LOG_TAG = getClass().getSimpleName();
    final String USER_AGENT = "BugKoops";

    private ProgressDialog mDialog;
    private Activity mActivity;

    private String mGetResult;
    private int mResponseCode;

    private String mTaskResult;

    private String mServer;
    private String mToken;

    private String mUser;
    private String mPassword;

    public BugzillaProgressTask(Activity activity) {
        mActivity = activity;
        mDialog = new ProgressDialog(activity);

        mGetResult = "";
        mResponseCode = 0;

        mTaskResult = null;

        mServer = "https://landfill.bugzilla.org/bugzilla-5.0-branch/";

        mUser = "Here comes your default account login";
        mPassword = "Here comes your default account password";
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
                Toast.makeText(mActivity, "Report sent!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mActivity, "Unexpected error while sending report!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mActivity, mTaskResult, Toast.LENGTH_LONG).show();
        }
    }

    protected Boolean doInBackground(final String... args) {
        try {
            if(!login()) {
                setTaskResult("Failed to login!");
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

        if(!get(builtUri.toString())) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mGetResult);

        return true;
    }

    private boolean logout() {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("logout")
                .appendQueryParameter("token", mToken)
                .build();

        if(!get(builtUri.toString())) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mGetResult);

        try {
            JSONObject json = new JSONObject(mGetResult);

            if(errorResponse(json)) {
                return false;
            }
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return false;
        }

        return true;
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
            setTaskResult("Unexpected exception!");
            return false;
        }
        return true;
    }

    private void setTaskResult(String result) {
        if(mTaskResult == null) {
            mTaskResult = result;
        }
    }

    private boolean get(String url) {
        return request(url, "GET");
    }

    private boolean request(String url, String method) {
        Uri builtUri = Uri.parse(url);
        if(builtUri.getScheme() == null || builtUri.getScheme().equals("https")) {
            return httpsRequest(url, method);
        } else {
            return httpRequest(url, method);
        }
    }

    private Boolean httpsRequest(String url, String method) {
        try {
            mGetResult = "";
            mResponseCode = 0;

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            con.setRequestMethod(method);
            con.setRequestProperty("User-Agent", USER_AGENT);

            mResponseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            mGetResult = response.toString();

            return true;
        } catch(MalformedURLException e) {
            Log.e(LOG_TAG, "MalformedInputException", e);
        } catch(ProtocolException e) {
            Log.e(LOG_TAG, "ProtocolException", e);
        } catch(IOException e) {
            Log.e(LOG_TAG, "IOException", e);
        }
        return false;
    }

    private Boolean httpRequest(String url, String method) {
        try {
            mGetResult = "";
            mResponseCode = 0;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod(method);
            con.setRequestProperty("User-Agent", USER_AGENT);

            mResponseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            mGetResult = response.toString();

            return true;
        } catch(MalformedURLException e) {
            Log.e(LOG_TAG, "MalformedInputException", e);
        } catch(ProtocolException e) {
            Log.e(LOG_TAG, "ProtocolException", e);
        } catch(IOException e) {
            Log.e(LOG_TAG, "IOException", e);
        }
        return false;
    }
}
