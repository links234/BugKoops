package com.intel.bugkoops;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.intel.bugkoops.UI.ScanButton;

public class MainActivityFragment extends Fragment {

    final String LOG_TAG = getClass().getSimpleName();

    static final float SCAN_BUTTON_HEIGHT_PERCENT = 0.75f;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_main, container, false);

        final ScanButton scanBtn = (ScanButton) mView.findViewById(R.id.scan_button);

        scanBtn.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int scanBtnSize = Math.min(scanBtn.getHeight(), scanBtn.getWidth());
                scanBtnSize = (int) ((float) (scanBtnSize) * SCAN_BUTTON_HEIGHT_PERCENT);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(scanBtnSize, scanBtnSize);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                scanBtn.setLayoutParams(params);

                ViewTreeObserver obs = scanBtn.getViewTreeObserver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });

        return mView;
    }
}
