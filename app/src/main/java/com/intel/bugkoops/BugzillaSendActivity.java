package com.intel.bugkoops;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

public class BugzillaSendActivity extends Activity implements OnTaskCompleted {
    final String LOG_TAG = getClass().getSimpleName();

    private EditText mServerEditText;
    private EditText mUserEditText;
    private EditText mPasswordEditText;
    private Button mConnectButton;
    private Spinner mProductSpinner;

    private Bundle mSession;
    private Bundle mProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bugzilla_send);

        mServerEditText = (EditText) findViewById(R.id.bugzilla_send_server_edittext);
        mUserEditText = (EditText) findViewById(R.id.bugzilla_send_user_edittext);
        mPasswordEditText = (EditText) findViewById(R.id.bugzilla_send_password_edittext);
        mConnectButton = (Button) findViewById(R.id.bugzilla_send_connect_button);
        mProductSpinner = (Spinner) findViewById(R.id.bugzilla_send_product_spinner);

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

        Bundle report = new Bundle();

        Bundle params = new Bundle();
        params.putString(BugzillaProgressTask.KEY_SERVER, mServerEditText.getText().toString());
        params.putString(BugzillaProgressTask.KEY_LOGIN, mUserEditText.getText().toString());
        params.putString(BugzillaProgressTask.KEY_PASSWORD, mPasswordEditText.getText().toString());
        params.putBundle(BugzillaProgressTask.KEY_REPORT, report);
        params.putInt(BugzillaProgressTask.KEY_TASK, BugzillaProgressTask.TASK_LOGIN_AND_GET_PRODUCT);
        new BugzillaProgressTask(this, params, this).execute();
    }

    public void onTaskCompleted(Bundle result) {
        switch(result.getInt(BugzillaProgressTask.KEY_TASK)) {
            case BugzillaProgressTask.TASK_LOGIN_AND_GET_PRODUCT:
                if(result.getBoolean(BugzillaProgressTask.KEY_ERROR)) {
                    mServerEditText.setEnabled(true);
                    mUserEditText.setEnabled(true);
                    mPasswordEditText.setEnabled(true);
                    mConnectButton.setEnabled(true);
                } else {
                    mSession = result.getBundle(BugzillaProgressTask.KEY_SESSION);
                    mProducts = result.getBundle(BugzillaProgressTask.KEY_PRODUCTS);

                    ArrayList<String> productList = new ArrayList<>();
                    for(String key : mProducts.keySet()) {
                        productList.add(key);
                    }
                    productList.add("Please select a product ...");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productList);
                    mProductSpinner.setAdapter(adapter);
                    mProductSpinner.setSelection(productList.size()-1);
                    mProductSpinner.setVisibility(View.VISIBLE);
                }
                break;
            case BugzillaProgressTask.TASK_SESSION_LOGOUT:
                setResult(RESULT_CANCELED, null);
                mSession = null;
                finish();
                break;
            case BugzillaProgressTask.TASK_SEND:
                Intent intent = new Intent();
                intent.putExtra(ReportDetailActivity.KEY_MESSAGE,"Report succesfuly sent!");
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mSession != null) {
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
}
