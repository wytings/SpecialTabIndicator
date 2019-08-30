package com.wytings.scroll;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.wytings.R;
import com.wytings.adapter.MyAdapter;
import com.wytings.special.util.LogWrapper;

/**
 * Created by Rex.Wei on 2019-08-08
 *
 * @author weiyuting
 */
public class AppBarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_bar);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        final AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        final CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinator_layout);

        recyclerView.setAdapter(new MyAdapter(this, 50));

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                LogWrapper.d("AppBarActivity onOffsetChanged verticalOffset = %s", verticalOffset);
            }
        });

    }
}
