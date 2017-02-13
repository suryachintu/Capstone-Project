package com.surya.quakealert;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Surya on 13-02-2017.
 */
public class QuakeAdapter extends RecyclerView.Adapter<QuakeAdapter.QuakeViewHolder> {

    private Context mContext;

    QuakeAdapter(Context context) {
        mContext = context;
    }

    @Override
    public QuakeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.quake_list_item,parent,false);

        return new QuakeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QuakeViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class QuakeViewHolder extends RecyclerView.ViewHolder {
        QuakeViewHolder(View itemView) {
            super(itemView);
        }
    }
}
