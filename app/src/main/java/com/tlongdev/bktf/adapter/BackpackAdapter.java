package com.tlongdev.bktf.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.activity.ItemDetailActivity;
import com.tlongdev.bktf.activity.UserBackpackActivity;
import com.tlongdev.bktf.data.ItemSchemaDbHelper;
import com.tlongdev.bktf.model.Tf2Item;

import java.io.IOException;
import java.io.InputStream;

/**
 * Adapter for the recycler view in the backpack activity-
 */
public class BackpackAdapter extends RecyclerView.Adapter<BackpackAdapter.ViewHolder> {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = BackpackAdapter.class.getSimpleName();

    /**
     * View types
     */
    public static final int VIEW_TYPE_ITEM = 0;
    public static final int VIEW_TYPE_HEADER = 1;

    /**
     * Cursor containing the non new items.
     */
    private Cursor mDataSet;

    /**
     * Cursor containing the new items (items with no slot in the backpack)
     */
    private Cursor mDataSetNew;

    /**
     * The context.
     */
    private Context mContext;

    /**
     * Determines which table to read from.
     */
    private boolean isGuest = false;

    /**
     * The number of new items.
     */
    private int newItemSlots = 0;

    /**
     * Database helper for getting the name of the item.
     * TODO replace with a join after combining databases
     */
    private ItemSchemaDbHelper mDbHelper;

