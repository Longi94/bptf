/**
 * Copyright 2016 Long Tran
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

package com.tlongdev.bktf.component;

import com.tlongdev.bktf.interactor.BackpackTfPriceHistoryInteractor;
import com.tlongdev.bktf.interactor.GetSearchedUserDataInteractor;
import com.tlongdev.bktf.interactor.GetUserDataInteractor;
import com.tlongdev.bktf.interactor.LoadAllPricesInteractor;
import com.tlongdev.bktf.interactor.LoadBackpackItemsInteractor;
import com.tlongdev.bktf.interactor.LoadCalculatorItemsInteractor;
import com.tlongdev.bktf.interactor.LoadCurrencyPricesInteractor;
import com.tlongdev.bktf.interactor.LoadFavoritesInteractor;
import com.tlongdev.bktf.interactor.LoadSearchItemsInteractor;
import com.tlongdev.bktf.interactor.LoadSelectorItemsInteractor;
import com.tlongdev.bktf.interactor.LoadUnusualEffectsInteractor;
import com.tlongdev.bktf.interactor.LoadUnusualHatCategoriesInteractor;
import com.tlongdev.bktf.interactor.LoadUnusualsInteractor;
import com.tlongdev.bktf.interactor.SearchUserInteractor;
import com.tlongdev.bktf.interactor.TlongdevItemSchemaInteractor;
import com.tlongdev.bktf.interactor.TlongdevPriceListInteractor;
import com.tlongdev.bktf.module.BptfAppModule;
import com.tlongdev.bktf.module.NetworkModule;
import com.tlongdev.bktf.module.StorageModule;
import com.tlongdev.bktf.interactor.Tf2UserBackpackInteractor;

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

    void inject(LoadAllPricesInteractor loadAllPricesInteractor);

    void inject(LoadCalculatorItemsInteractor loadCalculatorItemsInteractor);

    void inject(LoadFavoritesInteractor loadFavoritesInteractor);

    void inject(LoadUnusualHatCategoriesInteractor loadUnusualHatsInteractor);

    void inject(LoadUnusualEffectsInteractor loadUnusualEffectsInteractor);

    void inject(GetUserDataInteractor getUserDataInteractor);

    void inject(BackpackTfPriceHistoryInteractor backpackTfPriceHistoryInteractor);

    void inject(Tf2UserBackpackInteractor tf2UserBackpackInteractor);

    void inject(LoadCurrencyPricesInteractor loadCurrencyPricesInteractor);

    void inject(LoadBackpackItemsInteractor loadBackpackItemsInteractor);

    void inject(GetSearchedUserDataInteractor getSearchedUserDataInteractor);

    void inject(LoadUnusualsInteractor loadUnusualsInteractor);

    void inject(LoadSelectorItemsInteractor loadSelectorItemsInteractor);

    void inject(SearchUserInteractor searchUserInteractor);

    void inject(LoadSearchItemsInteractor loadSearchItemsInteractor);
}
