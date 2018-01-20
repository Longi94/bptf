package com.tlongdev.bktf.adapter;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.License;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LicensesAdapter extends RecyclerView.Adapter<LicensesAdapter.ViewHolder>{

    private final List<License> licenses;

    private OnClickListener mListener;

    public LicensesAdapter(List<License> licenses) {
        this.licenses = licenses;
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

        holder.link.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onLicenseClicked(license.getUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return licenses == null ? 0 : licenses.size();
    }

    public void setListener(OnClickListener listener) {
        mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name) TextView name;
        @BindView(R.id.link) TextView link;
        @BindView(R.id.license) TextView license;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            link.setPaintFlags(link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    public interface OnClickListener {
        void onLicenseClicked(String wrbPage);
    }
}
