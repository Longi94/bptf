package com.tlongdev.bktf;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tlongdev.bktf.fragment.HomeFragment;
import com.tlongdev.bktf.fragment.NavigationDrawerFragment;
import com.tlongdev.bktf.fragment.PriceListFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            if (intent.hasExtra(SearchManager.EXTRA_DATA_KEY)) {
                Toast.makeText(this, "" + intent.getIntExtra(SearchManager.EXTRA_DATA_KEY, 0), Toast.LENGTH_SHORT).show();
            }
            else {
                String query = intent.getStringExtra(SearchManager.QUERY);
                Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
            }
            // TODO doMySearch(query);
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
            fragment = new PriceListFragment();
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
            case 1:
                mTitle = getString(R.string.title_home);
                break;
            case 2:
                mTitle = getString(R.string.title_user_profile);
                break;
            case 3:
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
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
