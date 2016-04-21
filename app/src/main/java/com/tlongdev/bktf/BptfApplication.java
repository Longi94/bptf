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

package com.tlongdev.bktf;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.component.ActivityComponent;
import com.tlongdev.bktf.component.AdapterComponent;
import com.tlongdev.bktf.component.DaggerActivityComponent;
import com.tlongdev.bktf.component.DaggerAdapterComponent;
import com.tlongdev.bktf.component.DaggerFragmentComponent;
import com.tlongdev.bktf.component.DaggerInteractorComponent;
import com.tlongdev.bktf.component.DaggerPresenterComponent;
import com.tlongdev.bktf.component.DaggerProfileManagerComponent;
import com.tlongdev.bktf.component.DaggerServiceComponent;
import com.tlongdev.bktf.component.FragmentComponent;
import com.tlongdev.bktf.component.InteractorComponent;
import com.tlongdev.bktf.component.PresenterComponent;
import com.tlongdev.bktf.component.ProfileManagerComponent;
import com.tlongdev.bktf.component.ServiceComponent;
import com.tlongdev.bktf.module.BptfAppModule;
import com.tlongdev.bktf.module.NetworkModule;
import com.tlongdev.bktf.module.StorageModule;
import com.tlongdev.bktf.util.ProfileManager;

import io.fabric.sdk.android.Fabric;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app.
 */
public class BptfApplication extends Application {
    private Tracker mTracker;

    private ActivityComponent mActivityComponent;

    private FragmentComponent mFragmentComponent;

    private InteractorComponent mInteractorComponent;

    private PresenterComponent mPresenterComponent;

    private ProfileManagerComponent mProfileManagerComponent;

    private AdapterComponent mAdapterComponent;

    private ServiceComponent mServiceComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics(), new Answers());
        }

        BptfAppModule appModule = new BptfAppModule(this);
        StorageModule storageModule = new StorageModule();
        NetworkModule networkModule = new NetworkModule();

        mActivityComponent = DaggerActivityComponent.builder()
                .bptfAppModule(appModule)
                .build();

        mFragmentComponent = DaggerFragmentComponent.builder()
                .bptfAppModule(appModule)
                .build();

        mInteractorComponent = DaggerInteractorComponent.builder()
                .bptfAppModule(appModule)
                .networkModule(networkModule)
                .storageModule(storageModule)
                .build();

        mPresenterComponent = DaggerPresenterComponent.builder()
                .bptfAppModule(appModule)
                .build();

        mProfileManagerComponent = DaggerProfileManagerComponent.builder()
                .bptfAppModule(appModule)
                .storageModule(storageModule)
                .build();

        mAdapterComponent = DaggerAdapterComponent.builder()
                .bptfAppModule(appModule)
                .build();

        mServiceComponent = DaggerServiceComponent.builder()
                .bptfAppModule(appModule)
                .storageModule(storageModule)
                .build();

        if (!BuildConfig.DEBUG) {
            ProfileManager manager = new ProfileManager(this);
            if (manager.isSignedIn()) {
                Crashlytics.setUserIdentifier(manager.getResolvedSteamId());
            }
        }
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        startTracking();
        return mTracker;
    }

    public void startTracking() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.bptf_config);

            analytics.enableAutoActivityReports(this);
        }
    }

    public ActivityComponent getActivityComponent() {
        return mActivityComponent;
    }

    public FragmentComponent getFragmentComponent() {
        return mFragmentComponent;
    }

    public InteractorComponent getInteractorComponent() {
        return mInteractorComponent;
    }

    public PresenterComponent getPresenterComponent() {
        return mPresenterComponent;
    }

    public ProfileManagerComponent getProfileManagerComponent() {
        return mProfileManagerComponent;
    }

    public AdapterComponent getAdapterComponent() {
        return mAdapterComponent;
    }

    public ServiceComponent getServiceComponent() {
        return mServiceComponent;
    }
}