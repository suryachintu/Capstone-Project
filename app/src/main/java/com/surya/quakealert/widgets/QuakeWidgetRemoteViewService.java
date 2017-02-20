package com.surya.quakealert.widgets;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.surya.quakealert.R;
import com.surya.quakealert.Utility;
import com.surya.quakealert.data.QuakeContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by Surya on 17-02-2017.
 */

public class QuakeWidgetRemoteViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();



                String selection = QuakeContract.QuakeEntry.COLUMN_QUAKE_MAGNITUDE + " >= ?";
                String sortOrder = QuakeContract.QuakeEntry.COLUMN_QUAKE_MAGNITUDE + " DESC";
                // This is the same query from MyStocksActivity
                data = getContentResolver().query(
                        QuakeContract.QuakeEntry.CONTENT_URI,
                        null,
                        selection,
                        new String[]{Utility.getPreference(getApplicationContext(),getString(R.string.quake_order_by_key))},
                        sortOrder);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                // Get the layout
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                // Bind data to the views
                views.setTextViewText(R.id.magnitude,String.valueOf(data.getDouble(1)));

                String[] place = data.getString(2).split("of");
                if (place.length > 1)
                    views.setTextViewText(R.id.title,data.getString(2).split("of")[1]);
                else
                    views.setTextViewText(R.id.title,data.getString(2).split("of")[0]);
                SimpleDateFormat sdf_actual = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

                Date current = new Date(System.currentTimeMillis());
                Date quakeDate = new Date(data.getLong(3));
                views.setTextViewText(R.id.time,(current.getTime() - quakeDate.getTime())/(60*60*1000)%24
                        + " hrs ago, "
                        + sdf_actual.format(quakeDate));

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(getString(R.string.quake_extra),data.getInt(0));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null; // use the default loading view
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                // Get the row ID for the view at the specified position
                if (data != null && data.moveToPosition(position)) {
                    final int _ID_COL = 0;
                    return data.getLong(_ID_COL);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}
