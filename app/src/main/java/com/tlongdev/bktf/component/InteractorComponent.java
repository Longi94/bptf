package com.tlongdev.bktf.component;

import com.tlongdev.bktf.interactor.BackpackTfPriceHistoryInteractor;
import com.tlongdev.bktf.interactor.GetCurrencyExchangeRatesInteractor;
import com.tlongdev.bktf.interactor.GetSearchedUserDataInteractor;
import com.tlongdev.bktf.interactor.GetUserDataInteractor;
import com.tlongdev.bktf.interactor.LoadCalculatorItemsInteractor;
import com.tlongdev.bktf.interactor.LoadCurrencyPricesInteractor;
import com.tlongdev.bktf.interactor.LoadFavoritesInteractor;
import com.tlongdev.bktf.interactor.LoadItemDetailsInteractor;
import com.tlongdev.bktf.interactor.LoadSearchItemsInteractor;
import com.tlongdev.bktf.interactor.LoadSelectorItemsInteractor;
import com.tlongdev.bktf.interactor.LoadUnusualEffectsInteractor;
import com.tlongdev.bktf.interactor.LoadUnusualHatCategoriesInteractor;
import com.tlongdev.bktf.interactor.LoadUnusualsInteractor;
import com.tlongdev.bktf.interactor.SearchUserInteractor;
import com.tlongdev.bktf.interactor.Tf2UserBackpackInteractor;
import com.tlongdev.bktf.interactor.TlongdevItemSchemaInteractor;
import com.tlongdev.bktf.interactor.TlongdevPriceListInteractor;
import com.tlongdev.bktf.module.BptfAppModule;
import com.tlongdev.bktf.module.NetworkModule;
import com.tlongdev.bktf.module.StorageModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
@Singleton
@Component(modules = {BptfAppModule.class, NetworkModule.class, StorageModule.class})
public interface InteractorComponent {
    void inject(TlongdevPriceListInteractor tlongdevPriceListInteractor);

    void inject(TlongdevItemSchemaInteractor tlongdevItemSchemaInteractor);

    void inject(LoadCalculatorItemsInteractor loadCalculatorItemsInteractor);

    void inject(LoadFavoritesInteractor loadFavoritesInteractor);

    void inject(LoadUnusualHatCategoriesInteractor loadUnusualHatsInteractor);

    void inject(LoadUnusualEffectsInteractor loadUnusualEffectsInteractor);

    void inject(GetUserDataInteractor getUserDataInteractor);

    void inject(BackpackTfPriceHistoryInteractor backpackTfPriceHistoryInteractor);

    void inject(Tf2UserBackpackInteractor tf2UserBackpackInteractor);

    void inject(LoadCurrencyPricesInteractor loadCurrencyPricesInteractor);

    void inject(GetSearchedUserDataInteractor getSearchedUserDataInteractor);

    void inject(LoadUnusualsInteractor loadUnusualsInteractor);

    void inject(LoadSelectorItemsInteractor loadSelectorItemsInteractor);

    void inject(SearchUserInteractor searchUserInteractor);

    void inject(LoadSearchItemsInteractor loadSearchItemsInteractor);

    void inject(LoadItemDetailsInteractor loadItemDetailsInteractor);

    void inject(GetCurrencyExchangeRatesInteractor getCurrencyExchangeRatesInteractor);
}
