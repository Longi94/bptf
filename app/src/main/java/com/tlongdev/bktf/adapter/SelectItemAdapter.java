/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.model.Item;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Long on 2015. 12. 12..
 */
public class SelectItemAdapter extends RecyclerView.Adapter<SelectItemAdapter.ViewHolder> {

    /**
     * The data set
     */
    private Cursor mDataSet;

    /**
     * The context
     */
    private Context mContext;

    private OnItemSelectedListener listener;

    public SelectItemAdapter(Context context, Cursor dataSet) {
        this.mDataSet = dataSet;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_select_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataSet != null && mDataSet.moveToPosition(position)) {
            final Item item = new Item();
            item.setDefindex(mDataSet.getInt(mDataSet.getColumnIndex(ItemSchemaEntry.COLUMN_DEFINDEX)));
            item.setName(mDataSet.getString(mDataSet.getColumnIndex(ItemSchemaEntry.COLUMN_ITEM_NAME)));

            holder.name.setText(item.getName());

            Glide.with(mContext)
                    .load(item.getIconUrl(mContext))
                    .into(holder.icon);

            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemSelected(item.getDefindex(), item.getName());
                    }
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
     * @param closePrevious whether to close the previous cursor
     */
    public void swapCursor(Cursor data, boolean closePrevious) {
        if (closePrevious && mDataSet != null) mDataSet.close();
        mDataSet = data;
        notifyDataSetChanged();
    }

    public void setListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.icon) ImageView icon;
        @Bind(R.id.name) TextView name;
        View root;

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
