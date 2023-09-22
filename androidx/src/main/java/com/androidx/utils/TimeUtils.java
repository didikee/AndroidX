package com.androidx.utils;

import android.text.format.DateFormat;

/**
 * description: 关于时间的工具类
 */
public final class TimeUtils {

    public static String DATE_FORMAT_1 = "yyyyMMdd_HHmmss";

    public static CharSequence formatCurrentTime() {
        return DateFormat.format(DATE_FORMAT_1, System.currentTimeMillis());
    }

    public static CharSequence formatCurrentTime(CharSequence format) {
        return DateFormat.format(format, System.currentTimeMillis());
    }

    public static CharSequence format(CharSequence format, long timeInMillis) {
        return DateFormat.format(format, timeInMillis);
    }
}
