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

    private boolean isScrollingDown = false;
    private Integer lastOffset = null;
    private static boolean expand = true;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_bar);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        final AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        final CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinator_layout);

        recyclerView.setAdapter(new MyAdapter(this, 50));
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                LogWrapper.d("onScrollStateChanged, state = %s, height = %s", newState, recyclerView.getHeight());
//                if (newState == SCROLL_STATE_IDLE) {
//                    appBarLayout.setExpanded(isScrollingDown, true);
//                    isScrollingDown = false;
//                    lastOffset = null;
//                }
//            }
//
//            @Override
//            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                LogWrapper.d("onScrolled, dx = %s, dy =%s, height = %s", dx, dy, recyclerView.getHeight());
//            }
//        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {

                if (lastOffset == null) {
                    lastOffset = verticalOffset;
                }

                isScrollingDown = lastOffset < verticalOffset;

                LogWrapper.d("onOffsetChanged, verticalOffset = %s,isScrollingDown = %s", verticalOffset, isScrollingDown);

            }
        });

        appBarLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                expand = !expand;
                appBarLayout.setExpanded(expand,true);
            }
        },5000);

    }
}
