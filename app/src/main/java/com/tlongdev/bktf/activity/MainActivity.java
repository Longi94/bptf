package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
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

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.fragment.AdvancedCalculatorFragment;
import com.tlongdev.bktf.fragment.RecentsFragment;
import com.tlongdev.bktf.fragment.SimpleCalculatorFragment;
import com.tlongdev.bktf.fragment.UnusualFragment;
import com.tlongdev.bktf.fragment.UserFragment;
import com.tlongdev.bktf.service.NotificationsService;
import com.tlongdev.bktf.service.UpdateDatabaseService;

/**
 * Tha main activity if the application. Navigation drawer is used. This is where most of the
 * fragments are shown.
 */
public class MainActivity extends AppCompatActivity {

    //Request codes for onActivityResult
    public static final int REQUEST_SETTINGS = 100;
    public static final int REQUEST_NEW_ITEM = 101;

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * The drawer layout and the navigation drawer
     */
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    /**
     * The index of the current fragment.
     */
    private int mCurrentSelectedPosition = 0;

    //Variables used for managing fragments.
    private boolean restartUserFragment = false;

    private OnDrawerOpenedListener drawerListener;

    NavigationView.OnNavigationItemSelectedListener navigationListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            mDrawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.nav_recents:
                    menuItem.setCheckable(true);
                    switchFragment(0);
                    break;
                case R.id.nav_unusuals:
                    menuItem.setCheckable(true);
                    switchFragment(1);
                    break;
                case R.id.nav_calculator:
                    menuItem.setCheckable(true);
                    switchFragment(2);
                    break;
                case R.id.nav_settings:
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivityForResult(settingsIntent, REQUEST_SETTINGS);
                    break;
                case R.id.nav_help:
                    Uri webPage = Uri.parse("https://github.com/Longi94/bptf/wiki/Help");

                    //Open link in the device default web browser
                    Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    break;
            }
            return true; // TODO: 2015. 10. 13.
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the default values for all preferences when the app is first loaded
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        //Setup the drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(navigationListener);

        //User clicked on the header
        mNavigationView.inflateHeaderView(R.layout.navigation_drawer_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(-1);
            }
        });

        //Start services if option is on
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_notification), false)) {
            startService(new Intent(this, NotificationsService.class));
        }
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_background_sync), false)) {
            startService(new Intent(this, UpdateDatabaseService.class));
        }

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }
        mNavigationView.getMenu().getItem(mCurrentSelectedPosition).setChecked(true);

        // Select either the default item (0) or the last selected item.
        switchFragment(mCurrentSelectedPosition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        //If needed (mostly when the steamId was changed) reload a new instance of the UserFragment
        if (restartUserFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new UserFragment())
                    .commit();
            restartUserFragment = false;
        }
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the current fragment to be restored.
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    public void switchFragment(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mNavigationView);
        }

        //Start handling fragment transactions
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.simple_fade_in, R.anim.simple_fade_out);
        Fragment newFragment;

        switch (position) {
            case -1:
                newFragment = new UserFragment();
                break;
            case 0:
                newFragment = new RecentsFragment();
                drawerListener = (RecentsFragment) newFragment;
                break;
            case 1:
                newFragment = new UnusualFragment();
                drawerListener = (UnusualFragment) newFragment;
                break;
            case 2:
                if (!PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getString(R.string.pref_preferred_advanced_calculator), false)) {
                    newFragment = new SimpleCalculatorFragment();
                } else {
                    newFragment = new AdvancedCalculatorFragment();
                }
                drawerListener = null;
                break;
            default:
                return;
        }

        transaction.replace(R.id.container, newFragment);
        transaction.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SETTINGS) {
            //User returned from the settings activity
            if (resultCode == RESULT_OK) {
                if (mCurrentSelectedPosition == -1) {
                    if (data != null && data.getBooleanExtra("preference_changed", false)) {
                        //User fragment needs to be reloaded if the steamId was changed
                        restartUserFragment = true;
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

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mNavigationView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        restoreNavigationIcon();
    }

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
                if (drawerListener != null) {
                    drawerListener.onDrawerOpened();
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

    public interface OnDrawerOpenedListener {
        void onDrawerOpened();
    }
}
