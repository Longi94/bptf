package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;
import com.tlongdev.bktf.fragment.CalculatorFragment;
import com.tlongdev.bktf.fragment.ConverterFragment;
import com.tlongdev.bktf.fragment.RecentsFragment;
import com.tlongdev.bktf.fragment.UnusualFragment;
import com.tlongdev.bktf.fragment.UserFragment;
import com.tlongdev.bktf.service.NotificationsService;
import com.tlongdev.bktf.service.UpdateDatabaseService;

/**
 * Tha main activity if the application. Navigation drawer is used. This is where most of the
 * fragments are shown.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Request codes for onActivityResult
     */
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

    /**
     * Variables used for managing fragments.
     */
    private boolean restartUserFragment = false;

    /**
     * Listener to be notified when the drawer opens. Mainly for fragments with toolbars so we can
     * exapnd the fragment's toolbar when the drawer opens.
     */
    private OnDrawerOpenedListener drawerListener;

    /**
     * Views of the navigation header view.
     */
    private TextView name;
    private TextView backpack;
    private ImageView avatar;
    private View headerLayout;

    /**
     * Listener for the navigation drawer.
     */
    NavigationView.OnNavigationItemSelectedListener navigationListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            //Close the drawers and handle item clicks
            mDrawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.nav_settings:
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivityForResult(settingsIntent, REQUEST_SETTINGS);
                    break;
                case R.id.nav_help:
                    // TODO: 2015. 10. 25. needs a proper help section (not a web page)
                    Uri webPage = Uri.parse("https://github.com/Longi94/bptf/wiki/Help");

                    //Open link in the device default web browser
                    Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    break;
                default:
                    switchFragment(menuItem.getItemId());
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the default values for all preferences when the app is first loaded
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        //Setup the drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(Utility.getColor(this, R.color.primary_dark));

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        //The navigation view
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(navigationListener);

        //User clicked on the header
        View navigationHeader = mNavigationView.inflateHeaderView(R.layout.navigation_drawer_header);

        //Find the views of the navigation drawer header
        name = (TextView) navigationHeader.findViewById(R.id.user_name);
        backpack = (TextView) navigationHeader.findViewById(R.id.backpack_value);
        avatar = (ImageView) navigationHeader.findViewById(R.id.avatar);
        headerLayout = navigationHeader.findViewById(R.id.linear_layout);

        //Start services if option is on
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_notification), false)) {
            startService(new Intent(this, NotificationsService.class));
        }
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_background_sync), false)) {
            startService(new Intent(this, UpdateDatabaseService.class));
        }

        //Check if there is a fragment to be restored
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }
        mNavigationView.getMenu().getItem(mCurrentSelectedPosition).setChecked(true);

        // Select either the default item (0) or the last selected item.
        switchFragment(mCurrentSelectedPosition);
    }

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
        updateHeader();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the current fragment to be restored.
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

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

        //Initialize fragments and add them is the drawer listener
        switch (position) {
            case R.id.nav_user:
                newFragment = new UserFragment();
                drawerListener = (UserFragment) newFragment;
                break;
            case R.id.nav_recents:
                newFragment = new RecentsFragment();
                drawerListener = (RecentsFragment) newFragment;
                break;
            case R.id.nav_unusuals:
                newFragment = new UnusualFragment();
                drawerListener = (UnusualFragment) newFragment;
                break;
            case R.id.nav_converter:
                newFragment = new ConverterFragment();
                drawerListener = null;
                break;
            case R.id.nav_calculator:
                newFragment = new CalculatorFragment();
                drawerListener = (CalculatorFragment) newFragment;
                break;
            default:
                throw new IllegalArgumentException("unknown fragment to switch to");
        }

        //Commit the transaction
        transaction.replace(R.id.container, newFragment);
        transaction.commit();
    }

    /**
     * Updates the information in the navigation drawer header.
     */
    public void updateHeader() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Check if the user is logged in
        String steamId = prefs.getString(getString(R.string.pref_resolved_steam_id), "");
        if (!steamId.equals("")) {
            //Set the name
            name.setText(prefs.getString(getString(R.string.pref_player_name), null));

            //Set the backpack value
            double bpValue = Utility.getDouble(prefs,
                    getString(R.string.pref_player_backpack_value_tf2), -1);
            backpack.setText(getString(R.string.currency_metal, String.valueOf(Math.round(bpValue))));

            //Download the avatar (if needed) and set it
            if (prefs.contains(getString(R.string.pref_new_avatar)) &&
                    Utility.isNetworkAvailable(this)) {

                Picasso picasso = Picasso.with(this);
                picasso.setLoggingEnabled(true);
                picasso.setIndicatorsEnabled(true);
                picasso.load(PreferenceManager.getDefaultSharedPreferences(this).
                        getString(getString(R.string.pref_player_avatar_url), "")).into(avatar);

            }
            headerLayout.setVisibility(View.VISIBLE);
        } else {
            // TODO navigationHeader.setOnClickListener(null); right now the header is not updated
            // TODO when the user provide's his steam ID
            headerLayout.setVisibility(View.GONE);
        }
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
