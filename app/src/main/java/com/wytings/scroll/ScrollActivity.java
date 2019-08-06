package com.wytings.scroll;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.wytings.adapter.MyAdapter;
import com.wytings.special.R;

/**
 * Created by Rex.Wei on 2019-08-06
 *
 * @author weiyuting
 */
public class ScrollActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);


        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new MyAdapter(this, 30));

//        final FrameLayout frameLayout = findViewById(R.id.search_layout);
//        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
//
//            final Animation visibleAnimation = AnimationUtils.loadAnimation(getApplication(), R.anim.slide_top_in);
//            final Animation inVisibleAnimation = AnimationUtils.loadAnimation(getApplication(), R.anim.slide_top_out);
//
//            boolean isVisible = false;
//
//            @Override
//            public void onClick(final View v) {
//                isVisible = !isVisible;
//                if (isVisible) {
//                    frameLayout.setVisibility(View.VISIBLE);
//                    frameLayout.startAnimation(visibleAnimation);
//                } else {
//                    frameLayout.setVisibility(View.INVISIBLE);
//                    frameLayout.startAnimation(inVisibleAnimation);
//
//                }
//            }
//        });

    }
}
