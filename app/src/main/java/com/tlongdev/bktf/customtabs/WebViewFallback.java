package com.tlongdev.bktf.customtabs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.tlongdev.bktf.ui.activity.WebActivity;

/**
 * @author Long
 * @since 2016. 04. 21.
 */
public class WebViewFallback implements CustomTabActivityHelper.CustomTabFallback {

    @Override
    public void openUri(Activity activity, Uri uri) {
        Intent intent = new Intent(activity, WebActivity.class);
        intent.putExtra(WebActivity.EXTRA_URL, uri.toString());
        activity.startActivity(intent);
    }
}
