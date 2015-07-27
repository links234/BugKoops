package com.intel.bugkoops;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class ResetPreferencesDialog extends DialogPreference
{
    protected Context mContext;

    public ResetPreferencesDialog(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mContext = context;
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        super.onClick(dialog, which);

        if(which == DialogInterface.BUTTON_POSITIVE)
        {
            SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

            preferencesEditor.clear();
            PreferenceManager.setDefaultValues(mContext, R.xml.pref_general, true);

            preferencesEditor.commit();

            getOnPreferenceChangeListener().onPreferenceChange(this, true);
        }
    }
}
