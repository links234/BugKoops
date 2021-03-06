package com.intel.bugkoops;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.intel.bugkoops.Data.MessageManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.intel.bugkoops.Data.PacketManager;

import java.util.List;

public class ScannerActivity extends Activity implements CompoundBarcodeView.TorchListener {
    private static final String LOG_TAG = ScannerActivity.class.getSimpleName();

    private static ScannerActivity sInstance;

    private static final String KEY_FLASH_STATE = "mFlashState";
    private static final String KEY_LOCKED_ORIENTATION = "mLockedOrientation";
    private static final String KEY_INVERSE_SCAN = "mInverseScan";
    private static final String KEY_CAMERA_ID = "mCameraId";

    private static final int FLASH_STATE_OFF = 0;
    private static final int FLASH_STATE_AUTO = 1;
    private static final int FLASH_STATE_ON = 2;

    private static final int RELOAD_TORCH_DELAY = 400;

    private CompoundBarcodeView mBarcodeScannerView;
    private ImageButton mSwitchFlashButton;
    private ImageButton mSwitchOrientationLockingButton;
    private ImageButton mSwitchInverseScanButton;
    private ImageButton mSwitchCameraButton;

    private int mFlashState;
    private int mLockedOrientation;
    private boolean mInverseScan;
    private int mCameraId;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            boolean scannerFailed = false;
            if (result.getResultMetadata() == null) {
                scannerFailed = true;
            } else {
                if (result.getResultMetadata().get(
                        ResultMetadataType.BYTE_SEGMENTS) == null) {
                    scannerFailed = true;
                }
            }

