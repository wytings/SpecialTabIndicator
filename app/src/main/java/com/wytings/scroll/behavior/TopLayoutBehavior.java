package com.wytings.scroll.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.wytings.special.R;
import com.wytings.special.util.LogWrapper;

/**
 * Created by Rex.Wei on 2019-08-06
 *
 * @author weiyuting
 */
public class TopLayoutBehavior extends AbsBehavior<View> {

    private final int minHeight;
    private final int defaultHeight;
    private final int maxHeight;

    public TopLayoutBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final int[] minDefaultMax = parseMinDefaultMax(context, attrs);
        minHeight = dp(context, minDefaultMax[0]);
        defaultHeight = dp(context, minDefaultMax[1]);
        maxHeight = dp(context, minDefaultMax[2]);
    }

    @Override
    public boolean layoutDependsOn(final CoordinatorLayout parent, final View child, final View dependency) {
        if (dependency != null && dependency.getId() == R.id.behavior_content_layout) {
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        final int dependencyY = (int) dependency.getTranslationY();
        if (defaultHeight <= dependencyY && dependencyY <= maxHeight) {
            setViewParamsHeight(child, dependencyY);
            child.setTranslationY(0);
            dispatchViewChangedListener(child);
            LogWrapper.d("search onDependentViewChanged height dependencyY = %s, height = %s", dependencyY, child.getHeight());
        } else {
            setViewParamsHeight(child, defaultHeight);
            child.setTranslationY(-(defaultHeight - dependencyY));
            dispatchViewChangedListener(child);
            LogWrapper.d("search onDependentViewChanged translateY dependencyY = %s , height = %s", dependencyY, child.getHeight());
        }
        return true;
    }

    private void dispatchViewChangedListener(final View child) {
        final Object listener = child.getTag(R.id.behavior_view_changed_listener);
        if (listener instanceof OnViewChangedListener) {
            ((OnViewChangedListener) listener).onChanged();
        }
    }

}
