package com.tlongdev.bktf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class AboutActivity extends ActionBarActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        findViewById(R.id.text_view_quick_return).setOnClickListener(this);
        findViewById(R.id.text_view_particles).setOnClickListener(this);
        findViewById(R.id.button_send).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.text_view_quick_return:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/felipecsl/QuickReturn"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.text_view_particles:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://backpack.tf/developer/particles"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.button_send:
                intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","tlongdev@gmail.com", null));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intent, "Send email..."));
                }
                break;
        }
    }
}
