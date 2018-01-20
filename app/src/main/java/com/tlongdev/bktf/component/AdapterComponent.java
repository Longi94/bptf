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
