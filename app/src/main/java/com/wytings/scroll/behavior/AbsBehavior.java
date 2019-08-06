package com.wytings.scroll.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.wytings.special.R;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

/**
 * Created by Rex.Wei on 2019-08-06
 *
 * @author weiyuting
 */

public abstract class AbsBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private final int dependencyCollapseHeight, dependencyInitHeight;
    private final Context context;
    private final int dependencyId;
    View dependentView;
    private CoordinatorLayout coordinatorLayout;

    AbsBehavior(Context context, AttributeSet attrs, int dependencyId) {
        super(context, attrs);
        this.dependencyId = dependencyId;
        this.context = context;
        dependencyInitHeight = dp(220);
        dependencyCollapseHeight = dp(68 + 40);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        if (dependency != null && dependency.getId() == dependencyId) {
            dependentView = dependency;
            this.coordinatorLayout = parent;
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    public Context getContext() {
        return context;
    }

    public int getDependencyCollapseHeight() {
        return dependencyCollapseHeight;
    }

    public int getDependencyInitHeight() {
        return dependencyInitHeight;
    }

    boolean isAutoScrolling() {
        if (dependentView == null) {
            return false;
        } else {
            final Object isAutoScrolling = dependentView.getTag(R.id.tag_is_auto_scrolling);
            if (isAutoScrolling instanceof Boolean) {
                return (boolean) isAutoScrolling;
            }
            return false;
        }
    }

    void setAutoScrolling(boolean isAutoScrolling) {
        if (dependentView != null) {
            dependentView.setTag(R.id.tag_is_auto_scrolling, isAutoScrolling);
        }
    }

    int dp(int dp) {
        return (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    void setViewParamsHeight(View view, int height) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }

        params.height = height;
        view.setLayoutParams(params);
    }

    int getDependencyHeight() {
        final ViewGroup.LayoutParams params = dependentView.getLayoutParams();
        if (params == null) {
            return 0;
        }
        return dependentView.getLayoutParams().height;
    }

}
