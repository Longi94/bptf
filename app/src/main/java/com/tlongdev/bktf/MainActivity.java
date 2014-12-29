package com.tlongdev.bktf;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.tlongdev.bktf.fragment.HomeFragment;
import com.tlongdev.bktf.fragment.NavigationDrawerFragment;
import com.tlongdev.bktf.fragment.UnusualPriceListFragment;
import com.tlongdev.bktf.fragment.SearchFragment;
import com.tlongdev.bktf.fragment.UserFragment;
import com.tlongdev.bktf.task.FetchPriceList;


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

    private SearchView mSearchView;
    private SearchFragment mSearchFragment;

    private int previousFragment = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff5787c5));

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        onSectionAttached(0);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(getResources().getString(R.string.pref_initial_load), true)){
            new FetchPriceList(this).execute(getResources().getString(R.string.backpack_tf_api_key));
            SharedPreferences.Editor editor = prefs.edit();

            editor.putBoolean(getResources().getString(R.string.pref_initial_load), false);
            editor.apply();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = new HomeFragment();
        }
        else if (position == 1) {
            fragment = new UserFragment();
        }
        else /*if (position == 2)*/ {
            fragment = new UnusualPriceListFragment();
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();

        onSectionAttached(position);
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
                mTitle = getString(R.string.title_prices);
                break;
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

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) menuItem.getActionView();

        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (mSearchFragment != null && mSearchFragment.isAdded()){
                    mSearchFragment.restartLoader(s);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (mSearchFragment != null && mSearchFragment.isAdded()){
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}
