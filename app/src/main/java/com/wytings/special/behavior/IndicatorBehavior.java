package com.wytings.special.behavior;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.wytings.special.R;
import com.wytings.special.util.G;
import com.wytings.special.widget.CenterScaleIndicator;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */


public class IndicatorBehavior extends AbsHeaderInfoBehavior<View> {

    private View maskView;
    private CenterScaleIndicator indicator;
    private final int selectedBlue;
    private final int unselectedWhite50;
    private final int unselectedWhite70;
    private float initBaseScale;

    public IndicatorBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        selectedBlue = ContextCompat.getColor(context, R.color.blue_light);
        unselectedWhite50 = ContextCompat.getColor(context, R.color.white_color_50);
        unselectedWhite70 = ContextCompat.getColor(context, R.color.white_color_70);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        final int dependencyHeight = getDependencyHeight();
        child.setTranslationY(dependencyHeight - dp(40));

        if (maskView == null) {
            maskView = dependency.findViewById(R.id.mask);
            indicator = (CenterScaleIndicator) child;
            initBaseScale = indicator.getBaseScale();
        }

        final float progress = getDependencyHeightProgress();
        G.d("indicator progress = %s, dependencyHeight = %s", progress, dependencyHeight);

        if (progress < 0) {
            return false;
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

        indicator.forceInvalidate();

    }


}
