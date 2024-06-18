package com.androidx.utils;

import android.text.format.DateFormat;

import java.util.Date;
import java.util.Locale;

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

    public static String formatTimestamp(long timeInMillis) {
        if (timeInMillis <= 0) {
            return "";
        }
        java.text.DateFormat instance = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG,
                java.text.DateFormat.MEDIUM,
                Locale.getDefault());
        return instance.format(new Date(timeInMillis));
    }


    /**
     * icu下的android.icu.text.DateFormat 和 java.text.DateFormat
     *
     * @param timeInMillis
     * @return
     */
    public static String formatDateInMultiLanguage(Long timeInMillis) {
        if (timeInMillis <= 0) {
            return "";
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            android.icu.text.DateFormat df = android.icu.text.DateFormat.getDateTimeInstance(android.icu.text.DateFormat.LONG,
//                    android.icu.text.DateFormat.MEDIUM, Locale.getDefault());
//            return df.format(new Date(timeMs));
//        } else {
//            java.text.DateFormat instance = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG,
//                    java.text.DateFormat.LONG,
//                    Locale.getDefault());
//            return instance.format(new Date(timeMs));
//        }

        java.text.DateFormat instance = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG,
                java.text.DateFormat.MEDIUM,
                Locale.getDefault());
        return instance.format(new Date(timeInMillis));
    }
}