    /**
     * Constructor
     *
     * @param context the context
     * @param isGuest determines which table to read from
     */
    public BackpackAdapter(Context context, boolean isGuest) {
        this.mContext = context;
        this.isGuest = isGuest;
        mDbHelper = new ItemSchemaDbHelper(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycler_backpack, parent, false);
                break;
            case VIEW_TYPE_HEADER:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycler_backpack_header, parent, false);
                break;
            default:
                return null;
        }

        return new ViewHolder(v, viewType);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            //Header type
            case VIEW_TYPE_HEADER:
                //Check if there are new items or not
                if (newItemSlots > 0) {
                    if (position == 0) {
                        //Header of the new items
                        holder.header.setText(mContext.getString(R.string.header_new_items));
                    } else {
                        //Header of pages
                        holder.header.setText(mContext.getString(R.string.header_page,
                                (position - newItemSlots) / 51 + 1));
                    }
                } else {
                    //Header of pages
                    holder.header.setText(mContext.getString(R.string.header_page,
                            position / 51 + 1));
                }
                break;
            //Backpack item type
            case VIEW_TYPE_ITEM:
                //Determine which cursor to get the item from.
                Cursor currentCursor;
                int cursorPosition;
                if (newItemSlots > 0) {
                    //Check if there are new items or not
                    if (position < newItemSlots) {
                        //Item is a new item
                        currentCursor = mDataSetNew;
                        cursorPosition = (position - 1);
                    } else {
                        //Item is not a new item
                        currentCursor = mDataSet;
                        cursorPosition = ((position - newItemSlots) - ((position - newItemSlots) / 51) - 1);
                    }
                } else {
                    //There is no new item in the backpack
                    currentCursor = mDataSet;
                    cursorPosition = (position - (position / 51) - 1);
                }

                //Reset item slot to an empty slot
                holder.icon.setImageDrawable(null);
                holder.effect.setImageDrawable(null);
                holder.paint.setImageDrawable(null);
                holder.root.setCardBackgroundColor(Utility.getColor(mContext, R.color.card_color));
                holder.root.setOnClickListener(null);

                if (currentCursor.moveToPosition(cursorPosition)) {
                    //Get all the data from the cursor
                    final Tf2Item item = new Tf2Item(
                            currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_DEFI),
                            null,
                            currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_QUAL),
                            Math.abs(currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_TRAD) - 1) == 1,
                            Math.abs(currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_CRAF) - 1) == 1,
                            currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_AUS) == 1,
                            currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_INDE),
                            currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_WEAR),
                            null
                    );

                    final int id = currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_ID);
                    int paint = currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_PAIN);

                    if (item.getDefindex() != 0) {

                        //Set the background to the color of the quality
                        holder.root.setCardBackgroundColor(item.getColor(mContext, true));

                        //Load the image on a background thread to avoid hiccups
                        ImageLoader task = (ImageLoader) holder.icon.getTag();
                        if (task != null) {
                            task.cancel(true);
                        }
                        task = new ImageLoader(mContext, holder.icon, holder.effect, holder.paint, item, paint);
                        holder.icon.setTag(task);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        //The on click listener for an item
                        holder.root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Open the ItemDetailActivity and send some extra data to it
                                Intent i = new Intent(mContext, ItemDetailActivity.class);
                                i.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, id);
                                i.putExtra(ItemDetailActivity.EXTRA_GUEST, isGuest);
                                Cursor itemCursor = mDbHelper.getItem(item.getDefindex());
                                if (itemCursor != null && itemCursor.moveToFirst()) {
                                    i.putExtra(ItemDetailActivity.EXTRA_ITEM_NAME,
                                            itemCursor.getString(0));
                                    i.putExtra(ItemDetailActivity.EXTRA_ITEM_TYPE,
                                            itemCursor.getString(1));
                                    i.putExtra(ItemDetailActivity.EXTRA_PROPER_NAME,
                                            itemCursor.getInt(2));
                                    itemCursor.close();
                                }

                                if (Build.VERSION.SDK_INT >= 21) {
                                    //Fancy shared elements transition if above 20
                                    @SuppressWarnings("unchecked")
                                    ActivityOptions options = ActivityOptions
                                            .makeSceneTransitionAnimation((Activity) mContext,
                                                    Pair.create((View) holder.icon, "icon_transition"),
                                                    Pair.create((View) holder.effect, "effect_transition"),
                                                    Pair.create((View) holder.paint, "paint_transition"),
                                                    Pair.create((View) holder.root, "background_transition"));
                                    mContext.startActivity(i, options.toBundle());
                                } else {
                                    mContext.startActivity(i);
                                }
                            }
                        });
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        //Magic. Not really.
        int count = mDataSet == null ? 0 : mDataSet.getCount() + mDataSet.getCount() / 50;
        if (newItemSlots > 0) {
            return count + newItemSlots;
        } else {
            return count;
        }
    }

    @Override
    public int getItemViewType(int position) {
        //More black magic
        //Check if there are new items
        if (newItemSlots > 0) {
            if (position < newItemSlots) {
                if (position == 0) {
                    return VIEW_TYPE_HEADER;
                } else {
                    return VIEW_TYPE_ITEM;
                }
            } else {
                if ((position - newItemSlots) % 51 == 0) {
                    return VIEW_TYPE_HEADER;
                } else {
                    return VIEW_TYPE_ITEM;
                }
            }
        } else if (position % 51 == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    /**
     * Replaces the dataset of the adapter.
     *
     * @param cursor    the cursor for the items that has a slot in the backpack
     * @param cursorNew the cursor for items without a slot
     */
    public void swapCursor(Cursor cursor, Cursor cursorNew) {
        //Since this is used with a loader, there is no need to manually close the previous cursors.
        mDataSet = cursor;
        mDataSetNew = cursorNew;
        if (mDataSetNew != null && mDataSetNew.getCount() > 0) {
            newItemSlots = mDataSetNew.getCount() - mDataSetNew.getCount() % 5 + 6;
        }
        notifyDataSetChanged();
    }

    /**
     * View holder for the views.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * Views
         */
        public TextView header = null;
        public ImageView icon = null;
        public ImageView effect = null;
        public ImageView paint = null;
        public CardView root = null;

        /**
         * Constructor
         *
         * @param view     the root view
         * @param viewType the type of the view in the list
         */
        public ViewHolder(View view, int viewType) {
            super(view);
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    header = (TextView) view.findViewById(R.id.text_view_header);
                    break;
                case VIEW_TYPE_ITEM:
                    icon = (ImageView) view.findViewById(R.id.icon);
                    effect = (ImageView) view.findViewById(R.id.effect);
                    paint = (ImageView) view.findViewById(R.id.paint);
                    root = (CardView) view;
                    break;
            }
        }
    }

    /**
     * A task te loads images in the background.
     */
    private class ImageLoader extends AsyncTask<Void, Void, Drawable[]> {

        /**
         * Image views to set the images to
         */
        private ImageView icon;
        private ImageView effect;
        private ImageView paintView;

        private Tf2Item item;

        private int paint;

        /**
         * The context
         */
        private Context mContext;

        /**
         * Constructor
         *
         * @param context the context
         * @param icon    the icon of the item
         * @param effect  the effect of the item
         * @param paint   the pain indicator of the item
         */
        private ImageLoader(Context context, ImageView icon, ImageView effect, ImageView paintView, Tf2Item item, int paint) {
            this.mContext = context;
            this.icon = icon;
            this.effect = effect;
            this.paintView = paintView;
            this.item = item;
            this.paint = paint;
        }

        @Override
        protected Drawable[] doInBackground(Void... params) {
            //Three drawables will be returned at most
            Drawable[] drawables = new Drawable[3];

            try {
                InputStream ims;
                AssetManager assetManager = mContext.getAssets();

                drawables[0] = item.getIconDrawable(mContext);
                drawables[1] = item.getEffectDrawable(mContext);

                //Get the paint indicator if needed
                if (Utility.isPaint(paint)) {
                    ims = assetManager.open("paint/" + params[5] + ".png");
                    drawables[2] = Drawable.createFromStream(ims, null);
                }

            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
            }

            return drawables;
        }

        @Override
        protected void onPostExecute(Drawable[] drawables) {
            //Set the images
            icon.setImageDrawable(drawables[0]);
            effect.setImageDrawable(drawables[1]);
            paintView.setImageDrawable(drawables[2]);
        }
    }
}
