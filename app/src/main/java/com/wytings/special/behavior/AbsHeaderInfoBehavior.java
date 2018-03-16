package com.wytings.special.behavior;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.wytings.special.R;
import com.wytings.special.util.ViewUtils;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */


public abstract class AbsHeaderInfoBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private final Context context;
    final int lightColor;
    final int darkColor;
    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    final int dependencyCollapseHeight, dependencyInitHeight;

    private CoordinatorLayout coordinatorLayout;
    View dependentView;


    AbsHeaderInfoBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        lightColor = Color.TRANSPARENT;
        darkColor = ContextCompat.getColor(context, R.color.dark_background_color);
        dependencyInitHeight = dp(220);
        dependencyCollapseHeight = dp(68 + 40);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        if (dependency != null && dependency.getId() == R.id.header_layout) {
            dependentView = dependency;
            this.coordinatorLayout = parent;
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    void changeBackgroundColor(View view, float progress, int lightColor, int darkColor) {
        if (progress < 0 || view == null) {
            return;
        }
        view.setBackgroundColor(getEvaluateColor(progress, lightColor, darkColor));
    }

    int getEvaluateColor(float progress, int lightColor, int darkColor) {
        return (int) argbEvaluator.evaluate(progress, lightColor, darkColor);
    }

    int dp(int dp) {
        return (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    void setViewParamsHeight(View view, int height) {
        ViewUtils.setViewLayoutParamsHeight(view, height);
    }

    int getDependencyHeight() {
        return ViewUtils.getViewLayoutParamsHeight(dependentView);
    }

    float getDependencyHeightProgress() {
        final int gap = getDependencyHeight() - dependencyCollapseHeight;
        final int base = dependencyInitHeight - dependencyCollapseHeight;
        return 1.0f - 1.0f * gap / base;
    }

    boolean isAutoScrolling() {
        return coordinatorLayout != null
                && coordinatorLayout.getTag() instanceof StatusBehavior
                && ((StatusBehavior) coordinatorLayout.getTag()).isAutoScrolling;
    }

    static class StatusBehavior {
        boolean isAutoScrolling;
    }

}
