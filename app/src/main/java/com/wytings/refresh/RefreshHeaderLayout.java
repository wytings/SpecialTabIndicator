package com.wytings.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.wytings.R;
import com.wytings.special.util.LogWrapper;

/**
 * Created by rex on 2019-09-23.
 *
 * @author wytings@gmail.com
 */
public class RefreshHeaderLayout extends FrameLayout {

    private final LottieAnimationView lottieAnimationView;
    private ValueAnimator mRepeatProgressAnimator;
    private ValueAnimator mFinishAnimator;

    private static final int LOADING_FRAME_START = 0;
    private static final int LOADING_FRAME_END = 24;

    public RefreshHeaderLayout(@NonNull Context context) {
        this(context, null);
    }

    public RefreshHeaderLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshHeaderLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.refresh_header_layout, this);
        lottieAnimationView = findViewById(R.id.animation_view);

    }

    public void dispatchTopAndBottomOffset(SuperSwipeRefreshLayout refreshLayout, int offset) {
        printLottieInfo();
        ViewCompat.offsetTopAndBottom(this, offset);
        final int visibleHeight = getBottom();
        final int totalHeight = getMeasuredHeight();
        float visiblePercent = 1.0f * visibleHeight / totalHeight;
        visiblePercent = visiblePercent > 1 ? 1 : visiblePercent;
        int frame = (int) (visiblePercent * lottieAnimationView.getMaxFrame());

        final boolean isRepeatRunning = mRepeatProgressAnimator != null && mRepeatProgressAnimator.isRunning();

        if (isRepeatRunning || refreshLayout.isAnimationRunning()) {
            if (refreshLayout.isToStartPositionRunning()) {
                lottieAnimationView.setAlpha(visiblePercent);
            }
            return;
        }

        lottieAnimationView.setFrame(frame > LOADING_FRAME_END ? LOADING_FRAME_END : frame);
        lottieAnimationView.setAlpha(visiblePercent);
        LogWrapper.d("lottieAnimationView - frame = %s ", lottieAnimationView.getFrame());

    }

    private void printLottieInfo() {
        if (lottieAnimationView.getTag() == null) {
            lottieAnimationView.setTag(Object.class);
            LogWrapper.d("lottieAnimationView - minFrame = %s, maxFrame = %s", lottieAnimationView.getMinFrame(), lottieAnimationView.getMaxFrame());
        }
    }

    public void dispatchRefreshing(SuperSwipeRefreshLayout refreshLayout) {

        if (mRepeatProgressAnimator != null) {
            mRepeatProgressAnimator.cancel();
        }

        lottieAnimationView.setAlpha(1.0f);
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(LOADING_FRAME_START, LOADING_FRAME_END);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final int current = (int) animation.getAnimatedValue();
                lottieAnimationView.setFrame(current);
                LogWrapper.d("lottieAnimationView - dispatchRefreshing alpha = %s", lottieAnimationView.getAlpha());

            }
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setDuration(750);
        valueAnimator.start();
        mRepeatProgressAnimator = valueAnimator;
    }

    public void dispatchRefreshCancel(SuperSwipeRefreshLayout refreshLayout, Animator.AnimatorListener listener) {

        if (mRepeatProgressAnimator != null) {
            mRepeatProgressAnimator.cancel();
        }

        if (mFinishAnimator != null) {
            mFinishAnimator.cancel();
        }

        final ValueAnimator valueAnimator = ValueAnimator.ofInt(lottieAnimationView.getFrame(), (int) lottieAnimationView.getMaxFrame());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final int current = (int) animation.getAnimatedValue();
                lottieAnimationView.setFrame(current);
            }
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
            }
        });
        valueAnimator.setDuration(750);
        valueAnimator.start();
        mFinishAnimator = valueAnimator;
    }
}
