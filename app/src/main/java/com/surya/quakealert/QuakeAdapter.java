package com.surya.quakealert;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Surya on 13-02-2017.
 */
public class QuakeAdapter extends CursorRecyclerViewAdapter<QuakeAdapter.QuakeViewHolder> {

    private Context mContext;
    ListItemClickListener itemClickListener;

    public interface ListItemClickListener{
        void onListItemClick(int position);
    }

    QuakeAdapter(Context context,Cursor cursor,ListItemClickListener itemClickListener) {
        super(context,cursor);
        mContext = context;
        this.itemClickListener = itemClickListener;
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
            String[] place = cursor.getString(2).split("of");
            if (place.length > 1)
                holder.mTitle.setText(cursor.getString(2).split("of")[1]);
            else
                holder.mTitle.setText(cursor.getString(2).split("of")[0]);
            SimpleDateFormat sdf_actual = new SimpleDateFormat("h:mm a",Locale.ENGLISH);

            Date current = new Date(System.currentTimeMillis());
            Date quakeDate = new Date(cursor.getLong(3));
            holder.mTime.setText((current.getTime() - quakeDate.getTime())/(60*60*1000)%24
                    + " hrs ago, "
                    + sdf_actual.format(quakeDate));
        }
    }

    class QuakeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.magnitude)
        TextView mMagnitude;
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.time)
        TextView mTime;

        QuakeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
