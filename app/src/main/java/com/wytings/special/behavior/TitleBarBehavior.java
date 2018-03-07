package com.wytings.special.behavior;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wytings.special.R;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */


public class TitleBarBehavior extends AbsHeaderInfoBehavior<View> {

    private ImageView imageBack;
    private TextView textTitle;
    private final int blueColor;
    private Drawable wrappedDrawable;

    public TitleBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        blueColor = ContextCompat.getColor(context, R.color.blue_light);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        final float progress = getDependencyHeightProgress();

        if (progress < 0) {
            return false;
        }

        prepareParams(child);

        textTitle.setTextColor(getEvaluateColor(progress < 0.4f ? 0 : progress, Color.TRANSPARENT, Color.WHITE));

        DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(getEvaluateColor(progress, Color.WHITE, blueColor)));

        return false;
    }

    private void prepareParams(View view) {
        if (imageBack == null) {
            imageBack = view.findViewById(R.id.back);
            textTitle = view.findViewById(R.id.title);
            wrappedDrawable = DrawableCompat.wrap(imageBack.getDrawable()).mutate();
        }
    }

}
