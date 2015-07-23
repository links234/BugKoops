package com.intel.bugkoops;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

public class MainActivity extends MenuActivity {
    final String LOG_TAG = getClass().getSimpleName();

    static final int SCAN_BUTTON_DELAY = 190;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onScan(View view) {
        final Animation scanAnim = AnimationUtils.loadAnimation(this, R.anim.anim_scan);

        findViewById(R.id.scan_button).startAnimation(scanAnim);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                startActivity(intent);
            }
        }, SCAN_BUTTON_DELAY);
    }
}
