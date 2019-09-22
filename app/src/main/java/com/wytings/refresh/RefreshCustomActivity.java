package com.wytings.refresh;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.wytings.R;
import com.wytings.adapter.MyAdapter;
import com.wytings.special.util.LogWrapper;

/**
 * Created by Rex.Wei on 2019-09-19
 *
 * @author weiyuting
 */
public class RefreshCustomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_refresh_custom);
        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new MyAdapter(this, 30));

        final SuperSwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.behavior_content_layout);
        swipeRefreshLayout.setOnRefreshListener(new SuperSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LogWrapper.d("RefreshCustomActivity , onRefresh ,top = %s", recyclerView.getTop());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LogWrapper.d("RefreshCustomActivity , cancel onRefresh ,top = %s", recyclerView.getTop());
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 5000);
            }
        });

    }
}
