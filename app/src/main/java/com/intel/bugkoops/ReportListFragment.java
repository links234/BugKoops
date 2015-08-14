package com.intel.bugkoops;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.Loader;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.intel.bugkoops.Data.BugKoopsContract;

public class ReportListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = ReportListFragment.class.getSimpleName();
    private ReportListAdapter mReportListAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    private static final String SELECTED_KEY = "selected_position";

    private static final int REPORT_LOADER = 0;

    private static final String[] REPORT_COLUMNS = {
            BugKoopsContract.ReportEntry._ID,
            BugKoopsContract.ReportEntry.COLUMN_DATE,
            BugKoopsContract.ReportEntry.COLUMN_TITLE,
            BugKoopsContract.ReportEntry.COLUMN_TEXT
    };

    static final int COL_REPORT_ID = 0;
    static final int COL_REPORT_DATE = 1;
    static final int COL_REPORT_TITLE = 2;
    static final int COL_REPORT_TEXT = 3;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        void onItemSelected(Uri dateUri);
    }

    public ReportListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, ReportListFragment.this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mReportListAdapter = new ReportListAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_report, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listview_report);
        mListView.setAdapter(mReportListAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(BugKoopsContract.ReportEntry.buildUriFromId(cursor.getLong(COL_REPORT_ID)
                            ));
                }
                mPosition = position;
            }
        });

        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = mListView.getCheckedItemCount();
                mode.setTitle(checkedCount + " Selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_report_list_modal, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                final SparseBooleanArray checked = mListView.getCheckedItemPositions();
                switch (item.getItemId()) {
                    case R.id.action_report_list_modal_select_all:
                        for (int position = 0; position < mListView.getAdapter().getCount(); ++position) {
                            if(!checked.get(position)) {
                                mListView.performItemClick(
                                        mListView.getAdapter().getView(position, null, null),
                                        position,
                                        mListView.getAdapter().getItemId(position));
                            }
                        }
                        return false;
                    case R.id.action_report_list_modal_delete:
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        final ContentResolver contentResolver = getActivity().getContentResolver();
                                        for (int i = 0; i < checked.size(); i++) {
                                            Cursor cursor = (Cursor) mReportListAdapter.getItem(checked.keyAt(i));
                                            long id = cursor.getLong(COL_REPORT_ID);
                                            contentResolver.delete(BugKoopsContract.ReportEntry.buildUriFromId(id),
                                                    null, null);
                                        }
                                        getLoaderManager().restartLoader(0, null, ReportListFragment.this);
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        alertDialog.setMessage(ReportListFragment.this.getString(R.string.dialog_delete_reports_question))
                                .setPositiveButton(ReportListFragment.this.getString(R.string.dialog_positive), dialogClickListener)
                                .setNegativeButton(ReportListFragment.this.getString(R.string.dialog_negative), dialogClickListener).show();

                        return false;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mReportListAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(REPORT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = BugKoopsContract.ReportEntry.COLUMN_DATE + " DESC";

        Uri reportUri = BugKoopsContract.ReportEntry.buildUriFromStartDate(0);

        return new CursorLoader(getActivity(),
                reportUri,
                REPORT_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mReportListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mReportListAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mReportListAdapter != null) {
            mReportListAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }
}
