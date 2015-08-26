package com.intel.bugkoops.Integrations.Bugzilla;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.intel.bugkoops.Network.HttpConnection;
import com.intel.bugkoops.Utility;
import com.intel.bugkoops.XMLRPCBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class BugzillaXMLRPC implements BugzillaAPI {
    final String LOG_TAG = getClass().getSimpleName();

    static public final String API_VERSION = "XMLRPC";

    static private final String CONTENT_TYPE = "text/xml; charset=utf-8";

    private HttpConnection mHttpConnection;
    private String mServer;

    private String mToken;

    private String mUser;
    private String mPassword;

    private Bundle mResult;

    private String mVersion;

    public BugzillaXMLRPC(String server, String userAgent) {
        mHttpConnection = new HttpConnection(userAgent);

        mServer = server;

        mToken = null;

        mUser = null;
        mPassword = null;

        mVersion = null;

        mResult = new Bundle();
    }

    public boolean version() {
        if (mVersion == null) {
            Uri builtUri = Uri.parse(mServer).buildUpon()
                    .appendPath("xmlrpc.cgi")
                    .build();

            XMLRPCBuilder request = new XMLRPCBuilder();

            request.start("Bugzilla.version");
            request.startStruct();
            request.endStruct();
            request.end();

            Log.d(LOG_TAG, "Post body = " + request.toString());

            if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
                return false;
            }
            Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

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
                .appendPath("xmlrpc.cgi")
                .build();

        XMLRPCBuilder request = new XMLRPCBuilder();

        request.start("User.login");
        request.startStruct();
        request.member("login", user);
        request.member("password", password);
        request.endStruct();
        request.end();

        Log.d(LOG_TAG, "Post body = " + request.toString());

        if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

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
        if (mToken == null) {
            return false;
        }

        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("xmlrpc.cgi")
                .build();

        XMLRPCBuilder request = new XMLRPCBuilder();

        request.start("User.logout");
        request.startStruct();
        request.member("token", mToken);
        request.endStruct();
        request.end();

        Log.d(LOG_TAG, "Post body = " + request.toString());

        if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        mToken = null;

        mUser = null;
        mPassword = null;

        return true;
    }

    public boolean send(Bundle report) {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("xmlrpc.cgi")
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

        XMLRPCBuilder request = new XMLRPCBuilder();

        request.start("Bug.create");
        request.startStruct();
        request.member(KEY_TOKEN, mToken);
        request.member(KEY_PRODUCT, product);
        request.member(KEY_COMPONENT, component);
        request.member(KEY_VERSION, version);
        request.member(KEY_SUMMARY, summary);
        request.member(KEY_DESCRIPTION, description);
        request.member(KEY_OS, op_sys);
        request.member(KEY_PLATFORM, platform);
        request.member(KEY_PRIORITY, priority);
        request.member(KEY_SEVERITY, severity);
        request.endStruct();
        request.end();

        Log.d(LOG_TAG, "Post body = " + request.toString());

        if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
            return false;
        }

        Log.d(LOG_TAG, "Result get = " + mHttpConnection.getRequestResult());

        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        return true;
    }

    public boolean sendAttachment(Bundle attachment) {
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
        return true;
    }

    public boolean getFields() {
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
        Log.d(LOG_TAG, "message = " + message);
        mResult.putBoolean(KEY_RESULT_ERROR, true);
        mResult.putString(KEY_RESULT_MESSAGE, message);
    }

    private boolean translate(String data) {
        mResult = new Bundle();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(new InputSource(new StringReader(data)));
            Element root = dom.getDocumentElement();
            NodeList items = root.getElementsByTagName("member");
            for (int i = 0; i < items.getLength(); ++i) {
                Node item = items.item(i);
                NodeList properties = item.getChildNodes();

                String name = null;
                String type = null;
                String value = null;

                for (int j = 0; j < properties.getLength(); ++j) {
                    Node property = properties.item(j);
                    String propName = property.getNodeName().toLowerCase();

                    if (propName.equalsIgnoreCase("name")) {
                        name = property.getFirstChild().getNodeValue().toLowerCase();
                    } else if (propName.equalsIgnoreCase("value")) {
                        type = property.getFirstChild().getNodeName();

                        if (type == null) {
                            value = property.getFirstChild().getNodeValue();
                        } else {
                            value = property.getFirstChild().getFirstChild().getNodeValue();
                        }
                    }
                }

                if (name == null || value == null) {
                    setError("Invalid XMLRPC response: <name,value> pair is incomplete");
                    return false;
                }

                if (name.equalsIgnoreCase("faultstring")) {
                    name = KEY_RESULT_MESSAGE;
                    mResult.putBoolean(KEY_RESULT_ERROR, true);
                } else if (name.equalsIgnoreCase("faultcode")) {
                    name = "code";
                    mResult.putBoolean(KEY_RESULT_ERROR, true);
                }

                if (type == null || type.equalsIgnoreCase("string")) {
                    mResult.putString(name, value);
                } else if (type.equalsIgnoreCase("int") ||
                        type.equalsIgnoreCase("i4")) {
                    mResult.putInt(name, Integer.valueOf(value));
                } else if (type.equalsIgnoreCase("boolean")) {
                    mResult.putBoolean(name, Boolean.valueOf(value));
                }
            }

            if (mResult.getBoolean(KEY_RESULT_ERROR)) {
                return false;
            }
        } catch (Exception e) {
            setError("Invalid XMLRPC response: " + e.toString());
            return false;
        }
        return true;
    }
}
