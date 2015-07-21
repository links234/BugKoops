package com.intel.bugkoops;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
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
    private int mFlashState = FLASH_STATE_OFF;

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

        setContentView(R.layout.activity_scanner);

        mBarcodeScannerView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        mBarcodeScannerView.decodeContinuous(callback);
        mBarcodeScannerView.setTorchListener(this);

        mSwitchFlashButton = (Button)findViewById(R.id.switch_flashlight);

        if (!hasFlash()) {
            mSwitchFlashButton.setVisibility(View.GONE);
        }

        mFlashState = FLASH_STATE_OFF;
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

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mBarcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    public void switchFlashlight(View view) {
        if (mFlashState == FLASH_STATE_OFF) {
            mBarcodeScannerView.setTorchOn();
        } else {
            mBarcodeScannerView.setTorchOff();
        }
    }

    @Override
    public void onTorchOn() {
        mFlashState = FLASH_STATE_ON;
        mSwitchFlashButton.setText("FLASH ON");
    }

    @Override
    public void onTorchOff() {
        mFlashState = FLASH_STATE_OFF;
        mSwitchFlashButton.setText("FLASH OFF");
    }
}
