package com.intel.bugkoops.Network;

import android.net.Uri;
import android.util.Log;

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

public class HttpConnection {
    private final String LOG_TAG = HttpConnection.class.getSimpleName();

    private String mUserAgent;

    private int mResponseCode;
    private String mRequestResult;

    public HttpConnection(String userAgent) {
        mUserAgent = userAgent;
    }

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
        if(contentType == null) {
            contentType = "application/json";
        }

        Uri builtUri = Uri.parse(url);
        if(builtUri.getScheme() == null || builtUri.getScheme().equals("https")) {
            return httpsRequest(url, method, content, contentType, userAgent);
        } else {
            return httpRequest(url, method, content, contentType, userAgent);
        }
    }

    private Boolean httpsRequest(String url, String method, String content, String contentType, String userAgent) {
        try {
            mRequestResult = "";
            mResponseCode = 0;

            URL obj = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", userAgent);

            connection.setDoInput(true);
            if(content != null) {
                connection.setDoOutput(true);

                connection.setRequestProperty("Content-Type", contentType);
                connection.setRequestProperty("Content-Length",  String.valueOf(content.length()));

                OutputStream outputStream = connection.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");

                outputStreamWriter.write(content);

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

            mRequestResult = response.toString();

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

    private Boolean httpRequest(String url, String method, String content, String contentType, String userAgent) {
        try {
            mRequestResult = "";
            mResponseCode = 0;

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", userAgent);

            connection.setDoInput(true);
            if(content != null) {
                connection.setDoOutput(true);

                connection.setRequestProperty("Content-Type", contentType);
                connection.setRequestProperty("Content-Length",  String.valueOf(content.length()));

                OutputStream outputStream = connection.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");

                outputStreamWriter.write(content);

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

            mRequestResult = response.toString();

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