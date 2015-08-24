package com.intel.bugkoops.Integrations.Bugzilla;

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
        if(connection.version()) {
           return connection;
        }

        connection = new BugzillaXMLRPC(mServer, mUserAgent);
        if(connection.version()) {
            return connection;
        }

        return null;
    }
}
