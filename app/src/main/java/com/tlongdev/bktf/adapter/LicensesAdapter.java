package com.tlongdev.bktf.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.License;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Long on 2016. 01. 10..
 */
public class LicensesAdapter extends RecyclerView.Adapter<LicensesAdapter.ViewHolder>{

    private List<License> licenses;
    private Context mContext;

    public LicensesAdapter(List<License> licenses, Context context) {
        this.licenses = licenses;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_licenses, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final License license = licenses.get(position);

        holder.name.setText(license.getName());
        holder.license.setText(license.getLicense());
        holder.link.setText(license.getUrl());

        holder.link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webPage = Uri.parse(license.getUrl());

                //Open link in the device default web browser
                Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return licenses == null ? 0 : licenses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.name) TextView name;
        @Bind(R.id.link) TextView link;
        @Bind(R.id.license) TextView license;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            link.setPaintFlags(link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }
}
