package com.intel.bugkoops.Integrations.Bugzilla;

import android.os.Bundle;

public class BugzillaAutoDetect {
    final String LOG_TAG = getClass().getSimpleName();

    private String mServer;
    private String mUserAgent;

    public BugzillaAutoDetect(String server, String userAgent) {
        mServer = server;
        mUserAgent = userAgent;
    }

    public BugzillaAPI open() {
        BugzillaAPI connection = new BugzillaREST(mServer, mUserAgent);
        if (connection.version()) {
            return connection;
        }

        connection = new BugzillaXMLRPC(mServer, mUserAgent);
        if (connection.version()) {
            return connection;
        }

        return null;
    }

    public BugzillaAPI restore(Bundle session) {
        BugzillaAPI connection;
        if (session.getString(BugzillaAPI.KEY_API).equals(BugzillaREST.API_VERSION)) {
            connection = new BugzillaREST(mServer, mUserAgent);
        } else {
            connection = new BugzillaXMLRPC(mServer, mUserAgent);
        }
        connection.restore(session);
        return connection;
    }
}
