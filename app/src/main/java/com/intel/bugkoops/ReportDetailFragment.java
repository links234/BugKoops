package com.intel.bugkoops;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.intel.bugkoops.Data.BugKoopsContract;

public class ReportDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = ReportDetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            BugKoopsContract.ReportEntry._ID,
            BugKoopsContract.ReportEntry.COLUMN_DATE,
            BugKoopsContract.ReportEntry.COLUMN_TITLE,
            BugKoopsContract.ReportEntry.COLUMN_TEXT
    };

    public static final int COL_REPORT_ID = 0;
    public static final int COL_REPORT_DATE = 1;
    public static final int COL_REPORT_TITLE = 2;
    public static final int COL_REPORT_TEXT = 3;

    private TextView mDateView;
    private EditText mTitleEdit;
    private EditText mTextEdit;

    public ReportDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(ReportDetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_report_detail, container, false);
        mDateView = (TextView) rootView.findViewById(R.id.detail_report_date_textview);
        mTitleEdit = (EditText) rootView.findViewById(R.id.detail_report_title_textview);
        mTextEdit = (EditText) rootView.findViewById(R.id.detail_report_text_textview);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            long date = data.getLong(COL_REPORT_DATE);
            String title = data.getString(COL_REPORT_TITLE);
            String text = data.getString(COL_REPORT_TEXT);

            mDateView.setText(Utility.getDate(BugKoopsContract.dateFromDB(date)));
            mTitleEdit.setText(title);
            mTextEdit.setText(text);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void save() {
        ContentValues reportValues = new ContentValues();
        reportValues.put(BugKoopsContract.ReportEntry.COLUMN_TITLE, mTitleEdit.getText().toString());
        reportValues.put(BugKoopsContract.ReportEntry.COLUMN_TEXT, mTextEdit.getText().toString());
        final ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.update(mUri, reportValues, null, null);
    }
}