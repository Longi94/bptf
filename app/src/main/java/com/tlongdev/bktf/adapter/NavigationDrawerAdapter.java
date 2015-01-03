package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;

import java.util.List;

public class NavigationDrawerAdapter extends ArrayAdapter<String> {
    public NavigationDrawerAdapter(Context context, int resource, List<String> titles) {
        super(context, resource, titles);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder = null;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.simple_drawer_list_item, null);

            holder = new ViewHolder();
            holder.icon = (ImageView)v.findViewById(R.id.navigation_drawer_icon);
            holder.text = (TextView)v.findViewById(R.id.text);

            v.setTag(holder);
        } else {
            holder = (ViewHolder)v.getTag();
        }

        holder.text.setText(getItem(position));

        switch (position){
            case 0:
                holder.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_new_releases_black_24dp));
                break;
            case 1:
                holder.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_account_circle_black_24dp));
                break;
            case 2:
                holder.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_whatshot_black_24dp));
                break;
        }

        return v;
    }

    private class ViewHolder {
        public ImageView icon;
        public TextView text;
    }
}
