package com.intel.bugkoops.Integrations.Bugzilla;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.intel.bugkoops.Network.HttpConnection;
import com.intel.bugkoops.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class BugzillaREST implements BugzillaAPI {
    final String LOG_TAG = getClass().getSimpleName();

    static public final String API_VERSION = "REST";

    static private final String CONTENT_TYPE = "text/plain; charset=utf-8";

    private HttpConnection mHttpConnection;
    private String mServer;

    private String mToken;

    private String mUser;
    private String mPassword;

    private Bundle mResult;

    private String mVersion;

    public BugzillaREST(String server, String userAgent) {
        mHttpConnection = new HttpConnection(userAgent);

        mServer = server;

        mToken = null;

        mUser = null;
        mPassword = null;

        mVersion = null;
    }

    public boolean version() {
        if (mVersion == null) {
            Uri builtUri = Uri.parse(mServer).buildUpon()
                    .appendPath("rest")
                    .appendPath("version")
                    .build();

            if (!mHttpConnection.get(builtUri.toString())) {
                return false;
            }

            if (!translate(mHttpConnection.getRequestResult())) {
                return false;
            }

            mVersion = mResult.getString(KEY_VERSION);
            return true;
        }

        mResult = new Bundle();
        mResult.putString(KEY_VERSION, mVersion);
        return true;
    }

    public boolean login(String user, String password) {
        if (mToken != null && !user.equals(mUser) && !password.equals(mPassword)) {
            logout();
        }

        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("login")
                .appendQueryParameter("login", user)
                .appendQueryParameter("password", password)
                .build();

        if (!mHttpConnection.get(builtUri.toString())) {
            return false;
        }

        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        if (mResult.getString(KEY_TOKEN) == null) {
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

        if (!mHttpConnection.get(builtUri.toString())) {
            return false;
        }

        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        mToken = null;

        mUser = null;
        mPassword = null;

        mResult = new Bundle();

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
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return false;
        }

        if (!mHttpConnection.post(builtUri.toString(), jsonRequest.toString(), CONTENT_TYPE)) {
            return false;
        }


        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        return true;
    }

    public boolean sendAttachment(Bundle attachment) {
        String data = Utility.getString(attachment, KEY_ATTACHMENT_DATA);
        int bugId = Utility.getInt(attachment, KEY_ATTACHMENT_BUGID, -1);
        String contentType = Utility.getString(attachment, KEY_ATTACHMENT_CONTENT_TYPE, DEFAULT_ATTACHMENT_CONTENT_TYPE);

        if (bugId == -1) {
            setError("There is no bug id associated with the attachment");
        }

        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("bug")
                .appendPath(Integer.toString(bugId))
                .appendPath("attachment")
                .appendQueryParameter("token", mToken)
                .build();

        JSONObject jsonRequest = new JSONObject();
        try {
            JSONArray bugIds = new JSONArray();
            bugIds.put(bugId);

            jsonRequest.put("ids", bugIds);
            jsonRequest.put("data", Base64.encodeToString(data.getBytes(), Base64.DEFAULT));
            jsonRequest.put("file_name", DEFAULT_ATTACHMENT_FILE_NAME);
            jsonRequest.put("summary", DEFAULT_ATTACHMENT_SUMMARY);
            jsonRequest.put("content_type", contentType);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            return false;
        }

        if (!mHttpConnection.post(builtUri.toString(), jsonRequest.toString(), CONTENT_TYPE)) {
            return false;
        }


        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }
        return true;
    }

    public void restore(Bundle session) {
        mToken = session.getString(KEY_TOKEN);
        mUser = session.getString(KEY_USER);
        mPassword = session.getString(KEY_PASSWORD);
    }

    public Bundle save() {
        Bundle session = new Bundle();
        session.putString(KEY_TOKEN, mToken);
        session.putString(KEY_USER, mUser);
        session.putString(KEY_PASSWORD, mPassword);
        session.putString(KEY_API, API_VERSION);
        return session;
    }

    public boolean getHierarchy() {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("product")
                .appendQueryParameter("token", mToken)
                .appendQueryParameter("type", "enterable")
                .build();

        if (!mHttpConnection.get(builtUri.toString())) {
            return false;
        }

        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        return true;
    }

    public boolean getFields() {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("rest")
                .appendPath("field")
                .appendPath("bug")
                .appendQueryParameter("token", mToken)
                .build();

        if (!mHttpConnection.get(builtUri.toString())) {
            return false;
        }

        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        return true;
    }

    public String getAPIVersion() {
        return API_VERSION;
    }

    public Bundle getResult() {
        return mResult;
    }

    private void setError(String message) {
        mResult = new Bundle();
        mResult.putBoolean(KEY_RESULT_ERROR, true);
        mResult.putString(KEY_RESULT_MESSAGE, message);
    }

    private boolean translate(String string) {
        try {
            JSONObject jsonResult = new JSONObject(string);

            if (!translate(jsonResult)) {
                return false;
            }
        } catch (JSONException e) {
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

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (json.get(key) instanceof Integer) {
                    mResult.putInt(key, json.getInt(key));
                } else if (json.get(key) instanceof String) {
                    mResult.putString(key, json.getString(key));
                } else if (json.get(key) instanceof Boolean) {
                    mResult.putBoolean(key, json.getBoolean(key));
                } else if (json.get(key) instanceof JSONArray && key.equalsIgnoreCase("products")) {
                    Bundle productsBundle = new Bundle();
                    JSONArray jsonProductArray = json.getJSONArray("products");
                    for (int product = 0; product < jsonProductArray.length(); ++product) {
                        JSONObject jsonProduct = jsonProductArray.getJSONObject(product);
                        String productName = jsonProduct.getString("name");

                        Bundle productBundle = new Bundle();
                        Bundle componentsBundle = new Bundle();
                        JSONArray jsonComponentArray = jsonProduct.getJSONArray("components");
                        for (int component = 0; component < jsonComponentArray.length(); ++component) {
                            JSONObject jsonComponent = jsonComponentArray.getJSONObject(component);
                            String componentName = jsonComponent.getString("name");

                            Bundle componentBundle = new Bundle();
                            componentBundle.putString(KEY_RESULT_NAME, componentName);

                            int sortKey = 0;
                            if (jsonComponent.has("sort_key")) {
                                sortKey = jsonComponent.getInt("sort_key");
                            }

                            componentBundle.putInt("sort_key", sortKey);

                            componentsBundle.putBundle(componentName, componentBundle);
                        }
                        productBundle.putBundle(KEY_RESULT_COMPONENTS, componentsBundle);
                        productBundle.putString(KEY_RESULT_NAME, productName);

                        productsBundle.putBundle(productName, productBundle);
                    }
                    mResult.putBundle("products", productsBundle);
                } else if (json.get(key) instanceof JSONArray && key.equalsIgnoreCase("fields")) {
                    Bundle fieldsBundle = new Bundle();
                    JSONArray jsonFieldArray = json.getJSONArray("fields");
                    for (int field = 0; field < jsonFieldArray.length(); ++field) {
                        JSONObject jsonField = jsonFieldArray.getJSONObject(field);
                        if (!jsonField.has("values")) {
                            continue;
                        }
                        String fieldName = jsonField.getString("name");

                        Bundle fieldBundle = new Bundle();
                        fieldBundle.putString(KEY_RESULT_NAME, fieldName);

                        Bundle valuesBundle = new Bundle();
                        JSONArray jsonValuesArray = jsonField.getJSONArray("values");
                        for (int value = 0; value < jsonValuesArray.length(); ++value) {
                            JSONObject jsonValue = jsonValuesArray.getJSONObject(value);
                            String valueName = jsonValue.getString("name");

                            int sortKey = 0;
                            if (jsonValue.has("sort_key")) {
                                sortKey = jsonValue.getInt("sort_key");
                            }

                            Bundle valueBundle = new Bundle();
                            valueBundle.putString(KEY_RESULT_NAME, valueName);
                            valueBundle.putInt(KEY_RESULT_SORTKEY, sortKey);

                            valuesBundle.putBundle(valueName, valueBundle);
                        }
                        fieldBundle.putBundle(KEY_RESULT_VALUES, valuesBundle);

                        fieldsBundle.putBundle(translateField(fieldName), fieldBundle);
                    }

                    mResult.putBundle("fields", fieldsBundle);
                }
            }

            if (mResult.getBoolean(KEY_RESULT_ERROR)) {
                return false;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException", e);
            setError("Server response is corupted!");
            return false;
        }
        return true;
    }

    private String translateField(String field) {
        if (field.equals("rep_platform")) {
            return KEY_RESULT_PLATFORM;
        } else if (field.equals("bug_severity")) {
            return KEY_RESULT_SEVERITY;
        }
        return field;
    }
}
