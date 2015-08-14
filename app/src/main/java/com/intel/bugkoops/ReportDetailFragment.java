package com.intel.bugkoops;

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
    private EditText mTitleView;
    private EditText mTextView;

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
        mTitleView = (EditText) rootView.findViewById(R.id.detail_report_title_textview);
        mTextView = (EditText) rootView.findViewById(R.id.detail_report_text_textview);
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
            mTitleView.setText(title);
            mTextView.setText(text);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}