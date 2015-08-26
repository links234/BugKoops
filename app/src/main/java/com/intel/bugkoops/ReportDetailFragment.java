package com.intel.bugkoops;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import java.util.Date;

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

    public static final int REPORT_TYPE_TITLE = 1;
    public static final int REPORT_TYPE_DATE = 2;
    public static final int REPORT_TYPE_TEXT = 4;

    public static final int REPORT_TYPE_ALL = REPORT_TYPE_DATE | REPORT_TYPE_TEXT | REPORT_TYPE_TITLE;

    private TextView mDateView;
    private EditText mTitleEdit;
    private EditText mTextEdit;

    private long mInitialDate;
    private String mInitialTitle;
    private String mInitialText;

    private Snackbar mResultSnackbar;

    public ReportDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_report_detail, container, false);
        mDateView = (TextView) rootView.findViewById(R.id.detail_report_date_textview);
        mTitleEdit = (EditText) rootView.findViewById(R.id.detail_report_title_textview);
        mTextEdit = (EditText) rootView.findViewById(R.id.detail_report_text_textview);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(ReportDetailFragment.DETAIL_URI);
        } else {
            mInitialDate = BugKoopsContract.dateToDB(new Date());
            mInitialTitle = "";
            mInitialText = "";

            mDateView.setText(Utility.getDate(BugKoopsContract.dateFromDB(mInitialDate)));
            mTitleEdit.setText(mInitialTitle);
            mTextEdit.setText(mInitialText);
        }

        mResultSnackbar = Snackbar.make(
                rootView.findViewById(R.id.detail_report_snackbar),
                "",
                Snackbar.LENGTH_INDEFINITE);

        mResultSnackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultSnackbar.setDuration(0);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
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

            mInitialDate = date;
            mInitialTitle = title;
            mInitialText = text;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void save() {
        String title = mTitleEdit.getText().toString();
        String text = mTextEdit.getText().toString();

        final ContentResolver contentResolver = getActivity().getContentResolver();
        ContentValues reportValues = new ContentValues();

        if (mUri != null) {
            reportValues.put(BugKoopsContract.ReportEntry.COLUMN_TITLE, title);
            reportValues.put(BugKoopsContract.ReportEntry.COLUMN_TEXT, text);
            contentResolver.update(mUri, reportValues, null, null);
        } else {
            reportValues.put(BugKoopsContract.ReportEntry.COLUMN_DATE, mInitialDate);
            reportValues.put(BugKoopsContract.ReportEntry.COLUMN_TITLE, title);
            reportValues.put(BugKoopsContract.ReportEntry.COLUMN_TEXT, text);

            mUri = contentResolver.insert(
                    BugKoopsContract.ReportEntry.CONTENT_URI,
                    reportValues
            );
        }
        mInitialText = text;
        mInitialTitle = title;
    }

    public boolean modified() {
        String title = mTitleEdit.getText().toString();
        String text = mTextEdit.getText().toString();

        return !mInitialTitle.equals(title) || !mInitialText.equals(text);
    }

    public boolean firstTime() {
        return mUri == null;
    }

    public long getDate() {
        return mInitialDate;
    }

    public String getTitle() {
        return mInitialTitle;
    }

    public String getText() {
        return mInitialText;
    }

    public String getReport(int reportType) {
        String report = "";
        if ((reportType & REPORT_TYPE_TITLE) != 0) {
            report += "<title>\n" + mInitialTitle + "\n</title>\n";
        }
        if ((reportType & REPORT_TYPE_DATE) != 0) {
            report += "<date>\n" + Utility.getDate(BugKoopsContract.dateFromDB(mInitialDate)) + "\n</date>\n";
        }
        if ((reportType & REPORT_TYPE_TEXT) != 0) {
            report += "<text>\n" + mInitialText + "\n</text>\n";
        }

        return report;
    }

    public void setResultSlackbar(String message) {
        mResultSnackbar.setText(message).show();
    }
}