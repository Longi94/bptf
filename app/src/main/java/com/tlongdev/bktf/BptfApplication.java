package com.tlongdev.bktf;

import android.app.Application;

import com.tlongdev.bktf.component.ActivityComponent;
import com.tlongdev.bktf.component.AdapterComponent;
import com.tlongdev.bktf.component.DaggerActivityComponent;
import com.tlongdev.bktf.component.DaggerAdapterComponent;
import com.tlongdev.bktf.component.DaggerFragmentComponent;
import com.tlongdev.bktf.component.DaggerInteractorComponent;
import com.tlongdev.bktf.component.DaggerPresenterComponent;
import com.tlongdev.bktf.component.DaggerServiceComponent;
import com.tlongdev.bktf.component.FragmentComponent;
import com.tlongdev.bktf.component.InteractorComponent;
import com.tlongdev.bktf.component.PresenterComponent;
import com.tlongdev.bktf.component.ServiceComponent;
import com.tlongdev.bktf.module.BptfAppModule;
import com.tlongdev.bktf.module.NetworkModule;
import com.tlongdev.bktf.module.PresenterModule;
import com.tlongdev.bktf.module.StorageModule;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app.
 */
public class BptfApplication extends Application {

    private ActivityComponent mActivityComponent;

    private FragmentComponent mFragmentComponent;

    private InteractorComponent mInteractorComponent;

    private PresenterComponent mPresenterComponent;

    private AdapterComponent mAdapterComponent;

    private ServiceComponent mServiceComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        BptfAppModule appModule = new BptfAppModule(this);
        StorageModule storageModule = new StorageModule();
        NetworkModule networkModule = new NetworkModule();
        PresenterModule presenterModule = new PresenterModule();

        mActivityComponent = DaggerActivityComponent.builder()
                .bptfAppModule(appModule)
                .presenterModule(presenterModule)
                .build();

        mFragmentComponent = DaggerFragmentComponent.builder()
                .bptfAppModule(appModule)
                .presenterModule(presenterModule)
                .build();

        mInteractorComponent = DaggerInteractorComponent.builder()
                .bptfAppModule(appModule)
                .networkModule(networkModule)
                .storageModule(storageModule)
                .build();

        mPresenterComponent = DaggerPresenterComponent.builder()
                .bptfAppModule(appModule)
                .build();

        mAdapterComponent = DaggerAdapterComponent.builder()
                .bptfAppModule(appModule)
                .build();

        mServiceComponent = DaggerServiceComponent.builder()
                .bptfAppModule(appModule)
                .storageModule(storageModule)
                .build();
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

    public AdapterComponent getAdapterComponent() {
        return mAdapterComponent;
    }

    public ServiceComponent getServiceComponent() {
        return mServiceComponent;
    }
}