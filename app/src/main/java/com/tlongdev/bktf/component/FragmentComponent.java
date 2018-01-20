package com.tlongdev.bktf.component;

import com.tlongdev.bktf.module.BptfAppModule;
import com.tlongdev.bktf.module.PresenterModule;
import com.tlongdev.bktf.ui.fragment.BptfFragment;
import com.tlongdev.bktf.ui.fragment.CalculatorFragment;
import com.tlongdev.bktf.ui.fragment.ConverterFragment;
import com.tlongdev.bktf.ui.fragment.CurrencyFragment;
import com.tlongdev.bktf.ui.fragment.FavoritesFragment;
import com.tlongdev.bktf.ui.fragment.RecentsFragment;
import com.tlongdev.bktf.ui.fragment.UnusualFragment;
import com.tlongdev.bktf.ui.fragment.UserFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
@Singleton
@Component(modules = {PresenterModule.class, BptfAppModule.class})
public interface FragmentComponent {

    void inject(BptfFragment bptfFragment);

    void inject(UserFragment userFragment);

    void inject(UnusualFragment unusualFragment);

    void inject(CalculatorFragment calculatorFragment);

    void inject(ConverterFragment converterFragment);

    void inject(FavoritesFragment favoritesFragment);

    void inject(RecentsFragment recentsFragment);

    void inject(CurrencyFragment currencyFragment);
}
