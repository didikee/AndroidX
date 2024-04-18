package com.androidx.media

import android.text.TextUtils

/**
 *
 * description:
 */
object ExifUtils {
    /**
     * dmsFraction: 31/1,10/1,2639/100
     * output: 41.40338
     */
    fun convertDMSFractionToDecimal(dmsFraction: String?): Double {
        if (dmsFraction!=null && dmsFraction.contains(",")) {
            try {
                val parts = dmsFraction.split(",")
                val degreesStr = parts[0]
                val minutesStr = parts[1]
                val secondsStr = parts[2]
                return calculateFraction(degreesStr) + calculateFraction(minutesStr) / 60.0 + calculateFraction(secondsStr) / 3600.0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return 0.0
    }

    private fun calculateFraction(text: String): Double {
        try {
            if (text.contains("/")) {
                val split = text.split("/")
                if (split.size == 2) {
                    return split[0].toInt() / split[1].toInt() * 1.0
                }
            } else {
                return text.toInt() * 1.0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0.0
    }
}