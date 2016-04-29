/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.gcm.GcmRegisterPriceUpdatesService;
import com.tlongdev.bktf.ui.NavigationDrawerManager;
import com.tlongdev.bktf.ui.fragment.CalculatorFragment;
import com.tlongdev.bktf.ui.fragment.ConverterFragment;
import com.tlongdev.bktf.ui.fragment.FavoritesFragment;
import com.tlongdev.bktf.ui.fragment.RecentsFragment;
import com.tlongdev.bktf.ui.fragment.UnusualFragment;
import com.tlongdev.bktf.ui.fragment.UserFragment;
import com.tlongdev.bktf.util.ProfileManager;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Tha main activity if the application. Navigation drawer is used. This is where most of the
 * fragments are shown.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Request codes for onActivityResult
     */
    private static final int REQUEST_SETTINGS = 100;
    public static final int REQUEST_NEW_ITEM = 101;

    @Inject SharedPreferences mPrefs;
    @Inject ProfileManager mProfileManager;
    @Inject NavigationDrawerManager mNavigationDrawerManager;

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view) NavigationView mNavigationView;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * The index of the current fragment.
     */
    private int mCurrentSelectedPosition = -1;

    /**
     * Variables used for managing fragments.
     */
    private boolean mUserStateChanged = false;

    /**
     * Listener to be notified when the drawer opens. Mainly for fragments with toolbars so we can
     * expand the fragment's toolbar when the drawer opens.
     */
    private OnDrawerOpenedListener mDrawerListener;

    /**
     * Listener for the navigation drawer.
     */
    private final NavigationView.OnNavigationItemSelectedListener navigationListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            //Close the drawers and handle item clicks
            mDrawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.nav_recents:
                    switchFragment(0);
                    break;
                case R.id.nav_unusuals:
                    switchFragment(1);
                    break;
                case R.id.nav_user:
                    switchFragment(2);
                    break;
                case R.id.nav_favorites:
                    switchFragment(3);
                    break;
                case R.id.nav_converter:
                    switchFragment(4);
                    break;
                case R.id.nav_calculator:
                    switchFragment(5);
                    break;
                case R.id.nav_settings:
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivityForResult(settingsIntent, REQUEST_SETTINGS);
                    break;
                case R.id.nav_about:
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        BptfApplication application = (BptfApplication) getApplication();
        application.getActivityComponent().inject(this);
        application.startTracking();

        //Set the default values for all preferences when the app is first loaded
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        //Setup the drawer
        mDrawerLayout.setStatusBarBackgroundColor(Utility.getColor(this, R.color.primary_dark));

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        //The navigation view
        mNavigationView.setNavigationItemSelectedListener(navigationListener);

        mNavigationDrawerManager.attachView(mNavigationView.getHeaderView(0));
        mNavigationDrawerManager.setUserMenuItem(mNavigationView.getMenu().getItem(2));

        //Check if there is a fragment to be restored
        if (savedInstanceState == null) {
            mNavigationView.getMenu().getItem(0).setChecked(true);
            // Select either the default item (0) or the last selected item.
            switchFragment(0);
        }
    }

    @Override
    protected void onResume() {
        //If needed (mostly when the steamId was changed) reload a new instance of the UserFragment
        if (mUserStateChanged) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (mProfileManager.isSignedIn()) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new UserFragment())
                        .commit();
            } else {
                mNavigationView.getMenu().getItem(0).setChecked(true);
                mCurrentSelectedPosition = 0;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new RecentsFragment())
                        .commit();
            }
            mUserStateChanged = false;
        }

        boolean autoSync = !mPrefs.getString(getString(R.string.pref_auto_sync), "1").equals("0");

        if (mPrefs.getBoolean(getString(R.string.pref_registered_topic_price_updates), false) != autoSync) {
            Intent intent = new Intent(this, GcmRegisterPriceUpdatesService.class);
            intent.putExtra(GcmRegisterPriceUpdatesService.EXTRA_SUBSCRIBE, autoSync);
            startService(intent);
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNavigationDrawerManager.detachView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SETTINGS) {
            //User returned from the settings activity
            if (resultCode == RESULT_OK) {
                if (mCurrentSelectedPosition == 2) {
                    if (data != null && data.getBooleanExtra("login_changed", false)) {
                        //User fragment needs to be reloaded if the steamId was changed
                        mUserStateChanged = true;
                    }
                }
            }
            return;
        }
        //super call is needed to pass the result to the fragments
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handler the drawer toggle press
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        //Since each fragment has it's own toolbar we need to re add the drawer toggle everytime we
        //switch fragments
        restoreNavigationIcon();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Switches to another fragment.
     *
     * @param position the position of the clicked item in the navigation view
     */
    private void switchFragment(int position) {

        if (mCurrentSelectedPosition == position) {
            return;
        }

        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mNavigationView);
        }

        //Start handling fragment transactions
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.simple_fade_in, R.anim.simple_fade_out);
        Fragment newFragment;

        //Initialize fragments and add them is the drawer listener
        switch (position) {
            case 0:
                newFragment = new RecentsFragment();
                mDrawerListener = (RecentsFragment) newFragment;
                transaction.replace(R.id.container, newFragment);
                break;
            case 1:
                newFragment = new UnusualFragment();
                mDrawerListener = (UnusualFragment) newFragment;
                transaction.replace(R.id.container, newFragment);
                break;
            case 2:
                newFragment = UserFragment.newInstance();
                mDrawerListener = (UserFragment) newFragment;
                transaction.replace(R.id.container, newFragment);
                break;
            case 3:
                newFragment = new FavoritesFragment();
                mDrawerListener = (FavoritesFragment) newFragment;
                transaction.replace(R.id.container, newFragment);
                break;
            case 4:
                newFragment = new ConverterFragment();
                mDrawerListener = null;
                transaction.replace(R.id.container, newFragment);
                break;
            case 5:
                newFragment = new CalculatorFragment();
                mDrawerListener = (CalculatorFragment) newFragment;
                transaction.replace(R.id.container, newFragment);
                break;
            default:
                throw new IllegalArgumentException("unknown fragment to switch to: " + position);
        }

        //Commit the transaction
        transaction.commit();
    }

    /**
     * Restores the navigation icon of the toolbar.
     */
    private void restoreNavigationIcon() {
        // set up the drawer's list view with items and click listener
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //Notify the listeners
                if (mDrawerListener != null) {
                    mDrawerListener.onDrawerOpened();
                }
            }
        };

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /**
     * Interface for listening drawer open events.
     */
    public interface OnDrawerOpenedListener {

        /**
         * Called when the navigation drawer is opened.
         */
        void onDrawerOpened();
    }
}
