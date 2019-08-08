package com.wytings.special.behavior;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.wytings.R;
import com.wytings.special.widget.ScalableIndicator;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */


public class TopIndicatorBehavior extends BaseBehavior<View> {

    private View maskView;
    private ScalableIndicator indicator;
    private final int selectedBlue;
    private final int unselectedWhite50;
    private final int unselectedWhite70;
    private float initBaseScale;
    private final int indicatorHeight;

    public TopIndicatorBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        selectedBlue = ContextCompat.getColor(context, R.color.blue_light);
        unselectedWhite50 = ContextCompat.getColor(context, R.color.white_color_50);
        unselectedWhite70 = ContextCompat.getColor(context, R.color.white_color_70);
        indicatorHeight = dp(40);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        final int dependencyHeight = getDependencyHeight();
        child.setTranslationY(dependencyHeight - indicatorHeight);

        if (maskView == null) {
            maskView = dependency.findViewById(R.id.mask);
            indicator = (ScalableIndicator) child;
            initBaseScale = indicator.getBaseScale();
        }

        float progress = getDependencyHeightProgress();

        if (progress < 0) {
            progress = 0;
        }
        changeIndicator(progress);
        changeBackgroundColor(maskView, progress, lightColor, darkColor);
        return false;
    }

    private void changeIndicator(float progress) {
        final int selected = getEvaluateColor(progress, Color.WHITE, selectedBlue);
        final int unselected = getEvaluateColor(progress, unselectedWhite70, unselectedWhite50);
        final float scale = initBaseScale * (1 - progress);

        indicator.setBaseScale(scale);
        indicator.setIndicatorColor(selected);
        indicator.setSelectedTextColor(selected);
        indicator.setUnselectedTextColor(unselected);

        indicator.invalidateImmediately();

    }


}