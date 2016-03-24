/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.component;

import com.tlongdev.bktf.adapter.BackpackAdapter;
import com.tlongdev.bktf.adapter.CalculatorAdapter;
import com.tlongdev.bktf.adapter.FavoritesAdapter;
import com.tlongdev.bktf.adapter.HistoryAdapter;
import com.tlongdev.bktf.adapter.RecentsAdapter;
import com.tlongdev.bktf.adapter.SearchAdapter;
import com.tlongdev.bktf.adapter.SelectItemAdapter;
import com.tlongdev.bktf.adapter.UnusualAdapter;
import com.tlongdev.bktf.module.BptfAppModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Long
 * @since 2016. 03. 24.
 */
@Singleton
@Component(modules = {BptfAppModule.class})
public interface AdapterComponent {
    void inject(BackpackAdapter backpackAdapter);

    void inject(CalculatorAdapter calculatorAdapter);

    void inject(FavoritesAdapter favoritesAdapter);

    void inject(HistoryAdapter historyAdapter);

    void inject(RecentsAdapter recentsAdapter);

    void inject(SearchAdapter searchAdapter);

    void inject(SelectItemAdapter selectItemAdapter);

    void inject(UnusualAdapter unusualAdapter);
}
