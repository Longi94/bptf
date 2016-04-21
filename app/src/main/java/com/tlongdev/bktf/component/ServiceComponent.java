package com.tlongdev.bktf.component;

import com.tlongdev.bktf.gcm.GcmMessageHandler;
import com.tlongdev.bktf.module.BptfAppModule;
import com.tlongdev.bktf.module.StorageModule;
import com.tlongdev.bktf.widget.FavoritesWidgetService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Long
 * @since 2016. 04. 21.
 */
@Singleton
@Component(modules = {BptfAppModule.class, StorageModule.class})
public interface ServiceComponent {
    void inject(FavoritesWidgetService.FavoritesRemoteViewsFactory favoritesRemoteViewsFactory);

    void inject(GcmMessageHandler gcmMessageHandler);
}
