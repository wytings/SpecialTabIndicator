package com.wytings.special.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

/**
 * Created by Rex.Wei on 2019-08-07
 *
 * @author weiyuting
 */
public class ContextUtils {

    public static int dp(Context context, float dp) {
        return (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int[] parseMinDefaultMax(Context context, AttributeSet attrs) {
        final String tag = ContextUtils.getTag(context, attrs);
        return ContextUtils.parseMinDefaultMax(tag);
    }

    public static String getTag(Context context, AttributeSet attrs) {
        final int[] arr = new int[]{android.R.attr.tag};
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, arr);
        final int tagIndex = typedArray.getIndex(0);
        final String tag = typedArray.getString(tagIndex);
        typedArray.recycle();
        return tag;
    }

    public static boolean isMaxDragEnabled(String tag) {
        final String[] configs = tag.split(";");
        if (configs.length == 0) {
            throw new IllegalArgumentException("invalid tag = " + tag);
        }
        for (String config : configs) {
            final String[] values = config.split(":");
            if ("max_enabled".equals(values[0])) {
                return Boolean.parseBoolean(values[1]);
            }
        }
        return false;
    }

    public static int[] parseMinDefaultMax(String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("android:tag is empty ");
        }

        final String[] configs = tag.split(";");
        if (configs.length == 0) {
            throw new IllegalArgumentException("invalid tag = " + tag);
        }
        final int[] minDefaultMax = {-1, -1, -1};
        for (String config : configs) {
            final String[] values = config.split(":");
            final String key = values[0];
            final String value = values[1];
            if ("min".equals(key)) {
                minDefaultMax[0] = Integer.parseInt(value);
            } else if ("default".equals(key)) {
                minDefaultMax[1] = Integer.parseInt(value);
            } else if ("max".equals(key)) {
                minDefaultMax[2] = Integer.parseInt(value);
            }
        }

        if (minDefaultMax[0] == -1 || minDefaultMax[1] == -1 || minDefaultMax[2] == -1) {
            throw new IllegalArgumentException("invalid tag = " + tag);
        }
        return minDefaultMax;

    }

}
