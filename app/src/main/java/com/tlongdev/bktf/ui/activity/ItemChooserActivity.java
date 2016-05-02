/**
 * Copyright 2015 Long Tran
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

package com.tlongdev.bktf.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.spinner.EffectAdapter;
import com.tlongdev.bktf.adapter.spinner.QualityAdapter;
import com.tlongdev.bktf.adapter.spinner.WeaponWearAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.presenter.activity.ItemChooserPresenter;
import com.tlongdev.bktf.ui.view.activity.ItemChooserView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog style activity for selecting items to be added to the calculator list.
 */
public class ItemChooserActivity extends BptfActivity implements ItemChooserView {

    private static final int SELECT_ITEM = 100;

    public static final String EXTRA_ITEM = "item";
    public static final String EXTRA_IS_FROM_CALCULATOR = "calculator";

    @Inject ItemChooserPresenter mPresenter;

    @BindView(R.id.quality) Spinner qualitySpinner;
    @BindView(R.id.effect) Spinner effectSpinner;
    @BindView(R.id.weapon_wear) Spinner wearSpinner;
    @BindView(R.id.title_effect) TextView titleEffect;
    @BindView(R.id.title_wear) TextView titleWear;
    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.item_text) TextView itemText;
    @BindView(R.id.item_name) TextView itemName;
    @BindView(R.id.tradable) CheckBox tradable;
    @BindView(R.id.craftable) CheckBox craftable;
    @BindView(R.id.australium) CheckBox australium;
    @BindView(R.id.fab) FloatingActionButton fab;

    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_IS_FROM_CALCULATOR) boolean isFromCalculator = false;

    private Item mItem;
    private QualityAdapter qualityAdapter;
    private WeaponWearAdapter wearAdapter;
    private EffectAdapter effectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_chooser);
        ButterKnife.bind(this);
        Dart.inject(this);

        mApplication.getActivityComponent().inject(this);

        mPresenter.attachView(this);

        setTitle(null);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        effectAdapter = new EffectAdapter(this);
        effectSpinner.setAdapter(effectAdapter);

        qualityAdapter = new QualityAdapter(mApplication);
        qualitySpinner.setAdapter(qualityAdapter);
        qualitySpinner.setSelection(7);
        qualitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                effectSpinner.setVisibility(View.GONE);
                wearSpinner.setVisibility(View.GONE);
                titleEffect.setVisibility(View.GONE);
                titleWear.setVisibility(View.GONE);
                switch (position) {
                    case 8:
                        effectSpinner.setVisibility(View.VISIBLE);
                        titleEffect.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        wearSpinner.setVisibility(View.VISIBLE);
                        titleWear.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //huh?
            }
        });

        wearAdapter = new WeaponWearAdapter(this);
        wearSpinner.setAdapter(wearAdapter);

        mItem = new Item();

        fab.hide();

        mPresenter.loadEffects();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_ITEM:
                if (resultCode == RESULT_OK) {
                    mItem.setDefindex(data.getIntExtra(SelectItemActivity.EXTRA_DEFINDEX, -1));
                    mItem.setName(data.getStringExtra(SelectItemActivity.EXTRA_NAME));

                    icon.setVisibility(View.VISIBLE);
                    itemText.setVisibility(View.GONE);
                    itemName.setVisibility(View.VISIBLE);
                    itemName.setText(mItem.getName());
                    Glide.with(this)
                            .load(mItem.getIconUrl())
                            .into(icon);

                    fab.show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("WrongConstant")
    @OnClick(R.id.fab)
    public void submit() {
        mItem.setQuality(qualityAdapter.getQualityId(qualitySpinner.getSelectedItemPosition()));
        if (mItem.getQuality() == Quality.UNUSUAL) {
            mItem.setPriceIndex(effectAdapter.getEffectId(effectSpinner.getSelectedItemPosition()));
        } else if (mItem.getQuality() == Quality.PAINTKITWEAPON) {
            mItem.setWeaponWear(wearAdapter.getWearId(wearSpinner.getSelectedItemPosition()));
        }

        mItem.setTradable(tradable.isChecked());
        mItem.setCraftable(craftable.isChecked());
        mItem.setAustralium(australium.isChecked());

        if (isFromCalculator) {
            if (!mPresenter.checkCalculator(mItem)) {
                return;
            }
        }

        Intent result = new Intent();
        result.putExtra(EXTRA_ITEM, mItem);
        setResult(RESULT_OK, result);
        finish();
    }

    @OnClick(R.id.item)
    public void selectItem() {
        startActivityForResult(new Intent(this, SelectItemActivity.class), SELECT_ITEM);
    }

    @Override
    public void showEffects(List<Item> items) {
        effectAdapter.setDataSet(items);
        effectAdapter.notifyDataSetChanged();
    }
}
