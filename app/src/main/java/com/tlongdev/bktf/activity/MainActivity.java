package com.tlongdev.bktf.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.fragment.CalculatorFragment;
import com.tlongdev.bktf.fragment.ConverterFragment;
import com.tlongdev.bktf.fragment.RecentsFragment;
import com.tlongdev.bktf.fragment.UnusualFragment;
import com.tlongdev.bktf.fragment.UserFragment;
import com.tlongdev.bktf.service.NotificationsService;
import com.tlongdev.bktf.service.UpdateDatabaseService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
                    switchFragment(0);
                    break;
                case R.id.nav_unusuals:
                    switchFragment(1);
                    break;
                case R.id.nav_converter:
                    switchFragment(2);
                    break;
                case R.id.nav_calculator:
                    switchFragment(3);
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

    private View.OnClickListener headerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switchFragment(-1);
        }
    };

    private TextView name;
    private TextView backpack;
    private ImageView avatar;
    private View navigationHeader;
    private View headerLayout;

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
        navigationHeader = mNavigationView.inflateHeaderView(R.layout.navigation_drawer_header);
        navigationHeader.setOnClickListener(headerListener);

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
        updateHeader();
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
                drawerListener = (UserFragment) newFragment;
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
                newFragment = new ConverterFragment();
                drawerListener = null;
                break;
            case 3:
                newFragment = new CalculatorFragment();
                drawerListener = (CalculatorFragment) newFragment;
                break;
            default:
                return;
        }

        transaction.replace(R.id.container, newFragment);
        transaction.commit();
    }

    public void updateHeader() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String steamId = prefs.getString(getString(R.string.pref_resolved_steam_id), "");
        if (!steamId.equals("")) {
            name.setText(prefs.getString(getString(R.string.pref_player_name), null));

            double bpValue = Utility.getDouble(prefs,
                    getString(R.string.pref_player_backpack_value_tf2), -1);
            backpack.setText(String.valueOf(Math.round(bpValue)));

            if (prefs.contains(getString(R.string.pref_new_avatar)) &&
                    Utility.isNetworkAvailable(this)) {
                //Start downloading the avatar in the background
                new AvatarDownLoader(PreferenceManager.getDefaultSharedPreferences(this).
                        getString(getString(R.string.pref_player_avatar_url), ""), this,
                        avatar).
                        execute();
            }
            navigationHeader.setOnClickListener(headerListener);
            headerLayout.setVisibility(View.VISIBLE);
        } else {
            // TODO navigationHeader.setOnClickListener(null);
            headerLayout.setVisibility(View.GONE);
        }
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

    /**
     * Asynctask for downloading the avatar in the background.
     */
    private class AvatarDownLoader extends AsyncTask<Void, Void, Void> {

        //The url of the avatar
        private String url;

        //The context the task was launched in
        private Context mContext;

        //Bitmap to store the image
        private Bitmap bmp;

        //Drawable for the avatar
        private Drawable d;

        //Error message to be shown to the user
        private String errorMessage;

        private ImageView avatar;

        /**
         * Constructor.
         *
         * @param url     url link for the avatar
         * @param context the context the task was launched in
         */
        private AvatarDownLoader(String url, Context context, ImageView avatar) {
            this.url = url;
            this.mContext = context;
            this.avatar = avatar;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Void doInBackground(Void... params) {

            //Check again if we really need to download the image
            if (PreferenceManager.getDefaultSharedPreferences(mContext).
                    getBoolean(mContext.getString(R.string.pref_new_avatar), false)) {

                //Get the image
                bmp = getBitmapFromURL(url);

                try {
                    //Save avatar as png into the private data folder
                    FileOutputStream fos = mContext.openFileOutput("avatar.png",
                            Context.MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (IOException e) {
                    //IO error, shouldn't reach
                    errorMessage = e.getMessage();
                    publishProgress();
                    if (Utility.isDebugging(mContext))
                        e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onProgressUpdate(Void... values) {
            //There was an error, notify the user
            Toast.makeText(mContext, "bptf: " + errorMessage, Toast.LENGTH_SHORT).show();
        }

        /**
         * Method to download the image.
         *
         * @param link the link to download the image from
         * @return the bitmap object containing the image
         */
        public Bitmap getBitmapFromURL(String link) {

            try {
                //Open connection
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();

                //Get the input stream
                InputStream input = connection.getInputStream();

                //Decode the image
                return BitmapFactory.decodeStream(input);

            } catch (IOException e) {
                //There was an error, notify the user
                errorMessage = e.getMessage();
                publishProgress();
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            //Get the avatar from the private data folder and set it to the image view
            File path = mContext.getFilesDir();
            d = Drawable.createFromPath(path.toString() + "/avatar.png");
            avatar.setImageDrawable(d);

            //Save to preferences that we don't need to download the avatar again.
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(
                    mContext.getString(R.string.pref_new_avatar), false).apply();
        }
    }
}