            if (!scannerFailed) {
                List<byte[]> listData = (List<byte[]>) result.getResultMetadata().get(
                        ResultMetadataType.BYTE_SEGMENTS);

                MessageManager.lastScanGotHere = false;

                int messageLength = 0;

                for (byte[] dataSegment : listData) {
                    messageLength += dataSegment.length;
                }

                byte[] packet = new byte[messageLength];

                int offset = 0;
                for (byte[] dataSegment : listData) {
                    System.arraycopy(dataSegment, 0, packet, offset, dataSegment.length);
                    offset += dataSegment.length;
                }

                PacketManager.push(packet);

                Bitmap preview = result.getBitmapWithResultPoints(Color.YELLOW);

                if (MessageManager.lastScanGotHere) {
                    Canvas canvas = new Canvas(preview);

                    Paint red;
                    red = new Paint();
                    red.setStyle(Paint.Style.FILL);
                    red.setColor(Color.RED);

                    Paint green;
                    green = new Paint();
                    green.setStyle(Paint.Style.FILL);
                    green.setColor(Color.GREEN);

                    Paint blue;
                    blue = new Paint();
                    blue.setStyle(Paint.Style.FILL);
                    blue.setColor(Color.BLUE);

                    byte[] packetStatus = MessageManager.getLastPacketStatus();

                    int border = preview.getWidth() / 20;
                    int width = (preview.getWidth() - border * 2) / packetStatus.length;
                    int height = preview.getHeight() / 10;

                    int x = border;
                    int y = 0;

                    for (int i = 0; i < packetStatus.length; ++i) {
                        if (packetStatus[i] == MessageManager.PACKET_STATUS_NOTFOUND) {
                            canvas.drawRect(x, y, x + width, y + height, red);
                        } else if (packetStatus[i] == MessageManager.PACKET_STATUS_DONE) {
                            canvas.drawRect(x, y, x + width, y + height, green);
                        } else if (packetStatus[i] == MessageManager.PACKET_STATUS_LAST_SCANNED) {
                            canvas.drawRect(x, y, x + width, y + height, blue);
                        }
                        x += width;
                    }
                }

                ImageView imageView = (ImageView) findViewById(R.id.barcode_preview);
                imageView.setImageBitmap(preview);
            } else {
                Log.e(LOG_TAG, "Scanner failed! (invalid metadata)");
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    public static ScannerActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sInstance = this;

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Utility.isFullscreenEnabled(this)) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_scanner);

        mBarcodeScannerView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        mBarcodeScannerView.decodeContinuous(callback);
        mBarcodeScannerView.setTorchListener(this);

        mSwitchFlashButton = (ImageButton) findViewById(R.id.switch_flashlight);
        if (!hasFlash()) {
            mSwitchFlashButton.setVisibility(View.GONE);
        }

        mSwitchOrientationLockingButton = (ImageButton) findViewById(R.id.switch_orientation_locking);
        mSwitchInverseScanButton = (ImageButton) findViewById(R.id.switch_inverse_scan);
        mSwitchCameraButton = (ImageButton) findViewById(R.id.switch_camera);

        loadDefaultSettings();

        if (savedInstanceState != null) {
            loadStateFromBundle(savedInstanceState);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            loadStateFromBundle(bundle);
        }

        mBarcodeScannerView.getBarcodeView().getCameraSettings().setRequestedCameraId(mCameraId);
        mBarcodeScannerView.getBarcodeView().getCameraSettings().setScanInverted(mInverseScan);
        if (mFlashState == FLASH_STATE_ON) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBarcodeScannerView.setTorchOn();
                }
            }, RELOAD_TORCH_DELAY);
        } else if (mFlashState == FLASH_STATE_AUTO) {
            mBarcodeScannerView.getBarcodeView().getCameraSettings().setAutoTorchEnabled(true);
        }

        updateLayout();
    }

    private void updateLayoutSwitchFlashlight() {
        if (mFlashState == FLASH_STATE_OFF) {
            mSwitchFlashButton.setImageResource(R.drawable.ic_flash_off_white_48dp);
        } else if (mFlashState == FLASH_STATE_AUTO) {
            mSwitchFlashButton.setImageResource(R.drawable.ic_flash_auto_white_48dp);
        } else {
            mSwitchFlashButton.setImageResource(R.drawable.ic_flash_on_white_48dp);
        }
    }

    private void updateLayoutSwitchOrientationLocking() {
        if (mLockedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            mSwitchOrientationLockingButton.setImageResource(R.drawable.ic_screen_rotation_white_48dp);
        } else {
            mSwitchOrientationLockingButton.setImageResource(R.drawable.ic_screen_lock_rotation_white_48dp);
        }
    }

    private void updateLayoutSwitchInverseScan() {
        if (mInverseScan) {
            mSwitchInverseScanButton.setImageResource(R.drawable.ic_invert_colors_white_48dp);
        } else {
            mSwitchInverseScanButton.setImageResource(R.drawable.ic_invert_colors_off_white_48dp);
        }
    }

    private void updateLayoutSwitchCamera() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mSwitchCameraButton.setImageResource(R.drawable.ic_camera_rear_white_48dp);
        } else {
            mSwitchCameraButton.setImageResource(R.drawable.ic_camera_front_white_48dp);
        }
    }

    private void updateLayout() {
        updateLayoutSwitchFlashlight();
        updateLayoutSwitchOrientationLocking();
        updateLayoutSwitchInverseScan();
        updateLayoutSwitchCamera();
    }

    private void loadDefaultSettings() {

        final String flashState = Utility.getFlashState(this);
        if (flashState.equals(getString(R.string.pref_flashstate_off))) {
            mFlashState = FLASH_STATE_OFF;
        } else if (flashState.equals(getString(R.string.pref_flashstate_auto))) {
            mFlashState = FLASH_STATE_AUTO;
        } else {
            mFlashState = FLASH_STATE_ON;
        }

        mLockedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

        mInverseScan = Utility.isInvertColorsEnabled(this);

        final String cameraId = Utility.getCameraId(this);
        if (cameraId.equals(getString(R.string.pref_cameraid_back))) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        final String focusingMode = Utility.getFocusingMode(this);
        if (focusingMode.equals(getString(R.string.pref_focusingmode_off))) {
            mBarcodeScannerView.getBarcodeView().getCameraSettings().setAutoFocusEnabled(false);
            mBarcodeScannerView.getBarcodeView().getCameraSettings().setContinuousFocusEnabled(false);
        } else if (focusingMode.equals(getString(R.string.pref_focusingmode_auto))) {
            mBarcodeScannerView.getBarcodeView().getCameraSettings().setAutoFocusEnabled(true);
            mBarcodeScannerView.getBarcodeView().getCameraSettings().setContinuousFocusEnabled(false);
        } else {
            mBarcodeScannerView.getBarcodeView().getCameraSettings().setAutoFocusEnabled(false);
            mBarcodeScannerView.getBarcodeView().getCameraSettings().setContinuousFocusEnabled(true);
        }

        mBarcodeScannerView.getBarcodeView().getCameraSettings().setExposureEnabled(
                Utility.isExposureEnabled(this)
        );
        mBarcodeScannerView.getBarcodeView().getCameraSettings().setMeteringEnabled(
                Utility.isMeteringEnabled(this)
        );
    }

    public void onSwitchFlashlight(View view) {
        if (mFlashState == FLASH_STATE_OFF) {
            mFlashState = FLASH_STATE_AUTO;
            reload();
        } else if (mFlashState == FLASH_STATE_AUTO) {
            mFlashState = FLASH_STATE_ON;
            reload();
        } else {
            mFlashState = FLASH_STATE_OFF;
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
        } else {
            mInverseScan = true;
        }
        reload();
    }

    public void onSwitchCamera(View view) {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        reload();
        updateLayoutSwitchCamera();
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
        if (mLockedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
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
        mCameraId = inState.getInt(KEY_CAMERA_ID);
    }

    private void saveStateToBundle(Bundle outState) {
        outState.putInt(KEY_FLASH_STATE, mFlashState);
        outState.putInt(KEY_LOCKED_ORIENTATION, mLockedOrientation);
        outState.putBoolean(KEY_INVERSE_SCAN, mInverseScan);
        outState.putInt(KEY_CAMERA_ID, mCameraId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
