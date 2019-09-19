package com.wytings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.wytings.refresh.RefreshActivity;
import com.wytings.scroll.AppBarActivity;
import com.wytings.scroll.ScrollActivity;
import com.wytings.special.SpecialActivity;

/**
 * Created by Rex.Wei on 2019-08-08
 *
 * @author weiyuting
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findViewById(R.id.special).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(v.getContext(), SpecialActivity.class));
            }
        });
        findViewById(R.id.scroll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(v.getContext(), ScrollActivity.class));
            }
        });
        findViewById(R.id.app_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(v.getContext(), AppBarActivity.class));
            }
        });
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(v.getContext(), RefreshActivity.class));
            }
        });

    }
}
