package com.tlongdev.bktf.activity;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.fragment.CalculatorFragment;
import com.tlongdev.bktf.fragment.ConverterFragment;
import com.tlongdev.bktf.fragment.FavoritesFragment;
import com.tlongdev.bktf.fragment.RecentsFragment;
import com.tlongdev.bktf.fragment.UnusualFragment;
import com.tlongdev.bktf.fragment.UserFragment;
import com.tlongdev.bktf.gcm.GcmRegisterPriceUpdatesService;
import com.tlongdev.bktf.util.CircleTransform;
import com.tlongdev.bktf.util.Profile;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

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
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * The drawer layout and the navigation drawer
     */
    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.navigation_view) NavigationView mNavigationView;

    /**
     * The index of the current fragment.
     */
    private int mCurrentSelectedPosition = -1;

    /**
     * Variables used for managing fragments.
     */
    private boolean userStateChanged = false;

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

    private MenuItem userMenuItem;

    /**
     * Listener for the navigation drawer.
     */
    NavigationView.OnNavigationItemSelectedListener navigationListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            //Close the drawers and handle item clicks
            mDrawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                // TODO: 2015. 10. 25. fragment selections in the navigation view is incorrect when selecting the user fragment
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

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Set the default values for all preferences when the app is first loaded
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        //Setup the drawer
        mDrawerLayout.setStatusBarBackgroundColor(Utility.getColor(this, R.color.primary_dark));

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        //The navigation view
        mNavigationView.setNavigationItemSelectedListener(navigationListener);

        //User clicked on the header
        View navigationHeader = mNavigationView.getHeaderView(0);

        //Find the views of the navigation drawer header
        name = (TextView) navigationHeader.findViewById(R.id.user_name);
        backpack = (TextView) navigationHeader.findViewById(R.id.backpack_value);
        avatar = (ImageView) navigationHeader.findViewById(R.id.avatar);

        //Check if there is a fragment to be restored
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mNavigationView.getMenu().getItem(mCurrentSelectedPosition).setChecked(true);
        } else {
            mNavigationView.getMenu().getItem(0).setChecked(true);
        }

        userMenuItem = mNavigationView.getMenu().getItem(2);

        // Select either the default item (0) or the last selected item.
        switchFragment(0);
    }

    @Override
    protected void onResume() {
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        //If needed (mostly when the steamId was changed) reload a new instance of the UserFragment
        if (userStateChanged) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (Profile.isSignedIn(this)) {
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
            userStateChanged = false;
        }
        updateDrawer();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoSync = !prefs.getString(getString(R.string.pref_auto_sync), "1").equals("0");

        if (prefs.getBoolean(getString(R.string.pref_registered_topic_price_updates), false) != autoSync) {
            Intent intent = new Intent(this, GcmRegisterPriceUpdatesService.class);
            intent.putExtra(GcmRegisterPriceUpdatesService.EXTRA_SUBSCRIBE, autoSync);
            startService(intent);
        }

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
                if (mCurrentSelectedPosition == 2) {
                    if (data != null && data.getBooleanExtra("login_changed", false)) {
                        //User fragment needs to be reloaded if the steamId was changed
                        userStateChanged = true;
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
                drawerListener = (RecentsFragment) newFragment;
                break;
            case 1:
                newFragment = new UnusualFragment();
                drawerListener = (UnusualFragment) newFragment;
                break;
            case 2:
                newFragment = new UserFragment();
                drawerListener = (UserFragment) newFragment;
                break;
            case 3:
                newFragment = new FavoritesFragment();
                drawerListener = (FavoritesFragment) newFragment;
                break;
            case 4:
                newFragment = new ConverterFragment();
                drawerListener = null;
                break;
            case 5:
                newFragment = new CalculatorFragment();
                drawerListener = (CalculatorFragment) newFragment;
                break;
            default:
                throw new IllegalArgumentException("unknown fragment to switch to: " + position);
        }

        //Commit the transaction
        transaction.replace(R.id.container, newFragment);
        transaction.commit();
    }

    /**
     * Updates the information in the navigation drawer header.
     */
    public void updateDrawer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (Profile.isSignedIn(this)) {
            //Set the name
            name.setText(prefs.getString(getString(R.string.pref_player_name), null));

            //Set the backpack value
            double bpValue = Utility.getDouble(prefs,
                    getString(R.string.pref_player_backpack_value_tf2), -1);
            if (bpValue >= 0) {
                backpack.setText(String.format("Backpack: %s", getString(R.string.currency_metal, String.valueOf(Math.round(bpValue)))));
            } else {
                backpack.setText("Private backpack");
            }

            //Download the avatar (if needed) and set it
            if (prefs.contains(getString(R.string.pref_new_avatar)) &&
                    Utility.isNetworkAvailable(this)) {
                Glide.with(this)
                        .load(PreferenceManager.getDefaultSharedPreferences(this).
                                getString(getString(R.string.pref_player_avatar_url), ""))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new CircleTransform(this))
                        .into(avatar);

            }
            userMenuItem.setEnabled(true);
        } else {
            name.setText(null);
            backpack.setText(null);
            userMenuItem.setEnabled(false);
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
