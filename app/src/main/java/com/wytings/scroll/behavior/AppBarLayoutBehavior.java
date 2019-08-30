package com.wytings.scroll.behavior;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.wytings.special.util.LogWrapper;

public class AppBarLayoutBehavior extends AppBarLayout.Behavior {

    private static final String COLLAPSE = "collapse";

    private boolean isInFlingMode = false;
    private Boolean willExpanded = null;
    private ValueAnimator valueAnimator;
    private int collapseOffset;

    public AppBarLayoutBehavior() {
    }

    public AppBarLayoutBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final int[] arr = new int[]{android.R.attr.tag};
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, arr);
        final int tagIndex = typedArray.getIndex(0);
        final String tag = typedArray.getString(tagIndex);
        typedArray.recycle();
        if (!TextUtils.isEmpty(tag)) {
            String[] keyValues = tag.split(",");
            for (String keyValue : keyValues) {
                String[] kv = keyValue.split(":");
                if (kv.length == 2) {
                    if (COLLAPSE.equals(kv[0])) {
                        final int collapseDp = Integer.parseInt(kv[1]);
                        collapseOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, collapseDp, context.getResources().getDisplayMetrics());
                    }
                }

            }
        }
    }

    @Override
    public boolean onLayoutChild(final CoordinatorLayout parent, final AppBarLayout abl, final int layoutDirection) {
        LogWrapper.d("AppBarLayoutBehavior onLayoutChild");
        return super.onLayoutChild(parent, abl, layoutDirection);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        LogWrapper.d("AppBarLayoutBehavior onStartNestedScroll type = %s", type);
        if (type == ViewCompat.TYPE_TOUCH) {
            isInFlingMode = false;
            willExpanded = null;
        }
        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed, int type) {
        LogWrapper.d("AppBarLayoutBehavior onNestedPreScroll type = %s, dy = %s ", type, dy);
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        if (type == ViewCompat.TYPE_TOUCH) {
            if (dy > 0) {
                willExpanded = false;
            } else if (dy < 0) {
                willExpanded = true;
            }
        }
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
        LogWrapper.d("AppBarLayoutBehavior onStopNestedScroll ,type = %s,  isInFlingMode = %s,expanded = %s", type, isInFlingMode, willExpanded);

        if (willExpanded == null) {
            LogWrapper.d("AppBarLayoutBehavior ignore click");
            return;
        }

        if (isInFlingMode) {
            if (type == ViewCompat.TYPE_NON_TOUCH) {
                dispatchAnimation(abl, willExpanded);
            }
        } else {
            dispatchAnimation(abl, willExpanded);
        }
    }


    private void dispatchAnimation(final AppBarLayout appBarLayout, final boolean expanded) {
        final int defaultOffset = 0;
        final int currentOffset = getTopAndBottomOffset();

        if (currentOffset == collapseOffset || currentOffset == defaultOffset) {
            LogWrapper.d("AppBarLayoutBehavior ignore animation");
            return;
        }
        // animateAppBar(coordinatorLayout, abl, expanded);
        appBarLayout.setExpanded(expanded);
    }

    private void animateAppBar(final CoordinatorLayout coordinatorLayout, final AppBarLayout appBarLayout, boolean isExpand) {
        final int defaultOffset = 0;
        final int currentOffset = getTopAndBottomOffset();

        if (isExpand) {
            valueAnimator = ValueAnimator.ofInt(currentOffset, defaultOffset);
        } else {
            valueAnimator = ValueAnimator.ofInt(currentOffset, collapseOffset);
        }

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int value = (int) animation.getAnimatedValue();
                LogWrapper.d("AppBarLayoutBehavior onAnimationUpdate ,value = %s", value);
                setTopAndBottomOffset(value);
                coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
            }
        });
        valueAnimator.start();

    }

}