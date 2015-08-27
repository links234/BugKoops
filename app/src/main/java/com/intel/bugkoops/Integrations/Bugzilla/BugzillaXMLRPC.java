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
import java.util.ArrayList;
import java.util.Set;

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

            if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
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
                .appendPath("xmlrpc.cgi")
                .build();

        XMLRPCBuilder request = new XMLRPCBuilder();

        request.start("User.login");
        request.startStruct();
        request.member("login", user);
        request.member("password", password);
        request.endStruct();
        request.end();

        if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
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

        if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
            return false;
        }

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

        if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
            return false;
        }

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
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("xmlrpc.cgi")
                .build();

        XMLRPCBuilder request = new XMLRPCBuilder();

        request.start("Product.get_enterable_products");
        request.startStruct();
        request.member(KEY_TOKEN, mToken);
        request.endStruct();
        request.end();

        if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
            return false;
        }

        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        Bundle ids = mResult.getBundle("ids");

        if (ids == null) {
            return false;
        }

        request.start("Product.get");
        request.startStruct();
        request.startArray("ids");
        for (String key : ids.keySet()) {
            if (!(ids.get(key) instanceof Integer)) {
                return false;
            }
            request.putValue(ids.getInt(key));
        }
        request.endArray();
        request.endStruct();
        request.end();

        if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
            return false;
        }

        if (!translate(mHttpConnection.getRequestResult())) {
            return false;
        }

        Bundle products = mResult.getBundle("products");
        if (products == null) {
            setError("There is no \"products\" field!");
            return false;
        }


        ArrayList<String> productKeys = new ArrayList<>();
        for (String key : products.keySet()) {
            productKeys.add(key);
        }

        for (String productKey : productKeys) {
            Log.d(LOG_TAG, "productKey = " + productKey);

            Bundle product = products.getBundle(productKey);
            if (product == null) {
                setError("Not a valid product structure.");
                return false;
            }
            String productName = Utility.getString(product, "name", "");

            Log.d(LOG_TAG, "Product name = " + productName);

            Bundle components = product.getBundle("components");
            if (components == null) {
                setError("There is no \"components\" field!");
                return false;
            }

            ArrayList<String> componentKeys = new ArrayList<>();
            for (String key : components.keySet()) {
                componentKeys.add(key);
            }

            for (String componentKey : componentKeys) {

                Log.d(LOG_TAG, "componentKey = " + componentKey);
                Bundle component = components.getBundle(componentKey);
                String componentName = Utility.getString(component, "name", "");

                Log.d(LOG_TAG, "Component Name = " + componentName);

                components.remove(componentKey);
                components.putBundle(componentName, component);
            }
            product.putBundle("components", components);

            products.remove(productKey);
            products.putBundle(productName, product);
        }

        mResult.putBundle("products", products);

        return true;
    }

    public boolean getFields() {
        Uri builtUri = Uri.parse(mServer).buildUpon()
                .appendPath("xmlrpc.cgi")
                .build();

        XMLRPCBuilder request = new XMLRPCBuilder();

        request.start("Bug.fields");
        request.startStruct();
        request.member(KEY_TOKEN, mToken);
        request.endStruct();
        request.end();

        if (!mHttpConnection.post(builtUri.toString(), request.toString(), CONTENT_TYPE)) {
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

    private boolean translate(String data) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(new InputSource(new StringReader(data)));
            Element root = dom.getDocumentElement();

            if (root.getFirstChild().getNodeName().equalsIgnoreCase("fault")) {
                NodeList items = root.getFirstChild().getFirstChild().getFirstChild().getChildNodes();

                mResult = translate(items);
                return false;
            } else if (root.getFirstChild().getFirstChild().getFirstChild().getFirstChild() == null) {
                mResult = new Bundle();
                return true;
            } else {
                NodeList items = root.getFirstChild().getFirstChild().getFirstChild()
                        .getFirstChild().getChildNodes();

                mResult = translate(items);
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

    private void putToBundle(Bundle bundle, String name, String type, String value) {
        if (type == null || type.equalsIgnoreCase("string")) {
            bundle.putString(name, value);
        } else if (type.equalsIgnoreCase("int") ||
                type.equalsIgnoreCase("i4")) {
            bundle.putInt(name, Integer.valueOf(value));
        } else if (type.equalsIgnoreCase("boolean")) {
            bundle.putBoolean(name, Boolean.valueOf(value));
        }
    }

    private Bundle translate(NodeList items) throws Exception {
        Bundle bundle = new Bundle();
        for (int i = 0; i < items.getLength(); ++i) {
            Node item = items.item(i);
            NodeList properties = item.getChildNodes();

            String name = null;
            String type = null;
            String value = "";

            Bundle arrayBundle = null;

            boolean ignore = false;

            for (int j = 0; j < properties.getLength(); ++j) {
                Node property = properties.item(j);
                String propName = property.getNodeName().toLowerCase();

                if (propName.equalsIgnoreCase("name")) {
                    name = property.getFirstChild().getNodeValue().toLowerCase();
                } else if (propName.equalsIgnoreCase("value")) {
                    type = property.getFirstChild().getNodeName();

                    if (type == null) {
                        value = property.getFirstChild().getNodeValue();
                    } else if (type.equalsIgnoreCase("array")) {
                        NodeList arrayItems = property.getFirstChild().getFirstChild().getChildNodes();

                        arrayBundle = translateArray(arrayItems);
                    } else if (type.equalsIgnoreCase("struct")) {
                        ignore = true;
                        bundle.putAll(translate(property.getFirstChild().getChildNodes()));
                    } else {
                        if (property.getFirstChild().getFirstChild() == null) {
                            value = "";
                        } else {
                            value = property.getFirstChild().getFirstChild().getNodeValue();
                        }
                    }
                }
            }

            if (ignore) {
                continue;
            }

            if (arrayBundle != null) {
                bundle.putBundle(name, arrayBundle);
            } else {
                if (name == null || type == null) {
                    setError("<name,value> pair is incomplete");
                    throw new Exception();
                }

                if (value == null) {
                    value = "";
                }

                if (name.equalsIgnoreCase("faultstring")) {
                    bundle.putString(KEY_RESULT_MESSAGE, value);
                    bundle.putBoolean(KEY_RESULT_ERROR, true);
                } else if (name.equalsIgnoreCase("faultcode")) {
                    bundle.putInt("code", Integer.valueOf(value));
                    bundle.putBoolean(KEY_RESULT_ERROR, true);
                } else {
                    putToBundle(bundle, name, type, value);
                }
            }
        }
        return bundle;
    }

    private Bundle translateArray(NodeList arrayItems) throws Exception {
        Bundle arrayBundle = new Bundle();

        for (int arrayIndex = 0; arrayIndex < arrayItems.getLength(); ++arrayIndex) {
            Node arrayElement = arrayItems.item(arrayIndex);
            arrayElement.getNodeName();

            String arrayElementType = arrayElement.getFirstChild().getNodeName();
            String arrayElementValue = arrayElement.getFirstChild().getFirstChild().getNodeValue();

            if (arrayElementType.equalsIgnoreCase("array")) {
                arrayBundle.putBundle(Integer.toString(arrayIndex), translateArray(arrayElement.getFirstChild().getFirstChild().getChildNodes()));
            } else if (arrayElementType.equalsIgnoreCase("struct")) {
                arrayBundle.putBundle(Integer.toString(arrayIndex), translate(arrayElement.getFirstChild().getChildNodes()));
            } else {
                putToBundle(arrayBundle, Integer.toString(arrayIndex), arrayElementType, arrayElementValue);
            }
        }

        return arrayBundle;
    }
}
