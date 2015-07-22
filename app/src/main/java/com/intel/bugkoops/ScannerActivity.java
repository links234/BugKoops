package com.intel.bugkoops;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

public class ScannerActivity extends Activity implements CompoundBarcodeView.TorchListener {
    private static final String LOG_TAG = ScannerActivity.class.getSimpleName();

    private static final int FLASH_STATE_OFF = 0;
    private static final int FLASH_STATE_ON = 1;

    private CompoundBarcodeView mBarcodeScannerView;
    private Button mSwitchFlashButton;
    private Button mSwitchOrientationLocking;

    private int mFlashState;
    private int mLockedOrientation;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                mBarcodeScannerView.setStatusText(result.getText());
            }

            ImageView imageView = (ImageView) findViewById(R.id.barcode_preview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_scanner);

        mBarcodeScannerView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        mBarcodeScannerView.decodeContinuous(callback);
        mBarcodeScannerView.setTorchListener(this);

        mSwitchFlashButton = (Button)findViewById(R.id.switch_flashlight);
        if (!hasFlash()) {
            mSwitchFlashButton.setVisibility(View.GONE);
        }
        mFlashState = FLASH_STATE_OFF;

        mSwitchOrientationLocking = (Button)findViewById(R.id.switch_orientation_locking);
        mLockedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public void onSwitchFlashlight(View view) {
        if (mFlashState == FLASH_STATE_OFF) {
            mBarcodeScannerView.setTorchOn();
        } else {
            mBarcodeScannerView.setTorchOff();
        }
    }

    public void onSwitchOrientationLocking(View view) {
        if(mLockedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            lockOrientation();
        } else {
            unlockOrientation();
        }
    }

    private void lockOrientation() {
        if (mLockedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            Display display = getWindowManager().getDefaultDisplay();
            int rotation = display.getRotation();
            int baseOrientation = getResources().getConfiguration().orientation;
            int orientation = 0;
            if (baseOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                }
            } else if (baseOrientation == Configuration.ORIENTATION_PORTRAIT) {
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                }
            }

            mLockedOrientation = orientation;
        }
        setRequestedOrientation(mLockedOrientation);
        mSwitchOrientationLocking.setText("UNLOCK");
    }

    private void unlockOrientation() {
        if(mLockedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            mLockedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            setRequestedOrientation(mLockedOrientation);
        }
        mSwitchOrientationLocking.setText("LOCK");
    }

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    public void onTorchOn() {
        mFlashState = FLASH_STATE_ON;
        mSwitchFlashButton.setText("FLASH OFF");
    }

    @Override
    public void onTorchOff() {
        mFlashState = FLASH_STATE_OFF;
        mSwitchFlashButton.setText("FLASH ON");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBarcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBarcodeScannerView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mBarcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
