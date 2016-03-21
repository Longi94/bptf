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
import com.tlongdev.bktf.ui.activity.SteamIdActivity;
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
@Component(modules = BptfAppModule.class)
public interface ActivityComponent {

    void inject(UserBackpackActivity userBackpackActivity);

    void inject(UserActivity userActivity);

    void inject(UnusualActivity unusualActivity);

    void inject(SteamIdActivity steamIdActivity);
}