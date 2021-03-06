package com.tlongdev.bktf.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.User;
import com.tlongdev.bktf.presenter.activity.UserPresenter;
import com.tlongdev.bktf.ui.fragment.UserFragment;
import com.tlongdev.bktf.ui.view.activity.UserView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Profile page activity.
 */
public class UserActivity extends BptfActivity implements UserView {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = UserActivity.class.getSimpleName();

    //Keys for extra data in the intent.
    public static final String STEAM_ID_KEY = "steamid";

    @Inject UserPresenter mPresenter;

    //Progress bar that indicates downloading user data.
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.error_message) TextView mErrorMessage;

    @InjectExtra(STEAM_ID_KEY) String steamId;

    private UserFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        Dart.inject(this);

        mApplication.getActivityComponent().inject(this);

        mPresenter.attachView(this);

        Log.d(LOG_TAG, "steamID: " + steamId);

        mPresenter.loadData(steamId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void showData(User user) {
        mFragment.showFullData(user);
    }

    @Override
    public void showPartial(User user) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.simple_fade_in, R.anim.simple_fade_out);

        mFragment = UserFragment.newInstance(user);
        transaction.replace(R.id.container, mFragment);
        transaction.commit();

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);

        if (errorMessage != null) {
            mFragment.showError(errorMessage);
        }
    }
}