package com.tlongdev.bktf.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public abstract class BptfActivity extends AppCompatActivity implements BaseView {

    protected BptfApplication mApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (BptfApplication) getApplication();
        mApplication.getActivityComponent().inject(this);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));
    }

    @Override
    public void showToast(CharSequence message, int duration) {
        Toast.makeText(this, message, duration).show();
    }
}
