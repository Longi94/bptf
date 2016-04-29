package com.tlongdev.bktf.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.util.CircleTransform;
import com.tlongdev.bktf.util.ProfileManager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author longi
 * @since 2016.04.29.
 */
public class NavigationDrawerManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject ProfileManager mProfileManager;
    @Inject SharedPreferences mPrefs;
    @Inject Context mContext;

    @BindView(R.id.user_name) TextView mName;
    @BindView(R.id.backpack_value) TextView mBackpack;
    @BindView(R.id.avatar) ImageView mAvatar;

    private MenuItem mUserMenuItem;

    public NavigationDrawerManager(BptfApplication application) {
        application.getDrawerManagerComponent().inject(this);
        //mProfileManager.addOnProfileUpdateListener(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void attachView(View header) {
        ButterKnife.bind(this, header);

        mContext = header.getContext();
        if (mProfileManager.isSignedIn()) {
            update(mProfileManager.getUser());
        }
    }

    public void detachView() {
        mName = null;
        mBackpack = null;
        mAvatar = null;
        mContext = null;
    }

    public void onLogOut() {
        Glide.with(mContext)
                .load(R.drawable.steam_default_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CircleTransform(mContext))
                .into(mAvatar);
        mName.setText(null);
        mBackpack.setText(null);

        if (mUserMenuItem != null) {
            mUserMenuItem.setEnabled(false);
        }
    }

    public void update(User user) {
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
        Glide.with(mContext)
                .load(user.getAvatarUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CircleTransform(mContext))
                .into(mAvatar);
        if (mUserMenuItem != null) {
            mUserMenuItem.setEnabled(true);
        }
    }

    public void setUserMenuItem(MenuItem userMenuItem) {
        mUserMenuItem = userMenuItem;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!key.equals(mContext.getString(R.string.pref_user_data))) {
            return;
        }

        if (mProfileManager.isSignedIn()) {
            update(mProfileManager.getUser());
        } else {
            onLogOut();
        }
    }
}
