package com.tlongdev.bktf.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.ui.view.BaseView;

/**
 * @author Long
 * @since 2016. 03. 21.
 */
public abstract class BptfFragment extends Fragment implements BaseView {

    protected BptfApplication mApplication;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (BptfApplication) getActivity().getApplication();
        mApplication.getFragmentComponent().inject(this);
    }

    @Override
    public void showToast(CharSequence message, int duration) {
        Toast.makeText(getActivity(), message, duration).show();
    }
}
