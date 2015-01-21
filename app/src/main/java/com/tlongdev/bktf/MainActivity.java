package com.tlongdev.bktf;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.tlongdev.bktf.fragment.CalculatorFragment;
import com.tlongdev.bktf.fragment.HomeFragment;
import com.tlongdev.bktf.fragment.NavigationDrawerFragment;
import com.tlongdev.bktf.fragment.SearchFragment;
import com.tlongdev.bktf.fragment.UnusualPriceListFragment;
import com.tlongdev.bktf.fragment.UserFragment;
import com.tlongdev.bktf.service.NotificationsService;
import com.tlongdev.bktf.service.UpdateDatabaseService;

/**
 * Tha main activity if the application. Navigation drawer is used.
 */
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the color of the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff5787c5));

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
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_notification), false)){
            startService(new Intent(this, NotificationsService.class));
        }
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_background_sync), false)){
            startService(new Intent(this, UpdateDatabaseService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //If needed (mostly when the steamId was changed) reload a new instance of the UserFragment
        if (restartUserFragment){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new UserFragment())
                    .commit();
            restartUserFragment = false;
        }

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (position == 0 && currentFragment != 0) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new HomeFragment())
                    .commit();
        }
        else if (position == 1 && currentFragment != 1) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new UserFragment())
                    .commit();
        }
        else if (position == 2  && currentFragment != 2) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new UnusualPriceListFragment())
                    .commit();
        }
        else if (position == 3  && currentFragment != 3) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new CalculatorFragment())
                    .commit();
        }
        currentFragment = position;
        onSectionAttached(position);
    }

    public void startSettingsActivity(){
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivityForResult(settingsIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0){
            if (resultCode == RESULT_OK){
                if (currentFragment == 1){
                    if (data != null && data.getBooleanExtra("preference_changed", false)){
                        //User fragment needs to be reloaded if the steamId was changed
                        restartUserFragment = true;
                    }
                }
            }
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_home);
                break;
            case 1:
                mTitle = getString(R.string.title_user_profile);
                break;
            case 2:
                mTitle = "Unusuals";
                break;
            case 3:
                mTitle = getString(R.string.title_calculator);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        restoreActionBar();

        //Setup the search widget
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) menuItem.getActionView();

        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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

        MenuItemCompat.setOnActionExpandListener(menuItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        if (mNavigationDrawerFragment.isDrawerOpen()){
                            mNavigationDrawerFragment.closeDrawer();
                        }
                        mNavigationDrawerFragment.lockDrawer();

                        previousFragment = mNavigationDrawerFragment.getCheckedItemPosition();

                        mSearchFragment = new SearchFragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, mSearchFragment)
                                .commit();

                        currentFragment = -1;

                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        mNavigationDrawerFragment.unlockDrawer();
                        onNavigationDrawerItemSelected(previousFragment);
                        return true;
                    }
                })
        ;

        return super.onCreateOptionsMenu(menu);
    }
}
