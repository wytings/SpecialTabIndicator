package com.wytings.scroll.behavior;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

import com.wytings.special.util.LogWrapper;

/**
 * Created by Rex.Wei on 2019-08-06
 *
 * @author weiyuting
 */
public class BottomLayoutBehavior extends AbsBehavior<View> {

    private static final int MIN_VELOCITY = 800;
    private static final int MAX_VELOCITY = 5000;
    private final Scroller scroller;
    private final Handler mainHandler;
    private final int minHeight;
    private final int defaultHeight;
    private final int maxHeight;
    private final boolean isMaxDragEnabled;

    private boolean isAutoScrollEnabled = false;
    private boolean isDragging = false;

    public BottomLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        mainHandler = new Handler();
        final String tag = getTag(context, attrs);
        final int[] minDefaultMax = parseMinDefaultMax(tag);

        isMaxDragEnabled = isMaxDragEnabled(tag);
        minHeight = dp(context, minDefaultMax[0]);
        defaultHeight = dp(context, minDefaultMax[1]);
        maxHeight = dp(context, minDefaultMax[2]);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (parent.getHeight() > 0) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            if (params.height != parent.getHeight() - minHeight) {
                params.height = parent.getHeight() - minHeight;
                child.setLayoutParams(params);
            }
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull View child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int nestedScrollAxes,
                                       int type) {
        final boolean result = type == ViewCompat.TYPE_TOUCH && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        LogWrapper.d("onStartNestedScroll, child = %s, directTargetChild =%s ,target = %s , result = %s", child, directTargetChild, target, result);
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
        LogWrapper.d("onNestedScrollAccepted");
        scroller.abortAnimation();
        isAutoScrollEnabled = false;
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }
        LogWrapper.d("onStopNestedScroll - isAutoScrollEnabled = %s, isFinished = %s", isAutoScrollEnabled, scroller.isFinished());
        isDragging = false;
        if (scroller.isFinished() && isAutoScrollEnabled) {
            onAutoScrolling(child, 2000);
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
        LogWrapper.d("onNestedScroll, dxConsumed = %s, dyConsumed = %s, dxUnconsumed = %s,dyUnconsumed = %s",
                     dxConsumed,
                     dyConsumed,
                     dxUnconsumed,
                     dyUnconsumed);
        // dy>0 to up, dy<0 to down
        isDragging = dyUnconsumed < 0;

        if (dyUnconsumed > 0) {
            final int targetY = (int) (minHeight - dyUnconsumed * 0.7f);
            if (targetY <= minHeight) {
                child.setTranslationY(minHeight);
            } else {
                child.setTranslationY(targetY);
            }
        } else {
            if (child.getTranslationY() < maxHeight) {
                final int targetY = (int) (child.getTranslationY() - dyUnconsumed * 0.7f);
                final int destY = isMaxDragEnabled ? maxHeight : defaultHeight;
                if (targetY >= destY) {
                    child.setTranslationY(destY);
                } else {
                    child.setTranslationY(targetY);
                }
            }
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

        LogWrapper.d("onNestedPreScroll, dx = %s, dy = %s ,isDragging = %s",
                     dx,
                     dy,
                     isDragging);
        // dy>0 to up, dy<0 to down
        if (dy <= 0 || isDragging) {
            if (isDragging && dy < 0) {
                final int destY = isMaxDragEnabled ? maxHeight : defaultHeight;
                if (child.getTranslationY() < destY) {
                    final int targetY = (int) (child.getTranslationY() - dy);
                    if (targetY > destY) {
                        child.setTranslationY(destY);
                        consumed[1] = destY - targetY;
                    } else {
                        child.setTranslationY(targetY);
                        consumed[1] = dy;
                    }
                }
            }
            return;
        }

        final int targetY = (int) (child.getTranslationY() - dy);
        if (targetY >= minHeight) {
            child.setTranslationY(targetY);
            consumed[1] = dy;
        } else {
            consumed[1] = (int) (child.getTranslationY() - minHeight);
            child.setTranslationY(minHeight);
        }
    }

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout,
                                    @NonNull View child,
                                    @NonNull View target,
                                    float velocityX,
                                    float velocityY) {
        final boolean result = onAutoScrolling(child, velocityY);
        LogWrapper.d("onNestedPreFling, result = %s , velocityX = %s, velocityY = %s", result, velocityX, velocityY);
        return result;
    }

    private float getSuitableVelocity(float velocity) {
        final float sign = Math.signum(velocity);
        float absV = Math.abs(velocity);
        if (absV > MAX_VELOCITY) {
            absV = MAX_VELOCITY;
        } else if (absV < MIN_VELOCITY) {
            absV = MIN_VELOCITY;
        }
        return sign * absV;
    }

    private boolean onAutoScrolling(View child, float velocity) {// velocity<0 means dragging down, velocity>0 dragging up.
        final int currentY = (int) child.getTranslationY();
        if (currentY == defaultHeight || currentY == minHeight || currentY == maxHeight) {
            return false;
        }

        final float suitableVelocity = getSuitableVelocity(velocity);
        final int targetY;
        if (suitableVelocity > 0) {
            targetY = currentY > defaultHeight ? defaultHeight : minHeight;
        } else {
            targetY = defaultHeight;
        }
        LogWrapper.d("auto scrolling ,currentY = %s, dy = %s", currentY, targetY - currentY);
        scroller.startScroll(0, currentY, 0, targetY - currentY, (int) (1000000 / velocity));
        mainHandler.post(new FlingRunnable(child));
        return true;
    }

    private class FlingRunnable implements Runnable {

        private final View child;

        private FlingRunnable(final View child) {
            this.child = child;
        }

        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                isDragging = false;
                final int currY = scroller.getCurrY();
                LogWrapper.d("auto scrolling ,computeScrollOffset, getCurrY = %s", currY);
                child.setTranslationY(currY);
                if (child.getParent() instanceof CoordinatorLayout) {
                    ((CoordinatorLayout) child.getParent()).dispatchDependentViewsChanged(child);
                }
                mainHandler.post(this);
            }
        }
    }

}