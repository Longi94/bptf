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

package com.tlongdev.bktf.ui.view.fragment;

import android.database.Cursor;

import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public interface RecentsView extends BaseView {
    void showPrices(Cursor prices);

    void showError();

    void showRefreshAnimation();

    void hideRefreshingAnimation();

    void dismissLoadingDialog();

    void showLoadingDialog(String message);

    void updateLoadingDialog(int max, String message);

    void showItemSchemaError(String errorMessage);

    void showPricesError(String errorMessage);

    void updateCurrencyHeader(Price metalPrice, Price keyPrice, Price budPrice);

    void showErrorDialog();
}
