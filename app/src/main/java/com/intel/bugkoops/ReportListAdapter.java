package com.intel.bugkoops;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ReportListAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_LAST = 0;
    private static final int VIEW_TYPE_OTHER = 1;

    private boolean mUseLastLayout = true;

    public static class ViewHolder {
        public final TextView dateView;
        public final TextView titleView;
        public final TextView descriptionView;

        public ViewHolder(View view) {
            dateView = (TextView) view.findViewById(R.id.list_item_report_date_textview);
            titleView = (TextView) view.findViewById(R.id.list_item_report_title_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_report_description_textview);
        }
    }

    public ReportListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_LAST: {
                layoutId = R.layout.list_item_last_report;
                break;
            }
            case VIEW_TYPE_OTHER: {
                layoutId = R.layout.list_item_report;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        long dateInMillis = cursor.getLong(ReportListFragment.COL_REPORT_DATE);
        String title = cursor.getString(ReportListFragment.COL_REPORT_TITLE);
        String description = Utility.summary(cursor.getString(ReportListFragment.COL_REPORT_TEXT));

        viewHolder.dateView.setText(Long.toString(dateInMillis));
        viewHolder.titleView.setText(title);
        viewHolder.descriptionView.setText(description);

    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseLastLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseLastLayout) ? VIEW_TYPE_LAST : VIEW_TYPE_OTHER;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}