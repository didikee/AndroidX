package com.androidx

import java.text.DecimalFormat

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
}