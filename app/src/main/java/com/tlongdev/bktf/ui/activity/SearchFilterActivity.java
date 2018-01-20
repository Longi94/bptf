package com.tlongdev.bktf.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.spinner.QualityAdapter;
import com.tlongdev.bktf.model.Quality;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchFilterActivity extends BptfActivity {

    public static final String EXTRA_ENABLED = "enabled";
    public static final String EXTRA_QUALITY = "quality";
    public static final String EXTRA_TRADABLE = "tradable";
    public static final String EXTRA_CRAFTABLE = "craftable";
    public static final String EXTRA_AUSTRALIUM = "australium";

    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_ENABLED) boolean mEnabled = false;
    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_QUALITY) int mQuality = Quality.UNIQUE;
    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_TRADABLE) boolean mTradable = true;
    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_CRAFTABLE) boolean mCraftable = true;
    @SuppressWarnings("NullableProblems")
    @Nullable
    @InjectExtra(EXTRA_AUSTRALIUM) boolean mAustralium = true;

    @BindView(R.id.quality) Spinner qualitySpinner;
    @BindView(R.id.tradable) CheckBox tradable;
    @BindView(R.id.craftable) CheckBox craftable;
    @BindView(R.id.australium) CheckBox australium;
    @BindView(R.id.enable) Switch enableSwitch;

    private QualityAdapter qualityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);
        ButterKnife.bind(this);
        Dart.inject(this);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        setFinishOnTouchOutside(false);

        qualityAdapter = new QualityAdapter(mApplication);
        qualitySpinner.setAdapter(qualityAdapter);

        enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            qualitySpinner.setEnabled(isChecked);
            tradable.setEnabled(isChecked);
            craftable.setEnabled(isChecked);
            australium.setEnabled(isChecked);
        });

        enableSwitch.setChecked(mEnabled);
        qualitySpinner.setEnabled(mEnabled);
        tradable.setEnabled(mEnabled);
        craftable.setEnabled(mEnabled);
        australium.setEnabled(mEnabled);

        tradable.setChecked(mTradable);
        craftable.setChecked(mCraftable);
        australium.setChecked(mAustralium);
        qualitySpinner.setSelection(Arrays.asList(QualityAdapter.QUALITY_IDS).indexOf(mQuality));
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
