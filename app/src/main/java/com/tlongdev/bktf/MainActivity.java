package com.tlongdev.bktf;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tlongdev.bktf.fragment.AdvancedCalculatorFragment;
import com.tlongdev.bktf.fragment.HomeFragment;
import com.tlongdev.bktf.fragment.SearchFragment;
import com.tlongdev.bktf.fragment.SimpleCalculatorFragment;
import com.tlongdev.bktf.fragment.UnusualPriceListFragment;
import com.tlongdev.bktf.fragment.UserFragment;
import com.tlongdev.bktf.network.FetchPriceList;
import com.tlongdev.bktf.service.NotificationsService;
import com.tlongdev.bktf.service.UpdateDatabaseService;

/**
 * Tha main activity if the application. Navigation drawer is used. This is where most of the
 * fragments are shown.
 */
public class MainActivity extends AppCompatActivity implements FetchPriceList.OnPriceListFetchListener {

    //Request codes for onActivityResult
    public static final int REQUEST_SETTINGS = 100;
    public static final int REQUEST_NEW_ITEM = 101;

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * The drawer layout and the navigation drawer
     */
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;

    /**
     * The index of the current fragment.
     */
    private int mCurrentSelectedPosition = 0;

    //Store reference to search fragment to pass search queries.
    private SearchFragment mSearchFragment;

    //Variables used for managing fragments.
    private int previousFragment = -1;
    private int currentFragment = -1;
    private boolean restartUserFragment = false;

    private MenuItem currentMenuItem;


