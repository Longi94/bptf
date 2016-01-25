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

package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.spinner.QualityAdapter;
import com.tlongdev.bktf.model.Quality;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchFilterActivity extends AppCompatActivity {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = SearchActivity.class.getSimpleName();

    public static final String EXTRA_ENABLED = "enabled";
    public static final String EXTRA_QUALITY = "quality";
    public static final String EXTRA_TRADABLE = "tradable";
    public static final String EXTRA_CRAFTABLE = "craftable";
    public static final String EXTRA_AUSTRALIUM = "australium";

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    @Bind(R.id.quality) Spinner qualitySpinner;
    @Bind(R.id.tradable) CheckBox tradable;
    @Bind(R.id.craftable) CheckBox craftable;
    @Bind(R.id.australium) CheckBox australium;
    @Bind(R.id.enable) Switch enableSwitch;

    private QualityAdapter qualityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);
        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        setFinishOnTouchOutside(false);

        qualityAdapter = new QualityAdapter(this);
        qualitySpinner.setAdapter(qualityAdapter);

        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                qualitySpinner.setEnabled(isChecked);
                tradable.setEnabled(isChecked);
                craftable.setEnabled(isChecked);
                australium.setEnabled(isChecked);
            }
        });

        Intent intent = getIntent();

        boolean isChecked = intent.getBooleanExtra(EXTRA_ENABLED, false);
        enableSwitch.setChecked(isChecked);

        qualitySpinner.setEnabled(isChecked);
        tradable.setEnabled(isChecked);
        craftable.setEnabled(isChecked);
        australium.setEnabled(isChecked);

        tradable.setChecked(intent.getBooleanExtra(EXTRA_TRADABLE, true));
        craftable.setChecked(intent.getBooleanExtra(EXTRA_CRAFTABLE, true));
        australium.setChecked(intent.getBooleanExtra(EXTRA_AUSTRALIUM, true));
        qualitySpinner.setSelection(Arrays.asList(QualityAdapter.QUALITY_IDS)
                .indexOf(intent.getIntExtra(EXTRA_QUALITY, Quality.UNIQUE)));

    }

    @Override
    protected void onResume() {
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        super.onResume();
    }

    @OnClick(R.id.apply)
    public void apply(View v) {
        Intent intent = new Intent(this, SearchFilterActivity.class);
        intent.putExtra(EXTRA_ENABLED, enableSwitch.isChecked());
        intent.putExtra(EXTRA_TRADABLE, tradable.isChecked());
        intent.putExtra(EXTRA_CRAFTABLE, craftable.isChecked());
        intent.putExtra(EXTRA_QUALITY, qualityAdapter.getQualityId(qualitySpinner.getSelectedItemPosition()));
        intent.putExtra(EXTRA_AUSTRALIUM, australium.isChecked());

        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.cancel)
    public void cancel(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
