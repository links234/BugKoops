package com.intel.bugkoops;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    public static String summary(String text, int numberOfLines, int charsPerLine) {
        String summary = new String();
        String[] lines = text.split("\\r?\\n",numberOfLines);
        for(String line : lines) {
            if(summary.length() + line.length() >= charsPerLine*numberOfLines)
            {
                break;
            }
            summary += line;
            summary += "\n";
        }

        if(summary.length()==0)
        {
            return summary;
        }
        return summary.substring(0,Math.min(charsPerLine*numberOfLines,summary.length()-1));
    }

    public static String summarySmall(String text) {
        final int NUMBER_OF_LINES = 3;
        final int CHARS_PER_LINE = 21;

        return summary(text, NUMBER_OF_LINES, CHARS_PER_LINE);
    }

    public static String summaryMedium(String text) {
        final int NUMBER_OF_LINES = 7;
        final int CHARS_PER_LINE = 25;

        return summary(text, NUMBER_OF_LINES, CHARS_PER_LINE);
    }

    public static String getPrettyDate(Date date) {
        Date endDate   = new Date();

        long duration  = endDate.getTime() - date.getTime();

        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
        long diffInDays = endDate.getDay() - date.getDay();


        if(diffInDays==0) {
            if(diffInHours>0) {
                if(diffInHours == 1) {
                    return Long.toString(diffInHours) + " hour ago";
                } else {
                    return Long.toString(diffInHours) + " hours ago";
                }
            } else if(diffInMinutes>0) {
                if(diffInMinutes == 1) {
                    return Long.toString(diffInMinutes) + " minute ago";
                } else {
                    return Long.toString(diffInMinutes) + " minutes ago";
                }
            } else if(diffInSeconds>0) {
                if(diffInSeconds == 1) {
                    return Long.toString(diffInSeconds) + " second ago";
                } else {
                    return Long.toString(diffInSeconds) + " seconds ago";
                }
            } else {
                return "Just now";
            }
        } else if(diffInDays==1) {
            final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            return "Yesterday at "+dateFormat.format(date);
        } else {
            final DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM HH:mm:ss");
            return dateFormat.format(date);
        }
    }

    public static String getDate(Date date) {
        final DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM HH:mm:ss");
        return dateFormat.format(date);
    }
}
