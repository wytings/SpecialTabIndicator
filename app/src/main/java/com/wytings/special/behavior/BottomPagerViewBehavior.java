package com.wytings.special.behavior;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */

public class BottomPagerViewBehavior extends BaseBehavior<ViewPager> {

    private boolean isAutoScrollEnabled = false;
    private boolean isDragging = false;
    private final Scroller scroller;
    private final Handler mainHandler;
    private final Runnable flingRunnable = new Runnable() {
        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                isDragging = false;
                setViewParamsHeight(dependentView, scroller.getCurrY());
                mainHandler.post(this);
            } else {
                statusBehavior.isAutoScrolling = false;
            }
        }
    };
    private final StatusBehavior statusBehavior = new StatusBehavior();

    public BottomPagerViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        mainHandler = new Handler();
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ViewPager child, View dependency) {
        final int dependencyHeight = getDependencyHeight();

        child.setTranslationY(dependencyHeight);

        if (dependencyHeight == dependencyCollapseHeight || dependencyHeight == dependencyInitHeight) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            params.height = parent.getHeight() - dependencyCollapseHeight;
            child.setLayoutParams(params);
            if (parent.getTag() == null) {
                parent.setTag(statusBehavior);
            }
        }

        return true;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ViewPager child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes, int type) {
        return type == ViewCompat.TYPE_TOUCH && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
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
            int miniHeight = dependencyCollapseHeight;
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
        isDragging = dyUnconsumed < 0;

        if (dyUnconsumed > 0) {
            int newHeight = (int) (getDependencyHeight() - dyUnconsumed * 0.7f);
            if (newHeight < dependencyCollapseHeight) {
                newHeight = dependencyCollapseHeight;
            }
            setViewParamsHeight(dependentView, newHeight);
        } else {
            final int newHeight = (int) (getDependencyHeight() - dyUnconsumed * 0.7f);
            setViewParamsHeight(dependentView, newHeight);
        }
    }

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ViewPager child, @NonNull View target, float velocityX, float velocityY) {
        return onAutoScrolling(Math.abs(velocityY) > 5000 ? 5000 : velocityY);
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ViewPager child, @NonNull View target, int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }
        isDragging = false;

        if (scroller.isFinished() && isAutoScrollEnabled) {
            onAutoScrolling(1000);
        }
    }

    private boolean onAutoScrolling(float velocity) {// velocity>0 means dragging down, otherwise dragging up.
        float currentHeight = getDependencyHeight();
        if (currentHeight == dependencyCollapseHeight || currentHeight == dependencyInitHeight) {
            return false;
        }

        if (Math.abs(velocity) <= 800) {
            velocity = 800;
        }

        final boolean willCollapse = currentHeight < (dependencyInitHeight + dependencyCollapseHeight) / 2;

        float targetHeight = willCollapse ? dependencyCollapseHeight : dependencyInitHeight;

        scroller.startScroll(0, (int) currentHeight, 0, (int) (targetHeight - currentHeight), (int) (1000000 / Math.abs(velocity)));
        mainHandler.post(flingRunnable);
        statusBehavior.isAutoScrolling = true;
        return true;
    }

}