package com.intel.bugkoops.Network;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class HttpConnection {
    private final String LOG_TAG = HttpConnection.class.getSimpleName();

    private String mUserAgent;

    private int mResponseCode;
    private String mRequestResult;

    public HttpConnection(String userAgent) {
        mUserAgent = userAgent;
    }

    static boolean mNoSSLv3Enabled = false;

    public boolean post(String url, String content, String contentType) {
        return request(url, "POST", content, contentType, mUserAgent);
    }

    public boolean get(String url) {
        return request(url, "GET", null, null, mUserAgent);
    }

    public int getResponseCode() {
        return mResponseCode;
    }

    public String getRequestResult() {
        return mRequestResult;
    }

    private boolean request(String url, String method, String content, String contentType, String userAgent) {
        if (contentType == null) {
            contentType = "application/json; charset=utf-8";
        }

        Log.d(LOG_TAG, "Request to: " + url);
        Log.d(LOG_TAG, "Method: " + method);
        Log.d(LOG_TAG, "Content Type: " + contentType);
        if (content != null) {
            Log.d(LOG_TAG, "Content: " + content);
        } else {
            Log.d(LOG_TAG, "No content");
        }

        Uri builtUri = Uri.parse(url);
        if (builtUri.getScheme() == null || builtUri.getScheme().equals("https")) {
            return httpsRequest(url, method, content, contentType, userAgent);
        } else {
            return httpRequest(url, method, content, contentType, userAgent);
        }
    }

    private Boolean httpsRequest(String stringUrl, String method, String content, String contentType, String userAgent) {
        try {
            mRequestResult = "";
            mResponseCode = 0;

            URL url = new URL(stringUrl);

            try {
                if (mNoSSLv3Enabled) {
                    mNoSSLv3Enabled = true;
                    SSLContext sslContext = SSLContext.getInstance("TLSv1");

                    sslContext.init(null,
                            null,
                            null);
                    SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslContext.getSocketFactory());

                    HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "SSL error: ", e);
                return false;
            }
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", userAgent);

            connection.setDoInput(true);
            if (content != null) {
                connection.setDoOutput(true);

                byte[] contentData = content.getBytes("UTF-8");

                connection.setRequestProperty("Content-Type", contentType);
                connection.setRequestProperty("Content-Length", String.valueOf(contentData.length));

                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

                dataOutputStream.write(contentData);

                dataOutputStream.flush();
                dataOutputStream.close();
            }

            mResponseCode = connection.getResponseCode();

            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException exception) {
                inputStream = connection.getErrorStream();
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = input.readLine()) != null) {
                response.append(inputLine);
            }
            input.close();

            mRequestResult = response.toString();

            Log.d(LOG_TAG, "Response code: " + mResponseCode);
            Log.d(LOG_TAG, "Request result: " + mRequestResult);

            return true;
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "MalformedInputException", e);
        } catch (ProtocolException e) {
            Log.e(LOG_TAG, "ProtocolException", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException", e);
        }
        return false;
    }

    private Boolean httpRequest(String stringUrl, String method, String content, String contentType, String userAgent) {
        try {
            mRequestResult = "";
            mResponseCode = 0;

            URL url = new URL(stringUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", userAgent);

            connection.setDoInput(true);
            if (content != null) {
                connection.setDoOutput(true);

                byte[] contentData = content.getBytes("UTF-8");

                connection.setRequestProperty("Content-Type", contentType);
                connection.setRequestProperty("Content-Length", String.valueOf(contentData.length));

                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

                dataOutputStream.write(contentData);

                dataOutputStream.flush();
                dataOutputStream.close();
            }

            mResponseCode = connection.getResponseCode();

            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException exception) {
                inputStream = connection.getErrorStream();
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = input.readLine()) != null) {
                response.append(inputLine);
            }
            input.close();

            mRequestResult = response.toString();

            return true;
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "MalformedInputException", e);
        } catch (ProtocolException e) {
            Log.e(LOG_TAG, "ProtocolException", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException", e);
        }
        return false;
    }
}