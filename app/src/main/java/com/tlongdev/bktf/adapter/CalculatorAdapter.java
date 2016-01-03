package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.Item;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for the recycler view of the calculator.
 */
public class CalculatorAdapter extends RecyclerView.Adapter<CalculatorAdapter.ViewHolder> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = CalculatorAdapter.class.getSimpleName();

    /**
     * The context
     */
    private Context mContext;

    private ArrayList<Item> mDataSet;
    private ArrayList<Integer> mCountSet;

    /**
     * The listener that will be notified when an items is edited.
     */
    private OnItemEditListener listener;

    /**
     * Constructor.
     *
     * @param context the context
     */
    public CalculatorAdapter(Context context, ArrayList<Item> dataSet) {
        mContext = context;
        mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_calculator, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (mDataSet != null && mDataSet.size() > position) {

            final Item item = mDataSet.get(position);

            int count = mCountSet.get(position);

            Glide.with(mContext)
                    .load(item.getIconUrl(mContext))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.icon);

            holder.name.setText(item.getFormattedName(mContext));

            //Put the number of items behind the name if it is higher than 1
            if (count > 1) {
                holder.name.append(String.format(" %dx", count));
            }

            holder.effect.setBackgroundColor(item.getColor(mContext, true));

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        //Notify listener that an item was deleted
                        listener.onItemDeleted(item, 1);
                        notifyItemRemoved(mDataSet.indexOf(item));
                        mDataSet.remove(item);
                    }
                    notifyDataSetChanged();
                }
            });

            if (!item.isTradable()) {
                holder.quality.setVisibility(View.VISIBLE);
                if (!item.isCraftable()) {
                    holder.quality.setImageResource(R.drawable.uncraft_untrad);
                } else {
                    holder.quality.setImageResource(R.drawable.untrad);
                }
            } else if (!item.isCraftable()) {
                holder.quality.setVisibility(View.VISIBLE);
                holder.quality.setImageResource(R.drawable.uncraft);
            } else {
                holder.quality.setVisibility(View.GONE);
            }

            if (item.getPrice() != null) {
                holder.price.setText(item.getPrice().getFormattedPrice(mContext));
            } else {
                holder.price.setText("Price unknown");
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    public void setDataSet(ArrayList<Item> dataSet, ArrayList<Integer> count) {
        mDataSet = dataSet;
        mCountSet = count;
    }

    /**
     * Set the listener that will be notified when an items is edited/deleted
     *
     * @param listener the listener to be set
     */
    public void setOnItemDeletedListener(OnItemEditListener listener) {
        this.listener = listener;
    }

    public void clearDataSet() {
        mDataSet.clear();
        mCountSet.clear();
    }

    /**
     * The view holder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.icon) ImageView icon;
        @Bind(R.id.name) TextView name;
        @Bind(R.id.price) TextView price;
        @Bind(R.id.effect) ImageView effect;
        @Bind(R.id.delete) ImageView delete;
        @Bind(R.id.quality) ImageView quality;
        @Bind(R.id.count) EditText count;

        /**
         * Constructor
         *
         * @param view the root view
         */
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * Interface that will be notified when an item is edited/deleted.
     */
    public interface OnItemEditListener {
        /**
         * Called when an item is deleted.
         *
         * @param item  the id of the item
         * @param count the number of the item(s)
         */
        void onItemDeleted(Item item, int count);
    }
}
