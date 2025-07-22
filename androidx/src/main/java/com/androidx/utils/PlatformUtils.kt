package com.androidx.utils

import android.os.Build

/**
 * user author: didikee
 * create time: 2025/7/1 上午8:30
 * description:
 */
object PlatformUtils {
    const val NAME_VIVO = "vivo"
    const val NAME_IQOO = "iqoo"

    // 检测是否为vivo设备
    fun isVivoDevice(): Boolean {
        return Build.MANUFACTURER.equals(NAME_VIVO, ignoreCase = true) ||
                Build.BRAND.equals(NAME_VIVO, ignoreCase = true)
    }

    // 检测是否为vivo设备,此处包含子品牌
    fun isVivoOrSubBrand(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        return manufacturer == NAME_VIVO ||
                brand == NAME_VIVO ||
                manufacturer == NAME_IQOO ||
                brand == NAME_IQOO
    }
}