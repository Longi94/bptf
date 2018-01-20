package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectItemAdapter extends RecyclerView.Adapter<SelectItemAdapter.ViewHolder> {

    @Inject Context mContext;

    private Cursor mDataSet;

    private OnItemSelectedListener listener;

    public SelectItemAdapter(BptfApplication application) {
        application.getAdapterComponent().inject(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_select_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {
            final Item item = new Item();
            item.setDefindex(mDataSet.getInt(mDataSet.getColumnIndex("defindex")));
            item.setName(mDataSet.getString(mDataSet.getColumnIndex("item_name")));

            holder.name.setText(item.getName());

            Glide.with(mContext)
                    .load(item.getIconUrl(mContext))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.icon);

            holder.root.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemSelected(item.getDefindex(), item.getName());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.getCount();
    }

    /**
     * Replaces the cursor of the adapter
     *
     * @param data          the cursor that will replace the current one
     */
    public void swapCursor(Cursor data) {
        if (mDataSet != null) mDataSet.close();
        mDataSet = data;
        notifyDataSetChanged();
    }

    public void setListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public void closeCursor() {
        if (mDataSet != null) {
            mDataSet.close();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon) ImageView icon;
        @BindView(R.id.name) TextView name;
        final View root;

        public ViewHolder(View view) {
            super(view);
            root = view;
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int defindex, String name);
    }
}
