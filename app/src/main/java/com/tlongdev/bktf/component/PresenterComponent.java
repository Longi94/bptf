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
import com.tlongdev.bktf.module.StorageModule;
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
}
