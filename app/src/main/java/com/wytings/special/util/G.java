package com.wytings.special.util;

import android.util.Log;

import java.util.Locale;

/**
 * Created by Rex on 2018/03/07.
 * https://github.com/wytings
 */

public final class G {

    private static final String TAG = "wytings";

    public static void d(String msg, Object... args) {
        Log.d(TAG, String.format(Locale.CHINA, msg, args));
    }

    public static void w(String msg, Object... args) {
        Log.w(TAG, String.format(Locale.CHINA, msg, args));
    }

    public static void e(String msg, Object... args) {
        Log.e(TAG, String.format(Locale.CHINA, msg, args));
    }
}
