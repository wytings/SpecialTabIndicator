package com.wytings.scroll.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wytings.special.util.LogWrapper;

public class AppBarLayoutBehavior extends AppBarLayout.Behavior {

    public AppBarLayoutBehavior() {
    }

    public AppBarLayoutBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(final CoordinatorLayout parent, final AppBarLayout abl, final int layoutDirection) {
        LogWrapper.d("AppBarLayoutBehavior onLayoutChild");
        return super.onLayoutChild(parent, abl, layoutDirection);
    }

    @Override
    public void onStopNestedScroll(final CoordinatorLayout coordinatorLayout, final AppBarLayout abl, final View target, final int type) {
        super.onStopNestedScroll(coordinatorLayout, abl, target, type);
        if (type == ViewCompat.TYPE_TOUCH) {
            autoScrollAppBar(coordinatorLayout, abl);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(final CoordinatorLayout parent, final AppBarLayout child, final MotionEvent ev) {
        LogWrapper.d("AppBarLayoutBehavior onInterceptTouchEvent , event = %s", ev);
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    private void autoScrollAppBar(final CoordinatorLayout coordinatorLayout, final AppBarLayout abl) {
        LogWrapper.d("AppBarLayoutBehavior autoScrollAppBar , offset = %s", getTopAndBottomOffset());

        final boolean expand = Math.abs(getTopAndBottomOffset()) > 240 / 2;
        // abl.setExpanded(expand, true);
    }
}