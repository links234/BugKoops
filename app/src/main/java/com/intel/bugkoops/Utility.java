package com.intel.bugkoops;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {
    public static String getCameraId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_cameraid_key),
                context.getString(R.string.pref_cameraid_default));
    }

    public static String getFlashState(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_flashstate_key),
                context.getString(R.string.pref_flashstate_default));
    }

    public static String getFocusingMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_focusingmode_key),
                context.getString(R.string.pref_focusingmode_default));
    }

    public static boolean isInvertColorsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_invertcolors_key),
                Boolean.getBoolean(context.getString(R.string.pref_invertcolors_default)));
    }

    public static boolean isMeteringEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_metering_key),
                Boolean.getBoolean(context.getString(R.string.pref_metering_default)));
    }

    public static boolean isExposureEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_exposure_key),
                Boolean.getBoolean(context.getString(R.string.pref_exposure_default)));
    }

    public static boolean isFullscreenEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_fullscreen_key),
                Boolean.getBoolean(context.getString(R.string.pref_fullscreen_default)));
    }
}
