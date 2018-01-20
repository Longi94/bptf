package com.tlongdev.bktf.component;

import com.tlongdev.bktf.module.BptfAppModule;
import com.tlongdev.bktf.module.StorageModule;
import com.tlongdev.bktf.presenter.activity.ItemChooserPresenter;
import com.tlongdev.bktf.presenter.activity.LicensesPresenter;
import com.tlongdev.bktf.presenter.activity.SettingsPresenter;
import com.tlongdev.bktf.presenter.activity.SelectItemPresenter;
import com.tlongdev.bktf.presenter.activity.UserBackpackPresenter;
import com.tlongdev.bktf.presenter.fragment.CalculatorPresenter;
import com.tlongdev.bktf.presenter.fragment.FavoritesPresenter;
import com.tlongdev.bktf.presenter.fragment.RecentsPresenter;
import com.tlongdev.bktf.presenter.fragment.UnusualPresenter;
import com.tlongdev.bktf.presenter.fragment.UserPresenter;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
@Singleton
@Component(modules = {BptfAppModule.class, StorageModule.class})
public interface PresenterComponent {
    void inject(RecentsPresenter presenter);

    void inject(CalculatorPresenter calculatorPresenter);

    void inject(FavoritesPresenter favoritesPresenter);

    void inject(UnusualPresenter unusualPresenter);

    void inject(UserPresenter userPresenter);

    void inject(UserBackpackPresenter userBackpackPresenter);

    void inject(com.tlongdev.bktf.presenter.activity.UserPresenter userPresenter);

    void inject(com.tlongdev.bktf.presenter.activity.UnusualPresenter unusualPresenter);

    void inject(SelectItemPresenter selectItemPresenter);

    void inject(SettingsPresenter loginPresenter);

    void inject(LicensesPresenter licensesPresenter);

    void inject(ItemChooserPresenter itemChooserPresenter);
}
