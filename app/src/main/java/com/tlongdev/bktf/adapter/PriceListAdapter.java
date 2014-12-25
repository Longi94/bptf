package com.tlongdev.bktf.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tlongdev.bktf.R;

import java.util.ArrayList;

/**
 * Created by ThanhLong on 2014.12.24..
 */
public class PriceListAdapter extends RecyclerView.Adapter<PriceListAdapter.ViewHolder> {

    private final ArrayList<String> mDataset;

    public PriceListAdapter(ArrayList<String> mDataset) {
        this.mDataset = mDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_changes, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.text.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView text;

        public ViewHolder(View view) {
            super(view);
            text = (TextView)view.findViewById(R.id.list_item_changes);
        }
    }
}
