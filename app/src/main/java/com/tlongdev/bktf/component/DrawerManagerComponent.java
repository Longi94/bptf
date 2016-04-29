package com.tlongdev.bktf.component;

import com.tlongdev.bktf.module.BptfAppModule;
import com.tlongdev.bktf.ui.NavigationDrawerManager;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author longi
 * @since 2016.04.29.
 */
@Singleton
@Component(modules = {BptfAppModule.class})
public interface DrawerManagerComponent {
    void inject(NavigationDrawerManager navigationDrawerManager);
}
