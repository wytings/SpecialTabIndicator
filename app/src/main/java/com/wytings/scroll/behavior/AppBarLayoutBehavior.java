package com.wytings.scroll.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wytings.special.util.LogWrapper;

public class AppBarLayoutBehavior extends AppBarLayout.Behavior {

    private boolean isInFlingMode = false;
    private Boolean isToTop = null;

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
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        LogWrapper.d("AppBarLayoutBehavior onStartNestedScroll");
        isInFlingMode = true;
        isToTop = null;
        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed, int type) {
        LogWrapper.d("AppBarLayoutBehavior onNestedPreScroll type = %s, dy = %s ", type, dy);
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        if (dy > 0) {
            isToTop = true;
        } else if (dy < 0) {
            isToTop = false;
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        //LogWrapper.d("AppBarLayoutBehavior onNestedScroll type = %s, dyConsumed = %s ,dyUnconsumed =%s", type, dyConsumed, dyUnconsumed);
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    }


    @Override
    public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull AppBarLayout child, @NonNull View target, float velocityX, float velocityY, boolean consumed) {
        LogWrapper.d("AppBarLayoutBehavior onNestedFling , offset = %s", getTopAndBottomOffset());
        isInFlingMode = true;
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }


    @Override
    public void onStopNestedScroll(final CoordinatorLayout coordinatorLayout, final AppBarLayout abl, final View target, final int type) {
        super.onStopNestedScroll(coordinatorLayout, abl, target, type);
        LogWrapper.d("AppBarLayoutBehavior autoScrollAppBar ,type = %s,  offset = %s", type, getTopAndBottomOffset());

        if (isInFlingMode) {
            if (type == ViewCompat.TYPE_NON_TOUCH) {

            }
        } else {

        }
    }
}