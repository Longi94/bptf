package com.tlongdev.bktf.adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.ItemDetailActivity;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.UserBackpackActivity;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.data.ItemSchemaDbHelper;

import java.io.IOException;
import java.io.InputStream;

public class BackpackSectionHeaderAdapter extends RecyclerView.Adapter<BackpackSectionHeaderAdapter.ViewHolder> {

    public static final int VIEW_TYPE_ITEM_ROW = 0;
    public static final int VIEW_TYPE_HEADER = 1;

    private Cursor mDataSet;
    private Cursor mDataSetNew;

    private Context mContext;
    private boolean isGuest = false;
    private boolean hasNewItems = false;
    private int newRows = 0;
    private ItemSchemaDbHelper mDbHelper;

    public BackpackSectionHeaderAdapter(Context mContext, boolean isGuest) {
        this.mContext = mContext;
        this.isGuest = isGuest;
        mDbHelper = new ItemSchemaDbHelper(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType){
            case VIEW_TYPE_ITEM_ROW:
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
        switch (getItemViewType(position)){
            case VIEW_TYPE_HEADER:
                if (hasNewItems){
                    if (position == 0){
                        holder.header.setText("New items");
                    } else {
                        holder.header.setText("Page " + ((position - newRows) / 11 + 1));
                    }
                } else {
                    holder.header.setText("Page " + (position / 11 + 1));
                }
                break;
            case VIEW_TYPE_ITEM_ROW:
                Cursor currentCursor;
                int cursorPosition;
                if (hasNewItems){
                    if (position < newRows) {
                        currentCursor = mDataSetNew;
                        cursorPosition = (position - 1) * 5;
                    } else {
                        currentCursor = mDataSet;
                        cursorPosition = ((position - newRows) - ((position - newRows) / 11) - 1) * 5;
                    }
                } else {
                    currentCursor = mDataSet;
                    cursorPosition = (position - (position / 11) - 1) * 5;
                }

                if (currentCursor.moveToPosition(cursorPosition)){
                    int i = 0;
                    do {
                        final int i2 = i;
                        final int id = currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_ID);
                        final int defindex = currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_DEFI);
                        int quality = currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_QUAL);
                        int tradable = Math.abs(currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_TRAD) - 1);
                        int craftable = Math.abs(currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_CRAF) - 1);
                        int itemIndex = currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_INDE);
                        int paint = currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_PAIN);
                        int australium = currentCursor.getInt(UserBackpackActivity.COL_BACKPACK_AUS);

                        holder.icon[i].setImageDrawable(null);
                        holder.icon[i].setBackgroundDrawable(null);
                        holder.background[i].setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.item_background_blank));
                        holder.parent[i].setOnClickListener(null);

                        if (defindex != 0) {

                            holder.icon[i].setTag("" + id);
                            new ImageLoader(mContext, holder.icon[i], holder.background[i]).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                    defindex, quality, tradable, craftable, itemIndex, paint, australium);
                            holder.parent[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ItemDetailActivity.class);
                                    i.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, id);
                                    i.putExtra(ItemDetailActivity.EXTRA_GUEST, isGuest);
                                    Cursor itemCursor = mDbHelper.getItem(defindex);
                                    if (itemCursor.moveToFirst()) {
                                        i.putExtra(ItemDetailActivity.EXTRA_ITEM_NAME,
                                                itemCursor.getString(0));
                                        i.putExtra(ItemDetailActivity.EXTRA_ITEM_TYPE,
                                                itemCursor.getString(1));
                                        i.putExtra(ItemDetailActivity.EXTRA_PROPER_NAME,
                                                itemCursor.getInt(2));
                                    }
                                    itemCursor.close();

                                    if (Build.VERSION.SDK_INT >= 21) {
                                        ActivityOptions options = ActivityOptions
                                                .makeSceneTransitionAnimation((Activity) mContext,
                                                        Pair.create((View) holder.icon[i2], "icon_transition"),
                                                        Pair.create((View) holder.background[i2], "background_transition"));
                                        mContext.startActivity(i, options.toBundle());
                                    } else {
                                        mContext.startActivity(i);
                                    }
                                }
                            });
                        }
                        i++;
                    } while (currentCursor.moveToNext() && i < 5);

                    while (i < 5){
                        holder.icon[i].setImageDrawable(null);
                        holder.icon[i].setBackgroundDrawable(null);
                        holder.background[i].setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.item_background_blank));
                        holder.parent[i].setOnClickListener(null);
                        i++;
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        int rows = 0;
        if (mDataSet != null) {
            rows += mDataSet.getCount() / 5;
            rows += rows / 10;
        }
        rows += newRows;
        return rows;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasNewItems){
            if (position < newRows){
                if (position == 0)
                    return VIEW_TYPE_HEADER;
                else
                    return VIEW_TYPE_ITEM_ROW;
            } else {
                if ((position - newRows) % 11 == 0)
                    return VIEW_TYPE_HEADER;
                else
                    return VIEW_TYPE_ITEM_ROW;
            }
        }
        else if (position % 11 == 0)
            return VIEW_TYPE_HEADER;
        else
            return VIEW_TYPE_ITEM_ROW;
    }

    public void swapCursor(Cursor cursor, Cursor cursorNew){
        mDataSet = cursor;
        mDataSetNew = cursorNew;
        if (mDataSetNew != null) {
            hasNewItems = mDataSetNew.getCount() > 0;
            if (hasNewItems) {
                newRows = 1 + mDataSetNew.getCount() / 5;
                if (mDataSetNew.getCount() % 5 != 0) {
                    newRows++;
                }
            } else {
                newRows = 0;
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView header;
        ImageView[] background = new ImageView[5];
        ImageView[] icon = new ImageView[5];
        View[] parent = new View[5];

        public ViewHolder(View view, int viewType) {
            super(view);
            switch (viewType){
                case VIEW_TYPE_HEADER:
                    header = (TextView)view.findViewById(R.id.text_view_header);
                    break;
                case VIEW_TYPE_ITEM_ROW:
                    background[0] = (ImageView)view.findViewById(R.id.image_view_item_background_1);
                    icon[0] = (ImageView)view.findViewById(R.id.image_view_item_icon_1);
                    parent[0] = view.findViewById(R.id.relative_layout_1);
                    background[1] = (ImageView)view.findViewById(R.id.image_view_item_background_2);
                    icon[1] = (ImageView)view.findViewById(R.id.image_view_item_icon_2);
                    parent[1] = view.findViewById(R.id.relative_layout_2);
                    background[2] = (ImageView)view.findViewById(R.id.image_view_item_background_3);
                    icon[2] = (ImageView)view.findViewById(R.id.image_view_item_icon_3);
                    parent[2] = view.findViewById(R.id.relative_layout_3);
                    background[3] = (ImageView)view.findViewById(R.id.image_view_item_background_4);
                    icon[3] = (ImageView)view.findViewById(R.id.image_view_item_icon_4);
                    parent[3] = view.findViewById(R.id.relative_layout_4);
                    background[4] = (ImageView)view.findViewById(R.id.image_view_item_background_5);
                    icon[4] = (ImageView)view.findViewById(R.id.image_view_item_icon_5);
                    parent[4] = view.findViewById(R.id.relative_layout_5);
                    break;
            }
        }
    }

    private class ImageLoader extends AsyncTask<Integer, Void, Drawable[]>{

        private ImageView icon;
        private ImageView background;
        private Context mContext;

        private String errorMessage;
        private String tag;

        private ImageLoader(Context context, ImageView icon, ImageView background) {
            this.mContext = context;
            this.icon = icon;
            this.background = background;
            tag = icon.getTag().toString();
        }

        @Override
        protected Drawable[] doInBackground(Integer... params) {
            Drawable[] drawables = new Drawable[3];

            try {
                InputStream ims;
                AssetManager assetManager = mContext.getAssets();
                Drawable d;

                if (params[6] == 1) {
                    ims = assetManager.open("items/" + params[0] + "aus.png");
                } else {
                    ims = assetManager.open("items/" + params[0] + ".png");
                }

                Drawable iconDrawable = Drawable.createFromStream(ims, null);
                if (params[4] != 0 && (params[1] == 5 || params[1] == 7 || params[1] == 9)) {
                    ims = assetManager.open("effects/" + params[4] + "_188x188.png");
                    Drawable effectDrawable = Drawable.createFromStream(ims, null);
                    d = new LayerDrawable(new Drawable[]{effectDrawable, iconDrawable});
                } else {
                    d = iconDrawable;
                }
                drawables[0] = d;

            } catch (IOException e) {
                errorMessage = e.getMessage();
                publishProgress();
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
            }

            drawables[1] = Utility.getItemBackground(mContext, params[1], params[2], params[3]);

            if (params[5] != 0){
                int dotId = Utility.getPaintDrawableId(params[5]);
                if (dotId != 0)
                    drawables[2] = mContext.getResources().getDrawable(dotId);
            } else {
                drawables[2] = null;
            }

            return drawables;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onPostExecute(Drawable[] drawables) {
            if (icon.getTag().toString().equals(tag)) {
                icon.setBackgroundDrawable(drawables[0]);
                background.setBackgroundDrawable(drawables[1]);
                icon.setImageDrawable(drawables[2]);
            }

            ObjectAnimator anim = ObjectAnimator.ofFloat()
        }
    }
}
