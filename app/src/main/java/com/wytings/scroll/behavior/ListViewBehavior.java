package com.wytings.scroll.behavior;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

import com.wytings.special.R;
import com.wytings.special.util.LogUtils;

/**
 * Created by Rex.Wei on 2019-08-06
 *
 * @author weiyuting
 */
public class ListViewBehavior extends AbsBehavior<View> {

    private final Scroller scroller;
    private final Handler mainHandler;
    private boolean isAutoScrollEnabled = false;
    private boolean isDragging = false;
    private final Runnable flingRunnable = new Runnable() {
        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                isDragging = false;
                setViewParamsHeight(dependentView, scroller.getCurrY());
                mainHandler.post(this);
            } else {
                setAutoScrolling(false);
            }
        }
    };

    public ListViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs, R.id.search_layout);
        scroller = new Scroller(context);
        mainHandler = new Handler();
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        final int dependencyHeight = getDependencyHeight();

        child.setTranslationY(dependencyHeight);

        if (dependencyHeight == getDependencyCollapseHeight() || dependencyHeight == getDependencyInitHeight()) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            params.height = parent.getHeight() - dependencyHeight;
            child.setLayoutParams(params);
        }

        return true;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull View child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int nestedScrollAxes,
                                       int type) {
        final boolean result = type == ViewCompat.TYPE_TOUCH && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        LogUtils.d("onStartNestedScroll, child = %s, directTargetChild =%s ,target = %s , result = %s", child, directTargetChild, target, result);
        return result;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull View child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int nestedScrollAxes,
                                       int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }
        scroller.abortAnimation();
        isAutoScrollEnabled = false;
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }
        isDragging = false;

        if (scroller.isFinished() && isAutoScrollEnabled) {
            onAutoScrolling(1000);
        }
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull View child,
                               @NonNull View target,
                               int dxConsumed,
                               int dyConsumed,
                               int dxUnconsumed,
                               int dyUnconsumed,
                               int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }
        LogUtils.d("onNestedScroll, dxConsumed = %s, dyConsumed = %s, dxUnconsumed = %s,dyUnconsumed = %s",
                   dxConsumed,
                   dyConsumed,
                   dxUnconsumed,
                   dyUnconsumed);
        // dy>0 to up, dy<0 to down
        isDragging = dyUnconsumed < 0;

        if (dyUnconsumed > 0) {
            int newHeight = (int) (getDependencyHeight() - dyUnconsumed * 0.7f);
            if (newHeight < getDependencyCollapseHeight()) {
                newHeight = getDependencyCollapseHeight();
            }
            setViewParamsHeight(dependentView, newHeight);
        } else {
            int newHeight = (int) (getDependencyHeight() - dyUnconsumed * 0.7f);

            if (newHeight > getDependencyInitHeight()) {
                newHeight = getDependencyInitHeight();
            }
            setViewParamsHeight(dependentView, newHeight);
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                  @NonNull View child,
                                  @NonNull View target,
                                  int dx,
                                  int dy,
                                  @NonNull int[] consumed,
                                  int type) {
        isAutoScrollEnabled = true;
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }

        LogUtils.d("onNestedPreScroll, dx = %s, dy = %s ,isDragging = %s",
                   dx,
                   dy,
                   isDragging);

        if (dy <= 0 || isDragging) {
            if (isDragging && dy < 0) {
                if (getDependencyHeight() < getDependencyInitHeight()) {
                    int newHeight = getDependencyHeight() - dy;
                    if (newHeight > getDependencyInitHeight()) {
                        newHeight = getDependencyInitHeight();
                    }
                    setViewParamsHeight(dependentView, newHeight);
                    consumed[1] = dy;
                }
            }
            return;
        }

        int newHeight = getDependencyHeight() - dy;
        // dy>0 up
        int miniHeight = getDependencyCollapseHeight();
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

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout,
                                    @NonNull View child,
                                    @NonNull View target,
                                    float velocityX,
                                    float velocityY) {
        return onAutoScrolling(Math.abs(velocityY) > 5000 ? 5000 : velocityY);
    }

    private boolean onAutoScrolling(float velocity) {// velocity>0 means dragging down, otherwise dragging up.
        float currentHeight = getDependencyHeight();
        if (currentHeight == getDependencyCollapseHeight() || currentHeight == getDependencyInitHeight()) {
            return false;
        }

        if (Math.abs(velocity) <= 800) {
            velocity = 800;
        }

        final boolean willCollapse = currentHeight < (getDependencyInitHeight() + getDependencyCollapseHeight()) / 2;

        float targetHeight = willCollapse ? getDependencyCollapseHeight() : getDependencyInitHeight();

        scroller.startScroll(0, (int) currentHeight, 0, (int) (targetHeight - currentHeight), (int) (1000000 / Math.abs(velocity)));
        mainHandler.post(flingRunnable);
        setAutoScrolling(true);
        return true;
    }

}