package com.wytings.special;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.wytings.special.behavior.IndicatorBehavior;
import com.wytings.special.behavior.TitleBarBehavior;
import com.wytings.special.behavior.ViewPagerBehavior;
import com.wytings.special.util.Broadcaster;
import com.wytings.special.util.Event;
import com.wytings.special.util.G;
import com.wytings.special.util.ViewUtils;
import com.wytings.special.widget.CenterScaleIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rex.wei on 2018/02/26 12:00.
 * <p> we have three behaviors attached to this activity.
 * {@link IndicatorBehavior}
 * {@link TitleBarBehavior}
 * {@link ViewPagerBehavior}
 *
 * @author wytings@gmail.com
 */

public class MainActivity extends AppCompatActivity {

    private Runnable loadingRunnable;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.transparentStatusBar(this);
        viewPager = findViewById(R.id.view_pager);
        loadingRunnable = () -> {
            final int currentPosition = viewPager.getCurrentItem();
            if (viewPager.getAdapter() == null) {
                G.e("ViewPager doesn't have Adapter");
                return;
            }

            G.d("start to loading top in position = %s", currentPosition);
            if (0 <= currentPosition && currentPosition <= viewPager.getAdapter().getCount()) {
                viewPager.postDelayed(() -> Broadcaster.getInstance().notifyEvent(Event.ACTION_INFO_STOP_LOADING), 2000);
            } else {
                G.w("current position = %s is invalid", currentPosition);
            }
        };
        Broadcaster.getInstance().listenEvent(Event.ACTION_INFO_START_LOADING, loadingRunnable);

        initCategoryHeader();
        initPagerAnimator();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPagerAnimator() {
        final View headerImageAnimator = findViewById(R.id.image_view_animator);
        viewPager.setAdapter(new ViewPagerAdapter(createRecyclerViewList()));

        final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                G.d("onFling, e1.x=%s, e2.x=%s, velocityX = %s, velocityY = %s", e1.getX(), e2.getX(), velocityX, velocityY);
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

        headerImageAnimator.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        final ViewAnimator viewAnimator = (ViewAnimator) headerImageAnimator;
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
        CenterScaleIndicator indicator = findViewById(R.id.tab_indicator);
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
    private void initCategoryHeader() {
        initSingleCategoryHeader(R.id.top_news, R.drawable.ic_news, "#008EFF", "#193BC3");
        initSingleCategoryHeader(R.id.calendar, R.drawable.ic_calendar, "#5739EE", "#4225AA");
        initSingleCategoryHeader(R.id.stock_change, R.drawable.ic_signal, "#1EC9BC", "#005EB4");
        initSingleCategoryHeader(R.id.research, R.drawable.ic_report, "#C467F7", "#6919EB");
        initSingleCategoryHeader(R.id.micro_report, R.drawable.ic_search, "#FE6B4B", "#AB2525");
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

    private void initSingleCategoryHeader(@IdRes int idRes, @DrawableRes int drawableRes, String startColor, String endColor) {
        View view = findViewById(idRes);
        final int start = Color.parseColor(startColor);
        final int end = Color.parseColor(endColor);
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end});
        view.setBackground(gradient);
        ImageView imageView = view.findViewById(R.id.image_icon);
        imageView.setImageResource(drawableRes);
    }


    public List<View> createRecyclerViewList() {
        ArrayList<View> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MyAdapter(50));
            recyclerView.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background_color));
            list.add(recyclerView);
        }
        return list;
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        private List<String> modelList = new ArrayList<>();

        private MyAdapter(int count) {
            for (int i = 1; i <= count; i++) {
                modelList.add(getString(R.string.app_name) + i);
            }
        }

        @Override
        @NonNull
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyHolder(getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            holder.titleText.setText(modelList.get(position));
        }

        @Override
        public int getItemCount() {
            return modelList.size();
        }
    }


    private class MyHolder extends RecyclerView.ViewHolder {

        final TextView titleText;

        private MyHolder(final View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(android.R.id.text1);
            this.titleText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        }
    }

    private class ViewPagerAdapter extends PagerAdapter {

        private final List<View> viewList;

        private ViewPagerAdapter(List<View> viewList) {
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
                    return "News";
                case 1:
                    return "Calendar";
                case 2:
                    return "Signal";
                case 3:
                    return "Report";
                case 4:
                    return "Research";
                default:
                    return "Title " + position;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Broadcaster.getInstance().removeRunnable(loadingRunnable);
    }

}
