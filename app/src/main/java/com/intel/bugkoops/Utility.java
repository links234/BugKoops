package com.intel.bugkoops;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

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

    public static void showAbout(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View messageView = inflater.inflate(R.layout.about, null, false);

        TextView creditsTextView = (TextView) messageView.findViewById(R.id.about_credits);
        creditsTextView.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    public static int bytesToInt(byte b0, byte b1) {
        return ((0xFF & b0) << 8) | (0xFF & b1);
    }

    public static int bytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return ((0xFF & b0) << 24) | ((0xFF & b1) << 16) | ((0xFF & b2) << 8) | (0xFF & b3);
    }

    public static String bytesToString(byte[] data) {
        String text="";
        try {
            text = new String(data, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Unsupported encoding !");
        }
        return text;
    }
}
