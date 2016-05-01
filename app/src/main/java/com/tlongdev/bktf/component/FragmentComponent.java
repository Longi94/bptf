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
import com.tlongdev.bktf.ui.fragment.BptfFragment;
import com.tlongdev.bktf.ui.fragment.CalculatorFragment;
import com.tlongdev.bktf.ui.fragment.ConverterFragment;
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
}
