package com.intel.bugkoops.Integrations.Bugzilla;

import android.os.Bundle;

public interface BugzillaAPI {
    String DEFAULT_PRODUCT = "FoodReplicator";         //CHANGE THIS IN FINAL VERSION
    String DEFAULT_COMPONENT = "Salt";            //CHANGE THIS IN FINAL VERSION
    String DEFAULT_VERSION = "1.0";          //CHANGE THIS IN FINAL VERSION
    String DEFAULT_SUMMARY = "Bug Koops generated bug report";
    String DEFAULT_DESCRIPTION = "";         //CHANGE THIS IN FINAL VERSION
    String DEFAULT_OS = "All";
    String DEFAULT_PLATFORM = "All";
    String DEFAULT_PRIORITY = "P1";
    String DEFAULT_SEVERITY = "critical";

    String KEY_TOKEN = "token";

    String KEY_ERROR = "error";
    String KEY_MESSAGE = "message";

    String KEY_PRODUCT = "product";
    String KEY_COMPONENT = "component";
    String KEY_VERSION = "version";
    String KEY_SUMMARY = "summary";
    String KEY_DESCRIPTION = "description";
    String KEY_OS = "op_sys";
    String KEY_PLATFORM = "platform";
    String KEY_PRIORITY = "priority";
    String KEY_SEVERITY = "severity";

    boolean login(String user, String password);
    boolean logout();

    boolean send(Bundle report);

    Bundle getResult();
}