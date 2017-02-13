package com.surya.quakealert;

import android.content.Context;
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
public class QuakeAdapter extends RecyclerView.Adapter<QuakeAdapter.QuakeViewHolder> {

    private Context mContext;
    private ArrayList<QuakeModel> quakeModels;

    QuakeAdapter(Context context, ArrayList<QuakeModel> quakeModels) {
        mContext = context;
        this.quakeModels = quakeModels;
    }

    @Override
    public QuakeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.quake_list_item,parent,false);

        return new QuakeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QuakeViewHolder holder, int position) {
        if (quakeModels!=null){
            holder.mMagnitude.setText(quakeModels.get(position).getMag().toString());
            holder.mTitle.setText(quakeModels.get(position).getPlace().split("of")[1]);
            SimpleDateFormat sdf_diff = new SimpleDateFormat("h",Locale.ENGLISH);
            SimpleDateFormat sdf_actual = new SimpleDateFormat("h:mm a",Locale.ENGLISH);

            Date current = new Date(System.currentTimeMillis());
            Date quakeDate = new Date(quakeModels.get(position).getTime());
            holder.mTime.setText((current.getTime() - quakeDate.getTime())/(60*60*1000)%24
                                          + " hrs ago, "
                                          + sdf_actual.format(quakeDate));
        }
    }

    @Override
    public int getItemCount() {
        return quakeModels == null ?  0 : quakeModels.size();
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
