package com.wytings.special;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import com.wytings.R;
import com.wytings.adapter.MyAdapter;
import com.wytings.special.behavior.TopIndicatorBehavior;
import com.wytings.special.behavior.TitleLayoutBehavior;
import com.wytings.special.behavior.BottomPagerViewBehavior;
import com.wytings.special.util.LogWrapper;
import com.wytings.special.widget.ScalableIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.wytings.special.behavior.BaseBehavior.ACTION_INFO_START_LOADING;
import static com.wytings.special.behavior.BaseBehavior.ACTION_INFO_STOP_LOADING;

/**
 * Created by rex.wei on 2018/02/26 12:00.
 * <p> we have three behaviors attached to this activity.
 * {@link TopIndicatorBehavior}
 * {@link TitleLayoutBehavior}
 * {@link BottomPagerViewBehavior}
 *
 * @author wytings@gmail.com
 */

public class SpecialActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private final BroadcastReceiver loading = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int currentPosition = viewPager.getCurrentItem();
            if (viewPager.getAdapter() == null) {
                LogWrapper.e("ViewPager doesn't have Adapter");
                return;
            }

            LogWrapper.d("start to loading top in position = %s", currentPosition);
            if (0 <= currentPosition && currentPosition <= viewPager.getAdapter().getCount()) {
                viewPager.postDelayed(() ->
                                LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(new Intent(ACTION_INFO_STOP_LOADING))
                        , 2000);
            } else {
                LogWrapper.w("current position = %s is invalid", currentPosition);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special);
        transparentStatusBar(this);

        viewPager = findViewById(R.id.view_pager);
        LocalBroadcastManager.getInstance(this).registerReceiver(loading, new IntentFilter(ACTION_INFO_START_LOADING));

        initHeaderLayout();
        initPagerAnimator();

    }

    private void transparentStatusBar(Activity activity) {
        final Window window = activity.getWindow();
        final View decorView = window.getDecorView();

        final int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initPagerAnimator() {
        final View headerAnimator = findViewById(R.id.image_view_animator);
        viewPager.setAdapter(new Adapter(createRecyclerViewList()));

        final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                LogWrapper.d("onFling, e1.x=%s, e2.x=%s, velocityX = %s, velocityY = %s", e1.getX(), e2.getX(), velocityX, velocityY);
                if (viewPager.getAdapter() == null) {
                    return super.onFling(e1, e2, velocityX, velocityY);
                }

                int currentPosition = viewPager.getCurrentItem();
                if (velocityX > 0) {
                    if (currentPosition - 1 >= 0) {
                        viewPager.setCurrentItem(currentPosition - 1);
                    }
                } else {
                    if (currentPosition + 1 < viewPager.getAdapter().getCount()) {
                        viewPager.setCurrentItem(currentPosition + 1);
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        headerAnimator.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        final ViewAnimator viewAnimator = (ViewAnimator) headerAnimator;
        viewAnimator.setDisplayedChild(0);
        viewAnimator.setInAnimation(getApplication(), R.anim.alpha_background_in);
        viewAnimator.setOutAnimation(getApplication(), R.anim.alpha_background_out);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            private int previousPosition = -1;
            private int selectedPosition = -1;

            @Override
            public void onPageSelected(int position) {
                previousPosition = selectedPosition;
                selectedPosition = position;
                final View previous = viewAnimator.getChildAt(previousPosition);
                final View selected = viewAnimator.getChildAt(selectedPosition);
                final int animateIn;
                final int animateOut;
                if (previousPosition < selectedPosition) {
                    animateIn = R.anim.left_in;
                    animateOut = R.anim.left_out;
                } else {
                    animateIn = R.anim.right_in;
                    animateOut = R.anim.right_out;
                }
                viewAnimator.setDisplayedChild(position);
                animateView(selected, animateIn);
                animateView(previous, animateOut);
            }
        });
        ScalableIndicator indicator = findViewById(R.id.tab_indicator);
        indicator.attachViewPager(viewPager);
    }

    private void animateView(View view, @AnimRes int animRes) {
        if (view != null) {
            View icon = view.findViewById(R.id.image_icon);
            if (icon != null) {
                icon.startAnimation(AnimationUtils.loadAnimation(this, animRes));
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initHeaderLayout() {
        setUpSingleHeader(R.id.a1, R.drawable.snowflake, "#008EFF", "#193BC3");
        setUpSingleHeader(R.id.a2, R.drawable.snowflake, "#5739EE", "#4225AA");
        setUpSingleHeader(R.id.a3, R.drawable.snowflake, "#1EC9BC", "#005EB4");
        setUpSingleHeader(R.id.a4, R.drawable.snowflake, "#C467F7", "#6919EB");
        setUpSingleHeader(R.id.a5, R.drawable.snowflake, "#FE6B4B", "#AB2525");
        View back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
        back.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.3f);
            } else {
                v.setAlpha(1.0f);
            }
            return false;
        });
    }

    private void setUpSingleHeader(@IdRes int idRes, @DrawableRes int drawableRes, String startColor, String endColor) {
        View view = findViewById(idRes);
        final int start = Color.parseColor(startColor);
        final int end = Color.parseColor(endColor);
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end});
        view.setBackground(gradient);
        ImageView imageView = view.findViewById(R.id.image_icon);
        imageView.setImageResource(drawableRes);
        DrawableCompat.setTintList(DrawableCompat.wrap(imageView.getDrawable()), ColorStateList.valueOf(Color.parseColor(startColor)));


    }


    public List<View> createRecyclerViewList() {
        ArrayList<View> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MyAdapter(this,50));
            recyclerView.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background_color));
            list.add(recyclerView);
        }
        return list;
    }

    private class Adapter extends PagerAdapter {

        private final List<View> viewList;

        private Adapter(List<View> viewList) {
            this.viewList = Collections.unmodifiableList(viewList);
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View content = viewList.get(position);
            container.addView(content);
            return content;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Alphabet";
                case 1:
                    return "Google";
                case 2:
                    return "Android";
                case 3:
                    return "Flutter";
                case 4:
                    return "TensorFlow";
                default:
                    return "Title " + position;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loading);
    }

}
