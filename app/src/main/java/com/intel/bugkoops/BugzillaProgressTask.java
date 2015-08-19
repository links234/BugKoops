package com.intel.bugkoops;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class BugzillaProgressTask extends AsyncTask<String, Void, Boolean> {
    final String LOG_TAG = getClass().getSimpleName();
    final String USER_AGENT = "BugKoops";

    private ProgressDialog mDialog;
    private Activity mActivity;

    private String mHttpResult;
    private int mResponseCode;

    private String mTaskResult;

    public BugzillaProgressTask(Activity activity) {
        mActivity = activity;
        mDialog = new ProgressDialog(activity);

        mHttpResult = "";
        mResponseCode = 0;

        mTaskResult = null;
    }

    protected void onPreExecute() {
        mDialog.setTitle("Sending report to Bugzilla");
        mDialog.setMessage("Authenticating ... ");
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

            String url = "http://www.google.com/";

            if(httpGet(url)) {
                Log.d(LOG_TAG, "Result http = " + mHttpResult);
            } else {
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error", e);
        }
        return false;
    }

    private Boolean httpGet(String url) {
        try {
            mHttpResult = "";
            mResponseCode = 0;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
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

            mHttpResult = response.toString();

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
