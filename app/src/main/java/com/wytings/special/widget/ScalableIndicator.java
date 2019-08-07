package com.wytings.special.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wytings.special.R;
import com.wytings.special.util.LogWrapper;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.widget.LinearLayout.SHOW_DIVIDER_BEGINNING;
import static android.widget.LinearLayout.SHOW_DIVIDER_END;
import static android.widget.LinearLayout.SHOW_DIVIDER_MIDDLE;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */

public class ScalableIndicator extends HorizontalScrollView {

    private static final int STYLE_MATCH_TAB = 0;
    private static final int STYLE_USER_DEFINED = 1;


    private int tabCount;
    private ViewPager pager;
    private final Paint paint;
    private int dividerWidth = 20;
    private float tabTextSize = 15;
    private float baseScale = 0.4f;
    private int indicatorHeight = 2;
    private int indicatorWidth = 20;
    private int currentPosition = 0;
    private int tabBottomPadding = 5;
    private Animator runningAnimator;
    private int previousPosition = -1;
    private boolean isScaleEnabled = true;
    private int indicatorColor = 0xff499EF0;
    private final LinearLayout tabsContainer;
    private int lineStyle = STYLE_USER_DEFINED;
    private int selectedTextColor = 0xff499EF0;
    private final Rect visibleRect = new Rect();
    private int unselectedTextColor = 0x80FFFFFF;
    private float currentPositionOffsetRatio = 0f;
    private final ViewPager.OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffsetRatio, int positionOffsetPixels) {
            currentPosition = position;
            currentPositionOffsetRatio = positionOffsetRatio;
            invalidate();
        }

        @Override
        public void onPageSelected(int position) {
            TextView currentTab = (TextView) tabsContainer.getChildAt(position);
            currentTab.setTextColor(selectedTextColor);
            if (previousPosition >= 0) {
                TextView lastTab = (TextView) tabsContainer.getChildAt(previousPosition);
                lastTab.setTextColor(unselectedTextColor);
            }
            smoothScrollBy(getScrollXDistance(position), 0);
            animateTabTitle(position);
            previousPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == SCROLL_STATE_DRAGGING) {
                if (runningAnimator != null) {
                    runningAnimator.end();
                }
            }
        }


    };


    public ScalableIndicator(Context context) {
        this(context, null);
    }

    public ScalableIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScalableIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
        indicatorWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorWidth, dm);
        tabBottomPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabBottomPadding, dm);
        dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
        tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

        parseIndicatorAttrs(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        paint.setColor(indicatorColor);

        tabsContainer = createTabContainer(context);
        addView(tabsContainer);

        if (isInEditMode()) {
            tabsContainer.removeAllViews();
            tabCount = 2;
            addTab(0, createTextTab(0, "selected"));
            addTab(1, createTextTab(1, "unselected"));

        }
    }

    private void parseIndicatorAttrs(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScalableIndicator);
        indicatorColor = typedArray.getColor(R.styleable.ScalableIndicator_scalable_line_color, indicatorColor);
        indicatorHeight = typedArray.getDimensionPixelSize(R.styleable.ScalableIndicator_scalable_line_height, indicatorHeight);
        indicatorWidth = typedArray.getDimensionPixelSize(R.styleable.ScalableIndicator_scalable_line_width, indicatorWidth);
        tabBottomPadding = typedArray.getDimensionPixelSize(R.styleable.ScalableIndicator_scalable_tab_bottom_padding, tabBottomPadding);
        dividerWidth = typedArray.getDimensionPixelSize(R.styleable.ScalableIndicator_scalable_divider_width, dividerWidth);
        baseScale = typedArray.getFloat(R.styleable.ScalableIndicator_scalable_base_scale, baseScale);
        isScaleEnabled = typedArray.getBoolean(R.styleable.ScalableIndicator_scalable_scale_enabled, isScaleEnabled);
        lineStyle = typedArray.getInt(R.styleable.ScalableIndicator_scalable_line_style, lineStyle);

        tabTextSize = typedArray.getDimensionPixelSize(R.styleable.ScalableIndicator_scalable_text_size, (int) tabTextSize);
        unselectedTextColor = typedArray.getColor(R.styleable.ScalableIndicator_scalable_text_unselected_color, unselectedTextColor);
        selectedTextColor = typedArray.getColor(R.styleable.ScalableIndicator_scalable_text_selected_color, selectedTextColor);

        typedArray.recycle();
    }

    public void setBaseScale(float baseScale) {
        this.baseScale = baseScale;
    }

    public float getBaseScale() {
        return baseScale;
    }

    public void setSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
    }

    public void setUnselectedTextColor(int unselectedTextColor) {
        this.unselectedTextColor = unselectedTextColor;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        this.paint.setColor(indicatorColor);
    }

    private LinearLayout createTabContainer(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        LayoutParams linearLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        container.setLayoutParams(linearLayoutParams);

        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.getPaint().setColor(Color.TRANSPARENT);
        shapeDrawable.setIntrinsicWidth(dividerWidth);

        container.setDividerDrawable(shapeDrawable);
        container.setShowDividers(SHOW_DIVIDER_BEGINNING | SHOW_DIVIDER_MIDDLE | SHOW_DIVIDER_END);

        return container;
    }


    public void invalidateImmediately() {
        final int count = tabsContainer.getChildCount();
        if (count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            TextView textView = (TextView) tabsContainer.getChildAt(i);
            updateTextStyle(textView, i);
        }
        invalidate();
        LogWrapper.d("invalidate immediately , current position = %s", currentPosition);
    }

    private void animateTabTitle(int position) {
        final TextView previousTextView = (TextView) tabsContainer.getChildAt(previousPosition);
        final TextView currentTextView = (TextView) tabsContainer.getChildAt(position);
        ValueAnimator selectedToUnSelected = ValueAnimator.ofInt(selectedTextColor, unselectedTextColor);
        selectedToUnSelected.setEvaluator(new ArgbEvaluator());
        selectedToUnSelected.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            if (previousTextView != null) {
                previousTextView.setTextColor(color);
            }
        });

        ValueAnimator unselectedToSelected = ValueAnimator.ofInt(unselectedTextColor, selectedTextColor);
        unselectedToSelected.setEvaluator(new ArgbEvaluator());
        unselectedToSelected.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            if (currentTextView != null) {
                currentTextView.setTextColor(color);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.setDuration(200);
        AnimatorSet.Builder builder = set.play(selectedToUnSelected).with(unselectedToSelected);

        if (isScaleEnabled && Float.compare(baseScale, 0) != 0) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, baseScale);
            valueAnimator.addUpdateListener(animation -> {
                Float size = (Float) animation.getAnimatedValue();
                if (previousTextView != null) {
                    previousTextView.setScaleX(1 + baseScale - size);
                    previousTextView.setScaleY(1 + baseScale - size);
                }

                if (currentTextView != null) {
                    currentTextView.setScaleX(1 + size);
                    currentTextView.setScaleY(1 + size);
                }
            });
            builder.with(valueAnimator);
        }

        set.start();
        runningAnimator = set;
    }

    public void attachViewPager(@NonNull ViewPager pager) {
        this.pager = pager;

        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        pager.removeOnPageChangeListener(pageListener);
        pager.addOnPageChangeListener(pageListener);

        notifyPagerChanged();
    }

    public void notifyPagerChanged() {

        if (pager.getAdapter() == null) {
            return;
        }

        tabsContainer.removeAllViews();
        tabCount = pager.getAdapter().getCount();
        currentPosition = pager.getCurrentItem();
        for (int i = 0; i < tabCount; i++) {
            addTab(i, createTextTab(i, pager.getAdapter().getPageTitle(i)));
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                previousPosition = currentPosition = pager.getCurrentItem();
                scrollTo(getScrollXDistance(currentPosition), 0);
            }
        });
    }


    private TextView createTextTab(int position, CharSequence title) {
        TextView tab = new TextView(getContext());
        tab.setText(title);
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
        updateTextStyle(tab, position);
        return tab;
    }

    private void updateTextStyle(TextView textView, int position) {
        final int selectedPosition;
        if (pager != null) {
            selectedPosition = pager.getCurrentItem();
        } else {
            selectedPosition = 0;
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
        if (selectedPosition == position) {
            textView.setTextColor(selectedTextColor);
            if (isScaleEnabled) {
                if (runningAnimator != null && runningAnimator.isRunning()) {
                    return;
                }
                textView.setScaleX(1 + baseScale);
                textView.setScaleY(1 + baseScale);
            }
        } else {
            textView.setTextColor(unselectedTextColor);
        }
    }

    private void addTab(final int position, View tab) {
        tab.setFocusable(true);
        tab.setOnClickListener(v -> pager.setCurrentItem(position));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, tabBottomPadding);
        tabsContainer.addView(tab, position, params);
    }

    private int getScrollXDistance(int position) {
        View child = tabsContainer.getChildAt(position);
        if (null == child) {
            return 0;
        }
        visibleRect.setEmpty();
        child.getGlobalVisibleRect(visibleRect);
        int containerVisibleCenter = (getRight() + getLeft()) >> 1;
        int tabVisibleCenter = visibleRect.centerX();

        return tabVisibleCenter - containerVisibleCenter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (tabsContainer.getChildCount() <= currentPosition || currentPosition < 0) {
            return;
        }

        final int height = getHeight();
        View currentTab = tabsContainer.getChildAt(currentPosition);
        final boolean isMatchTab = lineStyle == STYLE_MATCH_TAB;

        float currentHalfWidth = indicatorWidth / 2;
        float currentCenter = (currentTab.getRight() + currentTab.getLeft()) / 2;
        float lineLeft = isMatchTab ? currentTab.getLeft() : currentCenter - currentHalfWidth;
        float lineRight = isMatchTab ? currentTab.getRight() : currentCenter + currentHalfWidth;

        View nextTab = tabsContainer.getChildAt(currentPosition + 1);
        if (nextTab != null) {
            float nextCenter = (nextTab.getRight() + nextTab.getLeft()) / 2;
            final float nextTabLeft = isMatchTab ? nextTab.getLeft() : nextCenter - currentHalfWidth;
            final float nextTabRight = isMatchTab ? nextTab.getRight() : nextCenter + currentHalfWidth;

            lineLeft = currentPositionOffsetRatio * nextTabLeft + (1f - currentPositionOffsetRatio) * lineLeft;
            lineRight = currentPositionOffsetRatio * nextTabRight + (1f - currentPositionOffsetRatio) * lineRight;
        }

        canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, paint);
    }

}