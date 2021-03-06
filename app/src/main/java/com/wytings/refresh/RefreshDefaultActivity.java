package com.wytings.refresh;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.wytings.R;
import com.wytings.adapter.MyAdapter;

/**
 * Created by Rex.Wei on 2019-09-19
 *
 * @author weiyuting
 */
public class RefreshDefaultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_system);

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.behavior_content_layout);
        // swipeRefreshLayout.setProgressViewOffset(false, 100, 300);
        // swipeRefreshLayout.setProgressViewEndTarget(false, 200);
        // swipeRefreshLayout.setDistanceToTriggerSync(300);

       // swipeRefreshLayout.setRefreshing(true);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(true);
//            }
//        }, 1000);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new MyAdapter(this, 30));

    }
}
