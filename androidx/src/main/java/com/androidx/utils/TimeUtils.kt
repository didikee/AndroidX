package com.androidx.utils

import android.text.format.DateFormat
import java.util.Date
import java.util.Locale

/**
 * description: 关于时间的工具类
 */
object TimeUtils {
    @JvmField
    var DATE_FORMAT_1: String = "yyyyMMdd_HHmmss"

    fun formatCurrentTime(): CharSequence {
        return DateFormat.format(DATE_FORMAT_1, System.currentTimeMillis())
    }

    fun formatCurrentTime(format: CharSequence?): CharSequence {
        return DateFormat.format(format, System.currentTimeMillis())
    }

    @JvmStatic
    fun format(format: CharSequence?, timeInMillis: Long): CharSequence {
        return DateFormat.format(format, timeInMillis)
    }

    fun formatTimestamp(timeInMillis: Long): String {
        if (timeInMillis <= 0) {
            return ""
        }
        val instance = java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.LONG,
            java.text.DateFormat.MEDIUM,
            Locale.getDefault()
        )
        return instance.format(Date(timeInMillis))
    }


    /**
     * icu下的android.icu.text.DateFormat 和 java.text.DateFormat
     *
     * @param timeInMillis
     * @return
     */
    fun formatDateInMultiLanguage(timeInMillis: Long): String {
        if (timeInMillis <= 0) {
            return ""
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
        val instance = java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.LONG,
            java.text.DateFormat.MEDIUM,
            Locale.getDefault()
        )
        return instance.format(Date(timeInMillis))
    }

    /**
     * 你想要的格式是：
     * 小于 1 小时：MM:SS.s
     * 大于等于 1 小时：HH:MM:SS.s
     * 即：只保留一位小数秒数（精确到 100 毫秒）。
     */
    fun formatTimeHHMMSSs(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val tenths = (milliseconds % 1000) / 100  // 保留一位小数（0~9）

        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d.%d", hours, minutes, seconds, tenths)
        } else {
            String.format(Locale.US, "%02d:%02d.%d", minutes, seconds, tenths)
        }
    }

    /**
     * 格式化时间
     * @param millsSeconds 毫秒
     * @return 比如：11:45:33.4
     * 或者：9:45:33.4
     * 或者：45:33.4
     */
    fun formatTimeMSs(millsSeconds: Long): String {
        val seconds = millsSeconds / 1000
        val s = seconds % 60
        val m = (seconds / 60) % 60
        val h = (seconds / (60 * 60)) % 24
        // 毫秒处理 0.3s这样的
        val ms = ((millsSeconds % 1000) / 100f + 0.0f).toInt()
        //        if (ms >= 10) {
//            s += 1;
//            ms = 0;
//        }
        return if (h > 0) {
            if (h > 9) {
                String.format(Locale.ENGLISH, "%02d:%02d:%02d.%d", h, m, s, ms)
            } else {
                String.format(Locale.ENGLISH, "%d:%02d:%02d.%d", h, m, s, ms)
            }
        } else {
            String.format(Locale.ENGLISH, "%02d:%02d.%d", m, s, ms)
        }
    }

    fun formatTimeMS(millsSeconds: Long): String {
        val seconds = millsSeconds / 1000
        val s = seconds % 60
        val m = (seconds / 60) % 60
        val h = (seconds / (60 * 60)) % 24
        return if (h > 0) {
            if (h > 9) {
                String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)
            } else {
                String.format(Locale.ENGLISH, "%d:%02d:%02d", h, m, s)
            }
        } else {
            String.format(Locale.ENGLISH, "%02d:%02d", m, s)
        }
    }

    fun formatTimeSs(millsSeconds: Long): String {
        val seconds = millsSeconds / 1000
        // 毫秒处理 0.3s这样的
        val ms = ((millsSeconds % 1000) / 100f + 0.0f).toInt()
        val length = seconds.toString().length
        return if (length == 1) {
            String.format(Locale.ENGLISH, "%d.%d", seconds, ms)
        } else if (length == 2) {
            String.format(Locale.ENGLISH, "%02d.%d", seconds, ms)
        } else if (length == 3) {
            String.format(Locale.ENGLISH, "%03d.%d", seconds, ms)
        } else if (length == 4) {
            String.format(Locale.ENGLISH, "%04d.%d", seconds, ms)
        } else {
            String.format(Locale.ENGLISH, "%05d.%d", seconds, ms)
        }
    }


    /**
     * 格式化
     * @param millsSeconds 毫秒
     * @return 比如：11:45:33
     * 或者：9:45:33
     */
    fun formatTimeHMS(millsSeconds: Long): String {
        val seconds = millsSeconds / 1000
        val s = seconds % 60
        val m = (seconds / 60) % 60
        val h = (seconds / (60 * 60)) % 24
        return if (h > 0) {
            if (h > 9) {
                String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)
            } else {
                String.format(Locale.ENGLISH, "%d:%02d:%02d", h, m, s)
            }
        } else {
            String.format(Locale.ENGLISH, "%02d:%02d", m, s)
        }
    }

    /**
     * ffmpeg 支持的时间格式：HH:MM:SS.xxx
     * @param millsSeconds
     * @return
     */
    fun formatTimeForFfmpeg(millsSeconds: Long): String {
        val seconds = millsSeconds / 1000
        val mills = millsSeconds % 1000
        val s = seconds % 60
        val m = (seconds / 60) % 60
        val h = (seconds / (60 * 60)) % 24
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d.%03d", h, m, s, mills)
    }

}
