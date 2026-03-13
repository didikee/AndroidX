package com.androidx

import android.content.Context
import android.text.format.DateFormat
import android.text.format.Formatter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * user author: didikee
 * create time: 2025/11/28 13:00
 * description:
 */
object FormatX {
    // 保留1位小数或者显示整数
    fun formatFloat(value: Float): String {
        val df = DecimalFormat("0.#") // 保留 1 位小数，如果是 0 则省略
        return df.format(value)
    }

    fun formatFloat2(value: Float): String {
        val df = DecimalFormat("0.##") // 保留 2 位小数，如果是 0 则省略
        return df.format(value)
    }

    // 保留 1 位小数，如果是 0 则省略
    fun formatDouble(value: Double): String {
        val df = DecimalFormat("0.#")
        return df.format(value)
    }

    // 保留 2 位小数，如果是 0 则省略
    fun formatDouble2(value: Double): String {
        val df = DecimalFormat("0.##")
        return df.format(value)
    }

    fun formatFileSize(context: Context, size: Long): String {
        return Formatter.formatFileSize(context, size)
    }

    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = size.toDouble()
        var index = 0

        while (value >= 1024 && index < units.lastIndex) {
            value /= 1024
            index++
        }
        return String.format(Locale.US, "%s %s", formatDouble(value), units[index])
    }

    /**
     *    地区	显示
     * 🇺🇸 美国	12/29/2025
     * 🇬🇧 英国	29/12/2025
     * 🇨🇳 中国	2025-12-29
     * 🇯🇵 日本	2025/12/29
     * 🇫🇷 法国	29/12/2025
     */
    fun formatDateAuto(context: Context, timeMillis: Long): String {
        val dateFormat = DateFormat.getDateFormat(context)
        val text = dateFormat.format(Date(timeMillis))
        return text
    }

    fun formatTimeAuto(context: Context, timeMillis: Long): String {
        val timeFormat = DateFormat.getTimeFormat(context)
        val text = timeFormat.format(Date(timeMillis))
        return text
    }

//    fun formatDateTimeDetailAuto(context: Context, timeMillis: Long): String {
//        val dateTimeFormat = DateFormat.getMediumDateFormat(
//            DateFormat.MEDIUM,
//            DateFormat.SHORT
//        )
//
//        val text = dateTimeFormat.format(Date(timeMillis))
//
//        return text
//    }

    // 格式化展示名称，不显示拓展名（如果有的话）
    fun formatDisplayName(displayName: String): String {
        if (displayName.isBlank()) return displayName

        val lastDotIndex = displayName.lastIndexOf('.')

        return if (lastDotIndex > 0) {
            displayName.substring(0, lastDotIndex)
        } else {
            displayName
        }
    }

    fun formatNow(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateTime = dateFormat.format(Date())
        return dateTime
    }


}