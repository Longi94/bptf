package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.UserBackpackActivity;
import com.tlongdev.bktf.Utility;

import java.io.IOException;
import java.io.InputStream;

public class BackpackSectionHeaderAdapter extends RecyclerView.Adapter<BackpackSectionHeaderAdapter.ViewHolder> {

    public static final int VIEW_TYPE_ITEM_ROW = 0;
    public static final int VIEW_TYPE_HEADER = 1;

    private Cursor mDataSet;
    private Context mContext;

    public BackpackSectionHeaderAdapter(Context mContext) {
        this.mContext = mContext;
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case VIEW_TYPE_HEADER:
                holder.header.setText("Page " + (position / 11 + 1));
                break;
            case VIEW_TYPE_ITEM_ROW:
                int cursorPosition = (position - (position / 11) - 1) * 5;
                if (mDataSet.moveToPosition(cursorPosition)){
                    for (int i = 0; i < 5; i++){
                        int defindex = mDataSet.getInt(UserBackpackActivity.COL_BACKPACK_DEFI);
                        int quality = mDataSet.getInt(UserBackpackActivity.COL_BACKPACK_QUAL);
                        int tradable = Math.abs(mDataSet.getInt(UserBackpackActivity.COL_BACKPACK_TRAD) - 1);
                        int craftable = Math.abs(mDataSet.getInt(UserBackpackActivity.COL_BACKPACK_CRAF) - 1);
                        int itemIndex = mDataSet.getInt(UserBackpackActivity.COL_BACKPACK_INDE);
                        int paint = mDataSet.getInt(UserBackpackActivity.COL_BACKPACK_PAIN);
                        int australium = mDataSet.getInt(UserBackpackActivity.COL_BACKPACK_AUS);

                        if (defindex == 0){
                            holder.icon[i].setImageDrawable(null);
                            holder.background[i].setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.item_background_blank));
                        } else {
                            setIconImage(mContext, holder.icon[i], defindex, australium == 1);
                            holder.background[i].setBackgroundDrawable(Utility.getItemBackground(mContext,
                                    quality, tradable, craftable));
                            holder.parent[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                        }
                        mDataSet.moveToNext();
                    }
                }
                break;
        }
    }

    private void setIconImage(Context context, ImageView icon, int defindex, boolean isAustralium) {
        try {
            InputStream ims;
            AssetManager assetManager = context.getAssets();
            if (isAustralium) {
                ims = assetManager.open("items/" + defindex + "aus.png");
            } else {
                ims = assetManager.open("items/" + defindex + ".png");
            }

            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            icon.setImageDrawable(d);
        } catch (IOException e) {
            Toast.makeText(context, "bptf: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (Utility.isDebugging(context))
                e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null){
            return 0;
        }
        int rows = mDataSet.getCount() / 5;
        return rows + rows / 10;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 11 == 0)
            return VIEW_TYPE_HEADER;
        else
            return VIEW_TYPE_ITEM_ROW;
    }

    public void swapCursor(Cursor cursor){
        mDataSet = cursor;
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
}
