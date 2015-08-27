package com.intel.bugkoops;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.intel.bugkoops.Integrations.Bugzilla.BugzillaAPI;

import java.util.ArrayList;

public class BugzillaSendActivity extends Activity implements OnTaskCompleted, AdapterView.OnItemSelectedListener {
    final String LOG_TAG = getClass().getSimpleName();

    private EditText mServerEditText;
    private EditText mUserEditText;
    private EditText mPasswordEditText;
    private Button mConnectButton;
    private Spinner mProductSpinner;
    private Spinner mComponentSpinner;
    private Spinner mVersionSpinner;
    private Spinner mOSSpinner;
    private Spinner mPlatformSpinner;
    private Spinner mPrioritySpinner;
    private Spinner mSeveritySpinner;
    private Button mSendButton;
    private TextView mStatusTextView;

    private boolean mVersionChoosed;
    private boolean mOSChoosed;
    private boolean mPlatformChoosed;
    private boolean mPriorityChoosed;
    private boolean mSeverityChoosed;

    private Bundle mSession;
    private Bundle mProducts;
    private Bundle mFields;

    private String mReportTitle;
    private long mReportDate;
    private String mReportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bugzilla_send);

        Bundle data = getIntent().getExtras();

        mReportTitle = Utility.getString(data, ReportDetailActivity.KEY_REPORT_TITLE, "");
        mReportDate = data.getLong(ReportDetailActivity.KEY_REPORT_DATE, 0);
        mReportText = Utility.getString(data, ReportDetailActivity.KEY_REPORT_TEXT, "");

        mServerEditText = (EditText) findViewById(R.id.bugzilla_send_server_edittext);
        mUserEditText = (EditText) findViewById(R.id.bugzilla_send_user_edittext);
        mPasswordEditText = (EditText) findViewById(R.id.bugzilla_send_password_edittext);
        mConnectButton = (Button) findViewById(R.id.bugzilla_send_connect_button);
        mProductSpinner = (Spinner) findViewById(R.id.bugzilla_send_product_spinner);
        mComponentSpinner = (Spinner) findViewById(R.id.bugzilla_send_component_spinner);
        mVersionSpinner = (Spinner) findViewById(R.id.bugzilla_send_version_spinner);
        mOSSpinner = (Spinner) findViewById(R.id.bugzilla_send_os_spinner);
        mPlatformSpinner = (Spinner) findViewById(R.id.bugzilla_send_platform_spinner);
        mPrioritySpinner = (Spinner) findViewById(R.id.bugzilla_send_priority_spinner);
        mSeveritySpinner = (Spinner) findViewById(R.id.bugzilla_send_severity_spinner);
        mSendButton = (Button) findViewById(R.id.bugzilla_send_send_button);
        mStatusTextView = (TextView) findViewById(R.id.bugzilla_send_status_textview);

        mProductSpinner.setOnItemSelectedListener(this);
        mComponentSpinner.setOnItemSelectedListener(this);
        mVersionSpinner.setOnItemSelectedListener(this);
        mOSSpinner.setOnItemSelectedListener(this);
        mPlatformSpinner.setOnItemSelectedListener(this);
        mPrioritySpinner.setOnItemSelectedListener(this);
        mSeveritySpinner.setOnItemSelectedListener(this);

        mServerEditText.setText(BugzillaProgressTask.DEFAULT_SERVER);
        mUserEditText.setText(BugzillaProgressTask.DEFAULT_LOGIN);
        mPasswordEditText.setText(BugzillaProgressTask.DEFAULT_PASSWORD);

        setFinishOnTouchOutside(false);

        mSession = null;
    }

    public void onConnect(View view) {
        mServerEditText.setEnabled(false);
        mUserEditText.setEnabled(false);
        mPasswordEditText.setEnabled(false);
        mConnectButton.setEnabled(false);


        Bundle params = new Bundle();
        params.putString(BugzillaProgressTask.KEY_SERVER, mServerEditText.getText().toString());
        params.putString(BugzillaProgressTask.KEY_LOGIN, mUserEditText.getText().toString());
        params.putString(BugzillaProgressTask.KEY_PASSWORD, mPasswordEditText.getText().toString());

        params.putInt(BugzillaProgressTask.KEY_TASK, BugzillaProgressTask.TASK_LOGIN_GET_PRODUCTS_GET_COMPONENTS_GET_FIELDS);
        new BugzillaProgressTask(this, params, this).execute();
    }

    public void onSend(View view) {
        Bundle params = new Bundle();
        params.putBundle(BugzillaProgressTask.KEY_SESSION, mSession);

        Bundle report = new Bundle();
        report.putString(BugzillaAPI.KEY_PRODUCT, mProductSpinner.getSelectedItem().toString());
        report.putString(BugzillaAPI.KEY_COMPONENT, mComponentSpinner.getSelectedItem().toString());
        report.putString(BugzillaAPI.KEY_VERSION, mVersionSpinner.getSelectedItem().toString());
        report.putString(BugzillaAPI.KEY_OS, mOSSpinner.getSelectedItem().toString());
        report.putString(BugzillaAPI.KEY_PLATFORM, mPlatformSpinner.getSelectedItem().toString());
        report.putString(BugzillaAPI.KEY_PRIORITY, mPrioritySpinner.getSelectedItem().toString());
        report.putString(BugzillaAPI.KEY_SEVERITY, mSeveritySpinner.getSelectedItem().toString());
        params.putBundle(BugzillaProgressTask.KEY_REPORT, report);

        Bundle attachment = new Bundle();
        attachment.putString(BugzillaAPI.KEY_ATTACHMENT_DATA, mReportText);
        params.putBundle(BugzillaProgressTask.KEY_ATTACHMENT, attachment);

        params.putInt(BugzillaProgressTask.KEY_TASK, BugzillaProgressTask.TASK_SESSION_SEND_WITH_ATTACHMENT_LOGOUT);
        new BugzillaProgressTask(this, params, this).execute();
    }

    public void onTaskCompleted(Bundle result) {
        switch (result.getInt(BugzillaProgressTask.KEY_TASK)) {
            case BugzillaProgressTask.TASK_LOGIN_GET_PRODUCTS_GET_COMPONENTS_GET_FIELDS:
                if (result.getBoolean(BugzillaProgressTask.KEY_ERROR)) {
                    mServerEditText.setEnabled(true);
                    mUserEditText.setEnabled(true);
                    mPasswordEditText.setEnabled(true);
                    mConnectButton.setEnabled(true);
                } else {
                    mSession = result.getBundle(BugzillaProgressTask.KEY_SESSION);
                    mProducts = result.getBundle(BugzillaProgressTask.KEY_PRODUCTS);
                    mFields = result.getBundle(BugzillaProgressTask.KEY_FIELDS);

                    ArrayList<String> productList = new ArrayList<>();
                    if (mProducts != null) {
                        for (String key : mProducts.keySet()) {
                            productList.add(key);
                        }
                    }
                    productList.add(getString(R.string.bugzilla_send_activity_default_product));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productList);
                    mProductSpinner.setAdapter(adapter);
                    mProductSpinner.setSelection(productList.size() - 1);
                    mProductSpinner.setVisibility(View.VISIBLE);
                }
                break;
            case BugzillaProgressTask.TASK_SESSION_LOGOUT:
                setResult(RESULT_CANCELED, null);
                mSession = null;
                finish();
                break;
            case BugzillaProgressTask.TASK_SESSION_SEND_WITH_ATTACHMENT_LOGOUT:
                if (!result.getBoolean(BugzillaProgressTask.KEY_ERROR)) {
                    Intent intent = new Intent();
                    intent.putExtra(ReportDetailActivity.KEY_MESSAGE, getString(R.string.bugzilla_send_activity_success));
                    intent.putExtra(ReportDetailActivity.KEY_RESULT, result.getString(BugzillaProgressTask.KEY_CREATED_BUG_URL));
                    setResult(RESULT_OK, intent);
                    mSession = null;
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSession != null) {
                Bundle params = new Bundle();
                params.putBundle(BugzillaProgressTask.KEY_SESSION, mSession);
                params.putInt(BugzillaProgressTask.KEY_TASK, BugzillaProgressTask.TASK_SESSION_LOGOUT);
                new BugzillaProgressTask(this, params, this).execute();
                return true;
            } else {
                return super.onKeyUp(keyCode, event);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()) {
            case R.id.bugzilla_send_product_spinner:
                String product = parent.getItemAtPosition(pos).toString();
                if (product.equals(getString(R.string.bugzilla_send_activity_default_product))) {
                    mComponentSpinner.setVisibility(View.GONE);

                    mVersionSpinner.setVisibility(View.GONE);
                    mOSSpinner.setVisibility(View.GONE);
                    mPlatformSpinner.setVisibility(View.GONE);
                    mPrioritySpinner.setVisibility(View.GONE);
                    mSeveritySpinner.setVisibility(View.GONE);
                    mSendButton.setVisibility(View.GONE);
                } else {
                    ArrayList<String> componentList = new ArrayList<>();
                    if (mProducts.getBundle(product) != null) {
                        Bundle productBundle = mProducts.getBundle(product);
                        Bundle componentsBundle = productBundle.getBundle(BugzillaProgressTask.KEY_COMPONENTS);
                        for (String key : componentsBundle.keySet()) {
                            Bundle componentBundle = componentsBundle.getBundle(key);
                            componentList.add(componentBundle.getString(BugzillaProgressTask.KEY_NAME));
                        }
                    }
                    componentList.add(getString(R.string.bugzilla_send_activity_default_component));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, componentList);
                    mComponentSpinner.setAdapter(adapter);
                    mComponentSpinner.setSelection(componentList.size() - 1);
                    mComponentSpinner.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.bugzilla_send_component_spinner:
                String component = parent.getItemAtPosition(pos).toString();
                if (component.equals(getString(R.string.bugzilla_send_activity_default_component))) {
                    mVersionSpinner.setVisibility(View.GONE);
                    mOSSpinner.setVisibility(View.GONE);
                    mPlatformSpinner.setVisibility(View.GONE);
                    mPrioritySpinner.setVisibility(View.GONE);
                    mSeveritySpinner.setVisibility(View.GONE);
                    mSendButton.setVisibility(View.GONE);
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    if (mFields.getBundle(BugzillaAPI.KEY_RESULT_VERSION) != null) {
                        Bundle fieldBundle = mFields.getBundle(BugzillaAPI.KEY_RESULT_VERSION);
                        Bundle valuesBundle = fieldBundle.getBundle(BugzillaAPI.KEY_RESULT_VALUES);
                        for (String key : valuesBundle.keySet()) {
                            list.add(key);
                        }
                    }
                    list.add(getString(R.string.bugzilla_send_activity_default_version));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
                    mVersionSpinner.setAdapter(adapter);
                    mVersionSpinner.setSelection(list.size() - 1);
                    mVersionSpinner.setVisibility(View.VISIBLE);

                    list = new ArrayList<>();
                    if (mFields.getBundle(BugzillaAPI.KEY_RESULT_OS) != null) {
                        Bundle fieldBundle = mFields.getBundle(BugzillaAPI.KEY_RESULT_OS);
                        Bundle valuesBundle = fieldBundle.getBundle(BugzillaAPI.KEY_RESULT_VALUES);
                        for (String key : valuesBundle.keySet()) {
                            list.add(key);
                        }
                    }
                    list.add(getString(R.string.bugzilla_send_activity_default_os));
                    adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
                    mOSSpinner.setAdapter(adapter);
                    mOSSpinner.setSelection(list.size() - 1);
                    mOSSpinner.setVisibility(View.VISIBLE);

                    list = new ArrayList<>();
                    if (mFields.getBundle(BugzillaAPI.KEY_RESULT_PLATFORM) != null) {
                        Bundle fieldBundle = mFields.getBundle(BugzillaAPI.KEY_RESULT_PLATFORM);
                        Bundle valuesBundle = fieldBundle.getBundle(BugzillaAPI.KEY_RESULT_VALUES);
                        for (String key : valuesBundle.keySet()) {
                            list.add(key);
                        }
                    }
                    list.add(getString(R.string.bugzilla_send_activity_default_platform));
                    adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
                    mPlatformSpinner.setAdapter(adapter);
                    mPlatformSpinner.setSelection(list.size() - 1);
                    mPlatformSpinner.setVisibility(View.VISIBLE);

                    list = new ArrayList<>();
                    if (mFields.getBundle(BugzillaAPI.KEY_RESULT_PRIORITY) != null) {
                        Bundle fieldBundle = mFields.getBundle(BugzillaAPI.KEY_RESULT_PRIORITY);
                        Bundle valuesBundle = fieldBundle.getBundle(BugzillaAPI.KEY_RESULT_VALUES);
                        for (String key : valuesBundle.keySet()) {
                            list.add(key);
                        }
                    }
                    list.add(getString(R.string.bugzilla_send_activity_default_priority));
                    adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
                    mPrioritySpinner.setAdapter(adapter);
                    mPrioritySpinner.setSelection(list.size() - 1);
                    mPrioritySpinner.setVisibility(View.VISIBLE);

                    list = new ArrayList<>();
                    if (mFields.getBundle(BugzillaAPI.KEY_RESULT_SEVERITY) != null) {
                        Bundle fieldBundle = mFields.getBundle(BugzillaAPI.KEY_RESULT_SEVERITY);
                        Bundle valuesBundle = fieldBundle.getBundle(BugzillaAPI.KEY_RESULT_VALUES);
                        for (String key : valuesBundle.keySet()) {
                            list.add(key);
                        }
                    }
                    list.add(getString(R.string.bugzilla_send_activity_default_severity));
                    adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
                    mSeveritySpinner.setAdapter(adapter);
                    mSeveritySpinner.setSelection(list.size() - 1);
                    mSeveritySpinner.setVisibility(View.VISIBLE);

                    mVersionChoosed = false;
                    mOSChoosed = false;
                    mPlatformChoosed = false;
                    mPriorityChoosed = false;
                    mSeverityChoosed = false;
                }
                break;
            case R.id.bugzilla_send_version_spinner:
                String version = parent.getItemAtPosition(pos).toString();
                mVersionChoosed = !version.equals(getString(R.string.bugzilla_send_activity_default_version));
                checkForSendButton();
                break;
            case R.id.bugzilla_send_os_spinner:
                String os = parent.getItemAtPosition(pos).toString();
                mOSChoosed = !os.equals(getString(R.string.bugzilla_send_activity_default_os));
                checkForSendButton();
                break;
            case R.id.bugzilla_send_platform_spinner:
                String platform = parent.getItemAtPosition(pos).toString();
                mPlatformChoosed = !platform.equals(getString(R.string.bugzilla_send_activity_default_platform));
                checkForSendButton();
                break;
            case R.id.bugzilla_send_priority_spinner:
                String priority = parent.getItemAtPosition(pos).toString();
                mPriorityChoosed = !priority.equals(getString(R.string.bugzilla_send_activity_default_priority));
                checkForSendButton();
                break;
            case R.id.bugzilla_send_severity_spinner:
                String severity = parent.getItemAtPosition(pos).toString();
                mSeverityChoosed = !severity.equals(getString(R.string.bugzilla_send_activity_default_severity));
                checkForSendButton();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    void checkForSendButton() {
        if (mVersionChoosed && mOSChoosed && mPlatformChoosed && mPriorityChoosed && mSeverityChoosed) {
            mSendButton.setVisibility(View.VISIBLE);
        } else {
            mSendButton.setVisibility(View.GONE);
        }
    }
}
