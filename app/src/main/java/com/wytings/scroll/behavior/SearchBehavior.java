package com.wytings.scroll.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.wytings.special.R;
import com.wytings.special.util.ContextUtils;

/**
 * Created by Rex.Wei on 2019-08-06
 *
 * @author weiyuting
 */
public class SearchBehavior extends CoordinatorLayout.Behavior<View> {

    private final int minHeight;
    private final int defaultHeight;
    private final int maxHeight;

    public SearchBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final int[] minDefaultMax = ContextUtils.parseMinDefaultMax(context, attrs);
        minHeight = ContextUtils.dp(context, minDefaultMax[0]);
        defaultHeight = ContextUtils.dp(context, minDefaultMax[1]);
        maxHeight = ContextUtils.dp(context, minDefaultMax[2]);
    }

    @Override
    public boolean layoutDependsOn(final CoordinatorLayout parent, final View child, final View dependency) {
        if (dependency != null && dependency.getId() == R.id.swipe_refresh_layout) {
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        final int dependencyY = (int) dependency.getTranslationY();
        if (defaultHeight < dependencyY && dependencyY <= maxHeight) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            params.height = dependencyY;
            child.setLayoutParams(params);
        } else {
            child.setTranslationY(-(defaultHeight - dependencyY));
        }
        return true;
    }

}