    private TextView metalPrice;
    private TextView keyPrice;
    private TextView budsPrice;
    private View metalPriceImage;
    private View keyPriceImage;
    private View budsPriceImage;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the default values for all preferences when the app is first loaded
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        //As we're using a Toolbar, we should retrieve it and set it to be our ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //Views used for toolbar behavior
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        //Setup the drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_recents:
                        menuItem.setCheckable(true);
                        selectItem(0);
                        currentMenuItem = menuItem.setChecked(true);
                        break;
                    case R.id.nav_unusuals:
                        menuItem.setCheckable(true);
                        selectItem(1);
                        currentMenuItem = menuItem.setChecked(true);
                        break;
                    case R.id.nav_calculator:
                        menuItem.setCheckable(true);
                        selectItem(2);
                        currentMenuItem = menuItem.setChecked(true);
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
        });

        //User clicked on the header
        /*mNavigationView.findViewById(R.id.navigation_view_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefs.getBoolean(getString(R.string.pref_facebook_logged_in), false)) {
                    selectItem(-1);
                    if (currentMenuItem != null) {
                        currentMenuItem.setCheckable(false);
                    }
                }
            }
        });*/

        setUp();

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
        currentMenuItem = mNavigationView.getMenu().getItem(mCurrentSelectedPosition).setChecked(true);

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
        
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        metalPrice = (TextView) findViewById(R.id.text_view_metal_price);
        keyPrice = (TextView) findViewById(R.id.text_view_key_price);
        budsPrice = (TextView) findViewById(R.id.text_view_buds_price);

        metalPriceImage = findViewById(R.id.image_view_metal_price);
        keyPriceImage = findViewById(R.id.image_view_key_price);
        budsPriceImage = findViewById(R.id.image_view_buds_price);

        metalPrice.setText(prefs.getString(getString(R.string.pref_metal_price), ""));
        keyPrice.setText(prefs.getString(getString(R.string.pref_key_price), ""));
        budsPrice.setText(prefs.getString(getString(R.string.pref_buds_price), ""));

        if (Utility.getDouble(prefs, getString(R.string.pref_metal_diff), 0) > 0) {
            metalPriceImage.setBackgroundColor(0xff008504);
        } else {
            metalPriceImage.setBackgroundColor(0xff850000);
        }
        if (Utility.getDouble(prefs, getString(R.string.pref_key_diff), 0) > 0) {
            keyPriceImage.setBackgroundColor(0xff008504);
        } else {
            keyPriceImage.setBackgroundColor(0xff850000);
        }
        if (Utility.getDouble(prefs, getString(R.string.pref_buds_diff), 0) > 0) {
            budsPriceImage.setBackgroundColor(0xff008504);
        } else {
            budsPriceImage.setBackgroundColor(0xff850000);
        }
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    public void onNavigationDrawerItemSelected(int position) {

        //Start handling fragment transactions
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (currentFragment != -1) {
            //Simple fade animation for switching between fragments
            transaction.setCustomAnimations(R.anim.simple_fade_in, R.anim.simple_fade_out);
        }
        if (position == 0 && currentFragment != 0) {
            //Home fragment
            HomeFragment fragment = new HomeFragment();
            fragment.setListener(this);
            transaction.replace(R.id.container, fragment);
        } else if (position == 1 && currentFragment != 1) {
            //Unusual prices fragment
            transaction.replace(R.id.container, new UnusualPriceListFragment());
        } else if (position == 2 && currentFragment != 2) {
            if (!PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(getString(R.string.pref_preferred_advanced_calculator), false)) {
                //Simple calculatior fragment
                transaction.replace(R.id.container, new SimpleCalculatorFragment());
            } else {
                //Advanced caltulator fragment
                transaction.replace(R.id.container, new AdvancedCalculatorFragment());
            }
        }
        transaction.commit();
        currentFragment = position;
        onSectionAttached(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SETTINGS) {
            //User returned from the settings activity
            if (resultCode == RESULT_OK) {
                if (currentFragment == 1) {
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

    /**
     * This sets the title according to the fragments. I have no idea why this was generated.
     *
     * @param number index of the fragment
     */
    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_home);
                break;
            case 1:
                mTitle = getString(R.string.title_unusuals);
                break;
            case 2:
                mTitle = getString(R.string.title_calculator);
                break;
        }
    }

    /**
     * Restores action bar I assume. It was auto generated.
     */
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Inflate the action bar menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        restoreActionBar();

        //Setup the search widget
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) menuItem.getActionView();

        //Set the searchable info
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        //Start a new query everytime the text is edited and when the player taps on submit.
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (mSearchFragment != null && mSearchFragment.isAdded()) {
                    mSearchFragment.restartLoader(s);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (mSearchFragment != null && mSearchFragment.isAdded()) {
                    mSearchFragment.restartLoader(s);
                }
                return true;
            }
        });

        //Switch to the search fragment when the searchview is expanded and switch back when
        //collapsed.
        MenuItemCompat.setOnActionExpandListener(menuItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        //Close the drawer if it was open
                        if (isDrawerOpen()) {
                            closeDrawer();
                        }
                        //Lock the drawer. Prevents the user from opening it while searching.
                        lockDrawer();

                        //Store the index of the previous fragment.
                        previousFragment = getCheckedItemPosition();

                        //Switch to the search fragment
                        mSearchFragment = new SearchFragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.simple_fade_in, R.anim.simple_fade_out)
                                .replace(R.id.container, mSearchFragment)
                                .commit();

                        currentFragment = -2;
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        //Unlock the drawer and switch back to the previous fragment
                        unlockDrawer();
                        onNavigationDrawerItemSelected(previousFragment);
                        return true;
                    }
                });

        return super.onCreateOptionsMenu(menu);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mNavigationView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     */
    public void setUp() {

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
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
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                expandToolbar();
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
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

    /**
     * Select an item from the navigation drawer.
     *
     * @param position the position of the item.
     */
    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mNavigationView);
        }
        onNavigationDrawerItemSelected(position);
    }

    public void lockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void unlockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public int getCheckedItemPosition() {
        return 0; // TODO: 2015. 10. 13.
    }

    public void closeDrawer() {
        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawers();
    }

    @Override
    public void onPriceListFetchFinished() {

        //Update the header with currency prices
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        metalPrice.setText(prefs.getString(getString(R.string.pref_metal_price), ""));
        keyPrice.setText(prefs.getString(getString(R.string.pref_key_price), ""));
        budsPrice.setText(prefs.getString(getString(R.string.pref_buds_price), ""));

        if (Utility.getDouble(prefs, getString(R.string.pref_metal_diff), 0.0) > 0.0) {
            metalPriceImage.setBackgroundColor(0xff008504);
        } else {
            metalPriceImage.setBackgroundColor(0xff850000);
        }
        if (Utility.getDouble(prefs, getString(R.string.pref_key_diff), 0.0) > 0.0) {
            keyPriceImage.setBackgroundColor(0xff008504);
        } else {
            keyPriceImage.setBackgroundColor(0xff850000);
        }
        if (Utility.getDouble(prefs, getString(R.string.pref_buds_diff), 0.0) > 0) {
            budsPriceImage.setBackgroundColor(0xff008504);
        } else {
            budsPriceImage.setBackgroundColor(0xff850000);
        }
    }
}
