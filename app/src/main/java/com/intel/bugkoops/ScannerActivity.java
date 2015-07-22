package com.intel.bugkoops;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.util.List;

public class ScannerActivity extends Activity implements CompoundBarcodeView.TorchListener {
    private static final String LOG_TAG = ScannerActivity.class.getSimpleName();

    private static final String KEY_FLASH_STATE = "mFlashState";
    private static final String KEY_LOCKED_ORIENTATION = "mLockedOrientation";
    private static final String KEY_INVERSE_SCAN = "mInverseScan";

    private static final int FLASH_STATE_OFF = 0;
    private static final int FLASH_STATE_AUTO = 1;
    private static final int FLASH_STATE_ON = 2;

    private static final int RELOAD_TORCH_DELAY = 400;

    private CompoundBarcodeView mBarcodeScannerView;
    private Button mSwitchFlashButton;
    private Button mSwitchOrientationLockingButton;
    private Button mSwitchInverseScanButton;

    private int mFlashState;
    private int mLockedOrientation;
    private boolean mInverseScan;

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

        mSwitchOrientationLockingButton = (Button)findViewById(R.id.switch_orientation_locking);
        mSwitchInverseScanButton = (Button)findViewById(R.id.switch_inverse_scan);

        loadDefaultSettings();

        if (savedInstanceState != null) {
            loadStateFromBundle(savedInstanceState);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            loadStateFromBundle(bundle);
        }

        mBarcodeScannerView.getBarcodeView().getCameraSettings().setScanInverted(mInverseScan);
        if(mFlashState == FLASH_STATE_ON) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBarcodeScannerView.setTorchOn();
                }
            }, RELOAD_TORCH_DELAY);
        } else if(mFlashState == FLASH_STATE_AUTO) {
            mBarcodeScannerView.getBarcodeView().getCameraSettings().setAutoTorchEnabled(true);
        }

        updateLayout();
    }

    private void updateLayoutSwitchFlashlight() {
        if (mFlashState == FLASH_STATE_OFF) {
            mSwitchFlashButton.setText("FLASH OFF");
        } else if (mFlashState == FLASH_STATE_AUTO) {
            mSwitchFlashButton.setText("FLASH AUTO");
        } else {
            mSwitchFlashButton.setText("FLASH ON");
        }
    }

    private void updateLayoutSwitchOrientationLocking() {
        if (mLockedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            mSwitchOrientationLockingButton.setText("UNLOCKED");
        } else {
            mSwitchOrientationLockingButton.setText("LOCKED");
        }
    }

    private void updateLayoutSwitchInverseScan() {
        if (mInverseScan) {
            mSwitchInverseScanButton.setText("INV ON");
        } else {
            mSwitchInverseScanButton.setText("INV OFF");
        }
    }

    private void updateLayout() {
        updateLayoutSwitchFlashlight();
        updateLayoutSwitchOrientationLocking();
        updateLayoutSwitchInverseScan();
    }

    private void loadDefaultSettings() {
        mFlashState = FLASH_STATE_OFF;
        mLockedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        mInverseScan = false;
    }

    public void onSwitchFlashlight(View view) {
        if (mFlashState == FLASH_STATE_OFF) {
            mFlashState = FLASH_STATE_AUTO;
            reload();
        } else if (mFlashState == FLASH_STATE_AUTO) {
            mFlashState = FLASH_STATE_ON;
            reload();
        } else {
            mBarcodeScannerView.setTorchOff();
        }

        updateLayoutSwitchFlashlight();
    }

    public void onSwitchOrientationLocking(View view) {
        if (mLockedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            lockOrientation();
        } else {
            unlockOrientation();
        }
    }

    public void onSwitchInverseScan(View view) {
        if (mInverseScan) {
            mInverseScan = false;
            reload();
        } else {
            mInverseScan = true;
            reload();
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

        updateLayoutSwitchOrientationLocking();
    }

    private void unlockOrientation() {
        if(mLockedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            mLockedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            setRequestedOrientation(mLockedOrientation);
        }

        updateLayoutSwitchOrientationLocking();
    }

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        Bundle bundle = new Bundle();
        saveStateToBundle(bundle);
        intent.putExtras(bundle);

        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void loadStateFromBundle(Bundle inState) {
        mFlashState = inState.getInt(KEY_FLASH_STATE);
        mLockedOrientation = inState.getInt(KEY_LOCKED_ORIENTATION);
        mInverseScan = inState.getBoolean(KEY_INVERSE_SCAN);
    }

    private void saveStateToBundle(Bundle outState) {
        outState.putInt(KEY_FLASH_STATE, mFlashState);
        outState.putInt(KEY_LOCKED_ORIENTATION, mLockedOrientation);
        outState.putBoolean(KEY_INVERSE_SCAN, mInverseScan);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        saveStateToBundle(outState);
    }

    @Override
    public void onTorchOn() {
        mFlashState = FLASH_STATE_ON;
        updateLayoutSwitchFlashlight();
    }

    @Override
    public void onTorchOff() {
        mFlashState = FLASH_STATE_OFF;
        updateLayoutSwitchFlashlight();
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
