package com.intel.bugkoops;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.intel.bugkoops.UI.ScanButton;

public class MainActivity extends MenuActivity {
    final String LOG_TAG = getClass().getSimpleName();

    static final int SCAN_BUTTON_DELAY = 190;
    static final int SCAN_BUTTON_ACTIVATE_DELAY = 200;
    static final int EXIT_TIME_WINDOW = 3000;

    boolean mExit;
    boolean mScan;

    static MainActivity sInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExit = false;
        mScan = false;

        sInstance = this;
    }

    public void onScan(View view) {
        if(!mScan) {
            mScan = true;
            final Animation scanAnim = AnimationUtils.loadAnimation(this, R.anim.anim_scan);

            findViewById(R.id.scan_button).startAnimation(scanAnim);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                    startActivity(intent);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.getInstance().mScan = false;
                        }
                    }, SCAN_BUTTON_ACTIVATE_DELAY);
                }
            }, SCAN_BUTTON_DELAY);
        }
    }

    public void onBackPressed() {
        if (mExit) {
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            mExit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mExit = false;
                }
            }, EXIT_TIME_WINDOW);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ScanButton)findViewById(R.id.scan_button)).reset();
    }

    static MainActivity getInstance() {
        return sInstance;
    }
}
