package com.wytings.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import com.wytings.special.util.LogWrapper;

import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * Created by Rex.Wei on 2019-09-20
 * <p>
 * modify from {@link android.support.v4.widget.SwipeRefreshLayout}
 *
 * @author 韦玉庭
 */
public class SuperSwipeRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private static final String LOG_TAG = "SwipeRefreshLayout";

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int INVALID_POINTER = -1;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    private static final int DEFAULT_TRIGGER_DISTANCE = 60;

    /**
     * the target of the gesture
     */
    private View mTarget;

    private final RefreshHeaderLayout refreshHeader;

    OnRefreshListener mListener;
    boolean mRefreshing = false;
    private final int mTouchSlop;
    private int mTriggerRefreshDistance;

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private boolean mNestedScrollInProgress;
    /**
     * there is some bad case that mNestedScrollInProgress is true but no nestedScrolling has ever happened.
     */
    private boolean mIsNestedScrollReallyHappened;

    private float mInitialMotionY;
    private float mInitialDownY = -1;
    private float mLastMotionY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    /**
     * for the case that {@link #onLayout(boolean, int, int, int, int)} is called when animation is running.
     */
    private int totalOffset = 0;

    private final DecelerateInterpolator mDecelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };

    private OnChildScrollUpCallback mChildScrollUpCallback;

    private ValueAnimator mToCorrectPositionAnimator = null;
    private ValueAnimator mToStartPositionAnimator = null;

    /**
     * Simple constructor to use when creating a SwipeRefreshLayout from code.
     *
     * @param context
     */
    public SuperSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating SwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public SuperSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        mTriggerRefreshDistance = (int) (DEFAULT_TRIGGER_DISTANCE * metrics.density);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();

        refreshHeader = new RefreshHeaderLayout(context);
        addView(refreshHeader);

    }

    void reset() {
        setTargetTopAndBottomOffset(-getTargetView().getTop());
        mIsBeingDragged = false;
        mNestedScrollInProgress = false;
        mInitialDownY = -1;
        totalOffset = 0;

        LogWrapper.d("reset");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            reset();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    public boolean isAnimationRunning() {
        return isToCorrectPositionRunning() || isToStartPositionRunning();
    }

    public boolean isToCorrectPositionRunning() {
        return mToCorrectPositionAnimator != null && mToCorrectPositionAnimator.isRunning();
    }

    public boolean isToStartPositionRunning() {
        return mToStartPositionAnimator != null && mToStartPositionAnimator.isRunning();
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    public void setOnRefreshListener(@Nullable OnRefreshListener listener) {
        mListener = listener;
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    public void setRefreshing(final boolean refreshing) {
        setRefreshing(refreshing, true);
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing        Whether or not the view should show refresh progress.
     * @param notifySateChanged Whether or not it updates refreshing state.
     */
    public void setRefreshing(final boolean refreshing, final boolean notifySateChanged) {
        if (notifySateChanged && mRefreshing != refreshing) {
            mRefreshing = refreshing;
            if (refreshing) {
                if (mListener != null) {
                    mListener.onRefresh();
                }
                refreshHeader.dispatchRefreshing(this);
            } else {
                refreshHeader.dispatchRefreshCancel(this, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animateOffsetToStartPosition();
                    }
                });
                return;
            }

        }

        if (refreshing) {
            animateOffsetToCorrectPosition();
        } else {
            animateOffsetToStartPosition();
        }
    }

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    private View getTargetView() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != refreshHeader) {
                    mTarget = child;
                    break;
                }
            }
        }

        return mTarget;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }

        final View target = getTargetView();
        final int targetLeft = getPaddingLeft();
        final int targetTop = getPaddingTop() + totalOffset;
        final int targetWidth = width - getPaddingLeft() - getPaddingRight();
        final int targetHeight = height - getPaddingTop() - getPaddingBottom();
        target.layout(targetLeft, targetTop, targetLeft + targetWidth, targetTop + targetHeight);

        refreshHeader.layout(targetLeft, targetTop - refreshHeader.getMeasuredHeight(), targetLeft + targetWidth, targetTop);
        LogWrapper.d("onLayout changed = %s,top = %s", changed, top);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getTargetView() == null) {
            return;
        }

        final int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        final int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        final int targetWidthMeasureSpec = makeMeasureSpec(width, MeasureSpec.EXACTLY);
        final int targetHeightMeasureSpec = makeMeasureSpec(height, MeasureSpec.EXACTLY);
        getTargetView().measure(targetWidthMeasureSpec, targetHeightMeasureSpec);

        final int headerWidthMeasureSpec = makeMeasureSpec(width, MeasureSpec.EXACTLY);
        final int headerHeightMeasureSpec = makeMeasureSpec(mTriggerRefreshDistance, MeasureSpec.EXACTLY);
        refreshHeader.measure(headerWidthMeasureSpec, headerHeightMeasureSpec);
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        final View target = getTargetView();
        if (mChildScrollUpCallback != null) {
            return mChildScrollUpCallback.canChildScrollUp(this, target);
        }
        if (target instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) target, -1);
        }
        return target.canScrollVertically(-1);
    }

    /**
     * Set a callback to override {@link #canChildScrollUp()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.
     *
     * @param callback Callback that should be called when canChildScrollUp() is called.
     */
    public void setOnChildScrollUpCallback(@Nullable OnChildScrollUpCallback callback) {
        mChildScrollUpCallback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        LogWrapper.d("onInterceptTouchEvent , MotionEvent = %s,mIsBeingDragged = %s", MotionEvent.actionToString(ev.getAction()), mIsBeingDragged);
        final int action = ev.getActionMasked();
        int pointerIndex;

        if (!isEnabled() || canChildScrollUp() || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            LogWrapper.d("onInterceptTouchEvent , canChildScrollUp = %s,mRefreshing = %s, mNestedScrollInProgress = %s ",
                         canChildScrollUp(),
                         mRefreshing,
                         mNestedScrollInProgress);
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    LogWrapper.d("onInterceptTouchEvent ACTION_MOVE - pointerIndex = %s", pointerIndex);
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                LogWrapper.d("onInterceptTouchEvent ACTION_MOVE - mIsBeingDragged - %s", mIsBeingDragged);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
            default:
                break;
        }

        return mIsBeingDragged;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        int pointerIndex;
        LogWrapper.d("onTouchEvent , MotionEvent = %s,mIsBeingDragged = %s", MotionEvent.actionToString(ev.getAction()), mIsBeingDragged);
        if (!isEnabled() || canChildScrollUp() || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            LogWrapper.d("onTouchEvent , isEnabled = %s, canChildScrollUp = %s, mNestedScrollInProgress = %s",
                         isEnabled(),
                         canChildScrollUp(),
                         mNestedScrollInProgress);
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragged) {
                    final int offset = (int) (y - mLastMotionY);
                    mLastMotionY = y;
                    return moveSpinner(offset);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointerIndex = ev.getActionIndex();
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG,
                          "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    finishSpinner();
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
            default:
                return true;
        }

        return true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        final View target = getTargetView();
        final boolean isAbsListView = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && target instanceof AbsListView;
        final boolean isNestedScrollingDisabled = target != null && !ViewCompat.isNestedScrollingEnabled(target);
        if (isAbsListView || isNestedScrollingDisabled) {
            // ignore Nope
            return;
        }
        super.requestDisallowInterceptTouchEvent(b);
    }

    /**
     * NestedScrollingParent
     */
    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        LogWrapper.d("onStartNestedScroll top = %s axes = %s", getTargetView().getTop(), (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL));
        return isEnabled() && (getTargetView().getTop() >= 0) && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mNestedScrollInProgress = true;
        LogWrapper.d("onNestedScrollAccepted");
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        LogWrapper.d("onNestedPreScroll, dx = %s, dy = %s", dx, dy);
        // If we are in the middle of consuming, a scroll, then we want to move the spinner back up before allowing the list to scroll
        mIsNestedScrollReallyHappened = true;
        final int targetTop = getTargetView().getTop();
        if (dy > 0 && targetTop > 0) { // up
            if (targetTop - dy >= 0) {
                moveSpinner(-dy);
                consumed[1] = dy;
            } else {
                consumed[1] = -targetTop;
                moveSpinner(-targetTop);
            }
        }

        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public void onNestedScroll(@NonNull final View target,
                               final int dxConsumed,
                               final int dyConsumed,
                               final int dxUnconsumed,
                               final int dyUnconsumed) {
        mIsNestedScrollReallyHappened = true;
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);

        // This is a bit of a hack. Nested scrolling works from the bottom up, and as we are
        // sometimes between two nested scrolling views, we need a way to be able to know when any
        // nested scrolling parent has stopped handling events. We do that by using the
        // 'offset in window 'functionality to see if we have been moved from the event.
        // This is a decent indication of whether we should take over the event stream or not.
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];

        LogWrapper.d("onNestedScroll, dyConsumed = %s, dyUnconsumed = %s, dy = %s, canChildScrollUp = %s",
                     dyConsumed,
                     dyUnconsumed,
                     dy,
                     canChildScrollUp());

        if (dy < 0 && !canChildScrollUp()) {
            moveSpinner(Math.abs(dy));
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        LogWrapper.d("onStopNestedScroll - mRefreshing = %s,mIsBeingDragged = %smNestedScrollInProgress=%s",
                     mRefreshing,
                     mIsBeingDragged,
                     mNestedScrollInProgress);

        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        // Finish the spinner for nested scrolling if we ever consumed any
        // unconsumed nested scroll

        if (mIsNestedScrollReallyHappened) {
            mIsNestedScrollReallyHappened = false;
            if (getTargetView().getTop() > 0) {
                finishSpinner();
            }
        }

        // Dispatch up our nested parent
        stopNestedScroll();
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    private void startDragging(float y) {
        if (mInitialDownY < 0) {
            mInitialDownY = y - mTouchSlop - 1; // 如果事件被中断，则假装偏移
        }

        final float yDiff = y - mInitialDownY;
        if (Math.abs(yDiff) > mTouchSlop && !mIsBeingDragged) {
            if (yDiff > 0) {
                mInitialMotionY = mInitialDownY + mTouchSlop;
            } else if (yDiff < 0) {
                mInitialMotionY = mInitialDownY - mTouchSlop;
            }
            mLastMotionY = mInitialMotionY;
            mIsBeingDragged = true;
            LogWrapper.d("startDragging, y = %s, yDiff = %s", y, yDiff);
        }
    }

    private boolean moveSpinner(int offset) {
        LogWrapper.d("moveSpinner");
        if (offset > 0) {
            setTargetTopAndBottomOffset((int) (offset * 0.6f));
            return true;
        } else if (offset < 0) {
            final View target = getTargetView();
            if (target.getTop() <= 0) {
                return false;
            } else {
                final float expected = target.getTop() + offset;
                if (expected >= 0) {
                    setTargetTopAndBottomOffset(offset);
                } else {
                    setTargetTopAndBottomOffset(-target.getTop());
                }
                return true;
            }
        }
        return false;
    }

    private void finishSpinner() {
        LogWrapper.d("finishSpinner,top = %s, mTriggerRefreshDistance = %s", getTargetView().getTop(), mTriggerRefreshDistance);

        if (getTargetView().getTop() > mTriggerRefreshDistance) {
            setRefreshing(true, true);
        } else {
            setRefreshing(false, false);
        }
    }

    private void animateOffsetToCorrectPosition() {
        final int startOffset = getTargetView().getTop();
        LogWrapper.d("animateOffsetToCorrectPosition, startOffset = %s", startOffset);

        if (mToCorrectPositionAnimator != null) {
            mToCorrectPositionAnimator.cancel();
        }

        final ValueAnimator valueAnimator = ValueAnimator.ofInt(startOffset, mTriggerRefreshDistance);
        valueAnimator.setInterpolator(mDecelerateInterpolator);
        valueAnimator.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final int current = (int) animation.getAnimatedValue();
                final int offset = current - getTargetView().getTop();
                setTargetTopAndBottomOffset(offset);
            }
        });

        valueAnimator.start();
        mToCorrectPositionAnimator = valueAnimator;
    }

    private void animateOffsetToStartPosition() {
        final int startOffset = getTargetView().getTop();
        LogWrapper.d("animateOffsetToStartPosition, startOffset = %s", startOffset);

        if (mToStartPositionAnimator != null) {
            mToStartPositionAnimator.cancel();
        }

        final ValueAnimator valueAnimator = ValueAnimator.ofInt(startOffset, 0);
        valueAnimator.setInterpolator(mDecelerateInterpolator);
        valueAnimator.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                super.onAnimationEnd(animation);
                reset();
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final int current = (int) animation.getAnimatedValue();
                final int offset = current - getTargetView().getTop();
                setTargetTopAndBottomOffset(offset);
            }
        });

        valueAnimator.start();
        mToStartPositionAnimator = valueAnimator;
    }

    void setTargetTopAndBottomOffset(int offset) {
        final View target = getTargetView();
        if (target != null) {
            LogWrapper.d("setTargetTopAndBottomOffset , offset = %s, top = %s", offset, target.getTop());

            if (offset > 0 && target.getTop() > mTriggerRefreshDistance) {
                if (target.getTop() > mTriggerRefreshDistance * 2) {
                    offset *= 0.2f;
                } else {
                    offset *= 0.5f;
                }
            }

            if (offset != 0) {
                ViewCompat.offsetTopAndBottom(target, offset);
                refreshHeader.dispatchTopAndBottomOffset(this, offset);
                totalOffset += offset;
            }
        } else {
            LogWrapper.d("setTargetTopDistance ,mTarget is null, offset = %s", offset);
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        /**
         * Called when a swipe gesture triggers a refresh.
         */
        void onRefresh();
    }

    /**
     * Classes that wish to override {@link #canChildScrollUp()} method
     * behavior should implement this interface.
     */
    public interface OnChildScrollUpCallback {
        /**
         * Callback that will be called when {@link #canChildScrollUp()} method
         * is called to allow the implementer to override its behavior.
         *
         * @param parent SwipeRefreshLayout that this callback is overriding.
         * @param child  The child view of SwipeRefreshLayout.
         *
         * @return Whether it is possible for the child view of parent layout to scroll up.
         */
        boolean canChildScrollUp(@NonNull SuperSwipeRefreshLayout parent, @Nullable View child);
    }
}

