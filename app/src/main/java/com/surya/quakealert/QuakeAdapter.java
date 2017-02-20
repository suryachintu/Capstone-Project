package com.surya.quakealert;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.surya.quakealert.R.id.magnitude;

/**
 * Created by Surya on 13-02-2017.
 */
public class QuakeAdapter extends CursorRecyclerViewAdapter<QuakeAdapter.QuakeViewHolder> {

    private Context mContext;
    private ListItemClickListener itemClickListener;

    public interface ListItemClickListener{
        void onListItemClick(int position,QuakeViewHolder holder);
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
            holder.mTitle.setText(Utility.getFormattedTitle(cursor.getString(2)));
            SimpleDateFormat sdf_actual = new SimpleDateFormat("h:mm a",Locale.ENGLISH);

            GradientDrawable magnitudeCircle = (GradientDrawable) holder.mIcon.getBackground();

            //Get the appropriate background color based on the current earthquake magnitude
            int magnitudeColor = Utility.getMagnitudeBg(cursor.getDouble(1),mContext);

            magnitudeCircle.setColor(magnitudeColor);
            Date current = new Date(System.currentTimeMillis());
            Date quakeDate = new Date(cursor.getLong(3));
            holder.mTime.setText((current.getTime() - quakeDate.getTime())/(60*60*1000)%24
                    + " hrs ago, "
                    + sdf_actual.format(quakeDate));

            //content descriptions
            holder.mMagnitude.setContentDescription(mContext.getString(R.string.a11y_magnitude, holder.mMagnitude.getText()));
            holder.mTitle.setContentDescription(mContext.getString(R.string.a11y_title,holder.mTitle.getText()));
            holder.mTime.setContentDescription(mContext.getString(R.string.a11y_time,holder.mTime.getText()));

        }
    }

    class QuakeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(magnitude)
        TextView mMagnitude;
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.time)
        TextView mTime;
        @BindView(R.id.icon_view)
        ImageView mIcon;

        QuakeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onListItemClick(getAdapterPosition(),this);
        }
    }
}
