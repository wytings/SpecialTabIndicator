package com.wytings.special.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */


public final class ViewUtils {

    public static void transparentStatusBar(Activity activity) {
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

    public static int getViewLayoutParamsHeight(View view) {

        final ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return 0;
        }
        return view.getLayoutParams().height;
    }

    public static void setViewLayoutParamsHeight(View view, int height) {

        final ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }

        params.height = height;
        view.setLayoutParams(params);
    }

}
