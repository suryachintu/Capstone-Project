package com.surya.quakealert;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Surya on 13-02-2017.
 */
public class QuakeAdapter extends CursorRecyclerViewAdapter<QuakeAdapter.QuakeViewHolder> {

    private Context mContext;

    QuakeAdapter(Context context,Cursor cursor) {
        super(context,cursor);
        mContext = context;
    }

    @Override
    public QuakeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.quake_list_item,parent,false);

        return new QuakeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QuakeViewHolder holder, Cursor cursor) {
        if (cursor!=null){
            holder.mMagnitude.setText(String.valueOf(cursor.getDouble(1)));
            holder.mTitle.setText(cursor.getString(2).split("of")[1]);
            SimpleDateFormat sdf_actual = new SimpleDateFormat("h:mm a",Locale.ENGLISH);

            Date current = new Date(System.currentTimeMillis());
            Date quakeDate = new Date(cursor.getLong(3));
            holder.mTime.setText((current.getTime() - quakeDate.getTime())/(60*60*1000)%24
                    + " hrs ago, "
                    + sdf_actual.format(quakeDate));
        }
    }

    class QuakeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.magnitude)
        TextView mMagnitude;
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.time)
        TextView mTime;

        QuakeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
