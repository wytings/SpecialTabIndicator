package com.wytings.special.behavior;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.wytings.special.util.G;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */

public class ViewPagerBehavior extends AbsHeaderInfoBehavior<ViewPager> {

    private boolean isAutoScrollEnabled = false;
    private boolean isDragging = false;
    private final Scroller scroller;
    private final Handler mainHandler;
    private final Runnable flingRunnable = new Runnable() {
        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                isDragging = false;
                // RDLog.d("computeScrollOffset, currentY = %s", scroller.getCurrY());
                setViewParamsHeight(dependentView, scroller.getCurrY());
                mainHandler.post(this);
            }
        }
    };

    public ViewPagerBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        mainHandler = new Handler();
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ViewPager child, View dependency) {
        final int dependencyHeight = getDependencyHeight();
        // RDLog.d("onDependentViewChanged, dependency.getHeight=%s,params.height=%s", dependency.getHeight(), dependencyHeight);

        child.setTranslationY(dependencyHeight);

        if (dependencyHeight == getDependentViewCollapsedHeight() || dependencyHeight == dp(220)) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            params.height = parent.getHeight() - getDependentViewCollapsedHeight();
            child.setLayoutParams(params);
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, ViewPager child, MotionEvent ev) {
        int dependencyHeight = getDependencyHeight();
        G.d("onInterceptTouchEvent dependencyHeight = %s, isDragging = %s", dependencyHeight, isDragging);
        if (dp(450) < dependencyHeight) {
            return true;
        } else if (ev.getHistorySize() > 1) {
            if (ev.getY() < ev.getHistoricalY(1)) { // to top
                if (getDependentViewCollapsedHeight() < dependencyHeight && dependencyHeight < dp(164)) {
                    return onUserStopDragging(2000);
                }
            }
        }
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ViewPager child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes, int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return false;
        }
        G.d("----------onNestedStartScroll, nestedScrollAxes=%s,type = %s", nestedScrollAxes, type);
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ViewPager child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes, int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }
        scroller.abortAnimation();
        isAutoScrollEnabled = false;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ViewPager child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        isAutoScrollEnabled = true;
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }

        G.d("----------onNestedPreScroll,getHeight = %s,dy=%s,isDragging=%s", getDependencyHeight(), dy, isDragging);
        if (dy <= 0) {
            return;
        }

        if (isDragging) {
            return;
        }


        int newHeight = getDependencyHeight() - dy;
        if (dy < 0) { // dy<0 down
            setViewParamsHeight(dependentView, newHeight);
            consumed[1] = dy;

        } else if (dy > 0) {
            // dy>0 up
            int miniHeight = getDependentViewCollapsedHeight();
            if (newHeight >= miniHeight) {
                setViewParamsHeight(dependentView, newHeight);
                consumed[1] = dy;
            } else {
                int gap = miniHeight - getDependencyHeight();
                if (gap < dy) {
                    setViewParamsHeight(dependentView, miniHeight);
                    consumed[1] = gap;
                }
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ViewPager child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }
        // dy>0 to up, dy<0 to down
        G.d("----------onNestedScroll, dyConsumed = %s, dyUnconsumed = %s,getHeight=%s", dyConsumed, dyUnconsumed, getDependencyHeight());
        if (dyUnconsumed > 0) {
            return;
        }

        if (dp(450) < getDependencyHeight()) {
            target.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
        }

        isDragging = dyUnconsumed < 0;
        G.d("----------onNestedScroll, isDragging=%s, dyUnconsumed = %s, getHeight=%s", isDragging, dyUnconsumed, getDependencyHeight());

        final int newHeight = getDependencyHeight() - dyUnconsumed;
        setViewParamsHeight(dependentView, newHeight);
    }

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ViewPager child, @NonNull View target, float velocityX, float velocityY) {
        G.d("----------onNestedPreFling, velocityX = %s, velocityY = %s", velocityX, velocityY);
        return onUserStopDragging(velocityY);
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ViewPager child, @NonNull View target, int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }
        isDragging = false;
        G.d("----------onNestedStopScroll, target = %s, type = %s,isDragging = %s", target, type, isDragging);

        if (scroller.isFinished() && isAutoScrollEnabled) {
            onUserStopDragging(1000);
        }
    }

    private boolean onUserStopDragging(float velocity) {// velocity>0 means dragging down, otherwise dragging up.
        float currentHeight = dependentView.getHeight();
        float miniHeight = getDependentViewCollapsedHeight();
        if (currentHeight == miniHeight || currentHeight == dp(220)) {
            return false;
        }

        final boolean willCollapse; // Flag indicates whether to expand the content.
        if (Math.abs(velocity) <= 800) {
            velocity = 800; // Limit velocity's minimum value.
        }

        if (currentHeight < dp(164)) { // 164(*3=492) is in the middle of [108,220]. gap=56
            willCollapse = true;
        } else {
            willCollapse = false;
        }

        float targetHeight = willCollapse ? miniHeight : dp(220);

        scroller.startScroll(0, (int) currentHeight, 0, (int) (targetHeight - currentHeight), (int) (1000000 / Math.abs(velocity)));
        mainHandler.post(flingRunnable);
        G.d("onUserStopDragging,velocity = %s,currentHeight=%s,isDragging = %s", velocity, currentHeight, isDragging);
        return true;
    }

}