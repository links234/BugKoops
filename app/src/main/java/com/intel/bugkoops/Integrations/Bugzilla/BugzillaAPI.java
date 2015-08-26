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

    String DEFAULT_ATTACHMENT_CONTENT_TYPE = "text/plain";
    String DEFAULT_ATTACHMENT_SUMMARY = "Bug Koops generated attachment";
    String DEFAULT_ATTACHMENT_FILE_NAME = "Oops message";


    String KEY_API = "api";

    String KEY_TOKEN = "token";
    String KEY_USER = "user";
    String KEY_PASSWORD = "password";

    String KEY_ID = "id";
    String KEY_PRODUCT = "product";
    String KEY_COMPONENT = "component";
    String KEY_VERSION = "version";
    String KEY_SUMMARY = "summary";
    String KEY_DESCRIPTION = "description";
    String KEY_OS = "op_sys";
    String KEY_PLATFORM = "platform";
    String KEY_PRIORITY = "priority";
    String KEY_SEVERITY = "severity";

    String KEY_ATTACHMENT_BUGID = "attachment_id";
    String KEY_ATTACHMENT_DATA = "attachment_data";
    String KEY_ATTACHMENT_CONTENT_TYPE = "attachment_content_type";

    String KEY_RESULT_ERROR = "error";
    String KEY_RESULT_MESSAGE = "message";

    String KEY_RESULT_PRODUCTS = "products";
    String KEY_RESULT_COMPONENTS = "components";
    String KEY_RESULT_VALUES = "values";
    String KEY_RESULT_SORTKEY = "sort_key";
    String KEY_RESULT_NAME = "name";
    String KEY_RESULT_FIELDS = "fields";

    String KEY_RESULT_VERSION = "version";
    String KEY_RESULT_OS = "op_sys";
    String KEY_RESULT_PLATFORM = "platform";
    String KEY_RESULT_PRIORITY = "priority";
    String KEY_RESULT_SEVERITY = "severity";

    boolean version();

    boolean login(String user, String password);
    boolean logout();

    boolean send(Bundle report);
    boolean sendAttachment(Bundle attachment);

    void restore(Bundle session);
    Bundle save();

    boolean getHierarchy();
    boolean getFields();

    String getAPIVersion();
    Bundle getResult();
}
