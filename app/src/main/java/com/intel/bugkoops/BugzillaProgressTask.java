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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

            publishProgress("Sending report ... ");
            if(!send()) {
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

        if(!get(builtUri.toString())) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mGetResult);

        try {
            JSONObject json = new JSONObject(mGetResult);

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

    private boolean send() {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("bug")
                .appendQueryParameter("token", mToken)
                .build();

        String product = "FoodReplicator";
        String component = "Salt";
        String version = "1.0";
        String summary = "This is a test bug - please disregard";
        String description = "This is a description";
        String op_sys = "All";
        String platform = "All";

        JSONObject jsonRequest = new JSONObject();

        try {
            jsonRequest.put("product", product);
            jsonRequest.put("component", component);
            jsonRequest.put("version", version);
            jsonRequest.put("summary", summary);
            jsonRequest.put("op_sys", op_sys);
            jsonRequest.put("description", description);
          //  jsonRequest.put("priority", priority);
            jsonRequest.put("platform", platform);
           // jsonRequest.put("severity", severity);
        } catch(JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return false;
        }

        Log.d(LOG_TAG, "Post body = " + jsonRequest.toString());

        if(!post(builtUri.toString(), jsonRequest.toString())) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mGetResult);

        try {
            JSONObject jsonResult = new JSONObject(mGetResult);

            if(errorResponse(jsonResult)) {
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

    private boolean post(String url, String data) {
        return request(url, "POST", data);
    }

    private boolean get(String url) {
        return request(url, "GET", null);
    }

    private boolean request(String url, String method, String data) {
        Uri builtUri = Uri.parse(url);
        if(builtUri.getScheme() == null || builtUri.getScheme().equals("https")) {
            return httpsRequest(url, method, data);
        } else {
            return httpRequest(url, method, data);
        }
    }

    private Boolean httpsRequest(String url, String method, String data) {
        try {
            mGetResult = "";
            mResponseCode = 0;

            URL obj = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", USER_AGENT);

            connection.setDoInput(true);
            if(data != null) {
                connection.setDoOutput(true);

                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Length",  String.valueOf(data.length()));

                OutputStream outputStream = connection.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");

                outputStreamWriter.write(data);

                outputStreamWriter.flush();
                outputStream.close();
            }

            mResponseCode = connection.getResponseCode();

            BufferedReader input;
            if (mResponseCode == HttpURLConnection.HTTP_OK) {
                input = new BufferedReader( new InputStreamReader(
                        connection.getInputStream()));
            } else {
                input = new BufferedReader( new InputStreamReader(
                        connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = input.readLine()) != null) {
                response.append(inputLine);
            }
            input.close();

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

    private Boolean httpRequest(String url, String method, String data) {
        try {
            mGetResult = "";
            mResponseCode = 0;

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", USER_AGENT);

            connection.setDoInput(true);
            if(data != null) {
                connection.setDoOutput(true);

                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Length",  String.valueOf(data.length()));

                OutputStream outputStream = connection.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");

                outputStreamWriter.write(data);

                outputStreamWriter.flush();
                outputStream.close();
            }

            mResponseCode = connection.getResponseCode();

            BufferedReader input;
            if (mResponseCode == HttpURLConnection.HTTP_OK) {
                input = new BufferedReader( new InputStreamReader(
                        connection.getInputStream()));
            } else {
                input = new BufferedReader( new InputStreamReader(
                        connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = input.readLine()) != null) {
                response.append(inputLine);
            }
            input.close();

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
