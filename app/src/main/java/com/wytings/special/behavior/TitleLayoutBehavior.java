package com.wytings.special.behavior;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.wytings.special.R;
import com.wytings.special.util.LogWrapper;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */


public class TitleLayoutBehavior extends BaseBehavior<View> {

    private final int blueColor;
    private final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 360);
    private final BroadcastReceiver stopLoading = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            cancelProgress();
        }
    };

    private ImageView imageBack;
    private ImageView progressView;
    private TextView textTitle;

    public TitleLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        blueColor = ContextCompat.getColor(context, R.color.blue_light);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(animation -> progressView.setRotation((Float) animation.getAnimatedValue()));
        LocalBroadcastManager.getInstance(context).registerReceiver(stopLoading, new IntentFilter(ACTION_INFO_STOP_LOADING));
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        prepareParams(child);

        final float progress = getDependencyHeightProgress();
        updateProgress(progress);

        if (progress < 0) {
            return false;
        }

        textTitle.setTextColor(getEvaluateColor(progress < 0.4f ? 0 : progress, Color.TRANSPARENT, Color.WHITE));

        ColorStateList tint = ColorStateList.valueOf(getEvaluateColor(progress, Color.WHITE, blueColor));
        DrawableCompat.setTintList(imageBack.getDrawable(), tint);
        DrawableCompat.setTintList(progressView.getDrawable(), tint);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            imageBack.invalidate();
            progressView.invalidate();
        }


        return false;
    }

    private void updateProgress(float progress) {
        if (progress < 0) {
            if (valueAnimator.isRunning()) {
                return;
            }

            final float rotation = Math.abs(progress * 360);
            progressView.setAlpha(rotation / 360);
            progressView.setRotation(rotation);
            if (isAutoScrolling()) {
                if (progressView.getRotation() > 360) {
                    valueAnimator.start();
                    LocalBroadcastManager.getInstance(progressView.getContext())
                            .sendBroadcast(new Intent(ACTION_INFO_START_LOADING));
                }
            }
        } else {
            if (valueAnimator.isRunning()) {
                progressView.setAlpha(1.0f);
            } else {
                progressView.setAlpha(0.0f);
            }
        }
    }

    private void cancelProgress() {
        if (valueAnimator.isRunning()) {
            valueAnimator.end();
            progressView.setAlpha(0.0f);
        }
    }

    private void prepareParams(View view) {
        if (imageBack == null) {
            imageBack = view.findViewById(R.id.back);
            textTitle = view.findViewById(R.id.title);
            progressView = view.findViewById(R.id.progress);

            imageBack.setImageDrawable(DrawableCompat.wrap(imageBack.getDrawable().mutate()));
            progressView.setImageDrawable(DrawableCompat.wrap((progressView).getDrawable().mutate()));

            progressView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    LogWrapper.d("progressView  onViewDetachedFromWindow");
                    valueAnimator.cancel();
                    progressView.removeOnAttachStateChangeListener(this);
                    LocalBroadcastManager.getInstance(v.getContext()).unregisterReceiver(stopLoading);
                }
            });
        }
    }

}
