package com.tlongdev.bktf.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Long on 2016. 01. 03..
 */
public class LoseFocusEditText extends EditText {
    public LoseFocusEditText(Context context) {
        super(context);
    }

    public LoseFocusEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoseFocusEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public void onEditorAction(int actionCode) {
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            clearFocus();
            InputMethodManager inputMethodManager = (InputMethodManager)  getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
        super.onEditorAction(actionCode);
    }
}