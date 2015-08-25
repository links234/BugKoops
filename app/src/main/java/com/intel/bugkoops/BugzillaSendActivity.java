package com.intel.bugkoops;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class BugzillaSendActivity extends Activity implements OnTaskCompleted {
    final String LOG_TAG = getClass().getSimpleName();

    private Button mConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bugzilla_send);

        mConnectButton = (Button) findViewById(R.id.bugzilla_send_connect_button);
    }

    public void onConnect(View view) {
        AsyncTask task = new BugzillaProgressTask(this, null, this).execute();
    }

    public void onTaskCompleted(Bundle result) {
        mConnectButton.setActivated(false);
    }
}
