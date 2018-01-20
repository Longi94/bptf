package com.tlongdev.bktf.module;

import android.app.Application;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.presenter.activity.ItemChooserPresenter;
import com.tlongdev.bktf.presenter.activity.ItemDetailPresenter;
import com.tlongdev.bktf.presenter.activity.LicensesPresenter;
import com.tlongdev.bktf.presenter.activity.SettingsPresenter;
import com.tlongdev.bktf.presenter.activity.PriceHistoryPresenter;
import com.tlongdev.bktf.presenter.activity.SearchPresenter;
import com.tlongdev.bktf.presenter.activity.SelectItemPresenter;
import com.tlongdev.bktf.presenter.activity.UnusualPresenter;
import com.tlongdev.bktf.presenter.activity.UserBackpackPresenter;
import com.tlongdev.bktf.presenter.activity.UserPresenter;
import com.tlongdev.bktf.presenter.fragment.CalculatorPresenter;
import com.tlongdev.bktf.presenter.fragment.CurrencyPresenter;
import com.tlongdev.bktf.presenter.fragment.FavoritesPresenter;
import com.tlongdev.bktf.presenter.fragment.RecentsPresenter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author lngtr
 * @since 2016. 04. 27.
 */
@Module
public class PresenterModule {

    @Provides
    @Singleton
    ItemChooserPresenter provideItemChooserPresenter(Application application) {
        return new ItemChooserPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    ItemDetailPresenter provideItemDetailPresenter(Application application) {
        return new ItemDetailPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    LicensesPresenter provideLicensesPresenter(Application application) {
        return new LicensesPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    SettingsPresenter provideLoginPresenter(Application application) {
        return new SettingsPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    PriceHistoryPresenter providePriceHistoryPresenter(Application application) {
        return new PriceHistoryPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    SearchPresenter provideSearchPresenter(Application application) {
        return new SearchPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    SelectItemPresenter provideSelectItemPresenter(Application application) {
        return new SelectItemPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    UnusualPresenter provideUnusualPresenter(Application application) {
        return new UnusualPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    UserBackpackPresenter provideUserBackpackPresenter(Application application) {
        return new UserBackpackPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    UserPresenter provideUserPresenter(Application application) {
        return new UserPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    CalculatorPresenter provideCalculatorPresenter(Application application) {
        return new CalculatorPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    FavoritesPresenter provideFavoritesPresenter(Application application) {
        return new FavoritesPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    RecentsPresenter provideRecentsPresenter(Application application) {
        return new RecentsPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    com.tlongdev.bktf.presenter.fragment.UnusualPresenter provideUnusualFragmentPresenter(Application application) {
        return new com.tlongdev.bktf.presenter.fragment.UnusualPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    com.tlongdev.bktf.presenter.fragment.UserPresenter provideUserFragmentPresenter(Application application) {
        return new com.tlongdev.bktf.presenter.fragment.UserPresenter((BptfApplication) application);
    }

    @Provides
    @Singleton
    CurrencyPresenter provideCurrencyPresenter(Application application) {
        return new CurrencyPresenter((BptfApplication) application);
    }
}
