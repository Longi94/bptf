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

import com.tlongdev.bktf.module.BptfAppModule;
import com.tlongdev.bktf.module.PresenterModule;
import com.tlongdev.bktf.module.StorageModule;
import com.tlongdev.bktf.ui.activity.AppCompatPreferenceActivity;
import com.tlongdev.bktf.ui.activity.BptfActivity;
import com.tlongdev.bktf.ui.activity.ItemChooserActivity;
import com.tlongdev.bktf.ui.activity.ItemDetailActivity;
import com.tlongdev.bktf.ui.activity.LicensesActivity;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.PriceHistoryActivity;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.ui.activity.SelectItemActivity;
import com.tlongdev.bktf.ui.activity.SettingsActivity;
import com.tlongdev.bktf.ui.activity.UnusualActivity;
import com.tlongdev.bktf.ui.activity.UserActivity;
import com.tlongdev.bktf.ui.activity.UserBackpackActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
@Singleton
@Component(modules = {PresenterModule.class, BptfAppModule.class, StorageModule.class})
public interface ActivityComponent {

    void inject(BptfActivity bptfActivity);

    void inject(AppCompatPreferenceActivity appCompatPreferenceActivity);

    void inject(MainActivity mainActivity);

    void inject(ItemChooserActivity itemChooserActivity);

    void inject(ItemDetailActivity itemDetailActivity);

    void inject(LicensesActivity licensesActivity);

    void inject(SettingsActivity settingsActivity);

    void inject(PriceHistoryActivity priceHistoryActivity);

    void inject(SearchActivity searchActivity);

    void inject(SelectItemActivity selectItemActivity);

    void inject(UnusualActivity unusualActivity);

    void inject(UserActivity userActivity);

    void inject(UserBackpackActivity userBackpackActivity);
}