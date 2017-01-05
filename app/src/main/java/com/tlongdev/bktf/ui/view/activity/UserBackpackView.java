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

package com.tlongdev.bktf.ui.view.activity;

import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.ui.view.BaseView;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 18.
 */
public interface UserBackpackView extends BaseView {

    void showItems(List<BackpackItem> items, List<BackpackItem> newItems);

    void privateBackpack();
}