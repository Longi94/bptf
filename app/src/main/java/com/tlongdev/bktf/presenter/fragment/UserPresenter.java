package com.tlongdev.bktf.presenter.fragment;

import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.interactor.GetUserDataInteractor;
import com.tlongdev.bktf.interactor.Tf2UserBackpackInteractor;
import com.tlongdev.bktf.presenter.Presenter;
import com.tlongdev.bktf.ui.view.fragment.UserView;
import com.tlongdev.bktf.util.Profile;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

/**
 * @author Long
 * @since 2016. 03. 15.
 */
public class UserPresenter implements Presenter<UserView>,GetUserDataInteractor.Callback, SwipeRefreshLayout.OnRefreshListener, Tf2UserBackpackInteractor.Callback {

    @Inject SharedPreferences mPrefs;
    @Inject SharedPreferences.Editor mEditor;
    @Inject Tracker mTracker;

    private UserView mView;
    private BptfApplication mApplication;

    public UserPresenter(BptfApplication application) {
        mApplication = application;
        application.getPresenterComponent().inject(this);
    }

    @Override
    public void attachView(UserView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    public void getUserDataIfNeeded() {
        //Update user info if last update was more than 30 minutes ago
        if (Utility.isNetworkAvailable(mView.getContext()) && System.currentTimeMillis()
                - mPrefs.getLong(mView.getContext().getString(R.string.pref_last_user_data_update), 0) >= 3600000L) {

            //Start the task and listne for the end
            GetUserDataInteractor task = new GetUserDataInteractor(mView.getContext(), mApplication, false, this);
            task.execute(Profile.getSteamId(mView.getContext()), Profile.getResolvedSteamId(mView.getContext()));

            mView.showRefreshingAnimation();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserData")
                    .build());
        }
    }

    @Override
    public void onUserInfoFinished(String steamId) {
        //Save the update time
        mEditor.putLong(mView.getContext().getString(R.string.pref_last_user_data_update),
                System.currentTimeMillis()).apply();

        Tf2UserBackpackInteractor interactor = new Tf2UserBackpackInteractor(
                mView.getContext(), mApplication, this);
        interactor.execute(steamId);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Request")
                .setAction("UserBackpack")
                .build());
    }

    @Override
    public void onUserInfoFailed(String errorMessage) {
        if (mView != null) {
            mView.updateUserPage();
            mView.hideRefreshingAnimation();
            Toast.makeText(mView.getContext(), "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefresh() {
        if (Utility.isNetworkAvailable(mView.getContext())) {
            //Start fetching the data and listen for the end
            GetUserDataInteractor fetchTask = new GetUserDataInteractor(mView.getContext(), mApplication, true, this);
            fetchTask.execute(Profile.getSteamId(mView.getContext()), Profile.getResolvedSteamId(mView.getContext()));

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Request")
                    .setAction("UserData")
                    .build());
        } else {
            //There is no internet connection, notify the user
            Toast.makeText(mView.getContext(), "bptf: " + mView.getContext().getString(R.string.error_no_network),
                    Toast.LENGTH_SHORT).show();
            mView.hideRefreshingAnimation();
        }
    }

    @Override
    public void onUserBackpackFinished(int rawKeys, double rawMetal, int backpackSlots, int itemNumber) {
        //Save all the data passed
        mEditor.putInt(mView.getContext().getString(R.string.pref_user_slots), backpackSlots);
        mEditor.putInt(mView.getContext().getString(R.string.pref_user_items), itemNumber);
        mEditor.putInt(mView.getContext().getString(R.string.pref_user_raw_key), rawKeys);
        Utility.putDouble(mEditor, mView.getContext().getString(R.string.pref_user_raw_metal), rawMetal);
        mEditor.apply();

        if (mView != null) {
            mView.backpack(false);
            mView.updateUserPage();
            mView.hideRefreshingAnimation();
            mView.updateDrawer();
        }
    }

    @Override
    public void onPrivateBackpack() {
        //Save all data that represent a private backpack
        mEditor.putInt(mView.getContext().getString(R.string.pref_user_slots), -1);
        mEditor.putInt(mView.getContext().getString(R.string.pref_user_items), -1);
        mEditor.putInt(mView.getContext().getString(R.string.pref_user_raw_key), -1);
        Utility.putDouble(mEditor, mView.getContext().getString(R.string.pref_user_raw_metal), -1);
        Utility.putDouble(mEditor, mView.getContext().getString(R.string.pref_player_backpack_value_tf2), -1);
        mEditor.apply();

        if (mView != null) {
            mView.backpack(true);
            mView.updateUserPage();
            mView.hideRefreshingAnimation();
            mView.updateDrawer();
        }
    }

    @Override
    public void onUserBackpackFailed(String errorMessage) {
        //Stop the refreshing animation and update the UI
        if (mView != null) {
            mView.updateUserPage();
            mView.hideRefreshingAnimation();
            Toast.makeText(mView.getContext(), "failed", Toast.LENGTH_SHORT).show();
        }
    }
}