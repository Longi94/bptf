package com.tlongdev.bktf;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.tlongdev.bktf.fragment.AdvancedCalculatorFragment;
import com.tlongdev.bktf.fragment.HomeFragment;
import com.tlongdev.bktf.fragment.NavigationDrawerFragment;
import com.tlongdev.bktf.fragment.SearchFragment;
import com.tlongdev.bktf.fragment.SimpleCalculatorFragment;
import com.tlongdev.bktf.fragment.UnusualPriceListFragment;
import com.tlongdev.bktf.fragment.UserFragment;
import com.tlongdev.bktf.service.NotificationsService;
import com.tlongdev.bktf.service.UpdateDatabaseService;

/**
 * Tha main activity if the application. Navigation drawer is used. This is where most of the
 * fragments are shown.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    //Request codes for onActivityResult
    public static final int REQUEST_SETTINGS = 100;
    public static final int REQUEST_NEW_ITEM = 101;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    //Store reference to search fragment to pass search queries.
    private SearchFragment mSearchFragment;

    //Variables used for managing fragments.
    private int previousFragment = -1;
    private int currentFragment = -1;
    private boolean restartUserFragment = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the default values for all preferences when the app is first loaded
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        onSectionAttached(0);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //Start services if option is on
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_notification), false)) {
            startService(new Intent(this, NotificationsService.class));
        }
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_background_sync), false)) {
            startService(new Intent(this, UpdateDatabaseService.class));
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
            transaction.replace(R.id.container, new HomeFragment());
        } else if (position == 1 && currentFragment != 1) {
            //User fragment
            transaction.replace(R.id.container, new UserFragment());
        } else if (position == 2 && currentFragment != 2) {
            //Unusual prices fragment
            transaction.replace(R.id.container, new UnusualPriceListFragment());
        } else if (position == 3 && currentFragment != 3) {
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
     * Convenience activity to start the settings activity
     */
    public void startSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivityForResult(settingsIntent, REQUEST_SETTINGS);
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
                mTitle = getString(R.string.title_user_profile);
                break;
            case 2:
                mTitle = getString(R.string.title_unusuals);
                break;
            case 3:
                mTitle = getString(R.string.title_calculator);
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
                        if (mNavigationDrawerFragment.isDrawerOpen()) {
                            mNavigationDrawerFragment.closeDrawer();
                        }
                        //Lock the drawer. Prevents the user from opening it while searching.
                        mNavigationDrawerFragment.lockDrawer();

                        //Store the index of the previous fragment.
                        previousFragment = mNavigationDrawerFragment.getCheckedItemPosition();

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
                        mNavigationDrawerFragment.unlockDrawer();
                        onNavigationDrawerItemSelected(previousFragment);
                        return true;
                    }
                });

        return super.onCreateOptionsMenu(menu);
    }
}
