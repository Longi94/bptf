package com.tlongdev.bktf.ui;

import android.app.Application;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.util.CircleTransform;
import com.tlongdev.bktf.util.ProfileManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author longi
 * @since 2016.04.29.
 */
public class NavigationDrawerManager implements ProfileManager.OnUpdateListener {

    @BindView(R.id.user_name) TextView mName;
    @BindView(R.id.backpack_value) TextView mBackpack;
    @BindView(R.id.avatar) ImageView mAvatar;

    private ProfileManager mProfileManager;
    private MenuItem mUserMenuItem;
    private Context mContext;

    public NavigationDrawerManager(Application context) {
        mProfileManager = ProfileManager.getInstance(context);
        mProfileManager.addOnProfileUpdateListener(this);
    }

    public void attachView(View header) {
        ButterKnife.bind(this, header);

        mContext = header.getContext();
        if (mProfileManager.isSignedIn()) {
            onUpdate(mProfileManager.getUser());
        } else {
            onLogOut();
        }
    }

    public void detachView() {
        mName = null;
        mBackpack = null;
        mAvatar = null;
        mContext = null;
    }

    public void setUserMenuItem(MenuItem userMenuItem) {
        mUserMenuItem = userMenuItem;
        if (mUserMenuItem != null) {
            mUserMenuItem.setEnabled(mProfileManager.isSignedIn());
        }
    }

    @Override
    public void onLogOut() {
        if (mUserMenuItem != null) {
            mUserMenuItem.setEnabled(false);
        }

        if (mContext == null || mName == null || mBackpack == null || mAvatar == null) {
            return;
        }

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transforms(new CircleTransform());
        Glide.with(mContext)
                .load(R.drawable.steam_default_avatar)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(options)
                .into(mAvatar);
        mName.setText(null);
        mBackpack.setText(null);
    }

    @Override
    public void onUpdate(User user) {
        if (mUserMenuItem != null) {
            mUserMenuItem.setEnabled(true);
        }

        if (mContext == null || mName == null || mBackpack == null || mAvatar == null) {
            return;
        }

        //Set the name
        mName.setText(user.getName());

        //Set the backpack value
        double bpValue = user.getBackpackValue();
        if (bpValue >= 0) {
            mBackpack.setText(String.format("Backpack: %s",
                    mContext.getString(R.string.currency_metal,
                            String.valueOf(Math.round(bpValue)))));
        } else {
            mBackpack.setText("Private backpack");
        }

        //Download the avatar (if needed) and set it
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transforms(new CircleTransform());
        Glide.with(mContext)
                .load(user.getAvatarUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(options)
                .into(mAvatar);
    }
}
